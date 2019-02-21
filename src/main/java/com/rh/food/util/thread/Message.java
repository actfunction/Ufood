package com.rh.food.util.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程上下文类，内部包含：<br>
 * （1）description：当前线程的描述信息，比如是批量并发线程中的第几个，主要用于打印日志方便定位问题；<br>
 * （2）holder：用于传递当前线程一些上下文信息，其生命周期与当前线程一致；<br>
 * 
 * @author  zhangwei
 */
public class Message {

    private String caller;

    private Map<String, Object> holder = new HashMap<String, Object>();

    public Message() {
        super();
    }

    public Message(String caller) {
        this.caller = caller;
    }

    public String getCaller() {
        return caller;
    }

    public Object get(String key) {
        return holder.get(key);
    }

    public void put(String key, Object value) {
        holder.put(key, value);
    }
}
