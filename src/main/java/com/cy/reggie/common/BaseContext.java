package com.cy.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用于存储用户当前登录id
 * */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置id值
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取id值
     * @return
     */
    public static Long getId(){
        return threadLocal.get();
    }
}
