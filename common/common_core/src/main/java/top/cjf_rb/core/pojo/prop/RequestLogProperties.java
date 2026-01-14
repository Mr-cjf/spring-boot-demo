package top.cjf_rb.core.pojo.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.AppHeaderConst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 日志记录

 @author lty
 @since 1.0 */
@Data
@Component
@ConfigurationProperties(prefix = "app.log.request")
public class RequestLogProperties {
    /**
     不记录日志的URL
     */
    private Set<String> ignorePaths = new HashSet<>(Set.of("/actuator/**"));
    /**
     要记录的请求头
     */
    private List<String> headerNames = new ArrayList<>(
            List.of(HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONTENT_TYPE, AppHeaderConst.CURRENT_USER));

}
