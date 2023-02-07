package com.my.message.produce;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.concurrent.ConcurrentHashMap;

import static com.my.message.util.MessageUtils.eventMapKey;

/**
 * Class MessageEventPublisher
 *
 * @author linyang
 * @date 2020/9/9
 */
@Log4j2
public class MessageEventPublisher implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @Qualifier("eventMap")
    private ConcurrentHashMap<String, Object> eventMap;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(MqMessageEvent event) {
        if (event == null) {
            log.warn("Event为null，将不会处理发送事件。");
            return;
        }
        String transactionNo = event.getTransactionNo();
        int eventHash = event.hashCode();
        try {
            eventMap.computeIfAbsent(eventMapKey(event), t -> {
                applicationEventPublisher.publishEvent(event);
                return eventHash;
            });
        } finally {
            log.info("单号【{}】的event已处理，hash code = {}", transactionNo, eventHash);
        }
    }


    public void publishEvent(MqMessageEventNoTransaction event) {
        if (event == null) {
            log.warn("Event为null，将不会处理发送事件。");
            return;
        }
        String transactionNo = event.getTransactionNo();
        int eventHash = event.hashCode();
        try {
            eventMap.computeIfAbsent(eventMapKey(event), t -> {
                applicationEventPublisher.publishEvent(event);
                return eventHash;
            });
        } finally {
            log.info("单号【{}】的event已处理，hash code = {}", transactionNo, eventHash);
        }
    }


}
