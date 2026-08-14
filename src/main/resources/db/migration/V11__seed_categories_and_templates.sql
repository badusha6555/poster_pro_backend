INSERT INTO categories (name, slug, icon_url, sort_order) VALUES
    ('Rings', 'rings', 'https://example.com/icons/rings.png', 1),
    ('Earrings', 'earrings', 'https://example.com/icons/earrings.png', 2),
    ('Necklaces', 'necklaces', 'https://example.com/icons/necklaces.png', 3),
    ('Bangles', 'bangles', 'https://example.com/icons/bangles.png', 4);

INSERT INTO templates (category_id, title, thumbnail_url, price, schema_json, is_festival, is_active, plan_tier_min) VALUES
    ((SELECT id FROM categories WHERE slug = 'rings'), 'Classic Gold Solitaire Ring', 'https://example.com/thumbs/ring1.png', 42800.00, '{}', false, true, 'FREE'),
    ((SELECT id FROM categories WHERE slug = 'earrings'), 'Diamond Drop Earrings', 'https://example.com/thumbs/earring1.png', 28500.00, '{}', false, true, 'BASIC'),
    ((SELECT id FROM categories WHERE slug = 'necklaces'), 'Bridal Gold Necklace Set', 'https://example.com/thumbs/necklace1.png', 155000.00, '{}', true, true, 'PREMIUM'),
    ((SELECT id FROM categories WHERE slug = 'bangles'), 'Antique Bangle Pair', 'https://example.com/thumbs/bangle1.png', 61200.00, '{}', false, true, 'FREE');
