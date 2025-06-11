package top.cjf_rb.core.constant;

/**
 * 系统配置相关的枚举类，用于统一管理系统中常用的常量值。
 * 每个枚举项对应一个系统级别的配置项，便于在代码中引用和维护。
 *
 */
public final class AppSystemConst {
    /**
     * 系统默认的序列化ID
     */
    public static final long SERIAL_VERSION_UID = 1024L;
    /**
     * 超级管理员的固定ID
     */
    public static final Long SUPER_ADMIN_USERID = 1024L;

    /**
     * 约定成功字符串
     */
    public static final String SUCCEEDED = "succeeded";
    /**
     * 约定失败字符串
     */
    public static final String FAILED = "failed";

    /**
     * 日志链路 ID 标识
     */
    public static final String TRACE_ID = "TRACE_ID";

    /**
     * 日期时间格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}