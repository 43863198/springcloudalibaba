package com.demo.service.impl;

import com.demo.entity.student;
import com.demo.service.StockService;
import com.my.distribute.LockType;
import com.my.distribute.lock.annotation.DistributedLock;
import com.my.distribute.lock.annotation.LockKey;
import com.my.redis.EntityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @DistributedLock(lockType = LockType.OCMS_ACCOUNT_LOCK,expireTime = 5,waitTime = 26,key = {"#lockKeys"})
    public String confirmStock(@LockKey String stockId, Set<String> lockKeys) {
//        student student = new student();
//        student.setId("111");
//        student.setName("123");
//        redisTemplate.opsForValue().set("test",student);
        return "stock lock success";
    }
}
