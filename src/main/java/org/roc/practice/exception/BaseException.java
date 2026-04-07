package org.roc.practice.exception;

import org.roc.practice.core.constant.ResultCode;

public class BaseException extends RuntimeException {
    private final ResultCode resultCode;

    public BaseException(ResultCode resultCode, String msg){
        super(msg);
        this.resultCode= resultCode;
    }
}
