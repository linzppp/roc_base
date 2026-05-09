package org.roc.practice.spi;

/**
 * 审计日志处理 SPI。
 *
 * <p>框架提供 AOP 切面采集操作元信息（模块、动作、用户、耗时、成败等），
 * 业务系统实现此接口决定如何持久化。
 *
 * <h3>接入步骤</h3>
 * <ol>
 *   <li>实现本接口并注册为 Spring Bean（加 {@code @Component} 即可）</li>
 *   <li>在需要审计的 Service 方法上加 {@code @AuditLog(module="订单", action="创建订单")}</li>
 *   <li>框架切面自动采集上下文并调用 {@link #handle}</li>
 * </ol>
 *
 * <h3>业务系统实现示例</h3>
 * <pre>{@code
 * @Component
 * @RequiredArgsConstructor
 * public class DbAuditLogHandler implements AuditLogHandler {
 *     private final AuditLogRepository repo;
 *
 *     @Override
 *     public void handle(AuditLogContext ctx) {
 *         AuditLogEntity entity = AuditLogEntity.builder()
 *             .module(ctx.getModule())
 *             .action(ctx.getAction())
 *             .userId(ctx.getUserId())
 *             .traceId(ctx.getTraceId())
 *             .argsJson(ctx.getArgsJson())
 *             .elapsedMs(ctx.getElapsedMs())
 *             .success(ctx.isSuccess())
 *             .errorMsg(ctx.getErrorMsg())
 *             .build();
 *         repo.save(entity);
 *     }
 * }
 * }</pre>
 *
 * <h3>注意</h3>
 * {@code handle} 在业务方法的调用线程内执行。如有性能顾虑（如写 DB），
 * 建议在实现内部异步处理（发 MQ 或使用 {@code @Async}）。
 */
public interface AuditLogHandler {

    /**
     * 处理审计日志。
     *
     * @param ctx 当前操作的审计上下文，由框架填充
     */
    void handle(AuditLogContext ctx);
}
