/**
 * 描述:
 * 包名:com.ibm.sc.osp.sample.service.impl
 * 版本信息: 版本1.0
 * 日期:2019/2/1820:27
 * Copyright
 */
package com.my.message.service.impl;

import com.my.message.MsgConstants;
import com.my.message.bean.MqMessage;
import com.my.message.config.MqChannelConfigInterface;
import com.my.message.entity.MqMessageFail;
import com.my.message.repository.MqMessageFailRepository;
import com.my.message.service.MqMessageFailService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * The type Mq message fail service.
 *
 * @describe:
 * @author: tool
 * @version: v1.0
 * @date 2019年03月07日 15:01:17 星期四
 */
@Service
public class MqMessageFailServiceImpl implements MqMessageFailService {
    //@Value("${sys.version}")
    private Double version;

    @Autowired
    private MqChannelConfigInterface mqChannelConfigInterface;

    @Autowired
    private MqMessageFailRepository mqMessageFailRepository;

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED)
    public void save(MqMessage mqMessage) {
        MqMessageFail mqMessageFail = new MqMessageFail();
        mqMessageFail.setMessageId(mqMessage.getUuid());
        mqMessageFail.setSalesBillNo(mqMessage.getMessageId());
        mqMessageFail.setBusinessType(mqMessage.getMessageTitle());
        mqMessageFail.setRouter(!Objects.isNull(mqMessage.getPartitionKey()) ? mqMessage.getPartitionKey().toString() : mqMessage.getMessageId());
        mqMessageFail.setContent(mqMessage.getMessageBody());
        mqMessageFail.setInput(mqMessage.getMessageRouter());
        mqMessageFail.setStatus(MsgConstants.MQ_STATUS_INIT);
        mqMessageFail.setInstanceIndex(mqChannelConfigInterface.getConsumerInstanceIndex(mqMessage.getMessageRouter().replace("OUTPUT", "INPUT")));
        //mqMessageFail.setVersion(version);
        mqMessageFail.setCreatedDate(new Date()); // dead time
        mqMessageFail.setRemarks(mqMessage.getCorrelationId());
        mqMessageFailRepository.save(mqMessageFail);
    }

    @Override
    public void deleteByUpdateDate(String date) {
    }
}