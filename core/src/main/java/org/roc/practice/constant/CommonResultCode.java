package org.roc.practice.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CommonResultCode implements IResultCode{
    // 常规业务成功场景
    SUCCESS("00000","操作成功"),

    // 4xx错误；需要前端对应响应处理场景
    PARAM_ERROR("A0001","传入参数错误"),
    TOKEN_INVALID("A0002","登陆令牌失效"),
    NO_PERMISSION("A0003","暂无操作权限"),
    NOT_FOUND("A0004","资源不存在"),

    // 业务逻辑不满足，需要提示用户
    RESUBMIT_FAILED("B0001", "请勿重复操作"),

    SYSTEM_ERROR("C0001","服务器繁忙,请稍后重试"),

    REMOTE_CALL_FAILED("D0001","调用第三方服务失败");

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
