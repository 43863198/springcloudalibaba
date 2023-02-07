package com.my.message.repository;

import com.my.message.entity.MqMessageFail;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * The interface Mq message fail repository.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.domain.repository
 * @ClassName MqMessageSendRepository
 * @Author Sean
 * @Date 2019 /3/8 15:42
 * @Description mq消息发送内容
 */
@Repository
public interface MqMessageFailRepository {

//    /**
//     * 根据messageId查询
//     * @param messageId
//     * @return
//     */
//    List<MqMessageFail> findByMessageId(String messageId);

    /**
     * 保存消息
     *
     * @param message the message
     * @return number number
     */
    Number save(MqMessageFail message);

    void deleteByUpdatedDateBefore(String date,Long start);

    Long findCountByUpdatedDateBefore(Date updatedDate);

    List<MqMessageFail> findByUpdatedDateBefore(Date createdDate,Long start);

    List<MqMessageFail> findByCreatedDateBefore(Date createdDate);
}
