package top.cjf_rb.oem.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityChainPrinter {

    private final List<SecurityFilterChain> filterChains;

    @PostConstruct
    public void printChains() {
        System.out.println("【安全链信息】当前注册的安全链数量：" + filterChains.size());
        for (int i = 0; i < filterChains.size(); i++) {
            SecurityFilterChain securityFilterChain = filterChains.get(i);
            List<Filter> filters = securityFilterChain.getFilters();
            for (Filter filter : filters) {
                System.out.printf("链 #%d: %s%n", i, filter);
            }
            System.out.printf("链 #%d: %s%n", i, filterChains.get(i));

        }
    }
}
