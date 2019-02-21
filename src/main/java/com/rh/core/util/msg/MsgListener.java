package com.rh.core.util.msg;

/**
 * 消息监听
 * @author wanglong
 */
public interface MsgListener {
    /**
     * 初始化
     * @param conf 配置
     */
    void init(String conf);
    /**
     * 处理消息
     * @param msg 消息
     */
    void onMsg(Msg msg);
}
