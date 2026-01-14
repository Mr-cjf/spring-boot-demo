package top.cjf_rb.redis.context.type.accessor;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

/**
 基于Redis实现的存取器

 @author cjf
 @since 1.0 */
public class RedisKeyfixAccessor<T> implements CacheAccessor<T> {

    public final String key;
    public final Duration expired;
    @Resource
    private RedisTemplate<String, T> redisTemplate;

    /**
     @param key     key前缀
     @param expired 有效期
     */
    public RedisKeyfixAccessor(String key, Duration expired) {
        this.expired = expired;
        this.key = key;
    }

    @Override
    public void set(T content) {
        ValueOperations<String, T> forValue = redisTemplate.opsForValue();
        forValue.set(key, content, expired);
    }

    @Override
    public void setIfAbsent(T content) {
        ValueOperations<String, T> forValue = redisTemplate.opsForValue();
        forValue.setIfAbsent(key, content, expired);
    }

    @Override
    public Optional<T> get() {
        ValueOperations<String, T> forValue = redisTemplate.opsForValue();
        return Optional.ofNullable(forValue.get(key));
    }

    @Override
    public boolean exists() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void clear() {
        redisTemplate.delete(key);
    }

}
