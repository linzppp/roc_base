package org.roc.practice.protect;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存空值助手，防止缓存穿透。
 *
 * <p>当数据库查询结果为 null 时，向 Redis 写入一个空值 sentinel（{@link NullValue#INSTANCE}），
 * 并设置较短的 TTL（避免长期占用），阻止后续请求击穿到数据库。
 *
 * <p>典型场景：用户查询不存在的商品 ID，每次都打到 DB。
 * 解决方案：第一次 miss 后缓存空值，后续命中空值直接返回 null。
 *
 * <p>注意：空值 TTL 应远短于正常缓存 TTL（建议 60s），避免已写入数据库的数据
 * 因空值缓存未过期而继续不可见。若业务有写入后立即可见需求，写入时需主动删除对应缓存 key。
 */
@RequiredArgsConstructor
public class NullValueCacheHelper {

    /** 空值缓存的默认 TTL（秒）。 */
    private static final long DEFAULT_NULL_TTL_SECONDS = 60L;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询缓存；命中则返回，miss 则调用 {@code loader} 加载数据并回填缓存（含空值缓存）。
     *
     * @param key      Redis key
     * @param ttl      正常值的缓存时长
     * @param timeUnit 时间单位
     * @param loader   DB 或下游查询逻辑，返回 null 表示数据不存在
     * @return 缓存或数据库中的值，若数据不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, long ttl, TimeUnit timeUnit, Supplier<T> loader) {
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            // 命中空值 sentinel，透明地返回 null
            if (cached instanceof NullValue) {
                return null;
            }
            return (T) cached;
        }

        // 缓存 miss，加载数据
        T value = loader.get();
        if (value == null) {
            // 缓存空值，使用较短 TTL 防止穿透
            redisTemplate.opsForValue().set(key, NullValue.INSTANCE, DEFAULT_NULL_TTL_SECONDS, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
        }
        return value;
    }

    /**
     * 主动删除缓存 key（写入数据库后调用，使空值缓存立即失效）。
     */
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 空值 sentinel，用于区分「缓存了 null」和「缓存 miss」。
     * 需要可序列化，以便 Jackson 正确处理。
     */
    public static final class NullValue {
        public static final NullValue INSTANCE = new NullValue();
        private NullValue() {}
    }
}
