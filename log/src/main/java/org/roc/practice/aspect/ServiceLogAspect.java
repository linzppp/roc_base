package org.roc.practice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roc.practice.annotation.Logged;

import java.util.function.Predicate;

/**
 * AOP级日志, 通过自定义注释Logged, 面向Service及其他非Controller提供日志服务
 * 记录请求耗时, 可选的记录入参, 出参
 * 将依据 SensitiveFieldSerializer 进行脱敏
 * 根据是否为 BusinessException异常, 进行WARN 或 ERROR级别日志.
 * 不进行INFO级别日志, 因为只存在自定义注解, 可以更好的进行运维监控报警.
 *
 */
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

    @Around("@annotation(org.roc.practice.annotation.Logged) || @within(org.roc.practice.annotation.Logged)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        // 方法级注解优先，回退到类级注解，避免两个分支同时匹配时的绑定歧义
        Logged logged = sig.getMethod().getAnnotation(Logged.class);
        if (logged == null) {
            logged = (Logged) sig.getDeclaringType().getAnnotation(Logged.class);
        }
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
        log.info("[{}] elapsed={}ms args={} resp={}", methodName, elapsed, argsJson, responseJson);
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
