package top.cjf_rb.security.web.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 被挤下线异常
 */
public class SqueezedOfflineException extends AuthenticationException {

    public SqueezedOfflineException(String msg, Throwable t) {
        super(msg, t);
    }

    public SqueezedOfflineException(String msg) {
        super(msg);
    }

}
