package com.demo.config;

import com.my.redis.RedisConfig;
import org.springframework.stereotype.Component;

/**
 * extends 公共模块中的配置去加载redisTemplate
 */
@Component
public class MyRedisConfig extends RedisConfig {
}
