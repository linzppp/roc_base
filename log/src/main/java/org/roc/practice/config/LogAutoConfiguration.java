package org.roc.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.roc.practice.aspect.AuditLogAspect;
import org.roc.practice.aspect.ServiceLogAspect;
import org.roc.practice.decorator.MdcTaskDecorator;
import org.roc.practice.filter.AccessLogFilter;
import org.roc.practice.filter.TraceIdFilter;
import org.roc.practice.serializer.SensitiveJacksonModule;
import org.roc.practice.spi.AuditLogHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.LinkedHashSet;
import java.util.List;

@Configuration
@ConditionalOnWebApplication
public class LogAutoConfiguration {
    @Value("${spring.application.name:unknown}")
    private String appName;

    /**
     * 访问日志排除前缀，匹配的请求不记录访问日志。
     * 默认排除：
     *   /actuator   — Spring Boot Actuator（健康检查、Prometheus 抓取等）
     *   /favicon.ico — 浏览器自动请求
     *   /doc.html / /v3/api-docs / /swagger-ui / /webjars — Knife4j / OpenAPI 文档
     */
    @Value("${roc.log.access.exclude-prefixes:/actuator,/favicon.ico,/doc.html,/v3/api-docs,/swagger-ui,/webjars}")
    private List<String> accessLogExcludePrefixes;

    @Bean("logObjectMapper")
    public ObjectMapper logObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SensitiveJacksonModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter(appName));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilterRegistrationBean(){
        FilterRegistrationBean<AccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessLogFilter(new LinkedHashSet<>(accessLogExcludePrefixes)));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE+1);
        return registration;
    }

    @Bean
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    /**
     * 审计日志切面，仅在业务系统提供 {@link AuditLogHandler} Bean 时才注册。
     *
     * <p>业务系统实现 {@code AuditLogHandler} 并注册为 Bean 即可激活审计功能，
     * 无需修改任何框架配置。
     */
    @Bean
    @ConditionalOnBean(AuditLogHandler.class)
    public AuditLogAspect auditLogAspect(AuditLogHandler auditLogHandler,
                                         @Qualifier("logObjectMapper") ObjectMapper logObjectMapper) {
        return new AuditLogAspect(auditLogHandler, logObjectMapper);
    }

    /**
     * 默认实现：所有异常均视为系统异常（error 级别）。
     *
     * <p>业务系统如需区分业务异常（warn）与系统异常（error），在自己的配置类中声明同类型 Bean 覆盖即可：
     * <pre>{@code
     * @Bean
     * public ServiceLogAspect serviceLogAspect(@Qualifier("logObjectMapper") ObjectMapper logObjectMapper) {
     *     return new ServiceLogAspect(logObjectMapper, e -> e instanceof BusinessException);
     * }
     * }</pre>
     */
    @Bean
    @ConditionalOnMissingBean(ServiceLogAspect.class)
    public ServiceLogAspect serviceLogAspect(@Qualifier("logObjectMapper") ObjectMapper logObjectMapper) {
        return new ServiceLogAspect(logObjectMapper, e -> false);
    }
}
