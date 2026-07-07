# OpenAPI CRUD Skill - AI Playbook

This file is an execution-ready playbook for AI coding assistants working in this repository.

## 1) Role and objective

You are implementing a new CRUD resource in this Spring Boot template using an OpenAPI-first approach.

Your objective is to deliver a production-ready resource with:

- OpenAPI contract updates
- Generated DTO/API boundaries
- Thin adapter layer
- Service implementation based on generic CRUD base
- RBAC + ownership permission checks
- Audit-safe DB writes
- Integration and authorization tests

## 2) Repository anchors (must follow)

- OpenAPI spec: `src/main/resources/swagger/server/sample.yaml`
- Generic CRUD base: `src/main/java/com/xuxiaoye/api/client/CRUDDbClient.java`
- DB query/error base: `src/main/java/com/xuxiaoye/api/client/BaseDbClient.java`
- Response envelope: `src/main/java/com/xuxiaoye/api/resp/AppResponse.java`
- Global exception mapping: `src/main/java/com/xuxiaoye/api/common/exceptions/GlobalExceptionHandler.java`
- Security filter: `src/main/java/com/xuxiaoye/api/interceptors/JWTAuthenticationFilter.java`
- Permission logic: `src/main/java/com/xuxiaoye/api/services/PermissionServiceImpl.java`
- Audit interceptor: `src/main/java/com/xuxiaoye/api/interceptors/TableAuditLogInterceptor.java`
- Service bean wiring: `src/main/java/com/xuxiaoye/api/conf/ServiceConfig.java`
- Adapter bean wiring: `src/main/java/com/xuxiaoye/api/conf/AdapterConfig.java`

## 3) Required input contract (from user/task)

Collect these fields before coding:

```yaml
resourceName: string                # Example: Student
resourceNamePlural: string          # Example: Students
entityIdType: string                # Example: String
apiBasePath: string                 # Example: /api/v1/students
fields:
  - name: string
    type: string                    # string|integer|number|boolean|date-time|...
    required: boolean
    constraints: string             # Optional
validationRules:
  - string
permissions:
  create: string                    # Example: student:create
  read: string                      # Example: student:get
  search: string                    # Example: student:search
  update: string                    # Example: student:update
  delete: string                    # Example: student:delete
  export: string                    # Optional
  import: string                    # Optional
ownershipEnabled: boolean
features:
  search: boolean
  pagination: boolean
  sorting: boolean
  import: boolean
  export: boolean
```

If required fields are missing, ask focused questions before implementation.

## 4) Non-negotiable implementation rules

- OpenAPI first, implementation second.
- Do not handwrite API DTOs that are generated from OpenAPI.
- Adapter layer must stay thin: policy/orchestration only.
- Service layer should reuse `CRUDDbClient` and override only business hooks.
- Keep API DTO and DB entity mapping explicit via mapper component.
- Use repository response/exception strategy consistently.
- Add tests for happy path, validation failures, and auth failures.

## 5) Execution workflow

### Step A - OpenAPI contract

1. Add/adjust resource paths in `sample.yaml`.
2. Define schemas for create/update/read/search payloads.
3. Include examples for success and error responses.
4. Keep operationId names aligned with existing naming style.

### Step B - Generate boundary code

1. Run compile/generation flow.
2. Confirm generated API interfaces and DTOs are updated.

### Step C - Data layer

1. Add DB entity extending `DBEntity<IdType>`.
2. Add MyBatis mapper extending `BaseMapper<Entity>`.
3. Add DB service extending `ServiceImpl<Mapper, Entity>`.

### Step D - Mapping layer

1. Add mapper for DTO <-> entity conversion.
2. Ensure list conversion methods exist.

### Step E - Service layer

1. Add service interface extending project `Service<...>` pattern.
2. Implement service via `CRUDDbClient<...>`.
3. Override only required hooks:
   - validation logic
   - query filter/sort building
   - import row mapping
   - export row writing
   - merge/patch behavior if needed

### Step F - Adapter layer

1. Implement generated delegate in adapter package.
2. Apply `@PreAuthorize` checks per endpoint.
3. Convert service `AppResponse` to `ResponseEntity` consistently.

### Step G - Wiring and config

1. Register service beans in `ServiceConfig` if required by existing style.
2. Register adapter beans in `AdapterConfig` if required by existing style.

