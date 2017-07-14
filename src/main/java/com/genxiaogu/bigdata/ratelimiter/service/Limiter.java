package com.genxiaogu.bigdata.ratelimiter.service;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public interface Limiter {

    boolean execute(String router, int limit);
}
