--liquibase formatted sql

-- Adds a role to every user; existing rows get 'USER'. Already applied by hand on Neon and local
-- on 2026-10-24 (as db/migrations/2026-10-24_001_user_role.sql), and IF NOT EXISTS makes
-- re-running it there a no-op.

--changeset ajkumarray:0001-user-role
ALTER TABLE user_master
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';
--rollback ALTER TABLE user_master DROP COLUMN role;

-- Promoting an admin is data, not schema, so it stays a manual one-off (then log in again):
-- UPDATE user_master SET role = 'ADMIN' WHERE email = '...';
