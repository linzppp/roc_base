package org.roc.practice.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 滑动窗口的限流模板。
 *
 * <p>算法：使用 ZSET 记录窗口内的请求时间戳，Lua 脚本原子执行：
 * <ol>
 *   <li>删除窗口外的过期成员（ZREMRANGEBYSCORE）</li>
 *   <li>统计当前窗口内的请求数（ZCARD）</li>
 *   <li>超限返回 0；未超限则添加当前请求并设置 key 过期时间，返回 1</li>
 * </ol>
 *
 * <p>优势：滑动窗口无固定边界突刺，精度高；Lua 保证原子性。
 */
@RequiredArgsConstructor
public class RateLimitTemplate {

    /**
     * Lua 滑动窗口脚本。
     * KEYS[1] = limitKey
     * ARGV[1] = windowStart（毫秒时间戳，now - windowMs）
     * ARGV[2] = now（毫秒时间戳，作为 score）
     * ARGV[3] = member（uuid，避免 score 相同时成员冲突）
     * ARGV[4] = limit（最大请求数）
     * ARGV[5] = windowMs（窗口毫秒数，用于 EXPIRE）
     *
     * 返回 1 表示允许，0 表示超限。
     */
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>(
            "redis.call('zremrangebyscore', KEYS[1], '-inf', ARGV[1]) " +
            "local count = redis.call('zcard', KEYS[1]) " +
            "if count < tonumber(ARGV[4]) then " +
            "    redis.call('zadd', KEYS[1], ARGV[2], ARGV[3]) " +
            "    redis.call('pexpire', KEYS[1], ARGV[5]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end",
            Long.class
    );

    private static final String KEY_PREFIX = "ratelimit";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 执行限流检查。
     *
     * @param limitKey 完整的限流 key（已包含业务前缀）
     * @param limit    窗口内最大请求数
     * @param period   窗口大小
     * @param timeUnit 时间单位
     * @return {@code true} 表示允许通过；{@code false} 表示超限
     */
    public boolean isAllowed(String limitKey, int limit, long period, TimeUnit timeUnit) {
        long windowMs = timeUnit.toMillis(period);
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;
        String member = UUID.randomUUID().toString();

        Long result = stringRedisTemplate.execute(
                SLIDING_WINDOW_SCRIPT,
                Collections.singletonList(limitKey),
                String.valueOf(windowStart),
                String.valueOf(now),
                member,
                String.valueOf(limit),
                String.valueOf(windowMs)
        );
        return Long.valueOf(1L).equals(result);
    }

    /**
     * 构建最终的 Redis key。
     */
    public String buildKey(String prefix, String parsedKey) {
        if (prefix != null && !prefix.isBlank()) {
            return KEY_PREFIX + ":" + prefix + ":" + parsedKey;
        }
        return KEY_PREFIX + ":" + parsedKey;
    }
}
