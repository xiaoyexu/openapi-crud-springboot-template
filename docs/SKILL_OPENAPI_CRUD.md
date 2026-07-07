# OpenAPI CRUD Skill (Spring Boot)

> Need an execution-ready version for coding agents? See: `docs/SKILL_OPENAPI_CRUD_AI.md`

## What this skill solves

Use this skill to quickly build a production-oriented CRUD service with:

- Contract-first API design (OpenAPI)
- Generated API models/interfaces
- Reusable CRUD service foundation
- JWT + RBAC authorization
- Audit logging for DB changes
- Unified error/response handling
- Regression-safe tests with coverage gates

---

## Skill inputs

Before execution, collect:

- Domain entities and field definitions
- Business validation rules
- Permission model (`entity:action`, wildcard, ownership)
- API behavior expectations (pagination, sorting, import/export)
- Error code/status expectations

---

## Skill outputs

A runnable Spring Boot module containing:

- OpenAPI contract and generated DTO/API interfaces
- Adapter layer implementations
- Service layer with generic CRUD inheritance
- DB entity/mapper/service wiring
- Security integration (JWT + method permissions)
- Audit logging integration
- Integration/auth/error-path tests

---

## Execution checklist (SOP)

### 1) Define the contract first

- Write resource paths, request/response DTOs, and error shapes in OpenAPI
- Include search, pagination, sorting, import, and export endpoints where needed
- Keep status/error semantics explicit in spec and examples

### 2) Generate boundary code

- Run OpenAPI generator to produce DTOs and delegate interfaces
- Do not handwrite duplicate request/response models

### 3) Implement adapter as thin orchestration

- Accept generated request models
- Call service methods only
- Apply permission annotations
- Convert `AppResponse` to HTTP response consistently

### 4) Implement service by extending generic CRUD base

- Reuse common `get/create/update/delete/search/import/export`
- Override only business-specific hooks:
  - `validate(...)`
  - `buildQuery(...)`
  - import/export row mapping
  - partial update merge logic

### 5) Keep entity/DTO mapping explicit

- Maintain separation between API DTO and DB entity
- Use mapper component for all conversions

### 6) Apply error strategy consistently

- Predictable business failures: return fail response with defined status
- Unexpected/system failures: throw exception and let global handler process
- Keep transactional DB calls wrapped in centralized error boundary

### 7) Add security and authorization

- Parse/validate JWT in filter
- Populate request context (user id, authorities)
- Enforce `@PreAuthorize` rules at adapter/service boundary
- Support wildcard/ownership checks in permission service

### 8) Enable audit trail

- Intercept INSERT/UPDATE/DELETE in data layer
- Persist before/after snapshots into audit storage

### 9) Build test matrix from contract

- CRUD happy paths
- validation failures
- authn/authz failures (role/permission permutations)
- search/pagination/sort behavior
- import/export paths

### 10) Gate quality in CI

- generated code sync check
- all tests pass
- coverage threshold gate (line/branch)

---

## Reusable prompt template

```text
Use the "OpenAPI CRUD Skill" to implement a new resource.

Inputs:
- Resource name: <RESOURCE>
- Fields: <FIELDS>
- Validation rules: <RULES>
- Permissions: <PERMISSIONS>
- Endpoints needed: CRUD + <SEARCH/IMPORT/EXPORT>

Requirements:
1. Define OpenAPI contract first, including status/error examples.
2. Generate API DTOs/interfaces, then implement adapter delegates.
3. Implement service by extending generic CRUD base and only overriding business hooks.
4. Keep DTO/entity mapping explicit via mapper.
5. Add JWT + RBAC checks and ownership rules where needed.
6. Ensure audit logging for DB write operations.
7. Add integration tests and auth tests for key paths.
8. Keep response/error semantics consistent with global strategy.
9. Provide a file-by-file change list and verification commands.
```

---

## Definition of done

A resource implementation is done when:

- OpenAPI contract and generated models are in sync
- Adapter is thin and policy-focused
- Service logic is mostly hook overrides on generic CRUD base
- Error behavior is predictable and documented
- Security and audit behavior are covered by tests
- CI quality gates pass
