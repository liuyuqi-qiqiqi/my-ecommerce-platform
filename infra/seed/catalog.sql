-- Catalog seed data for local development (product_db)
-- Run after product-service Flyway V2__product_schema.sql is applied

USE product_db;

INSERT INTO category (id, name, parent_id, sort_order) VALUES
  (1, '电子产品', NULL, 1),
  (2, '家居生活', NULL, 2),
  (3, '手机', 1, 1),
  (4, '笔记本电脑', 1, 2)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO brand (id, name) VALUES
  (1, 'TechBrand'),
  (2, 'HomePlus'),
  (3, 'MobileOne')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO product (id, sku, name, description, price, brand_id, category_id, stock_quantity, featured, status, created_at, updated_at) VALUES
  (1, 'SKU-PHONE-001', '智能手机 Pro', '6.7 英寸 OLED 显示屏，256GB 存储', 4999.00, 3, 3, 50, TRUE, 'ACTIVE', NOW(), NOW()),
  (2, 'SKU-LAPTOP-001', '轻薄笔记本 Air', '14 英寸，16GB 内存，512GB SSD', 6999.00, 1, 4, 30, TRUE, 'ACTIVE', NOW(), NOW()),
  (3, 'SKU-HOME-001', '智能台灯', '可调色温，App 控制', 299.00, 2, 2, 100, TRUE, 'ACTIVE', NOW(), NOW()),
  (4, 'SKU-PHONE-002', '入门智能手机', '6.1 英寸 LCD，128GB 存储', 1999.00, 3, 3, 80, FALSE, 'ACTIVE', NOW(), NOW()),
  (5, 'SKU-LAPTOP-002', '游戏笔记本', '15.6 英寸，RTX 显卡，32GB 内存', 9999.00, 1, 4, 15, FALSE, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

INSERT INTO product_image (product_id, url, is_primary, sort_order) VALUES
  (1, 'https://placehold.co/400x400?text=Phone+Pro', TRUE, 0),
  (2, 'https://placehold.co/400x400?text=Laptop+Air', TRUE, 0),
  (3, 'https://placehold.co/400x400?text=Smart+Lamp', TRUE, 0),
  (4, 'https://placehold.co/400x400?text=Phone+Lite', TRUE, 0),
  (5, 'https://placehold.co/400x400?text=Gaming+Laptop', TRUE, 0)
ON DUPLICATE KEY UPDATE url = VALUES(url);
