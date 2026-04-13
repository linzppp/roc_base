package org.roc.practice.exception;

import org.roc.practice.constant.IResultCode;

public class BusinessException extends BaseException {

    public BusinessException(IResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(IResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
