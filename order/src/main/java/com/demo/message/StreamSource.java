package com.demo.message;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 *
 * 发送通道 OUTPUT
 */
@Component
public interface StreamSource extends StreamChannel {

	/**
	 * 向死信队列入口发消息
	 *
	 * @return
	 */
	@Output(X_CRB_DLQ_OUTPUT)
	MessageChannel xRealDlqCrbOutput();

	@Output(ORDER_SETTLE_TO_STOCK_OUTPUT)
	MessageChannel orderSettleToAccount();

}
