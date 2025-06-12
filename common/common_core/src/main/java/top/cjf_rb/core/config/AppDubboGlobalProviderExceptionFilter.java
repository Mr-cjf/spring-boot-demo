package top.cjf_rb.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.AppServerConst;
import top.cjf_rb.core.exception.*;

/**
 * Dubbo 全局异常拦截器
 */
@Activate(group = {"provider"}) // 仅用于 provider
@Slf4j
@Component
public class AppDubboGlobalProviderExceptionFilter implements Filter {
    @Value("${app.exception.handler.type}")
    public String exceptionHandlerType;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            return invoker.invoke(invocation);
        } catch (Throwable t) {

            AppException appException;
            switch (exceptionHandlerType) {
                case AppServerConst.BUSINESS ->
                        appException = AppException.handleSubClassException(t, BusinessException.class,
                                BusinessException::new);
                case AppServerConst.BASIC ->
                        appException = AppException.handleSubClassException(t, BasicException.class,
                                BasicException::new);
                case AppServerConst.INTERNAL ->
                        appException = AppException.handleSubClassException(t, InternalServerException.class,
                                InternalServerException::new);
                case AppServerConst.THIRD_PARTY ->
                        appException = AppException.handleSubClassException(t, ThirdPartyServerException.class,
                                ThirdPartyServerException::new);
                default ->
                        appException = AppException.handleSubClassException(t, AppException.class, AppException::new);
            }
            log.error("全局异常处理器捕获到异常1：", appException);
            // 修复点：改为抛出 RpcException，框架会自动包装成 Result
            throw new RpcException(appException.getMessage(), appException);
        }
    }


}