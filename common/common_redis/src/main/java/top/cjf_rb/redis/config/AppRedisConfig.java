package top.cjf_rb.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.LinkedHashSet;
import java.util.List;


@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties({CacheProperties.class})
public class AppRedisConfig {

    private final CacheProperties cacheProperties;


    /**
     * 配置RedisTemplate, 更改 key 和 value 的序列化器
     *
     * @param redisConnectionFactory RedisConnectionFactory
     * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
     */
    @Autowired
    public void customizeRedisTemplate(RedisTemplate<Object, Object> template,
                                       RedisConnectionFactory redisConnectionFactory,
                                       @Lazy GenericJackson2JsonRedisSerializer jsonRedisSerializer) {
        //RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // redis key 使用字符串序列化器
        template.setKeySerializer(template.getStringSerializer());
        template.setHashKeySerializer(template.getStringSerializer());

        // redis value 使用GenericJackson2JsonRedisSerializer 序列化器
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
    }

    @Bean
    public GenericJackson2JsonRedisSerializer
    jackson2JsonRedisSerializer(Jackson2ObjectMapperBuilder objectMapperBuilder) {
        ObjectMapper objectMapper = objectMapperBuilder.build();
        /*
         * 开启反序列化类型转换, 序列化增加泛型信息
         * DefaultTyping.NON_FINAL: 非final类型字段在序列化时加上类型信息
         * JsonTypeInfo.As.PROPERTY: 序列化时, 包含对象class类型信息
         */
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * 配置 Spring cache序列化器, 如果不配置, 则默认使用 JdkSerializationRedisSerializer
     *
     * @param redisConnectionFactory RedisConnectionFactory
     * @return RedisCacheManager
     * * 配置 Spring cache序列化器, 如果不配置, 则默认使用 JdkSerializationRedisSerializer
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                          GenericJackson2JsonRedisSerializer jsonRedisSerializer) {
        // 设置默认的缓存配置
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultCacheConfig(jsonRedisSerializer));

        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!cacheNames.isEmpty()) {
            // 初始化缓存名称
            builder.initialCacheNames(new LinkedHashSet<>(cacheNames));
        }
        // The statistics collector supports capturing of relevant RedisCache operations such as hits & misses.
        if (cacheProperties.getRedis().isEnableStatistics()) {
            // 启用缓存统计
            builder.enableStatistics();
        }

        return builder.build();
    }

    /**
     * 默认缓存配置
     *
     * @return RedisCacheConfiguration
     */
    private RedisCacheConfiguration defaultCacheConfig(GenericJackson2JsonRedisSerializer jsonRedisSerializer) {
        // 获取Redis缓存配置属性
        CacheProperties.Redis redisProperties = this.cacheProperties.getRedis();

        // 创建默认的Redis缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        // 设置Redis缓存配置的序列化器
        config =
                config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer));
        // 如果Redis缓存配置属性中设置了TTL，则设置Redis缓存配置的TTL
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 如果Redis缓存配置属性中设置了key前缀，则设置Redis缓存配置的key前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        // 如果Redis缓存配置属性中设置了不缓存null值，则设置Redis缓存配置不缓存null值
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 如果Redis缓存配置属性中设置了不使用key前缀，则设置Redis缓存配置不使用key前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        // 返回Redis缓存配置
        return config;
    }

}
