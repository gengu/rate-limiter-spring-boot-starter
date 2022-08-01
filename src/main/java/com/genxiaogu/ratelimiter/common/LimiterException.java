package com.genxiaogu.ratelimiter.common;

/**
 * @author genxiaogu
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
