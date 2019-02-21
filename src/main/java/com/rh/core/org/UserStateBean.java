package com.rh.core.org;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;

/**
 * 用户状态Bean，用于配合session的有效和失效进行相关在线用户的处理
 * 
 * @author wanglong
 * 
 */
public class UserStateBean extends Bean implements HttpSessionBindingListener {
    
    /**
     * sid
     */
    private static final long serialVersionUID = 815860423965561507L;
    
    /**
     * 构建体方法
     */
    public UserStateBean() {
        super();
    }
    
    /**
     * 构建体方法
     * @param userCode 用户编码
     */
    public UserStateBean(String userCode) {
        super(userCode);
    }
    
    /**
     * 构建体方法
     * @param bean 数据信息
     */
    public UserStateBean(Bean bean) {
        super(bean);
    }

    /**
     * {@inheritDoc} session绑定时加入在线用户列表
     * @param event HttpSessionBindingEvent
     */
    public void valueBound(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        this.setId(session.getId()); //把自身ID设为sessionid
        Context.addOnlineUser(this);
    }

    /**
     * {@inheritDoc} session过期失效时将当前用户清除出在线用户列表
     * @param event HttpSessionBindingEvent
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        Context.clearOnlineUser(this.getId());
    }
    
    /**
     * 获取用户对应的URL全根路径
     * @return 用户对应URL全根路径
     */
    public String getHttpUrl() {
        return this.getStr("HTTP_URL");
    }
    
    /**
     * 设置用户对应的URL全根路径
     * @param url URL全根路径
     */
    public void setHttpUrl(String url) {
        this.set("HTTP_URL", url);
    }
    
    /**
     * 
     * @return 用户编码
     */
    public String getUserCode() {
        return this.getStr("USER_CODE");
    }
    
    /**
     * 
     * @return 客户端IP地址
     */
    public String getLastIp() {
        return this.getStr("USER_LAST_IP");
    }
}
