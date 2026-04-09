package org.roc.practice.exception;

import lombok.Getter;
import org.roc.practice.core.constant.ResultCode;

@Getter
public class BusinessException extends BaseException {
    private final String customMsg;

    public BusinessException(ResultCode resultCode, String customMsg) {
        super(resultCode, customMsg);
        this.customMsg = customMsg;
    }
}
