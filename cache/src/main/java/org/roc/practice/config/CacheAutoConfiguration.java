package org.roc.practice.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Cache 模块自动配置入口。
 *
 * <p>第 1 块（骨架阶段）仅占位，不注册任何 Bean。
 * 后续分块逐步加入：
 * <ol>
 *   <li>第 2 块：RedisTemplate / RedissonClient 配置（带 {@code @ConditionalOnClass}）</li>
 *   <li>第 3 块：KeySpELParser</li>
 *   <li>第 4 块：DistributedLockAspect / LockTemplate</li>
 *   <li>第 5 块：IdempotentAspect / IdempotentTokenService</li>
 *   <li>第 6 块：RateLimitAspect / RateLimitTemplate</li>
 *   <li>第 7 块：CacheLoadMutex / BloomFilterTemplate</li>
 * </ol>
 *
 * <p>组合 Bean 的注册条件参见 {@code Phase 4} 设计文档：
 * <ul>
 *   <li>Redis/Redisson 客户端 Bean：{@code @ConditionalOnClass} + {@code @ConditionalOnMissingBean}</li>
 *   <li>切面/Template Bean：{@code @ConditionalOnBean(RedissonClient.class)} 等</li>
 * </ul>
 */
@Configuration
@Import({
        RedisConfig.class,
        RedissonConfig.class
})
public class CacheAutoConfiguration {

}
