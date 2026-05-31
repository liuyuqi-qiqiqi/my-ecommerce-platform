# Implementation Plan: Full E-Commerce Platform

**Branch**: `001-ecommerce-platform` | **Date**: 2026-05-31 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-ecommerce-platform/spec.md`

## Summary

Deliver a PC web e-commerce platform with product discovery (homepage, Elasticsearch search/filters), JWT authentication and profile management, persistent shopping cart, checkout with address management, and order history with shipment tracking. Architecture follows constitution-mandated microservices (product, user, cart, order) behind Spring Cloud Gateway and a Vue-facing BFF. Order placement uses a RabbitMQ Saga for inventory reservation and cart clearing. v1 excludes online payment and live carrier APIs.

## Technical Context

**Language/Version**: Java 17+ (backend), TypeScript strict mode (frontend Vue 3.4+)

**Primary Dependencies**: Spring Boot 3.x, Spring Cloud 2023.x, Spring Cloud Gateway, Nacos, Vue 3, Element Plus 2.7+, Vite 5+, Elasticsearch 8.x client, Spring AMQP (RabbitMQ), Spring Data Redis, JWT (jjwt or Spring Security OAuth2 Resource Server)

**Storage**: MySQL 8.0 (one schema per service), Elasticsearch 8.x (product index), Redis (refresh tokens, guest carts)

**Testing**: JUnit 5, Mockito, Testcontainers, Jacoco (≥ 70%), Vitest, Cypress, OpenAPI contract validation

**Target Platform**: PC web browsers; backend on Linux containers (Docker Compose local, Kubernetes production)

**Project Type**: Microservices web application (frontend + multiple backend services)

**Performance Goals**: Order confirmation ≤ 5 s p95 at 500 concurrent shoppers (SC-006); search results perceived instant (< 2 s user-facing, SC-001/002)

**Constraints**: No cross-service DB access; no distributed transactions; JWT access ≤ 2 h; unified error format `{code, message}`; gateway-only frontend access

**Scale/Scope**: 5 user stories, 22 FRs, ~15 Vue pages/views, 4 microservices + gateway + BFF, v1 single currency domestic market

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Reference: `.specify/memory/constitution.md` (E-Commerce Platform v1.0.0)

- [x] **Microservice Boundaries**: Four domain services each with dedicated MySQL schema; inter-service REST + RabbitMQ only (see [data-model.md](./data-model.md))
- [x] **Frontend-Backend Separation**: Vue app calls `/api/**` on gateway → shop-bff only; no direct microservice URLs in frontend
- [x] **Data Consistency**: Checkout Saga via RabbitMQ (OrderCreated → StockReserved → OrderConfirmed); no 2PC (see [research.md](./research.md) R4)
- [x] **Test & Quality Gates**: JUnit/Testcontainers/Vitest/Cypress plan in research R9; OpenAPI contracts in [contracts/](./contracts/); unified `ErrorResponse` schema
- [x] **Security & Observability**: JWT + Redis refresh, Hibernate Validator, audit logs on checkout/auth, Micrometer + Zipkin on critical paths
- [x] **Technology Stack**: Java 17, Spring Boot/Cloud 2023.x, Vue 3/TS, Element Plus, Vite, MySQL, ES, RabbitMQ — all constitution-approved
- [x] **Development Workflow**: spec.md + plan.md complete before `/speckit-tasks`; human review required for AI-generated code

**Post-design re-check (Phase 1)**: All gates pass. No unjustified violations.

## Project Structure

### Documentation (this feature)

```text
specs/001-ecommerce-platform/
├── plan.md              # This file
├── research.md          # Phase 0 decisions
├── data-model.md        # Phase 1 entity definitions
├── quickstart.md        # Local dev guide
├── contracts/           # OpenAPI specs
│   ├── shop-bff-api.yaml
│   └── events.md
└── tasks.md             # Phase 2 (/speckit-tasks — not yet created)
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── components/       # Shared UI (ProductCard, CartItem, AddressForm)
│   ├── pages/            # Home, Search, ProductDetail, Cart, Checkout, Orders, Auth, Profile
│   ├── services/         # API client (axios → gateway /api)
│   ├── stores/           # Pinia: auth, cart
│   └── router/
└── tests/
    ├── unit/             # Vitest
    └── e2e/              # Cypress

gateway/
└── src/main/java/        # Spring Cloud Gateway routes, JWT filter

shop-bff/
└── src/main/java/        # Aggregates calls to product/user/cart/order services

services/
├── product-service/
│   ├── src/main/java/
│   └── src/test/
├── user-service/
│   ├── src/main/java/
│   └── src/test/
├── cart-service/
│   ├── src/main/java/
│   └── src/test/
└── order-service/
    ├── src/main/java/
    └── src/test/

infra/
├── docker-compose.yml    # MySQL, Redis, RabbitMQ, ES, Nacos, all services
└── k8s/                  # Production manifests (future)

.github/workflows/        # Per-service CI pipelines
```

**Structure Decision**: Option 2 (web application) extended to microservices layout per constitution. Frontend is a standalone Vite project; each backend service is an independent Spring Boot module under `services/`. Gateway and BFF are separate deployables. Shared DTOs/contracts live in spec `contracts/` and are copied/generated per service at build time.

## Complexity Tracking

> No constitution violations requiring justification. Multi-service architecture is constitution-mandated, not optional complexity.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Implementation Phases (High-Level)

| Phase | Deliverable | User Stories |
|-------|-------------|--------------|
| 0 | Infra scaffold (Docker Compose, Nacos, gateway, empty services) | — |
| 1 | product-service + ES search + BFF catalog API + Vue browse/search | P1 |
| 2 | user-service JWT auth + Vue auth/profile pages | P2 |
| 3 | cart-service + guest Redis cart + Vue cart page | P3 |
| 4 | order-service Saga checkout + Vue checkout flow | P4 |
| 5 | order history + shipment display + Cypress E2E full journey | P5 |

Detailed task breakdown: run `/speckit-tasks`.

## Generated Artifacts

| Artifact | Path | Description |
|----------|------|-------------|
| Research | [research.md](./research.md) | 10 technical decisions |
| Data model | [data-model.md](./data-model.md) | Per-service entities and events |
| BFF API contract | [contracts/shop-bff-api.yaml](./contracts/shop-bff-api.yaml) | OpenAPI 3.0 shopper API |
| Events | [contracts/events.md](./contracts/events.md) | RabbitMQ event schemas |
| Quickstart | [quickstart.md](./quickstart.md) | Local development setup |
