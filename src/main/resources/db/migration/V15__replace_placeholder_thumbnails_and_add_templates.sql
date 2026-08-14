-- Replaces remaining placeholder thumbnails with real images uploaded to MinIO
-- under the public-read templates/ prefix, and adds new template rows for
-- clean jewellery photos that didn't match any existing template's title.
--
-- Images are sourced from the local "poster templates" WhatsApp export folder.
-- Excluded: one watermarked stock photo (etsy.com watermark). No bangle/bracelet
-- photos were available, so id=4 ("Antique Bangle Pair") intentionally keeps its
-- example.com placeholder here. id=3 ("Bridal Gold Necklace Set") also keeps its
-- placeholder — the only available necklace-category photo (a simple heart
-- pendant) doesn't genuinely match a "bridal set" and is added as its own
-- template (id 11) instead. id=2 ("Diamond Drop Earrings") already has a real
-- thumbnail from a previous migration and is untouched here.

-- id=1: Classic Gold Solitaire Ring — oval diamond solitaire photo
UPDATE templates
SET thumbnail_url = 'http://localhost:9000/posterpro/templates/1/thumbnail.jpg'
WHERE title = 'Classic Gold Solitaire Ring';

-- New templates (ids assigned by templates_id_seq; current max is 4, so these
-- land as 5-11 in insertion order below)
INSERT INTO templates (category_id, title, thumbnail_url, price, schema_json, is_festival, is_active, plan_tier_min) VALUES
    -- id=5: rose gold round-diamond solitaire ring
    ((SELECT id FROM categories WHERE slug = 'rings'), 'Rose Gold Diamond Solitaire Ring', 'http://localhost:9000/posterpro/templates/5/thumbnail.jpg', 38000.00, '{}', false, true, 'FREE'),
    -- id=6: two hammered/textured gold rings
    ((SELECT id FROM categories WHERE slug = 'rings'), 'Twin Hammered Gold Ring Duo', 'http://localhost:9000/posterpro/templates/6/thumbnail.jpg', 35500.00, '{}', false, true, 'FREE'),
    -- id=7: hand wearing a rose gold pave ring
    ((SELECT id FROM categories WHERE slug = 'rings'), 'Rose Gold Pave Halo Ring', 'http://localhost:9000/posterpro/templates/7/thumbnail.jpg', 31000.00, '{}', false, true, 'FREE'),
    -- id=8: gold clover stud earrings
    ((SELECT id FROM categories WHERE slug = 'earrings'), 'Gold Clover Stud Earrings', 'http://localhost:9000/posterpro/templates/8/thumbnail.jpg', 19500.00, '{}', false, true, 'FREE'),
    -- id=9: pink/white enamel flower stud earrings
    ((SELECT id FROM categories WHERE slug = 'earrings'), 'Enamel Flower Stud Earrings', 'http://localhost:9000/posterpro/templates/9/thumbnail.jpg', 22000.00, '{}', false, true, 'FREE'),
    -- id=10: classic gold hoop earrings
    ((SELECT id FROM categories WHERE slug = 'earrings'), 'Classic Gold Hoop Earrings', 'http://localhost:9000/posterpro/templates/10/thumbnail.jpg', 24800.00, '{}', false, true, 'FREE'),
    -- id=11: gold heart pendant on a fine chain
    ((SELECT id FROM categories WHERE slug = 'necklaces'), 'Gold Heart Pendant Necklace', 'http://localhost:9000/posterpro/templates/11/thumbnail.jpg', 27500.00, '{}', false, true, 'BASIC');
