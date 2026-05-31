# Tasks: Full E-Commerce Platform

**Input**: Design documents from `/specs/001-ecommerce-platform/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Not explicitly requested in spec.md. Quality-gate test tasks are included in the final Polish phase per plan.md (Jacoco ≥ 70%, Cypress full journey).

**Organization**: Tasks grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1–US5)
- Include exact file paths in descriptions

## Path Conventions

- **Frontend**: `frontend/src/`
- **Gateway**: `gateway/src/main/java/`
- **BFF**: `shop-bff/src/main/java/`
- **Services**: `services/{service-name}/src/main/java/`
- **Infra**: `infra/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, repository layout, and local dev environment

- [x] T001 Create monorepo directory structure per plan.md (`frontend/`, `gateway/`, `shop-bff/`, `services/product-service/`, `services/user-service/`, `services/cart-service/`, `services/order-service/`, `infra/`, `.github/workflows/`)
- [x] T002 [P] Initialize Vue 3 + TypeScript + Vite project in `frontend/` with Element Plus 2.7+, Pinia, Vue Router, axios
- [x] T003 [P] Initialize Spring Boot 3.x parent POM at repo root with Java 17, Spring Cloud 2023.x BOM, shared dependency versions
- [x] T004 [P] Scaffold `gateway/` Spring Cloud Gateway module with `gateway/src/main/resources/application.yml`
- [x] T005 [P] Scaffold `shop-bff/` Spring Boot module with WebClient/Feign client stubs in `shop-bff/src/main/java/`
- [x] T006 [P] Scaffold empty `services/product-service/` Spring Boot module with `application.yml` and `product_db` datasource config placeholder
- [x] T007 [P] Scaffold empty `services/user-service/` Spring Boot module with `application.yml` and `user_db` datasource config placeholder
- [x] T008 [P] Scaffold empty `services/cart-service/` Spring Boot module with `application.yml` and `cart_db` datasource config placeholder
- [x] T009 [P] Scaffold empty `services/order-service/` Spring Boot module with `application.yml` and `order_db` datasource config placeholder
- [x] T010 Create `infra/docker-compose.yml` with MySQL 8.0 (4 schemas), Redis, RabbitMQ, Elasticsearch 8.x, Nacos, Zipkin
- [x] T011 [P] Configure ESLint + Prettier for frontend in `frontend/.eslintrc.cjs` and `frontend/.prettierrc`
- [x] T012 [P] Configure Checkstyle/Spotless for Java modules in root `pom.xml` or per-module config
- [x] T013 [P] Create GitHub Actions CI workflow skeleton in `.github/workflows/ci.yml` (build all modules, run unit tests)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T014 Register all services with Nacos service discovery; configure `spring.cloud.nacos` in each service `application.yml`
- [x] T015 Configure Spring Cloud Gateway routes in `gateway/src/main/resources/application.yml` — public `/api/**` → shop-bff, internal service routes
- [x] T016 Implement JWT validation filter in `gateway/src/main/java/` (validate access token, forward user claims to BFF via headers)
- [x] T017 Create shared `ErrorResponse` DTO and global exception handler in `shop-bff/src/main/java/` matching `contracts/shop-bff-api.yaml` schema `{code, message}`
- [x] T018 [P] Replicate `ErrorResponse` + `@ControllerAdvice` in each service under `services/*/src/main/java/` common package
- [x] T019 Configure Flyway/Liquibase migration framework in each service (`services/*/src/main/resources/db/migration/`)
- [x] T020 [P] Configure Redis connection in `services/user-service/` and `services/cart-service/` `application.yml`
- [x] T021 [P] Configure RabbitMQ connection and `commerce.events` topic exchange in all services that publish/consume events
- [x] T022 [P] Configure Elasticsearch client in `services/product-service/src/main/java/` with index name `products`
- [x] T023 Implement BFF service client layer in `shop-bff/src/main/java/` (WebClient/Feign interfaces for product, user, cart, order internal APIs)
- [x] T024 Configure Micrometer metrics + Zipkin tracing on gateway, BFF, and all services in respective `application.yml`
- [x] T025 Create frontend API client base in `frontend/src/services/api.ts` (axios instance → `http://localhost:8080/api`, interceptors for JWT)
- [x] T026 Create frontend router skeleton in `frontend/src/router/index.ts` with lazy-loaded page routes (Home, Search, ProductDetail, Cart, Checkout, Orders, Auth, Profile)
- [x] T027 Create catalog seed data script in `infra/seed/catalog.sql` (categories, brands, products, images) for local development
- [x] T028 Document local startup order in `specs/001-ecommerce-platform/quickstart.md` (verify docker-compose services healthy before apps)

