package com.rh.core.comm.entity;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;


/**
 * 我关注的事项
 * @author wanglong
 *
 */
public class EntityAttention extends StateBase {
    @Override
    protected void beforeQuery(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        
        StringBuilder strWhere = new StringBuilder(); 

        strWhere = getAttWhere(strWhere, userBean);
        
        //指定服务
        strWhere.append(getServIdWhere(paramBean));
        
        paramBean.set("_extWhere", strWhere.toString());
    }
    
    /**
     * 
     * @param strWhere where条件
     * @param userBean 用户Bean
     * @return 条件
     */
    private StringBuilder getAttWhere(StringBuilder strWhere, UserBean userBean) {
        strWhere.append(" and DATA_ID in (select DATA_ID from ").append(ServMgr.SY_COMM_ATTENTION);
        strWhere.append(" where S_USER = '").append(userBean.getCode()).append("'").append(")");
        
        
        return strWhere;
    }
}
