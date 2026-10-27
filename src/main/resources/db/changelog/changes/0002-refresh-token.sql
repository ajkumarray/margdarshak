--liquibase formatted sql

-- One row per login session (device). The refresh JWT carries this row's jti; the raw token is
-- never stored. Revoking a session (rotation, logout) is a soft delete; revoked rows are cleared
-- by a daily cleanup.

--changeset ajkumarray:0002-refresh-token
CREATE TABLE refresh_token (
    id         BIGSERIAL PRIMARY KEY,
    jti        VARCHAR(36)  NOT NULL CONSTRAINT uk_refresh_token_jti UNIQUE,
    user_code  VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    deleted_at TIMESTAMP(6),
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_refresh_token_user_code ON refresh_token (user_code);
--rollback DROP TABLE refresh_token;
