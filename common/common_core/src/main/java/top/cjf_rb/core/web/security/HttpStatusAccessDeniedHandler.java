package top.cjf_rb.core.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 接口无权限时异常处理
 */
public class HttpStatusAccessDeniedHandler implements AccessDeniedHandler {

    private final HttpStatus httpStatus;

    public HttpStatusAccessDeniedHandler(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        response.setStatus(httpStatus.value());
    }
}

