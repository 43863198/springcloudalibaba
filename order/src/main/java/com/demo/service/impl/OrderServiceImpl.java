package com.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.demo.entity.Order;
import com.demo.message.StreamChannel;
import com.demo.service.OrderService;
import com.my.distribute.LockType;
import com.my.distribute.lock.annotation.DistributedLock;
import com.my.message.bean.MqMessage;
import com.my.message.produce.MessageEventPublisher;
import com.my.message.produce.MqMessageEvent;
import com.my.message.service.MqMessageSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private MqMessageSendService mqMessageSendService;

    @Autowired
    private MessageEventPublisher messageEventPublisher;

    @Override
    //@DistributedLock(lockType = LockType.OCMS_ACCOUNT_LOCK,expireTime = 5,waitTime = 26,key = {"#lockKeys"})
    public String confirmOrder(String orderId, Set<String> lockKeys) {

        return "lock is success";
    }

    @Override
    public void orderMesasge() {
        Order order = new Order();
        order.setOrderNo("HK123");
        order.setNumber("1");
        order.setPrice("2");
        MqMessage message = mqMessageSendService.buildMqMessage("HK123", "order下单", JSON.toJSONString(order), StreamChannel.ORDER_SETTLE_TO_STOCK_OUTPUT);
        messageEventPublisher.publishEvent(new MqMessageEvent(message.getMessageId(),message));
        log.info("一发送消息");
    }


}
