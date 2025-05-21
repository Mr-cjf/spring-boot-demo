package top.cjf_rb.gateway.web.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import top.cjf_rb.gateway.pojo.prop.JwtProperties;

import javax.crypto.SecretKey;

/**
 * JWT 操作类
 *
 * @author lty
 * @since 1.0
 */
@Setter
@RequiredArgsConstructor
public class JwtTokens {

    private final JwtProperties jwtProperties;

    /**
     * 解析jwt token
     *
     * @param token jwt token
     * @return Claims
     */
    public Claims parse(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));

        return Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
