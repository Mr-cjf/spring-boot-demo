package top.cjf_rb.security.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 日志记录
 */
@Data
@ConfigurationProperties(prefix = "app.security")
@Component
public class AppSecurityProperties {

    private String[] permitUris = {"/captcha/**", "/internal-open/**", "/open/**", "/*/open/**"};

    private String[] permitStaticUris = {"/static/**"};

    private String[] permitActuatorUris = {"/actuator/**"};

}
