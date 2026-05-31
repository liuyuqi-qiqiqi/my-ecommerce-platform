CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS cart_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON product_db.* TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON user_db.* TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON cart_db.* TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'ecommerce'@'%';
FLUSH PRIVILEGES;
