package top.cjf_rb.gateway.constants;

/**
 * 应用自定义的一些请求头
 *
 * @author cjf
 * @since 1.0
 */
public final class AppHeaderConst {
    /**
     * 追踪ID
     */
    public static final String TRACE_ID = "X-Trace-Id";
    /**
     * 用户Token
     */
    public static final String ACCESS_TOKEN = "X-Access-Token";
    /**
     * 当前用户
     */
    public static final String CURRENT_USER = "X-Current-User";
    /**
     * 当前类型
     */
    public static final String CLIENT_AGENT = "X-Client-Agent";

    /**
     * API限流 时间内剩余次数
     */
    public static final String API_RATE_LIMIT_LIMIT = "X-Api-RateLimit-Limit";
    /**
     * API限流 重置限流的时间戳
     */
    public static final String API_RATE_LIMIT_RESET = "X-Api-RateLimit-Reset";

}
