package com.posterpro.api.poster;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posterpro.api.download.Download;
import com.posterpro.api.download.DownloadFormat;
import com.posterpro.api.download.DownloadRepository;
import com.posterpro.api.goldrate.GoldRateProfile;
import com.posterpro.api.goldrate.GoldRateRepository;
import com.posterpro.api.storage.StorageService;
import com.posterpro.api.template.Template;
import com.posterpro.api.template.TemplateRepository;
import com.posterpro.api.user.User;
import com.posterpro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosterService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final GoldRateRepository goldRateRepository;
    private final DownloadRepository downloadRepository;
    private final StorageService storageService;
    private final FontRegistry fontRegistry;
    private final ObjectMapper objectMapper;

    @Transactional
    public PosterResponse generate(Long templateId, String email, PosterGenerateRequest request, DownloadFormat format) {
        if (format == DownloadFormat.PDF) {
            throw new IllegalArgumentException("PDF export isn't supported yet — request PNG or JPG.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        Template template = templateRepository.findById(templateId)
                .filter(Template::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("Template not found: " + templateId));

        TemplateSchema schema = parseSchema(template);
        boolean hasLayout = schema != null && schema.placeholders() != null && !schema.placeholders().isEmpty();
        boolean hasBackground = StringUtils.hasText(template.getBackgroundImageKey())
                && storageService.exists(template.getBackgroundImageKey());
        if (!hasLayout || !hasBackground) {
            throw new PosterDesignNotReadyException(templateId);
        }

        GoldRateProfile profile = goldRateRepository.findByUser(user).orElse(null);

        BufferedImage canvas;
        try {
            canvas = compose(template.getBackgroundImageKey(), schema, user, profile, request, templateId);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to compose poster for template " + templateId, e);
        }

        byte[] encoded;
        try {
            encoded = encode(canvas, format);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode poster for template " + templateId, e);
        }

        String key = "generated/%d/%d/%d.%s".formatted(
                user.getId(), templateId, System.currentTimeMillis(), format.name().toLowerCase());
        storageService.upload(key, encoded, contentType(format));

        Download download = new Download();
        download.setUser(user);
        download.setTemplate(template);
        download.setFormat(format);
        downloadRepository.save(download);

        String url = storageService.presignedUrl(key);
        return new PosterResponse(url, format.name(), LocalDateTime.now().plusHours(1));
    }

    private TemplateSchema parseSchema(Template template) {
        String json = template.getSchemaJson();
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, TemplateSchema.class);
        } catch (JsonProcessingException e) {
            log.warn("Malformed schema_json on template {}", template.getId(), e);
            return null;
        }
    }

    private BufferedImage compose(String backgroundKey, TemplateSchema schema, User user,
                                   GoldRateProfile profile, PosterGenerateRequest request, Long templateId) throws IOException {
        byte[] bgBytes = storageService.download(backgroundKey);
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(bgBytes));
        if (source == null) {
            throw new IOException("Could not decode background image for key " + backgroundKey);
        }

        BufferedImage canvas = new BufferedImage(schema.canvasWidth(), schema.canvasHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, schema.canvasWidth(), schema.canvasHeight(), null);

        for (TemplateSchema.Placeholder p : schema.placeholders()) {
            try {
                switch (p.type()) {
                    case "text" -> drawText(g, p, user, profile, request);
                    case "image" -> drawImage(g, p, user);
                    default -> log.warn("Unknown placeholder type '{}' on template {}", p.type(), templateId);
                }
            } catch (Exception e) {
                log.warn("Skipping placeholder field='{}' on template {}: {}", p.field(), templateId, e.getMessage());
            }
        }
        g.dispose();
        return canvas;
    }

    private void drawText(Graphics2D g, TemplateSchema.Placeholder p, User user,
                           GoldRateProfile profile, PosterGenerateRequest request) {
        String value = resolveTextValue(p.field(), user, profile, request);
        if (!StringUtils.hasText(value)) {
            return;
        }

        float fontSize = p.fontSize() != null ? p.fontSize() : 24f;
        Font font = fontRegistry.resolve(p.fontFamily(), fontSize);
        g.setFont(font);
        g.setColor(parseColor(p.color()));

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(value);
        int x = switch (p.align() != null ? p.align() : "left") {
            case "center" -> p.x() - textWidth / 2;
            case "right" -> p.x() - textWidth;
            default -> p.x();
        };
        g.drawString(value, x, p.y());
    }

    private String resolveTextValue(String field, User user, GoldRateProfile profile, PosterGenerateRequest request) {
        return switch (field) {
            case "shopName" -> user.getShopName();
            case "rate22k916" -> formatRate(pickRate(request.getRate22k916(), profile != null ? profile.getRate22k916() : null));
            case "rate18k" -> formatRate(pickRate(request.getRate18k(), profile != null ? profile.getRate18k() : null));
            case "rate14k" -> formatRate(pickRate(request.getRate14k(), profile != null ? profile.getRate14k() : null));
            case "rate9k" -> formatRate(pickRate(request.getRate9k(), profile != null ? profile.getRate9k() : null));
            default -> {
                log.warn("Unknown text placeholder field '{}'", field);
                yield null;
            }
        };
    }

    private BigDecimal pickRate(BigDecimal override, BigDecimal saved) {
        return override != null ? override : saved;
    }

    private static final DecimalFormat RATE_FORMAT = new DecimalFormat("#,##0.##");

    private String formatRate(BigDecimal rate) {
        return rate != null ? "₹" + RATE_FORMAT.format(rate) : null;
    }

    private void drawImage(Graphics2D g, TemplateSchema.Placeholder p, User user) throws IOException {
        if (!"shopLogo".equals(p.field())) {
            log.warn("Unknown image placeholder field '{}'", p.field());
            return;
        }
        String logoKey = user.getLogoUrl();
        if (!StringUtils.hasText(logoKey)) {
            return;
        }
        byte[] logoBytes;
        try {
            logoBytes = storageService.download(logoKey);
        } catch (Exception e) {
            log.warn("Shop logo key '{}' could not be loaded, skipping logo placeholder: {}", logoKey, e.getMessage());
            return;
        }
        BufferedImage logo = ImageIO.read(new ByteArrayInputStream(logoBytes));
        if (logo == null) {
            log.warn("Shop logo key '{}' did not decode as an image, skipping logo placeholder", logoKey);
            return;
        }
        int width = p.width() != null ? p.width() : logo.getWidth();
        int height = p.height() != null ? p.height() : logo.getHeight();
        g.drawImage(logo, p.x(), p.y(), width, height, null);
    }

    private Color parseColor(String hex) {
        if (!StringUtils.hasText(hex)) {
            return Color.BLACK;
        }
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            log.warn("Invalid color '{}', defaulting to black", hex);
            return Color.BLACK;
        }
    }

    private byte[] encode(BufferedImage canvas, DownloadFormat format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (format == DownloadFormat.JPG) {
            BufferedImage rgb = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(canvas, 0, 0, null);
            g.dispose();
            ImageIO.write(rgb, "jpg", out);
        } else {
            ImageIO.write(canvas, "png", out);
        }
        return out.toByteArray();
    }

    private String contentType(DownloadFormat format) {
        return format == DownloadFormat.JPG ? "image/jpeg" : "image/png";
    }
}
