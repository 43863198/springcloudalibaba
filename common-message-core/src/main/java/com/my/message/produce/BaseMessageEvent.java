package com.my.message.produce;

import com.my.message.bean.MqMessage;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public class BaseMessageEvent {

    /**
     * The Transaction no.
     */
    final String transactionNo;
    /**
     * The Mq messages.
     */
    final MqMessage[] mqMessages;

    /**
     * Instantiates a new Mq message event.
     *
     * @param transactionNo the transaction no
     * @param mqMessages    the mq messages
     */
    public BaseMessageEvent(String transactionNo, MqMessage... mqMessages) {
        this.transactionNo = transactionNo;
        this.mqMessages = mqMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqMessageEvent that = (MqMessageEvent) o;
        return transactionNo.equals(that.transactionNo) &&
                Arrays.equals(mqMessages, that.mqMessages);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(transactionNo);
        result = 31 * result + Arrays.hashCode(mqMessages);
        return result;
    }
}
