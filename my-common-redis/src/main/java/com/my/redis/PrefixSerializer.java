package com.my.redis;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class PrefixSerializer extends FastJsonRedisSerializer {

    private String prefix;

    public PrefixSerializer(Class type, String prefix)
    {
        super(type);
        this.prefix = prefix;
    }

    public byte[] serialize(Object o)
            throws SerializationException
    {
        if ((o instanceof String)) {
            o = this.prefix + o;
        }
        return super.serialize(o);
    }
}
