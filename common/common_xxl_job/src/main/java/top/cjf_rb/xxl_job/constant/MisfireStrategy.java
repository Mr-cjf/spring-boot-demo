package top.cjf_rb.xxl_job.constant;

/**
 调度过期策略

 @author cjf
 @since 1.0 */
public final class MisfireStrategy {

    /**
     忽略
     */
    public static final String DO_NOTHING = "DO_NOTHING";

    /**
     立即执行一次
     */
    public static final String FIRE_ONCE_NOW = "FIRE_ONCE_NOW";

}
