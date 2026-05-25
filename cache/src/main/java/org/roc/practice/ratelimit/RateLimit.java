package org.roc.practice.ratelimit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解，基于 Redis 滑动窗口实现。
 *
 * <p>Redis key 格式：{@code ratelimit:{prefix}:{parsedKey}} 或 {@code ratelimit:{parsedKey}}。
 *
 * <p>示例：
 * <pre>{@code
 * // 同一用户每分钟最多 10 次下单
 * @RateLimit(key = "#userId", prefix = "order", limit = 10, period = 1, timeUnit = TimeUnit.MINUTES)
 * public Result<Void> placeOrder(Long userId, ...) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流维度 key，支持 SpEL 表达式（#paramName、#p0、#method、#target 等）。
     * 表达式结果会被转为字符串，拼入 Redis key。
     */
    String key();

    /**
     * 业务命名空间前缀，用于隔离不同接口的限流 key。为空时不加前缀。
     */
    String prefix() default "";

    /**
     * 滑动窗口内允许的最大请求数。
     */
    int limit();

    /**
     * 滑动窗口大小，与 {@link #timeUnit} 配合使用。
     */
    long period();

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
