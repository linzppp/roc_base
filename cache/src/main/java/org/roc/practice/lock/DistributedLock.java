package org.roc.practice.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    // SpEL表达式, 支持#root.method, #paramName等
    String key();

    // 业务键值命名空间前缀, 隔离不同模块的key, 避免碰撞
    // 最终Redis key格式为 : lock:{prefix}:{parsedKey}
    // 本字段为空时: lock:{parsedKey}
    String prefix() default "";

    /**
     * 尝试获取锁的最大等待时间。默认 0 = 立即失败（fail-fast）。
     * 适合防重复提交场景：拿不到锁说明已有并发操作在处理，直接拒绝。
     * 适合短暂竞争场景（如库存扣减）：可设为 1~3s。
     * 业务开发者应根据场景显式覆盖，不要盲目依赖默认值。
     */

    long waitTime() default 0;

    /**
     * 锁持有时长。默认 -1 = 启用 Redisson watchdog（30s，每 10s 自动续期）。
     * watchdog 适合执行时间不可预估的业务。
     * 若业务执行时间可控且对锁持有敏感，可显式指定固定值（单位由 timeUnit 决定）。
     */

    long leaseTime() default -1;

    TimeUnit timeUnit() default  TimeUnit.SECONDS;
}
