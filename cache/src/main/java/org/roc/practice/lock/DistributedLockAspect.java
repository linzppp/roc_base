package org.roc.practice.lock;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roc.practice.support.KeySpELParser;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

@Aspect
@Order(0)
@RequiredArgsConstructor
public class DistributedLockAspect {
    private static final String LOCK_KEY_PREFIX = "lock";
    private final LockTemplate lockTemplate;
    private final KeySpELParser keyParser;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint pjp, DistributedLock distributedLock) throws Throwable{
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String parsedKey = keyParser.parse(distributedLock.key(), sig.getMethod(), pjp.getArgs(), pjp.getTarget());
        String lockKey = buildLockKey(distributedLock.prefix(), parsedKey);

        return lockTemplate.execute(
                lockKey,
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit(),
                pjp::proceed          // CheckedSupplier，异常原样透传
        );
    }

    private String buildLockKey(String prefix, String parsedKey) {
        if (StringUtils.hasText(prefix)) {
            return LOCK_KEY_PREFIX + ":" + prefix + ":" + parsedKey;
        }
        return LOCK_KEY_PREFIX + ":" + parsedKey;
    }

}
