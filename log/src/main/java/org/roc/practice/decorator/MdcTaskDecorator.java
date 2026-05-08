package org.roc.practice.decorator;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 常见的TaskDecorator包含:
 * MDC传递
 * RequestAttributes 请求传递
 * SecurityContext 登录信息传递 userId + token
 * LocaleContextHodler 国际化 时区传递
 *
 * 通过Composite, 利用自动注入机制, 汇聚所有decorators集合
 * 通过Decorated内置的 decorate方法, 链式包装, 实现所有decorators的统一汇聚.
 *
 * 常见搭配 AsyncConfigurer实现, 由Config配置 taskDecorator.
 */
public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable){
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () ->{
            try{
                if(contextMap!=null){
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
