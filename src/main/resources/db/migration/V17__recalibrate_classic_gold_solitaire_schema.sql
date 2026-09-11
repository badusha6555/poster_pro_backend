-- id=1's background image was replaced (this session) with a new pre-branded
-- poster photo that has its own baked-in "22k916/18k/14k/9k" rate-box grid
-- (labels + underline only, values intentionally left blank) and a flat black
-- CTA bar near the bottom. The V13 schema_json was authored for a plain
-- product photo with no such layout and drew white/gold text into what turned
-- out to be a blank white footer band on the new image — invisible white text
-- on white background, confirmed by test-generating a poster and sampling
-- pixels.
--
-- Coordinates below were measured directly from the source JPEG (pixel color
-- analysis: cream box grid at source x:[67,241]/[250,424]/[432,606],
-- y:[658,736]/[753,831]; underline rows at source y=690/785; black CTA bar at
-- source x:[125,549] y:[859,899]) then scaled to the 1080x1350 canvas
-- (scaleX=1.6, scaleY=1.309408). Only 4 of the 6 boxes get a placeholder —
-- the "/8 GRAM" boxes have no corresponding field in GoldRateProfile, so
-- they're intentionally left blank rather than inventing data for them.
UPDATE templates
SET schema_json = '{
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
WHERE title = 'Classic Gold Solitaire Ring';
