package top.cjf_rb.core.constant;

public class AppHeaderConst {
    /**
     * 追踪ID
     */
    public static final String TRACE_ID = "TRACE_ID";
    /**
     * 错误标记
     */
    public static final String ERROR_TAG = "ERROR_TAG";
    /**
     * 用户Token
     */
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    /**
     * 当前用户
     */
    public static final String CURRENT_USER = "CURRENT_USER";

    /**
     * API限流 时间内剩余次数
     */
    public static final String API_RATE_LIMIT_LIMIT = "API_RATE_LIMIT_LIMIT";
    /**
     * API限流 重置限流的时间戳
     */
    public static final String API_RATE_LIMIT_RESET = "API_RATE_LIMIT_RESET";

    /**
     * Client Agent, 查看：{@link ClientAgentEnum}
     */
    public static final String CLIENT_AGENT = "CLIENT_AGENT";
    /**
     * 应用系统
     */
    public static final String APP_SYSTEM = "APP_SYSTEM";

}
