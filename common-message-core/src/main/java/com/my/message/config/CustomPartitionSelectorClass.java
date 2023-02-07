package com.my.message.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.PartitionSelectorStrategy;

/**
 * The type Custom partition selector class.
 */
@Slf4j
public class CustomPartitionSelectorClass implements PartitionSelectorStrategy {


    @Override
    public int selectPartition(Object key, int partitionCount) {
        try {
            if (key instanceof Long) {
                return Math.abs(Math.toIntExact(((Long) key % partitionCount)));
            } else if (key instanceof Integer) {
                return Math.abs((Integer) key % partitionCount);
            } else {
                log.info("send message by customPartitionSelectorClass");
                int hashCode = key.hashCode();
                if (hashCode == Integer.MIN_VALUE) {
                    hashCode = 0;
                }
                return Math.abs(hashCode % partitionCount);
            }
        } catch (Exception e) {
            log.error("选择分片时发生异常，key -> {}, partitionCount -> {}, 异常：{}", key, partitionCount, e);
        }
        return 0;
    }
}
