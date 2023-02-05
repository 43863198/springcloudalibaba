package com.demo.service.impl;

import com.demo.service.OrderService;
import com.my.distribute.LockType;
import com.my.distribute.lock.annotation.DistributedLock;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    //@DistributedLock(lockType = LockType.OCMS_ACCOUNT_LOCK,expireTime = 5,waitTime = 26,key = {"#lockKeys"})
    public String confirmOrder(String orderId, Set<String> lockKeys) {

        return "lock is success";
    }
}
