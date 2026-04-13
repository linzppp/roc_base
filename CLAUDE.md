# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

## Tech Stack

- Java 17, Spring Boot 3.3.x, Maven
- Lombok for boilerplate reduction
- Joda-Money for monetary value types
- Spring Boot Actuator

## BOM Module

`bom/pom.xml` manages dependency versions for downstream modules. Key managed dependencies:

| Category | Artifact | Version |
|---|---|---|
| Persistence | mybatis-plus-spring-boot3-starter | 3.5.9 |
| Object Mapping | mapstruct / mapstruct-processor | 1.6.3 |
| API Docs | knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 |
| Excel | easyexcel | 4.0.3 |
| Auth | sa-token-spring-boot3-starter | 1.40.0 |
| Redis/Distributed | redisson-spring-boot-starter | 3.45.0 |
| JWT | jjwt-api / jjwt-impl / jjwt-jackson | 0.12.6 |
| Utilities | hutool-all / guava | 5.8.35 / 33.4.0-jre |

## Architecture Overview

This is a Spring Boot base/scaffold project (`org.roc.practice`) that establishes common infrastructure patterns for REST APIs.

### Unified Response Wrapper

All controller responses are automatically wrapped in `Result<T>` via `ResponseAutoWrapper` (a `ResponseBodyAdvice`). Controllers can return raw objects — the wrapper converts them to `Result.success(data)` transparently.

- `Result<T>` — the envelope: `{bizCode, msg, data, traceId}` (`traceId` is a placeholder, not yet populated)
- `IResultCode` — interface with `getCode()` / `getMessage()`; implement to define custom result code enums
- `CommonResultCode` — built-in enum implementing `IResultCode`: `SUCCESS("00000")`, `PARAM_ERROR("A0001")`, `TOKEN_INVALID("A0002")`, `NO_PERMISSION("A0003")`, `NOT_FOUND("A0004")`, `RESUBMIT_FAILED("B0001")`, `SYSTEM_ERROR("C0001")`, `REMOTE_CALL_FAILED("D0001")`
- `ResponseAutoWrapper` — skips wrapping if body is already a `Result`

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) catches:
- `BusinessException` — user-facing business errors with optional custom message; uses `IResultCode`'s code
- `BaseException` — base for typed exceptions; maps to its `IResultCode`'s code
- `MethodArgumentNotValidException` — validation failures → `PARAM_ERROR` + field-level messages joined
- `Exception` — fallback, logs and returns `SYSTEM_ERROR`

To throw a business error: `throw new BusinessException(CommonResultCode.NOT_FOUND, "optional custom msg")`

### Pagination VOs

Two pagination patterns are available in `core/result/`:
- `PageVo<T>` — traditional offset pagination: `{records, total, current, size}`. Create with `PageVo.of(list, total, current, size)`.
- `ScrollVo<T>` — cursor-based infinite scroll: `{records, size, nextCursor, hasNext}`. Requires items to implement `ICursor` (provides `getCursorId(): Long`). Create with `ScrollVo.of(list, size)` — pass `size+1` records, it handles trimming and cursor extraction automatically.

### Jackson / HTTP Message Converters

- `WebMvcConfig` removes the default `StringHttpMessageConverter` to prevent it from intercepting JSON String responses, so `ResponseAutoWrapper` can wrap them correctly. This is critical for the auto-wrapping of `String` return types.
- `JacksonConfig` registers custom serializers/deserializers for `Money` (Joda-Money): serializes as a plain decimal number.

### Package Structure

```
org.roc.practice
├── Beginner.java              # Spring Boot entry point
├── config/                    # WebMvcConfig, JacksonConfig
├── convert/                   # MoneySerializer, MoneyDeserializer
├── core/
│   ├── constant/              # IResultCode (interface), CommonResultCode (enum)
│   ├── page/                  # ICursor (interface for cursor-based pagination)
│   └── result/                # Result, PageVo, ScrollVo, ResponseAutoWrapper
└── exception/                 # BaseException, BusinessException, GlobalExceptionHandler
```

New feature packages should be added at the `org.roc.practice` level. `ResponseAutoWrapper` and `GlobalExceptionHandler` are scoped to `basePackages = "org.roc.practice"`.