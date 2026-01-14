package cn.huahanedu.frame.captcha.supplier;

import cn.huahanedu.frame.captcha.pojo.vo.SliderCaptchaVo;
import cn.huahanedu.frame.captcha.util.SliderCaptchaUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;
import top.cjf_rb.core.util.Nones;
import top.cjf_rb.redis.context.type.accessor.PrefixCacheAccessor;

import java.io.IOException;
import java.util.Optional;

/**
 字母数字图片验证码

 @author cjf
 @since 1.0 */
@Slf4j
@Component
public class SliderCaptchaProvider implements ImageCaptchaProvider<SliderCaptchaVo> {

    @Resource
    public PrefixCacheAccessor<String> imageCaptchaAccessor;

    @Override
    public boolean supports(Type type) {
        return Type.SLIDER.equals(type);
    }

    @Override
    public SliderCaptchaVo create() {
        // 创建滑块图片
        SliderCaptchaVo sliderCaptchaVo;
        try {
            sliderCaptchaVo = SliderCaptchaUtils.createCaptcha();
        } catch (IOException e) {
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, "创建滑块验证码异常", e);
        }

        // 缓存验证码
        String imageKey = sliderCaptchaVo.getKey();
        imageCaptchaAccessor.set(imageKey, sliderCaptchaVo.getX()
                                                          .toString());
        // 清除 X 坐标
        sliderCaptchaVo.setX(null);
        return sliderCaptchaVo;
    }

    @Override
    public boolean verify(String key, String captcha) {
        if (Nones.isBlank(key) || Nones.isBlank(captcha)) {
            return false;
        }

        int inputX;
        try {
            inputX = Integer.parseInt(captcha);
        } catch (Exception e) {
            // 非数字
            return false;
        }

        // 缓存已失效
        Optional<String> optional = imageCaptchaAccessor.get(key);
        if (optional.isEmpty()) {
            return false;
        }

        // 误差 15 内
        int cacheX = Integer.parseInt(optional.get());
        if (Math.abs(cacheX - inputX) > 15) {
            return false;
        }

        imageCaptchaAccessor.clear(key);
        return true;
    }

}
