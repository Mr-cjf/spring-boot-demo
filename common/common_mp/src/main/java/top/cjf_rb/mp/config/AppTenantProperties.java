package top.cjf_rb.mp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 @author cjf
 @since 1.0 */
@Data
@ConfigurationProperties(prefix = "app.tenant")
public class AppTenantProperties {

    /**
     租户列名
     */
    private final String tenantColumn = "tenant_id";
    /**
     是否启用多租户, 默认false
     */
    private boolean enabled;
    /**
     不使用租户方式的的表名
     */
    private List<String> ignoreTables;

}
