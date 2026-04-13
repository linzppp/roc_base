package org.roc.practice.result;

import lombok.Data;
import org.roc.practice.constant.CommonResultCode;
import org.roc.practice.constant.IResultCode;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private String bizCode;
    private String msg;
    private T data;
    // 占位，暂时不生效
    private String traceId;

    private Result(){}

    /**
     * 内部方法，负责Result的构建；
     * 此处负责进行TraceId的统一构建、 RespTs的统一写入
     * 上层抽象success、error方法，已支持两种典型场景。
     * @param bizCode 业务码
     * @param msg 业务信息
     * @param data 业务结果，可以是空
     */
    private static <T> Result<T> build(String bizCode, String msg, T data){
        Result<T> result = new Result<>();
        result.setBizCode(bizCode);
        result.setMsg(msg);
        if(data != null){
            result.setData(data);
        }
        // TODO 实现MCD全链路日志
        result.setTraceId(null);
        return result;
    }

    public static <T> Result<T> success(T data){
        return build(CommonResultCode.SUCCESS.getCode(), CommonResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(){
        return build(CommonResultCode.SUCCESS.getCode(), CommonResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> error(IResultCode errorCode){
        return build(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
