package top.cjf_rb.xxl_job.constant;

/**
 阻塞处理策略

 @author lty
 @since 1.0 */
public final class ExecutorBlockStrategy {

    /**
     单机串行
     */
    public static final String SERIAL_EXECUTION = "SERIAL_EXECUTION";

    /**
     丢弃后续调度
     */
    public static final String DISCARD_LATER = "DISCARD_LATER";

    /**
     覆盖之前调度
     */
    public static final String COVER_EARLY = "COVER_EARLY";

}
