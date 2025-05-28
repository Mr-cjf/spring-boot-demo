package top.cjf_rb.oem.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cjf_rb.api_provider.api.service.HelloServiceApi;


/**
 * @author lty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {

    private final HelloServiceApi helloService;


    // test
    @RequestMapping("/hello")
    public String hello(String name) {
        return helloService.sayHello(name);
    }
}
