package top.cjf_rb.mq.pojo.entity;


import cn.huahanedu.beego.core.constant.MqTopicEnum;
import cn.huahanedu.beego.littleBountifulSchool.domain.mq.pojo.constant.MessageTypeEnum;
import cn.huahanedu.beego.littleBountifulSchool.domain.mq.pojo.constant.MqTypeEnum;
import cn.huahanedu.frame.mp.pojo.entity.BasicEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;

/**
 (MqMessagesProducer)表实体类

 @author cjf
 @since 2024-07-17 10:16:16 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@TableName("mq_messages_producer")
public class MqMessagesProducer extends BasicEntity<Long> {
    @Serial
    private static final long serialVersionUID = 3769962086090892013L;
    private String appName;
    /**
     消息id
     */
    private String msgId;
    /**
     主题
     */
    private MqTopicEnum topic;
    /**
     标签
     */
    private String mqTag;
    /**
     键值
     */
    private String mqKeys;
    /**
     消息体
     */
    private String body;
    /**
     失败原因
     */
    private String errInfo;
    /**
     链路id
     */
    private String traceId;
    /**
     0-rocketmq,1-kafka
     */
    private MqTypeEnum mqType;
    /**
     0 false 表示未发送失败，1 true 表示发送失败
     */
    private Boolean messageStatus;

    /**
     0:未重复，1:已经重复 2:重复中
     */
    private Integer repeatStatus;
    /**
     重复次数
     */
    private Integer mqRepeat;
    /**
     0-普通消息，1-顺序消息，2-延迟消息，3-事务消息，4-批量消息
     */
    private MessageTypeEnum messageType;
}

