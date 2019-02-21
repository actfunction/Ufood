package com.rh.food.util.thread;

import java.util.concurrent.Callable;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.gw.gdjh.exception.BaseException;


 
/**
 * 工人类<br>
 * 
 *@author  zhangwei
 */
class Worker<T, R> implements Callable<Result<R>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);
    private String name;
    private T t;
    private Callback<T, R> callback;
    private Message message;

    public Worker(String name, Callback<T, R> callback, T t, Message message) {
        this.name = name;
        this.t = t;
        this.callback = callback;
        this.message = message;
    }

    @Override
    public Result<R> call() throws Exception {
        return new Result<R>(work(), name);
    }

    public R work() throws BaseException {
        R result = null;
        try {
            LOGGER.debug("[{}][{}][Begin]{}", new Object[] { callback.getName(), name, t });

            result = callback.call(t, message);

            boolean success = true;

            if (result instanceof Boolean) {
                success = BooleanUtils.isTrue((Boolean) result);
            }

            LOGGER.debug("[{}][{}][End: {}]{}", new Object[] { callback.getName(), name,
                    success ? "success" : "failed", t });

        } catch (Exception ex) {
            LOGGER.debug("[{}][{}][End: exception-'{}']{}",
                    new Object[] { callback.getName(), name, ex.getMessage(), t });
            throw new BaseException(name, ex);
        }

        return result;
    }

    @Override
    public String toString() {
        return "Worker for " + t;
    }
}

class Result<R> {

    private R r;
    private String workName;

    public Result(R result, String workName) {
        this.r = result;
        this.workName = workName;
    }

    public R getResult() {
        return r;
    }

    public String getWorkName() {
        return workName;
    }

}