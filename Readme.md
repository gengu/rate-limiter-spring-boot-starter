# 读我

## 功能
集群QPS控制，在方法上添加注解即可限制QPS

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
