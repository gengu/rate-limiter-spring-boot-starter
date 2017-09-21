package com.genxiaogu.ratelimiter.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.data.redis.connection.RedisConnection;

/**
 * java实现分布式锁
 * <p>
 * Created by junzijian on 2017/9/18.
 */
public class DistributedLock {

    private static final Logger logger = LogManager.getLogger(DistributedLock.class);

    public static boolean getLock(RedisConnection connection, byte[] key, byte[] value, long lockTimeOut) {

        long acquireTimeEnd = System.currentTimeMillis() + lockTimeOut;

        while (System.currentTimeMillis() <= acquireTimeEnd) {

            if (connection.setNX(key, value)) {
                connection.pExpire(key, lockTimeOut);
                return true;
            }
            // 保险
            else if (connection.pTtl(key) == -1) {
                connection.pExpire(key, lockTimeOut);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error("getLock error.", e);
            }
        }

        return false;
    }

    public static void releaseLock(RedisConnection connection, byte[] key, byte[] value) {

        // WATCH命令可以监控一个或多个键，一旦其中有一个键被修改（或删除），之后的事务就不会执行。监控一直持续到EXEC命令（事务中的命令是在EXEC之后才执行的，所以在MULTI命令后可以修改WATCH监控的键值）
        connection.watch(key);
        // 当前线程还持有锁（校验是不是自己的锁）
        if (connection.get(key).equals(value)) {
            connection.multi();
            connection.del(key);
            connection.exec();      // connection.dto().isEmpty()
        }
        connection.unwatch();

    }

}
