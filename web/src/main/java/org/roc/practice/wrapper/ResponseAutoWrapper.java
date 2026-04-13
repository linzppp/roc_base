package org.roc.practice.wrapper;

import org.roc.practice.result.Result;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "org.roc.practice")
public class ResponseAutoWrapper implements ResponseBodyAdvice<Object> {
    @Override
    /*
        放弃该执行方案, 不做AutoWrapper
        由业务实现人员, 显式的在Controller当中进行结果返回.
        避免对File, String, 三方包等情况的特殊处理
     */
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType){
        return false;
    }

    @Override
    /*
      对于Spring框架， 其结果返回第一步是执行 Convert 选择，第二步是进行 beforeBodyWrite处理， 第三步是进行 Convert处理。
      针对String， 会采用StringHttpMessageConverter， 它接受的参数只能是String。
      本项目框架规则为 JSON REST API， 采用 extendMessageConverters 进行配置处理
     */
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response){
        if (body instanceof Result) {
            return body;
        }

        if(body == null){
            return Result.success();
        }

        return Result.success(body);
    }
}