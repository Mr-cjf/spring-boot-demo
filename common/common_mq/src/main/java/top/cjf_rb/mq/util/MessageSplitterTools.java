package top.cjf_rb.mq.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MessageSplitterTools {

    /**
     每批消息最大4MB
     */
    private static final int MAX_SIZE = 1000 * 4000;

    private MessageSplitterTools() {
    }

    public static List<List<Message>> splitterMessage(List<Message> messages) {

        ArrayList<List<Message>> resultList = new ArrayList<>();

        List<Message> tempList = new ArrayList<>();
        int tempSize = 0;
        for (Message message : messages) {
            int size = messageSize(message);
            if (size > MAX_SIZE) {
                //单个消息大于总消息,似乎没有什么好办法。放入这一轮然后开始下一轮消息切分
                log.warn("单个消息大于消息最大大小 message:{} size:{}", message, size);
                tempList.add(message);

                resultList.add(tempList);

                //开始下一轮消息
                tempList = new ArrayList<>();
                tempSize = 0;
            } else {
                tempSize += size;
                if (tempSize > MAX_SIZE) {
                    resultList.add(tempList);

                    //把当前元素放入下一轮中
                    tempList = new ArrayList<>();
                    tempList.add(message);
                    tempSize = size;
                } else {
                    tempList.add(message);
                }
            }
        }

        if (!tempList.isEmpty()) {
            resultList.add(tempList);
        }

        return resultList;
    }

    public static int messageSize(Message message) {
        int tmpSize = message.getTopic()
                             .length() + message.getBody().length;
        Map<String, String> properties = message.getProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            tmpSize += entry.getKey()
                            .length() + entry.getValue()
                                             .length();
        }

        return tmpSize + 20; //for log overhead
    }
}
