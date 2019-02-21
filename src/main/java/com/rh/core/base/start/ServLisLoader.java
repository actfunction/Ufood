package com.rh.core.base.start;

import com.rh.core.serv.listener.ServLisMgr;

/**
 * 服务监听加载器
 * @author wanglong
 */
public class ServLisLoader {
    /**
     * 加载服务监听
     */
    public void start() {
        ServLisMgr.getInstance().init();
        System.out.println("Service listener is OK!..............");
    }
    /**
     * 销毁
     */
    public void stop() {
    }
}
