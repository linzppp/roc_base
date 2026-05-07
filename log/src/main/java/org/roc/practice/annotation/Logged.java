package org.roc.practice.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Logged {
    // 是否记录请求入参
    boolean logArgs() default true;
    // 是否记录返回值
    boolean logResult() default false;
}
