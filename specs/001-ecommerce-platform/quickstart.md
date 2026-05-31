# Quickstart: Full E-Commerce Platform

**Feature**: 001-ecommerce-platform  
**Date**: 2026-05-31

## Prerequisites

| Tool | Version |
|------|---------|
| Docker Desktop | Latest (with Compose v2) |
| JDK | 17+ |
| Node.js | 20 LTS |
| Maven | 3.9+ |

## Startup Order

1. **Infrastructure first** — start Docker Compose services and wait for health checks (`docker compose ps` shows healthy).
2. **Nacos** — must be up before backend services register (port 8848).
3. **Microservices** — start product, user, cart, order services (ports 8081–8084).
4. **BFF** — start shop-bff (port 8085) after downstream services are registered.
5. **Gateway** — start gateway last (port 8080); this is the only public backend entry point.
6. **Frontend** — `npm run dev` in `frontend/` (port 5173, proxies `/api` to gateway).

```powershell
cd infra
docker compose up -d mysql redis rabbitmq elasticsearch nacos zipkin
docker compose ps   # wait until all show healthy
```

Seed catalog data (after product-service schema migration V2 is applied):

```powershell
docker exec -i ecommerce-mysql mysql -uecommerce -pecommerce < infra/seed/catalog.sql
```

## 1. Start Infrastructure

From repository root:

```powershell
cd infra
docker compose up -d mysql redis rabbitmq elasticsearch nacos
```

Wait until all health checks pass:

```powershell
docker compose ps
```

Default ports:

| Service | Port |
|---------|------|
| MySQL | 3306 |
| Redis | 6379 |
| RabbitMQ (management) | 15672 |
| Elasticsearch | 9200 |
| Nacos | 8848 |

## 2. Initialize Databases

Each microservice schema is created on first startup via Flyway migrations:

- `product_db`, `user_db`, `cart_db`, `order_db`

Seed data script (featured products, categories, brands):

```powershell
# After product-service is running
curl -X POST http://localhost:8081/admin/seed
```

## 3. Start Backend Services

```powershell
# Terminal 1 — Gateway
cd gateway && mvn spring-boot:run

# Terminal 2 — BFF
cd shop-bff && mvn spring-boot:run

# Terminal 3-6 — Microservices
cd services/product-service && mvn spring-boot:run
cd services/user-service && mvn spring-boot:run
cd services/cart-service && mvn spring-boot:run
cd services/order-service && mvn spring-boot:run
```

Or use Docker Compose for all apps:

```powershell
cd infra
docker compose up -d gateway shop-bff product-service user-service cart-service order-service
```

## 4. Start Frontend

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 — all API calls proxy to gateway at http://localhost:8080/api.

## 5. Verify Core Flows

### Browse (P1)

```powershell
curl "http://localhost:8080/api/products/featured"
curl "http://localhost:8080/api/products/search?q=phone&categoryId=1&minPrice=100&maxPrice=5000&brandId=2"
```

### Register & Login (P2)

```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"email":"test@example.com","password":"SecurePass123","displayName":"Test User"}'

curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"test@example.com","password":"SecurePass123"}'
```

Save the `accessToken` from the response for authenticated calls.

Gateway rate-limits login to 20 requests per IP per minute (HTTP 429 when exceeded).

### Cart (P3)

```powershell
curl -X POST http://localhost:8080/api/cart/items `
  -H "Authorization: Bearer <accessToken>" `
  -H "Content-Type: application/json" `
  -d '{"productId":1,"quantity":2}'
```

### Checkout (P4)

```powershell
# Add address
curl -X POST http://localhost:8080/api/addresses `
  -H "Authorization: Bearer <accessToken>" `
  -H "Content-Type: application/json" `
  -d '{"recipientName":"张三","phone":"13800138000","province":"广东省","city":"深圳市","district":"南山区","street":"科技园路1号","postalCode":"518000","isDefault":true}'

# Place order
curl -X POST http://localhost:8080/api/orders/checkout `
  -H "Authorization: Bearer <accessToken>" `
  -H "Content-Type: application/json" `
  -d '{"addressId":1}'
```

### Order History (P5)

```powershell
curl http://localhost:8080/api/orders `
  -H "Authorization: Bearer <accessToken>"

curl http://localhost:8080/api/orders/1 `
  -H "Authorization: Bearer <accessToken>"
```

### Admin shipment update (manual v1)

```powershell
curl -X PUT http://localhost:8084/internal/admin/shipments/1 `
  -H "Content-Type: application/json" `
  -d '{"carrier":"顺丰速运","trackingNumber":"SF1234567890","status":"SHIPPED"}'
```

## 6. Run Tests

```powershell
# Backend unit + integration + Jacoco (≥ 70% on service layer)
mvn verify

# Gateway + BFF tests
mvn verify -pl gateway,shop-bff -am

# Frontend unit (Vitest)
cd frontend && npm run test:unit

# E2E (Cypress — start frontend dev server first, or use intercept mocks)
cd frontend && npm run dev
# separate terminal:
cd frontend && npm run test:e2e
```

## 7. Observability (Optional)

- Prometheus metrics: http://localhost:8080/actuator/prometheus (per service port)
- Zipkin UI: http://localhost:9411 (when enabled in docker-compose)
- RabbitMQ management: http://localhost:15672 (guest/guest)

## Troubleshooting

| Issue | Fix |
|-------|-----|
| ES search returns empty | Run product seed; check index sync logs in product-service |
| 401 on cart/checkout | Token expired (2 h); re-login or use refresh token endpoint |
| 429 on login | Gateway rate limit hit; wait 60 s or reduce retry frequency |
| Order stuck PENDING | Check RabbitMQ queues; verify product-service stock consumer |
| Guest cart lost | Session expired (24 h); expected behavior per spec |
| Jacoco check fails | Run `mvn verify` per service; focus coverage on `service` packages |

## Status

All user stories (US1–US5) and polish tasks (Phase 8) are implemented. See [tasks.md](./tasks.md) for the full checklist.
