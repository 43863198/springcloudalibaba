package com.demo.structural.test;

public class JPtransfer implements Transfer{
    @Override
    public String transfer(String content) {
        System.out.println("开始翻译");
        String transfer = "hello";
        return transfer;
    }
}
