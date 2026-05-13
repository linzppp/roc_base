package org.roc.practice.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson-spring-boot-starter
 * 已完成RedissonClient 注册
 * 此处保留作为拓展点: 如自定义Codec, 监听器, 连接监听器等
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
public class RedissonConfig {
}
