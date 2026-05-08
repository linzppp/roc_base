package org.roc.practice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roc.practice.annotation.Logged;

import java.util.function.Predicate;

@Aspect
@Slf4j
public class ServiceLogAspect {
    private static final int MAX_LOG_LENGTH = 500;

    private final ObjectMapper objectMapper;
    /**
     * 判断异常是否为预期业务异常（warn），否则视为系统异常（error）。
     * 默认实现：全部视为系统异常。业务系统可替换此 Bean 传入自定义判断逻辑，
     * 例如：{@code e -> e instanceof BusinessException}
     */
    private final Predicate<Throwable> isBusinessException;

    public ServiceLogAspect(ObjectMapper objectMapper, Predicate<Throwable> isBusinessException) {
        this.objectMapper = objectMapper;
        this.isBusinessException = isBusinessException;
    }

    @Around("@annotation(logged) || @within(logged)")
    public Object around(ProceedingJoinPoint pjp, Logged logged) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String methodName = String.format("%s.%s", sig.getDeclaringTypeName(), sig.getName());
        long start = System.currentTimeMillis();

        String argsJson = "";
        if (logged.logArgs()) {
            // TODO: 无法处理 Byte[] 流，需要规范此场景的日志
            argsJson = safeSerialize(pjp.getArgs());
        }

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            if (isBusinessException.test(e)) {
                log.warn("[{}] exception={} elapsed={}ms args={}",
                        methodName, e.getClass().getSimpleName(), elapsed, argsJson);
            } else {
                log.error("[{}] exception={} elapsed={}ms args={}",
                        methodName, e.getClass().getSimpleName(), elapsed, argsJson, e);
            }
            throw e;
        }

        String responseJson = "";
        if (logged.logResult()) {
            responseJson = safeSerialize(result);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.debug("[{}] elapsed={}ms args={} resp={}", methodName, elapsed, argsJson, responseJson);
        return result;
    }

    private String safeSerialize(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            if (json.length() > MAX_LOG_LENGTH) {
                return json.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
