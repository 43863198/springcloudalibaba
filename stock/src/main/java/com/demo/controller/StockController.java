package com.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
public class StockController {

    @RequestMapping("/reduce")
    public String reduce(){
        System.out.println("扣减库存");
        return "扣减成功";
    }

    @RequestMapping("/{id}")
    public String add(@PathVariable("id") Integer id){
        System.out.println("增加库存");
        return "增加成功";
    }
}
