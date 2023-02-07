package com.my.message.exception;


/**
 * @Project ocms-order
 * @PackageName com.crb.ocms.order.domain.util.exceptions.code
 * @ClassName OrderException
 * @Author HuangLong
 * @Date 2019/3/4 11:00
 * @Description MQ异常
 */
public enum MQException implements OcmsExceptionType {
    /**
     * MQ异常
     */
    MQ_ACK_MSGID_NULL("E1O00100","监听器接收MQ消息缺失messageId！"),
    MQ_MSGID_NULL("E1O00101","监听器接收MQ消息缺失messageId！"),
    MQ_PAYLOAD_NULL("E1O00102","监听器接收MQ消息缺失payload！"),
    MQ_BIZ_ERROR("E1O00103","监听器消费MQ消息业务逻辑处理失败！"),
    MQ_BIZ_UNKNOWN("E1O00104","消息消费时未知业务类型！"),
    ;
    private String code;
    private String description;

    MQException(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
