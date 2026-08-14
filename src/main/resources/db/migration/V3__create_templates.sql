CREATE TABLE templates (
    id            BIGSERIAL PRIMARY KEY,
    category_id   BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    title         VARCHAR(255) NOT NULL,
    thumbnail_url TEXT,
    schema_json   JSONB,
    is_festival   BOOLEAN DEFAULT FALSE,
    festival_date DATE,
    is_active     BOOLEAN DEFAULT TRUE,
    plan_tier_min VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_templates_schema_gin  ON templates USING GIN (schema_json);
CREATE INDEX idx_templates_category    ON templates (category_id);
CREATE INDEX idx_templates_plan_tier   ON templates (plan_tier_min);
CREATE INDEX idx_templates_is_active   ON templates (is_active);
