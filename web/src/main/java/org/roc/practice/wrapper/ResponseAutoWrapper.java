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

/**
 * 响应体自动包装器（当前已停用，{@link #supports} 固定返回 false）。
 *
 * <h3>设计决策：放弃自动包装，改为 Controller 显式返回 {@code Result<T>}</h3>
 *
 * <p><b>放弃原因：</b>自动包装需要处理大量边界情况，维护成本高：
 * <ul>
 *   <li>文件下载（{@code Resource} / {@code byte[]} / {@code StreamingResponseBody}）不应被包装</li>
 *   <li>{@code String} 返回值会优先被 {@code StringHttpMessageConverter} 拦截，
 *       {@code beforeBodyWrite} 返回的 {@code Result} 对象无法直接写出，
 *       需要在 {@code WebMvcConfig} 中移除 {@code StringHttpMessageConverter}（参见注释代码）</li>
 *   <li>Knife4j / Swagger UI / Spring Actuator 等三方组件的响应体会被错误包装，
 *       需要逐一排除，白名单难以维护</li>
 *   <li>{@code ResponseEntity} 返回值携带自定义 HTTP Status，包装后 Status 信息丢失</li>
 * </ul>
 *
 * <p><b>当前方案：</b>Controller 方法显式返回 {@code Result<T>}，语义清晰，无隐式行为。
 *
 * <p><b>如需重新启用：</b>将 {@link #supports} 改为返回 {@code true}，
 * 并在 {@code WebMvcConfig#extendMessageConverters} 中移除 {@code StringHttpMessageConverter}，
 * 同时为 Actuator、文件下载等路径添加跳过逻辑。
 */
@RestControllerAdvice(basePackages = "org.roc.practice")
public class ResponseAutoWrapper implements ResponseBodyAdvice<Object> {
    @Override
    // 当前方案：停用自动包装。详见类注释。
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType){
        return false;
    }

    @Override
    /*
      Spring 响应处理流程：① 选择 Converter → ② beforeBodyWrite → ③ Converter 写出。
      String 返回值会被 StringHttpMessageConverter（支持 */*）优先匹配，
      beforeBodyWrite 若返回 Result 对象，StringHttpMessageConverter 无法写出会抛 ClassCastException。
      解决方案：在 WebMvcConfig#extendMessageConverters 中移除 StringHttpMessageConverter，
      或将其 supportedMediaTypes 限制为 text/plain（参见 WebMvcConfig 注释代码）。
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