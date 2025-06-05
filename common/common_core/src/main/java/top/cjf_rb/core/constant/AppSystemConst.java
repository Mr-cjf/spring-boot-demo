package top.cjf_rb.core.constant;

import lombok.Getter;

/**
 * 系统配置相关的枚举类，用于统一管理系统中常用的常量值。
 * 每个枚举项对应一个系统级别的配置项，便于在代码中引用和维护。
 *
 * @author lty
 */
@Getter
public enum AppSystemConst {
    /**
     * 序列化版本号，用于对象序列化和反序列化时的版本控制。
     */
    SERIAL_VERSION_UID(1024L),

    /**
     * 超级管理员用户ID，标识系统中具有最高权限的用户。
     */
    SUPER_ADMIN_USERID(1024L),

    /**
     * 成功状态标识，表示操作成功的结果。
     */
    SUCCEEDED("succeeded"),

    /**
     * 失败状态标识，表示操作失败的结果。
     */
    FAILED("failed"),

    /**
     * Trace ID，用于分布式系统中的链路追踪。
     */
    TRACE_ID("TRACE_ID"),

    /**
     * 日期格式，用于日期类型的格式化输出。
     */
    DATE_FORMAT("yyyy-MM-dd"),

    /**
     * 日期时间格式，用于日期时间类型的格式化输出。
     */
    DATETIME_FORMAT("yyyy-MM-dd HH:mm:ss");

    private final Object value;

    /**
     * 构造函数，初始化枚举项的值。
     *
     * @param value 枚举项的值
     */
    AppSystemConst(Object value) {
        this.value = value;
    }


    /**
     * 获取枚举项的值，并将其转换为目标类型。
     *
     * @param <T>   目标类型
     * @param clazz 目标类型的Class对象
     * @return 转换后的值
     * @throws ClassCastException 如果无法将值转换为目标类型，则抛出此异常
     */
    public <T> T getValueAs(Class<T> clazz) {
        return clazz.cast(this.value);
    }
}