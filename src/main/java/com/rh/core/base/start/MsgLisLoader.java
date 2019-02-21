package com.rh.core.base.start;

import com.rh.core.util.msg.MsgCenter;

/**
 * 消息监听加载器
 * @author wanglong
 */
public class MsgLisLoader {
    /**
     * 初始化消息监听
     */
    public void start() {
        MsgCenter.getInstance().init(); //初始化消息监听
        System.out.println("Message listener is OK!..............");
    }
    /**
     * 销毁
     */
    public void stop() {
    }
}
