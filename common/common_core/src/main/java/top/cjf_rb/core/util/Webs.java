package top.cjf_rb.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.cjf_rb.core.constant.AppHeaderConst;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 处理web请求响应的工具类

 @author cjf */
public final class Webs {
    public static final String TRACE_ID = AppHeaderConst.TRACE_ID;
    public static final String ACCESS_TOKEN = AppHeaderConst.ACCESS_TOKEN;
    public static final String FORWARDED_FOR = "X-Forwarded-For";
    public static final String FORWARDED_HOST = "X-Forwarded-Host";
    public static final String REAL_IP = "X-Real-IP";
    public static final String UNKNOWN_IP = "unknown";

    /**
     获取当前请求

     @return 如果是消息通讯的情况下, request是为null
     */
    public static Optional<HttpServletRequest> getRequest() {
        Optional<ServletRequestAttributes> optional = getServletRequestAttributes();
        return optional.map(ServletRequestAttributes::getRequest);
    }

    /**
     获取当前请求的响应

     @return 如果是消息通讯的情况下, response是为null
     */
    public static Optional<HttpServletResponse> getResponse() {
        Optional<ServletRequestAttributes> optional = getServletRequestAttributes();
        return optional.map(ServletRequestAttributes::getResponse);
    }

    /**
     从当前请求中获取{@link ServletRequestAttributes}
     */
    public static Optional<ServletRequestAttributes> getServletRequestAttributes() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(servletRequestAttributes);
    }

    /**
     获取当前请求中的请求头数据

     @param name 请求头
     @return 值
     */
    public static Optional<String> getHeader(String name) {
        Optional<HttpServletRequest> optional = getRequest();
        return optional.map(httpServletRequest -> httpServletRequest.getHeader(name));
    }

    /**
     获取当前请求中的Query参数

     @param name 参数名
     @return 值
     */
    public static Optional<String> getParameter(String name) {
        Optional<HttpServletRequest> optional = getRequest();
        return optional.map(httpServletRequest -> httpServletRequest.getParameter(name));
    }

    /**
     获取当前请求中的Query参数

     @param name 参数名
     @return 不可变的参数值集合
     */
    @NonNull
    public static List<String> getParameters(String name) {
        Optional<HttpServletRequest> optional = getRequest();
        if (optional.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parameterValues = optional.get()
                                           .getParameterValues(name);
        if (Objects.isNull(parameterValues)) {
            return Collections.emptyList();
        }

        return List.of(parameterValues);
    }

    /**
     获取当前请求的IP

     @return IP地址, 如果找不到IP, 则返回unknown
     */
    @NonNull
    public static String getIp() {
        Optional<HttpServletRequest> optional = getRequest();
        return optional.map(Webs::getIp)
                       .orElse(Webs.UNKNOWN_IP);
    }

    /**
     获取请求的真实IP

     @param request HttpServletRequest
     @return IP地址
     */
    public static String getIp(@NonNull HttpServletRequest request) {
        // X-Forwarded-For：Squid, HAProxy 服务代理
        String ip = request.getHeader(FORWARDED_FOR);
        String unknown = UNKNOWN_IP;

        // X-Real-IP：nginx 服务代理
        if (Nones.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader(REAL_IP);
        }

        // X-Forwarded-Host：Kong网关
        if (Nones.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader(FORWARDED_HOST);
        }

        if (Nones.nonBlank(ip) && !unknown.equalsIgnoreCase(ip)) {
            return ip.split(",")[0];
        }

        return request.getRemoteAddr();
    }

    /**
     <b>获取Access Token</b>

     @param request HttpServletRequest
     @return token
     */
    public static Optional<String> getAccessToken(@NonNull HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ACCESS_TOKEN));
    }

    /**
     获取TraceId

     @return 链路ID, 如果Request中没有链路ID, 则会生成ID
     */
    public static Optional<String> getTraceId() {
        Optional<HttpServletRequest> optional = getRequest();
        return optional.flatMap(Webs::getTraceId);

    }

    /**
     获取TraceId

     @param request {@link HttpServletRequest}
     @return TraceId
     */
    public static Optional<String> getTraceId(@NonNull HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(TRACE_ID));
    }

    /**
     获取TraceId

     @param request {@link HttpRequest}
     @return TraceId
     */
    public static Optional<String> getTraceId(@NonNull HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return Optional.ofNullable(headers.getFirst(TRACE_ID));
    }

    /**
     是否表单请求
     */
    public static boolean isFormRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && (contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE) ||
                contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)));
    }
}
