package top.cjf_rb.gateway.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.cjf_rb.gateway.exception.ErrorCode;

/**
 * @Author Zoe
 * @create 2024/3/19
 */
@Getter
@AllArgsConstructor
public enum GatewayErrorCodeEnum implements ErrorCode {

    /**
     * 未知错误
     */
    UNKNOWN_ERROR("系统忙不过来了, 稍后再试试"), GATEWAY_ERROR("系统忙不过来了, 稍后再试试"),
    /**
     * 鉴权相关
     */
    NOT_LOGGED_IN("未登录"), LOGIN_EXPIRED("登录已过期, 请重新登录"), LOGIN_INCORRECT("登录已失效, 请重新登录");

    private final String msg;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
