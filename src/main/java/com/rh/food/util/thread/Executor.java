package com.rh.food.util.thread;

import java.util.Collection;
import java.util.List;

/**
 * 执行器接口<br>
 * 
 * @author  zhangwei
 */
public interface Executor<T, R> {

    boolean execute(Collection<T> ts, Callback<T, R> callback, List<R> results);
    
    boolean execute(Collection<T> ts, Callback<T, R> callback);
}
