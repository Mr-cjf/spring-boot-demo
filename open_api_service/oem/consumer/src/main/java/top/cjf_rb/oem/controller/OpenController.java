package top.cjf_rb.oem.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cjf_rb.api_provider.api.service.HelloService;


/**
 * @author lty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {
    @DubboReference
    private final HelloService helloService;


    // test
    @RequestMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return helloService.sayHello7(name);
    }
}
