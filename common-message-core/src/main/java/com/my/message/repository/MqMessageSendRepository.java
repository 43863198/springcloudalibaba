package com.my.message.repository;

import com.my.message.entity.MqMessageSend;

import java.util.Date;
import java.util.List;

/**
 * The interface Mq message send repository.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.domain.repository
 * @ClassName MqMessageSendRepository
 * @Author Sean
 * @Date 2019 /3/8 15:42
 * @Description mq消息发送内容
 */
public interface MqMessageSendRepository {

    /**
     * 根据messageId查询
     *
     * @param messageId the message id
     * @return mq message send
     */
    MqMessageSend findByMessageId(String messageId);

    /**
     * 根据状态查询
     *
     * @param status the status
     * @return list list
     */
    List<MqMessageSend> findByStatus(Integer status);

    /**
     * 根据状态查询传入时间之前的消息
     *
     * @param status      the status
     * @param createdDate the created date
     * @return list list
     */
    List<MqMessageSend> findByStatusAndCreatedDateBefore(Integer status, Date createdDate);

    /**
     * 保存消息
     *
     * @param message the message
     * @return number number
     */
    Number save(MqMessageSend message);

    /**
     * 更新消息状态
     *
     * @param messageId       the message id
     * @param status          the status
     * @param originalContent the original content
     * @return int int
     */
    int updateStatus(String messageId, Integer status, String originalContent);

    /**
     * 更新消息内容
     *
     * @param messageId       the message id
     * @param content   the content
     * @return int int
     */
    int updateContent(String messageId, String content);

    void deleteByUpdatedDateBefore(String date,Long start);

    List<MqMessageSend> findByStatusAndUpdatedDateBefore(Integer status, Date updatedDate,Long start);
    Long findCountByStatusAndUpdatedDateBefore(Integer status, Date updatedDate);

}
