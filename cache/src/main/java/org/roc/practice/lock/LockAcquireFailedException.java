package org.roc.practice.lock;

import lombok.Getter;
import org.roc.practice.exception.BusinessException;
import org.roc.practice.exception.CacheResultCode;

/**
 * 分布式锁获取失败异常。
 *
 * <p>在 {@code @DistributedLock#waitTime} 内未获取到锁时抛出。
 * 默认对应业务码 {@link CacheResultCode#LOCK_ACQUIRE_FAILED}（B0100）。
 *
 * <p>抛出 cause：若获取锁过程中被中断（InterruptedException）或 Redisson 通信失败，
 * 原始异常通过 cause 透传，便于排查；用户侧 message 保持友好文案。
 */
@Getter
public class LockAcquireFailedException extends BusinessException {
    private final String lockKey;

    public LockAcquireFailedException(String lockKey){
        super(CacheResultCode.LOCK_ACQUIRE_FAILED);
        this.lockKey = lockKey;
    }

    public LockAcquireFailedException(String lockKey, Throwable cause) {
        super(CacheResultCode.LOCK_ACQUIRE_FAILED, CacheResultCode.LOCK_ACQUIRE_FAILED.getMessage(), cause);
        this.lockKey = lockKey;
    }
}
