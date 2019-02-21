/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.plug.im;

import com.rh.core.base.Bean;

/**
 * IM通用方法接口。
 * 
 * @author wanglong
 * @version $Id$
 */
public interface ImListener {
    /**
     * 初始化IM对象
     * @return 是否初始化成功
     */
    boolean init();
    
    /**
     * 关闭IM对象
     */
    void close();
    
    /**
     * 保存部门信息，不存添加，存在修改
     * @param deptBean 部门信息
     * @return 操作是否成功
     */
    boolean saveDept(Bean deptBean);
    
    /**
     * 删除部门
     * @param deptCodes 部门编码列表，多个逗号分隔
     * @return 实际删除数量
     */
    int deleteDept(String deptCodes);
    
    /**
     * 保存用户，不存在添加，存在修改
     * @param userBean 用户信息
     * @return 是否保存成功
     */
    boolean saveUser(Bean userBean);
    
    /**
     * 删除用户
     * @param userCodes 用户编码列表，多个逗号分隔
     * @return 实际删除的数量
     */
    int deleteUser(String userCodes);
    
    /**
     * 发送普通消息提醒
     * 
     * @param receivers 接收人(多个接收人以逗号分隔)
     * @param title 消息标题
     * @param msg 消息内容
     * @return true:发送成功 false:操作不成功
     */
    boolean sendNotify(String receivers, String title, String msg);
    
    /**
     * 发送短信
     * @param sender 发送人
     * @param receivers 接收人(RTX用户或手机号码均可,最多128个)
     * @param smsInfo 短信内容
     * @return 成功返回true，失败返回false
     */
    boolean sendSms(String sender, String receivers, String smsInfo);
}
