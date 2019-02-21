package com.rh.core.wfe.util;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.db.WfNodeUserDao;

/**
 * 流程状态帮助器
 * @author wanglong
 * 
 */
public class WfUserState {

    private List<Bean> userStateList = new ArrayList<Bean>();

    private StringBuilder nodeNames = new StringBuilder();

    private StringBuilder userCodes = new StringBuilder();

    /**
     * 
     * @param wfProc 工作流对象
     */
    public WfUserState(WfProcess wfProc) {
        // 如果流程正在执行过程中，则修改活动点数据，否则清空活动点数据。
        List<WfAct> runningWfActs = wfProc.getRunningWfAct();
        for (WfAct runWfAct : runningWfActs) {
            Bean nodeInst = runWfAct.getNodeInstBean();
            String nid = nodeInst.getId();
            
            nodeNames.append(nodeInst.getStr("NODE_NAME"));
            nodeNames.append(",");
            
            if (nodeInst.isEmpty("TO_USER_ID")) { //送了多个人 , 还没打开
                List<Bean> nodeUsers = WfNodeUserDao.getUserList(nid);
                for (Bean nodeUser: nodeUsers) {
                    String userCode = nodeUser.getStr("TO_USER_ID");
                    String userName = nodeUser.getStr("TO_USER_NAME");
                    
                    userStateList.add(createWfStatusBean(false, userCode, userName, nodeInst.getStr("NODE_NAME")));

                    setUserInfo(nodeUser.getStr("TO_USER_ID"));
                }
            } else { //单个人
                UserBean userBean = UserMgr.getUser(nodeInst.getStr("TO_USER_ID"));
                
                //适用于 这种情况 ： 送了一个人，TO_USER_ID 中有值，但是他还没打开
                boolean isOpen = false;
                if (nodeInst.isNotEmpty("OPEN_TIME")) {
                    isOpen = true;
                }
                
                userStateList.add(createWfStatusBean(isOpen, userBean.getCode(), 
                        userBean.getName(), nodeInst.getStr("NODE_NAME")));

                setUserInfo(userBean.getCode());
            }
        }

        // 去掉后面的逗号
        if (nodeNames.indexOf(",") > 0) {
            nodeNames.setLength(nodeNames.length() - 1);
        }
        if (userCodes.indexOf(",") > 0) {
            userCodes.setLength(userCodes.length() - 1);
        }

    }

    /**
     * 
     * @param userCode 用户编码
     */
    private void setUserInfo(String userCode) {
        userCodes.append(userCode);
        userCodes.append(",");
    }
    
    
    /**
     * 
     * @return 节点名称
     */
    public String getNodeNames() {
        return nodeNames.toString();
    }

    /**
     * 
     * @return 用户名称
     */
    public String getUserCodes() {
        return this.userCodes.toString();
    }

    /**
     * 
     * @return 用户状态对象
     */
    public String getUserState() {
        return JsonUtils.toJson(userStateList);
    }
    
    /**
     * 状态Bean的格式为：{"U":"用户ID","N":"用户名","O":"是否打开Y打开，N未打开"}
     * @param isOpen 是否已经打开
     * @param userCode 用户编码
     * @param userName 用户名
     * @param nodeName 节点名称
     * @return 状态Bean
     */
    private Bean createWfStatusBean(boolean isOpen, String userCode, String userName, String nodeName) {
        Bean wfStatus = new Bean();
        wfStatus.set("U", userCode);
        wfStatus.set("N", userName);
        wfStatus.set("D", nodeName);
        if (isOpen) {
            wfStatus.set("O", "Y");
        } else {
            wfStatus.set("O", "N");
        }

        return wfStatus;
    }
}
