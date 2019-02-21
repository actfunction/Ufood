package com.rh.core.org.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 用户中心类
 * @author wanglong
 *
 */
public class UserCenter extends CommonServ {
    /**
     * 显示本用户中心
     * @param paramBean 入参
     * @return OutBean 跳转页
     */
    public OutBean show(ParamBean paramBean) {
        ParamBean query = new ParamBean();
        query.set(Constant.PARAM_WHERE, " and USER_CODE='" + Context.getUserBean().getCode() + "'");
        Bean res = ServDao.find(paramBean.getServId(), query);
        String pkCode = "";
        if (res != null) {
            pkCode = res.getId();
        }
        OutBean outBean = new OutBean();
        return outBean.setToDispatcher("/sy/base/view/stdCardView.jsp?sId=" + paramBean.getServId() + "&pkCode="
                + pkCode);
    }
    
    /**
     * 显示指定用户的用户中心
     * @param paramBean - 传入参数userId
     * @return - 返回值
     */
    public OutBean showOther(ParamBean paramBean) {
        ParamBean query = new ParamBean();
        query.set(Constant.PARAM_WHERE, " and USER_CODE='" + paramBean.getStr("userId") + "'");
        Bean res = ServDao.find(paramBean.getServId(), query);
        String pkCode = "";
        if (res != null) {
            pkCode = res.getId();
        }
        OutBean outBean = new OutBean();
        return outBean.setToDispatcher("/sy/base/view/stdCardView.jsp?sId=" + paramBean.getServId() + "&pkCode="
                + pkCode);
    }
}
