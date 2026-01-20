package top.cjf_rb.frame.captcha.supplier;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;
import top.cjf_rb.core.util.Identifiers;
import top.cjf_rb.core.util.Nones;
import top.cjf_rb.frame.captcha.pojo.vo.ImageCaptchaVo;
import top.cjf_rb.redis.context.type.accessor.PrefixCacheAccessor;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;

/**
 字母数字图片验证码

 @author cjf
 @since 1.0 */
@Slf4j
@Component
public class AlphanumericImageCaptchaProvider implements ImageCaptchaProvider<ImageCaptchaVo> {

    @Resource
    public PrefixCacheAccessor<String> imageCaptchaAccessor;

    @Resource(name = "alphanumericProducer")
    private com.wf.captcha.SpecCaptcha alphanumericProducer;

    @Override
    public boolean supports(Type type) {
        return Type.ALPHANUMERIC.equals(type);
    }

    @Override
    public ImageCaptchaVo create() {
        // 生成文字验证码
        String code = alphanumericProducer.text(); // 使用text()方法获取验证码文本

        // 图片流转Base64字符串方式
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean success = alphanumericProducer.out(os); // 将验证码图片写入输出流
        if (!success) {
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, "字母数字图片验证码生成失败!");
        }

        // 缓存验证码
        String imageKey = Identifiers.nanoId();
        imageCaptchaAccessor.set(imageKey, code);
        return new ImageCaptchaVo().setKey(imageKey)
                                   .setImage(Base64.getEncoder()
                                                   .encodeToString(os.toByteArray()));
    }

    @Override
    public boolean verify(String key, String captcha) {
        if (Nones.isBlank(key) || Nones.isBlank(captcha)) {
            return false;
        }

        Optional<String> optional = imageCaptchaAccessor.get(key);
        if (optional.isEmpty()) {
            return false;
        }

        if (optional.get()
                    .equalsIgnoreCase(captcha)) {
            imageCaptchaAccessor.clear(key);
            return true;
        }

        return false;
    }

}
