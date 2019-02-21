package com.rh.core.comm.entity;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.db.ServDataDao;
import com.rh.core.wfe.db.WfNodeUserDao;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 部门在办/个人在办  -- 查询 SY_COMM_ENTITY 但是需要关联表 SY_WFE_NODE_INST
 * @author wanglong
 *
 */
public class EntityDoing extends StateBase {
    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 
     */
    public void beforeQuery(ParamBean paramBean) {
        String servId = super.getServId(paramBean);
        int queryType = super.getQueryType(paramBean);
        
        UserBean userBean = Context.getUserBean();
        
        StringBuilder strWhere = new StringBuilder(); 
        //查询方式是查个人还是部门
        if (queryType == QUERY_PERSONAL) {
            strWhere = getActInstWhere(strWhere, userBean);
        } else if (queryType == QUERY_DEPT) {
            strWhere = getActInstDeptWhere(strWhere, userBean, servId);
        }
        
        //指定服务
        strWhere.append(getServIdWhere(paramBean));
        
        paramBean.set("_extWhere", strWhere.toString());
    }
    
    /**
     * 在办的，查的是实例表的  , 节点实例在运行状态  并且 节点人上有这个人
     * @param strWhere 查询条件
     * @param userBean 用户Bean
     * @return 流经表中的信息
     */
    private StringBuilder getActInstWhere(StringBuilder strWhere, UserBean userBean) {
        strWhere.append(" and ");
        strWhere.append(ServDataDao.SERV_DATA_PROC_ID);
        strWhere.append(" in (select PI_ID from ");  
        strWhere.append(WfNodeUserDao.SY_WFE_NODE_USERS_V);
        strWhere.append(" where NODE_IF_RUNNING = ");
        strWhere.append(WfeConstant.NODE_IS_RUNNING);
        strWhere.append(" and TO_USER_ID = '").append(userBean.getCode()).append("'");
        strWhere.append(")");
        
        return strWhere;
    }
    
    /**
     * 在办的，查的是实例表的  , 角色的过滤条件是部门 + 角色
     * @param strWhere 查询条件
     * @param userBean 用户Bean
     * @param servId 服务ID
     * @return 流经表中的信息
     */
    private StringBuilder getActInstDeptWhere(StringBuilder strWhere, UserBean userBean, String servId) {
        
        //节点是活动的
        strWhere.append(" and ");
        strWhere.append(ServDataDao.SERV_DATA_PROC_ID);
        strWhere.append(" in (select PI_ID from ");
        strWhere.append(WfNodeUserDao.SY_WFE_NODE_USERS_V);
        strWhere.append(" where NODE_IF_RUNNING = ");
        strWhere.append(WfeConstant.NODE_IS_RUNNING);
        strWhere.append(" and TO_DEPT_ID = '").append(userBean.getTDeptCode()).append("'");
        strWhere.append(") ");
        
        return strWhere;
    }
}
