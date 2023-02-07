package com.my.message.repository;

import com.my.message.entity.MqMessageReceive;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * The interface Mq message receive repository.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.domain.repository
 * @ClassName MqMessageReceiveRepository
 * @Author Sean
 * @Date 2019 /3/8 15:42
 * @Description mq消息接收内容
 */
@Repository
public interface MqMessageReceiveRepository {

    /**
     * 根据messageId查询
     *
     * @param messageId the message id
     * @return mq message receive
     */
    MqMessageReceive findByMessageId(String messageId);

    /**
     * 保存消息
     *
     * @param message the message
     * @return 主键自增ID number
     */
    Number save(MqMessageReceive message);

    /**
     * Update original content int.
     *
     * @param messageId       the message id
     * @param originalContent the original content
     * @return int int
     */
    int updateOriginalContent(String messageId, String originalContent);

    void deleteByUpdatedDateBefore(String date,Long start);

    Long findCountByUpdatedDateBefore(Date updatedDate);

    List<MqMessageReceive> findByUpdatedDateBefore(Date updatedDate,Long start);
    List<MqMessageReceive> findByCreatedDateBefore(Date createdDate);
}
