package top.cjf_rb.oem.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cjf_rb.api.HelloServiceApiService;


/**\
 */
@Slf4j
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {
    @DubboReference
    private HelloServiceApiService helloService;

    // test
    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return helloService.sayHello(name);
    }
}
