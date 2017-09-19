package com.genxiaogu.ratelimiter.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Lua实现分布式锁
 * <p>
 * Created by wb-lz260260 on 2017/9/18.
 */
public class LuaScriptLock {

    private static final Logger logger = LogManager.getLogger(LuaScriptLock.class);

    /**
     * 获取锁
     *
     * @param redisTemplate
     * @param lockKey
     * @param lockValue
     * @param lockTimeOut   Int类型，需提前转换成String. 否则：ClassCastException
     * @return
     */
    public static boolean getLock(RedisTemplate redisTemplate, List<String> lockKey, String lockValue, String lockTimeOut) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/getLock2.lua")));
        redisScript.setResultType(Long.class);

        /**
         * 有Int时间参数  需要使用纯String序列化
         */
        Object result = redisTemplate.execute(redisScript, new GenericToStringSerializer(String.class), new StringRedisSerializer(), lockKey, lockValue, lockTimeOut);

        /**
         * 这里result为Long类型
         */
        if ((long) result == 1) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean releaseLock(RedisTemplate redisTemplate, List<String> lockKey, String lockValue) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/releaseLock.lua")));
        redisScript.setResultType(Long.class);

        Object result = redisTemplate.execute(redisScript, lockKey, lockValue);

        if ((long) result == 1) {
            return true;
        } else {
            return false;
        }

    }

    //-------------------------------------以下方法其实和上述方法一样，仅作为个人笔记用--------------------------------------

    public static boolean getLock2(RedisTemplate redisTemplate, List<String> lockKey, String lockValue, String lockTimeOut) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("local key = KEYS[1]\n" +
                "local value = ARGV[1]\n" +
                "local expire = ARGV[2]\n" +
                "\n" +
                "if redis.call(\"SET\", key, value, \"NX\", \"PX\", expire) then\n" +
                "    return 1\n" +
                "elseif redis.call(\"TTL\", key) == -1 then\n" +
                "    redis.call(\"PEXPIRE\", key, expire)\n" +
                "end\n" +
                "return 0");
        redisScript.setResultType(Long.class);

        /**
         *
         * TODO 序列化坑1  ——> Int类型
         * ERR value is not an integer or out of range  ==> 我明明传递的一个Int，为什么总是报错？？？
         * 对于Integer类型的参数传值，序列化要特别注意：（特别是涉及到计算的：incr / decr ... 或者时间的：expire / pexpire ...）
         *
         * 这里可使用：GenericToStringSerializer / StringRedisSerializer
         * GenericToStringSerializer、StringRedisSerializer 将字符串的值直接转为字节数组，所以保存到redis中是数字
         *
         * 只有这两个才能保证数据的完整性，而经其他序列器序列化后，数据其实上已经被篡改！！！
         *
         * 具体为：
         * GenericJackson2JsonRedisSerializer、Jackson2JsonRedisSerializer 是先将对象转为json，然后再保存到redis，所以，1在redis中是字符串 ==> "1"，所以无法识别为数字
         * JdkSerializationRedisSerializer 使用的jdk对象序列化，序列化后的值有类信息、版本号等，所以是一个包含很多字母的字符串 ==> "\xac\xed\x00\x05t\x00\x1elock:/poi/heat1745961731490546"，并且我们看上去就是一堆乱码,所以根本无法识别为数字. 这个序列化器跟memcache的序列化规则很像
         *
         * 详细分析：http://blog.csdn.net/wangjun5159/article/details/52387782
         */
        GenericToStringSerializer argsSerializer = new GenericToStringSerializer(String.class);
        StringRedisSerializer resultSerializer = new StringRedisSerializer();

        Object result = redisTemplate.execute(redisScript, argsSerializer, resultSerializer, lockKey, lockValue, lockTimeOut);

        if (Long.valueOf(result.toString()) == 1) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean releaseLock2(RedisTemplate redisTemplate, List<String> lockKey, String lockValue) {

        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("local key = KEYS[1]\n" +
                "local value = ARGV[1]\n" +
                "\n" +
                "if redis.call(\"GET\", key) == value then\n" +
                "    redis.call(\"DEL\", key)\n" +
                "    return 1\n" +
                "else return 0\n" +
                "end");
        redisScript.setResultType(Integer.class);

        // 而这里不涉及到Int的数字操作，用不用纯String方式都可！！！
        Object result = redisTemplate.execute(redisScript, lockKey, lockValue);

        if (Integer.valueOf(result.toString()) == 1) {
            return true;
        } else {
            return false;
        }

    }


    // -------------------------此种方法发现一直编译错误 o(╯□╰)o 放弃!!!--------------------------------------------------

    public static boolean getLock3(RedisConnection connection, byte[] lockKey, byte[] lockValue, byte[] lockTimeOut) {

        Object result = connection.eval(loadLuaScript("lua/getLock2.lua"), ReturnType.INTEGER, 1, lockKey, lockValue, lockTimeOut);

        if ((int) result == 1) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean releaseLock3(RedisConnection connection, byte[] lockKey, byte[] lockValue) {

        Object result = connection.eval(loadLuaScript("lua/releaseLock.lua"), ReturnType.VALUE, 1, lockKey, lockValue);

        if ((int) result == 1) {
            return true;
        } else {
            return false;
        }

    }

    private static byte[] loadLuaScript(String lua) {

        InputStream is = LuaScriptLock.class.getClassLoader().getResourceAsStream(lua);
        BufferedInputStream bis = new BufferedInputStream(is);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        try {
            while (bis.read(buffer, 0, 1024) != -1) {
                bos.write(buffer, 0, 1024);
            }
        } catch (IOException e) {
            logger.error("loadLuaScript >>> IO error. ", e);
        } finally {
            try {
                is.close();
                bis.close();
                bos.close();
            } catch (IOException e) {
                logger.error("loadLuaScript >>> close stream error. ", e);
            }
        }

        return bos.toByteArray();

    }

}
