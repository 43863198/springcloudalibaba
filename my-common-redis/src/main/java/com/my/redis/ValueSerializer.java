package com.my.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ValueSerializer<T> extends FastJsonRedisSerializer<T> {
    public static final String CACHE_KEY = "cacheType";

    public ValueSerializer(Class<T> type)
    {
        super(type);
    }

    public T deserialize(byte[] bytes)
            throws SerializationException
    {
        try
        {
            if (bytes != null)
            {
                String content = new String(bytes);
                if (StringUtils.equals(content, "[null]")) {
                    return (T) Collections.EMPTY_LIST;
                }
                if (StringUtils.equals(content, "null")) {
                    return null;
                }
                if ((StringUtils.startsWith(content, "[")) &&
                        (StringUtils.endsWith(content, "]")))
                {
                    List<JSONObject> objects = JSON.parseArray(content, JSONObject.class);
                    if ((objects.size() > 0) && (((JSONObject)objects.get(0)).containsKey("cacheType"))) {
                        return (T) JSON.parseArray(content,
                                Class.forName((String)((JSONObject)objects.get(0)).get("cacheType")));
                    }
                }
                else if ((StringUtils.startsWith(content, "{")) &&
                        (StringUtils.endsWith(content, "}")))
                {
                    JSONObject object = JSON.parseObject(content);
                    if (object.containsKey("cacheType")) {
                        return JSON.parseObject(bytes, Class.forName((String)object.get("cacheType")), new Feature[0]);
                    }
                }
            }
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(getClass()).error(e.getMessage());
        }
        return super.deserialize(bytes);
    }
}
