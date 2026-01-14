package top.cjf_rb.gateway.pojo.prop;

import lombok.Data;

import java.time.Duration;

/**
 * @author cjf
 * @since 1.0
 */
@Data
public class JwtProperties {

    /**
     * jwt密钥
     */
    private String secret;
    /**
     * 有效期
     */
    private Duration expires;

}
