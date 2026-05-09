package org.roc.practice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解，标记需要记录操作审计的 Service 方法。
 *
 * <p>框架负责采集上下文信息（traceId、userId、入参、耗时、成功/失败），
 * 业务系统通过实现 {@link org.roc.practice.spi.AuditLogHandler} 决定如何存储。
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @AuditLog(module = "订单", action = "创建订单")
 * public void createOrder(OrderRequest request) { ... }
 * }</pre>
 *
 * <h3>激活条件</h3>
 * 业务系统需注册 {@link org.roc.practice.spi.AuditLogHandler} Bean，
 * 切面才会被装配。未提供实现时注解无副作用。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 业务模块名称，如 "订单"、"用户"、"支付" */
    String module() default "";

    /** 操作描述，如 "创建订单"、"修改密码"、"删除商品" */
    String action() default "";

    /** 是否记录方法入参（默认开启；含敏感字段时可关闭或配合 @Sensitive 脱敏） */
    boolean logArgs() default true;
}
