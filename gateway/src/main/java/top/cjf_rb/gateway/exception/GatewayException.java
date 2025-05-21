package top.cjf_rb.gateway.exception;

/**
 * <pre>
 * 网管层异常
 * </pre>
 * 
 * @Author Zoe
 * @create 2024/3/15
 */
public class GatewayException extends RuntimeException {

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}
