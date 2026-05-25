package org.roc.practice.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 幂等切面。
 *
 * <p>拦截标注 {@link Idempotent} 的方法，从 HTTP Header 中提取 token，
 * 委托 {@link IdempotentService} 原子消费；消费失败时抛出 {@link DuplicateRequestException}。
 *
 * <p>{@code @Order(1)} 确保在分布式锁切面（Order=0）之后执行，
 * 避免重复请求占用锁资源。
 */
@Aspect
@Order(1)
@RequiredArgsConstructor
public class IdempotentAspect {

    private final IdempotentService idempotentService;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        String token = resolveToken(idempotent.tokenHeader());

        if (!StringUtils.hasText(token)) {
            throw new DuplicateRequestException("", buildMessage(idempotent));
        }

        boolean consumed = idempotentService.consumeToken(token);
        if (!consumed) {
            throw new DuplicateRequestException(token, buildMessage(idempotent));
        }

        return pjp.proceed();
    }

    private String resolveToken(String headerName) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader(headerName);
    }

    private String buildMessage(Idempotent idempotent) {
        return StringUtils.hasText(idempotent.message()) ? idempotent.message() : null;
    }
}
