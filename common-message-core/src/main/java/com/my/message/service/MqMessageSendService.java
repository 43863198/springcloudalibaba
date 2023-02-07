/**
* 描述:
* 包名:com.crb.ocms.order.service
* 版本信息: 版本1.0
* 日期:2019年03月07日 14:22:31 星期四
* Copyright
*/
package com.my.message.service;

import com.my.message.bean.MqMessage;

/**
 * The interface Mq message send service.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.service
 * @ClassName MqMessageSendService
 * @Author Sean
 * @Date 2019年03月07日 14:22:31 星期四
 * @Description 表mq_message_send的操作
 */
public interface MqMessageSendService {

    /**
     * 保存发送消息
     *
     * @param mqMessage    the mq message
     * @param channelName  the channel name
     * @param partitionKey the partition key
     * @return the number
     */
    Number save(MqMessage mqMessage, String channelName, Object partitionKey);

    /**
     * 修改消息状态
     *
     * @param messageId       the message id
     * @param status          the status
     * @param originalContent the original content
     */
    void update(String messageId, Integer status, String originalContent);

    /**
     * 兼容price服务个性化需求
     * 增加通过messageId更新content内容的方法
     * @param messageId
     * @param content
     */
    void updateContent(String messageId,  String content);
    /**
     * 构建消息，先保存
     *
     * @param billCode       the bill code
     * @param businessType   the business type
     * @param messageContent the message content
     * @param channelName    the channel name
     * @param inbound        the inbound
     * @return mq message
     */
    MqMessage buildMqMessage(String billCode, String businessType, String messageContent, String channelName, MqMessage... inbound);

    /**
     * 支持有效期或延时消息
     *
     * @param billCode       the bill code
     * @param businessType   the business type
     * @param messageContent the message content
     * @param channelName    the channel name
     * @param delay          the delay
     * @param inbound        the inbound
     * @return mq message
     */
    MqMessage buildMqMessage(String billCode, String businessType, String messageContent, String channelName, Integer delay, MqMessage... inbound);

    /**
     * 支持有效期或延时消息及消息分片
     *
     * @param billCode       the bill code
     * @param businessType   the business type
     * @param messageContent the message content
     * @param channelName    the channel name
     * @param delay          the delay
     * @param partitionIndex the partition index
     * @param inbound        the inbound
     * @return mq message
     */
    MqMessage buildMqMessage(String billCode, String businessType, String messageContent, String channelName, Integer delay, Object partitionIndex, MqMessage... inbound);
}
