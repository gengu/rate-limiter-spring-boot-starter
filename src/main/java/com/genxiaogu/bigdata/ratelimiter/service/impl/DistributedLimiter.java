package com.genxiaogu.bigdata.ratelimiter.service.impl;

import com.genxiaogu.bigdata.ratelimiter.service.Limiter;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public class DistributedLimiter implements Limiter {

    private RedisTemplate<String , String> redisTemplate;

    public DistributedLimiter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean execute(String key, int limit) {
        BoundValueOperations<String, String> boundValueOps = redisTemplate.boundValueOps(key);

        if(boundValueOps.get() == null) {
            boundValueOps.set("0");
            boundValueOps.expire(1000, TimeUnit.MILLISECONDS);
            return true ;
        }
        else {
            long currentCount = boundValueOps.increment(1);
            if (currentCount > limit) {
                return false;
            }
            return true;
        }
    }
}
