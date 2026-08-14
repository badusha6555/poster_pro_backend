package com.posterpro.api.poster;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the bundled .ttf files (see src/main/resources/fonts/README.md) once at
 * startup and hands out sized derivatives by logical key ("playfair", "inter") —
 * the same key used in a template's schema_json.fontFamily.
 */
@Slf4j
@Component
public class FontRegistry {

    private static final Map<String, String> FONT_FILES = Map.of(
            "playfair", "fonts/PlayfairDisplay-Regular.ttf",
            "inter", "fonts/Inter-Regular.ttf"
    );

    private static final String FALLBACK_KEY = "inter";

    private final Map<String, Font> baseFonts = new ConcurrentHashMap<>();

    @PostConstruct
    void loadFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        FONT_FILES.forEach((key, path) -> {
            try (InputStream is = new ClassPathResource(path).getInputStream()) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                ge.registerFont(font);
                baseFonts.put(key, font);
                log.info("Registered font '{}' from {}", key, path);
            } catch (IOException | java.awt.FontFormatException e) {
                log.error("Failed to load bundled font '{}' from {}", key, path, e);
            }
        });
    }

    /**
     * Resolves a logical font key from schema_json to a sized Font, falling back
     * to the bundled sans-serif font if the key is unknown so a bad/unrecognized
     * fontFamily value degrades gracefully instead of failing generation.
     */
    public Font resolve(String key, float size) {
        Font base = baseFonts.get(key);
        if (base == null) {
            log.warn("Unknown fontFamily '{}', falling back to '{}'", key, FALLBACK_KEY);
            base = baseFonts.get(FALLBACK_KEY);
        }
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }
        return base.deriveFont(size);
    }
}
