-- V4__user_status_enum.sql
-- Alters the user_account.status column from VARCHAR to ENUM
-- to match Hibernate 6's MySQL dialect mapping for @Enumerated(EnumType.STRING)
ALTER TABLE user_account MODIFY COLUMN status ENUM('ACTIVE','LOCKED','DISABLED') NOT NULL DEFAULT 'ACTIVE';
