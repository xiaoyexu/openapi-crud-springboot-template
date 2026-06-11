# OpenAPI CRUD Spring Boot Template

A production-ready Spring Boot template for building CRUD RESTful APIs with OpenAPI specification, JWT authentication, role-based authorization, and comprehensive audit logging.

Demo at [www.xuxiaoye.com/tadmin/](https://www.xuxiaoye.com/tadmin/)

## Features

- **OpenAPI-Driven Development**: Define your API in OpenAPI/Swagger specification and auto-generate server stubs
- **CRUD Operations**: Full Create, Read, Update, Delete, Search, Import, and Export capabilities
- **JWT Authentication**: Secure your APIs with JWT access and refresh tokens using RSA asymmetric keys
- **Role-Based Authorization**: Flexible permission system with support for ownership-based access control
- **Audit Logging**: Automatic tracking of all database changes with before/after snapshots
- **Excel Import/Export**: Bulk data operations via Excel files
- **Multi-Database Support**: Configured for MySQL with dynamic datasource support
- **Pagination & Sorting**: Built-in support for paginated results with customizable sorting
- **Comprehensive Testing**: Contract-based testing with Spring Cloud Contract

## Technology Stack

| Component         | Technology        | Version  |
|-------------------|-------------------|----------|
| Framework         | Spring Boot       | 3.5.14   |
| Database          | MyBatis Plus      | 3.5.10.1 |
| API Documentation | SpringDoc OpenAPI | 2.8.5    |
| Code Generation   | OpenAPI Generator | 7.12.0   |
| Authentication    | JJWT              | 0.12.3   |
| Excel Processing  | FastExcel         | 0.18.0   |
| Java Version      | OpenJDK           | 17+      |

---

## Design Methodology

### 1. OpenAPI-First Development

This template follows an **OpenAPI-first** approach where the API contract is defined first in YAML format, and all server stubs, DTOs, and controller interfaces are auto-generated.

```
OpenAPI Specification (YAML) → OpenAPI Generator → DTOs + Controller Stubs
```

**Benefits:**

- Frontend and backend teams can work in parallel
- API documentation is always in sync with implementation
- Type-safe DTOs are generated automatically
- Contract testing can be implemented easily

### 2. Layered Architecture

The application follows a clear layered architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ OpenAPI Adapter │  │  Controllers    │  │  Interceptors│ │
│  │ (Generated)     │  │  (Generated)    │  │  (JWT, etc.) │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ CRUDDbClient    │  │ ServiceImpl     │  │  Permission  │ │
│  │ (Base CRUD)     │  │ (Business Logic)│  │  Service     │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ MyBatis Plus    │  │ DB Mappers      │  │  DB Entities │ │
│  │ BaseMapper      │  │ (Generated)     │  │  (DBEntity)  │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 3. Generic CRUD Pattern with Type Safety

The `CRUDDbClient` provides a generic base implementation for all CRUD operations:

```java
public abstract class CRUDDbClient<
        PresentDto,           // API-facing DTO (from OpenAPI)
        SearchRequest,         // Search/filter request DTO
        PresentPagedEntities,  // Paginated response DTO
        PresentMapper,          // Mapper between PresentDto and Entity
        Entity extends DBEntity<String>,  // Database entity
        DBMapper extends BaseMapper<Entity>,  // MyBatis mapper
        DBService extends ServiceImpl<DBMapper, Entity>  // MyBatis Plus service
> extends BaseDbClient implements Service<...>
```

**Benefits:**

- Single implementation handles all CRUD operations
- Type-safe at compile time
- Easy to extend for custom business logic
- Consistent API response format across all endpoints

### 4. Separation of Concerns: Present DTO vs DB Entity

The template maintains a clear separation between:

| Layer          | DTO Type     | Purpose                                  |
|----------------|--------------|------------------------------------------|
| API/Controller | `PresentDto` | External API contract, OpenAPI generated |
| Database       | `Entity`     | Database schema, MyBatis Plus mapped     |

**Mapping is handled by `PresentMapper`:**

- `mapToPresent(entity)` - DB Entity → PresentDto
- `mapToDB(presentDto)` - PresentDto → DB Entity
- `mapListToPresent(entities)` - Batch conversion

### 5. Request Context Propagation

The `RequestContext` bean carries request-scoped information throughout the application:

```java
// Set by JWTInterceptor
requestContext.setXUserId(userId);
requestContext.setAuthorization(authorization);

// Used by CRUDDbClient for audit fields
dbEntity.setCreatedBy(this.requestContext.getXUserId());
dbEntity.setUpdatedBy(this.requestContext.getXUserId());
```

---

## Architecture Details

### Core Components

#### 1. BaseDbClient

Provides database query utilities and transaction handling:

- `addFilter()` - Add various filter operators (IN, LIKE, DATE_RANGE, etc.)
- `applyMultiColumnKeyWordFilter()` - Search across multiple columns
- `addSortField()` - Add sorting with direction control
- `handleDbCall()` - Centralized exception handling with rollback

#### 2. CRUDDbClient

Implements the `Service` interface with full CRUD operations:

- `get(id)` - Retrieve single entity
- `create(entity)` - Create with auto-generated UUID
- `updateById(id, entity)` - Update with audit fields
- `deleteById(id)` - Soft delete support
- `search(request, pagination)` - Paginated search
- `importData(file)` - Excel bulk import
- `exportData(request, pagination)` - Excel export

#### 3. TableAuditLogInterceptor

MyBatis interceptor for automatic audit logging:

- Intercepts INSERT, UPDATE, DELETE operations
- Captures before/after state of entities
- Stores audit records asynchronously (`@Async`)
- Supports audit tables with `{TABLE}_AUDIT` naming convention

#### 4. JWTInterceptor

Handles authentication and authorization:

- Validates JWT tokens using RSA public key
- Extracts user ID, roles, and authorities from claims
- Sets Spring Security context
- Prevents duplicate requests using trace ID cache

#### 5. PermissionServiceImpl

Implements Spring Security's `PermissionEvaluator`:

- Supports wildcard permissions (`*:*`)
- Supports entity-specific permissions (`student:search`)
- Supports ownership-based permissions (`student:search_own`)
- Caches permission checks for performance

### Database Design

#### Base Entity Structure

```java
@Data
public abstract class DBEntity<T> {
    protected T id;                    // Primary key (UUID)
    protected String createdBy;       // Creator user ID
    protected LocalDateTime createdAt; // Creation timestamp
    protected String updatedBy;       // Last modifier user ID
    protected LocalDateTime updatedAt; // Last modification timestamp
}
```

#### Audit Entity Structure

Each entity has a corresponding audit entity with additional fields:

- `action` - 'A' (Add), 'U' (Update), 'D' (Delete)
- All original entity fields preserved

### Security Model

#### JWT Token Structure

```json
{
  "sub": "user-id",
  "id": "user-id",
  "roles": "admin,user",
  "authorities": "student:search,student:create",
  "iat": 1234567890,
  "exp": 1234571490
}
```

#### Permission Format

- `{entity}:{action}` - Specific permission (e.g., `student:search`)
- `{entity}:{action}_own` - Ownership-based permission (e.g., `student:delete_own`)
- `{entity}:*` - All actions on entity (e.g., `student:*`)
- `*:*` - Superuser permission (Admin only)

#### Role-Based Access Matrix

| Role   | Create | Search | Get | Delete | Export | Import |
|--------|--------|--------|-----|--------|--------|--------|
| Admin  | ✓      | ✓      | ✓   | ✓      | ✓      | ✓      |
| Member | ✓      | ✓      | ✓   | -      | ✓      | ✓      |
| Owner  | ✓      | Own    | Own | Own    | Own    | -      |
| Guest  | -      | -      | -   | -      | -      | -      |

### Response Pattern

All API responses follow a consistent pattern via `AppResponse<T>`:

```java
@Data
@Builder
public class AppResponse<T> implements Serializable {
    private T data;           // Response payload
    private AppStatus status; // Status code and message
}
```

**Status Codes:**

- `200` - Success
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

---

## Project Structure

```
src/
├── main/
│   ├── java/com/xuxiaoye/api/
│   │   ├── Application.java              # Main application entry
│   │   ├── adapter/                      # Generated OpenAPI adapters
│   │   │   ├── api/server/               # Server-side API definitions
│   │   │   │   ├── dto/                  # Generated DTOs
│   │   │   │   └── Api.java              # Generated API interface
│   │   │   └── server/
│   │   │       └── mapper/               # Generated MyBatis mappers
│   │   ├── bean/                         # Core beans and DTOs
│   │   │   ├── JWT.java
│   │   │   ├── PagedEntity.java
│   │   │   ├── Pagination.java
│   │   │   └── RequestContext.java
│   │   ├── client/                       # Base clients for DB operations
│   │   │   ├── BaseDbClient.java         # Query utilities
│   │   │   └── CRUDDbClient.java         # CRUD implementation
│   │   ├── conf/                         # Configuration classes
│   │   │   ├── AdapterConfig.java
│   │   │   ├── MybatisPlusConfig.java
│   │   │   ├── ServiceConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── WebSecurityConfig.java
│   │   ├── constant/                      # Application constants
│   │   ├── interceptors/                 # Request interceptors
│   │   │   ├── JWTInterceptor.java
│   │   │   ├── RequestContextInterceptor.java
│   │   │   └── TableAuditLogInterceptor.java
│   │   ├── interfaces/                   # Custom interfaces
│   │   │   └── OwnerChecker.java
│   │   ├── resp/                         # Response wrappers
│   │   │   ├── AppResponse.java
│   │   │   ├── AppStatus.java
│   │   │   └── FileResponse.java
│   │   ├── services/                     # Business logic implementations
│   │   │   ├── interfaces/               # Service interfaces
│   │   │   └── db/                        # Database entities and mappers
│   │   │       ├── dto/entity/            # DB entities
│   │   │       ├── dto/mapper/            # Entity mappers
│   │   │       └── mapper/                # MyBatis mappers
│   │   └── utils/                        # Utility classes
│   │       ├── DateTimeUtils.java
│   │       ├── ExcelHelper.java
│   │       ├── JwtUtils.java
│   │       └── ...
│   └── resources/
│       ├── application.yaml               # Main configuration
│       ├── application-local.yaml        # Local development config
│       ├── db/db.sql                     # Database schema
│       └── swagger/server/sample.yaml    # OpenAPI specification
└── test/
    ├── java/                             # Unit and integration tests
    └── resources/
        ├── apis/                         # API test fixtures
        ├── contracts/                    # Contract tests
        └── test_certs/                   # Test certificates
```

---

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+ (or H2 for testing)

### Build & Run

```bash
# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/openapi-service.jar
```

### Run Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:check
```

---

## Configuration

### Environment Variables

| Variable                 | Description           | Default     |
|--------------------------|-----------------------|-------------|
| `DB_HOST`                | Database host         | `localhost` |
| `DB_PORT`                | Database port         | `3306`      |
| `DB_SCHEMA`              | Database name         | `dev`       |
| `DB_USERNAME`            | Database username     | `root`      |
| `DB_PASSWORD`            | Database password     | `root`      |
| `WEB_BASE_PATH`          | API base path         | `/api/v1`   |
| `BY_PASS_TOKEN_CHECK`    | Skip JWT validation   | `false`     |
| `ACCESS_EXPIRE_SECONDS`  | Access token expiry   | `1800`      |
| `REFRESH_EXPIRE_SECONDS` | Refresh token expiry  | `7200`      |
| `LOG_LEVEL`              | Application log level | `debug`     |

### Database Setup

Execute the SQL script to create the required tables:

```bash
mysql -u root -p dev < src/main/resources/db/db.sql
```

---

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:6666/swagger
```

---

## API Examples

### Authentication

**Login:**

```bash
curl -X POST http://localhost:6666/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"accountName":"username1","password":"password"}'
```

**Refresh Token:**

```bash
curl -X POST http://localhost:6666/api/v1/users/refresh \
  -H "Authorization: Bearer <refresh_token>"
```

### CRUD Operations

**Create:**

```bash
curl -X POST http://localhost:6666/api/v1/students \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","age":20,"height":175.5}'
```

**Search:**

```bash
curl -X POST http://localhost:6666/api/v1/students/search \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"John"}'
```

**Get by ID:**

```bash
curl http://localhost:6666/api/v1/students/ST000001 \
  -H "Authorization: Bearer <access_token>"
```

**Update:**

```bash
curl -X PUT http://localhost:6666/api/v1/students/ST000001 \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","age":21}'
```

**Delete:**

```bash
curl -X DELETE http://localhost:6666/api/v1/students/ST000001 \
  -H "Authorization: Bearer <access_token>"
```

### Import/Export

**Export to Excel:**

```bash
curl -X POST http://localhost:6666/api/v1/students/export \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{}' \
  --output students.xlsx
```

**Import from Excel:**

```bash
curl -X POST http://localhost:6666/api/v1/students/import \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@students.xlsx"
```

---

## Creating New Entities

### Step-by-Step Guide

1. **Define OpenAPI Specification** in `src/main/resources/swagger/server/sample.yaml`

   ```yaml
   /new-entities:
     post:
       operationId: createSingleNewEntity
       ...
   ```

2. **Generate Code:**

   ```bash
   mvn clean compile
   ```

   This triggers the OpenAPI Generator to create DTOs and controller stubs.

3. **Create Database Entity** in `src/main/java/com/xuxiaoye/api/services/db/dto/entity/`

   ```java
   @Data
   @EqualsAndHashCode(callSuper = true)
   public class NewEntity extends DBEntity<String> {
       private String name;
       private Integer value;
   }
   ```

4. **Create DB Mapper** extending `BaseMapper<Entity>`

   ```java
   @Mapper
   public interface NewEntityDBMapper extends BaseMapper<NewEntity> {
   }
   ```

5. **Create DB Service** extending `ServiceImpl<DBMapper, Entity>`

   ```java
   @Service
   public class NewEntityDBService extends ServiceImpl<NewEntityDBMapper, NewEntity> {
   }
   ```

6. **Create PresentMapper** for DTO conversion

   ```java
   @Mapper
   public interface NewEntityMapper {
       NewEntityDto mapToPresent(NewEntity entity);
       NewEntity mapToDB(NewEntityDto dto);
       List<NewEntityDto> mapListToPresent(List<NewEntity> entities);
   }
   ```

7. **Create Service Interface** in `src/main/java/com/xuxiaoye/api/services/interfaces/`

   ```java
   public interface NewEntityService extends Service<...> {
       // Custom methods
   }
   ```

8. **Implement Service** extending `CRUDDbClient<...>`

   ```java
   @Service
   public class NewEntityServiceImpl extends CRUDDbClient<...> implements NewEntityService {
       // Implement custom logic
   }
   ```

9. **Register in ServiceConfig** as a Spring Bean

   ```java
   @Bean
   NewEntityService newEntityService(...) {
       return new NewEntityServiceImpl(...);
   }
   ```

10. **Implement Controller** (generated, just implement the delegate)

    ```java
    @RestController
    public class NewEntityApiController implements NewEntityApiDelegate {
        @Autowired
        NewEntityService newEntityService;

        // Override delegate methods
    }
    ```

---

## Audit Logging

The `TableAuditLogInterceptor` automatically logs all database changes:

| Action | Description |
| ------ | ----------- |
| **A**  | Insert/Add  |
| **U**  | Update      |
| **D**  | Delete      |

Audit tables follow the naming convention `{TABLE}_AUDIT` and store:

- Complete record state before and after changes
- Action type (A/U/D)
- Timestamp and user information

---

## Testing Strategy

### Unit Tests

- Service layer tests with mocked dependencies
- Utility class tests
- Bean validation tests

### Integration Tests

- Database integration with H2 in-memory database
- API endpoint tests with MockMvc

### Contract Tests

- Spring Cloud Contract for API contract validation
- Consumer-driven contract testing

---

## License

This project is licensed under the MIT License.
