package top.cjf_rb.core.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import top.cjf_rb.core.util.Nones;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 记录原请求的数据

 @author cjf
 @since 1.0 */
@Slf4j
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        if (log.isInfoEnabled()) {
            log.info(">> (外部调用)Request Url: {} {}", request.getMethod(), request.getURI());
            log.info(">> (外部调用)Request Headers: {}", request.getHeaders());
            if (body.length > 0) {
                log.info(">> (外部调用)Request Body: {}", new String(body, StandardCharsets.UTF_8));
            }
        }

        // 执行调用
        long start = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long end = System.currentTimeMillis();

        if (log.isInfoEnabled()) {
            log.info(">> (外部调用)Response Status: {}", response.getStatusCode());
            log.info(">> (外部调用)Response Headers: {}", response.getHeaders());
            String responseBody = this.bodyToString(response.getBody());
            if (Nones.nonBlank(responseBody)) {
                log.info(">> (外部调用)Response Body: {}", responseBody);
            }
        }

        long elapsedTime = end - start;
        if (elapsedTime > 1000) {
            log.warn(">> (外部调用)Elapsed Time: {}ms", elapsedTime);
        }

        return response;
    }

    private String bodyToString(InputStream inputStream) throws IOException {
        try (inputStream) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

}
