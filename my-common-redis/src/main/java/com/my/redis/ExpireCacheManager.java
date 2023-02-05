package com.my.redis;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

/**
 * 支持注解上添加过期时间，通过重新RedisCacheManager
 */
public class ExpireCacheManager extends RedisCacheManager {
    public static final String EXPIRE = "-EXPIRE-";
    private RedisCacheWriter writer;
    private RedisCacheConfiguration configuration;

    public ExpireCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration)
    {
        super(cacheWriter, defaultCacheConfiguration);
        this.writer = cacheWriter;
        this.configuration = defaultCacheConfiguration;
    }

    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig)
    {
        return new ExpireCache(name, this.writer, cacheConfig != null ? cacheConfig : this.configuration);
    }

    private class ExpireCache extends RedisCache {
        private String name;
        private RedisCacheWriter writer;
        private RedisCacheConfiguration configuration;

        public ExpireCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
            super(name, cacheWriter, cacheConfig);
            this.name = name;
            this.writer = cacheWriter;
            this.configuration = cacheConfig;
        }

        public void put(Object key, Object value)
        {
            Object cacheValue = preProcessCacheValue(value);
            if ((!isAllowNullValues()) && (cacheValue == null)) {
                throw new IllegalArgumentException(String.format("Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.", new Object[] { this.name }));
            }
            this.writer.put(this.name, createAndConvertCacheKey(key), serializeCacheValue(cacheValue), StringUtils.contains(this.name, "-EXPIRE-") ? Duration.ofSeconds(Long.valueOf(this.name.split("-EXPIRE-")[1]).longValue()) : this.configuration.getTtl());
        }

        private byte[] createAndConvertCacheKey(Object key)
        {
            return serializeCacheKey(createCacheKey(key));
        }

        public ValueWrapper putIfAbsent(Object key, Object value)
        {
            Object cacheValue = preProcessCacheValue(value);
            if ((!isAllowNullValues()) && (cacheValue == null)) {
                return get(key);
            }
            byte[] result = this.writer.putIfAbsent(this.name, createAndConvertCacheKey(key), serializeCacheValue(cacheValue), StringUtils.contains(this.name, "-EXPIRE-") ? Duration.ofSeconds(Long.valueOf(this.name.split("-EXPIRE-")[1]).longValue()) : this.configuration.getTtl());
            return result == null ? null : new SimpleValueWrapper(fromStoreValue(deserializeCacheValue(result)));
        }
    }
}
