package org.roc.practice.protect;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

/**
 * Redisson 布隆过滤器薄包装，防止缓存穿透。
 *
 * <p>布隆过滤器可以以极小的内存开销判断一个 key「一定不存在」或「可能存在」：
 * <ul>
 *   <li>返回 {@code false}（一定不存在）→ 直接拒绝，不查缓存也不查 DB</li>
 *   <li>返回 {@code true}（可能存在）→ 继续查询缓存/DB</li>
 * </ul>
 *
 * <p>使用须知：
 * <ul>
 *   <li>布隆过滤器初始化（{@link #tryInit}）时需指定预期元素数量和误判率，不可动态扩容</li>
 *   <li>已存入数据库的记录需在写入时同步调用 {@link #add}</li>
 *   <li>布隆过滤器不支持删除，逻辑删除场景需额外处理（定期重建或使用 Counting Bloom Filter）</li>
 * </ul>
 */
@RequiredArgsConstructor
public class BloomFilterTemplate {

    private final RedissonClient redissonClient;

    /**
     * 初始化布隆过滤器（若已存在则跳过，幂等安全）。
     *
     * @param filterName     布隆过滤器名称（Redis key）
     * @param expectedInsertions 预期存入的元素数量
     * @param falseProbability   可接受的误判率（如 0.01 表示 1%）
     */
    public <T> void tryInit(String filterName, long expectedInsertions, double falseProbability) {
        RBloomFilter<T> filter = redissonClient.getBloomFilter(filterName);
        filter.tryInit(expectedInsertions, falseProbability);
    }

    /**
     * 判断元素是否可能存在于布隆过滤器中。
     *
     * @return {@code false} 表示一定不存在；{@code true} 表示可能存在（存在误判率）
     */
    public <T> boolean mightContain(String filterName, T value) {
        RBloomFilter<T> filter = redissonClient.getBloomFilter(filterName);
        return filter.contains(value);
    }

    /**
     * 向布隆过滤器中添加元素（数据写入 DB 后调用）。
     */
    public <T> void add(String filterName, T value) {
        RBloomFilter<T> filter = redissonClient.getBloomFilter(filterName);
        filter.add(value);
    }

    /**
     * 获取布隆过滤器当前已存入的元素数量。
     */
    public long count(String filterName) {
        return redissonClient.<Object>getBloomFilter(filterName).count();
    }
}
