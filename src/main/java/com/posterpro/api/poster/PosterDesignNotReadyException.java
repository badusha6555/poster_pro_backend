package com.posterpro.api.poster;

/**
 * Thrown when a template has no usable design data yet (empty schema_json
 * and/or no background_image_key) — see V11 seed data, which ships every
 * template with schema_json = '{}'. Mapped to a 4xx in GlobalExceptionHandler
 * so callers get a clear reason instead of a blank/broken image.
 */
public class PosterDesignNotReadyException extends RuntimeException {
    public PosterDesignNotReadyException(Long templateId) {
        super("Template " + templateId + " has no design data yet (missing layout or background image) — it cannot generate a poster.");
    }
}
