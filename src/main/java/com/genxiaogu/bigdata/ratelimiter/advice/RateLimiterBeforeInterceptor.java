package com.genxiaogu.bigdata.ratelimiter.advice;

import com.genxiaogu.bigdata.ratelimiter.annotation.Limiter;
import com.genxiaogu.bigdata.ratelimiter.common.LimiterException;
import com.genxiaogu.bigdata.ratelimiter.service.impl.DistributedLimiter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 方法拦截器：执行前通知
 * @author genxiaogu
 */
@Component
public class RateLimiterBeforeInterceptor implements MethodInterceptor {

    Logger logger = LoggerFactory.getLogger(RateLimiterBeforeInterceptor.class) ;

    @Autowired
    private RedisTemplate<String , String> redisTemplate;

    private static HashMap<String , DistributedLimiter> distributedLimiterHashMap = new HashMap<String, DistributedLimiter>(16) ;

    /**
     * 执行逻辑
     * @param methodInvocation
     * @return
     * @throws Throwable
     */
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String route = "";
        int limit = 1;

        Method method = methodInvocation.getMethod();

        for (Annotation annotation : method.getAnnotations()) {
            /*
             * 如果方法具有Limiter注解，则需要把method，limit拿出来
             */
            if (annotation instanceof Limiter) {
                Limiter limiter = method.getAnnotation(Limiter.class);
                route = limiter.route();
                limit = limiter.limit();
                if(!distributedLimiterHashMap.containsKey(route)){
                    distributedLimiterHashMap.put(route , new DistributedLimiter(redisTemplate , route , limit)) ;
                }
            }
        }

        if(!distributedLimiterHashMap.get(route).execute()) {
            throw new LimiterException("访问太过频繁，请稍后再试！") ;
        }
        return methodInvocation.proceed() ;
    }
}
