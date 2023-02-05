package com.demo.controller;

import com.demo.service.impl.StockServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    StockServiceImpl stockService;
    @RequestMapping("/reduce")
    public String reduce(){
        Set lockKeys = new HashSet();
        lockKeys.add("lock1");
        lockKeys.add("lock2");
        String stockId = stockService.confirmStock("stockId", lockKeys);
        System.out.println("扣减库存");
        return stockId;
    }

    @RequestMapping("/{id}")
    public String add(@PathVariable("id") Integer id){
        System.out.println("增加库存");
        return "增加成功";
    }
}