**Checkpoint**: Foundation ready — user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Browse & Discover Products (Priority: P1) 🎯 MVP

**Goal**: Homepage with featured products, keyword search with filters, and product detail pages

**Independent Test**: Load homepage → view featured products → search by keyword → apply category/price/brand filters → open product detail — no login required

### Implementation for User Story 1

- [x] T029 [P] [US1] Create Flyway migration for Category, Brand, Product, ProductImage tables in `services/product-service/src/main/resources/db/migration/V2__product_schema.sql`
- [x] T030 [P] [US1] Create JPA entities Category, Brand, Product, ProductImage in `services/product-service/src/main/java/`
- [x] T031 [P] [US1] Create repositories in `services/product-service/src/main/java/` for Category, Brand, Product, ProductImage
- [x] T032 [US1] Implement ProductService (featured list, get by id) in `services/product-service/src/main/java/`
- [x] T033 [US1] Implement internal REST controller in `services/product-service/src/main/java/` — GET `/internal/products/featured`, GET `/internal/products/{id}`
- [x] T034 [US1] Implement Elasticsearch product document mapping and index template in `services/product-service/src/main/java/`
- [x] T035 [US1] Implement ProductSearchService (keyword + category/brand/price filters, pagination) in `services/product-service/src/main/java/`
- [x] T036 [US1] Implement internal search endpoint GET `/internal/products/search` in `services/product-service/src/main/java/`
- [x] T037 [US1] Implement RabbitMQ event publisher for ProductCreated/ProductUpdated/StockChanged in `services/product-service/src/main/java/`
- [x] T038 [US1] Implement ES index sync consumer (ProductCreated/ProductUpdated/StockChanged → upsert `products` index) in `services/product-service/src/main/java/`
- [x] T039 [US1] Implement BFF catalog endpoints in `shop-bff/src/main/java/` — GET `/products/featured`, GET `/products/search`, GET `/products/{productId}` per `contracts/shop-bff-api.yaml`
- [x] T040 [P] [US1] Create ProductCard component in `frontend/src/components/ProductCard.vue`
- [x] T041 [P] [US1] Create ProductFilter sidebar component in `frontend/src/components/ProductFilter.vue`
- [x] T042 [US1] Create Home page with featured product grid in `frontend/src/pages/Home.vue`
- [x] T043 [US1] Create Search page with keyword input, filters, and paginated results in `frontend/src/pages/Search.vue`
- [x] T044 [US1] Create ProductDetail page (description, price, brand, category, stock status) in `frontend/src/pages/ProductDetail.vue`
- [x] T045 [US1] Create catalog API service in `frontend/src/services/catalogService.ts` calling BFF catalog endpoints
- [x] T046 [US1] Add empty-state UI for zero search results in `frontend/src/pages/Search.vue`
- [x] T047 [US1] Run seed script and verify ES index populated; reindex existing products on startup if index empty

**Checkpoint**: User Story 1 fully functional — visitors can browse, search, filter, and view product details without authentication

---

## Phase 4: User Story 2 - Register, Sign In & Manage Profile (Priority: P2)

**Goal**: JWT-based registration, login, logout, profile management, and account lockout after failed attempts

**Independent Test**: Register → sign out → sign in → update profile → verify persistence → sign out → protected routes blocked

