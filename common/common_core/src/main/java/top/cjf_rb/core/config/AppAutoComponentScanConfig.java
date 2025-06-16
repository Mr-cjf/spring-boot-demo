package top.cjf_rb.core.config;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("top.cjf_rb.**.**") // 要自动扫描的包路径
@DubboComponentScan(basePackageClasses = AppDubboGlobalProviderExceptionFilter.class)
public class AppAutoComponentScanConfig {
}
