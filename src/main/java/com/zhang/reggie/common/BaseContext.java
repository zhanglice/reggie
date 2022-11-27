package com.zhang.reggie.common;

/**
 * 工具类：
 * 基于ThreadLocal封装，用户保存和获取当前用户id
 * 作用是线程
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
