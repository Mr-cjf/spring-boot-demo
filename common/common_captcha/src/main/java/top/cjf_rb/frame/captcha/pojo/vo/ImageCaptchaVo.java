package top.cjf_rb.frame.captcha.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 @author cjf
 @since 1.0 */
@Data
@Accessors(chain = true)
public class ImageCaptchaVo {

    /**
     图片标识
     */
    private String key;

    /**
     BASE64图片
     */
    private String image;

}
