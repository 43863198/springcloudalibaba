package com.demo.message;

import com.my.message.config.MqChannelConfigInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Project crb
 * @PackageName com.crb.ocms.price.message.config
 * @ClassName MqConfig
 * @Author yangshuo
 * @Date 2019/4/27 20:43
 * @Description 消息通道配置
 */
@Slf4j
@Component
public class MqChannelConfig implements MqChannelConfigInterface {

    // producer.partitionCount 请放在该区域

    @Value("${spring.cloud.stream.bindings.ORDER_SETTLE_TO_STOCK_OUTPUT.producer.partitionCount:-1}")
    private Integer orderSettleToStockPartitionCount;

    // consumer.instanceIndex 请放在该区域

    @Value("${spring.cloud.stream.bindings.STOCK_REPLY_TO_ORDER_INPUT.consumer.instanceIndex:-1}")
    private Integer stockReplyToOrderPartitionCount;


    /**
     * 根据通道获取发送端实例总数 producer.partitionCount
     */
    public Integer getProducerPartitionCount(String channelName) {
        switch (channelName) {
            case StreamChannel.ORDER_SETTLE_TO_STOCK_OUTPUT:
                return orderSettleToStockPartitionCount;
            default:
                log.error("----- Unknown producer.partitionCount channelName={}", channelName);
                return -1;
        }
    }
    
    /**
     * 根据通道获取消费端实例索引 consumer.instanceIndex
     */
    public Integer getConsumerInstanceIndex(String channelName) {
        switch (channelName) {
            case StreamChannel.STOCK_REPLY_TO_ORDER_INPUT:
                return stockReplyToOrderPartitionCount;
            default:
                log.error("----- Unknown consumer.instanceIndex channelName={}", channelName);
                return -1;
        }
    }
    
}
