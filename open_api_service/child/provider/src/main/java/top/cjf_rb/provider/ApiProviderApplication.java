package top.cjf_rb.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableDubbo
public class ApiProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiProviderApplication.class, args);
    }
}