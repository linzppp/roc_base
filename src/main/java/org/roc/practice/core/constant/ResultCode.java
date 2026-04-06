package org.roc.practice.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResultCode {
    // 常规业务成功场景
    SUCCESS("00000","操作成功", HttpStatus.OK),

    // 4xx错误；需要前端对应响应处理场景
    PARAM_ERROR("A0001","传入参数错误", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID("A0002","登陆令牌失效", HttpStatus.UNAUTHORIZED),
    NO_PERMISSION("A0003","暂无操作权限", HttpStatus.FORBIDDEN),
    NOT_FOUND("A0004","资源不存在", HttpStatus.NOT_FOUND),

    // 业务逻辑不满足，需要提示用户
    USER_NOT_EXIST("B0001","用户信息不存在", HttpStatus.OK),
    LOGIN_FAILED("B0002","登陆失败", HttpStatus.OK),
    RESUBMIT_FAILED("B0003", "请勿重复操作", HttpStatus.OK),
    PROCESSING("B0004", "业务正在处理中, 请稍后重试", HttpStatus.OK),

    SYSTEM_ERROR("C0001","服务器繁忙,请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR),

    REMOTE_CALL_FAILED("D0001","调用第三方服务失败", HttpStatus.BAD_GATEWAY);

    private final String bizCode;
    private final String msg;
    private final HttpStatus httpStatus;
}
