package top.cjf_rb.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.cjf_rb.core.constant.AppServerConst;
import top.cjf_rb.core.exception.*;
import top.cjf_rb.core.pojo.prop.AppExceptionHandler;
import top.cjf_rb.core.pojo.vo.ErrorVo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
@Configuration
public class AppGlobalExceptionHandler {


    // 定义异常类型与对应构造函数的映射
    private static final Map<String, Function<Exception, AppException>> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(AppServerConst.BUSINESS,
                     ex -> AppException.handleSubClassException(ex, BusinessException.class, BusinessException::new));
        HANDLERS.put(AppServerConst.BASIC,
                     ex -> AppException.handleSubClassException(ex, BasicException.class, BasicException::new));
        HANDLERS.put(AppServerConst.INTERNAL,
                     ex -> AppException.handleSubClassException(ex, InternalServerException.class,
                                                                InternalServerException::new));
        HANDLERS.put(AppServerConst.THIRD_PARTY,
                     ex -> AppException.handleSubClassException(ex, ThirdPartyServerException.class,
                                                                ThirdPartyServerException::new));
        HANDLERS.put(AppServerConst.DEFAULT,
                     ex -> AppException.handleSubClassException(ex, AppException.class, AppException::new));
    }

    private final AppExceptionHandler appExceptionHandler;

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex) {
        AppException appException;
        switch (ex) {
            case BusinessException ignored -> appException = AppException.handleSubClassException(ex,
                                                                                                  BusinessException.class,
                                                                                                  BusinessException::new);
            case BasicException ignored -> appException = AppException.handleSubClassException(ex, BasicException.class,
                                                                                               BasicException::new);
            case ThirdPartyServerException ignored -> appException = AppException.handleSubClassException(ex,
                                                                                                          ThirdPartyServerException.class,
                                                                                                          ThirdPartyServerException::new);
            case InternalServerException ignored -> appException = AppException.handleSubClassException(ex,
                                                                                                        InternalServerException.class,
                                                                                                        InternalServerException::new);
            default -> {
                appException = HANDLERS.getOrDefault(appExceptionHandler.getType(),
                                                     HANDLERS.get(AppServerConst.DEFAULT))
                                       .apply(ex);
                log.error("全局未处理异常处理器捕获到异常：", appException);
            }
        }
        return ErrorVo.of(appException.getErrorCodeEnum()
                                      .getCode(), appException.getErrorCodeEnum()
                                                              .getMsg());
    }


}
