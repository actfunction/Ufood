package com.rh.food.util.thread;

/**
 * 并发执行器-回调类<br>
 * 
 * @author  zhangwei
 */
public abstract class Callback<T, R> {

    private String name;

    public Callback(String name) {
        this.name = name;
    }

    /**
     * 回调函数 <br>
     *
     * @param param 作业内容
     * @param message
     * @return R
     */
    public abstract R call(T t, Message message);

    public String getName() {
        return name;
    }

}
