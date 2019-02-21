package com.rh.core.comm.entity;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.db.ServDataDao;
import com.rh.core.wfe.db.WfNodeUserDao;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 已办未结 查询 SY_COMM_ENTITY
 * 未结 关联表 SY_WFE_NODE_INST 
 * @author wanglong
 *
 */
public class EntityDoneRun extends StateBase {
    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 
     */
    public void beforeQuery(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        
        StringBuilder strWhere = new StringBuilder(); 

        strWhere = getRunningActWhere(strWhere, userBean);
        
        //指定服务
        strWhere.append(getServIdWhere(paramBean));
        
        paramBean.set("_extWhere", strWhere.toString());
    }
    
    /**
     * 已办未结，查的是实例表的  , 节点实例在非运行状态  并且 节点人上有这个人
     * @param strWhere 查询条件
     * @param userBean 用户Bean
     * @return 流经表中的信息
     */
    private StringBuilder getRunningActWhere(StringBuilder strWhere, UserBean userBean) {
        strWhere.append(" and ");
        strWhere.append(ServDataDao.SERV_DATA_PROC_ID);
        strWhere.append(" in (select PI_ID from ");
        strWhere.append(WfNodeUserDao.SY_WFE_NODE_USERS_V);
        strWhere.append(" where NODE_IF_RUNNING = ");
        strWhere.append(WfeConstant.NODE_NOT_RUNNING);
        strWhere.append(" and TO_USER_ID = '").append(userBean.getCode()).append("'");
        strWhere.append(" and DONE_TYPE != 4");
        strWhere.append(")");
        
        return strWhere;
    }
}
