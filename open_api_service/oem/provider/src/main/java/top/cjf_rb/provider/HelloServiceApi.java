package top.cjf_rb.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import top.cjf_rb.annotation.AutoDubboService;

@DubboService
@RequiredArgsConstructor
@AutoDubboService
public class HelloServiceApi {


    @Autowired
    private ObjectMapper ObjectMapper1;

    @Resource
    private ObjectMapper ObjectMapper2;

    public String sayHello(String name) {
        // 返回一个字符串，内容为"Hello "加上传入的name参数
        System.out.println("Hello " + name);
        if (name != null) {
            throw new RuntimeException("测试异常");
        }

        return "Hello " + name;
    }

    public String sayHello2(String name) {
        return "Hello " + name;
    }

    public String sayHello3(String name) {
        return "Hello " + name;
    }

    // 测试objectMapper
    public byte[] sayHello4(String name) {
        try {
            return ObjectMapper1.writeValueAsString(name).getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 测试ObjectMapper1
    public byte[] sayHello5(String name) {
        try {
            return ObjectMapper1.writeValueAsString(name).getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 测试ObjectMapper2
    public byte[] sayHello6(String name) {
        try {
            return ObjectMapper2.writeValueAsString(name).getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String sayHello7(String name) {
        return "Hello " + name;
    }
}
