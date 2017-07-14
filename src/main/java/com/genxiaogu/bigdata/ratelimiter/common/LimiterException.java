package com.genxiaogu.bigdata.ratelimiter.common;

/**
 * Created by wb-lz260260 on 2017/7/4.
 */
public class LimiterException extends RuntimeException{
    private String msg;

    public LimiterException() {
        super();
    }

    public LimiterException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

}
