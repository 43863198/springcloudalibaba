package com.my.message.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * The type Mq message receive.
 *
 * @Project ocms -order
 * @PackageName com.crb.ocms.order.domain.entity
 * @ClassName
 * @Author Xiao
 * @Date 2019 -03-15 11:11:10
 * @Description 消息队列接收表实体
 */
@Setter
@Getter
@ToString
@Table ( name ="mq_message_receive")
public class MqMessageReceive extends BaseEntity {

	private static final long serialVersionUID =  307227294438980090L;

	/**
	 * 消息ID
	 */
   	@Column(name = "MESSAGE_ID" )
	private String messageId;
	/**
	 * 业务单据号
	 */
	@Column(name = "SALES_BILL_NO" )
	private String salesBillNo;
	/**
	 * 业务类型
	 */
   	@Column(name = "BUSINESS_TYPE" )
	private String businessType;

	/**
	 * 路由器（目前没规范，作为保留字段）
	 */
	@Column(name = "ROUTER" )
	private String router;

	/**
	 * 消息通道名称<channelName>
	 */
   	@Column(name = "INPUT" )
	private String input;

	/**
	 * 消息内容
	 */
	@Column(name = "CONTENT" )
	private String content;

	/**
	 * 原始消息内容
	 */
	@Column(name = "ORIGCONTENT")
	private String originalContent;

	/**
	 * 分区索引
	 */
	@Column(name = "INSTANCE_INDEX" )
	private Integer instanceIndex;

}
