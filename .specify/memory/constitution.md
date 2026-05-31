<!--
Sync Impact Report
- Version change: (template/unfilled) → 1.0.0
- Modified principles: N/A (initial ratification from CONSTITUTION.md)
- Added sections: Core Principles (5), Technology Stack Constraints, Development Workflow, Governance
- Removed sections: None
- Templates requiring updates:
  - .specify/templates/plan-template.md ✅ updated
  - .specify/templates/spec-template.md ✅ no changes required (already aligned)
  - .specify/templates/tasks-template.md ✅ no changes required (already aligned)
- Follow-up TODOs: None
-->

# E-Commerce Platform (PC Web) Constitution

## Core Principles

### I. Microservice Boundaries

Each business domain (product, order, cart, user) MUST be an independent microservice.
Services MUST NOT access another service's database directly. Inter-service communication
MUST use RESTful APIs (synchronous) or RabbitMQ events (asynchronous) only.

**Rationale**: Clear boundaries enable independent deployment, scaling, and team ownership
while preventing hidden coupling through shared data stores.

### II. Frontend-Backend Separation

The Vue 3 + Element Plus frontend MUST consume data through a BFF or aggregated API layer.
The frontend MUST NOT call microservices directly. All external traffic MUST enter through
Spring Cloud Gateway.

**Rationale**: A single gateway enforces authentication, routing, rate limiting, and shields
internal service topology from clients.

### III. Data Consistency (NON-NEGOTIABLE)

Cross-service operations (e.g., order placement with inventory deduction) MUST use Saga
patterns or reliable messaging for eventual consistency. Direct distributed transactions
across service databases are forbidden.

**Rationale**: Microservices require explicit consistency models; implicit two-phase commit
across boundaries creates fragility and operational risk.

### IV. Test & Quality Gates

- Backend unit test coverage MUST be ≥ 70% (Jacoco).
- Core flows (order, payment) MUST include integration tests using Testcontainers
  (MySQL, Elasticsearch, RabbitMQ).
- Frontend critical paths MUST include Vitest unit tests and Cypress E2E tests for
  primary user journeys.
- All REST APIs MUST follow OpenAPI 3.0 with documented request/response schemas.
- Error responses MUST use the unified format:
  `{ "code": "ORDER_NOT_FOUND", "message": "订单不存在" }`.

**Rationale**: Measurable quality gates prevent regressions in revenue-critical paths and
ensure contract stability between services and clients.

### V. Security & Observability

- All user input MUST be validated (Hibernate Validator + custom annotations).
- Sensitive operations MUST produce auditable operation logs.
- JWT access tokens MUST expire within 2 hours; refresh tokens MUST be stored in Redis
  with sliding expiration.
- Services MUST expose metrics via Micrometer → Prometheus → Grafana (QPS, latency,
  error rate).
- Critical paths (search, order, payment) MUST have distributed tracing (Sleuth + Zipkin).
- Application logs MUST be structured JSON collected to ELK.

**Rationale**: E-commerce systems require provable security controls and end-to-end
visibility for incident response and SLA management.

## Technology Stack Constraints

| Layer | Technology | Version | Constraints |
|-------|------------|---------|-------------|
| Backend | Java | ≥ 17 | Use modern language features (Records, pattern matching) |
| Microservices | Spring Boot + Spring Cloud | 2023.x+ | Nacos for registry and config |
| Database | MySQL | 8.0+ | Read/write split; one schema per service |
| Search | Elasticsearch | 8.x | Real-time product sync; keyword + attribute filtering |
| Messaging | RabbitMQ | 3.12+ | Async decoupling for orders, inventory, notifications |
| Frontend | Vue 3 + TypeScript | ≥ 3.4 | Composition API; `strict: true`; no `any` |
| UI | Element Plus | ≥ 2.7 | MUST NOT override core component themes |
| Build | Vite | ≥ 5.0 | Production compression and tree-shaking enabled |

**Coding standards**: Backend follows Google Java Style Guide. Frontend components use
PascalCase naming.

**Deployment**: All services MUST be containerized (Docker) with a standard Dockerfile.
Local development MUST support Docker Compose; production MUST run on Kubernetes with
per-service CI/CD pipelines (GitHub Actions recommended).

## Development Workflow

All feature work MUST follow the Spec Kit sequence: `/speckit-specify` → `/speckit-plan` →
`/speckit-tasks` → `/speckit-implement`. Implementation without an approved spec and plan
is prohibited.

**AI-assisted development (Cursor Agent)**:

- Generated code MUST comply with this constitution; MUST NOT bypass validation, hardcode
  configuration, or use deprecated/non-standard APIs.
- Complex logic (state machines, concurrency) MUST include comments explaining intent and
  boundary conditions.
- All AI-generated code MUST pass human review before merge.

**Code review gates**: PRs MUST verify constitution compliance. Any deviation MUST be
documented, reviewed by the team, and recorded.

## Governance

This constitution supersedes all other development practices for the E-Commerce Platform.
Amendments require:

1. Documented rationale and impact analysis.
2. Version bump per semantic versioning (see below).
3. Propagation to dependent Spec Kit templates and agent context.
4. Team approval before ratification of MAJOR changes.

**Compliance review**: Every `/speckit-plan` Constitution Check section MUST be completed
before Phase 0 research. Re-check after Phase 1 design. `/speckit-analyze` treats
constitution violations as CRITICAL.

**Versioning policy**:

- MAJOR: Backward-incompatible principle removal or redefinition.
- MINOR: New principle or materially expanded guidance.
- PATCH: Clarifications, wording, non-semantic refinements.

Runtime development guidance: see `CONSTITUTION.md` (Chinese reference) and feature
specs under `specs/`.

**Version**: 1.0.0 | **Ratified**: 2025-11-21 | **Last Amended**: 2026-05-31
