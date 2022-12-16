package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
//@RibbonClients(value = {@RibbonClient(name = "stock-service",configuration = RibbonRandomRuleConfig.class)})
public class ConfigApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ConfigApplication.class, args);
        System.setProperty("spring.cloud.bootstrap.enabled","true");
        String age = context.getEnvironment().getProperty("age");
        System.out.println("age: "+  age);
    }

    //一般在config配置
//    @Bean
//    @LoadBalanced
//    public RestTemplate restTemplate(RestTemplateBuilder builder){
//        RestTemplate restTemplate = builder.build();
//        return restTemplate;
//    }
}