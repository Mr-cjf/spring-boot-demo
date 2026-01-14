package top.cjf_rb.redis.context.type.accessor;

import java.util.Optional;

/**
 标识固定的缓存存取器

 @author cjf
 @since 1.0 */
public interface CacheAccessor<T> {

    /**
     获取缓存数据

     @return 缓存数据
     */
    Optional<T> get();

    /**
     设置缓存, 会覆盖就数据

     @param content 缓存的内容
     */
    void set(T content);

    /**
     数据不存在才设置缓存

     @param content 缓存的内容
     */
    void setIfAbsent(T content);

    /**
     缓存是否存在

     @return 存在则true, 反之, false
     */
    boolean exists();

    /**
     清除缓存
     */
    void clear();

}
