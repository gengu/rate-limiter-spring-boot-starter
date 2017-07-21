package com.genxiaogu.ratelimiter.advice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.jws.soap.SOAPBinding.Use;

import com.genxiaogu.ratelimiter.annotation.Limiter;
import com.genxiaogu.ratelimiter.annotation.UserLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * 静态切入点
 * 用于匹配用户层面的切面
 * @author genxiaogu
 */
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UserRateLimiterAdvisor extends StaticMethodMatcherPointcutAdvisor {

    Logger logger = LoggerFactory.getLogger(UserRateLimiterAdvisor.class) ;

    @Autowired
    MethodRateLimiterBeforeInterceptor advice ;

    @PostConstruct
    public void init(){
        super.setAdvice(this.advice);
    }

    /**
     * 只有加了UserLimiter注解的才需求切入
     * @param method
     * @param clazz
     * @return
     */
    @Override
    public boolean matches(Method method, Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method mod : methods) {
            Annotation[][] annotations = mod.getParameterAnnotations() ;
            for (int i = 0; i < annotations.length; i++) {
                for (int j = 0; j < annotations[i].length; j++) {
                    if(annotations[i][j].annotationType() == UserLimiter.class){
                        return true ;
                    }
                }
            }
        }
        return false ;
    }

}