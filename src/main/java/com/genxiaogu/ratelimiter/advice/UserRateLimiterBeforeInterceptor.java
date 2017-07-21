package com.genxiaogu.ratelimiter.advice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.genxiaogu.ratelimiter.annotation.Limiter;
import com.genxiaogu.ratelimiter.annotation.UserLimiter;
import com.genxiaogu.ratelimiter.common.LimiterException;
import com.genxiaogu.ratelimiter.service.impl.DistributedLimiter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 用户拦截器：执行前通知
 * @author genxiaogu
 */
@Component
public class UserRateLimiterBeforeInterceptor implements MethodInterceptor {

    Logger logger = LoggerFactory.getLogger(UserRateLimiterBeforeInterceptor.class) ;


    @Autowired
    DistributedLimiter distributedLimiter ;

    /**
     * 执行逻辑
     * @param methodInvocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String route = "";
        int limit = 1;
        String obj = null ;

        Method method = methodInvocation.getMethod();

        for (Annotation[] annotations : method.getParameterAnnotations()) {
            /*
             * 如果方法具有Limiter注解，则需要把method，limit拿出来
             */
            for (Annotation annotation : annotations) {
                if(annotation instanceof UserLimiter){
                    route = ((UserLimiter)annotation).route() ;
                    limit = ((UserLimiter)annotation).limit() ;
                    methodInvocation.getArguments() ;
                    if(!distributedLimiter.execute(route , limit)) {
                        throw new LimiterException("访问太过频繁，请稍后再试！") ;
                    }
                }
            }
        }

        Annotation[][] annotations = method.getParameterAnnotations() ;
        for (int i = 0; i < annotations.length ; i++) {
            for (int j = 0; j < annotations[i].length ; j++) {
                Annotation annotation = annotations[i][j] ;
                if(annotation instanceof UserLimiter){
                    route = ((UserLimiter)annotation).route() ;
                    limit = ((UserLimiter)annotation).limit() ;
                    obj = String.valueOf(methodInvocation.getArguments()[i]) ;
                    if(!distributedLimiter.execute(route , limit , obj)) {
                        throw new LimiterException("访问太过频繁，请稍后再试！") ;
                    }
                }
            }
        }
        return methodInvocation.proceed() ;
    }
}
