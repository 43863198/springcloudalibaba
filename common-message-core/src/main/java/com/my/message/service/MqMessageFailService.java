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
 * The interface Mq message fail service.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.service
 * @ClassName MqMessageFailService
 * @Author Sean
 * @Date 2019年03月07日 14:22:31 星期四
 * @Description 表mq_message_fail的操作
 */
public interface MqMessageFailService {

    /**
     * 保存发送消息
     *
     * @param mqMessage the mq message
     */
    void save(MqMessage mqMessage);

    /**
     * 删除消息通过更新时间
     * @param date
     */
    void deleteByUpdateDate(String date);

}
