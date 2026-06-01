-- V4__product_status_enum.sql
-- Alters the product.status column from VARCHAR to ENUM
-- to match Hibernate 6's MySQL dialect mapping for @Enumerated(EnumType.STRING)
ALTER TABLE product MODIFY COLUMN status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE';
