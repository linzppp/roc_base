package org.roc.practice.exception;

import org.roc.practice.constant.IResultCode;

/**
 * 系统故障，用户正常行为但导致了不符合系统预期的事件。
 * 逻辑BUG、已付款订单但无订单 等不符合PRD行为
 * HTTP 500； 需日志、联系客服介入处理
 */
public class RocSystemException extends BaseException {
    public RocSystemException(IResultCode resultCode) {
        super(resultCode);
    }

    public RocSystemException(IResultCode resultCode, String message) {super(resultCode, message);}
}
