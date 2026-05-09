package org.roc.practice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roc.practice.annotation.AuditLog;
import org.roc.practice.constants.TraceConstants;
import org.roc.practice.spi.AuditLogContext;
import org.roc.practice.spi.AuditLogHandler;
import org.slf4j.MDC;

/**
 * 审计日志 AOP 切面。
 *
 * <p>拦截 {@code @AuditLog} 标注的方法，采集上下文后委托给 {@link AuditLogHandler} 实现。
 * 本切面仅在 Spring 上下文中存在 {@code AuditLogHandler} Bean 时才被注册
 * （通过 {@code LogAutoConfiguration} 的 {@code @ConditionalOnBean} 控制）。
 *
 * <p>切面本身不做任何存储，不影响业务方法的返回值和异常传播。
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogHandler auditLogHandler;
    private final ObjectMapper logObjectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();

        String argsJson = "";
        if (auditLog.logArgs()) {
            argsJson = safeSerialize(pjp.getArgs());
        }

        Object result;
        boolean success = true;
        String errorMsg = null;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            submitAuditLog(auditLog, argsJson, System.currentTimeMillis() - start, false, errorMsg);
            throw e;
        }

        submitAuditLog(auditLog, argsJson, System.currentTimeMillis() - start, true, null);
        return result;
    }

    private void submitAuditLog(AuditLog auditLog, String argsJson, long elapsedMs,
                                boolean success, String errorMsg) {
        try {
            Long userId = parseUserId(MDC.get(TraceConstants.USER_ID));
            AuditLogContext ctx = AuditLogContext.builder()
                    .module(auditLog.module())
                    .action(auditLog.action())
                    .traceId(MDC.get(TraceConstants.TRACE_ID))
                    .userId(userId)
                    .argsJson(argsJson)
                    .elapsedMs(elapsedMs)
                    .success(success)
                    .errorMsg(errorMsg)
                    .build();
            auditLogHandler.handle(ctx);
        } catch (Exception e) {
            // 审计日志失败不影响业务流程，只记录框架级 warn
            log.warn("[AuditLog] handler threw exception, audit record may be lost", e);
        }
    }

    private Long parseUserId(String userIdStr) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String safeSerialize(Object obj) {
        try {
            return logObjectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
