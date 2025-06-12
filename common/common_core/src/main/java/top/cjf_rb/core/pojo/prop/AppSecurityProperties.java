package top.cjf_rb.core.pojo.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志记录
 */
@Data
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private String[] permitUris = {"/captcha/**", "/internal-open/**", "/open/**", "/*/open/**"};

    private String[] permitStaticUris = {"/static/**"};

    private String[] permitActuatorUris = {"/actuator/**"};

}
