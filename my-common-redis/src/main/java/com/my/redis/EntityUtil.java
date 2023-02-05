package com.my.redis;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class EntityUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    public Object setEntity(String token, Object entity)
    {
        try
        {
            Object result = this.redisTemplate.boundValueOps(token).get();
            if (result == null) {
                return entity;
            }
            String userId = JSON.parseObject(result.toString()).getString("userId");
            for (Field field : entity.getClass().getDeclaredFields())
            {
                if (StringUtils.equals(field.getName(), "id"))
                {
                    field.setAccessible(true);
                    if ((field.get(entity) == null) ||
                            (StringUtils.isBlank(String.valueOf(field.get(entity))))) {
                        field.set(entity, UUID.randomUUID().toString().replace("-", ""));
                    }
                }
                if (StringUtils.equals(field.getName(), "updatedDate"))
                {
                    field.setAccessible(true);
                    field.set(entity, new Date());
                }
                if (StringUtils.equals(field.getName(), "createdDate"))
                {
                    field.setAccessible(true);
                    if (field.get(entity) == null) {
                        field.set(entity, new Date());
                    }
                }
                if (StringUtils.equals(field.getName(), "updatedBy"))
                {
                    field.setAccessible(true);
                    field.set(entity, userId);
                }
                if (StringUtils.equals(field.getName(), "createdBy"))
                {
                    field.setAccessible(true);
                    if ((field.get(entity) == null) ||
                            (StringUtils.isBlank(String.valueOf(field.get(entity))))) {
                        field.set(entity, userId);
                    }
                }
            }
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(getClass()).error(e.getMessage());
        }
        return entity;
    }

    public Object setEntity(Map token, Object entity)
    {
        return setEntity(String.valueOf(token.get("token")), entity);
    }
}
