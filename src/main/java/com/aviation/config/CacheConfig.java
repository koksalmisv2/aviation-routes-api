package com.aviation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("routes", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("locations", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("transportations", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);

        return new RedisCacheManager(cacheWriter, defaultConfig, cacheConfigurations) {
            @Override
            protected Cache decorateCache(Cache cache) {
                return new LoggingCache(super.decorateCache(cache));
            }
        };
    }

    /**
     * A Cache decorator that logs HIT/MISS/PUT/EVICT/CLEAR operations.
     */
    private static class LoggingCache implements Cache {

        private final Cache delegate;

        LoggingCache(Cache delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper wrapper = delegate.get(key);
            if (wrapper != null) {
                log.debug("CACHE HIT  [{}] key={}", delegate.getName(), key);
            } else {
                log.debug("CACHE MISS [{}] key={}", delegate.getName(), key);
            }
            return wrapper;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = delegate.get(key, type);
            if (value != null) {
                log.debug("CACHE HIT  [{}] key={}", delegate.getName(), key);
            } else {
                log.debug("CACHE MISS [{}] key={}", delegate.getName(), key);
            }
            return value;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            // This variant is used by @Cacheable(sync=true).
            // We can't distinguish hit/miss easily here without double-lookup,
            // so we log the access and delegate.
            log.debug("CACHE GET  [{}] key={} (sync loader)", delegate.getName(), key);
            return delegate.get(key, valueLoader);
        }

        @Override
        public void put(Object key, Object value) {
            log.debug("CACHE PUT  [{}] key={}", delegate.getName(), key);
            delegate.put(key, value);
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            log.debug("CACHE PUT_IF_ABSENT [{}] key={}", delegate.getName(), key);
            return delegate.putIfAbsent(key, value);
        }

        @Override
        public void evict(Object key) {
            log.debug("CACHE EVICT [{}] key={}", delegate.getName(), key);
            delegate.evict(key);
        }

        @Override
        public boolean evictIfPresent(Object key) {
            log.debug("CACHE EVICT_IF_PRESENT [{}] key={}", delegate.getName(), key);
            return delegate.evictIfPresent(key);
        }

        @Override
        public void clear() {
            log.debug("CACHE CLEAR [{}]", delegate.getName());
            delegate.clear();
        }

        @Override
        public boolean invalidate() {
            log.debug("CACHE INVALIDATE [{}]", delegate.getName());
            return delegate.invalidate();
        }
    }
}
