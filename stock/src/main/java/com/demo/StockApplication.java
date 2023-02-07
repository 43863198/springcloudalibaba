package com.demo;

import com.my.distribute.lock.annotation.EnableDistributedLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDistributedLock
@Import(value = com.my.redis.RedisConfig.class)
@ComponentScan({"com.my","com.demo"})
public class StockApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class,args);
    }
}