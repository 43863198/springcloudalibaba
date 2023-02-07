package com.my.message.bean;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


/**
 * The type Mq message.
 *
 * @Project ocms -product
 * @PackageName com.crb.ocms.product.domain.util.bean
 * @ClassName MqMessage
 * @Description RabbitMQ消息body （即业务体）
 */
public class MqMessage implements Serializable {

    private static final long serialVersionUID = 5682431048458801302L;
    /**
     * messageId : 业务编号（订单号、特征码、产品ID）
     * messageTitle : 业务类型
     * messageBody : 消息内容eg.{"SALESBILLNO":"abcde","CORPORATION_ID":1,"STATUS":1,"IS_SETTLED":true,"IS_LADING":true}
     * messageRouter : 消息通道，StreamChannel类中定义的通道，eg.StreamChannel.<channelName>
     * uuid 用于消息幂等，保证唯一
     * instanceCount 消息分区总数
     * delay 延时时间（单位毫秒）
     */

    private String transactionNo;
    private String messageId;
    private String messageTitle;
    private String messageBody;
    private String messageRouter;
    private String uuid;
    // private Integer instanceCount;
    private Integer delay;
    private String exceptionCode;
    private String exceptionDesc;
    private Object partitionKey;
    // 暂未启用，后续会用于追踪关联消息
    private String correlationId;


    /**
     * Instantiates a new Mq message.
     */
    public MqMessage() {

    }


    /**
     * Instantiates a new Mq message.
     *
     * @param messageId     the message id
     * @param messageTitle  the message title
     * @param messageBody   the message body
     * @param messageRouter the message router
     * @param uuid          the uuid
     */
    public MqMessage(String messageId, String messageTitle, String messageBody, String messageRouter, String uuid) {
        this.messageId = messageId;
        this.transactionNo = messageId;
        this.messageTitle = messageTitle;
        this.messageBody = messageBody;
        this.messageRouter = messageRouter;
        this.uuid=uuid;
    }

    /**
     * Instantiates a new Mq message.
     *
     * @param messageId     the message id
     * @param messageTitle  the message title
     * @param messageBody   the message body
     * @param messageRouter the message router
     * @param uuid          the uuid
     * @param expiration    未使用
     * @param delay         the delay
     */
    public MqMessage(String messageId, String messageTitle, String messageBody, String messageRouter, String uuid, Integer expiration, Integer delay) {
        this.messageId = messageId;
        this.transactionNo = messageId;
        this.messageTitle = messageTitle;
        this.messageBody = messageBody;
        this.messageRouter = messageRouter;
        this.uuid=uuid;
        this.delay = delay;
    }

    /**
     * Gets transaction no.
     *
     * @return the transaction no
     */
    public String getTransactionNo() {
        return transactionNo;
    }

    /**
     * Sets transaction no.
     *
     * @param transactionNo the transaction no
     */
    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    /**
     * Gets message id.
     *
     * @return the message id
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets message id.
     *
     * @param messageId the message id
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
        this.transactionNo = messageId;
    }

    /**
     * Gets message title.
     *
     * @return the message title
     */
    public String getMessageTitle() {
        return messageTitle;
    }

    /**
     * Sets message title.
     *
     * @param messageTitle the message title
     */
    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    /**
     * Gets message body.
     *
     * @return the message body
     */
    public String getMessageBody() {
        return messageBody;
    }

    /**
     * Sets message body.
     *
     * @param messageBody the message body
     */
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * Gets message router.
     *
     * @return the message router
     */
    public String getMessageRouter() {
        return messageRouter;
    }