### Implementation for User Story 2

- [x] T048 [P] [US2] Create Flyway migration for User table in `services/user-service/src/main/resources/db/migration/V2__user_schema.sql`
- [x] T049 [P] [US2] Create User JPA entity with status enum (ACTIVE, LOCKED, DISABLED) in `services/user-service/src/main/java/`
- [x] T050 [US2] Implement PasswordEncoder (BCrypt) and UserRepository in `services/user-service/src/main/java/`
- [x] T051 [US2] Implement JwtTokenProvider (access token ≤ 2 h TTL) in `services/user-service/src/main/java/`
- [x] T052 [US2] Implement RefreshTokenService (Redis `refresh:{tokenId}`, sliding TTL) in `services/user-service/src/main/java/`
- [x] T053 [US2] Implement AuthService (register, login, refresh, logout) with failed-login counter and lock after 3 attempts (FR-022) in `services/user-service/src/main/java/`
- [x] T054 [US2] Implement internal auth REST controller in `services/user-service/src/main/java/` — POST register/login/refresh/logout
- [x] T055 [US2] Implement ProfileService (get/update displayName, phone, email) in `services/user-service/src/main/java/`
- [x] T056 [US2] Implement internal profile REST controller in `services/user-service/src/main/java/` — GET/PUT `/internal/profile`
- [x] T057 [US2] Implement BFF auth endpoints in `shop-bff/src/main/java/` — POST `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout` per OpenAPI contract
- [x] T058 [US2] Implement BFF profile endpoints in `shop-bff/src/main/java/` — GET/PUT `/profile` with bearerAuth
- [x] T059 [US2] Update gateway JWT filter to allow unauthenticated access to `/api/auth/**` and `/api/products/**` only
- [x] T060 [P] [US2] Create Pinia auth store in `frontend/src/stores/auth.ts` (tokens, user profile, login/logout/refresh actions)
- [x] T061 [P] [US2] Create Register page in `frontend/src/pages/Register.vue`
- [x] T062 [P] [US2] Create Login page in `frontend/src/pages/Login.vue`
- [x] T063 [US2] Create Profile page in `frontend/src/pages/Profile.vue` with editable displayName, phone, email
- [x] T064 [US2] Create auth API service in `frontend/src/services/authService.ts`
- [x] T065 [US2] Add route guards in `frontend/src/router/index.ts` for protected pages (Profile, Checkout, Orders)
- [x] T066 [US2] Wire axios interceptor in `frontend/src/services/api.ts` for token attach and 401 refresh retry

**Checkpoint**: User Story 2 fully functional — account lifecycle works independently of cart/checkout

---

## Phase 5: User Story 3 - Manage Shopping Cart (Priority: P3)

**Goal**: Add/update/remove cart items with totals; guest Redis cart and signed-in MySQL cart with session persistence

**Independent Test**: Add items → change quantities → remove items → verify totals → sign out/in and verify cart restored for signed-in user

### Implementation for User Story 3

