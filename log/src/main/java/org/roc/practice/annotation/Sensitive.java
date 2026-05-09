package org.roc.practice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记敏感字段，在日志输出时自动脱敏。
 *
 * <h3>生效范围</h3>
 * <b>仅在 {@code @Logged} 标注方法的 AOP 日志中生效。</b>
 * {@link org.roc.practice.aspect.ServiceLogAspect} 使用 {@code logObjectMapper}（注册了
 * {@link org.roc.practice.serializer.SensitiveJacksonModule}）序列化入参/出参，此时脱敏有效。
 *
 * <h3>不生效的场景</h3>
 * <ul>
 *   <li>API 响应序列化 —— 走 web 模块 {@code JacksonConfig} 的主 ObjectMapper，
 *       未注册 SensitiveJacksonModule，字段原样输出。</li>
 *   <li>直接调用 {@code Logger.info(obj.toString())} 等手动日志。</li>
 * </ul>
 *
 * <h3>API 响应脱敏</h3>
 * 如需在接口响应中隐藏敏感字段，请在业务 DTO 上使用 {@code @JsonIgnore}、
 * {@code @JsonProperty(access = WRITE_ONLY)} 或自定义序列化器，框架层不做全局拦截。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    SensitiveType type() default SensitiveType.PASSWORD;
}
