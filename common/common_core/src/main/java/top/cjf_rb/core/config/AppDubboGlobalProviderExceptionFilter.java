package top.cjf_rb.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.dubbo.rpc.support.RpcUtils;
import top.cjf_rb.core.constant.AppServerConst;
import top.cjf_rb.core.context.AppContextHolder;
import top.cjf_rb.core.exception.*;
import top.cjf_rb.core.pojo.prop.AppExceptionHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.apache.dubbo.common.constants.LoggerCodeConstants.CONFIG_FILTER_VALIDATION_EXCEPTION;

/**
 * Dubbo 全局异常拦截器
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class AppDubboGlobalProviderExceptionFilter implements Filter, BaseFilter.Listener {

    // 定义异常类型与对应构造函数的映射
    private static final Map<String, Function<Throwable, AppException>> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(AppServerConst.BUSINESS,
                     t -> AppException.handleSubClassException(t, BusinessException.class, BusinessException::new));
        HANDLERS.put(AppServerConst.BASIC,
                     t -> AppException.handleSubClassException(t, BasicException.class, BasicException::new));
        HANDLERS.put(AppServerConst.INTERNAL,
                     t -> AppException.handleSubClassException(t, InternalServerException.class,
                                                               InternalServerException::new));
        HANDLERS.put(AppServerConst.THIRD_PARTY,
                     t -> AppException.handleSubClassException(t, ThirdPartyServerException.class,
                                                               ThirdPartyServerException::new));
        HANDLERS.put(AppServerConst.DEFAULT,
                     t -> AppException.handleSubClassException(t, AppException.class, AppException::new));
    }

    private AppExceptionHandler exceptionHandler;

    public AppDubboGlobalProviderExceptionFilter() {
        exceptionHandler = AppContextHolder.getBean(AppExceptionHandler.class);
    }

    /**
     * Always call invoker.invoke() in the implementation to hand over the request to the next filter node.
     *
     * @param invoker    the invoker
     * @param invocation the invocation
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (exceptionHandler == null) {
            exceptionHandler = new AppExceptionHandler();
        }
        AppException appException;
        Result invoke = invoker.invoke(invocation);

        Throwable t = invoke.getException();
        if (invoke.hasException() && !(t instanceof RpcException)) {
            // 手动处理异常
            switch (t) {
                case BusinessException ignored -> appException = AppException.handleSubClassException(t,
                                                                                                      BusinessException.class,
                                                                                                      BusinessException::new);
                case BasicException ignored -> appException = AppException.handleSubClassException(t,
                                                                                                   BasicException.class,
                                                                                                   BasicException::new);
                case ThirdPartyServerException ignored -> appException = AppException.handleSubClassException(t,
                                                                                                              ThirdPartyServerException.class,
                                                                                                              ThirdPartyServerException::new);
                case InternalServerException ignored -> appException = AppException.handleSubClassException(t,
                                                                                                            InternalServerException.class,
                                                                                                            InternalServerException::new);
                default -> appException = HANDLERS.getOrDefault(exceptionHandler.getType(),
                                                                HANDLERS.get(AppServerConst.DEFAULT))
                                                  .apply(t);
            }
            log.info("dubbo全局异常处理器捕获到异常：", appException);

            invoke.setException(appException);
        }
        return invoke;
    }

    /**
     * 该方法只会在远程 rpc 执行成功时调用，也就是说，远程接收到的服务
     * 请求和结果（正常或异常）成功返回.
     *
     * @param appResponse 服务响应
     * @param invoker     调用者
     * @param invocation  调用信息
     */
    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException() && GenericService.class != invoker.getInterface()) {
            try {
                Throwable exception = appResponse.getException();

                // 如果是 checked 异常，则直接抛出
                if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                    return;
                }
                // 如果签名中出现异常，则直接引发
                try {
                    Method method = invoker.getInterface()
                                           .getMethod(RpcUtils.getMethodName(invocation),
                                                      invocation.getParameterTypes());
                    Class<?>[] exceptionClasses = method.getExceptionTypes();
                    for (Class<?> exceptionClass : exceptionClasses) {
                        if (exception.getClass()
                                     .equals(exceptionClass)) {
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    return;
                }

                // 如果异常类和接口类在同一个 jar 文件中，则直接引发。
                String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
                    return;
                }
                // 如果是 JDK 异常，则直接抛出
                String className = exception.getClass()
                                            .getName();
                if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith(
                        "jakarta.")) {
                    return;
                }
                // 如果是 dubbo 异常就直接扔
                if (exception instanceof RpcException) {
                    return;
                }

                // 如果是 app 预知异常就直接扔
                if (exception instanceof AppException) {
                    return;
                }


                // 否则，使用 RuntimeException 包装并返回给客户端
                appResponse.setException(new RuntimeException(StringUtils.toString(exception)));
            } catch (Throwable e) {
                log.warn(
                        CONFIG_FILTER_VALIDATION_EXCEPTION + " " + "Fail to ExceptionFilter when called by " + RpcContext.getServiceContext()
                                                                                                                         .getRemoteHost() + ". service: " + invoker.getInterface()
                                                                                                                                                                   .getName() + ", method: " + RpcUtils.getMethodName(
                                invocation) + ", exception: " + e.getClass()
                                                                 .getName() + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * 在检测到框架异常时，将调用此方法，例如，在筛选器中引发的 TimeoutException、NetworkException 异常等.
     *
     * @param t          抛出的异常
     * @param invoker    调用者
     * @param invocation 调用信息
     */
    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
    }
}