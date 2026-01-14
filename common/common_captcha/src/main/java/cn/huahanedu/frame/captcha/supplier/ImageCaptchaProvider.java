package cn.huahanedu.frame.captcha.supplier;

import cn.huahanedu.frame.captcha.pojo.vo.ImageCaptchaVo;

/**
 @author cjf
 @since 1.0 */
public interface ImageCaptchaProvider<T extends ImageCaptchaVo> {

    /**
     是否支持该验证码

     @param type 验证码类型
     @return 支持则 true，反之，false
     */
    boolean supports(Type type);

    T create();

    boolean verify(String key, String captcha);

    enum Type {
        /**
         字母数字
         */
        ALPHANUMERIC,
        /**
         算术
         */
        ARITHMETIC,
        /**
         滑块
         */
        SLIDER
    }

}
