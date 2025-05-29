package top.cjf_rb.api_provider.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.cjf_rb.aut_interface.annotation.AutoDubboService;

@AutoDubboService(interfaceName = "HelloService")
@Service
@RequiredArgsConstructor
public class HelloServiceApi {

    private final ObjectMapper objectMapper;

    @Autowired
    private ObjectMapper ObjectMapper1;

    @Resource
    private ObjectMapper ObjectMapper2;

    public String sayHello(String name) {
        // 返回一个字符串，内容为"Hello "加上传入的name参数
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
            return objectMapper.writeValueAsString(name).getBytes();
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
}
