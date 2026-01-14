package top.cjf_rb.core.web.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.cjf_rb.core.pojo.prop.RequestLogProperties;
import top.cjf_rb.core.util.Webs;
import top.cjf_rb.core.web.BufferingHttpServletRequest;
import top.cjf_rb.core.web.UrisMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 记录原请求的数据

 @author cjf
 @since 1.0 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RequestLogProperties requestLogProperties;

    /**
     无需记录日志的接口
     */
    private UrisMatcher urisMatcher;
    private List<String> headerNames;

    @PostConstruct
    private void init() {
        this.urisMatcher = new UrisMatcher(requestLogProperties.getIgnorePaths());
        this.headerNames = requestLogProperties.getHeaderNames();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 判断当前请求url是否在排除的列表中
        String path = request.getServletPath();
        if (urisMatcher.match(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        this.loggingUri(request);

        this.loggingHeaders(request);

        // 表单提交
        if (Webs.isFormRequest(request)) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!parameterMap.isEmpty()) {
                log.info(">> Request Form Content: {}", objectMapper.writeValueAsString(parameterMap));
            }

            this.doInternal(request, response, filterChain);
            return;
        }

        // 非表单提交
        BufferingHttpServletRequest bufferingHttpServletRequest = new BufferingHttpServletRequest(request);
        this.loggingBody(bufferingHttpServletRequest);

        this.doInternal(bufferingHttpServletRequest, response, filterChain);
    }

    /**
     执行并记录耗时
     */
    private void doInternal(HttpServletRequest request, HttpServletResponse response,
                            FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long end = System.currentTimeMillis();

        long elapsedTime = end - start;
        if (elapsedTime > 1000) {
            log.warn(">> Elapsed Time: {}ms", elapsedTime);
        }
    }

    /**
     记录uri
     */
    private void loggingUri(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)) {
            requestURI = requestURI + "?" + queryString;
        }

        log.info(">> Request Url: {} {}", request.getMethod(), requestURI);
    }

    /**
     记录请求头
     */
    private void loggingHeaders(HttpServletRequest request) {
        HashMap<String, Object> headers = new HashMap<>(8);

        for (String headerName : headerNames) {
            String headerValue = request.getHeader(headerName);
            if (Objects.nonNull(headerValue)) {
                headers.put(headerName, headerValue);
            }
        }

        if (!headers.isEmpty()) {
            log.info(">> Request Headers: {}", headers);
        }
    }

    /**
     记录请求体
     */
    private void loggingBody(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        // 字节流
        if (MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(contentType)) {
            log.info(">> Request Body is stream, not log!");
            return;
        }

        byte[] bytes = StreamUtils.copyToByteArray(request.getInputStream());
        int bodySize = bytes.length;
        // 64k 大小
        if (bodySize > 0 && bodySize <= 65536) {
            if (isSpecifyFile(bytes)) {
                log.info(">> Request Body is file, not log!");
            } else {
                log.info(">> Request Body: {}", new String(bytes, StandardCharsets.UTF_8));
            }
        } else if (bodySize > 65536) {
            log.info(">> Request Body is too large, not log!");
        }
    }

    /**
     是否是文件

     @param bytes 字节
     */
    private boolean isSpecifyFile(byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }

        // JPEG
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
            return true;
        }
        // PNG
        else if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E &&
                bytes[3] == (byte) 0x47) {
            return true;
        }
        // GIF
        else if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49 && bytes[2] == (byte) 0x46 &&
                bytes[3] == (byte) 0x38) {
            return true;
        }
        // BMP
        else if (bytes[0] == (byte) 0x42 && bytes[1] == (byte) 0x4D) {
            return true;
        }
        // .rar
        else if (bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x61 && bytes[2] == (byte) 0x72 &&
                bytes[3] == (byte) 0x21) {
            return true;
        }
        // .doc or .xls or .ppt
        else if (bytes[0] == (byte) 0xD0 && bytes[1] == (byte) 0xCF && bytes[2] == (byte) 0x11 &&
                bytes[3] == (byte) 0xE0) {
            return true;
        }
        // .pdf
        else if (bytes[0] == (byte) 0x25 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x44 &&
                bytes[3] == (byte) 0x46) {
            return true;
        }
        // .docx or .xlsx or .zip
        else {
            return bytes[0] == (byte) 0x50 && bytes[1] == (byte) 0x4B && bytes[2] == (byte) 0x03 &&
                    bytes[3] == (byte) 0x04;
        }

    }

}
