-- Template id=11 "Gold Heart Pendant Necklace" background image has a 3x2
-- grid of 6 rate boxes baked in (row1: 22k916/1g, 22k916/8g, 18k/1g; row2:
-- 18k/8g, 14k/1g, 9k/1g), but V18 only added placeholders for 4 of them —
-- the two "/8 GRAM" boxes (22k916/8g, 18k/8g) were left blank because
-- GoldRateProfile has no matching column (see V18's comment). PosterService
-- and PosterGenerateRequest now accept rate22k8g/rate18k8g as request-only
-- overrides (no saved-profile fallback), so these can be wired up.
--
-- Coordinates were measured directly from the source JPEG (fetched from
-- MinIO at templates/backgrounds/gold-heart-pendant-necklace.jpg, 675x1031)
-- by locating each missing box's white "₹" glyph via pixel-brightness
-- scanning, the same method used for the 4 existing rate placeholders:
--   row1 col2 (22k916/8g) glyph: source x=[286,302] y=[699,718]
--   row2 col1 (18k/8g)     glyph: source x=[96,113]  y=[798,816]
-- Scaled to the 1080x1350 canvas (scaleX=1.6, scaleY=1.309408) and offset
-- past the glyph the same ~13px the other 4 placeholders use, these land
-- exactly on the existing rate14k (x=496) and rate22k916 (x=194) columns —
-- expected, since box columns are shared vertically between the two rows:
--   rate22k8g: x=496, y=940  (row1's y, column shared with rate14k)
--   rate18k8g: x=194, y=1068 (row2's y, column shared with rate22k916)
-- Style/color match the template's existing Style B rate placeholders
-- (white text, inter 26, left-aligned, showCurrencySymbol:false since the
-- ₹ glyph is already baked into the image).
UPDATE templates
SET schema_json = '{
        "canvasWidth": 1080,
        "canvasHeight": 1350,
        "placeholders": [
            { "type": "image", "field": "shopLogo", "x": 40, "y": 40, "width": 100, "height": 100 },
            { "type": "text", "field": "shopName", "x": 224, "y": 1160, "fontSize": 26, "fontFamily": "playfair", "color": "#F5D061", "align": "left" },
            { "type": "text", "field": "rate22k916", "x": 194, "y": 940, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate22k8g", "x": 496, "y": 940, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate18k", "x": 790, "y": 940, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate18k8g", "x": 194, "y": 1068, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate14k", "x": 496, "y": 1068, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false },
            { "type": "text", "field": "rate9k", "x": 790, "y": 1068, "fontSize": 26, "fontFamily": "inter", "color": "#FFFFFF", "align": "left", "showCurrencySymbol": false }
        ]
    }'
WHERE id = 11;
