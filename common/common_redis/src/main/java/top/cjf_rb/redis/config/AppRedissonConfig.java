package top.cjf_rb.redis.config;


import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * 原封复制RedissonAutoConfiguration的配置
 */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
public class AppRedissonConfig {
    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    private final RedisProperties redisProperties;

    @Bean
    @Lazy
    @ConditionalOnMissingBean(RedissonReactiveClient.class)
    public RedissonReactiveClient redissonReactive(RedissonClient redisson) {
        return redisson.reactive();
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(RedissonRxClient.class)
    public RedissonRxClient redissonRxJava(RedissonClient redisson) {
        return redisson.rxJava();
    }

    @SuppressWarnings("all")
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redisson() {
        // 创建RedissonClient的配置
        Config config;

        // 获取Redis的连接超时时间，默认为10秒
        int timeout = redisProperties.getTimeout() != null ?
                (int) redisProperties.getTimeout().toMillis() : 10000;

        // 如果Redis配置了哨兵模式
        if (redisProperties.getSentinel() != null) {
            config = new Config();
            // 使用哨兵模式
            config.useSentinelServers()
                    // 设置主节点名称
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    // 添加哨兵节点地址
                    .addSentinelAddress(convert(redisProperties.getSentinel().getNodes()))
                    // 设置数据库编号
                    .setDatabase(redisProperties.getDatabase())
                    // 设置连接超时时间
                    .setConnectTimeout(timeout)
                    // 设置密码
                    .setPassword(redisProperties.getPassword());
            // 如果Redis配置了集群模式
        } else if (redisProperties.getCluster() != null) {
            config = new Config();
            // 使用集群模式
            config.useClusterServers()
                    // 添加集群节点地址
                    .addNodeAddress(convert(redisProperties.getCluster().getNodes()))
                    // 设置连接超时时间
                    .setConnectTimeout(timeout)
                    // 设置密码
                    .setPassword(redisProperties.getPassword());
            // 否则，使用单节点模式
        } else {
            String url = redisProperties.getUrl();
            // 判断是否使用SSL
            boolean useSsl = url != null && url.startsWith(REDISS_PROTOCOL_PREFIX);
            // 设置协议前缀
            String prefix = useSsl ? REDISS_PROTOCOL_PREFIX : REDIS_PROTOCOL_PREFIX;
            config = new Config();
            // 使用单节点模式
            config.useSingleServer()
                    // 设置节点地址
                    .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                    // 设置连接超时时间
                    .setConnectTimeout(timeout)
                    // 设置数据库编号
                    .setDatabase(redisProperties.getDatabase())
                    // 设置密码
                    .setPassword(redisProperties.getPassword());
        }
        // 创建RedissonClient
        return Redisson.create(config);
    }

    // 将List<String>类型的nodesObject转换为String[]类型
    private String[] convert(List<String> nodesObject) {
        // 使用stream()方法将nodesObject转换为流
        return nodesObject.stream()
                // 使用map()方法对流中的每个元素进行处理
                .map(node -> node.startsWith(REDIS_PROTOCOL_PREFIX) || node.startsWith(REDISS_PROTOCOL_PREFIX) ?
                        // 如果元素以REDIS_PROTOCOL_PREFIX或REDISS_PROTOCOL_PREFIX开头，则直接返回该元素
                        node : REDIS_PROTOCOL_PREFIX + node)
                // 使用toArray()方法将流转换为String[]类型
                .toArray(String[]::new);
    }

}

