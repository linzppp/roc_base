package org.roc.practice.protect;

import lombok.RequiredArgsConstructor;
import org.roc.practice.lock.LockTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存加载互斥锁，防止缓存击穿（热点 key 失效时的并发重建风暴）。
 *
 * <p>算法（double-check locking）：
 * <ol>
 *   <li>查询缓存，命中直接返回</li>
 *   <li>未命中：获取分布式锁（fail-fast，等待时间 0）</li>
 *   <li>获取锁成功后再次查询缓存（防止其他线程已完成重建）</li>
 *   <li>仍未命中则调用 {@code loader} 加载数据并回填缓存</li>
 *   <li>获取锁失败：短暂等待后重试（最多 {@link #MAX_RETRY} 次），超时返回 null</li>
 * </ol>
 *
 * <p>lock key 格式：{@code mutex:{cacheKey}}，与业务缓存 key 隔离。
 *
 * <p>适用场景：热点 key（如秒杀商品详情）过期时，防止所有请求同时打到 DB。
 * 若 key 不是热点（均匀分散），缓存穿透场景更适合使用 {@link NullValueCacheHelper}。
 */
@RequiredArgsConstructor
public class CacheLoadMutex {

    private static final String MUTEX_KEY_PREFIX = "mutex";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_INTERVAL_MS = 50L;

    /** 互斥锁等待时间：0 = 立即失败，让其他线程自旋等待 */
    private static final long LOCK_WAIT_TIME = 0L;
    /** 互斥锁最长持有时间：给 DB 加载留出足够时间，防止锁续期无限占用 */
    private static final long LOCK_LEASE_TIME = 10L;

    private final RedisTemplate<String, Object> redisTemplate;
    private final LockTemplate lockTemplate;

    /**
     * 查询缓存；热点 key 失效时通过互斥锁确保只有一个线程重建缓存。
     *
     * @param key      Redis 缓存 key
     * @param ttl      缓存时长
     * @param timeUnit 时间单位
     * @param loader   数据加载逻辑（查 DB 或调用下游），返回 null 表示数据不存在
     * @return 数据，若多次重试后仍未就绪则返回 null（降级处理）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, long ttl, TimeUnit timeUnit, Supplier<T> loader) {
        // 1. 快速路径：缓存命中直接返回
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (T) cached;
        }

        String mutexKey = MUTEX_KEY_PREFIX + ":" + key;

        // 2. 竞争锁，最多重试 MAX_RETRY 次
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                return lockTemplate.execute(mutexKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS, () -> {
                    // 3. double-check：获取锁后再次确认缓存是否已被其他线程填充
                    Object rechecked = redisTemplate.opsForValue().get(key);
                    if (rechecked != null) {
                        return (T) rechecked;
                    }
                    // 4. 确实未命中，加载数据并回填
                    T value = loader.get();
                    if (value != null) {
                        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
                    }
                    return value;
                });
            } catch (org.roc.practice.lock.LockAcquireFailedException e) {
                // 获取锁失败，说明有其他线程正在重建，等待后重试
                if (i < MAX_RETRY - 1) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                    // 重试前先尝试读缓存（可能已被其他线程填充）
                    Object retryCache = redisTemplate.opsForValue().get(key);
                    if (retryCache != null) {
                        return (T) retryCache;
                    }
                }
            }
        }
        // 超过最大重试次数，降级返回 null
        return null;
    }
}
