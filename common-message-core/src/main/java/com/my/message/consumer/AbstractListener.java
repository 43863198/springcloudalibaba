package com.my.message.consumer;

import com.my.message.bean.MqMessage;
import com.my.message.exception.DuplicateMessageException;
import com.my.message.exception.OcmsException;
import com.my.message.exception.RecoverableOcmsException;
import com.my.message.exception.RetryableOcmsException;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.messaging.Message;
import org.springframework.transaction.TransactionSystemException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.my.message.MsgConstants.MESSAGE_ID;
import static com.my.message.MsgConstants.X_DEATH_COUNT;
import static com.my.message.util.MessageUtils.*;

/**
 * Class AbstractConsumer
 *
 * @author linyang
 * @date 2020 /8/22
 */
public abstract class AbstractListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractListener.class);
    /**
     * 重试次数
     * 注意：确保配置文件有@Value中的定义，否则启动报错
     */
    @Value("${MAX_CONSUMER_COUNT:#{1}}")
    private Long maxRetryAttempts;

    /**
     * The Handler.
     */
    @Autowired
    protected MessageHandler handler;


    /**
     * Gets processor.
     *
     * @return processor processor
     */
    protected abstract Function<MqMessage, Optional<MqMessage[]>> getProcessor();

    /**
     * Get retry processor function.
     *
     * @return function function
     */
    protected Function<MqMessage, Optional<MqMessage[]>> getRetryProcessor() {
        return message -> Optional.empty();
    }

    /**
     * Get old ocms exception processor function.
     *
     * @return function function
     */
    protected Function<MqMessage, Optional<MqMessage[]>> getOCMSExceptionProcessor() {
        return message -> Optional.empty();
    }

    /**
     * Get old ocms exception is retry able.
     * implements this method if need retry customs message.
     * default false
     * FIXME 返回true更合乎以前的代码逻辑，但是会出现一些重复消费导致数据一致性出错的情况。
     * FIXME 有些场景一旦出错直接建议人工介入，所以目前的情况是使用OcmsException抛出的异常，不会进行重试。
     * FIXME 后续根据特定的场景抛出相应的重试/回滚异常
     *
     * @return customs message retry able.
     */
    protected boolean isOCMSExceptionRetryable() {
        return false;
    }

    /**
     * Get rollback processor function.
     *
     * @return function function
     */
    protected Function<MqMessage, Optional<MqMessage[]>> getRollbackProcessor() {
        return message -> Optional.empty();
    }


    /**
     * Gets max retry attempts.
     * 从配置文件中获取默认重试次数，如果需要，子类可以根据情况覆盖此方法。
     *
     * @return the max retry attempts
     */
    protected Long getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    /**
     * 事务性监听入口 target需要手动指定
     *
     * @param message      the message
     * @param channel      the channel
     * @param deliveryTag  the delivery tag
     * @param inputChannel the input channel
     * @param xDeathHeader 死信队列相关信息
     * @throws Exception the exception
     */
    protected void createTxListener(Message<MqMessage> message, Channel channel, Long deliveryTag, String inputChannel, Map<String, ?> xDeathHeader) throws Exception {
        log.info("------ [{}] 通道的原始报文：------ \n {}", inputChannel, message);

        final MqMessage payload = message.getPayload();
        String uuid = Optional.ofNullable(payload.getUuid())
                .orElse((String) message.getHeaders().get(MESSAGE_ID));
        if (StringUtils.isBlank(uuid)) {
            log.error("消息唯一ID为空，无法处理，消息：{}", message);
            moveToDlq(message, channel, deliveryTag);
            return;
        }

        final String transactionNo = payload.getMessageId();
        log.info("------ 业务编号：{}，消息ID：{}------ ", transactionNo, uuid);

        if (hasExceededRetryCount(payload, xDeathHeader)) {
            // 进入死信队列
            moveToDlq(message, channel, deliveryTag);
        } else {
            internalProcess(message, channel, deliveryTag);
        }
    }

