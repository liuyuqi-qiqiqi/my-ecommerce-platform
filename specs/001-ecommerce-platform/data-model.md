# Data Model: Full E-Commerce Platform

**Feature**: 001-ecommerce-platform  
**Date**: 2026-05-31

Each entity is owned by exactly one microservice. Cross-service references use IDs only (no foreign keys across databases).

---

## product-service (schema: `product_db`)

### Category

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK, auto-increment |
| name | VARCHAR(100) | NOT NULL, UNIQUE |
| parent_id | BIGINT | NULL, self-ref for hierarchy |
| sort_order | INT | DEFAULT 0 |

### Brand

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| name | VARCHAR(100) | NOT NULL, UNIQUE |

### Product

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| sku | VARCHAR(50) | NOT NULL, UNIQUE |
| name | VARCHAR(200) | NOT NULL |
| description | TEXT | |
| price | DECIMAL(10,2) | NOT NULL, ≥ 0 |
| brand_id | BIGINT | FK → Brand |
| category_id | BIGINT | FK → Category |
| stock_quantity | INT | NOT NULL, ≥ 0 |
| featured | BOOLEAN | DEFAULT false |
| status | ENUM | ACTIVE, INACTIVE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

**Validation**: FR-003, FR-004, FR-015 — price ≥ 0; stock cannot go negative on reservation.

### ProductImage

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| product_id | BIGINT | FK → Product |
| url | VARCHAR(500) | NOT NULL |
| is_primary | BOOLEAN | DEFAULT false |
| sort_order | INT | DEFAULT 0 |

### Elasticsearch Document: `products`

Mirrors Product + brand name, category name for faceted search. Fields: `id`, `name`, `description`, `price`, `brandId`, `brandName`, `categoryId`, `categoryName`, `stockQuantity`, `featured`, `status`.

---

## user-service (schema: `user_db`)

### User

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL (BCrypt) |
| display_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(20) | NULL |
| status | ENUM | ACTIVE, LOCKED, DISABLED |
| failed_login_count | INT | DEFAULT 0 |
| locked_until | TIMESTAMP | NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

**State transitions**: ACTIVE → LOCKED (after 3 failed logins, FR-022); LOCKED → ACTIVE (after lock TTL or admin unlock).

### RefreshToken (Redis)

| Key | Value | TTL |
|-----|-------|-----|
| `refresh:{tokenId}` | userId, issuedAt | Sliding, config-driven |

### Address

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| user_id | BIGINT | FK → User, NOT NULL |
| recipient_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(20) | NOT NULL |
| province | VARCHAR(50) | NOT NULL |
| city | VARCHAR(50) | NOT NULL |
| district | VARCHAR(50) | NOT NULL |
| street | VARCHAR(200) | NOT NULL |
| postal_code | VARCHAR(20) | NOT NULL |
| is_default | BOOLEAN | DEFAULT false |
| created_at | TIMESTAMP | NOT NULL |

**Validation**: FR-013 — all address fields required; one default per user max.

---

## cart-service (schema: `cart_db`)

### Cart

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| user_id | BIGINT | NOT NULL, UNIQUE (one cart per user) |
| updated_at | TIMESTAMP | NOT NULL |

### CartItem

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| cart_id | BIGINT | FK → Cart |
| product_id | BIGINT | NOT NULL (reference only) |
| product_name | VARCHAR(200) | Snapshot at add time |
| unit_price | DECIMAL(10,2) | Snapshot at add time |
| quantity | INT | NOT NULL, ≥ 1 |
| created_at | TIMESTAMP | NOT NULL |

**Unique**: (cart_id, product_id)

**Guest cart (Redis)**: Key `guest-cart:{sessionId}` → JSON array of `{productId, productName, unitPrice, quantity}`; TTL 24 h.

---

## order-service (schema: `order_db`)

### Order

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| order_number | VARCHAR(32) | NOT NULL, UNIQUE |
| user_id | BIGINT | NOT NULL (reference) |
| status | ENUM | PENDING, CONFIRMED, CANCELLED, SHIPPED, DELIVERED |
| subtotal | DECIMAL(10,2) | NOT NULL |
| shipping_fee | DECIMAL(10,2) | NOT NULL |
| total_amount | DECIMAL(10,2) | NOT NULL |
| payment_method | VARCHAR(50) | DEFAULT 'PAY_ON_DELIVERY' |
| address_snapshot | JSON | NOT NULL (full address at order time) |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

**State transitions**:

```text
PENDING → CONFIRMED (stock reserved)
PENDING → CANCELLED (stock reservation failed / timeout)
CONFIRMED → SHIPPED (fulfillment)
SHIPPED → DELIVERED (carrier update)
```

### OrderItem

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| order_id | BIGINT | FK → Order |
| product_id | BIGINT | NOT NULL |
| product_name | VARCHAR(200) | NOT NULL |
| unit_price | DECIMAL(10,2) | NOT NULL |
| quantity | INT | NOT NULL, ≥ 1 |

### Shipment

| Field | Type | Constraints |
|-------|------|-------------|
| id | BIGINT | PK |
| order_id | BIGINT | FK → Order, UNIQUE |
| carrier | VARCHAR(100) | NULL until shipped |
| tracking_number | VARCHAR(100) | NULL |
| status | ENUM | PENDING, SHIPPED, IN_TRANSIT, DELIVERED |
| status_updated_at | TIMESTAMP | NOT NULL |
| events | JSON | Timeline of status changes |

---

## Cross-Service Reference Map

| From | To | Mechanism |
|------|-----|-----------|
| cart-service | product-service | REST: validate product + price on add |
| order-service | product-service | RabbitMQ: StockReserve / StockRelease |
| order-service | cart-service | RabbitMQ: OrderConfirmed → clear cart |
| product-service | Elasticsearch | RabbitMQ events → index sync |
| shop-bff | all services | REST via gateway (internal routes) |
| Vue frontend | shop-bff | REST via gateway (public routes) |

---

## Domain Events (RabbitMQ)

| Event | Publisher | Consumers |
|-------|-----------|-----------|
| ProductCreated / ProductUpdated / StockChanged | product-service | search-indexer |
| OrderCreated | order-service | product-service |
| StockReserved / StockReservationFailed | product-service | order-service |
| OrderConfirmed | order-service | cart-service |
| OrderCancelled | order-service | product-service (compensation) |
