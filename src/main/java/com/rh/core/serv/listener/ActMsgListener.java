package com.rh.core.serv.listener;

import com.rh.core.base.Context;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.msg.MsgCenter;
import com.rh.core.util.msg.listener.ServActMsg;

/**
 * 操作操作完成，向消息中心发送消息
 * @author wanglong
 */
public class ActMsgListener {
    private String[] acts = null;
    private String serv;
    /**
     * 初始化
     * @param serv 服务
     * @param conf 配置
     */
    public void init(String serv, String conf) {
        this.serv = serv;
        if (conf.length() > 0) {
            this.acts = conf.split(",");
        }
    }
    /**
     * after监听
     * @param act 操作
     * @param param 参数
     * @param result 结果
     */
    public void after(String act, ParamBean param, OutBean result) {
        if (this.acts == null) {
            addMsg(act, param, result);
        } else {
            for (int i = 0; i < this.acts.length; i++) {
                if (this.acts[i].equals(act)) {
                    addMsg(act, param, result);
                    break;
                }
            }
        }
    }
    /**
     * 添加消息
     * @param act 操作
     * @param param 参数
     * @param result 结果
     */
    private void addMsg(String act, ParamBean param, OutBean result) {
        ServActMsg msg = new ServActMsg(this.serv, act, param, result);
        msg.setActUser(Context.getUserBean());
        MsgCenter.getInstance().addMsg(msg);
    }
}
