package cn.huahanedu.frame.captcha.config;

import cn.huahanedu.frame.captcha.pojo.prop.AppCaptchaProperties;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.cjf_rb.redis.context.type.accessor.PrefixCacheAccessor;
import top.cjf_rb.redis.context.type.accessor.RedisPrefixAccessor;

/**
 验证码配置
 */
@Configuration
public class AppCaptchaConfig {

    @Resource
    private AppCaptchaProperties captchaProperties;

    @Bean
    public PrefixCacheAccessor<String> imageCaptchaAccessor() {
        String keyPrefix = "app:captcha:image:";
        return new RedisPrefixAccessor<>(keyPrefix, captchaProperties.getExpire());
    }

    @Bean
    public SpecCaptcha alphanumericProducer() {
        return new SpecCaptcha(160, 60, captchaProperties.getAlphanumericSize());
    }

    @Bean
    public ArithmeticCaptcha arithmeticProducer() {
        return new ArithmeticCaptcha(160, 60);
    }
}
