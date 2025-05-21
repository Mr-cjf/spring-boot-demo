package top.cjf_rb.gateway.exception;

/**
 * 找不到凭据异常
 * 
 * @author lty
 * @since 1.0
 */
public class CredentialsNotFoundException extends GatewayException {

    public CredentialsNotFoundException(String msg) {
        super(msg);
    }

    public CredentialsNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
