package top.cjf_rb.security.support;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.cjf_rb.core.constant.ClientAgentEnum;
import top.cjf_rb.security.pojo.bo.AuthenticatedUserBo;

import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;

/**
 * <pre>
 *     <h3>登陆令牌的缓存操作工具</h3>
 *     <li>key的格式：${keyPrefix}:${端口}:${userId}</li>
 *     <li>key的示例：app-authenticatedUser:oem:12345</li>
 * </pre>
 */
@Component
public class AuthUserAccessor {

    private static final String keyPrefix = "beego:authenticatedUser:";
    private static final Duration expired = Duration.ofHours(12L);
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    @Lazy
    private RedisTemplate<Serializable, String> redisTemplate;

    /**
     * <pre>
     *
     * 拼接redis的主键key
     * <li>示例：app-authenticatedUser:oem:12345</li>
     * &#64;param type {@linkplain ClientAgentEnum} 客户端类型
     * @param identifier 用户id
     *
     * </pre>
     */
    private String generateKey(ClientAgentEnum type, Serializable identifier) {
        return keyPrefix + type + ":" + identifier;
    }

    public void set(@NonNull ClientAgentEnum type, @NonNull Serializable identifier, AuthenticatedUserBo content) {

        String key = generateKey(type, identifier);

        ValueOperations<Serializable, String> forValue = redisTemplate.opsForValue();
        try {
            String asString = objectMapper.writeValueAsString(content);
            forValue.set(key, asString, expired);
        } catch (JsonProcessingException ignore) {
        }
    }

    public Optional<AuthenticatedUserBo> get(@NonNull ClientAgentEnum type, @NonNull Serializable identifier) {

        String key = generateKey(type, identifier);

        ValueOperations<Serializable, String> forValue = redisTemplate.opsForValue();
        String s = forValue.get(key);
        if (!StringUtils.hasText(s)) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(s, AuthenticatedUserBo.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(@NonNull ClientAgentEnum type, @NonNull Serializable identifier) {

        String key = generateKey(type, identifier);

        return redisTemplate.hasKey(key);
    }

    public void clear(@NonNull ClientAgentEnum type, @NonNull Serializable identifier) {
        String key = generateKey(type, identifier);

        redisTemplate.delete(key);
    }
}
