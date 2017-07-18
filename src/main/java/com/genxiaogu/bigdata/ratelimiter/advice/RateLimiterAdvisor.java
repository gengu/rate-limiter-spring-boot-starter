package com.genxiaogu.bigdata.ratelimiter.advice;

import com.genxiaogu.bigdata.ratelimiter.annotation.Limiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 静态切入点
 * 用于匹配静态方法的切面
 * Created by wb-lz260260 on 2017/7/4.
 */
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RateLimiterAdvisor extends StaticMethodMatcherPointcutAdvisor {

    Logger logger = LoggerFactory.getLogger(RateLimiterAdvisor.class) ;

    @Autowired
    RateLimiterBeforeInterceptor advice ;

    @PostConstruct
    public void init(){
        super.setAdvice(this.advice);
    }

    /**
     * 只有加了Limiter注解的才需求切入
     * @param method
     * @param clazz
     * @return
     */
    public boolean matches(Method method, Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method mod : methods) {
            Limiter methodAnnotation = mod.getAnnotation(Limiter.class);
            if (null != methodAnnotation) {
                logger.info(String.format("======%s .. %s .. %s .. %s .. %s ======"
                    , method.getName()
                    ,this.getPointcut()
                    ,Thread.currentThread().getName() ,
                    clazz.getCanonicalName(),
                    mod.getName()) );
                return true;
            }
        }
        return false ;
    }

}