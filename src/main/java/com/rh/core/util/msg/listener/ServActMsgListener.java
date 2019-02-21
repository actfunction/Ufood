package com.rh.core.util.msg.listener;

import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.MsgListener;



/**
 * 服务器操作监听
 * @author wanglong
 */
public abstract class ServActMsgListener implements MsgListener {
    private String serv;
    private String act;
    /**
     * 服务操作消息监听
     * @param serv 服务
     */
    public ServActMsgListener(String serv) {
        this(serv, "");
    }
    /**
     * 服务操作消息监听
     * @param serv 服务
     * @param act 操作
     */
    public ServActMsgListener(String serv, String act) {
        this.serv = serv;
        this.act = "," + act + ",";
    }
    /**
     * 获取服务
     * @return 服务
     */
    public String getServ() {
        return serv;
    }
    /**
     * 获取操作
     * @return 操作
     */
    public String getAct() {
        return act;
    }
    @Override
    public void init(String conf) {
    }
    @Override
    public void onMsg(Msg msg) {
        if (msg instanceof ServActMsg) {
            ServActMsg servActMsg = (ServActMsg) msg; 
            if (servActMsg.getType().equals(this.serv) 
                    && (this.act.length() == 2 || this.act.indexOf("," + servActMsg.getName() + ",") >= 0)) {
                onActMsg(servActMsg);
            }
        }
    }
    /**
     * 处理操作消息
     * @param actMsg 服务操作消息
     */
    abstract void onActMsg(ServActMsg actMsg);
}
