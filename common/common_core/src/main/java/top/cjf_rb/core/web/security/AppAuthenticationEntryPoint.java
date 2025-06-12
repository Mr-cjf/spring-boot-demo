package top.cjf_rb.core.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.pojo.vo.ErrorVo;
import top.cjf_rb.core.web.security.exception.InvalidCaptchaException;
import top.cjf_rb.core.web.security.exception.SqueezedOfflineException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 身份认证错误
 */
@Slf4j
@RequiredArgsConstructor
public final class AppAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorCodeEnum expCodeEnum;
        switch (authException) {
            case CredentialsExpiredException ignored -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                expCodeEnum = ErrorCodeEnum.LOGIN_EXPIRED;
            }
            case AccountExpiredException ignored -> expCodeEnum = ErrorCodeEnum.ACCOUNT_EXPIRED;
            case BadCredentialsException ignored -> expCodeEnum = ErrorCodeEnum.LOGIN_INCORRECT;
            case DisabledException ignored -> expCodeEnum = ErrorCodeEnum.ACCOUNT_DISABLED;
            case LockedException ignored -> expCodeEnum = ErrorCodeEnum.ACCOUNT_LOCKED;
            case AuthenticationCredentialsNotFoundException ignored -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                expCodeEnum = ErrorCodeEnum.NOT_LOGGED_IN;
            }
            case SqueezedOfflineException ignored -> expCodeEnum = ErrorCodeEnum.SQUEEZED_OFFLINE;
            case InternalAuthenticationServiceException ignored -> {
                log.error("身份验证内部服务异常", authException);
                expCodeEnum = ErrorCodeEnum.AUTHENTICATION_INCORRECT;
            }
            case AuthenticationServiceException ignored -> {
                log.error("身份验证服务异常", authException);
                expCodeEnum = ErrorCodeEnum.AUTHENTICATION_INCORRECT;
            }
            case InvalidCaptchaException ignored -> expCodeEnum = ErrorCodeEnum.BAD_CAPTCHA;
            case null, default -> {
                log.error("身份验证未知异常", authException);
                expCodeEnum = ErrorCodeEnum.AUTHENTICATION_INCORRECT;
            }
        }

        ErrorVo errorVo = ErrorVo.of(expCodeEnum.getCode(), expCodeEnum.getMsg());
        response.getWriter().write(objectMapper.writeValueAsString(errorVo));
    }

}