package com.rh.core.comm.entity;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;

/**
 * 个人主办/部门主办 (即起草的人/部门) -- 查询 SY_COMM_ENTITY 
 * @author wanglong
 *
 */
public class EntityZhuBan extends StateBase {
    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 参数Bean
     */
    public void beforeQuery(ParamBean paramBean) {
        int queryType = super.getQueryType(paramBean);
        
        UserBean userBean = Context.getUserBean();
        
        StringBuilder strWhere = new StringBuilder(); 
        
        //查询方式是查个人还是部门
        if (queryType == QUERY_PERSONAL) {
            strWhere.append(" and S_USER = '");
            strWhere.append(userBean.getCode());
            strWhere.append("'");
        } else if (queryType == QUERY_DEPT) {
            strWhere.append(" and S_TDEPT = '");
            strWhere.append(userBean.getTDeptCode());
            strWhere.append("'");
        }
        
        //指定服务
        strWhere.append(getServIdWhere(paramBean));
        
        paramBean.set("_extWhere", strWhere.toString());
    }
}
