You are a senior Java / Spring Boot backend architect.

Please perform a **comprehensive code quality review** of the provided Spring Boot project (or code snippets).

Taking this into consideration: This project is using openapi generator with swagger v2 and RestAssured for unit test

Focus on production-readiness, maintainability, scalability, and industry best practices.

---

### Review Scope

1. **Architecture & Design**
   - Layered architecture compliance (Controller / Service / Repository)
   - Separation of concerns
   - Proper use of DTOs vs Entities
   - Domain-driven design awareness

2. **Spring Boot Best Practices**
   - Correct usage of annotations (`@RestController`, `@Service`, `@Repository`, `@Component`)
   - Application configuration (`application.yml` / `application.properties`)
   - Profiles (`dev`, `prod`) usage
   - Dependency injection (constructor-based only)

3. **REST API Design**
   - RESTful URL structure
   - HTTP methods correctness
   - Status codes usage
   - Request / Response DTO validation

4. **Exception Handling**
   - Global exception handling (`@ControllerAdvice`)
   - Consistent error response structure
   - Avoid leaking stack traces

5. **Validation & Input Sanitization**
   - Use of `javax.validation` / `jakarta.validation`
   - DTO-level validation
   - Defensive programming

6. **Data Access Layer**
   - JPA / Hibernate best practices
   - N+1 query issues
   - Transaction management (`@Transactional`)
   - Database schema design

7. **Security**
   - Authentication & Authorization
   - JWT / OAuth2 usage (if applicable)
   - Sensitive data exposure
   - CORS configuration

8. **Testing**
   - Unit tests (JUnit 5)
   - Integration tests (`@SpringBootTest`)
   - Mocking strategy (Mockito)
   - Test coverage quality

9. **Performance & Scalability**
   - Thread-safety
   - Connection pooling
   - Caching strategy (Redis / Caffeine)
   - Logging & monitoring readiness

10. **Code Style & Maintainability**
   - Clean Code principles
   - SOLID adherence
   - Magic numbers / hardcoded values
   - Meaningful naming conventions

---

### Output Format

Merge output into this file IMPROVE.md, make sure no duplicated sections or content. Invalid item need to be cleaned up.

# Project structure

list down folder structure, design digram

# Technology stack summary

show in table with column category, technology, version, purpose, status, recommendation

# Design/Architecture comparison with industrial standards

summarize design or architectural solution comparison with industrial
show in table with column aspect, industry best practice, this project, status, gap, recommendation

# Code & QA Unit Test summary

show project metrics in table with below columns: lines of code, number of classes, number of methods, number of unit tests, test coverage, api endpoints, database tables ,services, test classes etc.

# Evaluation Summary

a table with column category, score, assessment, total score at the end.

# Pros and Cons

list pros and cons of each category mentioned in above table, list down what done well and what need to improve

#Recommendations
list improvement item ordered by priority from high to low

# Actions

For each issue found, provide:

- ✅ **Category**
- ❌ **Problem**
- 💡 **Suggested Fix**
- 🔥 **Severity** (Low / Medium / High / Critical)

Also include:

- ✅ Overall Code Quality Score (1–10)
- ✅ Strengths
- ✅ Weaknesses
- ✅ Top 5 Actionable Improvements

---

If specific files or packages are provided, prioritize reviewing:

- Controllers
- Services
- Entities
- Configuration classes
- Security-related code

You are a senior Java / Spring Boot backend architect.

Please perform a **comprehensive code quality review** of the provided Spring Boot project (or code snippets).

Focus on production-readiness, maintainability, scalability, and industry best practices.

---

### Review Scope

1. **Architecture & Design**
   - Layered architecture compliance (Controller / Service / Repository)
   - Separation of concerns
   - Proper use of DTOs vs Entities
   - Domain-driven design awareness

2. **Spring Boot Best Practices**
   - Correct usage of annotations (`@RestController`, `@Service`, `@Repository`, `@Component`)
   - Application configuration (`application.yml` / `application.properties`)
   - Profiles (`dev`, `prod`) usage
   - Dependency injection (constructor-based only)

3. **REST API Design**
   - RESTful URL structure
   - HTTP methods correctness
   - Status codes usage
   - Request / Response DTO validation

4. **Exception Handling**
   - Global exception handling (`@ControllerAdvice`)
   - Consistent error response structure
   - Avoid leaking stack traces

5. **Validation & Input Sanitization**
   - Use of `javax.validation` / `jakarta.validation`
   - DTO-level validation
   - Defensive programming

6. **Data Access Layer**
   - JPA / Hibernate best practices
   - N+1 query issues
   - Transaction management (`@Transactional`)
   - Database schema design

7. **Security**
   - Authentication & Authorization
   - JWT / OAuth2 usage (if applicable)
   - Sensitive data exposure
   - CORS configuration

8. **Testing**
   - Unit tests (JUnit 5)
   - Integration tests (`@SpringBootTest`)
   - Mocking strategy (Mockito)
   - Test coverage quality

9. **Performance & Scalability**
   - Thread-safety
   - Connection pooling
   - Caching strategy (Redis / Caffeine)
   - Logging & monitoring readiness

10. **Code Style & Maintainability**
   - Clean Code principles
   - SOLID adherence
   - Magic numbers / hardcoded values
   - Meaningful naming conventions

---

### Output Format

For each issue found, provide:

- ✅ **Category**
- ❌ **Problem**
- 💡 **Suggested Fix**
- 🔥 **Severity** (Low / Medium / High / Critical)

Also include:

- ✅ Overall Code Quality Score (1–10)
- ✅ Strengths
- ✅ Weaknesses
- ✅ Top 5 Actionable Improvements

---

If specific files or packages are provided, prioritize reviewing:

- Controllers
- Services
- Entities
- Configuration classes
- Security-related code
