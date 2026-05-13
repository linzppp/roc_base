package org.roc.practice.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {
    @Bean("innerObjectMapper")
    @ConditionalOnMissingBean(name = "innerObjectMapper")
    public ObjectMapper innerObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 时间处理
        JavaTimeModule timeModule = new JavaTimeModule();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
        mapper.registerModule(timeModule);
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 容错策略
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        // 序列化策略
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 性能
        mapper.registerModule(new BlackbirdModule());

        return mapper;
    }
}
