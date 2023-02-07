package com.demo.service;

import com.demo.config.FeignConfig;
import com.my.distribute.lock.annotation.LockKey;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 配置日志用于联调，生产不需要配置configuration = FeignConfig.class
 */
@FeignClient(name = "stock-service",path = "stock")
public interface StockFeignService {

    @RequestMapping("/reduce")
    public String findStock();

    @RequestMapping("/{id}")
    public String addStock(@PathVariable("id") Integer id);

    @RequestMapping("/{name}")
    public String addStock1(@PathVariable("name") String id);

    //public String addStock2(@LockKey String id);
}
