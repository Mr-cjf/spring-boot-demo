package top.cjf_rb.mq.base;



import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.MDC;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import top.cjf_rb.mq.pojo.constant.MessageTypeEnum;
import top.cjf_rb.mq.pojo.constant.MqTypeEnum;
import top.cjf_rb.mq.pojo.entity.MqMessagesProducer;
import top.cjf_rb.mq.service.MqMessagesProducerService;
import top.cjf_rb.mq.util.MessageSplitterTools;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseProducer {

    private static final String END_ERR = "发送消息失败";
    private final RocketMQTemplate rocketMQTemplate;

    /**
     同步发送顺序消息。

     @param topic   消息主题。
     @param tag     消息标签。
     @param data    消息体。
     @param hashKey 用于消息有序性的哈希键。
     @param <T>     消息体的泛型类型。
     @return 发送结果。
     @throws BusinessException 当消息发送失败时，抛出业务异常。
     */
    public <T> SendResult syncSendOrderly(MqTopicEnum topic, String tag, T data, String hashKey) {
        // 生成跟踪ID，用于跟踪消息的生命周期。
        String traceId = getTraceId();

        // 根据传入参数准备MQ消息对象。
        var mqMessages = getMqMessagesProducer(topic, tag, data, traceId, MessageTypeEnum.ORDERED_MESSAGE);
        // 构建Spring Messaging的消息对象，设置消息体和头信息。
        org.springframework.messaging.Message<T> message = MessageBuilder.withPayload(data)
                                                                         .setHeader(RocketMQHeaders.KEYS, traceId)
                                                                         .setHeader(AppSystemConst.TRACE_ID, traceId)
                                                                         .build();
        try {
            // 使用RocketMQTemplate同步发送顺序消息，并返回发送结果。
            return rocketMQTemplate.syncSendOrderly(RocketMqs.destination(topic, tag), message, hashKey);
        } catch (Exception e) {
            // 当消息发送异常时，记录错误信息并抛出业务异常。
            errMqMessageProducer(topic, tag, mqMessages);
            throw new InternalServerException(END_ERR, e);
        }
    }


    /**
     同步有序发送消息。
     <p>
     通过此方法可以将消息以同步的方式发送到指定的主题，并保证消息的有序性。使用了泛型T，允许发送任意类型的数据。 如果需要指定消息的分区顺序性，可以提供hashKey。如果不需要分区顺序性，可以传入0。
     此方法是syncSendOrderly(String, String, Object, String, long, int)的重载版本，简化了调用时的参数设置。

     @param topic   消息的主题，用于标识消息的类别。
     @param tag     消息的标签，用于对消息进行分类，同一主题下的不同标签消息可以进行过滤。
     @param data    消息的实际内容，使用泛型T表示，可以是任意类型。
     @param hashKey 用于计算消息发送的分区的键，如果需要保证分区内的消息顺序，此参数应非空。如果不需要分区顺序性，可以传入0。
     @param timeout 超时时间，单位为毫秒，用于控制发送操作的等待时间。
     @return 返回发送结果，包含消息的ID等信息。
     */
    public <T> SendResult syncSendOrderly(MqTopicEnum topic, String tag, T data, String hashKey, long timeout) {
        return syncSendOrderly(topic, tag, data, hashKey, timeout, 0);
    }


    public <T> SendResult syncSendOrderly(MqTopicEnum topic, String tag, T data, String hashKey, long timeout,
                                          int delayLevel) {
        String traceId = getTraceId();


        var mqMessages = getMqMessagesProducer(topic, tag, data, traceId, MessageTypeEnum.ORDERED_MESSAGE);
        org.springframework.messaging.Message<T> message = MessageBuilder.withPayload(data)
                                                                         .setHeader(RocketMQHeaders.KEYS, traceId)
                                                                         .setHeader(AppSystemConst.TRACE_ID, traceId)
                                                                         .build();
        try {
            return rocketMQTemplate.syncSendOrderly(RocketMqs.destination(topic, tag), message, hashKey, timeout,
                                                    delayLevel);
        } catch (Exception e) {
            errMqMessageProducer(topic, tag, mqMessages);
            throw new InternalServerException(END_ERR, e);
        }
    }

    /**
     发送MQ消息

     @param topic topic
     @param tag   tag
     @param data  发送数据
     */
    public <T> void send(MqTopicEnum topic, String tag, T data) {
        String traceId = getTraceId();

        var mqMessages = getMqMessagesProducer(topic, tag, data, traceId, MessageTypeEnum.NORMAL_MESSAGE);
        org.springframework.messaging.Message<T> message = MessageBuilder.withPayload(data)
                                                                         .setHeader(RocketMQHeaders.KEYS, traceId)
                                                                         .setHeader(AppSystemConst.TRACE_ID, traceId)
                                                                         .build();
        try {
            rocketMQTemplate.send(RocketMqs.destination(topic, tag), message);
        } catch (Exception e) {
            errMqMessageProducer(topic, tag, mqMessages);
            throw new InternalServerException(END_ERR, e);
        }
    }

    /**
     发送延时MQ消息

     @param topic topic
     @param tag   tag
     @param data  发送数据
     */
    public <T> void asyncSend(MqTopicEnum topic, String tag, T data, SendCallback sendCallback, long timeout,
                              int delayLevel) {
        String traceId = getTraceId();


        var mqMessages = getMqMessagesProducer(topic, tag, data, traceId, MessageTypeEnum.DELAYED_MESSAGE);
        org.springframework.messaging.Message<T> message = MessageBuilder.withPayload(data)
                                                                         .setHeader(RocketMQHeaders.KEYS, traceId)
                                                                         .setHeader(AppSystemConst.TRACE_ID, traceId)
                                                                         .build();
        try {
            rocketMQTemplate.asyncSend(RocketMqs.destination(topic, tag), message, sendCallback, timeout, delayLevel);
        } catch (Exception e) {
            errMqMessageProducer(topic, tag, mqMessages);
            throw new InternalServerException(END_ERR, e);
        }

    }

    /**
     发送顺序消息

     @param topic topic
     @param tag   tag
     @param data  发送数据
     */
    public <T> void asyncSend(MqTopicEnum topic, String tag, T data, SendCallback sendCallback, long timeout) {
        asyncSend(topic, tag, data, sendCallback, timeout, 0);
    }

    /**
     批量发送mq消息

     @param topic    topic
     @param tag      tag
     @param dataList 批量发送的数据
     */
    public <T> void batchSend(MqTopicEnum topic, String tag, List<T> dataList) {
        Asserts.notEmpty(dataList, ErrorCodeEnum.UNKNOWN_ERROR);
        List<Message> listMessage = dataList.stream()
                                            .map(data -> {
                                                String traceId = getTraceId();
                                                Message message = new Message(topic.getValue(), tag, traceId,
                                                                              Jsons.stringify(data)
                                                                                   .getBytes(StandardCharsets.UTF_8));
                                                message.putUserProperty(MessageHeaders.CONTENT_TYPE,
                                                                        MimeTypeUtils.APPLICATION_JSON_VALUE);
                                                message.putUserProperty(AppSystemConst.TRACE_ID, traceId);


                                                return message;
                                            })
                                            .toList();

        DefaultMQProducer producer = rocketMQTemplate.getProducer();
        MessageSplitterTools.splitterMessage(listMessage)
                            .forEach(list -> {
                                try {
                                    producer.send(list, producer.getSendMsgTimeout());
                                } catch (Exception e) {
                                    if (e instanceof InterruptedException) {
                                        Thread.currentThread()
                                              .interrupt();
                                    }

                                    log.error("syncSend with batch failed. destination:{}, messages.size:{} ",
                                              RocketMqs.destination(topic, tag), list.size());
                                    errMqMessageProducerBatch(list);
                                    throw new InternalServerException(END_ERR, e);
                                }
                            });

    }

    private void errMqMessageProducerBatch(List<Message> list) {
        List<MqMessagesProducer> mqMessagesProducerList = list.stream()
                                                              .map(message -> {
                                                                  var mqMessages = new MqMessagesProducer();
                                                                  mqMessages.setErrInfo("发送失败");
                                                                  mqMessages.setTopic(
                                                                          MqTopicEnum.valueOf(message.getTopic()));
                                                                  mqMessages.setBody(
                                                                          Jsons.stringify(message.getBody()));
                                                                  mqMessages.setMqKeys(message.getKeys());
                                                                  mqMessages.setMqTag(message.getTags());
                                                                  mqMessages.setMqType(MqTypeEnum.ROCKETMQ);
                                                                  mqMessages.setTraceId(message.getUserProperty(
                                                                          AppSystemConst.TRACE_ID));
                                                                  mqMessages.setMessageStatus(true);
                                                                  mqMessages.setMessageType(
                                                                          MessageTypeEnum.NORMAL_MESSAGE);
                                                                  mqMessages.setRepeatStatus(0);
                                                                  mqMessages.setCreatorId(0L);
                                                                  mqMessages.setCreateTime(Instant.now());
                                                                  mqMessages.setUpdateTime(Instant.now());
                                                                  return mqMessages;
                                                              })
                                                              .toList();
        SpringContext.getBean(MqMessagesProducerService.class)
                     .saveBatch(mqMessagesProducerList);
    }

    private String getTraceId() {
        return Optional.ofNullable(MDC.get(TraceIdFilter.TRACE_ID))
                       .orElse(Identifiers.uuid());
    }

    private void errMqMessageProducer(MqTopicEnum topic, String tag, MqMessagesProducer mqMessages) {
        log.info("mqMessages: {}", mqMessages);
        // 表示已经发送失败
        mqMessages.setMessageStatus(true);
        SpringContext.getBean(MqMessagesProducerService.class)
                     .save(mqMessages);
        log.error("发送失败:{}", RocketMqs.destination(topic, tag));
    }

    private <T> @NotNull MqMessagesProducer getMqMessagesProducer(MqTopicEnum topic, String tag, T data, String traceId,
                                                                  MessageTypeEnum messageType) {
        Asserts.notNull(data, ErrorCodeEnum.UNKNOWN_ERROR);
        // 保存生产者的消息落数据库
        MqMessagesProducer mqMessages = new MqMessagesProducer();
        mqMessages.setTopic(topic);
        mqMessages.setMqTag(tag);
        mqMessages.setBody(Jsons.stringify(data));
        mqMessages.setMqKeys(traceId);
        mqMessages.setTraceId(traceId);
        mqMessages.setMqRepeat(0);
        mqMessages.setMqType(MqTypeEnum.ROCKETMQ);
        mqMessages.setRepeatStatus(0);
        mqMessages.setMessageType(messageType);
        mqMessages.setCreatorId(0L);
        mqMessages.setCreateTime(Instant.now());
        mqMessages.setUpdateTime(Instant.now());
        log.info("topic：{}，tag：{}，traceId:{},destination：{}，消息内容{}", topic, tag, traceId,
                 RocketMqs.destination(topic, tag), data);
        return mqMessages;
    }

}
