package top.cjf_rb.gateway.config;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.cjf_rb.gateway.pojo.prop.AppGatewayProperties;
import top.cjf_rb.gateway.web.UrisMatcher;
import top.cjf_rb.gateway.web.jwt.JwtRouter;
import top.cjf_rb.gateway.web.jwt.JwtTokens;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lty
 * @since 1.0
 */
@Slf4j
@Configuration
@Import(AppGatewayProperties.class)
public class GatewayConfig {

    @Resource
    private AppGatewayProperties appGatewayProperties;

    @Bean
    public UrisMatcher whitelistMatcher() {
        return new UrisMatcher(appGatewayProperties.getWhitelist().getUris());
    }

    @Bean
    public JwtRouter jwtRouter() {
        Map<String, JwtTokens> router = new HashMap<>();

        appGatewayProperties.getJwts().forEach((key, value) -> router.put(key, new JwtTokens(value)));

        return new JwtRouter(router);
    }
}
