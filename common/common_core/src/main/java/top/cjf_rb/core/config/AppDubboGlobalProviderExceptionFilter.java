package top.cjf_rb.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.dubbo.rpc.support.RpcUtils;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static org.apache.dubbo.common.constants.LoggerCodeConstants.CONFIG_FILTER_VALIDATION_EXCEPTION;

/**
 * Dubbo 全局异常拦截器
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class AppDubboGlobalProviderExceptionFilter implements Filter, BaseFilter.Listener {

    /**
     * 调用下一个过滤器节点并处理异常
     *
     * @param invoker    调用者
     * @param invocation 调用信息
     * @return 调用结果
     * @throws RpcException RPC异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        AppException appException;
        Result invoke = invoker.invoke(invocation);

        Throwable t = invoke.getException(); if (invoke.hasException()) {
            // 手动处理异常
            if (Objects.requireNonNull(t) instanceof AppException ignored) {
                appException = ignored;
            } else {
                appException = new AppException(ErrorCodeEnum.UNKNOWN_ERROR, t.getMessage(), t);
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
        if (!appResponse.hasException() || GenericService.class == invoker.getInterface()) {
            return;
        }

        try {
            Throwable exception = appResponse.getException();
            if (shouldDirectlyThrow(exception, invoker, invocation)) {
                return;
            }
            // 否则包装为RuntimeException
            appResponse.setException(new RuntimeException(StringUtils.toString(exception)));
        } catch (Throwable e) {
            logExceptionWarning(e, invoker, invocation);
        }
    }

    /**
     * 检查是否应直接抛出原始异常
     *
     * @param exception  异常
     * @param invoker    调用者
     * @param invocation 调用信息
     * @return 是否应直接抛出原始异常
     */
    private boolean shouldDirectlyThrow(Throwable exception, Invoker<?> invoker, Invocation invocation) {
        // 检查异常是否为已检查异常
        return isCheckedException(exception) ||
                // 检查异常是否在方法签名中声明
                isDeclaredInMethodSignature(exception, invoker, invocation) ||
                // 检查异常是否与调用者位于同一代码库
                isSameCodebase(exception, invoker) ||
                // 检查异常是否为JDK或Dubbo异常
                isJdkOrDubboException(exception) ||
                // 检查异常是否为应用异常
                isAppException(exception);
    }

    /**
     * 检查是否为受检异常
     */
    private boolean isCheckedException(Throwable exception) {
        return !(exception instanceof RuntimeException) && (exception instanceof Exception);
    }

    /**
     * 检查是否在方法签名中声明
     */
    private boolean isDeclaredInMethodSignature(Throwable exception, Invoker<?> invoker, Invocation invocation) {
        try {
            Method method = invoker.getInterface()
                                   .getMethod(RpcUtils.getMethodName(invocation), invocation.getParameterTypes());
            return Arrays.stream(method.getExceptionTypes())
                         .anyMatch(ex -> ex.equals(exception.getClass()));
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * 检查类加载路径是否相同
     */
    private boolean isSameCodebase(Throwable exception, Invoker<?> invoker) {
        String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
        String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
        return serviceFile != null && serviceFile.equals(exceptionFile);
    }

    /**
     * 检查是否为JDK/Dubbo内置异常
     */
    private boolean isJdkOrDubboException(Throwable exception) {
        String className = exception.getClass()
                                    .getName();
        return className.startsWith("java.") || className.startsWith("javax.") || className.startsWith(
                "jakarta.") || exception instanceof RpcException;
    }

    /**
     * 检查是否为自定义应用异常
     */
    private boolean isAppException(Throwable exception) {
        return exception instanceof AppException;
    }

    /**
     * 统一记录异常日志
     */
    private void logExceptionWarning(Throwable e, Invoker<?> invoker, Invocation invocation) {
        String serviceName = invoker.getInterface()
                                    .getName();
        String methodName = RpcUtils.getMethodName(invocation);
        String remoteHost = RpcContext.getServiceContext()
                                      .getRemoteHost();

        log.warn(
                CONFIG_FILTER_VALIDATION_EXCEPTION + " Fail to ExceptionFilter when called by {}. service: {}, " +
                        "method: {}, exception: {}: {}",
                remoteHost, serviceName, methodName, e.getClass()
                                                      .getName(), e.getMessage(), e);
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