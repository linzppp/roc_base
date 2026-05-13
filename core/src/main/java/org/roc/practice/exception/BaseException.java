package org.roc.practice.exception;

import lombok.Getter;
import org.roc.practice.constant.IResultCode;


/**
 * 框架统一异常基类，所有携带 {@link IResultCode} 的异常应继承此类。
 *
 * <p>由 web 模块的 {@code GlobalExceptionHandler} 统一兜底处理，
 * 输出 {@code Result<T>} 结构，HTTP 状态码恒为 200，业务状态由 bizCode 表达。
 *
 * <p>位置说明：本类零 Spring / Web 依赖，下沉到 core 模块以供 cache、security 等
 * 底层模块复用，避免反向依赖 web 模块。
 */
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

    public BaseException(IResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }
}
