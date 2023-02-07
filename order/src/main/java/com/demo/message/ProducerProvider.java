package com.demo.message;

import com.my.message.produce.ProducerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 消息生产者提供者实现类
 * @author: LangK
 * @create: 2020-09-02 10:09
 */
@EnableBinding(StreamSource.class)
@Component
public class ProducerProvider implements ProducerInterface {

    private static final Map<String, MessageChannel> CHANNEL_MAP = new HashMap<>();

    @Autowired
    private BinderAwareChannelResolver resolver;


    @Override
    public MessageChannel getMessageChannel(String channelName) {
        MessageChannel messageChannel = CHANNEL_MAP.computeIfAbsent(channelName, name -> resolver.resolveDestination(name));
        return messageChannel;
    }

    @Override
    public String getDlqRouterKey() {
        return StreamSource.X_CRB_DLQ_OUTPUT;
    }
}
