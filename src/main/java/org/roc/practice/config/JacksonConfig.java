package org.roc.practice.config;

import org.joda.money.Money;
import org.roc.practice.convert.MoneyDeserializer;
import org.roc.practice.convert.MoneySerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectMapper() {
        return builder -> {
            builder.serializerByType(Money.class, new MoneySerializer());
            builder.deserializerByType(Money.class, new MoneyDeserializer());
        };
    }
}
