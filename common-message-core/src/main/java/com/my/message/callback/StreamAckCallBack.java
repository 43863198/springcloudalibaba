package com.my.message.callback;

import com.my.message.exception.OcmsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.amqp.support.NackedAmqpMessageException;
import org.springframework.integration.amqp.support.ReturnedAmqpMessageException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;

import static com.my.message.MsgConstants.*;


/**
 * The type Stream ack call back.
 *
 * @Project crb -order
 * @PackageName com.crb.cdp.ocms.message.callback
 * @ClassName StreamAckCallBack
 * @Author yangshuo
 * @Date 2019 /3/26 9:50 AM
 * @Description 消息发送成功回调
 */
@Slf4j
@Component("streamAckCallBack")
public class StreamAckCallBack implements MessageChannel {

//    @Autowired
//    private MqMessageSendService mqMessageSendService;

    @Autowired
    private MessageConverter messageConverter;

//    @Autowired
//    @Qualifier("optimisticLockRetryTemplate")
//    private RetryTemplate retryTemplate;

    @Override
    public boolean send(Message<?> message, long timeout) {
        return send(message);
    }

    @Override
    public boolean send(Message<?> message) {
        if (message == null) {
            log.error("接收到的callback消息为NULL！");
            return false;
        }

        if (log.isInfoEnabled()) {
            log.info("接收到 publisher-confirms callback 消息: => {}", message);
        }

        final MessageHeaders headers = message.getHeaders();
        // 解开Message Body会耗费资源，但目前当服务器端nack或者return消息时候没有更好的办法
        final Message<?> payloadMessage = messageConverter.toMessage(message.getPayload(), headers);

        if (log.isInfoEnabled()) {
            log.info("PayloadMessage = {}", payloadMessage);
        }

        /**
         * Check if it's an ErrorMessage?
         *
         * If yes(in case of ErrorMessage), typically, this's a message which is not able to be routed to any queue(publisher-returns).
         *      Just log it and no need to do any future processing, this message will be scanned by JOB and resend later.
         * If no(in case of GenericMessage), it's an ACK message, check the ack flag and update message status accordingly.
         **/
        if (payloadMessage instanceof ErrorMessage) {
            Object payload = payloadMessage.getPayload();

            if (payload instanceof ReturnedAmqpMessageException || payload instanceof NackedAmqpMessageException) {
                // ReturnedAmqpMessageException exception = (ReturnedAmqpMessageException) payload;
                log.warn("接收到 publisher-confirms(NACK)/publisher-returns 消息, 将不会更新消息发送表的状态 => {}", payload);
            } else {
                log.warn("返回消息的payload类型未知 => {}, 将被忽略，消息表状态将不会被更新。", payload);
            }

            return false;
        }

        String transactionNo = headers.get(OCMS_TRANSACTION_NO, String.class);
        if (log.isInfoEnabled()) {
            log.info("Header中的{} = {}", OCMS_TRANSACTION_NO, transactionNo);
        }
        String uuid = headers.get(MESSAGE_ID, String.class);
        if (log.isInfoEnabled()) {
            log.info("Header中的{} = {}", MESSAGE_ID, uuid);
        }

        try {
            if (log.isInfoEnabled()) {
                log.info("开始更新消息状态 ...");
            }

            //JudgeUtils.isNotNull(uuid, MQException.MQ_MSGID_NULL);



//            String messageId = uuid.toString().replace("-", "");
//            if (log.isInfoEnabled()) {
//                log.info("Unique message ID，messageId = {}.", messageId);
//            }

            Object ackValue = headers.get(PUBLISH_CONFIRM);
            if (log.isInfoEnabled()) {
                log.info("Header中的{} = {}.", PUBLISH_CONFIRM, ackValue);
            }
            boolean ack = ackValue == null ? false : Boolean.parseBoolean(ackValue.toString());
            if (log.isInfoEnabled()) {
                log.info("Parsed ACK flag = {} from {}.", ack, ackValue);
            }

            if (ack) {
                if (log.isInfoEnabled()) {
                    log.info("ACK flag is true, publisher-confirms successfully, will update message send status, transactionNo：{}, message ID：{}", transactionNo, uuid);
                }


//                retryTemplate.execute((RetryCallback<Boolean, ObjectOptimisticLockingFailureException>) context -> {
//                    log.info("第{}次尝试更新消息发送状态, transactionNo={}, messageId={}", context.getRetryCount(), transactionNo, messageId);
                //mqMessageSendService.update(uuid, MQ_STATUS_SEND, JSON.toJSONString(message.getHeaders()));
                if (log.isInfoEnabled()) {
                    log.info("Message send status is updated to {}, transactionNo：{}, Message ID：{}.", MQ_STATUS_SEND, transactionNo, uuid);
                }
//
//                    return true;
//                });
            } else {
                log.warn("ACK flag is false, this's abnormal, the amqp_publishConfirm header should exist and has value true.");
            }
        } catch (OcmsException ignored) {
            log.error("Business exception was thrown while processing publisher-confirms，单号：{}，uuid：{}，异常：{}.", transactionNo, uuid, ignored);
        } catch (Exception e) {
            log.error("Error happened while processing publisher-confirms，，单号：{}，uuid：{}，异常：{}.", transactionNo, uuid, e);
        }

        //return super.send(message);
        return true;
    }
}
