package com.genxiaogu.ratelimiter.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * Lua实现分布式锁
 * <p>
 * Created by junzijian on 2017/9/18.
 */
public class LuaScriptLock {

    private static final Logger logger = LogManager.getLogger(LuaScriptLock.class);

    /**
     * 获取锁
     *
     * @param redisTemplate
     * @param lockKey
     * @param lockValue
     * @param lockTimeOut   Int类型，需提前转换成String. 否则：ClassCastException
     * @return
     */
    public static boolean getLock(RedisTemplate redisTemplate, List<String> lockKey, String lockValue, String lockTimeOut) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/getLock.lua")));
        redisScript.setResultType(Long.class);

        /**
         * 有Int时间参数  需要使用纯String序列化
         */
        Object result = redisTemplate.execute(redisScript, lockKey, lockValue, lockTimeOut);

        /**
         * 这里result为Long类型
         */
        if ((long) result == 1) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean releaseLock(RedisTemplate redisTemplate, List<String> lockKey, String lockValue) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/releaseLock.lua")));
        redisScript.setResultType(Long.class);

        Object result = redisTemplate.execute(redisScript, lockKey, lockValue);

        if ((long) result == 1) {
            return true;
        } else {
            return false;
        }

    }

}
