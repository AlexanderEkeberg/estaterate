CREATE TABLE favourites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    analysis_id UUID        NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_favourites_user_analysis UNIQUE (user_id, analysis_id)
);
