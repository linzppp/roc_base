package org.roc.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/*
    放弃该执行方案, 不做AutoWrapper
    由业务实现人员, 显式的在Controller当中进行结果返回.
    避免对File, String, 三方包等情况的特殊处理
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
//    private final ObjectMapper objectMapper;
//
//    public WebMvcConfig(ObjectMapper objectMapper){
//        this.objectMapper=objectMapper;
//    }
//
//    /*
//    该方案与其他 WbvMvcConfigurer并行, 不能消除其他配置影响.
//     */
////    @Override
////    public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
////        // 1. Jackson converter 放在最前面（最高优先级）
////        MappingJackson2HttpMessageConverter jacksonConverter =
////                new MappingJackson2HttpMessageConverter(objectMapper);
////        converters.add(jacksonConverter);
////
////        // 2. String converter 在 Jackson 之后（降级处理纯文本场景）
////        StringHttpMessageConverter stringConverter =
////                new StringHttpMessageConverter(StandardCharsets.UTF_8);
////        stringConverter.setSupportedMediaTypes(
////                List.of(MediaType.TEXT_PLAIN)  // 缩小其适用范围
////        );
////        converters.add(stringConverter);
////
////        // 3. 字节流兜底
////        converters.add(new ByteArrayHttpMessageConverter());
////    }
//
//    @Override
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters){
//        // 移除默认的 StringHttpMessageConverter（它的 */* 支持会抢走 String 返回的处理）
//        converters.removeIf(c -> c instanceof StringHttpMessageConverter);
//
////        // 把自定义 Jackson 插到第一位（最高优先级）
////        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
//
//        // 如果确实需要纯文本场景，再加一个仅 text/plain 的 String converter
//        StringHttpMessageConverter sc = new StringHttpMessageConverter(StandardCharsets.UTF_8);
//        sc.setSupportedMediaTypes(List.of(MediaType.TEXT_PLAIN));
//        converters.add(sc);
//    }
}
