-- Adds background_image_key + schema_json for the 7 templates that got a
-- real background image uploaded this session (ids 5,6,7,8,9,10,11) but had
-- no layout yet. All 8 images (id=1 included, see V17) share one export size
-- (675x1031) and one pixel-identical box grid/button overlay — confirmed by
-- measuring box borders, underline rows, and (where present) the baked-in ₹
-- glyph position across every image before writing this migration — so the
-- same coordinate math applies to all of them; only the per-image style
-- differs:
--
--   Style A — filled cream box, blank value slot (ids 1, 7):
--     rate text drawn in dark text (#1A1206) below the underline, same
--     coordinates as V17's id=1 recalibration.
--
--   Style B — outlined/transparent box, value slot already has a white ₹
--     glyph baked in at source x:[96,113]/[470,486] (rows) x y:[699,718]/
--     [798,816] (ids 5, 6, 8, 9, 10, 11): rate text is drawn in white,
--     positioned to start just right of the existing glyph, with
--     "showCurrencySymbol": false so PosterService doesn't draw a second ₹
--     next to the one already in the image.
--
-- As with id=1, only 4 of the 6 boxes (22k916/1g, 18k/1g, 14k/1g, 9k/1g) get
-- a placeholder — the "/8 GRAM" boxes have no corresponding field in
-- GoldRateProfile, so they're intentionally left blank.

-- Style A: id=7 (same coordinates as id=1 / V17 — pixel-identical grid)
UPDATE templates
SET background_image_key = 'templates/backgrounds/rose-gold-pave-halo-ring.jpg',
    schema_json = '{
        "canvasWidth": 1080,
        "canvasHeight": 1350,
        "placeholders": [
            { "type": "image", "field": "shopLogo", "x": 40, "y": 40, "width": 100, "height": 100 },
            { "type": "text", "field": "shopName", "x": 224, "y": 1160, "fontSize": 26, "fontFamily": "playfair", "color": "#F5D061", "align": "left" },
            { "type": "text", "field": "rate22k916", "x": 146, "y": 938, "fontSize": 28, "fontFamily": "inter", "color": "#1A1206", "align": "left" },
            { "type": "text", "field": "rate18k", "x": 730, "y": 938, "fontSize": 28, "fontFamily": "inter", "color": "#1A1206", "align": "left" },
            { "type": "text", "field": "rate14k", "x": 438, "y": 1062, "fontSize": 28, "fontFamily": "inter", "color": "#1A1206", "align": "left" },
            { "type": "text", "field": "rate9k", "x": 730, "y": 1062, "fontSize": 28, "fontFamily": "inter", "color": "#1A1206", "align": "left" }
        ]
    }'
WHERE title = 'Rose Gold Pave Halo Ring';

-- Style B template, reused for all 6 rows below (showCurrencySymbol=false,
-- white text, positioned right after each box's existing ₹ glyph)
UPDATE templates
SET background_image_key = 'templates/backgrounds/rose-gold-diamond-solitaire-ring.jpg',
    schema_json = '{
        "canvasWidth": 1080,
        "canvasHeight": 1350,
        "placeholders": [
            { "type": "image", "field": "shopLogo", "x": 40, "y": 40, "width": 100, "height": 100 },
            { "type": "text", "field": "shopName", "x": 224, "y": 1160, "fontSize": 26, "fontFamily": "playfair", "color": "#F5D061", "align": "left" },
            { "type": "text", "field": "rate22k916", "x": 194, "y": 940, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate18k", "x": 790, "y": 940, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate14k", "x": 496, "y": 1068, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate9k", "x": 790, "y": 1068, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false }
        ]
    }'
WHERE title = 'Rose Gold Diamond Solitaire Ring';

UPDATE templates
SET background_image_key = 'templates/backgrounds/twin-hammered-gold-ring-duo.jpg',
    schema_json = (SELECT schema_json FROM templates WHERE title = 'Rose Gold Diamond Solitaire Ring')
WHERE title = 'Twin Hammered Gold Ring Duo';

UPDATE templates
SET background_image_key = 'templates/backgrounds/gold-clover-stud-earrings.jpg',
    schema_json = (SELECT schema_json FROM templates WHERE title = 'Rose Gold Diamond Solitaire Ring')
WHERE title = 'Gold Clover Stud Earrings';

UPDATE templates
SET background_image_key = 'templates/backgrounds/enamel-flower-stud-earrings.jpg',
    schema_json = (SELECT schema_json FROM templates WHERE title = 'Rose Gold Diamond Solitaire Ring')
WHERE title = 'Enamel Flower Stud Earrings';

UPDATE templates
SET background_image_key = 'templates/backgrounds/classic-gold-hoop-earrings.jpg',
    schema_json = (SELECT schema_json FROM templates WHERE title = 'Rose Gold Diamond Solitaire Ring')
WHERE title = 'Classic Gold Hoop Earrings';

UPDATE templates
SET background_image_key = 'templates/backgrounds/gold-heart-pendant-necklace.jpg',
    schema_json = (SELECT schema_json FROM templates WHERE title = 'Rose Gold Diamond Solitaire Ring')
WHERE title = 'Gold Heart Pendant Necklace';
