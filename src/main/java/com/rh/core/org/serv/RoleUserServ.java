package com.rh.core.org.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 角色用户关系服务类
 * 
 * @author wanglong
 * 
 */
public class RoleUserServ extends CommonServ {
    
    /**
     * 保存之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeSave(ParamBean paramBean) {
        Bean user = paramBean.getSaveFullData();
        //清除菜单信息
        UserMgr.clearMenuByUsers(user.getStr("USER_CODE"));
        //清除缓存中的用户扩展信息
        UserBean userBean = UserMgr.getCacheUser(user.getStr("USER_CODE"));
        if (userBean != null) {
            userBean.clearUserExt();
        }
    }
    
    /**
     * 删除之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        List<Bean> deletes = outBean.getDataList();
        StringBuilder userCodes = new StringBuilder();
        for (Bean data : deletes) {
            userCodes.append(data.getStr("USER_CODE")).append(",");
            //清除缓存中的用户扩展信息
            UserBean userBean = UserMgr.getCacheUser(data.getStr("USER_CODE"));
            if (userBean != null) {
                userBean.clearUserExt();
            }
        }
        int len = userCodes.length();
        if (len > 0) {
            userCodes.setLength(len - 1);
            UserMgr.clearMenuByUsers(userCodes.toString());
        }
    }

    /**
     * 复制角色用户列表到当前角色的用户列表中
     * @param paramBean 参数
     * @return 执行结果
     */
    public OutBean copyRoleUser(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String servId = paramBean.getServId();
        final String roleCode = paramBean.getStr("ROLE_CODE");
        String userScope = paramBean.getStr("USER_SCOPE");
        String fromRoleCode = paramBean.getStr("FROM_ROLE_CODE");
        UserBean userBean = Context.getUserBean();
        StringBuilder where = new StringBuilder(" and CMPY_CODE");
        where.append("='").append(userBean.getCmpyCode()).append("' and ROLE_CODE='")
            .append(fromRoleCode).append("'");
        if (userScope.equals("IN")) {
            where.append(" and ODEPT_CODE='").append(userBean.getODeptCode()).append("'");
        } else if (userScope.equals("SUB")) {
            where.append(" and CODE_PATH like '").append(userBean.getODeptCodePath()).append("%'");
        }
        ParamBean param = new ParamBean();
        param.set(Constant.PARAM_WHERE, where.toString());
        List<Bean> roleUserList = ServDao.finds(servId, param, new QueryCallback() {
            public void call(List<Bean> columns, Bean data) {
                data.setId("");
                data.set("RU_ID", "");
                data.set("ROLE_CODE", roleCode);
            }
        });
        if (roleUserList.size() > 0) {
            param = new ParamBean(servId).setAddFlag(true).setBatchSaveDatas(roleUserList);
            outBean = batchSave(param);
        }
        return outBean;
    }
}
