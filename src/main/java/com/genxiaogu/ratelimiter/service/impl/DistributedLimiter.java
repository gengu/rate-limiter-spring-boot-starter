package com.genxiaogu.ratelimiter.service.impl;

import com.genxiaogu.ratelimiter.service.Limiter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.UUID;

import static com.genxiaogu.ratelimiter.lock.LuaScriptLock.*;

/**
 * Created by junzijian on 2017/7/4.
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
        final String lockKey = "lock:" + key;
        final String lockValue = UUID.randomUUID().toString();

        boolean bool = false;
        try {
            if (getLock(redisTemplate, new ArrayList<String>() {{
                add(lockKey);
            }}, lockValue, String.valueOf(LOCK_TIME_OUT))) {

                // doSomething
                bool = execLimit(redisTemplate, key, String.valueOf(limit), String.valueOf(KEY_TIME_OUT));
            }
        } catch (Exception e) {
            logger.error("DistributedLimiter execute error.", e);
        } finally {
            releaseLock(redisTemplate, new ArrayList<String>() {{
                add(lockKey);
            }}, lockValue);
        }

        return bool;
    }


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
    private boolean execLimit(RedisTemplate redisTemplate, final String key, String limit, String timeOut) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rateLimit.lua")));
        redisScript.setResultType(Long.TYPE);

        Object result = redisTemplate.execute(redisScript, new ArrayList() {{
            add(key);
        }}, limit, timeOut);

        if ((long) result == 1) {
            return true;
        }
        return false;

    }

}
