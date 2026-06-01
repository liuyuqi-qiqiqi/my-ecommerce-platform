-- V3__order_status_enum.sql
-- Alters status columns from VARCHAR to ENUM
-- to match Hibernate 6's MySQL dialect mapping for @Enumerated(EnumType.STRING)
ALTER TABLE customer_order MODIFY COLUMN status ENUM('PENDING','CONFIRMED','CANCELLED','SHIPPED','DELIVERED') NOT NULL;
ALTER TABLE shipment MODIFY COLUMN status ENUM('PENDING','SHIPPED','IN_TRANSIT','DELIVERED') NOT NULL;
