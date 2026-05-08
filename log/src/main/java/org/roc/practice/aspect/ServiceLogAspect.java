package org.roc.practice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roc.practice.annotation.Logged;

@Aspect
@Slf4j
public class ServiceLogAspect {
    private final ObjectMapper objectMapper;

    public ServiceLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(logged) || @within(logged)")
    public Object around(ProceedingJoinPoint pjp, Logged logged) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String methodName = String.format("%s.$s", sig.getDeclaringTypeName(), sig.getName());
        Long start = System.currentTimeMillis();

        String argsJson = "";
        if (logged.logArgs()) {
            // TODO
            // 无法处理Byte[]流, 需要规范此场景的日志
            argsJson = objectMapper.writeValueAsString(pjp.getArgs());
        }

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            Long elapsed = System.currentTimeMillis() - start;
            log.warn("[{}] exception={} elapsed={}ms args={}",
                    methodName,
                    e.getClass().getSimpleName(),
                    elapsed,
                    argsJson);

            throw e;
        }

        String responseJson = "";
        if (logged.logResult()) {
            responseJson = objectMapper.writeValueAsString(result);
        }

        Long elapsed = System.currentTimeMillis() - start;
        log.debug("[{}] elapsed={}ms args={} resp={}", methodName, elapsed, argsJson, responseJson);
        return result;
    }
}
