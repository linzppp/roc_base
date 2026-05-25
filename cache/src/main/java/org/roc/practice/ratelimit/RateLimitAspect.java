package org.roc.practice.ratelimit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roc.practice.support.KeySpELParser;
import org.springframework.core.annotation.Order;

/**
 * 限流切面。
 *
 * <p>拦截标注 {@link RateLimit} 的方法，通过 SpEL 解析 key，
 * 委托 {@link RateLimitTemplate} 进行滑动窗口限流判断。
 * 超限时抛出 {@link RateLimitExceededException}。
 *
 * <p>{@code @Order(2)} 确保在幂等切面（Order=1）之后执行。
 */
@Aspect
@Order(2)
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitTemplate rateLimitTemplate;
    private final KeySpELParser keyParser;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String parsedKey = keyParser.parse(rateLimit.key(), sig.getMethod(), pjp.getArgs(), pjp.getTarget());
        String limitKey = rateLimitTemplate.buildKey(rateLimit.prefix(), parsedKey);

        boolean allowed = rateLimitTemplate.isAllowed(limitKey, rateLimit.limit(), rateLimit.period(), rateLimit.timeUnit());
        if (!allowed) {
            throw new RateLimitExceededException(limitKey);
        }

        return pjp.proceed();
    }
}
