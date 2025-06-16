package top.cjf_rb.core.pojo.prop;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.AppHeaderConst;

/**
 * 模块类型配置
 */
@Data
@ConfigurationProperties(prefix = "app.exception.handler")
@Component
public class AppExceptionHandler {
    private String type = AppHeaderConst.APP_SYSTEM;
}
