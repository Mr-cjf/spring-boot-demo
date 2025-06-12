package top.cjf_rb.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.cjf_rb.core.constant.AppServerConst;
import top.cjf_rb.core.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class AppGlobalExceptionHandler {

    @Value("${app.exception.handler.type}")
    public String exceptionHandlerType;

    // 定义异常类型与对应构造函数的映射
    private static final Map<String, Function<Exception, AppException>> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(AppServerConst.BUSINESS, ex -> AppException.handleSubClassException(ex, BusinessException.class,
                BusinessException::new));
        HANDLERS.put(AppServerConst.BASIC, ex -> AppException.handleSubClassException(ex, BasicException.class,
                BasicException::new));
        HANDLERS.put(AppServerConst.INTERNAL, ex -> AppException.handleSubClassException(ex,
                InternalServerException.class, InternalServerException::new));
        HANDLERS.put(AppServerConst.THIRD_PARTY, ex -> AppException.handleSubClassException(ex,
                ThirdPartyServerException.class, ThirdPartyServerException::new));
        HANDLERS.put(AppServerConst.DEFAULT, ex -> AppException.handleSubClassException(ex, AppException.class,
                AppException::new));
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex) {
        AppException appException =
                HANDLERS.getOrDefault(exceptionHandlerType, HANDLERS.get(AppServerConst.DEFAULT)).apply(ex);
        log.error("全局异常处理器捕获到异常2：", appException);
        return appException;
    }


}
