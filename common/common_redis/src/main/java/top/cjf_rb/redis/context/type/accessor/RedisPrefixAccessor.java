package top.cjf_rb.redis.context.type.accessor;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.*;
import org.springframework.lang.NonNull;
import top.cjf_rb.core.constant.SeparatorEnum;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 基于Redis实现的存取器

 @author cjf
 @since 1.0 */
public class RedisPrefixAccessor<T> implements PrefixCacheAccessor<T> {

    public final String keyPrefix;
    public final Duration expired;
    @Resource
    private RedisTemplate<Serializable, T> redisTemplate;

    /**
     @param prefix  key前缀
     @param expired 有效期
     */
    public RedisPrefixAccessor(String prefix, Duration expired) {
        this.expired = expired;
        if (prefix.endsWith(SeparatorEnum.COLON.getSeparator())) {
            this.keyPrefix = prefix;
        } else {
            this.keyPrefix = prefix + SeparatorEnum.COLON.getSeparator();
        }
    }

    @Override
    public void set(@NonNull Serializable identifier, T content) {
        String key = keyPrefix + identifier;

        ValueOperations<Serializable, T> forValue = redisTemplate.opsForValue();
        forValue.set(key, content, expired);
    }

    @Override
    public void setIfAbsent(@NonNull Serializable identifier, T content) {
        String key = keyPrefix + identifier;

        ValueOperations<Serializable, T> forValue = redisTemplate.opsForValue();
        forValue.setIfAbsent(key, content, expired);
    }

    @Override
    public Optional<T> get(@NonNull Serializable identifier) {
        String key = keyPrefix + identifier;

        ValueOperations<Serializable, T> forValue = redisTemplate.opsForValue();
        return Optional.ofNullable(forValue.get(key));
    }

    /**
     获取当前prefix key的总数量

     @param scanCount 每次扫描Redis key的数量
     @return prefix key的总数量
     */
    public int count(int scanCount) {
        return this.count("", scanCount);
    }

    /**
     获取当前prefix key的总数量

     @param identifier 标识
     @param scanCount  每次扫描Redis key的数量
     @return prefix key的总数量
     */
    public int count(Serializable identifier, int scanCount) {
        String key = keyPrefix + identifier;

        int count = 0;
        try (Cursor<byte[]> cursor = this.getCursor(key, scanCount)) {
            if (Objects.isNull(cursor)) {
                return 0;
            }

            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        }

        return count;
    }

    /**
     获取多个值, 默认扫描的keys为: {@link RedisPrefixAccessor#keyPrefix} + '*'

     @param scanCount 每次扫描Redis key的数量, 并非返回集合的总数
     */
    public List<T> multiGet(int scanCount) {
        return this.multiGet("", scanCount);
    }

    /**
     获取多个值, 默认扫描的keys为: {@link RedisPrefixAccessor#keyPrefix} + identifier + '*'

     @param identifier 标识
     @param scanCount  每次扫描Redis key的数量, 并非返回集合的总数
     */
    public List<T> multiGet(Serializable identifier, int scanCount) {
        String key = keyPrefix + identifier;

        Set<Serializable> keys = new HashSet<>(scanCount * 4);
        try (Cursor<byte[]> cursor = this.getCursor(key, scanCount)) {
            if (Objects.isNull(cursor)) {
                return Collections.emptyList();
            }

            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
        }

        ValueOperations<Serializable, T> forValue = redisTemplate.opsForValue();
        return forValue.multiGet(keys);
    }

    @Override
    public boolean exists(@NonNull Serializable identifier) {
        String key = keyPrefix + identifier;

        return redisTemplate.hasKey(key);
    }

    @Override
    public void clear(@NonNull Serializable identifier) {
        String key = keyPrefix + identifier;

        redisTemplate.delete(key);
    }

    /**
     获取游标

     @param keyPrefix key前缀
     @param scanCount 扫描数量
     @return 游标
     */
    private Cursor<byte[]> getCursor(String keyPrefix, int scanCount) {
        ScanOptions scanOptions = ScanOptions.scanOptions()
                                             .match(keyPrefix + "*")
                                             .count(scanCount)
                                             .build();
        return redisTemplate.execute((RedisCallback<Cursor<byte[]>>) connection -> connection.keyCommands()
                                                                                             .scan(scanOptions));
    }
}
