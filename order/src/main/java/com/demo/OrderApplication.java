package com.demo;

import com.my.distribute.lock.annotation.EnableDistributedLock;
import com.my.message.annotation.EnableMessageCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
@EnableFeignClients
@RefreshScope
@EnableDistributedLock
@EnableMessageCore
//@RibbonClients(value = {@RibbonClient(name = "stock-service",configuration = RibbonRandomRuleConfig.class)})
public class OrderApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrderApplication.class, args);
        System.setProperty("spring.cloud.bootstrap.enabled","true");
            String age = context.getEnvironment().getProperty("age");
            String name = context.getEnvironment().getProperty("name");
            System.out.println("age: "+  age + "name:" + name);
        }

    //一般在config配置
//    @Bean
//    @LoadBalanced
//    public RestTemplate restTemplate(RestTemplateBuilder builder){
//        RestTemplate restTemplate = builder.build();
//        return restTemplate;
//    }
}