package top.cjf_rb.xxl_job.constant;

/**
 执行器路由策略

 @author cjf
 @since 1.0 */
public final class ExecutorRouteStrategy {

    /**
     第一个
     */
    public static final String FIRST = "FIRST";

    /**
     最后一个
     */
    public static final String LAST = "LAST";

    /**
     轮询
     */
    public static final String ROUND = "ROUND";

    /**
     随机
     */
    public static final String RANDOM = "RANDOM";

    /**
     一致性HASH
     */
    public static final String CONSISTENT_HASH = "CONSISTENT_HASH";

    /**
     最不经常使用
     */
    public static final String LEAST_FREQUENTLY_USED = "LEAST_FREQUENTLY_USED";

    /**
     最近最久未使用
     */
    public static final String LEAST_RECENTLY_USED = "LEAST_RECENTLY_USED";

    /**
     故障转移
     */
    public static final String FAILOVER = "FAILOVER";

    /**
     忙碌转移
     */
    public static final String BUSYOVER = "BUSYOVER";

    /**
     分片广播
     */
    public static final String SHARDING_BROADCAST = "SHARDING_BROADCAST";

}
