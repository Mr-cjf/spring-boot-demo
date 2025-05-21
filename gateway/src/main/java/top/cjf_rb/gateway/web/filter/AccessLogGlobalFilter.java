package top.cjf_rb.gateway.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.cjf_rb.gateway.constants.AppHeaderConst;

import java.util.Objects;

/**
 * 访问日志
 */
@Slf4j
@Component
public final class AccessLogGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        String rawPath = request.getURI().getRawPath();
        String traceId = request.getHeaders().getFirst(AppHeaderConst.TRACE_ID);

        log.info("[{}] Request: {} {}", traceId, method, rawPath);

        ServerHttpResponse response = exchange.getResponse();
        return chain.filter(exchange).then(Mono.fromRunnable(() -> log.info("[{}] Response Status: {}", traceId,
                Objects.requireNonNull(response.getStatusCode()).value())));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

}
