package com.genxiaogu.ratelimiter.service.impl;

import com.genxiaogu.ratelimiter.service.Limiter;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public class DistributedLimiter implements Limiter {

    private RedisTemplate<String , String> redisTemplate;

    private String route ;

    private Integer limit ;
    /**
     * @param redisTemplate
     */
    public DistributedLimiter(RedisTemplate redisTemplate , String route , Integer limit) {
        this.redisTemplate = redisTemplate;
        this.route  = route ;
        this.limit = limit ;
    }

    public DistributedLimiter(
        RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 第一次如果key存在则从redis删除，并加入到keySet,相当于是初始化
     * @return
     */
    @Override
    public boolean execute() {

        BoundValueOperations<String, String> boundValueOps = redisTemplate.boundValueOps(route);

        if(boundValueOps.setIfAbsent("1")){
            boundValueOps.expire(1000, TimeUnit.MILLISECONDS);
            System.out.println(
                "add value to redis,key=" + route + ",second=" + (System.currentTimeMillis() / 1000));
            return true ;
        }else{
            long currentCount = boundValueOps.increment(1);
            if (currentCount > limit) {
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean execute(String route , Integer limit){
        BoundValueOperations<String, String> boundValueOps = redisTemplate.boundValueOps(route);

        if(boundValueOps.setIfAbsent("1")){
            boundValueOps.expire(1000, TimeUnit.MILLISECONDS);
            System.out.println(
                "add value to redis,key=" + route + ",second=" + (System.currentTimeMillis() / 1000));
            return true ;
        }else{
            long currentCount = boundValueOps.increment(1);
            if (currentCount > limit) {
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean execute(String route , Integer limit ,String obj){
        BoundValueOperations<String, String> boundValueOps = redisTemplate.boundValueOps(route.concat(obj));

        if(boundValueOps.setIfAbsent("1")){
            boundValueOps.expire(1000, TimeUnit.MILLISECONDS);
            System.out.println(
                "add value to redis,key=" + route + ",second=" + (System.currentTimeMillis() / 1000));
            return true ;
        }else{
            long currentCount = boundValueOps.increment(1);
            if (currentCount > limit) {
                return false;
            }
            return true;
        }
    }
}
