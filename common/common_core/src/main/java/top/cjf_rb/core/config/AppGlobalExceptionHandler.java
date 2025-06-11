package top.cjf_rb.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.cjf_rb.core.constant.AppServerConst;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.*;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class AppGlobalExceptionHandler {

    @Value("${app.exception.handler.type}")
    public String exceptionHandlerType;

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex) {
        return switch (exceptionHandlerType) {
            case AppServerConst.BUSINESS -> businessHandleException(ex);
            case AppServerConst.BASIC -> basicHandleException(ex);
            case AppServerConst.INTERNAL -> internalHandleException(ex);
            case AppServerConst.THIRD_PARTY -> thirdPartyHandleException(ex);
            default -> appHandleException(ex);
        };
    }

    public AppException appHandleException(Exception ex) {
        if (ex instanceof AppException appException) {
            // 已提前预知异常，直接返回
            return appException;
        } else {
            // 非业务异常统一包装成 内部错误
            return new AppException(ErrorCodeEnum.UNKNOWN_ERROR, ex.getMessage(), ex.getCause());
        }
    }

    /**
     * 捕获所有异常
     */
    public BusinessException businessHandleException(Exception ex) {
        if (ex instanceof BusinessException businessException) {
            // 已提前预知异常，直接返回
            return businessException;
        } else if (ex instanceof AppException appException) {
            if (appException.getData() != null) {
                return new BusinessException(appException.getErrorCodeEnum(), appException.getDescription(),
                        appException.getData());
            } else {
                if (appException.getDescription() != null) {
                    return new BusinessException(appException.getErrorCodeEnum(), appException.getDescription(),
                            appException.getCause());
                } else {
                    return new BusinessException(appException.getErrorCodeEnum(), appException.getCause());
                }
            }
        } else {
            // 非业务异常统一包装成 内部错误
            return new BusinessException(ErrorCodeEnum.UNKNOWN_ERROR, ex.getMessage(), ex.getCause());
        }
    }

    /**
     * 捕获所有异常
     */
    public BasicException basicHandleException(Exception ex) {
        if (ex instanceof BasicException basicException) {
            // 已提前预知异常，直接返回
            return basicException;
        } else if (ex instanceof AppException appException) {
            if (appException.getData() != null) {
                return new BasicException(appException.getErrorCodeEnum(), appException.getDescription(),
                        appException.getData());
            } else {
                if (appException.getDescription() != null) {
                    return new BasicException(appException.getErrorCodeEnum(), appException.getDescription(),
                            appException.getCause());
                } else {
                    return new BasicException(appException.getErrorCodeEnum(), appException.getCause());
                }
            }
        } else {
            // 非业务异常统一包装成 内部错误
            return new BasicException(ErrorCodeEnum.UNKNOWN_ERROR, ex.getMessage(), ex.getCause());
        }
    }

    /**
     * 捕获所有异常
     */
    public InternalServerException internalHandleException(Exception ex) {
        if (ex instanceof InternalServerException internalServerException) {
            // 已提前预知异常，直接返回
            return internalServerException;
        } else if (ex instanceof AppException appException) {
            if (appException.getData() != null) {
                return new InternalServerException(appException.getErrorCodeEnum(), appException.getDescription(),
                        appException.getData());
            } else {
                if (appException.getDescription() != null) {
                    return new InternalServerException(appException.getErrorCodeEnum(), appException.getDescription()
                            , appException.getCause());
                } else {
                    return new InternalServerException(appException.getErrorCodeEnum(), appException.getCause());
                }
            }
        } else {
            // 非业务异常统一包装成 内部错误
            return new InternalServerException(ErrorCodeEnum.UNKNOWN_ERROR, ex.getMessage(), ex.getCause());
        }
    }

    /**
     * 捕获所有异常
     */
    public ThirdPartyServerException thirdPartyHandleException(Exception ex) {
        if (ex instanceof ThirdPartyServerException thirdPartyServerException) {
            // 已提前预知异常，直接返回
            return thirdPartyServerException;
        } else if (ex instanceof AppException appException) {
            if (appException.getData() != null) {
                return new ThirdPartyServerException(appException.getErrorCodeEnum(), appException.getDescription(),
                        appException.getData());
            } else {
                if (appException.getDescription() != null) {
                    return new ThirdPartyServerException(appException.getErrorCodeEnum(),
                            appException.getDescription(), appException.getCause());
                } else {
                    return new ThirdPartyServerException(appException.getErrorCodeEnum(), appException.getCause());
                }
            }
        } else {
            // 非业务异常统一包装成 内部错误
            return new ThirdPartyServerException(ErrorCodeEnum.UNKNOWN_ERROR, ex.getCause());
        }
    }

}
