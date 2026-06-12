# Exception Handling Guidelines

This document outlines the decision boundary between throwing exceptions and returning `AppResponse` failures in the service layer.

## Principle

- **Use `AppResponse` with failure status** for expected business logic failures
- **Throw exceptions** for unexpected technical failures that should be caught by the global exception handler

## Decision Matrix

| Scenario | Expected | Handling | Example |
|----------|----------|----------|---------|
| **Business Validation** | Yes | Return `AppResponse.failWithStatus(...)` | User not found, invalid input |
| **Business Rule Violation** | Yes | Return `AppResponse.failWithStatus(...)` | Duplicate entity, insufficient balance |
| **Database Operation Failure** | No | Wrapped in `handleDbCall()` | DB query fails unexpectedly |
| **External Service Failure** | No | Wrapped in `handleDbCall()` | API timeout, network error |
| **Programming Error** | No | Throw exception | NPE, wrong parameter type |

## Implementation Patterns

### Pattern 1: Using `ifOk()` for Conditional Success

```java
// Good: Chain operations together with ifOk()
return this.search(searchRequest, pagination).ifOk(pagedEntity -> {
    // Proceed only if search succeeds
    ExcelHelper.ExcelWriter excelWriter = ExcelHelper.getWriter();
    // ... export logic
    return new FileResponse(...);
});
```

### Pattern 2: Using `ifOkElse()` for Fallback

```java
// Good: Handle both success and failure cases
return getUserResponse().ifOkElse(
    user -> AppResponse.okWithData(user.getFullName()),  // Success case
    AppResponse.failWithStatus(AppStatus.badRequest("User not found"))  // Failure case
);
```

### Pattern 3: Handling DB Exceptions

```java
// Good: Wrapped exception handling
return handleDbCall(() -> {
    try {
        // Business logic
        return AppResponse.okWithData(result);
    } catch (SQLException e) {
        // Convert technical exception to business response
        log.error("Database error", e);
        return AppResponse.failWithStatus(AppStatus.internalError("Failed to query database"));
    }
});
```

### Pattern 4: Manual Exception to AppResponse Conversion

```java
// Good: Use convertToAppStatus() for AppException handling
try {
    // Business logic that may throw AppException
    return someBusinessOperation();
} catch (AppException e) {
    return AppResponse.failWithStatus(convertToAppStatus(e.getResponseStatus()));
}
```

## Service Implementation Examples

### ✅ Good Example: UserServiceImpl.login()

```java
@Override
public AppResponse<com.xuxiaoye.api.bean.JWT> login(LoginRequest request) {
    return handleDbCall(() -> {
        // Expected failure: user not found or wrong password
        if (dbUser == null || !passwordEncoder.matches(request.getPassword(), dbUser.getPasswordHash())) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Invalid access"));
        }

        // Generate tokens
        TokenPair tokenPair = JwtUtils.generateJWTTokenPair(...);
        dbUser.setRefreshToken(tokenPair.refreshToken());

        // Unexpected failure: DB error is caught by handleDbCall()
        if (!userDBService.updateById(dbUser)) {
            return AppResponse.failWithStatus(AppStatus.internalError());
        }

        return AppResponse.okWithData(jwt);
    });
}
```

### ✅ Good Example: CRUDDbClient.exportData()

```java
public AppResponse<FileResponse> exportData(SearchRequest searchRequest, Pagination pagination, String sheetName) {
    // Using ifOk() chain: if search succeeds, proceed with export
    return this.searchInternal(searchRequest, pagination).ifOk(pagedEntity -> {
        ExcelHelper.ExcelWriter excelWriter = ExcelHelper.getWriter();
        // ... export logic
        return fileResponse;
    });
}
```

### ✅ Good Example: Validation in Service

```java
@Override
protected AppResponse<User> validate(User user) {
    // Return AppResponse with failure for business validation
    if (StringUtils.isBlank(user.getAccountName())) {
        return AppResponse.failWithStatus(AppStatus.badRequest("Missing Account Name"));
    }
    return AppResponse.ok();
}
```

## Common Patterns to Avoid

### ❌ Don't: Return null instead of AppResponse

```java
// Bad
public AppResponse<User> getUser(String id) {
    User user = userDBService.getById(id);
    if (user == null) {
        return null;  // ❌ Wrong: should return AppResponse.failWithStatus()
    }
    return AppResponse.okWithData(user);
}
```

### ❌ Don't: Throw exceptions for business failures

```java
// Bad
public AppResponse<User> create(User user) {
    if (StringUtils.isBlank(user.getAccountName())) {
        throw new IllegalArgumentException("Missing Account Name");  // ❌ Wrong: should return failWithStatus()
    }
    return AppResponse.okWithData(save(user));
}
```

### ❌ Don't: Ignore database exceptions

```java
// Bad
public AppResponse<String> deleteById(String id) {
    try {
        userDBService.removeById(id);
        return AppResponse.okWithData("Deleted");
    } catch (Exception e) {
        return AppResponse.okWithData("Deleted anyway");  // ❌ Wrong: should return failure status
    }
}
```

## Adding New Exception Handling

When adding new services or methods:

1. Always return `AppResponse` with appropriate status
2. Use `handleDbCall()` wrapper for database operations
3. Convert business exceptions using `convertToAppStatus()` method
4. Use `ifOk()` / `ifOkElse()` patterns for chaining operations

## AppStatus Reference

Common status builders available:

```java
AppStatus.ok()                           // 200 OK
AppStatus.badRequest(message)            // 400 Bad Request
AppStatus.unauthorized(message)          // 401 Unauthorized
AppStatus.forbidden(message)             // 403 Forbidden
AppStatus.notFound()                     // 404 Not Found
AppStatus.internalError()                // 500 Internal Server Error
AppStatus.internalError(message)         // 500 with custom message
AppStatus.builder()
    .code(customCode)
    .message(customMessage)
    .build()
```

## Utility Methods in CRUDDbClient

All Service implementations inherit these utilities:

```java
// Convert ResponseStatus to AppStatus
protected AppStatus convertToAppStatus(ResponseStatus responseStatus)

// Wrap database calls with exception handling
protected <T> AppResponse<T> handleDbCall(Supplier<AppResponse<T>> operation)

// Validate entities before CRUD operations
protected AppResponse<Entity> validate(Entity entity)
```

## Migration Guide

If updating existing services to follow these guidelines:

1. Identify all `throw` statements - should they be `AppResponse.failWithStatus()` instead?
2. Review all `catch` blocks - ensure exceptions are converted to AppResponse
3. Add `ifOk()` chains where multiple operations depend on each other
4. Test with both success and failure scenarios

---

**Last Updated**: 2026-06-12  
**Version**: 1.0

