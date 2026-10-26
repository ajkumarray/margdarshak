--liquibase formatted sql

-- Schema as Hibernate's ddl-auto created it before Liquibase was introduced.
-- Existing databases (Neon, local) already have these tables, so the precondition just marks
-- this changeset as run there; a fresh database gets the tables created.

--changeset ajkumarray:0000-baseline
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'user_master'
CREATE TABLE user_master (
    id                  BIGSERIAL PRIMARY KEY,
    created_at          TIMESTAMP(6),
    deleted             BOOLEAN,
    deleted_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    email               VARCHAR(255) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    profile_pic         VARCHAR(255),
    status              VARCHAR(255) NOT NULL
        CONSTRAINT user_master_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    user_code           VARCHAR(255) NOT NULL,
    verification_status VARCHAR(255) NOT NULL
        CONSTRAINT user_master_verification_status_check
            CHECK (verification_status IN ('VERIFIED', 'UNVERIFIED', 'PENDING', 'REJECTED'))
);

CREATE TABLE url_master (
    id               BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMP(6),
    deleted          BOOLEAN,
    deleted_at       TIMESTAMP(6),
    updated_at       TIMESTAMP(6),
    click_count      BIGINT       NOT NULL,
    code             VARCHAR(255) NOT NULL CONSTRAINT uk_url_master_code UNIQUE,
    created_by       VARCHAR(255) NOT NULL,
    expires_at       TIMESTAMP(6) NOT NULL,
    last_accessed_at TIMESTAMP(6),
    short_url        VARCHAR(255) NOT NULL,
    status           VARCHAR(255) NOT NULL
        CONSTRAINT url_master_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED')),
    url              VARCHAR(255) NOT NULL
);
--rollback DROP TABLE url_master; DROP TABLE user_master;
