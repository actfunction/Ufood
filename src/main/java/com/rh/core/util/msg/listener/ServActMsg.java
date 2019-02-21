package com.rh.core.util.msg.listener;

import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.msg.CommonMsg;

/**
 * 服务操作消息
 * @author wanglong
 */
public class ServActMsg extends CommonMsg {
    /**
     * suid
     */
    private static final long serialVersionUID = -271700683227239036L;

    /**
     * 服务操作消息
     * @param serv 服务
     * @param act 操作
     * @param param 参数bean
     * @param result 结果
     */
    public ServActMsg(String serv, String act, ParamBean param, OutBean result) {
        this(serv, act);
        this.set("PARAM", param);
        this.set("OUT", result);
    }
    
    private ServActMsg(String serv, String act) {
        super(serv, act);
    }

    /**
     * 获取参数
     * @return 参数
     */
    public ParamBean getParam() {
        return (ParamBean) this.getBean("PARAM");
    }

    /**
     * 获取结果
     * @return 结果
     */
    public OutBean getOut() {
        return (OutBean) this.getBean("OUT");
    }

    /**
     * 
     * @return 做此操作的用户
     */
    public UserBean getActUser() {
        return (UserBean) this.getBean("__ACT_USER");
    }

    /**
     * 
     * @param actUser 做此操作的用户
     */
    public void setActUser(UserBean actUser) {
        this.set("__ACT_USER", actUser);
    }
}
