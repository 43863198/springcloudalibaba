package com.my.message.produce;

import org.springframework.messaging.MessageChannel;

/**
 * The interface Producer interface.
 */
public interface ProducerInterface {

    /**
     * 获取消息通道
     *
     * @param channelName the channel name
     * @return message channel
     */
    MessageChannel getMessageChannel(String channelName);

    /**
     * 获取死信队列路由Key
     *
     * @return dlq router key
     */
    String getDlqRouterKey();

}
