package org.roc.practice.protect;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 缓存防护门面，组合多种防护策略。
 *
 * <p>完整防护链（各步骤均可选）：
 * <pre>
 *  请求
 *   │
 *   ├─ [布隆过滤器] 一定不存在 → 直接返回 null（防穿透）
 *   │
 *   ├─ [空值缓存]   命中空值 sentinel → 返回 null（防穿透）
 *   │   命中正常值 → 返回值
 *   │
 *   └─ [互斥锁加载] 竞争锁 → double-check → 加载 DB → 回填缓存（防击穿）
 * </pre>
 *
 * <p>使用示例（完整防护）：
 * <pre>{@code
 * // 初始化时（应用启动或数据导入后）
 * cacheProtector.initBloomFilter("product:bloom", 1_000_000, 0.01);
 * // 查询
 * Product product = cacheProtector.protect(
 *     "product:" + productId,
 *     "product:bloom",
 *     productId,
 *     30, TimeUnit.MINUTES,
 *     () -> productMapper.selectById(productId)
 * );
 * }</pre>
 *
 * <p>若不需要布隆过滤器，使用 {@link #protect(String, long, TimeUnit, Supplier)}。
 */
@RequiredArgsConstructor
public class CacheProtector {

    private final NullValueCacheHelper nullValueCacheHelper;
    private final BloomFilterTemplate bloomFilterTemplate;
    private final CacheLoadMutex cacheLoadMutex;

    /**
     * 初始化布隆过滤器（应用启动时调用，幂等安全）。
     */
    public <T> void initBloomFilter(String filterName, long expectedInsertions, double falseProbability) {
        bloomFilterTemplate.tryInit(filterName, expectedInsertions, falseProbability);
    }

    /**
     * 布隆过滤器 + 互斥锁加载（无空值缓存）。
     *
     * <p>适用场景：数据存在概率高（极少有空查询），热点 key 失效防击穿。
     *
     * @param key            Redis 缓存 key
     * @param filterName     布隆过滤器名称
     * @param bloomCheckValue 用于布隆过滤器判断的值（通常是业务 ID）
     * @param ttl            缓存时长
     * @param timeUnit       时间单位
     * @param loader         数据加载逻辑
     */
    public <T, V> T protect(String key, String filterName, V bloomCheckValue,
                             long ttl, TimeUnit timeUnit, Supplier<T> loader) {
        // 布隆过滤器：一定不存在直接返回
        if (!bloomFilterTemplate.mightContain(filterName, bloomCheckValue)) {
            return null;
        }
        // 互斥锁加载（含 double-check）
        return cacheLoadMutex.get(key, ttl, timeUnit, loader);
    }

    /**
     * 空值缓存 + 互斥锁加载。
     *
     * <p>适用场景：存在大量空查询（如查询不存在的用户），无需布隆过滤器的轻量方案。
     *
     * @param key      Redis 缓存 key
     * @param ttl      正常值缓存时长（空值固定 60s TTL）
     * @param timeUnit 时间单位
     * @param loader   数据加载逻辑，返回 null 表示数据不存在
     */
    public <T> T protect(String key, long ttl, TimeUnit timeUnit, Supplier<T> loader) {
        return nullValueCacheHelper.getOrLoad(key, ttl, timeUnit,
                () -> cacheLoadMutex.get(key, ttl, timeUnit, loader));
    }

    /**
     * 完整防护链：布隆过滤器 + 空值缓存 + 互斥锁加载。
     *
     * <p>适用场景：高并发 + 大量空查询 + 热点 key 失效，三重防护。
     *
     * @param key            Redis 缓存 key
     * @param filterName     布隆过滤器名称
     * @param bloomPredicate 布隆过滤器判断逻辑，返回 false 表示一定不存在
     * @param ttl            正常值缓存时长
     * @param timeUnit       时间单位
     * @param loader         数据加载逻辑
     */
    public <T> T protectFull(String key, String filterName, Predicate<String> bloomPredicate,
                              long ttl, TimeUnit timeUnit, Supplier<T> loader) {
        // 1. 布隆过滤器
        if (!bloomPredicate.test(filterName)) {
            return null;
        }
        // 2. 空值缓存 + 互斥锁
        return protect(key, ttl, timeUnit, loader);
    }

    /**
     * 数据写入 DB 后调用：添加到布隆过滤器 + 删除空值缓存。
     */
    public <T> void onDataWritten(String cacheKey, String filterName, T bloomValue) {
        bloomFilterTemplate.add(filterName, bloomValue);
        nullValueCacheHelper.evict(cacheKey);
    }
}
