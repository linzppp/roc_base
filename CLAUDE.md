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

## Architecture Overview

This is a Spring Boot base/scaffold project (`org.roc.practice`) that establishes common infrastructure patterns for REST APIs.

### Unified Response Wrapper

All controller responses are automatically wrapped in `Result<T>` via `ResponseAutoWrapper` (a `ResponseBodyAdvice`). Controllers can return raw objects — the wrapper converts them to `Result.success(data)` transparently.

- `Result<T>` — the envelope: `{bizCode, msg, data, traceId, respTs}`
- `ResultCode` enum — maps business codes to HTTP statuses (e.g. `"00000"` → 200, `"C0001"` → 500)
- `ResponseAutoWrapper` — skips wrapping if body is already a `Result`

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) catches:
- `BusinessException` — user-facing business errors with optional custom message; uses `ResultCode`'s HTTP status
- `BaseException` — base for typed exceptions; maps to its `ResultCode`'s HTTP status
- `MethodArgumentNotValidException` — validation failures → `PARAM_ERROR` + field-level messages joined
- `Exception` — fallback, logs and returns `SYSTEM_ERROR`

To throw a business error: `throw new BusinessException(ResultCode.NOT_FOUND, "optional custom msg")`

### Pagination VOs

Two pagination patterns are available in `core/result/`:
- `PageVO<T>` — traditional offset pagination: `{list, total, pageNum, pageSize, pages}`. Create with `PageVO.of(list, total, pageNum, pageSize)`. (Has commented-out MyBatis-Plus `IPage` adapter.)
- `ScrollVO<T>` — cursor-based infinite scroll: `{list, size, nextCursor, hasNext}`. Requires items to implement `CursorTarget` (provides `getCursorPosition(): long`). Create with `ScrollVO.of(list, size)` — pass `size+1` records, it handles trimming and cursor extraction automatically.

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
│   ├── constant/ResultCode.java
│   └── result/                # Result, PageVO, ScrollVO, CursorTarget, ResponseAutoWrapper
└── exception/                 # BaseException, BusinessException, GlobalExceptionHandler
```

New feature packages should be added at the `org.roc.practice` level. `ResponseAutoWrapper` and `GlobalExceptionHandler` are scoped to `basePackages = "org.roc.practice"`.