    /**
     * Sets message router.
     *
     * @param messageRouter the message router
     */
    public void setMessageRouter(String messageRouter) {
        this.messageRouter = messageRouter;
    }

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets uuid.
     *
     * @param uuid the uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets delay.
     *
     * @return the delay
     */
    public Integer getDelay() {
        return delay;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     */
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    /**
     * Gets exception desc.
     *
     * @return the exception desc
     */
    public String getExceptionDesc() {
        return exceptionDesc;
    }

    /**
     * Sets exception desc.
     *
     * @param exceptionDesc the exception desc
     */
    public void setExceptionDesc(String exceptionDesc) {
        this.exceptionDesc = exceptionDesc;
    }

    /**
     * Gets partition key.
     *
     * @return the partition key
     */
    public Object getPartitionKey() {
        return partitionKey;
    }

    /**
     * Sets partition key.
     *
     * @param partitionKey the partition key
     */
    public void setPartitionKey(Object partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Gets correlation id.
     *
     * @return the correlation id
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets correlation id.
     *
     * @param correlationId the correlation id
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "MqMessage{" +
                "messageId='" + messageId + '\'' +
                ", transactionNo='" + transactionNo + '\'' +
                ", messageTitle='" + messageTitle + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", messageRouter='" + messageRouter + '\'' +
                ", uuid='" + uuid + '\'' +
                ", delay=" + delay +
                ", exceptionCode='" + exceptionCode + '\'' +
                ", exceptionDesc='" + exceptionDesc + '\'' +
                ", partitionKey=" + partitionKey +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MqMessage message = (MqMessage) o;
        return uuid.equals(message.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }


    /**
     * The type Builder.
     */
    public static final class Builder {
        private MqMessage mqMessage;

        private Builder() {
            mqMessage = new MqMessage();
      //      mqMessage.setUuid(UUIDGenerator.generate());
            mqMessage.setUuid(UUID.randomUUID().toString());
        }

        /**
         * Message builder.
         *
         * @return the builder
         */
        public static Builder message() {
            return new Builder();
        }

        /**
         * Transaction no builder.
         *
         * @param transactionNo the transaction no
         * @return the builder
         */
        public Builder transactionNo(String transactionNo) {
            mqMessage.setMessageId(transactionNo);
            mqMessage.setTransactionNo(transactionNo);
            return this;
        }

        /**
         * Message title builder.
         *
         * @param messageTitle the message title
         * @return the builder
         */
        public Builder messageTitle(String messageTitle) {
            mqMessage.setMessageTitle(messageTitle);
            return this;
        }

        /**
         * Message body builder.
         *
         * @param messageBody the message body
         * @return the builder
         */
        public Builder messageBody(String messageBody) {
            mqMessage.setMessageBody(messageBody);
            return this;
        }

        /**
         * Message router builder.
         *
         * @param messageRouter the message router
         * @return the builder
         */
        public Builder messageRouter(String messageRouter) {
            mqMessage.setMessageRouter(messageRouter);
            return this;
        }

        /**
         * Uuid builder.
         *
         * @param uuid the uuid
         * @return the builder
         */
        public Builder uuid(String uuid) {
            mqMessage.setUuid(uuid);
            return this;
        }

        /**
         * Delay builder.
         *
         * @param delay the delay
         * @return the builder
         */
        public Builder delay(Integer delay) {
            mqMessage.setDelay(delay);
            return this;
        }

        /**
         * Exception desc builder.
         *
         * @param exceptionDesc the exception desc
         * @return the builder
         */
        public Builder exceptionDesc(String exceptionDesc) {
            mqMessage.setExceptionDesc(exceptionDesc);
            return this;
        }

        public Builder exceptionDesc(String exceptionCode,String exceptionDesc) {
            mqMessage.setExceptionCode(exceptionCode);
            mqMessage.setExceptionDesc(exceptionDesc);
            return this;
        }

        /**
         * Partition key builder.
         *
         * @param partitionKey the partition key
         * @return the builder
         */
        public Builder partitionKey(Object partitionKey) {
            mqMessage.setPartitionKey(partitionKey);
            return this;
        }

        /**
         * Correlation id builder.
         *
         * @param correlationId the correlation id
         * @return the builder
         */
        public Builder correlationId(String correlationId) {
            mqMessage.setCorrelationId(correlationId);
            return this;
        }

        /**
         * Build mq message.
         *
         * @return the mq message
         */
        public MqMessage build() {
            if (!StringUtils.hasText(mqMessage.getUuid())) {
                mqMessage.setUuid(UUID.randomUUID().toString());
            }

            if (mqMessage.getPartitionKey() == null) {
                mqMessage.setPartitionKey(mqMessage.getMessageId());
            }

            return mqMessage;
        }
    }
}
