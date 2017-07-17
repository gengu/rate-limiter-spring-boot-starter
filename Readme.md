# 读我

## 功能
集群QPS控制，在方法上添加注解即可限制QPS

## 使用方法

### 注解定义
```java
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
```

### 资源
> 在pom.xml中添加依赖
```xml
<dependency>
    <groupId>com.genxiaogu.bigdata.ratelimiter</groupId>
    <artifactId>rate-limiter-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>

```
> 在application.properties中添加redis资源
```properties
spring.redis.database=0
spring.redis.host=127.0.0.1
spring.redis.port=8888
spring.redis.password=xxx
spring.redis.pool.max-active=50000
spring.redis.pool.max-wait=-1
spring.redis.pool.max-idle=10
spring.redis.pool.min-idle=0
spring.redis.timeout=500
```

> 在需要进行流量控制的方法上添加注解例如
```java

在mapping方法上
@Limiter(route = "test", limit = 100)
@RequestMapping("/test")
public String check(){
    return dataPermissionTest.test() ;
}
或者在普通方法上
@Limiter(route = "test", limit = 100)
public String test(){
    return "test" ;
}
```

***注意，Limiter的route参数指代控制方法的唯一key，limit表示1s内允许访问多少次***


## DEMO
[spring-boot-rate-limiter-demo](https://github.com/gengu/spring-boot-demos/tree/master/spring-boot-rate-limiter-demo)

## 使用siege测试


> 总结
> > 我尝试把系统的QPS压测到100 

```javascript
siege -c 50 -t 1 'http://localhost:8080/test'
```

```properties
Transactions:		        1125 hits
Availability:		       98.00 %
Elapsed time:		       11.80 secs
Data transferred:	        0.01 MB
Response time:		        0.00 secs
Transaction rate:	       95.34 trans/sec
Throughput:		        0.00 MB/sec
Concurrency:		        0.41
Successful transactions:        1125
Failed transactions:	          23
Longest transaction:	        0.14
Shortest transaction:	        0.00
```
