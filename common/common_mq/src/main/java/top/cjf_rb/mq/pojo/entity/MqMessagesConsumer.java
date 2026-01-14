package top.cjf_rb.mq.pojo.entity;


import cn.huahanedu.beego.littleBountifulSchool.domain.mq.pojo.constant.MessageTypeEnum;
import cn.huahanedu.beego.littleBountifulSchool.domain.mq.pojo.constant.MqTypeEnum;
import cn.huahanedu.frame.mp.pojo.entity.BasicEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;

/**
 (Messages)表实体类

 @author cjf
 @since 2024-07-15 14:46:52 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@TableName("mq_messages_consumer")
public class MqMessagesConsumer extends BasicEntity<Long> {
    @Serial
    private static final long serialVersionUID = 8131387268003361539L;
    private String appName;
    // 获取消息id
    private String msgId;
    /**
     主题
     */
    private String topic;
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
     重复次数
     */
    private Integer mqRepeat;
    /**
     链路id
     */
    private String traceId;
    /**
     0-rocketmq,1-kafka
     */
    private MqTypeEnum mqType;
    /**
     0-false:未重复，1-true:已经重复 2：重复中
     */
    private Integer repeatStatus;
    /**
     0-普通消息，1-顺序消息，2-延迟消息，3-事务消息，4-批量消息
     */
    private MessageTypeEnum messageType;
}

