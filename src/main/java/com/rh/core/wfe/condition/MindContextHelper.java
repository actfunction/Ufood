package com.rh.core.wfe.condition;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.comm.mind.MindUtils;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;

/**
 * 为条件流提供意见判断方面的帮助类
 * 
 * @author wanglong
 */
public class MindContextHelper {
    
    private WfAct wfAct = null;
    
    private UserBean currentUser = null;
    
    /**
     * @param aWfAct 当前节点流程实例
     * @param currUser 当前用户
     */
    public MindContextHelper(WfAct aWfAct , UserBean currUser) {
        this.wfAct = aWfAct;
        this.currentUser = currUser;
    }
    
    /**
     * 判断本部门内是否存在未生效的指定意见类型。常用于判断本次未出部门的意见。
     * @param mindCodeId 指定意见类型
     * @return 本审批单当前用户所在部门内是否存在指定类型意见
     */
    public boolean existsInDept(String mindCodeId) {
        Bean mindCodeBean = ServDao.find(ServMgr.SY_COMM_MIND_CODE, mindCodeId);
        
        if (mindCodeBean == null) {
            // 找不到指定意见类型
            return false;
        }
        
        final String deptId = this.currentUser.getTDeptCode();
        
        // 查找审批单指定类型的意见列表
        List<Bean> list = MindUtils.getMindListByCodeInDept(mindCodeId, this.wfAct.getProcess().getServId(), this.wfAct
                .getProcess().getDocId(), deptId);
        
        if (list.size() > 0) {
            Bean bean = list.get(0);
            if (bean.getInt("S_FLAG") == Constant.NO_INT) {
                // 指定部门存在此类意见，且为未启用则
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断整个审批单的意见中，是否存在指定类型的意见？ 如果存在，无论是否生效都会返回true。
     * @param mindCodeId 意见类型
     * @return 本审批单是否已存在指定类型的意见
     */
    public boolean exists(String mindCodeId) {
        
        // 查找审批单指定类型的意见列表
        List<Bean> list = MindUtils.getMindListByCode(mindCodeId, this.wfAct.getProcess().getServId(), this.wfAct
                .getProcess().getDocId());
        
        if (list.size() > 0) {
            // 部门为NULL，且列表大于0
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断整个审批单的意见中，是否存在指定类型的意见？ 如果存在，无论是否生效都会返回true。
     * @param mindCodeId 意见类型编码
     * @return 取得本审批单指定意见类型的固定意见值
     */
    public int mindValue(String mindCodeId) {
        // 取得本审批单最后一条固定意见。
        Bean mindBean = MindUtils.getLastRegularMind(this.wfAct.getProcess().getServId(), this.wfAct.getProcess()
                .getDocId(), mindCodeId, null);
        
        if (mindBean == null) {
            return 0;
        }
        
        return mindBean.getInt("MIND_VALUE");
    }
    
    /**
     * 判断当前用户所在部门未启用的固定意见的值。常用于判断本次未出部门的意见。
     * 
     * @param mindCodeId 固定意见的意见编码
     * @return 未启用的固定意见的值
     */
    public int mindValueInDept(String mindCodeId) {
        final String deptId = this.currentUser.getTDeptCode();
        
        // 取得本审批单最后一条固定意见。
        Bean mindBean = MindUtils.getLastRegularMind(this.wfAct.getProcess().getServId(), this.wfAct.getProcess()
                .getDocId(), mindCodeId, deptId);
        
        if (mindBean == null) {
            return 0;
        }
        
        // 如果已启用，则返回0
        if (mindBean.getInt("S_FLAG") == Constant.YES_INT) {
            return 0;
        }
        
        return mindBean.getInt("MIND_VALUE");
    }
}
