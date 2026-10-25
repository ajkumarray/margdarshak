-- 2026-10-24_001_user_role.sql
-- Adds a role to every user. Existing rows (and any row inserted by code that doesn't
-- set it) get 'USER'. Safe to re-run.
-- Run BEFORE deploying code that maps UserMasterEntity.role (Lambda uses ddl-auto: none).

ALTER TABLE user_master
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Promote your own account to ADMIN (fill in the email, then run this line manually).
-- Log in again afterwards so the new token carries the role claim.
-- UPDATE user_master SET role = 'ADMIN' WHERE email = '...';
