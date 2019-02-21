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
 * 群组用户关系服务类
 * 
 * @author wanglong
 * 
 */
public class GroupUserServ extends CommonServ {
    
    /**
     * 保存之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeSave(ParamBean paramBean) {
        Bean user = paramBean.getSaveFullData();
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
        for (Bean data : deletes) {
            //清除缓存中的用户扩展信息
            UserBean userBean = UserMgr.getCacheUser(data.getStr("USER_CODE"));
            if (userBean != null) {
                userBean.clearUserExt();
            }
        }
    }

    /**
     * 复制群组用户列表到当前群组的用户列表中
     * @param paramBean 参数
     * @return 执行结果
     */
    public OutBean copyUser(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String servId = paramBean.getServId();
        final String groupCode = paramBean.getStr("GROUP_CODE");
        String userScope = paramBean.getStr("USER_SCOPE");
        String fromGroupCode = paramBean.getStr("FROM_GROUP_CODE");
        UserBean userBean = Context.getUserBean();
        StringBuilder where = new StringBuilder(" and S_CMPY");
        where.append("='").append(userBean.getCmpyCode()).append("' and GROUP_CODE='")
            .append(fromGroupCode).append("'");
        if (userScope.equals("IN")) {
            where.append(" and ODEPT_CODE='").append(userBean.getODeptCode()).append("'");
        } else if (userScope.equals("SUB")) {
            where.append(" and CODE_PATH like '").append(userBean.getODeptCodePath()).append("%'");
        }
        ParamBean param = new ParamBean();
        param.set(Constant.PARAM_WHERE, where.toString());
        List<Bean> roleUserList = ServDao.finds(servId, param, new QueryCallback() {
            public void call(List<Bean> columns, Bean data) {
                data.set("GROUP_CODE", groupCode);
            }
        });
        if (roleUserList.size() > 0) {
            param = new ParamBean(servId).setAddFlag(true).setBatchSaveDatas(roleUserList);
            outBean = batchSave(param);
        }
        return outBean;
    }
}
