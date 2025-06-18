package top.cjf_rb.core.constant;

/**
 * 服务常量
 */
public final class AppCodeErrorTypeConst {

    /**
     * 业务异常(业务数据问题，请求参数问题)
     */
    public static final String BUSINESS_ERROR = "business_error";
    /**
     * 第三方服务异常(调用第三方服务问题)
     */
    public static final String THIRD_PARTY_ERROR = "third_party_error";
    /**
     * 自身代码异常(未定义错误，未知错误)
     */
    public static final String INTERNAL_ERROR = "internal_error";
    /**
     * 基础服务异常(mq异常,xxJob异常,中间件错误，数据库连接中断，xxJob连接中断)
     */
    public static final String BASIC_ERROR = "basic_error";
    /**
     * 无需处理错误(限流，验证码错误，下线错误)
     */
    public static final String IGNORE_ERROR = "ignore_error";
}