//    /**
//     * Global error channel for handling all errors
//     * 目前为实验性质，后续会在试验成功后启用
//     * @param message
//     */
//    @StreamListener("errorChannel")
//    public void error(Message<?> message) {
//        System.out.println("Handling ERROR: " + message);
//    }

    private void internalProcess(Message<MqMessage> message, Channel channel, Long deliveryTag) {
        final MqMessage payload = message.getPayload();
        final String transactionNo = payload.getMessageId();

        try {
            // 消费消息，进行业务逻辑处理
            handler.handle(payload, getProcessor());
            // 消费成功，手动确认
            immediateAckMessage(channel, deliveryTag);
            log.info("确认消费成功，单号：{}，消息：{}", transactionNo, payload);
        } catch (DuplicateMessageException duplicateMsgEx) {
            // 消息重复消费, 直接确认消费掉消息
            log.warn("消息重复消费，单号：{}，消息：{}，异常；{}", transactionNo, payload, duplicateMsgEx);
            deleteFromQueue(channel, deliveryTag);
//        } catch (ObjectOptimisticLockingFailureException optimisticLockEx) {
//            // 乐观锁并发冲突，让消息进入waiting queue，确保再次消费时在新的事务里进行
//            log.warn("乐观锁并发冲突，单号：{}，消息：{}，异常；{}", transactionNo, payload, optimisticLockEx);
//            rejectAndMoveToWaitingQueue(channel, deliveryTag);
        } catch (CannotAcquireLockException | TransactionSystemException deadlockEx) {
            // 死锁或数据冲突，让消息进入waiting queue，进行重试
            log.warn("死锁或数据冲突，单号：{}，消息：{}，异常；{}", transactionNo, payload, deadlockEx);
            rejectAndMoveToWaitingQueue(channel, deliveryTag);
        } catch (PessimisticLockingFailureException deadlockEx) {
            // 死锁冲突，让消息进入waiting queue，进行重试
            log.warn("死锁冲突，单号：{}，消息：{}，异常；{}", transactionNo, payload, deadlockEx);
            rejectAndMoveToWaitingQueue(channel, deliveryTag);
        } catch (RetryableOcmsException retryableEx) {
            // 业务异常
            final String desc = retryableEx.getMsg().getReturnDesc();
            log.error("消费消息时发生可重试业务异常, 单号：{}，业务信息：{}", transactionNo, desc);
            log.error("消费消息时发生可重试业务异常, 异常：", retryableEx);
            handleRetryAbleOCMSException(message, channel, deliveryTag, retryableEx);
        } catch (RecoverableOcmsException recoverableEx) {
            // 业务异常
            final String desc = recoverableEx.getMsg().getReturnDesc();
            log.error("消费消息时发生可回滚业务异常, 单号：{}，业务信息：{}", transactionNo, desc);
            log.error("消费消息时发生可回滚业务异常, 异常：{}", recoverableEx);
            handleRecoverableOCMSException(message, channel, deliveryTag, recoverableEx);
        } catch (OcmsException bizEx) {
            // FIXME: 临时处理方案，后续不应该存在此分支
            // 业务异常
            final String desc = bizEx.getMsg().getReturnDesc();
            log.error("消费消息时发生业务异常, 单号：{}，业务信息：{}", transactionNo, desc);
            log.error("消费消息时发生业务异常, 异常：{}", bizEx);
            handleOCMSException(message, channel, deliveryTag, bizEx);
        } catch (Exception unknownEx) {
            log.error("消费消息时发生未知异常, 消息将被放入死信队列，单号：{}，消息：{}", transactionNo, message);
            log.error("消费消息时发生未知异常, 消息将被放入死信队列，异常：{}", unknownEx);
            // 未知异常，不确定该如何处理，放入死信队列
            moveToDlq(message, channel, deliveryTag);
        } finally {
            //housekeeping();
        }
    }


