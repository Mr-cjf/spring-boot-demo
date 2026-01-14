package top.cjf_rb.redis.context.type.accessor;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Optional;

/**
 前缀固定的缓存存取器

 @author cjf
 @since 1.0 */
public interface PrefixCacheAccessor<T> {

    /**
     获取缓存数据

     @param identifier 标识
     @return 缓存数据
     */
    Optional<T> get(@NonNull Serializable identifier);

    /**
     设置缓存, 会覆盖就数据

     @param identifier 标识
     @param content    缓存的内容
     */
    void set(@NonNull Serializable identifier, T content);

    /**
     数据不存在才设置缓存

     @param identifier 标识
     @param content    缓存的内容
     */
    void setIfAbsent(@NonNull Serializable identifier, T content);

    /**
     缓存是否存在

     @param identifier 标识
     @return 存在则true, 反之, false
     */
    boolean exists(@NonNull Serializable identifier);

    /**
     清除缓存

     @param identifier 标识
     */
    void clear(@NonNull Serializable identifier);

}
