# Exception Handling Code Review Checklist

This checklist helps code reviewers ensure that exception handling follows the established guidelines.

## Pre-Review Setup

1. Reviewer should read `docs/EXCEPTION_HANDLING_GUIDE.md` first
2. Focus on service layer exception handling patterns
3. Pay attention to AppResponse vs Exception boundary

## Review Checklist

### ✅ AppResponse Usage

- [ ] All public service methods return `AppResponse<T>` or `AppResponse`
- [ ] Failure cases use `AppResponse.failWithStatus(...)` not `null` or `throw`
- [ ] Success cases use `AppResponse.okWithData(...)` or `AppResponse.ok()`
- [ ] No `AppResponse` instances are ignored (check for unhandled return values)

### ✅ Business Logic Failures

- [ ] Null checks return `failWithStatus(AppStatus.notFound())`
- [ ] Validation failures return `failWithStatus(AppStatus.badRequest(...))`
- [ ] Authorization failures return `failWithStatus(AppStatus.forbidden(...))`
- [ ] Duplicate entity checks return `failWithStatus(AppStatus.badRequest(...))`
- [ ] All expected business logic failures are documented

### ✅ Exception Handling

- [ ] No unchecked exceptions thrown for business failures
- [ ] `try-catch` blocks only for technical/external failures (DB, network, parsing)
- [ ] Caught exceptions are converted to `AppResponse` failures
- [ ] Exception stack traces are logged (not in response)
- [ ] No sensitive information in error messages

### ✅ Method Chaining (ifOk/ifOkElse)

- [ ] Methods that depend on another service call use `ifOk()` pattern
- [ ] If-else checks for `isOk()` are replaced with `ifOk()`/`ifOkElse()`
- [ ] Nested callbacks are readable (not more than 2-3 levels)
- [ ] Lambda expressions are properly indented

### ✅ Database Operations

- [ ] Database calls are wrapped in `handleDbCall()`
- [ ] No raw database exceptions leak to response
- [ ] Database errors are converted to `AppStatus.internalError()`
- [ ] Transaction boundaries are correct (`@Transactional` placement)

### ✅ Validation

- [ ] `validate()` method is overridden if entity needs validation
- [ ] Validation runs before database operations
- [ ] Validation errors have clear, user-friendly messages
- [ ] All required fields are validated

### ✅ Documentation

- [ ] Complex exception handling logic has comments
- [ ] Decision between `AppResponse` vs `throw` is documented
- [ ] Service class has exception handling guidelines comment (see examples in UserServiceImpl)
- [ ] Method-level comments explain unusual error handling

## Common Anti-Patterns to Watch For

### ❌ Pattern: Silent Failures

```java
// Bad - catches but ignores
try {
    userDBService.updateById(user);
} catch (Exception e) {
    // Do nothing
    return AppResponse.ok();  // ❌ User doesn't know it failed!
}

// Good
try {
    if (!userDBService.updateById(user)) {
        return AppResponse.failWithStatus(AppStatus.internalError("Failed to update user"));
    }
} catch (Exception e) {
    log.error("Database error", e);
    return AppResponse.failWithStatus(AppStatus.internalError());
}
```

### ❌ Pattern: Unchecked Exception Bubbling

```java
// Bad - security exception leaks to client
public AppResponse<User> login(LoginRequest request) {
    User user = userDBService.getUserByAccountName(request.getUsername());
    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        throw new BadCredentialsException("Invalid credentials");  // ❌ Creates 500 error
    }
    return AppResponse.okWithData(user);
}

// Good
public AppResponse<User> login(LoginRequest request) {
    User user = userDBService.getUserByAccountName(request.getUsername());
    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        return AppResponse.failWithStatus(AppStatus.badRequest("Invalid credentials"));
    }
    return AppResponse.okWithData(user);
}
```

### ❌ Pattern: Manual Error Checking Instead of ifOk()

```java
// Bad - verbose error checking
public AppResponse<FileResponse> exportData(...) {
    AppResponse<List<Items>> response = this.fetchItems(...);
    if (!response.isOk()) {
        return AppResponse.failWithStatus(response.getStatus());
    }
    
    List<Items> items = response.getData();
    // ... export logic
    return AppResponse.okWithData(result);
}

// Good - using ifOk()
public AppResponse<FileResponse> exportData(...) {
    return this.fetchItems(...).ifOk(items -> {
        // ... export logic
        return result;
    });
}
```

### ❌ Pattern: External Exception Messages

```java
// Bad - exposes internal exception details
catch (SQLException e) {
    return AppResponse.failWithStatus(
        AppStatus.internalError(e.getMessage())  // ❌ SQL error exposed to client
    );
}

// Good - generic message
catch (SQLException e) {
    log.error("Database query failed", e);
    return AppResponse.failWithStatus(
        AppStatus.internalError("Failed to query database")
    );
}
```

## Review Decision Matrix

| Scenario | Action | Reason |
|----------|--------|--------|
| Throws `IllegalArgumentException` for missing field | ✅ Request changes | Should be `AppResponse.failWithStatus(badRequest)` |
| Catches `IOException` and returns ok() anyway | ✅ Request changes | Should return error status |
| Has try-catch for business logic (non-IO) | ✅ Request changes | Should validate before call |
| Missing `handleDbCall()` wrapper | ✅ Request changes | DB exceptions might leak |
| Uses manual `if (!response.isOk())` checks | ⚠️ Suggest ifOk() | Not wrong, but can be cleaner |
| No comment on why exception conversion | ⚠️ Add comment | Helps future maintainers |

## Questions to Ask

1. **What could go wrong here?** - Is every failure case handled?
2. **Is this unexpected?** - Should this throw or return AppResponse?
3. **Who sees this error?** - Is the message appropriate for the client?
4. **How does this fail?** - What exceptions could be thrown?
5. **Is this recoverable?** - Should we log and continue or fail the operation?

## Approval Criteria

✅ Issue approval if:
- [ ] No new unchecked exceptions for business failures
- [ ] All database exceptions are caught and converted
- [ ] AppResponse failures are returned consistently
- [ ] Code matches patterns in UserServiceImpl, StudentServiceImpl examples
- [ ] Documentation/comments are clear

⚠️ Request changes if:
- [ ] Any of the above are violated
- [ ] Anti-patterns are present
- [ ] Exception handling strategy is inconsistent with guidelines

---

**Last Updated**: 2026-06-12  
**Version**: 1.0

