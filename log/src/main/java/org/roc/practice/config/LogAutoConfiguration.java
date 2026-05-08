package org.roc.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.roc.practice.aspect.ServiceLogAspect;
import org.roc.practice.decorator.MdcTaskDecorator;
import org.roc.practice.filter.AccessLogFilter;
import org.roc.practice.filter.TraceIdFilter;
import org.roc.practice.serializer.SensitiveJacksonModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnWebApplication
public class LogAutoConfiguration {
    @Value("${spring.application.name:unknown}")
    private String appName;

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
    public FilterRegistrationBean<AccessLogFilter> accessLogFilterFilterRegistrationBean(){
        FilterRegistrationBean<AccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessLogFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE+1);
        return registration;
    }

    @Bean
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    @Bean
    public ServiceLogAspect serviceLogAspect(@Qualifier("logObjectMapper") ObjectMapper logObjectMapper) {
        return new ServiceLogAspect(logObjectMapper, e -> false);
    }
}
