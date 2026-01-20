package top.cjf_rb.frame.captcha.web.controller;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cjf_rb.frame.captcha.pojo.vo.ImageCaptchaVo;
import top.cjf_rb.frame.captcha.supplier.ImageCaptchaManager;
import top.cjf_rb.frame.captcha.supplier.ImageCaptchaProvider;

/**
 验证码控制器

 @author cjf */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/captcha/image")
public class ImageCaptchaController {

    @Resource
    private ImageCaptchaManager imageCaptchaManager;

    /**
     获取字母数字图片验证码
     */
    @GetMapping("/alphanumeric")
    public ImageCaptchaVo getAlphanumericCaptcha() {
        return imageCaptchaManager.create(ImageCaptchaProvider.Type.ALPHANUMERIC);
    }

    /**
     获取算术图片验证码
     */
    @GetMapping("/arithmetic")
    public ImageCaptchaVo getArithmeticCaptcha() {
        return imageCaptchaManager.create(ImageCaptchaProvider.Type.ARITHMETIC);
    }

    /**
     获取滑块验证码
     */
    @GetMapping("/slider")
    public ImageCaptchaVo getSliderCaptcha() {
        return imageCaptchaManager.create(ImageCaptchaProvider.Type.SLIDER);
    }

}
