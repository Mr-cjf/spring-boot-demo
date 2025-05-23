package top.cjf_rb.gateway.web.filter;


import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.cjf_rb.gateway.constants.AppHeaderConst;
import top.cjf_rb.gateway.exception.CredentialsNotFoundException;
import top.cjf_rb.gateway.web.UrisMatcher;
import top.cjf_rb.gateway.web.jwt.JwtRouter;

import java.util.List;

/**
 * <pre>
 *     <h2>token处理的过滤器</h2>
 *     <li>配置名称为 - AccessToken （必须切掉前缀） </li>
 *     <li>与配置的GateFilter工厂可共用</li>
 * </pre>
 *
 */
@Slf4j
@Component
public class AccessTokenGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AccessTokenGatewayFilterFactory.Config> {

    public static final String TOKEN_NAME = "name";

    private final UrisMatcher urisMatcher;
    private final JwtRouter jwtRouter;

    public AccessTokenGatewayFilterFactory(JwtRouter jwtRouter, UrisMatcher urisMatcher) {
        super(Config.class);
        this.jwtRouter = jwtRouter;
        this.urisMatcher = urisMatcher;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of(TOKEN_NAME);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest rawRequest = exchange.getRequest();
            HttpHeaders headers = rawRequest.getHeaders();
            String rawPath = rawRequest.getPath().value();

            String accessToken = headers.getFirst(AppHeaderConst.ACCESS_TOKEN);
            String clientAgent = headers.getFirst(AppHeaderConst.CLIENT_AGENT);
            // 白名单内，若有凭证信息，则解析并携带，若解析异常，则跳过直接转发
            if (urisMatcher.match(rawPath)) {
                // 无 X-Access-Token 或 X-Client-Agent 请求头
                if (!(StringUtils.hasText(accessToken) && StringUtils.hasText(clientAgent))) {
                    // 直接转发
                    return chain.filter(exchange);
                }

                try {
                    Claims claims = jwtRouter.distribute(clientAgent).parse(accessToken);
                    ServerHttpRequest newRequest =
                        rawRequest.mutate().header(AppHeaderConst.CURRENT_USER, claims.getId())
                            .header(AppHeaderConst.CLIENT_AGENT, claims.getAudience().toArray(new String[0])).build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                } catch (Exception e) {
                    return chain.filter(exchange);
                }
            }

            // 白名单外，需要身份凭证访问
            if (!(StringUtils.hasText(accessToken) && StringUtils.hasText(clientAgent))) {
                throw new CredentialsNotFoundException("无法获取Access-Token 或 Client-Agent");
            }

            Claims claims = jwtRouter.distribute(clientAgent).parse(accessToken);

            // 组装新的请求
            ServerHttpRequest newRequest = rawRequest.mutate().header(AppHeaderConst.CURRENT_USER, claims.getId())
                .header(AppHeaderConst.CLIENT_AGENT, claims.getAudience().toArray(new String[0])).build();
            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }

    @Override
    public String name() {
        // 返回用于配置的名称
        return "AccessToken";
    }

    @Getter
    @Setter
    public static class Config {
        private String name;
    }
}
