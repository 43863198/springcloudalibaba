package com.my.redis;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import redis.clients.jedis.JedisPoolConfig;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.apache.commons.codec.binary.Base64;


@ConditionalOnProperty(name={"spring.data.redis.repositories.enabled"})
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
//@EnableRedisRepositories(basePackages={"${entity.jpa.package:com.cre.dmp.osp}"})
@EnableCaching
public class RedisConfig {
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(RedisProperties properties, @Value("${password.encode.enabled:false}") boolean encode)
    {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        if (properties.getJedis().getPool() != null)
        {
            config.setMaxIdle(properties.getJedis().getPool().getMaxIdle());
            config.setMaxWaitMillis(properties.getJedis().getPool()
                    .getMaxWait().toMillis());
            config.setMinIdle(properties.getJedis().getPool().getMinIdle());
            config.setMaxTotal(properties.getJedis().getPool().getMaxActive());
        }
        if (encode) {
            properties.setPassword(new String(Base64.decodeBase64(properties.getPassword()), StandardCharsets.UTF_8));
        }
        if ((properties.getSentinel() != null) &&
                (StringUtils.isNotBlank(properties.getSentinel().getMaster())))
        {
            RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
            configuration.master(properties.getSentinel().getMaster());
            for (String node : properties.getSentinel().getNodes()) {
                configuration.sentinel(StringUtils.split(node, ":")[0],
                        Integer.valueOf(StringUtils.split(node, ":")[1]));
            }
            configuration.setPassword(RedisPassword.of(properties.getPassword()));
            return new JedisConnectionFactory(configuration, config);
        }
        if ((properties.getCluster() != null) && (properties.getCluster().getNodes() != null))
        {
            RedisClusterConfiguration configuration = new RedisClusterConfiguration(properties.getCluster().getNodes());
            configuration.setPassword(RedisPassword.of(properties.getPassword()));
            return new JedisConnectionFactory(configuration, config);
        }
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        configuration.setPassword(RedisPassword.of(properties.getPassword()));
        return new JedisConnectionFactory(configuration, new JedisConnectionFactory(config).getClientConfiguration());
    }

    @Bean
    @Primary
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory, @Value("${spring.redis.key.prefix:}") String prefix)
    {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(factory);
        template.setValueSerializer(new ValueSerializer(Object.class));
        template.setKeySerializer(new PrefixSerializer(Object.class, prefix));
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory factory, @Value("${spring.redis.key.prefix:}") String prefix, @Value("${spring.redis.key.expire:3000}") int expire)
    {
        return new ExpireCacheManager(RedisCacheWriter.lockingRedisCacheWriter(factory),
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(expire))
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(new PrefixSerializer(Object.class, prefix)))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(new ValueSerializer(Object.class))));
    }
}
