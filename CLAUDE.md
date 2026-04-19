# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the project
mvn clean package

# Run the application (from demo module)
mvn spring-boot:run -pl demo

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

## Tech Stack

- Java 17, Spring Boot 3.5.6, Maven multi-module
- Lombok for boilerplate reduction
- MyBatis-Plus for persistence (with pagination and full-table-update protection interceptors)
- Dynamic datasource support (`dynamic-datasource-spring-boot3-starter`)

## Module Structure

```
roc-base (parent POM)
├── bom/       — dependency version management only; no code
├── core/      — pure Java: Result<T>, PageVo, ScrollVo, IResultCode, CommonResultCode
├── web/       — Spring Web layer: GlobalExceptionHandler, ResponseAutoWrapper, validation groups, exceptions
├── data/      — MyBatis-Plus config, BaseEntity, LogicDeleteEntity
└── demo/      — runnable Spring Boot app (DemoBeginner) + integration tests
```

Dependency direction: `demo` → `web` + `bom`; `web` → `core`; `data` → `core` + `bom`. The `core` module has no Spring dependency.

## Architecture

### Unified Response Wrapper

All controller responses are automatically wrapped in `Result<T>` via `ResponseAutoWrapper` (a `ResponseBodyAdvice`). Controllers can return raw objects; the wrapper converts them to `Result.success(data)` transparently.

- `Result<T>` — envelope: `{bizCode, msg, data, traceId}` (`traceId` is a placeholder, not yet populated)
- `IResultCode` — interface: `getCode()` / `getMessage()`; implement to define custom result code enums
- `CommonResultCode` — built-in enum:
  - `SUCCESS("00000")`, `PARAM_ERROR("A0001")`, `TOKEN_INVALID("A0002")`, `NO_PERMISSION("A0003")`, `NOT_FOUND("A0004")`, `NOT_READABLE("A0005")`, `RESUBMIT_FAILED("B0001")`, `SYSTEM_ERROR("C0001")`, `REMOTE_CALL_FAILED("D0001")`
- `ResponseAutoWrapper` — skips wrapping if body is already a `Result`
- `WebMvcConfig` removes `StringHttpMessageConverter` so String return types are wrapped correctly (not intercepted before `ResponseAutoWrapper`)
- `JacksonConfig` registers: `Long`/`long` serialized as String (avoids JS precision loss for snowflake IDs), `LocalDateTime` formatted as `"yyyy-MM-dd HH:mm:ss"`, `FAIL_ON_UNKNOWN_PROPERTIES` disabled (tolerates extra fields from downstream services)

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice(basePackages = "org.roc.practice")`) handles:

| Exception | HTTP Status | Use case |
|---|---|---|
| `BusinessException` | 200 | Expected user-facing business errors; carries `IResultCode` |
| `RocSystemException` | 500 | Logic bugs / data inconsistency needing dev investigation; logs the URI |
| `MethodArgumentNotValidException` / `BindException` | 400 | Bean validation failures; `msg` = first field error, `data` = all field errors as `Map<String, String>` |
| `HttpMessageNotReadableException` | 400 | Malformed JSON / type mismatch in request body → `NOT_READABLE` |
| `Exception` | 500 | Catch-all fallback; logs full stack trace → `SYSTEM_ERROR` |

Usage:
```java
// Business error (user should see message, HTTP 200)
throw new BusinessException(CommonResultCode.NOT_FOUND, "optional custom msg");

// System/logic bug (HTTP 500, triggers alerts)
throw new RocSystemException(CommonResultCode.SYSTEM_ERROR, "detail for logs");

// Return error without throwing
return Result.fail(CommonResultCode.NO_PERMISSION);
```

### Validation Groups

`web/validate/` provides `Create` and `Update` marker interfaces (both extend `jakarta.validation.groups.Default`). Use `@Validated(Create.class)` / `@Validated(Update.class)` on controller parameters to differentiate create vs. update validation constraints on the same request DTO.

### Pagination VOs

Both live in `core/result/`:
- `PageVo<T>` — offset pagination: `{records, total, current, size}`. Create with `PageVo.of(list, total, current, size)`.
- `ScrollVo<T>` — cursor-based scroll: `{records, size, nextCursor, hasNext}`. Items must implement `ICursor` (`getCursorId(): Long`). Create with `ScrollVo.of(list, size)` — pass `size+1` records; it trims and extracts the cursor automatically.

### Data Layer Base Classes

`data/base/` provides two entity base classes for MyBatis-Plus:
- `BaseEntity` — `id` (snowflake via `IdType.ASSIGN_ID`), auto-filled `createTime`, `updateTime`, `createBy`, `updateBy`
- `LogicDeleteEntity extends BaseEntity` — adds `deleted` field annotated with `@TableLogic`

`MyBatisPlusConfig` registers two interceptors: `PaginationInnerInterceptor` (MySQL, max 1000/page) and `BlockAttackInnerInterceptor` (prevents full-table UPDATE/DELETE).

`BaseMetaObjectHandler` implements `MetaObjectHandler` to auto-fill audit fields on insert/update. Insert uses `strictInsertFill` (only fills if null); update uses `setFieldValByName` (force-overwrites). `getCurrentUserId()` is a TODO — wire in auth context when auth is implemented.

## BOM Module

`bom/pom.xml` manages versions. Key entries:

| Category | Artifact | Version |
|---|---|---|
| Persistence | mybatis-plus-spring-boot3-starter | 3.5.9 |
| Persistence | dynamic-datasource-spring-boot3-starter | 4.3.1 |
| Object Mapping | mapstruct / mapstruct-processor | 1.6.3 |
| API Docs | knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 |
| Excel | easyexcel | 4.0.3 |
| Auth | sa-token-spring-boot3-starter | 1.40.0 |
| Redis/Distributed | redisson-spring-boot-starter | 3.45.0 |
| JWT | jjwt-api / jjwt-impl / jjwt-jackson | 0.12.6 |
| Utilities | hutool-all / guava | 5.8.35 / 33.4.0-jre |

To use a managed dependency in a module, declare it without a `<version>` tag — versions are resolved via the `bom` parent import.