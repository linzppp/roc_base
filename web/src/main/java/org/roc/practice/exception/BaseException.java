package org.roc.practice.exception;

import lombok.Getter;
import org.roc.practice.constant.IResultCode;

@Getter
public class BaseException extends RuntimeException {

    private final IResultCode resultCode;

    public BaseException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BaseException(IResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
