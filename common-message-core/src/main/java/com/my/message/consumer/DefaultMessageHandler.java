package com.my.message.consumer;

import com.my.message.bean.MqMessage;
import com.my.message.produce.MessageEventPublisher;
import com.my.message.produce.MqMessageEvent;
import com.my.message.produce.ProducerInterface;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.amqp.AmqpIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.function.Function;

import static com.my.message.util.MessageUtils.deleteFromQueue;

/**
 * The type Default message handler.
 *
 * @author linyang
 * @date 2020 /08/20
 */
@Service
@Slf4j
public class DefaultMessageHandler implements MessageHandler {

//    @Autowired
//    private MqMessageConsumerService messageConsumerService;

//    @Autowired
//    private MessageProperties messageProperties;

    @Autowired
    private ProducerInterface producerInterface;

//    @Autowired
//    private MqMessageFailService messageFailService;

    @Autowired
    @Qualifier("deadLetterRetryTemplate")
    private RetryTemplate deadLetterRetryTemplate;

    @Autowired
    private MessageEventPublisher messageEventPublisher;

    /**
     * 消息处理从此开始，保证非requeue情况下的重试在新事务里进行
     *
     * @param inbound   将要处理的消息
     * @param processor 扩展点，由BaseListener各个子类覆盖父类方法，提供自己的业务处理逻辑
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void handle(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> processor) {
//        if (messageConsumerService.isDuplicate(inbound)) {
//            // 抛出异常，让上层处理，同时封装为自定义异常，便于上层区分处理
//            throw new DuplicateMessageException(String.format("单号：%s，消息ID：%s，被重复消费。", inbound.getMessageId(), inbound.getUuid()));
//        }

        // 设置公参
        //setBaseParam(inbound);
        // 处理业务逻辑
        processor.apply(inbound)
                .ifPresent(outbound -> publishEvent(inbound, outbound));

    }

    /**
     * FIXME: 完善此方法
     *
     * @param inbound
     * @param errorProcessor
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void rollback(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> errorProcessor) {
//        // 处理回滚逻辑,回滚逻辑处理时，将消息保存到receive表中
//        if (messageConsumerService.isDuplicate(inbound)) {
//            // 抛出异常，让上层处理，同时封装为自定义异常，便于上层区分处理
//            throw new DuplicateMessageException(String.format("单号：%s，消息ID：%s，被重复消费。", inbound.getMessageId(), inbound.getUuid()));
//        }
        errorProcessor.apply(inbound)
                .ifPresent(rollbackMessages -> publishEvent(inbound, rollbackMessages));
    }

    private void publishEvent(MqMessage inbound, MqMessage[] outbound) {
        if (ArrayUtils.isEmpty(outbound)) {
            // 回复消息为空，取消发布事件
            return;
        }
        // 如果需要回复消息，在事务提交成功后，触发消息发送
        messageEventPublisher.publishEvent(new MqMessageEvent(inbound.getMessageId(), outbound));
    }

    /**
     * 将消费失败的消息保存
     *
     * @param inbound
     * @param channel
     * @param deliveryTag
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void deadLetter(MqMessage inbound, Channel channel, Long deliveryTag) {
        if (inbound == null) {
            log.warn("【死信队列】- 消息为空，终止dead letter处理。");
            return;
        }
        String transactionNo = inbound.getMessageId();
        String uuid = inbound.getUuid();
        log.warn("【死信队列】- 消息将被转移到死信队列，单号：{}，消息ID：{}，message：{}", transactionNo, uuid, inbound);

        try {
            //源消息保存到fail表中
            //messageFailService.save(inbound);
            //设置死信队列名称，顺序要放在保存之后，否则数据库中原始记录会被冲掉
            inbound.setMessageRouter(producerInterface.getDlqRouterKey());
            // 保存成功，事务成功提交后，会触发消息发送机制，消息会被发送到死信队列
            messageEventPublisher.publishEvent(new MqMessageEvent(transactionNo, inbound));
        } catch (Exception e) {
            // TODO: 此种情况下原始消息会从队列中被删除，但是数据没有被被持久化到数据库，消息会丢失，只能在日志中找到，需要结合ES日志关键字监控，如若频繁发生需要引起注意，进一步调查具体原因。
            log.error("OCMS_FATAL_EXCEPTION [DEAD LETTER] --> 将消费失败的消息保存到数据库出错，单号：{}, 消息：{}", transactionNo, inbound);
            log.error("OCMS_FATAL_EXCEPTION [DEAD LETTER] --> 将消费失败的消息保存到数据库出错，异常：", e);
        } finally {
            // 从原来队列删除
            String messageRouter = inbound.getMessageRouter();
            log.warn("【死信队列】- 从队列 {} ACK单号：{} 的消息 {}", messageRouter, transactionNo, inbound);

            //
            deadLetterRetryTemplate.execute((RetryCallback<Boolean, AmqpIOException>) context -> {
                try {
                    // 需要用ack从队列中删除此消息，如果用reject/nack同时requeue=false，会使消息进入此队列的死信队列
                    deleteFromQueue(channel, deliveryTag);
                    log.info("【死信队列】- ACK消息成功，单号：{}，消息ID：{}，deliveryTag：{}", transactionNo, inbound.getUuid(), deliveryTag);
                } catch (AmqpIOException ioEx) {
                    log.error("【死信队列】- ACK消息失败，单号：{}，消息ID：{}，deliveryTag：{}", transactionNo, inbound.getUuid(), deliveryTag);
                    throw ioEx;
                }

                return true;
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void retryCompensate(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> retryProcessor) {
        // 处理回滚逻辑
        retryProcessor.apply(inbound)
                .ifPresent(retryMessage -> publishEvent(inbound, retryMessage));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void temporaryHandleOCMSException(MqMessage inbound, Function<MqMessage, Optional<MqMessage[]>> ocmsExceptionProcessor) {
        ocmsExceptionProcessor.apply(inbound)
                .ifPresent(retryMessage -> publishEvent(inbound, retryMessage));
    }


//    /**
//     * TODO: 此方法从listener.BaseListener旧代码移植过来，后续需要评估做法合理性
//     *
//     * @param mqMessage
//     */
//    private void setBaseParam(MqMessage mqMessage) {
//        String body = mqMessage.getMessageBody();
//        if (StringUtils.isEmpty(body)) {
//            log.error("消费消息时设置公参失败，原因是messageBody为空！");
//            return;
//        }
//        BaseReqVo baseReqVo = null;
//        try {
//            if (JSON.isValid(body)){
//                if (StringUtils.startsWithIgnoreCase(body, "[")) {
//                    List<BaseReqVo> baseReqVos = JSON.parseArray(body, BaseReqVo.class);
//                    baseReqVo = baseReqVos.get(0);
//                } else if (StringUtils.startsWithIgnoreCase(body, "{")) {
//                    baseReqVo = JSON.parseObject(body, BaseReqVo.class);
//                }
//            }
//        } catch (Exception ignored) {
//            log.error("消费消息时设置公参异常：", ignored);
//            log.error("消费消息时设置公参失败，原因是解析messageBody异常！{}", ignored.getMessage());
//        }
//        PublicParam baseParamVo = new PublicParam();
//        baseParamVo.setUpdatedBy(GlobalConstant.DEFAULT_BY);
//        baseParamVo.setUpdatedUser(GlobalConstant.DEFAULT_USER);
//        if (!ObjectUtils.isEmpty(baseReqVo) && !ObjectUtils.isEmpty(baseReqVo.getUpdatedBy())) {
//            baseParamVo.setUpdatedBy(baseReqVo.getUpdatedBy());
//            baseParamVo.setUpdatedUser(baseReqVo.getUpdatedUser());
//        }
//        SessionUtil.set(baseParamVo);
//    }
}
