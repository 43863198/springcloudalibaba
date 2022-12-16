package com.demo.config;

import com.demo.interceptor.feign.CustomerInterceptionFeign;
import feign.Logger;
import feign.Request;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局配置 ：配置类上加configuration
 * 局部配置 ： 不需要配置@Configuration ，在feign的属性@FeignClient(name = "stock-service",path = "stock",configuration = FeignConfig.class)
 */

@Configuration
public class FeignConfig {
    /**
     *
     */
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.HEADERS;
    }

    /**
     * 基于代码的超时时间配置
     * @return
     */
    @Bean
    public Request.Options options(){
        return new Request.Options(5000,10000);
    }

    /**
     * feign拦截器bean
     * @return
     */
    @Bean
    public CustomerInterceptionFeign basicAuthRequestInterceptor(){
        return new CustomerInterceptionFeign();
    }

//    @Bean
//    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(){
//        return new BasicAuthRequestInterceptor("xxx","xxx");
//    }
}
