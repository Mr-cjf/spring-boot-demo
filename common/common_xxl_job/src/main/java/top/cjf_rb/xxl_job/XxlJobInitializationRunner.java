package top.cjf_rb.xxl_job;

import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.cjf_rb.xxl_job.client.XxlJobClient;

/**
 @author lty */
@Order(1)
@Component
public class XxlJobInitializationRunner implements ApplicationRunner {

    @Resource
    private XxlJobClient xxlJobClient;

    @Override
    public void run(ApplicationArguments args) {
        // 初始化 xxl-job 执行器
        this.xxlJobClient.initJobGroup();
    }

}
