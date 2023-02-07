package com.my.message.util;

import com.my.message.produce.BaseMessageEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.support.RabbitExceptionTranslator;

import java.io.IOException;

/**
 * Class MessageUtils
 *
 */
public final class MessageUtils {
    private static final Logger log = LoggerFactory.getLogger(MessageUtils.class);

    private MessageUtils() {
    }

    /**
     * EventMap Key生成规则
     *
     * @param event
     * @return
     */
    public static final String eventMapKey(BaseMessageEvent event) {
        return event.getTransactionNo() + "_" + event.hashCode();
    }

//    /**
//     * JOB重试时，转换消息
//     * @param messageSend
//     * @return
//     */
//    public static MqMessage convertToMqMessage(MqMessageSend messageSend) {
//        MqMessage message = MqMessage.Builder.message()
//                .transactionNo(messageSend.getSalesBillNo())
//                .messageTitle(messageSend.getBusinessType())
//                .messageBody(messageSend.getContent())
//                .messageRouter(messageSend.getOutput())
//                .uuid(messageSend.getMessageId())
//                .delay(null)
//                .partitionKey(messageSend.getRouter())
//                .build();
//        return message;
 //   }

    /**
     * 确认消费成功
     *
     * @param channel
     * @param deliveryTag
     * @throws IOException
     */
    public static final void immediateAckMessage(Channel channel, Long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Throwable e) {
            log.error("ACK消息失败：{}", e);
            // FIXME: 如果ack因为网络原因失败，到底该如何处理，重试？超过次数限制时候又该如何？
            throw RabbitExceptionTranslator.convertRabbitAccessException(e);
        }
    }

    /**
     * 从原始队列删除消息，不需要requeue，用于重复消息消费，或手动移入死信队列
     *
     * @param channel
     * @param deliveryTag
     * @throws IOException
     */
    public static final void deleteFromQueue(Channel channel, Long deliveryTag) {
        immediateAckMessage(channel, deliveryTag);
    }

    /**
     * nack消息，同时将其移到等待队列，待TTL到期后再次进行重试消费
     *
     * @param channel
     * @param deliveryTag
     * @throws IOException
     */
    public static final void rejectAndMoveToWaitingQueue(Channel channel, Long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (Throwable e) {
            log.error("NACK消息失败：{}", e);
            // FIXME: 如果nack因为网络原因失败，到底该如何处理，重试？超过次数限制时候又该如何？
            throw RabbitExceptionTranslator.convertRabbitAccessException(e);
        }
    }
}
