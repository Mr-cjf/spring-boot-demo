package top.cjf_rb.redis.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 Redis工具类，提供Redis操作的便捷方法
 使用Spring Data Redis和Jackson进行序列化/反序列化
 <p>
 警告：使用该工具类的所有缓存操作默认设置1天过期时间，
 如需自定义过期时间应使用带timeout参数的方法
 </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisUtils {

    // 默认Redis数据库索引
    public static int REDIS_DB_DEFAULT = 0;
    // Redis模板对象，用于执行Redis操作
    private final RedisTemplate<Object, Object> redisTemplate;
    private final LettuceConnectionFactory redisConnectionFactory;
    // JSON对象映射器，用于复杂类型的序列化/反序列化
    private final ObjectMapper objectMapper;

    // 为每个数据库维护一个独立的RedisTemplate，每个模板使用独立的连接工厂
    private final Map<Integer, RedisTemplate<Object, Object>> dbTemplates = new ConcurrentHashMap<>();

    /**
     根据数据库索引获取对应的RedisTemplate实例

     @param dbIndex 数据库索引
     @return 对应数据库的RedisTemplate实例
     */
    private RedisTemplate<Object, Object> getTemplateForDB(int dbIndex) {
        if (dbIndex == REDIS_DB_DEFAULT) {
            return redisTemplate;
        }

        return dbTemplates.computeIfAbsent(dbIndex, index -> {
            // 获取原始配置并创建新的配置，设置数据库索引
            RedisStandaloneConfiguration standaloneConfiguration = redisConnectionFactory.getStandaloneConfiguration();

            // 创建新的独立配置并设置数据库索引
            RedisStandaloneConfiguration newStandaloneConfig = new RedisStandaloneConfiguration(
                    standaloneConfiguration.getHostName(), standaloneConfiguration.getPort());
            newStandaloneConfig.setDatabase(index);
            newStandaloneConfig.setPassword(standaloneConfiguration.getPassword());

            // 为每个数据库创建独立的连接工厂配置
            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(newStandaloneConfig,
                                                                                      redisConnectionFactory.getClientConfiguration());
            connectionFactory.afterPropertiesSet();

            RedisTemplate<Object, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.afterPropertiesSet(); // 初始化模板
            return template;
        });
    }

    /**
     在指定数据库索引上执行操作

     @param dbIndex  数据库索引
     @param callback 回调函数
     @param <T>      返回值类型
     @return 执行结果
     */
    private <T> T executeInDB(int dbIndex, RedisCallback<T> callback) {
        if (dbIndex == REDIS_DB_DEFAULT) {
            return redisTemplate.execute(callback);
        }

        // 直接使用指定数据库的模板，无需切换数据库
        return getTemplateForDB(dbIndex).execute(callback);
    }

    /**
     设置字符串值

     @param key   键
     @param value 值
     @param <T>   值的类型
     @return 操作是否成功
     */
    public <T> boolean set(String key, T value) {
        return set(key, value, REDIS_DB_DEFAULT);
    }

    /**
     设置字符串值到指定数据库

     @param key     键
     @param value   值
     @param dbIndex 数据库索引
     @param <T>     值的类型
     */
    public <T> boolean set(String key, T value, int dbIndex) {
        // 如果key没有设置过期时间，则设置默认过期时间
        if (!exists(key, dbIndex) || ttl(key, dbIndex) == -1) {
            return set(key, value, 1, TimeUnit.DAYS, dbIndex);
        } else {
            return set(key, value, 0, TimeUnit.DAYS, dbIndex);
        }
    }

    /**
     设置字符串值并指定过期时间

     @param key     键
     @param value   值
     @param timeout 过期时间
     @param unit    时间单位
     @param <T>     值的类型
     */
    public <T> boolean set(String key, T value, long timeout, TimeUnit unit) {
        return set(key, value, timeout, unit, REDIS_DB_DEFAULT);
    }

    /**
     设置字符串值并指定过期时间和数据库

     @param key     键
     @param value   值
     @param timeout 过期时间
     @param unit    时间单位
     @param dbIndex 数据库索引
     @param <T>     值的类型
     */
    public <T> boolean set(String key, T value, long timeout, TimeUnit unit, int dbIndex) {
        return Boolean.TRUE.equals(executeInDB(dbIndex, connection -> {
            RedisStringCommands stringCommands = connection.stringCommands();

            // 使用JSON序列化替代Java原生序列化
            byte[] serializedValue = serializeValue(value);

            if (timeout > 0) {
                return stringCommands.set(key.getBytes(), serializedValue, Expiration.from(timeout, unit),
                                          RedisStringCommands.SetOption.UPSERT);
            } else {
                return stringCommands.set(key.getBytes(), serializedValue);
            }
        }));
    }

    /**
     获取字符串值

     @param key   键
     @param clazz 值类型的Class对象
     @param <T>   值的类型
     @return 值对象
     */
    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取字符串值

     @param key     键
     @param clazz   值类型的Class对象
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 值对象
     */
    public <T> T get(String key, Class<T> clazz, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            // 使用新的 stringCommands API
            RedisStringCommands stringCommands = connection.stringCommands();
            byte[] value = stringCommands.get(getBytes(key));

            if (value == null) {
                return null;
            }

            // 使用JSON反序列化替代Java原生反序列化
            try {
                return objectMapper.readValue(value, clazz);
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize value using JSON", e);
            }
        });
    }

    /**
     获取字符串值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param typeReference 值类型的TypeReference对象
     @param <T>           值的类型
     @return 值对象
     @throws IllegalArgumentException 当Redis键格式无效时抛出
     */
    @SneakyThrows
    public <T> T get(String key, TypeReference<T> typeReference) {
        return get(key, typeReference, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取字符串值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param typeReference 值类型的TypeReference对象
     @param dbIndex       数据库索引
     @param <T>           值的类型
     @return 值对象
     @throws IllegalArgumentException 当Redis键格式无效时抛出
     */
    @SneakyThrows
    public <T> T get(String key, TypeReference<T> typeReference, int dbIndex) {

        RedisTemplate<Object, Object> template = getTemplateForDB(dbIndex);
        byte[] value = template.execute((RedisCallback<byte[]>) connection -> {
            RedisStringCommands stringCommands = connection.stringCommands();
            return stringCommands.get(getBytes(key));
        });

        if (value == null) {
            return null;
        }

        // 反序列化为字符串
        String valueStr = new String(value);
        // 使用 JSON 库进行反序列化
        // 例如使用 Jackson: objectMapper.readValue(value.toString(), typeReference)
        return objectMapper.readValue(valueStr, typeReference);
    }

    /**
     获取字符串值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param typeReference 值类型的TypeReference对象
     @param <T>           值的类型
     @return 值对象的Optional包装
     */
    public <T> Optional<T> getOptional(String key, TypeReference<T> typeReference) {
        return getOptional(key, typeReference, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取字符串值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param typeReference 值类型的TypeReference对象
     @param dbIndex       数据库索引
     @param <T>           值的类型
     @return 值对象的Optional包装
     */
    public <T> Optional<T> getOptional(String key, TypeReference<T> typeReference, int dbIndex) {
        try {
            RedisTemplate<Object, Object> template = getTemplateForDB(dbIndex);
            byte[] value = template.execute((RedisCallback<byte[]>) connection -> {
                RedisStringCommands stringCommands = connection.stringCommands();
                return stringCommands.get(getBytes(key));
            });

            if (value == null) {
                return Optional.empty();
            }

            // 反序列化为字符串
            String valueStr = new String(value);
            T result = objectMapper.readValue(valueStr, typeReference);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.warn("Redis getOptional failed for key: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     获取字符串值

     @param key   键
     @param clazz 值类型的Class对象
     @param <T>   值的类型
     @return 值对象的Optional包装
     */
    public <T> Optional<T> getOptional(String key, Class<T> clazz) {
        return getOptional(key, clazz, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取字符串值

     @param key     键
     @param clazz   值类型的Class对象
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 值对象的Optional包装
     */
    public <T> Optional<T> getOptional(String key, Class<T> clazz, int dbIndex) {
        RedisTemplate<Object, Object> template = getTemplateForDB(dbIndex);
        return Optional.ofNullable(template.execute((RedisCallback<T>) connection -> {
            // 使用新的 stringCommands API
            RedisStringCommands stringCommands = connection.stringCommands();
            byte[] value = stringCommands.get(getBytes(key));

            if (value == null) {
                return null;
            }

            // 使用配置的序列化器
            RedisSerializer<?> serializer = template.getValueSerializer();
            return clazz.cast(serializer.deserialize(value));
        }));
    }

    /**
     删除指定key

     @param key 键
     @return 删除成功返回true，否则返回false
     */
    public Boolean del(String key) {
        return del(key, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库删除指定key

     @param key     键
     @param dbIndex 数据库索引
     @return 删除成功返回true，否则返回false
     */
    public Boolean del(String key, int dbIndex) {
        return executeInDB(dbIndex, connection -> Objects.equals(connection.keyCommands()
                                                                           .del(getBytes(key)), 1L));
    }

    /**
     判断key是否存在

     @param key 键
     @return 存在返回true，否则返回false
     */
    public Boolean exists(String key) {
        return exists(key, REDIS_DB_DEFAULT);
    }

    /**
     判断指定数据库中key是否存在

     @param key     键
     @param dbIndex 数据库索引
     @return 存在返回true，否则返回false
     */
    public Boolean exists(String key, int dbIndex) {
        return executeInDB(dbIndex, connection -> connection.keyCommands()
                                                            .exists(getBytes(key)));
    }

    /**
     设置指定数据库中key的过期时间

     @param key     键
     @param timeout 过期时间
     @param unit    时间单位
     @param dbIndex 数据库索引
     @return 设置成功返回true，否则返回false
     */
    public Boolean expire(String key, long timeout, TimeUnit unit, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            long seconds = unit.toSeconds(timeout);
            return connection.keyCommands()
                             .expire(getBytes(key), seconds);
        });
    }

    /**
     获取指定数据库中key的剩余生存时间

     @param key     键
     @param dbIndex 数据库索引
     @return 剩余生存时间(秒)，-1表示没有设置过期时间，-2表示key不存在
     */
    public Long ttl(String key, int dbIndex) {
        return executeInDB(dbIndex, connection -> connection.keyCommands()
                                                            .ttl(getBytes(key)));
    }

    /**
     添加整个Set集合到Set

     @param key 键
     @param set 要添加的Set集合
     @param <T> 值的类型
     @return 添加成功的元素数量
     */
    public <T> Long setAdd(String key, Set<T> set) {
        return setAdd(key, set, REDIS_DB_DEFAULT);
    }

    /**
     添加整个Set集合到指定数据库Set

     @param key     键
     @param set     要添加的Set集合
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 添加成功的元素数量
     */
    public <T> Long setAdd(String key, Set<T> set, int dbIndex) {
        Long result = executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);

            // 将Set中的每个元素序列化
            byte[][] rawValues = set.stream()
                                    .map(this::serializeValue)
                                    .toArray(byte[][]::new);

            return connection.setCommands()
                             .sAdd(rawKey, rawValues);
        });
        // 如果key没有设置过期时间，则设置默认过期时间
        setDefaultExpireIfNeeded(key, dbIndex);
        return result;
    }

    /**
     获取Set中的所有成员

     @param key   键
     @param clazz 值类型的Class对象
     @param <T>   值的类型
     @return Set中所有元素的集合
     */
    public <T> Set<T> getSetMembers(String key, Class<T> clazz) {
        return getSetMembers(key, clazz, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取Set中的所有成员

     @param key     键
     @param clazz   值类型的Class对象
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return Set中所有元素的集合
     */
    public <T> Set<T> getSetMembers(String key, Class<T> clazz, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);

            Set<byte[]> rawMembers = connection.setCommands()
                                               .sMembers(rawKey);
            if (rawMembers == null) {
                return new HashSet<>();
            }

            // 使用JSON反序列化替代Java原生反序列化
            try {
                Set<T> members = new HashSet<>();
                for (byte[] rawMember : rawMembers) {
                    T member = objectMapper.readValue(rawMember, clazz);
                    members.add(member);
                }
                return members;
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize value using JSON", e);
            }
        });
    }


    /**
     设置默认过期时间（如果需要）

     @param key     键
     @param dbIndex 数据库索引
     */
    private void setDefaultExpireIfNeeded(String key, int dbIndex) {
        if (!exists(key, dbIndex) || ttl(key, dbIndex) == -1) {
            // 如果key没有设置过期时间，则设置默认过期时间
            Boolean result = expire(key, 1, TimeUnit.DAYS, dbIndex);
            if (Boolean.FALSE.equals(result)) {
                log.warn("Failed to set default expiration for key: {}", key);
            }
        }
    }

    /**
     设置Hash中的字段值

     @param key   键
     @param field 字段名
     @param value 值
     @param <T>   值的类型
     @return 如果字段是新增的返回true，如果是更新已存在的字段返回false
     */
    public <T> Boolean hashSet(String key, String field, T value) {
        return hashSet(key, field, value, REDIS_DB_DEFAULT);
    }

    /**
     设置Hash中的字段值并指定数据库

     @param key     键
     @param field   字段名
     @param value   值
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 如果字段是新增的返回true，如果是更新已存在的字段返回false
     */
    public <T> Boolean hashSet(String key, String field, T value, int dbIndex) {
        Boolean result = executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);
            byte[] rawField = getBytes(field);
            byte[] rawValue = serializeValue(value);

            return connection.hashCommands()
                             .hSet(rawKey, rawField, rawValue);
        });
        setDefaultExpireIfNeeded(key, dbIndex);
        return result;
    }

    /**
     获取Hash中指定字段的值

     @param key   键
     @param field 字段名
     @param clazz 值类型的Class对象
     @param <T>   值的类型
     @return 字段值
     */
    public <T> T hashGet(String key, String field, Class<T> clazz) {
        return hashGet(key, field, clazz, REDIS_DB_DEFAULT);
    }

    /**
     获取Hash中指定字段的值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param field         字段名
     @param typeReference 值类型的TypeReference对象
     @param <T>           值的类型
     @return 字段值
     */
    public <T> T hashGet(String key, String field, TypeReference<T> typeReference) {
        return hashGet(key, field, typeReference, REDIS_DB_DEFAULT);
    }

    /**
     内部方法：从指定数据库获取Hash中指定字段的值

     @param key           键
     @param field         字段名
     @param clazz         值类型的Class对象
     @param typeReference 值类型的TypeReference对象
     @param dbIndex       数据库索引
     @param <T>           值的类型
     @return 字段值
     */
    private <T> T hashGetInternal(String key, String field, Class<T> clazz, TypeReference<T> typeReference,
                                  int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);
            byte[] rawField = getBytes(field);

            byte[] rawValue = connection.hashCommands()
                                        .hGet(rawKey, rawField);
            if (rawValue == null) {
                return null;
            }

            try {
                if (clazz != null) {
                    return objectMapper.readValue(rawValue, clazz);
                } else {
                    return objectMapper.readValue(rawValue, typeReference);
                }
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize value using JSON", e);
            }
        });
    }

    /**
     从指定数据库获取Hash中指定字段的值

     @param key     键
     @param field   字段名
     @param clazz   值类型的Class对象
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 字段值
     */
    public <T> T hashGet(String key, String field, Class<T> clazz, int dbIndex) {
        return hashGetInternal(key, field, clazz, null, dbIndex);
    }

    /**
     从指定数据库获取Hash中指定字段的值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param field         字段名
     @param typeReference 值类型的TypeReference对象
     @param dbIndex       数据库索引
     @param <T>           值的类型
     @return 字段值
     */
    public <T> T hashGet(String key, String field, TypeReference<T> typeReference, int dbIndex) {
        return hashGetInternal(key, field, null, typeReference, dbIndex);
    }

    /**
     从指定数据库删除Hash中的一个或多个字段

     @param key     键
     @param dbIndex 数据库索引
     @param fields  字段名数组
     @return 被成功删除的字段数量
     */
    public Long hashDelete(String key, int dbIndex, String... fields) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);
            byte[][] rawFields = Arrays.stream(fields)
                                       .map(this::getBytes)
                                       .toArray(byte[][]::new);

            return connection.hashCommands()
                             .hDel(rawKey, rawFields);
        });
    }

    /**
     检查指定数据库的Hash中是否存在指定字段

     @param key     键
     @param field   字段名
     @param dbIndex 数据库索引
     @return 存在返回true，否则返回false
     */
    public Boolean hashExists(String key, String field, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);
            byte[] rawField = getBytes(field);

            return connection.hashCommands()
                             .hExists(rawKey, rawField);
        });
    }

    /**
     获取Hash中所有字段和值

     @param key   键
     @param clazz 值类型的Class对象
     @param <T>   值的类型
     @return 包含所有字段和值的Map
     */
    public <T> Map<String, T> hashGetAll(String key, Class<T> clazz) {
        return hashGetAll(key, clazz, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取Hash中所有字段和值

     @param key     键
     @param clazz   值类型的Class对象
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 包含所有字段和值的Map
     */
    public <T> Map<String, T> hashGetAll(String key, Class<T> clazz, int dbIndex) {
        return hashGetAllInternal(key, clazz, null, dbIndex);
    }

    /**
     获取Hash中所有字段和值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param typeReference 值类型的TypeReference对象
     @param <T>           值的类型
     @return 包含所有字段和值的Map
     */
    public <T> Map<String, T> hashGetAll(String key, TypeReference<T> typeReference) {
        return hashGetAll(key, typeReference, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取Hash中所有字段和值（使用 TypeReference 支持复杂泛型）

     @param key           键
     @param typeReference 值类型的TypeReference对象
     @param dbIndex       数据库索引
     @param <T>           值的类型
     @return 包含所有字段和值的Map
     */
    public <T> Map<String, T> hashGetAll(String key, TypeReference<T> typeReference, int dbIndex) {
        return hashGetAllInternal(key, null, typeReference, dbIndex);
    }

    /**
     内部方法：从指定数据库获取Hash中所有字段和值

     @param key           键
     @param clazz         值类型的Class对象
     @param typeReference 值类型的TypeReference对象
     @param dbIndex       数据库索引
     @param <T>           值的类型
     @return 包含所有字段和值的Map
     */
    private <T> Map<String, T> hashGetAllInternal(String key, Class<T> clazz, TypeReference<T> typeReference,
                                                  int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);

            Map<byte[], byte[]> rawMap = connection.hashCommands()
                                                   .hGetAll(rawKey);
            if (rawMap == null) {
                return new HashMap<>();
            }

            Map<String, T> resultMap = new HashMap<>();
            try {
                for (Map.Entry<byte[], byte[]> entry : rawMap.entrySet()) {
                    String field = new String(entry.getKey());
                    T value;
                    if (clazz != null) {
                        value = objectMapper.readValue(entry.getValue(), clazz);
                    } else {
                        value = objectMapper.readValue(entry.getValue(), typeReference);
                    }
                    resultMap.put(field, value);
                }
                return resultMap;
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize value using JSON", e);
            }
        });
    }

    /**
     获取Hash中的字段数量

     @param key 键
     @return 字段数量
     */
    public Long hashLen(String key) {
        return hashLen(key, REDIS_DB_DEFAULT);
    }

    /**
     获取指定数据库Hash中的字段数量

     @param key     键
     @param dbIndex 数据库索引
     @return 字段数量
     */
    public Long hashLen(String key, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);

            return connection.hashCommands()
                             .hLen(rawKey);
        });
    }

    /**
     获取Hash中所有字段名

     @param key 键
     @return 所有字段名的集合
     */
    public Set<String> hashKeys(String key) {
        return hashKeys(key, REDIS_DB_DEFAULT);
    }

    /**
     获取指定数据库Hash中所有字段名

     @param key     键
     @param dbIndex 数据库索引
     @return 所有字段名的集合
     */
    public Set<String> hashKeys(String key, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);

            Set<byte[]> rawKeys = connection.hashCommands()
                                            .hKeys(rawKey);
            if (rawKeys == null) {
                return new HashSet<>();
            }

            Set<String> keys = new HashSet<>();
            for (byte[] rawKeyBytes : rawKeys) {
                keys.add(new String(rawKeyBytes));
            }
            return keys;
        });
    }

    /**
     获取Hash中所有值

     @param key   键
     @param clazz 值类型的Class对象
     @param <T>   值的类型
     @return 所有值的集合
     */
    public <T> List<T> hashVals(String key, Class<T> clazz) {
        return hashVals(key, clazz, REDIS_DB_DEFAULT);
    }

    /**
     从指定数据库获取Hash中所有值

     @param key     键
     @param clazz   值类型的Class对象
     @param dbIndex 数据库索引
     @param <T>     值的类型
     @return 所有值的集合
     */
    public <T> List<T> hashVals(String key, Class<T> clazz, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            byte[] rawKey = getBytes(key);

            List<byte[]> rawValues = connection.hashCommands()
                                               .hVals(rawKey);
            if (rawValues == null) {
                return new ArrayList<>();
            }

            List<T> values = new ArrayList<>();
            try {
                for (byte[] rawValue : rawValues) {
                    T value = objectMapper.readValue(rawValue, clazz);
                    values.add(value);
                }
                return values;
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize value using JSON", e);
            }
        });
    }

    /**
     将字符串转换为字节数组

     @param str 要转换的字符串
     @return 字节数组
     */
    private byte[] getBytes(String str) {
        return str.getBytes();
    }

    /**
     将值序列化为字节数组

     @param value 要序列化的值
     @param <T>   值的类型
     @return 序列化后的字节数组
     @throws SerializationException 序列化失败时抛出
     */
    private <T> byte[] serializeValue(T value) throws SerializationException {
        try {
            byte[] rawValue = objectMapper.writeValueAsBytes(value);
            if (rawValue == null) {
                throw new SerializationException("Failed to serialize value: serialized value is null");
            }
            return rawValue;
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize value using JSON", e);
        }
    }

    /**
     获取匹配指定模式的所有键

     @param pattern 键的模式，支持通配符(*)匹配
     @param dbIndex 数据库索引
     @return 匹配的键集合
     */
    public Set<String> keys(String pattern, int dbIndex) {
        return executeInDB(dbIndex, connection -> {
            Set<String> keys = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.keyCommands()
                                                   .scan(org.springframework.data.redis.core.ScanOptions.scanOptions()
                                                                                                        .match(pattern)
                                                                                                        .count(1000)
                                                                                                        .build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                log.error("扫描Redis键时出错，pattern: {}", pattern, e);
            }
            return keys;
        });
    }

    /**
     获取匹配指定模式的所有键

     @param pattern 键的模式，支持通配符(*)匹配
     @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        return keys(pattern, REDIS_DB_DEFAULT);
    }

    /**
     原子递增操作

     @param key 键
     @return 递增后的值
     */
    public Long incr(String key) {
        return incr(key, REDIS_DB_DEFAULT);
    }

    /**
     在指定数据库中原子递增操作

     @param key     键
     @param dbIndex 数据库索引
     @return 递增后的值
     */
    public Long incr(String key, int dbIndex) {
        Long result = executeInDB(dbIndex, connection -> {
            RedisStringCommands stringCommands = connection.stringCommands();
            return stringCommands.incr(getBytes(key));
        });

        // 如果key没有设置过期时间，则设置默认过期时间
        setDefaultExpireIfNeeded(key, dbIndex);
        return result;
    }

    /**
     带过期时间的原子递增操作

     @param key     键
     @param timeout 过期时间
     @param unit    时间单位
     @return 递增后的值
     */
    public Long incr(String key, long timeout, TimeUnit unit) {
        return incr(key, timeout, unit, REDIS_DB_DEFAULT);
    }

    /**
     在指定数据库中带过期时间的原子递增操作

     @param key     键
     @param timeout 过期时间
     @param unit    时间单位
     @param dbIndex 数据库索引
     @return 递增后的值
     */
    public Long incr(String key, long timeout, TimeUnit unit, int dbIndex) {

        return executeInDB(dbIndex, connection -> {
            RedisStringCommands stringCommands = connection.stringCommands();
            byte[] rawKey = getBytes(key);

            // 先递增
            Long value = stringCommands.incr(rawKey);

            // 设置过期时间
            if (timeout > 0) {
                long seconds = unit.toSeconds(timeout);
                connection.keyCommands()
                          .expire(rawKey, seconds);
            }

            return value;
        });
    }
}