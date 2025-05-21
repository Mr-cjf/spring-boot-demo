package top.cjf_rb.gateway.web.filter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.cjf_rb.gateway.constants.AppHeaderConst;
import top.cjf_rb.gateway.util.Identifiers;

/**
 * <pre>
 *     <h2>全局链路id生成工具</h2>
 * </pre>
 */
@Slf4j
@Component
public final class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 暂定使用UUID做生成规则
        String traceId = Identifiers.uuid();
        ServerHttpRequest newReq = exchange.getRequest().mutate().header(AppHeaderConst.TRACE_ID, traceId).build();

        exchange.getResponse().getHeaders().set(AppHeaderConst.TRACE_ID, traceId);
        return chain.filter(exchange.mutate().request(newReq).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
