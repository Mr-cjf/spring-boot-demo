package top.cjf_rb.gateway.exception;

/**
 * 业务错误码枚举需要实现的接口
 *
 * @author lty
 * @since 1.0
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码, 统一固定为枚举名称
     */
    String getCode();

    /**
     * 获取错误描述信息
     *
     * @return 错误描述信息
     */
    String getMsg();

}
