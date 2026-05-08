package org.roc.practice.decorator;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 将当前线程的 MDC 上下文（traceId、spanId、appName 等）传播到异步线程。
 *
 * <p>由 Log 模块统一注册为 Bean，业务系统在配置线程池时装配：
 * <pre>{@code
 * @Configuration
 * @EnableAsync
 * public class AsyncConfig implements AsyncConfigurer {
 *
 *     @Autowired
 *     private MdcTaskDecorator mdcTaskDecorator;
 *
 *     @Override
 *     public Executor getAsyncExecutor() {
 *         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *         executor.setCorePoolSize(8);
 *         executor.setMaxPoolSize(32);
 *         executor.setQueueCapacity(500);
 *         executor.setThreadNamePrefix("async-");
 *         executor.setTaskDecorator(mdcTaskDecorator);
 *         executor.initialize();
 *         return executor;
 *     }
 * }
 * }</pre>
 *
 * <p>如需同时传播其他上下文（RequestAttributes、SecurityContext、LocaleContext 等），
 * 可各自实现 {@link org.springframework.core.task.TaskDecorator} 后在 executor 上链式组合。
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
