package cn.huahanedu.frame.captcha.supplier;

import cn.huahanedu.frame.captcha.pojo.vo.ImageCaptchaVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;

import java.util.List;

/**
 @author cjf
 @since 1.0 */
@Slf4j
@Component
public class ImageCaptchaManager {

    @Resource
    private List<ImageCaptchaProvider<?>> imageCaptchaProviders;

    /**
     获取图片验证码

     @param type 图片验证码类型
     @return {@link ImageCaptchaVo}
     */
    public ImageCaptchaVo create(ImageCaptchaProvider.Type type) {
        for (ImageCaptchaProvider<?> imageCaptchaProvider : imageCaptchaProviders) {
            if (!imageCaptchaProvider.supports(type)) {
                continue;
            }

            return imageCaptchaProvider.create();
        }

        throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, "没有匹配到合适的图片验证码类型:" + type);
    }

    /**
     校验验证码

     @param type    图片验证码类型
     @param key     验证码标识
     @param captcha 验证码
     @return 如果匹配则 true，反之，false
     */
    public boolean verify(ImageCaptchaProvider.Type type, String key, String captcha) {
        for (ImageCaptchaProvider<?> imageCaptchaProvider : imageCaptchaProviders) {
            if (!imageCaptchaProvider.supports(type)) {
                continue;
            }

            return imageCaptchaProvider.verify(key, captcha);
        }

        throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, "没有匹配到合适的图片验证码类型:" + type);
    }

}
