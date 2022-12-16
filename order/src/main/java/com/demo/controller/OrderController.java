package com.demo.controller;

import com.demo.service.StockFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    //替换为openfeign
//    @Autowired
//    RestTemplate restTemplate;

    @Autowired
    StockFeignService stockFeignService;

    @RequestMapping("/add")
    public String add(@RequestHeader("X-request-color") String color){
        System.out.println("下单成功:" + color);
        String response = stockFeignService.addStock(1);
        //String response = restTemplate.getForObject("http://stock-service/stock/reduce", String.class);
        return "success" + response;
    }
}
