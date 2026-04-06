package org.roc.practice.exception;

import lombok.Getter;
import org.roc.practice.core.constant.ResultCode;

@Getter
public class BusinessException extends RuntimeException {
    private final ResultCode resultCode;
    private final String customMsg;

    public BusinessException(ResultCode resultCode, String customMsg) {
        super();
        this.resultCode = resultCode;
        this.customMsg = customMsg;
    }

}
