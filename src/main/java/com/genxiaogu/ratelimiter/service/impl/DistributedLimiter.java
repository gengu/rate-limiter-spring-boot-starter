package com.genxiaogu.ratelimiter.service.impl;

import com.genxiaogu.ratelimiter.service.Limiter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.UUID;

import static com.genxiaogu.ratelimiter.lock.LuaScriptLock.*;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public class DistributedLimiter implements Limiter {

    private static final Logger logger = LogManager.getLogger(DistributedLimiter.class);

    public static final long LOCK_TIME_OUT = 1000;

    public static final long KEY_TIME_OUT = 1000;

    private RedisTemplate redisTemplate;

    private String route;

    private Integer limit;

    /**
     * @param redisTemplate
     */
    public DistributedLimiter(RedisTemplate redisTemplate, String route, Integer limit) {
        this.redisTemplate = redisTemplate;
        this.route = route;
        this.limit = limit;
    }

    public DistributedLimiter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 第一次如果key存在则从redis删除，并加入到keySet,相当于是初始化
     *
     * @return
     */
    @Override
    public boolean execute() {
        return execute(route, limit, "");
    }

    @Override
    public boolean execute(String route, Integer limit) {
        return execute(route, limit, "");
    }

    /*@Override
    public boolean execute(String route, Integer limit, final String obj) {

        final byte[] key = route.concat(obj).getBytes();
        byte[] lockKey = ("lock:" + route.concat(obj)).getBytes();
        byte[] lockValue = UUID.randomUUID().toString().getBytes();
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        boolean bool = false;
        try {

            if (getLock(connection, lockKey, lockValue, LOCK_TIME_OUT)) {
                bool = execLimit(connection, key, limit, KEY_TIME_OUT);
            }

        } catch (Exception e) {
            logger.error("DistributedLimiter execute error.", e);
        } finally {
            releaseLock(connection, lockKey, lockValue);
        }

        return bool;
    }*/


    @Override
    public boolean execute(String route, Integer limit, final String obj) {

        final String key = route.concat(obj);
        final String lockKey = "lock:" + route.concat(obj).substring(25);
        final String lockValue = UUID.randomUUID().toString().substring(25);
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        boolean bool = false;
        try {

//            if (getLock(connection, lockKey, lockValue, LOCK_TIME_OUT)) {
            if (getLock2(redisTemplate, new ArrayList<String>() {{add(lockKey);}}, lockValue, LOCK_TIME_OUT)) {
                bool = execLimit(connection, key.getBytes(), limit, KEY_TIME_OUT);
            }

        } catch (Exception e) {
            logger.error("DistributedLimiter execute error.", e);
        } finally {
//            releaseLock(connection, lockKey, lockValue);
            releaseLock2(redisTemplate, new ArrayList<String>() {{
                add(lockKey);
            }}, lockValue);
        }

        return bool;
    }

    private boolean execLimit(RedisConnection connection, final byte[] key, final Integer limit, final long timeOut) {

        connection.pSetEx(key, timeOut, "0".getBytes());

        if (connection.incr(key) > limit) {
            return false;
        } else {
            return true;
        }

    }
}
