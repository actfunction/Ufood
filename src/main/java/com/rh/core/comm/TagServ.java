package com.rh.core.comm;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;

/**
 * 标签服务类
 * 
 * @author wanglong 我的收藏夹如果做删除系统标签操作的话判断是否为管理员角色
 * 
 */
public class TagServ extends CommonServ {

    @Override
    protected void beforeDelete(ParamBean paramBean) {
        String servId = paramBean.getServId();
        ServDefBean servDef = ServUtils.getServDef(servId);
        //获取将要被删除的数据列表
        List<Bean> dataList;
        if (paramBean.getId().length() > 0) {
            dataList = ServDao.finds(servId, " and " + servDef.getPKey() + " in ('"
                    + paramBean.getId().replaceAll(",", "','") + "')");
        } else {
            dataList = new ArrayList<Bean>();
        }
        String userCode = Context.getUserBean().getCode();
        UserBean userBean = Context.getUserBean();
        
        //判断当前用户是否有权限删除这些数据，无权限则报错。
        for (Bean dataBean : dataList) {
            final String tagLevel = dataBean.getStr("TAG_LEVEL");
            final String sUserCode = dataBean.getStr("S_USER");
            if (tagLevel.equals("PRIVATE") && sUserCode.equals(userCode)) {
                // 如果是删除本人的数据，则允许
                return;
            } else if ((tagLevel.equals("SYS") && userBean.isAdminRole())) {
                // 如果是管理员删除公共数据，则允许
                return;
            } else {
                throw new TipException("删除失败：您没有权限删除此数据。");
            }
        }
    }
}
