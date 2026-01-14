package top.cjf_rb.mq.pojo.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 @author 崔江枫
 @since 1.0 */
@Getter
@AllArgsConstructor
public enum MqTypeEnum {
    ROCKETMQ(0, "rocketmq"),
    KAFKA(1, "kafka"),
    ;
    @EnumValue
    private final long value;
    private final String desc;

    @Override
    public String toString() {
        return desc;
    }
}
