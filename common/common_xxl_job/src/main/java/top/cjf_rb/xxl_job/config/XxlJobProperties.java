package top.cjf_rb.xxl_job.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 xxl-job 配置文件

 @author lty
 @since 1.0 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    @NotNull
    private String adminUsername;
    @NotNull
    private String adminPassword;
    @NotNull
    private String adminAddresses;
    @NotNull
    private String accessToken;

    private Duration connectTimeout = Duration.ofSeconds(5L);
    private Duration connectRequestTimeout = Duration.ofSeconds(5L);

    @NotNull
    private Executor executor;

    @Data
    public static class Executor {

        private String appName;

        private String address;

        private String ip;

        private int port;

        private String logPath;

        private int logRetentionDays;

    }

}
