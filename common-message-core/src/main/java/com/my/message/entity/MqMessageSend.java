package com.my.message.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * The type Mq message send.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.domain.entity
 * @ClassName MqMessageSend
 * @Author Xiao
 * @Date 2019 -03-15 11:11:10
 * @Description 消息队列发送表实体
 */
@Setter
@Getter
@ToString
@Table ( name ="mq_message_send")
public class MqMessageSend extends BaseEntity{

	private static final long serialVersionUID =  2192680214316689226L;

	/**
	 * 消息ID
	 */
   	@Column(name = "MESSAGE_ID" )
	private String messageId;

	/**
	 * 业务类型
	 */
   	@Column(name = "BUSINESS_TYPE" )
	private String businessType;

	/**
	 * 状态(0暂存1已发送2已消费)
	 */
   	@Column(name = "STATUS" )
	private Integer status;

	/**
	 * 路由器
	 * 目前被用作存储partitionKey
	 */
	@Column(name = "ROUTER" )
	private String router;

	/**
	 * 消息通道名称<channelName>
	 */
   	@Column(name = "OUTPUT" )
	private String output;

	/**
	 * 消息内容
	 */
   	@Column(name = "CONTENT" )
	private String content;

	/**
	 * 原始消息内容
	 */
	@Column(name = "ORIGCONTENT")
	private String origContent;

   	/**
	 * 业务单据号
	 */
   	@Column(name = "SALES_BILL_NO" )
	private String salesBillNo;

	/**
	 * 消息实例总数
	 */
	@Column(name="INSTANCE_COUNT")
   	private Integer instanceCount;

}
