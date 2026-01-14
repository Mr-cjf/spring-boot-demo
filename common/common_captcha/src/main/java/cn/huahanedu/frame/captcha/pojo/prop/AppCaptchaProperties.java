package cn.huahanedu.frame.captcha.pojo.prop;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 @author cjf
 @since 1.0 */
@Data
@Component
@ConfigurationProperties("app.captcha")
public class AppCaptchaProperties {

    /**
     验证码有效期，默认 5 分钟
     */
    private Duration expire = Duration.ofMinutes(5);
    /**
     字体颜色
     */
    private String fontColor = "25,144,255";
    /**
     样式
     */
    private ObscurificatorStyle obscurificatorStyle = ObscurificatorStyle.WATER_RIPPLE;
    /**
     仅对字母数字验证码有效，显示字母和数字的个数
     */
    private Integer alphanumericSize = 4;

    /**
     图片验证码样式
     */

    @Getter
    public enum ObscurificatorStyle {
        /**
         阴影
         */
        SHADOW_GIMPY("com.google.code.kaptcha.impl.ShadowGimpy"),
        /**
         水纹
         */
        WATER_RIPPLE("com.google.code.kaptcha.impl.WaterRipple");

        final String value;

        ObscurificatorStyle(String value) {
            this.value = value;
        }
    }
}
