package org.roc.practice.idempotent;

import java.lang.annotation.*;

/**
 * 幂等注解，防止接口重复提交。
 *
 * <p>使用方式：
 * <ol>
 *   <li>调用方先通过 {@link IdempotentService#generateToken} 获取一次性 token。</li>
 *   <li>业务请求将 token 放入 {@link #tokenHeader} 指定的 HTTP Header 中。</li>
 *   <li>切面原子消费 token：已消费或不存在时抛出 {@link DuplicateRequestException}。</li>
 * </ol>
 *
 * <p>示例：
 * <pre>{@code
 * @PostMapping("/order/submit")
 * @Idempotent
 * public Result<Void> submit(@RequestBody OrderRequest req) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 存放幂等 token 的 HTTP Header 名称，默认 {@code X-Idempotent-Token}。
     */
    String tokenHeader() default "X-Idempotent-Token";

    /**
     * 自定义拒绝提示，为空时使用 {@link org.roc.practice.exception.CacheResultCode#DUPLICATE_REQUEST} 的默认 message。
     */
    String message() default "";
}
