package com.rh.core.comm.entity;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;

/**
 * 我的收藏夹
 * @author wanglong
 *
 */
public class Bookmark extends StateBase {
    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 参数Bean
     */
    public void beforeQuery(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        
        StringBuilder strWhere = new StringBuilder(); 
        
        //指定服务
        strWhere.append(getServIdWhere(paramBean));
        
        strWhere.append(" and S_USER = '");
        strWhere.append(userBean.getCode());
        strWhere.append("'");
        
        paramBean.set("_extWhere", strWhere.toString());
    }
}
