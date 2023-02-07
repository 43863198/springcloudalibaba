package com.my.message.produce;

import com.my.message.bean.MqMessage;
import lombok.ToString;

import java.util.List;

/**
 * The type Mq message event.
 */
@ToString
public class MqMessageEventNoTransaction extends BaseMessageEvent {


    /**
     * Instantiates a new Mq message event.
     *
     * @param transactionNo the transaction no
     * @param mqMessages    the mq messages
     */
    public MqMessageEventNoTransaction(String transactionNo, MqMessage... mqMessages) {
        super(transactionNo, mqMessages);
    }

    public MqMessageEventNoTransaction(String transactionNo, List<MqMessage> mqMessages) {
        super(transactionNo, mqMessages == null ? new MqMessage[0] : mqMessages.toArray(new MqMessage[mqMessages.size()]));
    }

}
