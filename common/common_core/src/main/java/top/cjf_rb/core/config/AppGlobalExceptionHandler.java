package top.cjf_rb.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;
import top.cjf_rb.core.pojo.vo.ErrorVo;

import java.util.Objects;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
@Configuration
public class AppGlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex) {
        log.error("全局未处理异常处理器捕获到异常：", ex);
        AppException appException;
        if (Objects.requireNonNull(ex) instanceof AppException appException1) {
            appException = appException1;
        } else {
            appException = new AppException(ErrorCodeEnum.UNKNOWN_ERROR, ex.getMessage());

        }
        return ErrorVo.of(appException.getErrorCodeEnum()
                                      .getCode(), appException.getErrorCodeEnum()
                                                              .getMsg());
    }


}
