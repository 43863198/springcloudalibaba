package com.my.message;


import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.MessageHeaders;

/**
 * Class MsgConstants
 *
 * @author linyang
 * @date 2020 /8/26
 */
public final class MsgConstants {
    private MsgConstants() {
    }


    /**
     * 为消息ACK预留时间（分钟）
     */
    public static final int MQ_ACK_PRE=-5;
    /**
     * 消息状态 初始化
     */
    public static final int MQ_STATUS_INIT=0;
    /**
     * 消息状态  已发送
     */
    public static final int MQ_STATUS_SEND=1;
    /**
     * 消息状态  发送失败
     */
    public static final int MQ_STATUSFAIL=2;
    /**
     * 消息默认延时时间
     */
    public static final Integer MQ_DELAY=500;

    /**
     * The constant CONTENT_TYPE.
     */
    public static final String CONTENT_TYPE = MessageHeaders.CONTENT_TYPE;
    /**
     * The constant PARTITION_KEY.
     */
    public static final String PARTITION_KEY = "partitionKey";
    /**
     * The constant OCMS_TRANSACTION_NO.
     */
    public static final String OCMS_TRANSACTION_NO = "ocms_transaction_no";
    /**
     * The constant MESSAGE_ID.
     */
    public static final String MESSAGE_ID = AmqpHeaders.MESSAGE_ID;
    /**
     * The constant DELAY.
     */
    public static final String DELAY = AmqpHeaders.DELAY;
    /**
     * The constant CONTENT_APPLICATION_JSON.
     */
    public static final String CONTENT_APPLICATION_JSON = "application/json";
    /**
     * The constant X_DEATH.
     */
    public static final String X_DEATH = "x-death";
    /**
     * The constant X_DEATH_COUNT.
     */
    public static final String X_DEATH_COUNT = "count";
    /**
     * The constant DEFAULT_BY.
     */
    public static final String DEFAULT_BY = "crb-message-core";
    /**
     * The constant DEFAULT_USER.
     */
    public static final String DEFAULT_USER = "crb-message-core";

    /**
     * Table names
     */
    public static final String TABLE_MQ_MESSAGE_RECEIVE = "mq_message_receive";
    /**
     * The constant TABLE_MQ_MESSAGE_SEND.
     */
    public static final String TABLE_MQ_MESSAGE_SEND = "mq_message_send";
    /**
     * The constant TABLE_MQ_MESSAGE_FAIL.
     */
    public static final String TABLE_MQ_MESSAGE_FAIL = "mq_message_fail";

    public static final String TABLE_MQ_MESSAGE_BACKUP = "mq_message_backup";

    /**
     * 重试次数redis key
     */
    public static final String MQ_CONSUMER_RETRY_COUNT_KEY = "MQ_CONSUMER_RETRY_COUNT_KEY";

    /**
     * The constant PUBLISH_CONFIRM.
     */
    public static final String PUBLISH_CONFIRM = AmqpHeaders.PUBLISH_CONFIRM;
}
