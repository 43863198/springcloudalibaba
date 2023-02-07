package com.my.message.produce;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.my.message.bean.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.my.message.MsgConstants.*;
import static com.my.message.util.MessageUtils.eventMapKey;


/**
 * The type Stream sender.
 *
 * @Project rocketmq -test
 * @PackageName com.example.demo.service.impl
 * @ClassName MessageProviderImpl
 * @Author yangshuo
 * @Date 2019 /3/21 7:09 PM
 * @Description MQ消息发送中心
 */
@Slf4j
@Component
public class StreamSender {
    @Autowired
    @Qualifier("eventMap")
    private ConcurrentHashMap<String, Object> eventMap;

    @Autowired
    private ProducerInterface producerInterface;

    /**
     * Send message.
     *
     * @param event the event
     */
    @Async("msgTaskExecutor")
    @EventListener
    public void sendMessage(MqMessageEventNoTransaction event) {
        sendMessageUnifyEntrance(event);
    }

    /**
     * Send message.
     *
     * @param event the event
     */
    @Async("msgTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
    public void sendMessage(MqMessageEvent event) {
        sendMessageUnifyEntrance(event);
    }

    /**
     * unify entrance send message
     * @param event
     */
    private void sendMessageUnifyEntrance(BaseMessageEvent event){
        String transactionNo = event.getTransactionNo();
        int eventHash = event.hashCode();
        log.info("收到发送消息请求, 单号：{}, hash --> {}, event --> {}", transactionNo, eventHash, event);

        MqMessage[] messages = event.getMqMessages();
        if (ArrayUtils.isEmpty(messages)) {
            log.warn("event中无消息，取消发送。");
            return;
        }

        log.info("开始发送消息 ...");
        if (messages.length == 1) {
            internalSend(messages[0]);
        } else {
            Arrays.stream(messages)
                    .parallel()
                    .forEach(this::internalSend);
        }
        //发送完成后，移除当前消息
        try {
            eventMap.remove(eventMapKey(event));
        } finally {
            log.info("从eventMap中删除单号【{}】的事件【{}】", transactionNo, eventHash);
        }

        log.info("发送完成！");
    }


    private void internalSend(MqMessage mqMessage) {
        if (ObjectUtils.isEmpty(mqMessage) || ObjectUtils.isEmpty(mqMessage.getMessageId())) {
            log.warn("消息为空或消息ID为空:{}", mqMessage);
            return;
        }
        String messageId = mqMessage.getUuid();
        if (StringUtils.isBlank(messageId)) {
            log.warn("原始消息ID为空。");
            return;
        }

        String channelName = mqMessage.getMessageRouter();
        // 如果没有设置partitionKey，默认使用messageId作为partitionKey
        Object partitionKey = Optional.ofNullable(mqMessage.getPartitionKey()).orElse(mqMessage.getMessageId());
        //mqMessage.getPartitionKey() == null ? mqMessage.getMessageId() : mqMessage.getPartitionKey();
        if(ObjectUtils.isEmpty(partitionKey)){
            partitionKey = "default";
        }
        Message<String> message = convertToGenericMessage(mqMessage, messageId, partitionKey);

        route2Send(message, channelName);
    }

    /**
     * 构建消息体
     *
     * @param mqMessage    业务消息结构
     * @param messageId    消息ID
     * @param partitionKey 分片键
     * @return Message<String>
     */
    private Message<String> convertToGenericMessage(final MqMessage mqMessage, final String messageId, final Object partitionKey) {
        Map<String, Object> headers = Maps.newHashMap();
        headers.put(CONTENT_TYPE, CONTENT_APPLICATION_JSON);
        headers.put(PARTITION_KEY, partitionKey);
        headers.put(MESSAGE_ID, messageId);
        headers.put(OCMS_TRANSACTION_NO, mqMessage.getMessageId());
        Optional.ofNullable(mqMessage.getDelay())
                .ifPresent(delay -> headers.put(DELAY, delay));
//        if (null != mqMessage.getDelay()) {
//            // 设置消息延迟时间
//            headers.put(DELAY, mqMessage.getDelay());
//        }
        return new GenericMessage<>(JSON.toJSONString(mqMessage), headers);
    }

    /**
     * 路由发消息
     *
     * @param message     org.springframework.messaging.Message
     * @param channelName 通道名称
     */
    private void route2Send(Message<String> message, String channelName) {
        if (message == null) {
            log.warn("发送的消息为空，取消发送。");
            return;
        }

        if (channelName == null) {
            log.warn("channelName为空，取消发送。");
            return;
        }

        Object transactionNo = message.getHeaders().get(OCMS_TRANSACTION_NO);
        log.info("单号【{}】, 向channel -> {}, 发送消息 -> {}", transactionNo, channelName, message);

        try {
            /**
             * 路由发送消息，computeIfAbsent时由于采用HashMap，有并发问题，但是由于只是覆盖name-channel的对应关系，所以可以接受。
             * BinderAwareChannelResolver的resolveDestination方法为synchronized的，并发发送时会有性能问题，所以此处只试图解析
             * 一次，后面使用缓存的结果。
             */
            MessageChannel messageChannel = producerInterface.getMessageChannel(channelName);
            log.info("解析到的MessageChannel = {}", messageChannel);

            if (messageChannel != null) {
                messageChannel.send(message, 500);
            } else {
                log.warn("发送消息时无法找到对应的队列，单号：{}, 队列：{}", transactionNo, channelName);
            }
        } catch (Exception e) {
            log.error("发送消息时发生错误，单号：{}, 异常：{}", transactionNo, e);
        }
    }
}