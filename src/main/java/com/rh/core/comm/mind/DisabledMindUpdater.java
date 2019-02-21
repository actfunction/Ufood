package com.rh.core.comm.mind;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;

/**
 * 工作流由当前节点送交下一个节点时，修改未启用意见的状态等数据。
 * 
 * @author wanglong
 */
public class DisabledMindUpdater {
    
    private WfAct wfAct = null;
    
    private UserBean doUser = null;
    
    private List<Bean> mindList = null;
    
    private boolean isOtherDept = false;
    
    /**
     * @param aWfAct 工作流节点实例对象
     * @param aDoUser 办理用户
     */
    public DisabledMindUpdater(WfAct aWfAct , UserBean aDoUser) {
        this.wfAct = aWfAct;
        this.doUser = aDoUser;
        
        String dataId = aWfAct.getProcess().getDocId();
        this.mindList = MindUtils.getDisabledMindInDept(doUser.getTDeptCode(), dataId);
    }
    
    /**
     * 送下一个节点，且出部门
     */
    public void toOtherDept() {
        isOtherDept = true;
        toNextNode();
    }
    
    /**
     * 送下一个节点，但是不出部门
     */
    public void toNextNode() {
        updateTerminalMind();
        updateRegularMind();
        updateGeneralMind();
    }
    
    /**
     * @return 最终意见列表
     */
    public List<Bean> getTerminalMindList() {
        List<Bean> rtnList = new ArrayList<Bean>();
        for (Bean bean : mindList) {
            // 是否是最终意见
            if (bean.getInt("MIND_IS_TERMINAL") == Constant.YES_INT) {
                rtnList.add(bean);
            }
        }
        
        return rtnList;
    }
    
    /**
     * @return 固定意见列表
     */
    public List<Bean> getRegularMindList() {
        List<Bean> rtnList = new ArrayList<Bean>();
        for (Bean bean : mindList) {
            if (!bean.isEmpty("USUAL_ID")) {
                rtnList.add(bean);
            }
        }
        
        return rtnList;
    }
    
    /**
     * 更新最终意见数据
     */
    private void updateTerminalMind() {
        List<Bean> list = getTerminalMindList();
        for (Bean bean : list) {
            boolean ifModify = false;
            if (isOtherDept) {
                // 送下一个部门则启用意见
                bean.set("S_FLAG", Constant.YES);
                bean.set("WF_NI_ID", wfAct.getId());
                ifModify = true;
            }
            
            if (!wfAct.getNodeDef().isEmpty("MIND_TERMINAL")) {
                // 修改办理人的信息
                fillDoUser(bean);
                bean.set("WF_NI_ID", wfAct.getId());
                ifModify = true;
            }
            if (ifModify) {
                ServDao.update(ServMgr.SY_COMM_MIND, bean);
            }
        }
    }
    
    /**
     * 更新普通意见
     */
    private void updateGeneralMind() {
        for (Bean bean : mindList) {
            if (bean.getInt("MIND_IS_TERMINAL") == Constant.YES_INT || !bean.isEmpty("USUAL_ID")) {
                continue;
            }
            
            if (bean.getStr("S_USER").equals(doUser.getCode())) {
                bean.set("S_FLAG", Constant.YES);
                ServDao.update(ServMgr.SY_COMM_MIND, bean);
            }
        }
    }
    
    /**
     * 更新固定意见的数据
     */
    private void updateRegularMind() {
        List<Bean> list = getRegularMindList();
        for (Bean bean : list) {
            boolean ifModify = false;
            if (isOtherDept) {
                // 送下一个部门则启用意见
                bean.set("S_FLAG", Constant.YES);
                bean.set("WF_NI_ID", wfAct.getId());
                ifModify = true;
            }
            
            // 是否能修改固定意见
            if (!wfAct.getNodeDef().isEmpty("MIND_REGULAR")) {
                // 修改办理人的信息
                fillDoUser(bean);
                bean.set("WF_NI_ID", wfAct.getId());
                ifModify = true;
            }
            if (ifModify) {
                ServDao.update(ServMgr.SY_COMM_MIND, bean);
            }
        }
    }
    
    /**
     * 填充办理人信息到指定意见上
     * 
     * @param mindBean 意见记录
     */
    private void fillDoUser(Bean mindBean) {
        mindBean.set("S_USER", doUser.getCode());
        mindBean.set("S_UNAME", doUser.getName());
        mindBean.set("S_DEPT", doUser.getDeptCode());
        mindBean.set("S_DNAME", doUser.getDeptName());
        mindBean.set("S_TDEPT", doUser.getTDeptCode());
        mindBean.set("S_TNAME", doUser.getTDeptName());
    }
}
