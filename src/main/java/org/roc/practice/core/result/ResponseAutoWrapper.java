package org.roc.practice.core.result;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "org.roc.practice")
public class ResponseAutoWrapper implements ResponseBodyAdvice<Object> {
    @Override
    /*
        由于限制了packages范围，
     */
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType){
        return true;
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
