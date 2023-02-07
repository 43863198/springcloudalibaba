package com.my.message.consumer;

import com.my.message.bean.MqMessage;
import com.rabbitmq.client.Channel;

import java.util.Optional;
import java.util.function.Function;

/**
 * The interface Message handler.
 *
 * @author linyang
 * @date 2020 /08/20
 */
public interface MessageHandler {
    /**
     * 对消息进行正向处理
     *
     * @param inbound   消息
     * @param processor 处理方法，由各个事件的listener实现自己的业务逻辑
     */
    void handle(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> processor);

    /**
     * 如果对消息进行正向处理出错，需要回滚时候，要调用此方法
     *
     * @param inbound        消息
     * @param errorProcessor 回滚方法，由各个事件的listener实现自己的回滚逻辑
     */
    void rollback(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> errorProcessor);

    /**
     * 将消息变为死信，同时移入死信队列
     *
     * @param inbound     the message
     * @param channel     the channel
     * @param deliveryTag the delivery tag
     */
    void deadLetter(MqMessage inbound, Channel channel, Long deliveryTag);

    /**
     * 消息重试，并补偿日志/数据等操作
     *
     * @param inbound        the message
     * @param retryProcessor the retry processor
     */
    void retryCompensate(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> retryProcessor);


    /**
     * Temporary handle ocms exception.
     *
     * @param inbound                the inbound
     * @param ocmsExceptionProcessor the ocms exception processor
     */
    void temporaryHandleOCMSException(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> ocmsExceptionProcessor);

//    /**
//     * 发送消息
//     *
//     * @param inbound  the inbound
//     * @param outbound the outbound
//     */
//    void publishEvent(MqMessage inbound, MqMessage[] outbound);
}
