package com.genxiaogu.ratelimiter.configuration;

import com.genxiaogu.ratelimiter.advice.MethodRateLimiterAdvisor;
import com.genxiaogu.ratelimiter.advice.MethodRateLimiterBeforeInterceptor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by wb-lz260260 on 2017/7/5.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class LimiterConfiguration {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 自动代理生成器
     * 这个类可以扫描所有的切面类，并为其自动生成代理。
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        return new DefaultAdvisorAutoProxyCreator() ;
    }

    @Bean
    public MethodRateLimiterBeforeInterceptor myAroundInterceptor(){
        return new MethodRateLimiterBeforeInterceptor() ;
    }

    @Bean
    public MethodRateLimiterAdvisor myAdvisor(){
        return new MethodRateLimiterAdvisor() ;
    }
}
