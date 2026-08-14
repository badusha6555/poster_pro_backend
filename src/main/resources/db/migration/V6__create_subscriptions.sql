CREATE TABLE subscriptions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_tier  VARCHAR(20) NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    starts_at  TIMESTAMP NOT NULL,
    ends_at    TIMESTAMP NOT NULL
);

CREATE INDEX idx_subscriptions_user   ON subscriptions (user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions (status);
