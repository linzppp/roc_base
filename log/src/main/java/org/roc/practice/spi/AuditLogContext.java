package org.roc.practice.spi;

import lombok.Builder;
import lombok.Data;

/**
 * 审计日志上下文，由框架切面填充，传递给 {@link AuditLogHandler} 实现。
 */
@Data
@Builder
public class AuditLogContext {

    /** 业务模块，来自 {@code @AuditLog#module} */
    private String module;

    /** 操作描述，来自 {@code @AuditLog#action} */
    private String action;

    /** 当前请求的 traceId（来自 MDC），可用于关联访问日志和服务日志 */
    private String traceId;

    /** 当前操作用户 ID（来自 MDC userId，需认证过滤器填充；未登录场景为 null） */
    private Long userId;

    /** 方法入参 JSON（{@code @AuditLog#logArgs=false} 时为空字符串） */
    private String argsJson;

    /** 方法执行耗时（毫秒） */
    private long elapsedMs;

    /** 方法是否正常返回（false 表示抛出了异常） */
    private boolean success;

    /** 异常信息（success=true 时为 null） */
    private String errorMsg;
}
