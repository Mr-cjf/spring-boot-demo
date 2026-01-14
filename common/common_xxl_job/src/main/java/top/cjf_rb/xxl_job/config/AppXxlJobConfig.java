package top.cjf_rb.xxl_job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.cjf_rb.core.web.filter.HttpLoggingInterceptor;

import java.util.List;

/**
 xxl-job config

 @author lty */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Import(XxlJobProperties.class)
public class AppXxlJobConfig {

    private final XxlJobProperties xxlJobProperties;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("xxl-job config init.");
        XxlJobProperties.Executor executor = xxlJobProperties.getExecutor();
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());

        xxlJobSpringExecutor.setAppname(executor.getAppName());
        xxlJobSpringExecutor.setAddress(executor.getAddress());
        xxlJobSpringExecutor.setIp(executor.getIp());
        xxlJobSpringExecutor.setPort(executor.getPort());
        xxlJobSpringExecutor.setLogPath(executor.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(executor.getLogRetentionDays());

        return xxlJobSpringExecutor;
    }

    @Bean
    public RestTemplate xxlJobRestTemplate() {
        // 创建连接配置，将Duration转换为Timeout
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                                                            .setConnectTimeout(Timeout.ofMilliseconds(
                                                                    xxlJobProperties.getConnectTimeout()
                                                                                    .toMillis()))
                                                            .build();

        // 创建请求配置，将Duration转换为Timeout
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectionRequestTimeout(Timeout.ofMilliseconds(
                                                           xxlJobProperties.getConnectRequestTimeout()
                                                                           .toMillis()))
                                                   .build();

        // 创建连接管理器并设置连接配置
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        connectionManager.setConnectionConfig(connectionConfig);

        // 创建HttpClient
        org.apache.hc.client5.http.impl.classic.CloseableHttpClient httpClient = HttpClients.custom()
                                                                                            .setConnectionManager(
                                                                                                    connectionManager)
                                                                                            .setDefaultRequestConfig(
                                                                                                    requestConfig)
                                                                                            .build();

        // 使用自定义HttpClient创建HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        ClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);
        RestTemplate restTemplate = new RestTemplate(bufferingRequestFactory);

        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(new HttpLoggingInterceptor());

        return restTemplate;
    }

}