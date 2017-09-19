package com.genxiaogu.ratelimiter.service.impl;

import com.genxiaogu.ratelimiter.service.Limiter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    /**
     * 限流实现
     *
     * @param route
     * @param limit
     * @param obj
     * @return
     */
    @Override
    public boolean execute(String route, Integer limit, String obj) {

        final String key = route.concat(obj);
        final String lockKey = "lock:" + route.concat(obj);
        final String lockValue = UUID.randomUUID().toString();

        boolean bool = false;
        try {
            if (getLock(redisTemplate, new ArrayList<String>() {{add(lockKey);}}, lockValue, String.valueOf(LOCK_TIME_OUT))) {

                // doSomething
                bool = execLimit(redisTemplate, key.getBytes(), limit, KEY_TIME_OUT);
            }
        } catch (Exception e) {
            logger.error("DistributedLimiter execute error.", e);
        } finally {
            releaseLock(redisTemplate, new ArrayList<String>() {{add(lockKey);}}, lockValue);
        }

        return bool;
    }

    /**
     * doSomething
     *
     * @param redisTemplate
     * @param key
     * @param limit         这里默认 limit必须 >= 1
     * @param timeOut
     * @return
     */
    private boolean execLimit(RedisTemplate redisTemplate, byte[] key, Integer limit, long timeOut) {

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




    // ------------------------------------以下代码已废弃！仅留作笔记用----------------------------------------------------


    /**
     * 3
     *
     * 留作笔记用
     *
     * @param route
     * @param limit
     * @param obj
     * @return
     */
    public boolean execute3(String route, Integer limit, String obj) {

        final byte[] key = route.concat(obj).getBytes();
        final byte[] lockKey = ("lock:" + route.concat(obj)).getBytes();
        final byte[] lockValue = UUID.randomUUID().toString().getBytes();
        final byte[] lockTimeOut = String.valueOf(LOCK_TIME_OUT).getBytes();
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        boolean bool = false;
        try {

            if (getLock3(connection, lockKey, lockValue, lockTimeOut)) {

                // doSomething
                bool = execLimit(connection, key, limit, KEY_TIME_OUT);
            }

        } catch (Exception e) {
            logger.error("DistributedLimiter execute error.", e);
        } finally {
            releaseLock3(connection, lockKey, lockValue);
        }

        return bool;
    }


    /**
     * doSomething
     *
     * 留作笔记用
     *
     * @param connection
     * @param key
     * @param limit         limit 可以为0
     * @param timeOut
     * @return
     */
    private boolean execLimit(RedisConnection connection, byte[] key, Integer limit, long timeOut) {

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
