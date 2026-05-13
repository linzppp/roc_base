package org.roc.practice.exception;

import lombok.AllArgsConstructor;
import org.roc.practice.constant.IResultCode;

@AllArgsConstructor
public enum CacheResultCode implements IResultCode {
    LOCK_ACQUIRE_FAILED("B0101","操作繁忙, 请稍后再试"),
    DUPLICATE_REQUEST("B0102","请勿重复操作"),
    RATE_LIMIT_EXCEEDED("B0103","请求过于频繁, 请稍后再试");

    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
