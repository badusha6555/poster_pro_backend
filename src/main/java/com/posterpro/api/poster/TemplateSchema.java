package com.posterpro.api.poster;

import java.util.List;

/**
 * Parsed shape of templates.schema_json — see docs/TEMPLATE_SCHEMA_JSON.md.
 */
public record TemplateSchema(int canvasWidth, int canvasHeight, List<Placeholder> placeholders) {

    /**
     * showCurrencySymbol defaults to true (draw "₹1,234") when null/absent.
     * Set false on templates whose background image already has its own ₹
     * glyph baked in next to the placeholder position, so the app only
     * draws the number.
     */
    public record Placeholder(
            String type,
            String field,
            int x,
            int y,
            Integer fontSize,
            String fontFamily,
            String color,
            String align,
            Integer width,
            Integer height,
            Boolean showCurrencySymbol
    ) {}
}
