package com.genxiaogu.ratelimiter.dto;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by wb-lz260260 on 2017/9/21.
 */
public class LimitDTO {

    /**
     * doSomething
     * <p>
     * lua实现
     *
     * @param redisTemplate
     * @param key
     * @param limit         这里默认 limit必须 >= 1
     * @param timeOut
     * @return
     */
    public static boolean execLimit(RedisTemplate redisTemplate, final String key, String limit, String timeOut) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rateLimit.lua")));
        redisScript.setResultType(Long.TYPE);

        Object result = redisTemplate.execute(redisScript, new StringRedisSerializer(), new StringRedisSerializer(), new ArrayList() {{
            add(key);
        }}, limit, timeOut);

        if ((long) result == 1) {
            return true;
        }
        return false;

    }


    /**
     * doSomething
     * <p>
     * java实现 ---redisTemplate
     *
     * @param redisTemplate
     * @param key
     * @param limit         这里默认 limit必须 >= 1
     * @param timeOut
     * @return
     */
    private boolean execLimit2(RedisTemplate redisTemplate, String key, Integer limit, long timeOut) {

        BoundValueOperations boundValueOps = redisTemplate.boundValueOps(key);

        if (null == boundValueOps.get()) {
            boundValueOps.set("1", timeOut, TimeUnit.MILLISECONDS);
            return true;
        } else {
            if (boundValueOps.increment(1) > limit) {
                return false;
            }
            return true;
        }

    }


    /**
     * doSomething
     * <p>
     * 留作笔记用
     * <p>
     * java实现  ---connection
     *
     * @param connection
     * @param key
     * @param limit      limit 可以为0
     * @param timeOut
     * @return
     */
    public static boolean execLimit3(RedisConnection connection, byte[] key, Integer limit, long timeOut) {

        if (!connection.exists(key)) {
            connection.pSetEx(key, timeOut, "0".getBytes());
        }

        if (connection.incr(key) > limit) {
            return false;
        } else {
            return true;
        }

    }

}
