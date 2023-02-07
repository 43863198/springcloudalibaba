package com.my.message.config;

/**
 * The interface Mq channel config interface.
 *
 * @description: MQ队列配置接口
 * @author: LangK
 * @create: 2020 -09-02 10:20
 */
public interface MqChannelConfigInterface {

    /**
     * 获取队列index
     *
     * @param channelName the channel name
     * @return consumer instance index
     */
    Integer getConsumerInstanceIndex(String channelName);
}
