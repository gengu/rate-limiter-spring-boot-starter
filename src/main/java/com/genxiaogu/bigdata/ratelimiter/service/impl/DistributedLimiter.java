package com.genxiaogu.bigdata.ratelimiter.service.impl;

import com.genxiaogu.bigdata.ratelimiter.service.Limiter;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public class DistributedLimiter implements Limiter {

    private static Set<String> keys = new HashSet<String>() ;

    private RedisTemplate<String , String> redisTemplate;

    /**
     * @param redisTemplate
     */
    public DistributedLimiter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 第一次如果key存在则从redis删除，并加入到keySet,相当于是初始化
     * @param key
     * @param limit
     * @return
     */
    public synchronized boolean execute(String key, int limit) {
        if(!keys.contains(key)){
            redisTemplate.delete(key);
            keys.add(key) ;
        }
        BoundValueOperations<String, String> boundValueOps = redisTemplate.boundValueOps(key);

        if(boundValueOps.get() == null) {
            boundValueOps.set("0");
            boundValueOps.expire(1000, TimeUnit.MILLISECONDS);
            System.out.println("add value to redis,key=" + key + ",second=" + (System.currentTimeMillis() / 1000));
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
