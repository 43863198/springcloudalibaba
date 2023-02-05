package com.demo.structural.test.clazz;

import com.demo.structural.test.JPtransfer;
import com.demo.structural.test.Player;

/**
 * 类结构模型适配
 */
public class TransferAdapt extends JPtransfer implements Player {
    private Player target; //被适配对象

    public TransferAdapt(Player target){
        this.target = target;
    }
    @Override
    public String play() {
        String content
                = target.play();
        String transfer = transfer(content);
        return transfer;
    }
}
