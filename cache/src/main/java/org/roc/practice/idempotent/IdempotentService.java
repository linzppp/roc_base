package org.roc.practice.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 幂等 token 生命周期管理。
 *
 * <p>Redis key 格式：{@code idempotent:{token}}，其中 token 由调用方持有并随请求传回。
 * 若生成时指定了 prefix，token 格式为 {@code {prefix}:{uuid}}，方便按业务归类。
 *
 * <p>{@link #consumeToken} 使用 Lua 脚本原子地执行 check-and-delete，
 * 确保并发场景下只有第一个请求能消费成功。
 */
@RequiredArgsConstructor
public class IdempotentService {

    private static final String KEY_PREFIX = "idempotent";

    /**
     * Lua 脚本：存在则删除并返回 1，不存在返回 0。
     * 原子语义：同一 token 并发消费时只有一个请求返回 1。
     */
    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('exists', KEYS[1]) == 1 then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成幂等 token 并存入 Redis。
     *
     * <p>token 格式：若 prefix 非空则为 {@code {prefix}:{uuid}}，否则为 {@code {uuid}}。
     * Redis key 固定为 {@code idempotent:{token}}。
     *
     * @param prefix   业务命名空间前缀（可为空），用于区分不同接口的 token
     * @param ttl      token 有效时长
     * @param timeUnit 时间单位
     * @return 生成的 token，调用方需通过 Header 原样传回
     */
    public String generateToken(String prefix, long ttl, TimeUnit timeUnit) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String token = StringUtils.hasText(prefix) ? prefix + ":" + uuid : uuid;
        stringRedisTemplate.opsForValue().set(buildKey(token), "1", ttl, timeUnit);
        return token;
    }

    /**
     * 原子消费 token。
     *
     * @param token 从请求 Header 中取出的 token（与 {@link #generateToken} 返回值一致）
     * @return {@code true} 表示消费成功（首次）；{@code false} 表示 token 不存在或已被消费
     */
    public boolean consumeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Long result = stringRedisTemplate.execute(CONSUME_SCRIPT, Collections.singletonList(buildKey(token)));
        return Long.valueOf(1L).equals(result);
    }

    private String buildKey(String token) {
        return KEY_PREFIX + ":" + token;
    }
}
