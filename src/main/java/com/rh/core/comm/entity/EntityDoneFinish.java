package com.rh.core.comm.entity;


import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 已办已结
 * @author wanglong
 *
 */
public class EntityDoneFinish extends StateBase {
    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 
     */
    public void beforeQuery(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        
        StringBuilder strWhere = new StringBuilder(); 
        strWhere = getFinishedActWhere(strWhere, userBean);
            
        //指定服务
        strWhere.append(getServIdWhere(paramBean));
        
        paramBean.set("_extWhere", strWhere.toString());
    }
    
    
    /**
     * 已办已结的，查流经过的并且已经办结的
     * @param strWhere 查询条件
     * @param userBean 用户Bean
     * @return 流经表中的信息
     */
    private StringBuilder getFinishedActWhere(StringBuilder strWhere, UserBean userBean) {
        //流经本人的
        strWhere.append(" and DATA_ID in ");
        strWhere.append(" (SELECT DATA_ID");
        strWhere.append(" FROM SY_SERV_FLOW ");
        strWhere.append(" WHERE ");
        strWhere.append(" OWNER_ID = '").append(userBean.getCode()).append("'");
        strWhere.append(" and FLOW_FLAG = ").append(Constant.YES_INT).append(")");
        strWhere.append(" and S_WF_STATE = ").append(WfeConstant.WFE_NODE_INST_NOT_RUNNING);
        
        return strWhere;
    }

}
