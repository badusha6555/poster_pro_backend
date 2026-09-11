package com.posterpro.api.user;

import com.posterpro.api.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final long MAX_LOGO_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_LOGO_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${storage.s3.public-base-url}")
    private String publicBaseUrl;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String email) {
        return toDto(findUser(email));
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = findUser(email);
        if (request.getShopName() != null) {
            if (!StringUtils.hasText(request.getShopName())) {
                throw new IllegalArgumentException("Shop name cannot be blank");
            }
            user.setShopName(request.getShopName());
        }
        userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public UserProfileDto uploadLogo(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Logo file is required");
        }
        if (file.getSize() > MAX_LOGO_SIZE_BYTES) {
            throw new IllegalArgumentException("Logo image must be 5MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_LOGO_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Logo must be a JPG or PNG image");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded logo file", e);
        }

        User user = findUser(email);
        // Delete the previous logo first, regardless of its extension, so a
        // format change on re-upload (e.g. png -> jpg) can't leave an
        // orphaned file behind in storage.
        if (StringUtils.hasText(user.getLogoUrl())) {
            storageService.delete(user.getLogoUrl());
        }

        String extension = "image/png".equalsIgnoreCase(contentType) ? "png" : "jpg";
        String key = "users/%d/logo.%s".formatted(user.getId(), extension);
        storageService.upload(key, bytes, contentType);
        user.setLogoUrl(key);
        userRepository.save(user);

        log.info("Shop logo uploaded for user {} ({} bytes)", user.getId(), bytes.length);
        return toDto(user);
    }

    @Transactional
    public UserProfileDto deleteLogo(String email) {
        User user = findUser(email);
        if (StringUtils.hasText(user.getLogoUrl())) {
            storageService.delete(user.getLogoUrl());
            user.setLogoUrl(null);
            userRepository.save(user);
            log.info("Shop logo removed for user {}", user.getId());
        }
        return toDto(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private UserProfileDto toDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getShopName(),
                user.getShopPhone(),
                user.getShopAddress(),
                user.getBusinessType(),
                resolveLogoUrl(user.getLogoUrl()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * `users.logo_url` stores a bucket-relative MinIO object key, resolved
     * against storage.s3.public-base-url at read-time — same pattern as
     * TemplateService#resolveThumbnailUrl.
     */
    private String resolveLogoUrl(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return publicBaseUrl + "/" + bucket + "/" + key;
    }
}
