/**
 * 描述:
 * 包名:com.ibm.sc.osp.sample.service.impl
 * 版本信息: 版本1.0
 * 日期:2019/2/1820:27
 * Copyright
 */
package com.my.message.service.impl;

import com.my.message.bean.MqMessage;
import com.my.message.entity.MqMessageSend;
import com.my.message.repository.MqMessageSendRepository;
import com.my.message.service.MqMessageSendService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

import static com.my.message.MsgConstants.*;


/**
 * The type Mq message send service.
 *
 * @describe:
 * @author: tool
 * @version: v1.0
 * @date 2019年03月07日 15:01:17 星期四
 */
@Service
public class MqMessageSendServiceImpl implements MqMessageSendService {
    //@Value("${sys.version}")
    private Double version;
    //    @Autowired
//    private MqChannelConfig mqChannelConfig;
    @Resource
    private MqMessageSendRepository mqMessageSendRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Number save(MqMessage mqMessage, String channelName, Object partitionKey) {
        MqMessageSend mqMessageSend = new MqMessageSend();
        mqMessageSend.setMessageId(mqMessage.getUuid());
        mqMessageSend.setSalesBillNo(mqMessage.getMessageId());
        mqMessageSend.setBusinessType(mqMessage.getMessageTitle());
        mqMessageSend.setContent(mqMessage.getMessageBody());
        /**
         * router字段被启用，存储partitionKey
         */
        mqMessageSend.setRouter(partitionKey == null ? mqMessage.getMessageId() : partitionKey.toString());
        mqMessageSend.setOutput(channelName);
        /**
         * 此字段暂时无用，先存0
         */
        mqMessageSend.setInstanceCount(0);
        mqMessageSend.setStatus(MQ_STATUS_INIT);
        //mqMessageSend.setVersion(version);
        mqMessageSend.setUpdatedDate(new Date());
        //mqMessageSend.setUpdatedBy(DEFAULT_BY);
        mqMessageSend.setUpdatedUser(DEFAULT_USER);
        mqMessageSend.setCreatedDate(new Date());
        //mqMessageSend.setCreatedBy(DEFAULT_BY);
        mqMessageSend.setCreatedUser(DEFAULT_USER);
        /**
         * 此字段暂时用作存储correlationId
         */
        mqMessageSend.setRemarks(mqMessage.getCorrelationId());

        return mqMessageSendRepository.save(mqMessageSend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void update(String messageId, Integer status, String originalContent) {
//        MqMessageSend messageSends = mqMessageSendRepository.findByMessageId(messageId);
//        if (Objects.isNull(messageSends)) {
//            throw new OcmsException("当前messageId不存在:" + messageId);
//        }
//        messageSends.setOrigContent(originalContent);
//        messageSends.setStatus(status);
//        messageSends.setUpdatedBy(GlobalConstant.DEFAULT_BY);
//        messageSends.setUpdatedUser(GlobalConstant.DEFAULT_BY);
//        messageSends.setUpdatedDate(new Date());
//        mqMessageSendRepository.save(messageSends);
        mqMessageSendRepository.updateStatus(messageId, status, originalContent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContent(String messageId,  String content) {
        mqMessageSendRepository.updateContent(messageId, content);
    }

    /**
     * 构建消息，先保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public MqMessage buildMqMessage(String billCode, String businessType, String messageContent, String channelName, MqMessage... inbound) {
        return buildMqMessage(billCode, businessType, messageContent, channelName, null, inbound);
    }

    /**
     * 构建消息，先保存
     * 支持有效期或延时消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public MqMessage buildMqMessage(String billCode, String businessType, String messageContent, String channelName, Integer delay, MqMessage... inbound) {
        return buildMqMessage(billCode, businessType, messageContent, channelName, delay, null, inbound);
    }

    /**
     * 构建消息，先保存
     *
     * @param billCode
     * @param businessType
     * @param messageContent
     * @param channelName
     * @param delay
     * @param partitionKey
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public MqMessage buildMqMessage(String billCode, String businessType, String messageContent, String channelName, Integer delay, Object partitionKey, MqMessage... inbound) {
        //MqMessage mqMessage = new MqMessage(billCode, businessType, messageContent, channelName, UniqueId.getUuid(), delay, partitionKey);
        // 根据通道获取实例总数
        //final int instanceCount = mqChannelConfig.getProducerPartitionCount(channelName);
        //mqMessage.setInstanceCount(instanceCount);
        MqMessage mqMessage = MqMessage.Builder.message()
                .transactionNo(billCode)
                .transactionNo(billCode)
                .messageTitle(businessType)
                .messageBody(messageContent)
                .messageRouter(channelName)
                .uuid(UUID.randomUUID().toString())
                .delay(delay)
                .partitionKey(partitionKey)
                .correlationId(ArrayUtils.isNotEmpty(inbound) ? inbound[0].getCorrelationId() : null)
                .build();
        // 保存消息
        this.save(mqMessage, channelName, partitionKey);
        //设置 消息分区总数
        return mqMessage;
    }

}
