package top.cjf_rb.security.web.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 无效验证码
 */
public class InvalidCaptchaException extends AuthenticationException {

    public InvalidCaptchaException(String msg, Throwable t) {
        super(msg, t);
    }

    public InvalidCaptchaException(String msg) {
        super(msg);
    }

}

