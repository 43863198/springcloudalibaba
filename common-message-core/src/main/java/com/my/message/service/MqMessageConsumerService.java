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
 * The interface Mq message consumer service.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.service
 * @ClassName MqMessageReceiveService
 * @Author Sean
 * @Date 2019年03月07日 14:22:31 星期四
 * @Description 表mq_message_receive的操作
 */
public interface MqMessageConsumerService {

    /**
     * 保存发送消息
     *
     * @param mqMessage the mq message
     */
    void save(MqMessage mqMessage);

//    /**
//     * 保存原始报文
//     * @param messageId
//     * @param originalContent
//     */
//    void update(String messageId, String originalContent);


    /**
     * Is duplicate boolean.
     *
     * @param mqMessage the mq message
     * @return boolean boolean
     */
    boolean isDuplicate(MqMessage mqMessage);
}
