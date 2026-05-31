# Research: Full E-Commerce Platform

**Feature**: 001-ecommerce-platform  
**Date**: 2026-05-31  
**Status**: Complete — all technical context resolved

## R1: Microservice Decomposition

**Decision**: Four domain services — `product-service`, `user-service`, `cart-service`, `order-service` — plus `api-gateway` and `shop-bff`.

**Rationale**: Aligns with constitution Principle I (product, order, cart, user domains). Each service owns one MySQL schema. BFF aggregates shopper-facing responses; gateway handles routing, JWT validation, and rate limiting.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| Monolith | Violates constitution microservice boundaries and independent deployment goals |
| Product + Inventory split early | Inventory changes are tightly coupled to product catalog in v1; stock lives in product-service until scale demands split |
| Frontend calls services directly | Violates constitution Principle II |

---

## R2: Product Search & Filtering

**Decision**: Elasticsearch 8.x indexes product documents synced from `product-service` via RabbitMQ domain events (`ProductCreated`, `ProductUpdated`, `StockChanged`).

**Rationale**: Constitution mandates ES for keyword + attribute filtering. Async sync avoids blocking writes and keeps search eventually consistent (acceptable for catalog browse).

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| MySQL full-text only | Poor relevance ranking and multi-filter performance at scale |
| Synchronous dual-write to ES | Write latency and failure coupling |

---

## R3: Authentication (JWT)

**Decision**: `user-service` issues JWT access tokens (≤ 2 h TTL) and refresh tokens stored in Redis with sliding expiration. Gateway validates access tokens; BFF forwards `Authorization` header.

**Rationale**: Matches constitution Principle V and user spec requirement. Redis refresh enables revocation and session control.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| Session cookies only | Harder to scale statelessly across gateway instances without shared session store |
| OAuth2 third-party only | Out of scope for v1; email/password is primary shopper flow |

---

## R4: Checkout & Inventory (Saga)

**Decision**: Choreography-based Saga via RabbitMQ for order placement:

1. `order-service` receives checkout command → creates order in `PENDING` state
2. Publishes `OrderCreated` → `product-service` reserves/decrements stock
3. `product-service` publishes `StockReserved` or `StockReservationFailed`
4. On success: `order-service` → `CONFIRMED`; `cart-service` clears cart via `OrderConfirmed` event
5. On failure: `order-service` → `CANCELLED`; compensating message releases stock if reserved

**Rationale**: Constitution forbids cross-service DB transactions. Saga with reliable messaging satisfies Principle III.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| Orchestrator in BFF | Business logic in BFF violates domain ownership; order lifecycle belongs in order-service |
| 2PC/XA transactions | Explicitly forbidden by constitution |

---

## R5: Guest vs Signed-In Cart

**Decision**: Guest carts stored in Redis keyed by session ID (24 h TTL). Signed-in carts in `cart-service` MySQL, merged on login (guest items appended, duplicate SKUs sum quantities).

**Rationale**: Spec allows guest browse; FR-012 requires persistence for signed-in users. Redis gives fast guest cart without account provisioning.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| Login required for cart | Violates browse-first UX in spec assumptions |
| Guest cart in MySQL | Unnecessary persistence overhead for anonymous sessions |

---

## R6: Shipping Cost (v1)

**Decision**: Flat domestic shipping fee (configurable in Nacos) plus optional region surcharge table in `order-service`.

**Rationale**: Spec assumption excludes live carrier rate APIs for v1. Simple rule set meets checkout summary requirements.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| Free shipping always | Hides real checkout total; spec requires shipping in order summary |
| Real-time carrier API | Out of v1 scope per spec assumptions |

---

## R7: Shipment Tracking (v1)

**Decision**: `order-service` owns `Shipment` entity; status updated via admin API or batch CSV import. Shopper reads status from order detail API.

**Rationale**: Spec assumption: manual/batch carrier updates in v1. No live carrier integration needed.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|------------------|
| Third-party tracking API | Out of v1 scope |
| Separate fulfillment service | Over-engineering for initial launch |

---

## R8: API Contract Strategy

**Decision**: OpenAPI 3.0 contracts for BFF shopper API (single surface for Vue frontend). Internal service-to-service contracts documented separately under `contracts/internal/`.

**Rationale**: Constitution requires OpenAPI for REST endpoints. Frontend talks only to BFF through gateway — one primary contract reduces drift.

**Alternatives considered**:

| Alternative | Rejected because |
|-------------|-------------------|
| Per-microservice frontend calls | Violates constitution |
| GraphQL BFF | Not in approved stack; REST + OpenAPI is mandated |

---

## R9: Testing Strategy

**Decision**:

| Layer | Tool | Scope |
|-------|------|-------|
| Backend unit | JUnit 5 + Mockito | ≥ 70% Jacoco per service |
| Backend integration | Testcontainers (MySQL, ES, RabbitMQ, Redis) | Order saga, auth, search |
| Contract | Spring Cloud Contract or OpenAPI diff tests | BFF ↔ services |
| Frontend unit | Vitest + Vue Test Utils | Components, cart logic |
| Frontend E2E | Cypress | Browse → cart → checkout → order history |

**Rationale**: Constitution Principle IV mandates these tools and coverage thresholds for core flows.

---

## R10: Observability

**Decision**: Micrometer metrics, Sleuth/Zipkin trace IDs propagated via RabbitMQ headers and HTTP, structured JSON logs to stdout (ELK in deployment).

**Rationale**: Constitution Principle V. Critical paths: search, order placement, auth.

**Alternatives considered**: None — constitution is prescriptive.
