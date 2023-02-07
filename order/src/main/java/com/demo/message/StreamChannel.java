package com.demo.message;


/**

 * @description 各通道定义
 */
public interface StreamChannel {

	/**
	 * 命名规则:
	 *  系统内部传播 以 CRB_ 开头 + 微服务名称+ INPUT/OUTPUT区分入 和 出
	 *  单词大写,下划线分词 ,申明和值保持一致
	 */
	String X_CRB_DLQ_OUTPUT = "X_CRB_DLQ_OUTPUT";

	String ORDER_SETTLE_TO_STOCK_OUTPUT="ORDER_SETTLE_TO_STOCK_OUTPUT";

	String STOCK_REPLY_TO_ORDER_INPUT = "STOCK_REPLY_TO_ORDER_INPUT";

}
