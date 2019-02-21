package com.rh.core.comm.remind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.rh.core.base.Bean;
import com.rh.core.comm.msg.MsgResultBean;
import com.rh.core.comm.msg.MsgSender;
import com.rh.core.comm.msg.MsgSender.MsgItem;
import com.rh.core.comm.msg.MsgSenderFactory;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;

/** 
 * @author wanglong 
 * @version v1.0 创建时间：2013-3-27 下午8:40:30 
 * 类说明  发送提醒并记录提醒发送的状态
 */
public class RemindMsgSender {
    private Logger log = Logger.getLogger(getClass());

    /** 记录成功给用户发送提醒的提醒方式 **/
    private Map<String, String> successMap = new HashMap<String, String>();

    /** 记录不成功给用户发送提醒的提醒方式 **/
    private Map<String, String> failedMap = new HashMap<String, String>();
    
    /**
     * 发送提醒
     * @param remindBean 意见编码
     */
    public void send(Bean remindBean) {
        String type = remindBean.getStr("TYPE");

        if (StringUtils.isEmpty(type)) {
            return;
        }

        String[] remindTypes = type.split(",");

        Bean whereBean = new Bean();
        whereBean.set("REMIND_ID", remindBean.getId());
        List<Bean> waitSendList = ServDao.finds(RemindMgr.REMIND_USER_SERV_ID, whereBean);
        List<UserBean> userList = new ArrayList<UserBean>();
        if (waitSendList.size() > 0) {
            for (Bean waitSendBean : waitSendList) {
                UserBean bean = UserMgr.getUser(waitSendBean.getStr("USER_ID"));
                userList.add(bean);
            }
        }

        remindBean.set(MsgItem.RECEIVER_LIST, userList);        
        remindBean.set(MsgItem.SEND_USER, remindBean.getStr("S_USER"));
        
        // 按照提醒类型发送消息
        for (String typeCode : remindTypes) {
            sendMsgSingleType(typeCode, remindBean, userList);
        }

        // 设置用户的提醒状态
        for (UserBean user : userList) {
            String userCode = user.getCode();
            RemindMgr.modifyUserRemindStatus(userCode, remindBean.getId()
                    , successMap.get(userCode), failedMap.get(userCode));
        }
    }

    /**
     * 
     * @param typeCode 提醒方式编码
     * @param remindBean 提醒消息数据
     * @param userList 接收用户列表
     */
    private void sendMsgSingleType(String typeCode, Bean remindBean, List<UserBean> userList) {
        MsgSender ms = null;
        try {
            ms = MsgSenderFactory.getMsgSender(typeCode);
            ms.send(remindBean);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        if (ms != null) {
            for (UserBean userBean : userList) {
                String userCode = userBean.getCode();
                MsgResultBean bean = ms.getExecResult(userCode);
                if (bean != null) {
                    // 执行状态
                    boolean success = bean.isOk();
                    if (success) {
                        addSuccessType(userCode, typeCode, bean.getExecLog());
                    } else {
                        addFailedType(userCode, typeCode, bean.getExecLog());
                    }
                } else {
                    addFailedType(userCode, typeCode, "无日志");
                }
            }
        } else {
            for (UserBean userBean : userList) {
                String userCode = userBean.getCode();
                addFailedType(userCode, typeCode, "实现类不存在。");
            }
        }
    }

    /**
     * 
     * @param userCode 用户编码
     * @param remindType 提醒方式
     * @param log 日志
     */
    private void addSuccessType(String userCode, String remindType, String log) {
        addMindType(this.successMap, userCode, remindType, log);
    }

    /**
     * 
     * @param userCode 用户编码
     * @param remindType 提醒方式
     * @param log 日志
     */
    private void addFailedType(String userCode, String remindType, String log) {
        addMindType(this.failedMap, userCode, remindType, log);
    }

    /**
     * 设置用户的提醒状态
     * @param statusMap 用户状态记录Map
     * @param userCode 用户CODE
     * @param remindType 提醒方式
     * @param log   执行日志
     */
    private void addMindType(Map<String, String> statusMap, String userCode, String remindType, String log) {
        String types = statusMap.get(userCode);
        if (types == null) {
            statusMap.put(userCode, remindType + "," + log);
        } else {
            if (types.length() > 0) {
                statusMap.put(userCode, types + "|" + remindType + "," + log);
            } else {
                statusMap.put(userCode, remindType + "," + log);
            }
        }
    }  
}
