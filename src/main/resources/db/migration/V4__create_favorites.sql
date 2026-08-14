CREATE TABLE favorites (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, template_id)
);