- [x] T067 [P] [US3] Create Flyway migration for Cart, CartItem tables in `services/cart-service/src/main/resources/db/migration/V2__cart_schema.sql`
- [x] T068 [P] [US3] Create Cart and CartItem JPA entities in `services/cart-service/src/main/java/`
- [x] T069 [US3] Implement GuestCartService (Redis `guest-cart:{sessionId}`, 24 h TTL) in `services/cart-service/src/main/java/`
- [x] T070 [US3] Implement CartService (get, add, update quantity, remove, calculate subtotals) in `services/cart-service/src/main/java/`
- [x] T071 [US3] Implement product validation client (REST call to product-service for price/stock on add) in `services/cart-service/src/main/java/`
- [x] T072 [US3] Implement cart merge on login (guest items appended, duplicate SKUs sum quantities) in `services/cart-service/src/main/java/`
- [x] T073 [US3] Implement internal cart REST controller in `services/cart-service/src/main/java/` — GET `/internal/cart`, POST/PUT/DELETE items
- [x] T074 [US3] Implement BFF cart endpoints in `shop-bff/src/main/java/` — GET `/cart`, POST `/cart/items`, PUT/DELETE `/cart/items/{productId}` per OpenAPI contract
- [x] T075 [US3] Pass session cookie for guest cart and user ID header for authenticated cart in `shop-bff/src/main/java/`
- [x] T076 [P] [US3] Create CartItem component in `frontend/src/components/CartItem.vue`
- [x] T077 [US3] Create Pinia cart store in `frontend/src/stores/cart.ts`
- [x] T078 [US3] Create Cart page with line items, quantity controls, remove actions, and totals in `frontend/src/pages/Cart.vue`
- [x] T079 [US3] Create cart API service in `frontend/src/services/cartService.ts`
- [x] T080 [US3] Add "Add to Cart" action on ProductDetail page in `frontend/src/pages/ProductDetail.vue`
- [x] T081 [US3] Flag unavailable/out-of-stock items in cart UI and block checkout button when any item unavailable in `frontend/src/pages/Cart.vue`

**Checkpoint**: User Story 3 fully functional — cart management works for guests and signed-in users

---

## Phase 6: User Story 4 - Checkout with Address & Order Placement (Priority: P4)

**Goal**: Address CRUD, checkout summary, Saga-based order placement with stock reservation and cart clearing

**Independent Test**: Sign in → fill cart → add/select address → confirm checkout → receive order number → cart cleared; verify stock conflict blocks checkout

### Implementation for User Story 4

- [x] T082 [P] [US4] Create Flyway migration for Address table in `services/user-service/src/main/resources/db/migration/V3__address_schema.sql`
- [x] T083 [P] [US4] Create Address JPA entity in `services/user-service/src/main/java/`
- [x] T084 [US4] Implement AddressService (CRUD, default address logic) in `services/user-service/src/main/java/`
- [x] T085 [US4] Implement internal address REST controller in `services/user-service/src/main/java/` — GET/POST `/internal/addresses`, PUT/DELETE `/internal/addresses/{id}`
- [x] T086 [P] [US4] Create Flyway migration for Order, OrderItem, Shipment tables in `services/order-service/src/main/resources/db/migration/V2__order_schema.sql`
- [x] T087 [P] [US4] Create Order, OrderItem, Shipment JPA entities with status enums in `services/order-service/src/main/java/`
- [x] T088 [US4] Implement OrderNumberGenerator (unique order number format) in `services/order-service/src/main/java/`
- [x] T089 [US4] Implement ShippingFeeCalculator (flat fee + region surcharge from Nacos config) in `services/order-service/src/main/java/`
- [x] T090 [US4] Implement CheckoutService — create PENDING order, publish OrderCreated event in `services/order-service/src/main/java/`
- [x] T091 [US4] Implement OrderCreated consumer in `services/product-service/src/main/java/` — reserve/decrement stock, publish StockReserved or StockReservationFailed
- [x] T092 [US4] Implement StockReserved/StockReservationFailed consumer in `services/order-service/src/main/java/` — transition to CONFIRMED or CANCELLED
- [x] T093 [US4] Implement OrderConfirmed publisher in `services/order-service/src/main/java/` on successful confirmation
- [x] T094 [US4] Implement OrderConfirmed consumer in `services/cart-service/src/main/java/` — clear user cart
- [x] T095 [US4] Implement OrderCancelled compensation consumer in `services/product-service/src/main/java/` — release reserved stock
- [x] T096 [US4] Configure RabbitMQ DLQ `commerce.events.dlq` with idempotent handlers keyed by eventId in all event consumers
- [x] T097 [US4] Implement internal checkout REST endpoint POST `/internal/orders/checkout` in `services/order-service/src/main/java/`
- [x] T098 [US4] Implement BFF address endpoints in `shop-bff/src/main/java/` — GET/POST `/addresses`, PUT/DELETE `/addresses/{addressId}` per OpenAPI contract
- [x] T099 [US4] Implement BFF checkout endpoint POST `/orders/checkout` in `shop-bff/src/main/java/` (requires bearerAuth)
- [x] T100 [P] [US4] Create AddressForm component in `frontend/src/components/AddressForm.vue`
- [x] T101 [US4] Create Checkout page with order summary, address selection/creation, and confirm button in `frontend/src/pages/Checkout.vue`
- [x] T102 [US4] Create OrderConfirmation page/modal showing order number and navigation to order detail in `frontend/src/pages/OrderConfirmation.vue`
- [x] T103 [US4] Create address and checkout API services in `frontend/src/services/addressService.ts` and `frontend/src/services/orderService.ts`
- [x] T104 [US4] Handle stock conflict errors (400) with affected item highlighting and redirect to cart in `frontend/src/pages/Checkout.vue`
- [x] T105 [US4] Add audit logging for checkout and auth events in `services/order-service/` and `services/user-service/`

