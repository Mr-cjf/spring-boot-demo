package top.cjf_rb.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import top.cjf_rb.api.HelloServiceApiService;
@Service
@DubboService
public class HelloServiceApiServiceImpl implements HelloServiceApiService {

    private final top.cjf_rb.provider.HelloServiceApi delegate;

    public HelloServiceApiServiceImpl(top.cjf_rb.provider.HelloServiceApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public String sayHello(String arg0) {
        return delegate.sayHello(arg0);
    }

    @Override
    public String sayHello2(String arg0) {
        return delegate.sayHello2(arg0);
    }

    @Override
    public String sayHello3(String arg0) {
        return delegate.sayHello3(arg0);
    }

    @Override
    public byte[] sayHello4(String arg0) {
        return delegate.sayHello4(arg0);
    }

    @Override
    public byte[] sayHello5(String arg0) {
        return delegate.sayHello5(arg0);
    }

    @Override
    public byte[] sayHello6(String arg0) {
        return delegate.sayHello6(arg0);
    }

    @Override
    public String sayHello7(String arg0) {
        return delegate.sayHello7(arg0);
    }

}