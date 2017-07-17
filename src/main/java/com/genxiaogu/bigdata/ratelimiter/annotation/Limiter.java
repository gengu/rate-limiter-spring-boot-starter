package com.genxiaogu.bigdata.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Limiter {

    /**
     * 限流route
     * @return
     */
    String route();

    /**
     * 限流次数
     * @return
     */
    int limit() default 10;

}
