package top.cjf_rb.oem;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import top.cjf_rb.aut_interface.annotation.AutoDubboService;

@SpringBootApplication
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(classes = {AutoDubboService.class})
)
@EnableDubbo
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}