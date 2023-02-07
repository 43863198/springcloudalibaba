package com.my.message.annotation;


import com.my.message.callback.StreamAckCallBack;
import com.my.message.config.RabbitConfig;
import com.my.message.consumer.DefaultMessageHandler;
import com.my.message.entity.MqMessageFail;
import com.my.message.entity.MqMessageReceive;
import com.my.message.entity.MqMessageSend;
import com.my.message.produce.StreamSender;
import com.my.message.repository.impl.MqMessageFailRepositoryImpl;
import com.my.message.repository.impl.MqMessageReceiveRepositoryImpl;
import com.my.message.repository.impl.MqMessageSendRepositoryImpl;
import com.my.message.service.impl.MqMessageConsumerServiceImpl;
import com.my.message.service.impl.MqMessageFailServiceImpl;
import com.my.message.service.impl.MqMessageSendServiceImpl;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(value = {RabbitConfig.class, StreamAckCallBack.class, DefaultMessageHandler.class,
        MqMessageFail.class, MqMessageReceive.class
        , MqMessageReceiveRepositoryImpl.class, MqMessageSendRepositoryImpl.class, MqMessageConsumerServiceImpl.class
        , MqMessageFailServiceImpl.class, MqMessageSendServiceImpl.class
        , MqMessageSend.class, MqMessageFailRepositoryImpl.class,
        StreamSender.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EnableMessageCore {
}