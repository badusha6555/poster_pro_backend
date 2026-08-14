package com.posterpro.api.poster;

import java.util.List;

/**
 * Parsed shape of templates.schema_json — see docs/TEMPLATE_SCHEMA_JSON.md.
 */
public record TemplateSchema(int canvasWidth, int canvasHeight, List<Placeholder> placeholders) {

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
            Integer height
    ) {}
}
