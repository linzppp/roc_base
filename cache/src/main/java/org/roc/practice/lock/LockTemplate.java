package org.roc.practice.lock;

import com.fasterxml.jackson.module.blackbird.util.CheckedSupplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LockTemplate {
    private final RedissonClient redissonClient;

    public <T> T execute(String lockKey, long waitTime, long leaseTime, TimeUnit unit, CheckedSupplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                throw new LockAcquireFailedException(lockKey);
            }
            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();          // 恢复中断标志
            throw new LockAcquireFailedException(lockKey, e);
        } catch (Throwable e) {
            Thread.currentThread().interrupt();          // 恢复中断标志
            throw new RuntimeException(e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /** void 版本重载，委托给有返回值版本 */
    public void execute(String lockKey, long waitTime, long leaseTime,
                        TimeUnit unit, CheckedRunnable task) {
        execute(lockKey, waitTime, leaseTime, unit, () -> {
            task.run();
            return null;
        });
    }


    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Throwable;
    }
}
