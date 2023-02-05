package com.my.distribute.lock.aspect;


import com.alibaba.fastjson.JSON;
import com.my.distribute.LockType;
import com.my.distribute.lock.annotation.DistributedLock;
import com.my.distribute.lock.provide.LockKeyProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
@Order(1)
@Slf4j
public class DistributedLockAspect {
    public static final String MESSAGE_DISTRIBUTED_LOCK_FAILED = "Get Distributed lock failed.";
    public static final String KEY_SEPARATOR = ".";
    public static final String TRY_LOCK_FAILED = "Try Lock false, lockKey was occupied";

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private LockKeyProvider keyProvider;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        return execute(joinPoint, distributedLock);
    }

    private Object execute(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String prefix = null;
        RedissonMultiLock lock = null;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        try {
            log.info("LockType:{}", distributedLock.lockType());
            prefix = getLockPrefix(joinPoint, distributedLock);
            lock = getLock(joinPoint, distributedLock, prefix);
        } catch (Exception e) {
            log.error("--GET DISTRIBUTED LOCK FAIL",e);
            throw e;
            //throw new OcmsException(MESSAGE_DISTRIBUTED_LOCK_FAILED);
        }
        long startTryLock = System.currentTimeMillis();
        try {
            boolean isSuccessLocked = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.expireTime(),
                    distributedLock.timeUnit());
            long endTryLock = System.currentTimeMillis();
            log.info("---DISTRIBUTED_LOCK:{},GET_TRYLOCK_TIME:{}, LOCK_OBJ:{}", isSuccessLocked, endTryLock - startTryLock, JSON.toJSONString(lock));
            if (isSuccessLocked) {
                long start = System.currentTimeMillis();
                log.info(method.getName() + "PROCEED_START:{}", start);
                log.info("Procced Before Thread:{} transaction name: {}", Thread.currentThread().getId(), TransactionSynchronizationManager.getCurrentTransactionName());
                Object proceed = joinPoint.proceed();
                log.info("Procced After Thread:{} transaction name: {}", Thread.currentThread().getId(), TransactionSynchronizationManager.getCurrentTransactionName());
                long end = System.currentTimeMillis();
                log.info(method.getName() + "_PROCEED_END:{}", end);
                log.info(method.getName() + "_PROCEED_EXP_TIME:{}", end - start);
                return proceed;
            } else {
                throw new Exception();
                //throw new OcmsException(TRY_LOCK_FAILED);
            }
        } catch (Exception e) {
            log.error("Do proceed Exception: {}", e.getMessage());
            throw e;
//        } catch (ObjectOptimisticLockingFailureException e) {
//            log.error("Do proceed ObjectOptimisticLockingFailureException: {}, method name : {}", e.getMessage(), method.getName());
//            throw e;
//        } catch (Exception e) {
//            log.error("Get Lock Or do proceed Exception: {}", e.getMessage());
//            e.printStackTrace();
//            throw e;
        } finally {
            try {
                lock.unlock();
                log.info("---DISTRIBUTED_UNLOCK---:{}", JSON.toJSONString(lock));
            } catch (Exception e) {
                log.error("Thread " + Thread.currentThread().getId() + " " + e.getMessage());
            }
        }
    }

    private String getLockPrefix(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        return distributedLock.lockType() == LockType.Default
                ? joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName()
                : distributedLock.lockType().toString();
    }

    private RedissonMultiLock getLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock, String prefix) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        List<List<String>> keyCollections = getKeyCollections(method, distributedLock, joinPoint.getArgs());
        List<String> keys = keyCollections.stream()
                .flatMap(Collection::stream)
                .filter(o -> o != null && o != "")
                .map(o -> prefix + KEY_SEPARATOR + o)
                .collect(Collectors.toList());

        return this.getLockByKeys(new HashSet<String>(keys));
    }

    private List<List<String>> getKeyCollections(Method method, DistributedLock annotation, Object[] arguments) {
        if (annotation.key().length == 0) {
            return Collections.singletonList(keyProvider.get(null, method, arguments));
        } else {
            return Stream.of(annotation.key())
                    .map(keyDefinition -> keyProvider.get(keyDefinition, method, arguments))
                    .collect(Collectors.toList());
        }
    }

    private RedissonMultiLock getLockByKeys(HashSet<String> keys) {
        List<RLock> locks = new ArrayList<>();
        log.info("LOCK_KEYS:{}", keys);
        for (String key : keys) {

            locks.add(redisson.getLock(key));
        }

        return new RedissonMultiLock(locks.toArray(new RLock[locks.size()]));
    }
}
