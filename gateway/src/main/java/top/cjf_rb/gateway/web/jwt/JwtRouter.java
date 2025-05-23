package top.cjf_rb.gateway.web.jwt;

import lombok.RequiredArgsConstructor;
import top.cjf_rb.gateway.exception.GatewayException;

import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 *     <h2>
 *         多权限路由分发者
 *     </h2>
 * </pre>
 *
 */
@RequiredArgsConstructor
public class JwtRouter {

    private final Map<String, JwtTokens> router;

    /**
     * 获取该客户端的配置
     *
     * @param clientAgent 系统客户端
     * @return jwt配置参数
     */
    public JwtTokens distribute(String clientAgent) {
        return Optional.ofNullable(router.get(clientAgent))
            .orElseThrow(() -> new GatewayException("undefined client agent"));
    }

}
