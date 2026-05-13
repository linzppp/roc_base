package org.roc.practice.idempotent;

import lombok.Getter;
import org.roc.practice.exception.BusinessException;
import org.roc.practice.exception.CacheResultCode;

@Getter
public class DuplicateRequestException extends BusinessException {
    private final String idempotentKey;

    public DuplicateRequestException(String idempotentKey) {
        super(CacheResultCode.DUPLICATE_REQUEST);
        this.idempotentKey = idempotentKey;
    }

    public DuplicateRequestException(String idempotentKey, String message) {
        super(CacheResultCode.DUPLICATE_REQUEST, message);
        this.idempotentKey = idempotentKey;
    }
}
