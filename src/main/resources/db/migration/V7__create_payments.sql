CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id     BIGINT REFERENCES subscriptions(id) ON DELETE SET NULL,
    provider            VARCHAR(20) NOT NULL,
    provider_session_id VARCHAR(255),
    amount              NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_user            ON payments (user_id);
CREATE INDEX idx_payments_provider_session ON payments (provider_session_id);
