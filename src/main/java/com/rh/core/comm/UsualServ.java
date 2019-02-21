package com.rh.core.comm;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.Constant;

/**
 * 常用语服务类
 * 
 * @author wanglong 如果做删除操作的话判断是否为管理员角色
 * 
 */
public class UsualServ extends CommonServ {

    @Override
    protected void beforeDelete(ParamBean paramBean) {
        //获取将要被删除的数据列表
        List<Bean> dataList = paramBean.getDeleteDatas();
        UserBean userBean = Context.getUserBean();
        
        // 判断当前用户是否有权限删除这些数据，无权限则报错。
        for (Bean dataBean : dataList) {
            final int publicFlag = dataBean.getInt("S_PUBLIC");
            final String sUserCode = dataBean.getStr("S_USER");

            // 如果是自己的数据，那么可以删除。
            if (sUserCode.equals(userBean.getCode())) {
                continue;
            }

            // 管理员可以删除公共数据
            if (publicFlag == Constant.YES_INT && userBean.isAdminRole()) {
                continue;
            }

            throw new TipException("删除失败：您没有权限删除此数据。");
        }
    }

    /**
     * @author wanglong
     * @param paramBean  
     * 如果做修改操作的话判断是否为管理员角色
     */
    @Override
    protected void beforeSave(ParamBean paramBean) {
        // 如果Id不为空且不是空字符串则是编辑保存操作
        if (!paramBean.getAddFlag()) { //修改模式再进行是否存在的判断
            //获取要被修改的数据
            Bean oldBean = paramBean.getSaveOldData();
            final String sUserCode = oldBean.getStr("S_USER");
            final int publicFlag = oldBean.getInt("S_PUBLIC");
            UserBean userBean = Context.getUserBean();
            
            // 如果是自己的数据，那么可以修改。
            if (sUserCode.equals(userBean.getCode())) {
                return;
            }

            // 管理员可以修改公共数据
            if (publicFlag == Constant.YES_INT && userBean.isAdminRole()) {
                return;
            }
            
            throw new TipException("您没有权限修改此数据。");
        }
    }
}
