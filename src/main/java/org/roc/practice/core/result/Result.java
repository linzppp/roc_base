package org.roc.practice.core.result;

import lombok.Data;
import org.roc.practice.core.constant.ResultCode;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private String bizCode;
    private String msg;
    private T data;
    // 占位，暂时不生效
    private String traceId;
    private Long respTs;

    private Result(){}

    private static <T> Result<T> build(String bizCode, String msg, T data){
        Result<T> result = new Result<>();
        result.setBizCode(bizCode);
        result.setMsg(msg);
        result.setData(data);
        result.setTraceId(null);
        result.setRespTs(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> success(T data){
        return build(ResultCode.SUCCESS.getBizCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(){
        return build(ResultCode.SUCCESS.getBizCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> error(ResultCode bizCodeEnum){
        return build(bizCodeEnum.getBizCode(), bizCodeEnum.getMsg(), null);
    }

    public static <T> Result<T> error(ResultCode bizCodeEnum, String customMsg) {
        return build(bizCodeEnum.getBizCode(), customMsg, null);
    }
}
