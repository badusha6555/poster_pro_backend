CREATE TABLE gold_rate_profiles (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    rate_22k916 NUMERIC(10, 2),
    rate_18k    NUMERIC(10, 2),
    rate_14k    NUMERIC(10, 2),
    rate_9k     NUMERIC(10, 2),
    updated_at  TIMESTAMP DEFAULT NOW()
);
