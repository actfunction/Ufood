package com.rh.core.org.auth.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;

/**
 * 登录日志信息监听
 * @author wanglong
 */
public class LoginInfoListener {
    private Log log = LogFactory.getLog(LoginInfoListener.class);
    /**
     * 登录前
     * @param paramBean 参数
     */
    public void beforeLogin(ParamBean paramBean) {
        log.info("用户[" + paramBean.getStr("loginName") + "]开始登录系统");
    }
    /**
     * 注销前
     * @param paramBean 参数
     */
    public void beforeLogout(ParamBean paramBean) {
        UserBean user = Context.getUserBean();
        if (user != null) {
            log.info("用户[" + user.getCode() + "]退出系统");
        }
    }
}
