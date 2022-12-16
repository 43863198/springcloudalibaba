package com.demo.interceptor.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

public class CustomerInterceptionFeign implements RequestInterceptor {
    Logger logger  = LoggerFactory.getLogger(CustomerInterceptionFeign.class);

    /**
     * 可以做一些header或者body的修改
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        logger.info("自定义feign拦截器");
        requestTemplate.header("xxx","xxx");

    }

}
