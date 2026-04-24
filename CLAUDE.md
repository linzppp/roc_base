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
├── data/      — MyBatis-Plus config, BaseEntity, LogicDeleteEntity, BaseMapper, BaseService, multi-datasource
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

`MyBatisPlusConfig` registers two interceptors: `PaginationInnerInterceptor` (MySQL, max 500/page) and `BlockAttackInnerInterceptor` (prevents full-table UPDATE/DELETE).

`BaseMetaObjectHandler` implements `MetaObjectHandler` to auto-fill audit fields on insert/update. Insert uses `strictInsertFill` (only fills if null); update uses `setFieldValByName` (force-overwrites). `getCurrentUserId()` is a TODO — wire in auth context when auth is implemented.

## Data 模块架构

### 包结构

```
data/src/main/java/org/roc/practice/
├── config/        # MyBatisPlusConfig（插件配置）
├── base/          # BaseEntity、LogicDeleteEntity、BaseMapper、BaseService、BaseServiceImpl
├── datasource/    # DsConstants、数据源配置
└── page/          # PageConvertUtil
```

### 继承链

- **Entity**   : 继承 `BaseEntity` 或 `LogicDeleteEntity`（按需选择）
- **Mapper**   : 继承 `BaseMapper<T>`，加 `@Mapper` 注解
- **Service**  : 接口继承 `BaseService<T>`，实现类继承 `BaseServiceImpl<M, T>`
- Service 层非必须，无业务逻辑的简单 CRUD 可由 Controller 直接注入 `IService` 实现调用

## 实体设计规范

### BaseEntity 公共字段

| 字段 | 说明 |
|---|---|
| `id` | 雪花 ID（Long），前端接收为 String（JacksonConfig 统一处理） |
| `createTime` | 由 `MetaObjectHandler` 自动填充，禁止手动赋值 |
| `updateTime` | 由 `MetaObjectHandler` 自动填充，禁止手动赋值 |
| `createBy` | Phase 5 引入 Security 后由 `UserContextHolder` 填充，当前为 null |
| `updateBy` | Phase 5 引入 Security 后由 `UserContextHolder` 填充，当前为 null |

### 逻辑删除

- 有数据审计、合规保留要求的实体继承 `LogicDeleteEntity`
- 日志表、流水表、临时表使用 `BaseEntity` + 硬删除
- 逻辑删除不是默认选项，按业务需求选择，不要所有表都继承 `LogicDeleteEntity`
- 大表需同步规划归档策略，避免已删除数据长期堆积影响查询性能
- 手写 XML SQL 操作逻辑删除表时，必须手动添加 `AND deleted = 0`（MP 自动过滤只对 MP 生成的 SQL 生效）

### 禁止使用 `java.util.Date`

统一使用 `LocalDateTime` / `LocalDate` / `LocalTime`。

## Jackson 配置（web 模块 JacksonConfig）

| 配置项 | 行为 |
|---|---|
| `Long` / `long` → `String` 序列化 | 解决 JS 精度丢失（雪花 ID 19 位，超出 JS `Number` 精度） |
| `String` → `Long` 反序列化 | 对称处理 `RequestBody` 中的 Long 字段 |
| `LocalDateTime` 序列化格式 | `yyyy-MM-dd HH:mm:ss` |
| `LocalDateTime` 反序列化格式 | `yyyy-MM-dd HH:mm:ss` |
| `FAIL_ON_UNKNOWN_PROPERTIES` | `false`，防止上下游独立迭代时新增字段导致服务崩溃 |
| `null` 值序列化 | 不在框架层全局配置，由业务 DTO 自己用 `@JsonInclude` 按需声明 |

## MyBatis-Plus 使用规范

### 插件

- `PaginationInnerInterceptor`：分页插件，单次最大 500 条（`maxLimit=500`），溢出返回空
- `BlockAttackInnerInterceptor`：防全表 update/delete

### 主键

- 统一使用雪花 ID（`IdType.ASSIGN_ID`）
- `workerId` 通过配置注入，禁止使用默认随机值（多实例部署时有极小概率冲突）

### 关键行为

- `updateById` 默认忽略 null 字段，需要将字段置 null 时用 `UpdateWrapper` 显式 `set`
- `saveBatch` 需在 JDBC URL 添加 `rewriteBatchedStatements=true` 才触发真正批量插入，否则退化为循环单条 INSERT
- 手写 XML SQL 不享受 MP 的逻辑删除自动过滤，必须手动加 `AND deleted = 0`

### SQL 编写规范

- 单表操作：优先用 `lambdaQuery()` / `lambdaUpdate()` 链式调用
- 联表或复杂 SQL：在 `XxxMapper` 接口声明方法，XML 里写 SQL
- 动态 SQL：使用 XML，禁止使用注解式动态 SQL（`@SelectProvider`），可读性差

## 事务规范

- `@Transactional` 加在 Service 方法上，Mapper 层不加
- 必须显式指定 `rollbackFor = Exception.class`，禁止依赖默认行为
- 外部 HTTP 调用、文件操作、消息发送禁止放在 `@Transactional` 方法内（连接池占用风险）
- 事务方法必须是 `public`，同类内部调用 `@Transactional` 不生效（Spring AOP 代理限制）
- 跨数据源操作禁止使用 `@Transactional`（Seata 接入前），等 Phase 引入 Seata 后处理

## 多数据源规范（dynamic-datasource）

### 数据源名称

- 使用 `DsConstants` 常量（`DsConstants.MASTER` / `DsConstants.SLAVE`），禁止硬编码字符串
- `application.yml` 必须开启 `strict: true`，防止数据源名拼写错误时静默回落到主库

### @DS 注解使用规则

| 场景 | 做法 |
|---|---|
| 写操作 | 不加 `@DS`（走默认 master） |
| 非强一致性读 | `@DS(DsConstants.SLAVE)` |
| 强一致性读（写后立即读，如支付后查余额） | `@DS(DsConstants.MASTER)` |
| 同类内部方法调用 | `@DS` 不生效（Spring AOP 代理限制，与 `@Transactional` 同理） |

### 禁止事项

- 禁止开启 `MasterSlaveAutoRoutingPlugin`（事务内 select 会错误路由到从库）
- 禁止跨数据源方法上加 `@Transactional`（Seata 接入前数据源切换在事务外，不一致且不报错）
- 禁止在 `@Async` 方法或线程池任务里使用 `@DS`（ThreadLocal 不传递，静默回落到默认数据源）

### 主从延迟

写后立即读的强一致性场景，显式加 `@DS(DsConstants.MASTER)`。框架层无法自动识别，由业务开发者按场景判断。

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
