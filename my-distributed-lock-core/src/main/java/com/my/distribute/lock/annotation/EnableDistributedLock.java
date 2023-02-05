package com.my.distribute.lock.annotation;


import com.my.distribute.lock.aspect.DistributedLockAspect;
import com.my.distribute.lock.config.RedissonConfiguration;
import com.my.distribute.lock.provide.LockKeyComponentsProvider;
import com.my.distribute.lock.provide.LockKeyProvider;
import com.my.distribute.lock.provide.SpelLockKeyProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(value = {DistributedLockAspect.class, RedissonConfiguration.class,
        LockKeyComponentsProvider.class, LockKeyProvider.class, SpelLockKeyProvider.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EnableDistributedLock {
}