**Checkpoint**: User Story 4 fully functional — end-to-end purchase flow completes with Saga and cart clearing

---

## Phase 7: User Story 5 - Order History & Shipment Tracking (Priority: P5)

**Goal**: Order list (newest first), order detail with items/address/amounts, and shipment tracking display

**Independent Test**: Place order (or use seeded data) → open order history → view detail → see shipment status when shipped; empty state when no orders

### Implementation for User Story 5

- [x] T106 [US5] Implement OrderQueryService (list by userId paginated newest-first, get detail with items and address snapshot) in `services/order-service/src/main/java/`
- [x] T107 [US5] Implement ShipmentService (read status, carrier, tracking number from Shipment entity) in `services/order-service/src/main/java/`
- [x] T108 [US5] Implement internal order REST controller in `services/order-service/src/main/java/` — GET `/internal/orders`, GET `/internal/orders/{orderId}`
- [x] T109 [US5] Implement admin/batch shipment status update endpoint (CSV import or REST) in `services/order-service/src/main/java/` for v1 manual carrier updates
- [x] T110 [US5] Implement BFF order endpoints in `shop-bff/src/main/java/` — GET `/orders`, GET `/orders/{orderId}` per OpenAPI contract
- [x] T111 [P] [US5] Create OrderListItem component in `frontend/src/components/OrderListItem.vue`
- [x] T112 [P] [US5] Create ShipmentTracker component in `frontend/src/components/ShipmentTracker.vue`
- [x] T113 [US5] Create Orders list page (newest-first, order number, date, total, status) in `frontend/src/pages/Orders.vue`
- [x] T114 [US5] Create OrderDetail page (items, address, payment summary, shipment info) in `frontend/src/pages/OrderDetail.vue`
- [x] T115 [US5] Add empty state with link to homepage in `frontend/src/pages/Orders.vue`
- [x] T116 [US5] Show estimated next update message when shipment status is stale in `frontend/src/components/ShipmentTracker.vue`

**Checkpoint**: All user stories independently functional — full shopper journey from browse to post-purchase tracking

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Quality gates, E2E validation, and production readiness

- [x] T117 [P] Add unit tests for product-service (ProductService, ProductSearchService) in `services/product-service/src/test/` with Testcontainers for MySQL + ES
- [x] T118 [P] Add unit tests for user-service (AuthService, lockout logic) in `services/user-service/src/test/` with Testcontainers for MySQL + Redis
- [x] T119 [P] Add unit tests for cart-service (CartService, guest merge) in `services/cart-service/src/test/` with Testcontainers
- [x] T120 [P] Add unit tests for order-service Saga handlers in `services/order-service/src/test/` with Testcontainers for MySQL + RabbitMQ
- [x] T121 [P] Add Vitest unit tests for Pinia stores in `frontend/tests/unit/`
- [x] T122 Configure Jacoco coverage report (≥ 70% threshold) in root `pom.xml` and enforce in `.github/workflows/ci.yml`
- [x] T123 Implement Cypress E2E full shopper journey in `frontend/tests/e2e/shopper-journey.cy.ts` (browse → register → cart → checkout → order history)
- [x] T124 [P] Add OpenAPI contract validation test comparing BFF responses to `specs/001-ecommerce-platform/contracts/shop-bff-api.yaml`
- [x] T125 [P] Add shared app layout (header nav, cart badge, auth menu) in `frontend/src/components/AppLayout.vue`
- [x] T126 Security hardening — rate limiting on `/api/auth/login` in gateway, input validation (Hibernate Validator) on all BFF request DTOs
- [x] T127 Run quickstart.md validation end-to-end and update `specs/001-ecommerce-platform/quickstart.md` with any fixes discovered

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup — **BLOCKS all user stories**
- **User Stories (Phase 3–7)**: All depend on Foundational completion
  - Recommended sequential order: US1 → US2 → US3 → US4 → US5 (matches priority and natural data flow)
  - US1 and US2 can partially parallelize after Foundational (different services)
