# OpenAPI CRUD Spring Boot Template

A production-ready Spring Boot template for building CRUD RESTful APIs with OpenAPI specification, JWT authentication, role-based authorization, and comprehensive audit logging.

## Features

- **OpenAPI-Driven Development**: Define your API in OpenAPI/Swagger specification and auto-generate server stubs
- **CRUD Operations**: Full Create, Read, Update, Delete, Search, Import, and Export capabilities
- **JWT Authentication**: Secure your APIs with JWT access and refresh tokens
- **Role-Based Authorization**: Flexible permission system with support for ownership-based access control
- **Audit Logging**: Automatic tracking of all database changes with before/after snapshots
- **Excel Import/Export**: Bulk data operations via Excel files
- **Multi-Database Support**: Configured for MySQL with dynamic datasource support
- **Pagination & Sorting**: Built-in support for paginated results with customizable sorting
- **Comprehensive Testing**: Contract-based testing with Spring Cloud Contract

## Technology Stack

| Component         | Technology        | Version  |
| ----------------- | ----------------- | -------- |
| Framework         | Spring Boot       | 3.5.14   |
| Database          | MyBatis Plus      | 3.5.10.1 |
| API Documentation | SpringDoc OpenAPI | 2.8.5    |
| Code Generation   | OpenAPI Generator | 7.12.0   |
| Authentication    | JJWT              | 0.12.3   |
| Excel Processing  | FastExcel         | 0.18.0   |
| Java Version      | OpenJDK           | 17+      |

## Project Structure

```
src/
├── main/
│   ├── java/com/xuxiaoye/api/
│   │   ├── Application.java              # Main application entry
│   │   ├── adapter/                      # Generated OpenAPI adapters
│   │   │   ├── api/server/               # Server-side API definitions
│   │   │   └── server/mapper/            # Generated MyBatis mappers
│   │   ├── bean/                         # Core beans and DTOs
│   │   │   ├── JWT.java
│   │   │   ├── PagedEntity.java
│   │   │   ├── Pagination.java
│   │   │   └── RequestContext.java
│   │   ├── client/                       # Base clients for API/DB operations
│   │   │   ├── BaseApiClient.java
│   │   │   ├── BaseDbClient.java
│   │   │   └── CRUDDbClient.java
│   │   ├── conf/                         # Configuration classes
│   │   │   ├── AdapterConfig.java
│   │   │   ├── MybatisPlusConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── WebSecurityConfig.java
│   │   ├── constant/                      # Application constants
│   │   ├── interceptors/                 # Request interceptors
│   │   │   ├── JWTInterceptor.java
│   │   │   ├── RequestContextInterceptor.java
│   │   │   └── TableAuditLogInterceptor.java
│   │   ├── resp/                         # Response wrappers
│   │   │   ├── AppResponse.java
│   │   │   ├── AppStatus.java
│   │   │   └── FileResponse.java
│   │   ├── services/                     # Business logic implementations
│   │   │   ├── interfaces/               # Service interfaces
│   │   │   └── db/                       # Database entities and mappers
│   │   └── utils/                        # Utility classes
│   │       ├── DateTimeUtils.java
│   │       ├── ExcelHelper.java
│   │       ├── JwtUtils.java
│   │       └── ...
│   └── resources/
│       ├── application.yaml               # Main configuration
│       ├── application-local.yaml         # Local development config
│       ├── db/db.sql                     # Database schema
│       └── swagger/server/sample.yaml    # OpenAPI specification
└── test/
    ├── java/                             # Unit and integration tests
    └── resources/
        ├── apis/                         # API test fixtures
        ├── contracts/                    # Contract tests
        └── test_certs/                   # Test certificates
```

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

## Configuration

### Environment Variables

| Variable                 | Description           | Default     |
| ------------------------ | --------------------- | ----------- |
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

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:6666/swagger
```

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

## Authorization Model

### Permission Matrix

| Role   | Create | Search | Get | Delete | Export | Import |
| ------ | ------ | ------ | --- | ------ | ------ | ------ |
| Admin  | ✓      | ✓      | ✓   | ✓      | ✓      | ✓      |
| Member | ✓      | ✓      | ✓   | -      | ✓      | ✓      |
| Owner  | ✓      | Own    | Own | Own    | Own    | -      |
| Guest  | -      | -      | -   | -      | -      | -      |

### Permission Format

Permissions follow the pattern `{entity}:{action}` or `{entity}:{action}_own`:

- `student:search` - Search any student's data
- `student:search_own` - Search only own data
- `student:*` - All permissions on students
- `*:*` - Superuser (Admin only)

## Creating New Entities

1. **Define OpenAPI Specification** in `src/main/resources/swagger/server/sample.yaml`

2. **Generate Code:**

   ```bash
   mvn clean compile
   ```

   This triggers the OpenAPI Generator to create DTOs and controller stubs.

3. **Create Database Entity** in `src/main/java/com/xuxiaoye/api/services/db/dto/entity/`

4. **Create DB Mapper** extending `BaseMapper<Entity>`

5. **Create DB Service** extending `ServiceImpl<DBMapper, Entity>`

6. **Create Service Interface** in `src/main/java/com/xuxiaoye/api/services/interfaces/`

7. **Implement Service** extending `CRUDDbClient<...>` and implement the interface

8. **Register in ServiceConfig** as a Spring Bean

## Audit Logging

The `TableAuditLogInterceptor` automatically logs all database changes:

- **A** - Insert/Add
- **U** - Update
- **D** - Delete

Audit tables follow the naming convention `{TABLE}_AUDIT` and store the complete record state before and after changes.

## License

This project is licensed under the MIT License.
