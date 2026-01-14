package top.cjf_rb.mq.pojo.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 @author 崔江枫
 @since 1.0 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    NORMAL_MESSAGE(0, "", "普通消息"),
    /**
     顺序 topic 标识
     */
    ORDERED_MESSAGE(1, "-fifo", "顺序消息"),
    /**
     延时 topic 标识
     */
    DELAYED_MESSAGE(2, "-delay", "延时消息"),
    /**
     事务 topic 标识
     */
    TRANSACTIONAL_MESSAGE(3, "-tx", "事务消息"),
    ;
    @EnumValue
    private final long value;
    private final String desc;
    private final String messageDesc;


    @Override
    public String toString() {
        return messageDesc;
    }
}
