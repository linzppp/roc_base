package org.roc.practice.exception;

import org.roc.practice.constant.IResultCode;

/**
 * 业务异常，用户操作不符合业务定义；
 * 如：订单重复支付、取消订单的付款、归档数据的变更
 * HTTP 200
 */
public class BusinessException extends BaseException {

    public BusinessException(IResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(IResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