//    private void housekeeping() {
//        // 清理公参
//        // todo: 旧代码遗留方法
//        SessionUtil.remove();
//        doHousekeeping();
//    }

    /**
     * 扩展点，子类可以额外清理其他资源
     */
    protected void doHousekeeping() {
    }

    /**
     * 是否已经超最大重试次数
     *
     * @param payload
     * @param xDeathHeader rabbitmq的x-death header
     * @return
     */
    private boolean hasExceededRetryCount(MqMessage payload, Map<String, ?> xDeathHeader) {
        log.info("死信队列标记 xDeathHeader = {}", xDeathHeader);
        if (xDeathHeader != null && xDeathHeader.get(X_DEATH_COUNT) != null) {
            String transactionNo = payload.getMessageId();
            String messageId = payload.getUuid();
            Long count = (Long) xDeathHeader.get(X_DEATH_COUNT);
            log.warn("单号：{}, 消息ID：{}, 第{}次进行重试。", transactionNo, messageId, count);
            if (count >= getMaxRetryAttempts()) {
                log.warn("单号：{}, 消息ID：{}, 已经超过最大重试{}次的限制。", transactionNo, messageId, getMaxRetryAttempts());
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否是最后一次重试
     *
     * @param xDeathHeader rabbitmq的x-death header
     * @return
     */
    protected boolean hasLastRetryCount(Map<String, ?> xDeathHeader) {
        log.info("死信队列标记 xDeathHeader = {}", xDeathHeader);
        if (xDeathHeader != null && xDeathHeader.get(X_DEATH_COUNT) != null) {
            Long count = (Long) xDeathHeader.get(X_DEATH_COUNT);
            if (count == getMaxRetryAttempts() - 1) {
                log.info("当前是最后一次重试");
                return true;
            }
        }

        return false;
    }

    /**
     * 处理业务异常
     *
     * @param message     消息
     * @param channel     rabbitmq通道
     * @param deliveryTag rabbitmq消息位置标记
     * @param bizEx       业务异常
     */
    private void handleOCMSException(Message<MqMessage> message, Channel channel, Long deliveryTag, OcmsException bizEx) {
        final MqMessage payload = message.getPayload();
        final String transactionNo = payload.getMessageId();
        final String desc = bizEx.getMsg() != null ? bizEx.getMsg().getReturnDesc() : null;
        final String code = bizEx.getMsg() != null ? bizEx.getMsg().getReturnCode() : null;
        // 此方法为兼容以前业务异常情况，兼容重试或者移除到死信队列功能
        log.warn("消费消息时发生业务异常, 兼容重试或者移除到死信队列功能，单号：{}，业务信息：{}，消息：{}", transactionNo, desc, message);
        log.warn("消费消息时发生业务异常, 兼容重试或者移除到死信队列功能，异常：", bizEx);
        try {
            payload.setExceptionDesc(desc);
            payload.setExceptionCode(code);
            //getOCMSExceptionProcessor().apply(payload).ifPresent(retryMessage -> handler.publishEvent(payload, retryMessage));
            handler.temporaryHandleOCMSException(payload, getOCMSExceptionProcessor());
            if (isOCMSExceptionRetryable()) {
                rejectAndMoveToWaitingQueue(channel, deliveryTag);
            } else {
                moveToDlq(message, channel, deliveryTag);
            }
        } catch (Exception e) {
            log.error("消息异常时补偿机制处理也发送异常：", e);
            moveToDlq(message, channel, deliveryTag);
        }
    }

    private void handleRecoverableOCMSException(Message<MqMessage> message, Channel channel, Long deliveryTag, RecoverableOcmsException recoverableEx) {
        final MqMessage payload = message.getPayload();
        final String transactionNo = payload.getMessageId();
        final String desc = recoverableEx.getMsg() != null ? recoverableEx.getMsg().getReturnDesc() : null;
        final String code = recoverableEx.getMsg() != null ? recoverableEx.getMsg().getReturnCode() : null;

        if (recoverableEx.isRecoverable()) {
            log.info("此异常可以回滚，开始回滚...");
            try {
                // 处理回滚
                payload.setExceptionDesc(desc);
                payload.setExceptionCode(code);
                handler.rollback(payload, getRollbackProcessor());
                // 回滚完成之后，消息消费掉，直接结束
                immediateAckMessage(channel, deliveryTag);
            } catch (Exception rollbackEx) {
                // todo:此种情况消息拒绝失败，消息不会进入等待队列，尝试直接进入死信队列
                // 回滚时发生异常，记录此异常，消息进入死信队列，需要人工干预
                log.error("回滚时发生异常，消息将被放入死信队列，单号：{}，消息：{}，异常；{}", transactionNo, message, rollbackEx);
                moveToDlq(message, channel, deliveryTag);
            }
        } else {
            // FIXME: 不可回滚的异常是否还要重试？多次尝试是否会产生重复扣减？暂不重试，移入死信队列
            log.warn("此异常不可以回滚，将不进行业务回滚操作：{}", recoverableEx);
            // 回滚完成之后，消息进行wait queue，等待下次重试
//            try {
            //rejectAndMoveToWaitingQueue(channel, deliveryTag);
            moveToDlq(message, channel, deliveryTag);
//            } catch (AmqpIOException e) {
//                // todo:此种情况消息拒绝失败，消息不会进入等待队列
//                log.error("可回滚的消息拒绝失败：", e);
//            }
        }
    }

    private void handleRetryAbleOCMSException(Message<MqMessage> message, Channel channel, Long deliveryTag, RetryableOcmsException retryableEx) {
        final MqMessage payload = message.getPayload();
        final String transactionNo = payload.getMessageId();
        final String desc = retryableEx.getMsg() != null ? retryableEx.getMsg().getReturnDesc() : null;
        final String code = retryableEx.getMsg() != null ? retryableEx.getMsg().getReturnCode() : null;

        if (retryableEx.isRetryable()) {
            // 此消息可以重试，让消息进入waiting queue，进行重试
            log.warn("消费消息时发生业务异常, 可以重试，将被放入waiting queue，单号：{}，业务信息：{}，消息：{}，异常：{}", transactionNo, desc, message, retryableEx);
            try {
                handler.retryCompensate(payload, getRetryProcessor());
                rejectAndMoveToWaitingQueue(channel, deliveryTag);
            } catch (Exception e) {
                // todo:此种情况消息拒绝失败，消息不会进入等待队列
                log.error("可重试的消息拒绝失败：", e);
                // FIXME 如果在处理重试逻辑时出错，消息是否需要进入死信队列？
                moveToDlq(message, channel, deliveryTag);
            }
        } else {
            // 此消息不可重试，将被放入死信队列
            log.warn("消费消息时发生业务异常，此异常不可重试，消息将被放入死信队列，单号：{}，业务信息：{}，消息：{}，异常；{}", transactionNo, desc, message, retryableEx);
            moveToDlq(message, channel, deliveryTag);
        }
    }

    /**
     * 将消息放入死信队列
     *
     * @param message     消息
     * @param channel     rabbitmq通道
     * @param deliveryTag rabbitmq消息位置标记
     */
    private void moveToDlq(Message<MqMessage> message, Channel channel, Long deliveryTag) {
        MqMessage payload = message.getPayload();
        handler.deadLetter(payload, channel, deliveryTag);
    }
}
