package cn.huahanedu.frame.captcha.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SliderCaptchaVo extends ImageCaptchaVo {

    /**
     BASE64滑块图片
     */
    private String sliceImage;

    /**
     滑块x轴坐标
     */
    private Integer x;

    /**
     滑块y轴坐标
     */
    private Integer y;

}
