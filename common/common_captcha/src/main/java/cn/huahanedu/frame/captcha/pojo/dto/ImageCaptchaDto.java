package cn.huahanedu.frame.captcha.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 @author cjf
 @since 1.0 */
@Data
@Accessors(chain = true)
public class ImageCaptchaDto {

    /**
     验证码标识
     */
    @NotBlank
    private String key;

    /**
     验证码
     */
    @NotBlank
    private String captcha;

}
