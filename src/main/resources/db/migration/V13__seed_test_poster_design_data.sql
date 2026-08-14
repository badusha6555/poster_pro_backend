-- Test/dev-only design data for ONE seeded template ("Classic Gold Solitaire Ring")
-- so poster generation can be exercised end-to-end. The other seeded templates
-- intentionally still have schema_json = '{}' and no background_image_key —
-- populating the full catalogue with real design assets is a separate,
-- out-of-scope task. See PROJECT_STATUS_REVIEW.md.
UPDATE templates
SET background_image_key = 'templates/backgrounds/classic-gold-solitaire-ring.png',
    schema_json = '{
        "canvasWidth": 1080,
        "canvasHeight": 1350,
        "placeholders": [
            { "type": "text", "field": "shopName", "x": 60, "y": 1180, "fontSize": 42, "fontFamily": "playfair", "color": "#F5D061", "align": "left" },
            { "type": "text", "field": "rate22k916", "x": 60, "y": 1235, "fontSize": 30, "fontFamily": "inter", "color": "#FFFFFF", "align": "left" },
            { "type": "text", "field": "rate18k", "x": 60, "y": 1270, "fontSize": 24, "fontFamily": "inter", "color": "#FFFFFF", "align": "left" },
            { "type": "image", "field": "shopLogo", "x": 900, "y": 1150, "width": 120, "height": 120 }
        ]
    }'
WHERE title = 'Classic Gold Solitaire Ring';
