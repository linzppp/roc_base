package org.roc.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 使用innerObjectMapper作为 序列化工具
 * 不覆盖StringRedisTemplate, 此部分已由 Spring Boot注册, 且无需修改
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            @Qualifier("innerObjectMapper")ObjectMapper innerObjectMapper) {
        RedisTemplate<String ,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        GenericJackson2JsonRedisSerializer valueSerializer =
                GenericJackson2JsonRedisSerializer.builder()
                        .objectMapper(innerObjectMapper)
                        .defaultTyping(false)
                        .build();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

}
