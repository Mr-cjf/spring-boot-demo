package top.cjf_rb.core.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.cjf_rb.core.constant.AppHeaderConst;
import top.cjf_rb.core.constant.AppSystemConst;
import top.cjf_rb.core.util.Identifiers;
import top.cjf_rb.core.util.Nones;

import java.io.IOException;

/**
 链路标识

 @author lty
 @since 1.0 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {
    public static final String TRACE_ID = AppSystemConst.TRACE_ID;
    public static final String HEADER_TRACE_ID = AppHeaderConst.TRACE_ID;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER_TRACE_ID);
        if (Nones.isBlank(traceId)) {
            traceId = Identifiers.uuid();
        }

        try {
            // 设置请求ID
            MDC.put(TRACE_ID, traceId);

            response.setHeader(HEADER_TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }

    }

    @Override
    public void destroy() {
        MDC.clear();
    }

}
