package com.demo.controller;

import com.demo.service.StockFeignService;
import com.demo.service.impl.OrderServiceImpl;
import com.my.distribute.LockType;
import com.my.distribute.lock.annotation.DistributedLock;
import com.my.distribute.lock.annotation.LockKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/order")
public class OrderController {

    //替换为openfeign
//    @Autowired
//    RestTemplate restTemplate;

    @Autowired
    StockFeignService stockFeignService;

    @Autowired
    OrderServiceImpl orderService;

    @RequestMapping("/add")
    public String add(@LockKey String name){
       // System.out.println("下单成功:" + color);
        String key1 = "123";
        String response = stockFeignService.addStock1(name);
        //String response = restTemplate.getForObject("http://stock-service/stock/reduce", String.class);
        return "success";
    }

    @RequestMapping("/orderConfirm")
    public String confirm(String name){

        Set lockKeys = new HashSet();
        lockKeys.add("lock1");
        lockKeys.add("lock2");

        String response = orderService.confirmOrder(name, lockKeys);
        return "success" + response;
    }
}
