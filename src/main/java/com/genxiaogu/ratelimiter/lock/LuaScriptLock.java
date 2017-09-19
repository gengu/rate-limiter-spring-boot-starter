package com.genxiaogu.ratelimiter.lock;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.List;
import java.util.Objects;

/**
 * Lua实现分布式锁
 * <p>
 * Created by wb-lz260260 on 2017/9/18.
 */
public class LuaScriptLock {

    private static final Logger logger = LogManager.getLogger(LuaScriptLock.class);

    public static boolean getLock(RedisConnection connection, byte[] lockKey, byte[] lockValue, long lockTimeOut) {
        byte[] lockTimeOutBytes = String.valueOf(lockTimeOut).getBytes();

        Object result = connection.eval(loadLuaScript("lua/getLock.lua"), ReturnType.INTEGER, 1, lockKey, lockValue, lockTimeOutBytes);

        if ((int) result == 1) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean releaseLock(RedisConnection connection, byte[] lockKey, byte[] lockValue) {

        Object result = connection.eval(loadLuaScript("lua/releaseLock.lua"), ReturnType.VALUE, 1, lockKey, lockValue);

        if (Integer.valueOf(result.toString()) == 1) {
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


    //------------------------------------------------------------------------------------------------------------------


    public static boolean getLock2(RedisTemplate redisTemplate, List<String> lockKey, String lockValue, long lockTimeOut) {

//        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>(loadLuaScript2("lua/getLock.lua"), String.class);
//        redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("local key = KEYS[1]\n" +
                "local identifier = ARGV[1]\n" +
                "local lockTimeOut = ARGV[2]\n" +
                "if redis.call(\"SETNX\", key, identifier) == 1 then\n" +
                "    redis.call(\"EXPIRE\", key, lockTimeOut)\n" +
                "    return 1\n" +
                "elseif redis.call(\"TTL\", key) == -1 then\n" +
                "    redis.call(\"EXPIRE\", key, lockTimeOut)\n" +
                "end\n" +
                "return 0");
        redisScript.setResultType(Integer.class);

        Object result = redisTemplate.execute(redisScript, new StringRedisSerializer(), new StringRedisSerializer(), lockKey, lockValue, "10");
        System.out.println(result);

        if (Integer.valueOf(result.toString()) == 1) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean releaseLock2(RedisTemplate redisTemplate, List<String> lockKey, String lockValue) {

//        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>(loadLuaScript2("lua/releaseLock.lua"), String.class);
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

        Object result = redisTemplate.execute(redisScript, lockKey, lockValue);

        if (Integer.valueOf(result.toString()) == 1) {
            return true;
        } else {
            return false;
        }

    }

    private static String loadLuaScript2(String lua) {

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

        return bos.toString();

    }


    // -----------------------------------------------------------------------------------------------------------------


    public static boolean getLock22(StringRedisTemplate redisTemplate, List<String> lockKey, String lockValue, Long lockTimeOut) {

        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/getLock.lua")));
        redisScript.setResultType(Boolean.class);

        Boolean result = redisTemplate.execute(redisScript, lockKey, lockValue, lockTimeOut);

        return result;

    }

    public static boolean releaseLock22(StringRedisTemplate redisTemplate, List<String> lockKey, String lockValue) {

        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/releaseLock.lua")));
        redisScript.setResultType(Boolean.class);

        Boolean result = redisTemplate.execute(redisScript, lockKey, lockValue);
//        RedisSerializer argsSerialize = new JSONSerializer();
//        RedisSerializer resultSerializer = new JSONSerializer();

//        Boolean result = redisTemplate.execute(redisScript, argsSerialize, resultSerializer, lockKey, lockValue);

        return result;

    }

}