### Step H - Test coverage

1. Add integration tests for CRUD and search.
2. Add auth tests for role/permission denial and allow cases.
3. Add negative tests for validation and not-found behavior.

### Step I - Quality gate check

1. Ensure generated code and source changes are synchronized.
2. Ensure tests pass locally.
3. Ensure coverage threshold policies are respected.

## 6) Output format for AI responses

When finishing, always output in this order:

1. `Checklist` of completed steps (A-I)
2. `Changed files` list with one-line reason per file
3. `Risk notes` (if any)
4. `Verification commands`
5. `Next options` (numbered, 2-3 items)

## 7) Verification command template

Use these commands as a baseline and adjust if needed:

```bash
mvn clean compile
mvn test
mvn jacoco:check
```

## 8) Ready-to-use prompt block

```text
Follow docs/SKILL_OPENAPI_CRUD_AI.md strictly.
Implement a new resource with OpenAPI-first workflow in this repository.

Inputs:
- resourceName: <RESOURCE>
- resourceNamePlural: <RESOURCE_PLURAL>
- entityIdType: <ID_TYPE>
- apiBasePath: <API_BASE_PATH>
- fields: <FIELDS>
- validationRules: <RULES>
- permissions: <PERMISSIONS>
- ownershipEnabled: <true|false>
- features: <FEATURE_FLAGS>

Requirements:
- Update OpenAPI contract first.
- Reuse CRUDDbClient pattern; keep adapter thin.
- Apply consistent AppResponse/exception semantics.
- Add integration + auth + negative tests.
- Return checklist, changed files, risks, and verification commands.
```

## 9) Definition of done

A task is done only if all are true:

- Contract and generated artifacts are consistent
- Endpoint behavior matches permission model
- Validation and error paths are covered by tests
- No duplicated DTO definitions outside generated boundaries
- Service logic is primarily hook overrides on generic CRUD base
- Build/test/coverage commands are provided for verification

## 10) Example input and expected output

### Example input (Student)

```yaml
resourceName: Student
resourceNamePlural: Students
entityIdType: String
apiBasePath: /api/v1/students
fields:
  - name: id
    type: string
    required: false
    constraints: generated server-side
  - name: name
    type: string
    required: true
    constraints: non-empty, maxLength=100
  - name: age
    type: integer
    required: true
    constraints: min=1, max=150
  - name: height
    type: number
    required: false
    constraints: min=30, max=300
validationRules:
  - name is mandatory for create/update
  - age must be between 1 and 150
permissions:
  create: student:create
  read: student:get
  search: student:search
  update: student:update
  delete: student:delete
  export: student:export
  import: student:import
ownershipEnabled: true
features:
  search: true
  pagination: true
  sorting: true
  import: true
  export: true
```

### Expected AI output style (abbreviated)

```text
Checklist
- [x] Step A OpenAPI contract updated
- [x] Step B Generated boundaries refreshed
- [x] Step C Data layer added
- [x] Step D Mapper added
- [x] Step E Service implemented via CRUDDbClient hooks
- [x] Step F Adapter delegate + @PreAuthorize added
- [x] Step G Bean wiring updated
- [x] Step H Tests added
- [x] Step I Quality checks executed

Changed files
- src/main/resources/swagger/server/sample.yaml (add Student endpoints/schemas)
- src/main/java/.../StudentServiceImpl.java (CRUD hooks + validation/query)
- src/main/java/.../StudentAdapter.java (delegate endpoints + auth annotations)
- src/test/java/.../StudentAdapterTest.java (CRUD/search integration)
- src/test/java/.../StudentAdapterAuthTest.java (permission matrix)

Risk notes
- AuthorizationDeniedException mapping should align with 403 policy.
- Import template column order must match mapper.

Verification commands
mvn clean compile
mvn test
mvn jacoco:check

Next options
1) Generate example curl contract tests.
2) Add import/export fixture files.
3) Add ownership-only authorization scenarios.
```

### Common failure patterns to avoid

- Implementing controller logic in adapter instead of service hooks
- Handwriting DTOs that should be generated from OpenAPI
- Returning inconsistent status mapping outside `AppResponse` strategy
- Missing auth negative tests for forbidden/unauthorized paths
- Skipping import/export path validation when feature flags enable them
