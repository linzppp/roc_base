package org.roc.practice.exception;

import lombok.Getter;
import org.roc.practice.core.constant.ResultCode;

@Getter
public class BaseException extends RuntimeException {
    private final ResultCode resultCode;

    public BaseException(ResultCode resultCode, String msg){
        super(msg);
        this.resultCode= resultCode;
    }
}
