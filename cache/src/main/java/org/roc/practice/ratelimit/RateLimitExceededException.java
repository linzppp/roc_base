package org.roc.practice.ratelimit;

import lombok.Getter;
import org.roc.practice.exception.BusinessException;
import org.roc.practice.exception.CacheResultCode;

/**
 * 限流超限异常。
 *
 * <p>在滑动窗口内请求次数超过 {@link RateLimit#limit()} 时抛出，
 * 对应业务码 {@link CacheResultCode#RATE_LIMIT_EXCEEDED}（B0103）。
 */
@Getter
public class RateLimitExceededException extends BusinessException {

    private final String limitKey;

    public RateLimitExceededException(String limitKey) {
        super(CacheResultCode.RATE_LIMIT_EXCEEDED);
        this.limitKey = limitKey;
    }

    public RateLimitExceededException(String limitKey, String message) {
        super(CacheResultCode.RATE_LIMIT_EXCEEDED, message);
        this.limitKey = limitKey;
    }
}
