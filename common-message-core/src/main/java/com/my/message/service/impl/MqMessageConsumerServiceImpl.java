/**
 * 描述:
 * 包名:com.ibm.sc.osp.sample.service.impl
 * 版本信息: 版本1.0
 * 日期:2019/2/1820:27
 * Copyright
 */
package com.my.message.service.impl;

import com.my.message.bean.MqMessage;
import com.my.message.config.MqChannelConfigInterface;
import com.my.message.entity.MqMessageReceive;
import com.my.message.repository.MqMessageReceiveRepository;
import com.my.message.service.MqMessageConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * The type Mq message consumer service.
 *
 * @Project ocms -demo
 * @PackageName com.crb.ocms.order.domain.controller
 * @ClassName IndexController
 * @Author Tools
 * @Date 2019年03月07日 14:22:43 星期四
 * @Description 表order_status的操作
 */
@Slf4j
@Service
public class MqMessageConsumerServiceImpl implements MqMessageConsumerService {

    //@Value("${sys.version}")
    private Double version;
    @Autowired
    private MqChannelConfigInterface mqChannelConfigInterface;
    @Autowired
    private MqMessageReceiveRepository mqMessageReceiveRepository;

    /**
     * 消息判重方法，必须在事务中执行，否则抛出异常
     *
     * @param mqMessage
     * @return
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED)
    public boolean isDuplicate(MqMessage mqMessage) {
        try {
            save(mqMessage);
        } catch (DuplicateKeyException duplicateKeyException) {
            logException(mqMessage, duplicateKeyException);
            return true;
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            logException(mqMessage, dataIntegrityViolationException);
            return true;
        }

        return false;
    }

    private void logException(MqMessage mqMessage, Exception duplicateKeyEx) {
        String transactionNo = mqMessage.getMessageId();
        String uuid = mqMessage.getUuid();
        log.warn("消息重复消费，单号：{}，消息ID：{}，消息：{}，异常；{}", transactionNo, uuid, mqMessage, duplicateKeyEx);
    }

    /**
     * 保存接收到的消息
     *
     * @param mqMessage
     */
    @Override
    public void save(MqMessage mqMessage) {
        MqMessageReceive mqMessageReceive = new MqMessageReceive();
        mqMessageReceive.setMessageId(mqMessage.getUuid());
        mqMessageReceive.setSalesBillNo(mqMessage.getMessageId());
        mqMessageReceive.setBusinessType(mqMessage.getMessageTitle());
        mqMessageReceive.setRouter(!Objects.isNull(mqMessage.getPartitionKey())?mqMessage.getPartitionKey().toString():mqMessage.getMessageId());
        mqMessageReceive.setContent(mqMessage.getMessageBody());
        mqMessageReceive.setInput(mqMessage.getMessageRouter());
        mqMessageReceive.setInstanceIndex(mqChannelConfigInterface.getConsumerInstanceIndex(mqMessage.getMessageRouter().replace("OUTPUT", "INPUT")));
        //mqMessageReceive.setVersion(version);
        mqMessageReceive.setUpdatedDate(new Date());
        //mqMessageReceive.setUpdatedBy(GlobalConstant.DEFAULT_BY);
        //mqMessageReceive.setUpdatedUser(GlobalConstant.DEFAULT_USER);
        mqMessageReceive.setCreatedDate(new Date());
        //mqMessageReceive.setCreatedBy(GlobalConstant.DEFAULT_BY);
        //mqMessageReceive.setCreatedUser(GlobalConstant.DEFAULT_USER);
        mqMessageReceive.setRemarks(mqMessage.getCorrelationId());
        mqMessageReceiveRepository.save(mqMessageReceive);
    }
}
