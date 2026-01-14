package top.cjf_rb.mq.base;



import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.MDC;
import org.springframework.core.ResolvableType;
import org.springframework.messaging.support.MessageBuilder;
import top.cjf_rb.mq.pojo.constant.MessageTypeEnum;
import top.cjf_rb.mq.pojo.constant.MqTypeEnum;
import top.cjf_rb.mq.pojo.entity.MqMessagesConsumer;
import top.cjf_rb.mq.service.MqMessagesConsumerService;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Slf4j
public abstract class BaseConsumer<T> implements RocketMQListener<MessageExt> {

    protected final Function<Message, T> function;

    @SuppressWarnings("unchecked")
    protected BaseConsumer() {
        //获取子类定义的泛型信息
        var resolvableType = ResolvableType.forClass(getClass())
                                           .getSuperType()
                                           .getGeneric(0);
        //泛型对应的class
        var clazz = resolvableType.resolve();
        if (clazz == org.springframework.messaging.Message.class) {
            /*
             *子类接收spring message格式的消息,把rocket message的header放入spring message的header中
             * 把rocket message body按照spring message指定的泛型格式转换成对应的类型
             */
            var type = resolvableType.getGeneric(0)
                                     .getType();
            var resolvedClass = ResolvableType.forType(type)
                                              .getRawClass();
            if (resolvedClass == null) {
                throw new IllegalStateException("无法解析消息体类型: " + type);
            }
            function = message -> (T) MessageBuilder.withPayload(
                                                            Jsons.parse(new String(message.getBody()), resolvedClass))
                                                    .copyHeaders(message.getProperties())
                                                    .build();
        } else if (clazz == Message.class) {
            //子类接收rocketmq的原始消息直接返回
            function = message -> (T) message;
        } else if (clazz == String.class) {
            //子类接收String类型返回rocketmq的body
            function = message -> (T) new String(message.getBody(), StandardCharsets.UTF_8);
        } else if (clazz == byte[].class) {
            //子类接收byte数组直接返回body
            function = message -> (T) message.getBody();
        } else {
            //直接按照普通对象处理,通过jackson把body格式转化成对应的类型
            var type = resolvableType.getType();
            var resolvedClass = ResolvableType.forType(type)
                                              .getRawClass();
            if (resolvedClass == null) {
                throw new IllegalStateException("无法解析消息体类型: " + type);
            }
            function = message -> (T) Jsons.parse(new String(message.getBody()), resolvedClass);
        }
    }

    @Override
    public void onMessage(MessageExt message) {

        try {
            var propertiesMap = message.getProperties();
            var traceId = propertiesMap.get(RocketMQHeaders.KEYS);
            if (traceId == null) {
                traceId = propertiesMap.get(AppSystemConst.TRACE_ID);
                if (traceId == null) {
                    traceId = Identifiers.uuid();
                }
            }
            MDC.put(AppSystemConst.TRACE_ID, traceId);
            consumer(function.apply(message));
            log.info("消息消费成功，消息ID：{}", message.getMsgId());
        } catch (IllegalArgumentException e) {
            log.error("捕获Json解析异常", e);
            handleFailedMessageConsumption(message, e);
        } catch (Exception e) {
            handleFailedMessageConsumption(message, e);
        } finally {
            MDC.remove(AppSystemConst.TRACE_ID);
        }
    }

    public abstract void consumer(T t);

    /**
     处理消息消费失败的情况

     @param messageExt 消费失败的消息
     @param exception  引发消费失败的异常
     */
    private void handleFailedMessageConsumption(MessageExt messageExt, Exception exception) {


        // 获取追踪ID用于后续追踪
        String traceId = messageExt.getProperties()
                                   .get(AppSystemConst.TRACE_ID);

        MessageTypeEnum messageTypeEnum = getMessageTypeEnum(messageExt.getTopic());

        // 构建唯一键和标签用于后续处理
        String tags = messageExt.getProperties()
                                .get("TAGS");

        // 创建一个新的MqMessagesConsumer对象存储消费失败的信息
        MqMessagesConsumer mqMessages = new MqMessagesConsumer();
        mqMessages.setBody(new String(messageExt.getBody(), StandardCharsets.UTF_8));
        mqMessages.setErrInfo(exception.toString());
        mqMessages.setMsgId(messageExt.getMsgId());
        mqMessages.setMqKeys(messageExt.getKeys());
        mqMessages.setMqTag(tags);
        mqMessages.setMqRepeat(0);
        mqMessages.setMqType(MqTypeEnum.ROCKETMQ);
        mqMessages.setRepeatStatus(0);
        mqMessages.setTopic(messageExt.getTopic());
        mqMessages.setTraceId(traceId);
        mqMessages.setMessageType(messageTypeEnum);

        log.info("消息消费失败，达到最大重试次数，消息ID：{}", messageExt.getMsgId());
        log.info("消息消费失败，异常情况", exception);
        log.info("消息详情：{}", messageExt);
        log.info("入库消息消费失败数据 {}", mqMessages);
        SpringContext.getBean(MqMessagesConsumerService.class)
                     .save(mqMessages);
    }


    private MessageTypeEnum getMessageTypeEnum(String topic) {
        // 判断消息类型
        // 通过topic 结尾匹配上 "-fifo" 就是顺序消息
        // NORMAL_MESSAGE(0, "", "普通消息"),
        //     /**
        //      * 顺序 topic 标识
        //      */
        //     ORDERED_MESSAGE(1, "-fifo", "顺序消息"),
        //     /**
        //      * 延时 topic 标识
        //      */
        //     DELAYED_MESSAGE(2, "-delay", "延时消息"),
        //     /**
        //      * 事务 topic 标识
        //      */
        //     TRANSACTIONAL_MESSAGE(3, "-tx", "事务消息")
        if (topic.endsWith(MessageTypeEnum.ORDERED_MESSAGE.getDesc())) {
            return MessageTypeEnum.ORDERED_MESSAGE;
        } else if (topic.endsWith(MessageTypeEnum.DELAYED_MESSAGE.getDesc())) {
            return MessageTypeEnum.DELAYED_MESSAGE;
        } else if (topic.endsWith(MessageTypeEnum.TRANSACTIONAL_MESSAGE.getDesc())) {
            return MessageTypeEnum.TRANSACTIONAL_MESSAGE;
        } else {
            return MessageTypeEnum.NORMAL_MESSAGE;
        }
    }

}
