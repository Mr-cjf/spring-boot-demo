package top.cjf_rb.gateway.pojo.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

/**
 * @author lty
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "app.gateway")
public class AppGatewayProperties {

    private Map<String, JwtProperties> jwts;

    private Whitelist whitelist;

    /**
     * 白名单
     */
    @Data
    public static class Whitelist {
        /**
         * 路径白名单
         */
        private Set<String> uris;

    }
}