- **Polish (Phase 8)**: Depends on all desired user stories being complete

### User Story Dependencies

| Story | Depends On | Notes |
|-------|-----------|-------|
| US1 (P1) | Foundational only | Fully independent — no auth/cart required |
| US2 (P2) | Foundational only | Independent of catalog beyond gateway public routes |
| US3 (P3) | Foundational + product-service internal API | Validates products on add; UI links from US1 pages |
| US4 (P4) | US2 (auth) + US3 (cart) + addresses | Checkout requires signed-in user with cart and address |
| US5 (P5) | US4 (orders exist) | Can use seeded orders for isolated testing |

### Within Each User Story

- Database migrations before entities
- Entities before services
- Services before REST controllers
- Backend endpoints before BFF aggregation
- BFF before frontend pages
- Story checkpoint before starting next priority

### Parallel Opportunities

- **Phase 1**: T002–T009, T011–T012 can run in parallel after T001
- **Phase 2**: T018, T020–T022 can run in parallel after T014–T017
- **US1**: T029–T031, T040–T041 in parallel
- **US2**: T048–T049, T060–T062 in parallel
- **US3**: T067–T068, T076 in parallel
- **US4**: T082–T083, T086–T087, T100 in parallel
- **US5**: T111–T112 in parallel
- **Polish**: T117–T121, T124–T125 in parallel

---

## Parallel Example: User Story 1

```bash
# Backend entities in parallel:
Task T029: Flyway migration in services/product-service/src/main/resources/db/migration/V1__product_schema.sql
Task T030: JPA entities in services/product-service/src/main/java/
Task T031: Repositories in services/product-service/src/main/java/

# Frontend components in parallel (after BFF endpoints ready):
Task T040: ProductCard in frontend/src/components/ProductCard.vue
Task T041: ProductFilter in frontend/src/components/ProductFilter.vue
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (**CRITICAL**)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Browse homepage, search, filter, product detail — no login needed
5. Demo/deploy catalog MVP

### Incremental Delivery

1. Setup + Foundational → infrastructure ready
2. US1 → catalog browsing MVP → Demo
3. US2 → authentication → Demo
4. US3 → cart management → Demo
5. US4 → checkout + Saga → Demo (first revenue milestone)
6. US5 → order history + tracking → Demo (full v1)
7. Polish → quality gates + E2E

### Parallel Team Strategy

With multiple developers after Foundational:

- **Developer A**: US1 (product-service + catalog frontend)
- **Developer B**: US2 (user-service + auth frontend)
- **Developer C**: US3 (cart-service + cart frontend)

Then converge for US4 (checkout Saga touches all services) and US5.

---

## Notes

- v1 excludes online payment and live carrier APIs per spec assumptions
- Guest cart uses Redis session; signed-in cart uses MySQL — merge on login (research R5)
- Checkout uses choreography Saga via RabbitMQ — no 2PC (research R4, constitution Principle III)
- All frontend API calls go through gateway `/api/**` → shop-bff only — no direct microservice URLs
- Commit after each task or logical group; stop at any checkpoint to validate story independently
