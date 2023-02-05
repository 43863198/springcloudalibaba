package com.my.distribute.lock.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedissonConfiguration {
    /**
     * 单机模式 redisson 客户端
     */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    RedissonClient redissonClient(RedisProperties redisProperties) {
        return createRedissonClient(redisProperties);
    }

    private RedissonClient createRedissonClient(RedisProperties properties) {
        Config config = new Config();
        if (properties.getSentinel() != null && properties.getSentinel().getNodes() != null &&
                StringUtils.isNotBlank(properties.getSentinel().getMaster())) {
            log.info("sentinel redisProperties:{}", properties.getSentinel());
            log.info("SENTINEL_SERVERS_NODES:{}",properties.getSentinel().getNodes());
            config.useSentinelServers().setMasterName(properties.getSentinel().getMaster())
                    .addSentinelAddress(properties.getSentinel().getNodes().toArray(new String[0])).setPassword(properties.getPassword());
        } else if(properties.getCluster() != null && properties.getCluster().getNodes() != null) {
            config.useClusterServers().addNodeAddress(properties.getCluster().getNodes().toArray(new String[0])).setPassword(properties.getPassword());
        }else {
            config.useSingleServer().setAddress(properties.getHost() + ":" + properties.getPort()).setPassword(properties.getPassword());
        }
        return Redisson.create(config);
    }
}
