package com.genxiaogu.bigdata.ratelimiter.service.impl;

import com.genxiaogu.bigdata.ratelimiter.service.Limiter;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public class DistributedLimiter implements Limiter {

    private StringRedisTemplate stringRedisTemplate;

    public DistributedLimiter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean execute(String key, int limit) {
        BoundValueOperations<String, String> boundValueOps = stringRedisTemplate.boundValueOps(key);
        boundValueOps.setIfAbsent("0");
        /**
         * 剩余过期时间
         */
        boundValueOps.expire(1000 , TimeUnit.MILLISECONDS);
        System.out.println(boundValueOps.getExpire() + "=====") ;
        /**
         * 当前访问次数
         **/
        long currentCount = boundValueOps.increment(1);
        if (currentCount > limit) {
            return false;
        }
        return true;
    }
}
