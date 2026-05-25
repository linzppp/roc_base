package org.roc.practice.config;


import org.redisson.api.RedissonClient;
import org.roc.practice.idempotent.IdempotentAspect;
import org.roc.practice.idempotent.IdempotentService;
import org.roc.practice.lock.DistributedLockAspect;
import org.roc.practice.lock.LockTemplate;
import org.roc.practice.protect.BloomFilterTemplate;
import org.roc.practice.protect.CacheLoadMutex;
import org.roc.practice.protect.CacheProtector;
import org.roc.practice.protect.NullValueCacheHelper;
import org.roc.practice.ratelimit.RateLimitAspect;
import org.roc.practice.ratelimit.RateLimitTemplate;
import org.roc.practice.support.KeySpELParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    @Bean
    @ConditionalOnMissingBean
    public KeySpELParser keySpELParser() {
        return new KeySpELParser();
    }

    // Phase 4
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public LockTemplate lockTemplate(RedissonClient redissonClient) {
        return new LockTemplate(redissonClient);
    }

    @Bean
    @ConditionalOnBean(LockTemplate.class)
    public DistributedLockAspect distributedLockAspect(LockTemplate lockTemplate, KeySpELParser keySpELParser) {
        return new DistributedLockAspect(lockTemplate, keySpELParser);
    }

    // Phase 5
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public IdempotentService idempotentService(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentService(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnBean(IdempotentService.class)
    public IdempotentAspect idempotentAspect(IdempotentService idempotentService) {
        return new IdempotentAspect(idempotentService);
    }

    // Phase 6
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public RateLimitTemplate rateLimitTemplate(StringRedisTemplate stringRedisTemplate) {
        return new RateLimitTemplate(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnBean(RateLimitTemplate.class)
    public RateLimitAspect rateLimitAspect(RateLimitTemplate rateLimitTemplate, KeySpELParser keySpELParser) {
        return new RateLimitAspect(rateLimitTemplate, keySpELParser);
    }

    // Phase 7
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "redisTemplate")
    public NullValueCacheHelper nullValueCacheHelper(RedisTemplate<String, Object> redisTemplate) {
        return new NullValueCacheHelper(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public BloomFilterTemplate bloomFilterTemplate(RedissonClient redissonClient) {
        return new BloomFilterTemplate(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({NullValueCacheHelper.class, LockTemplate.class})
    public CacheLoadMutex cacheLoadMutex(RedisTemplate<String, Object> redisTemplate, LockTemplate lockTemplate) {
        return new CacheLoadMutex(redisTemplate, lockTemplate);
    }

    @Bean
    @ConditionalOnBean({NullValueCacheHelper.class, BloomFilterTemplate.class, CacheLoadMutex.class})
    public CacheProtector cacheProtector(NullValueCacheHelper nullValueCacheHelper,
                                         BloomFilterTemplate bloomFilterTemplate,
                                         CacheLoadMutex cacheLoadMutex) {
        return new CacheProtector(nullValueCacheHelper, bloomFilterTemplate, cacheLoadMutex);
    }
}
