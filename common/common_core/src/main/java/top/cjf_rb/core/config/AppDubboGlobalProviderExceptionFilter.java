package top.cjf_rb.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import org.apache.dubbo.rpc.support.RpcUtils;
import top.cjf_rb.core.exception.AppException;

import static org.apache.dubbo.common.constants.LoggerCodeConstants.CONFIG_FILTER_VALIDATION_EXCEPTION;

/**
 * Dubbo 全局异常拦截器
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER, order = -10000)
public class AppDubboGlobalProviderExceptionFilter extends ExceptionFilter {


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
        try {
            Throwable exception = appResponse.getException();
            if (isAppException(exception)) {
                return;
            }
            super.onResponse(appResponse, invoker, invocation);
        } catch (Throwable e) {
            logExceptionWarning(e, invoker, invocation);
        }
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

        log.warn(CONFIG_FILTER_VALIDATION_EXCEPTION + " Fail to ExceptionFilter when called by {}. service: {}, " +
                         "method: {}, exception: {}: {}",
                remoteHost, serviceName, methodName, e.getClass()
                                                      .getName(), e.getMessage(), e);
    }

}