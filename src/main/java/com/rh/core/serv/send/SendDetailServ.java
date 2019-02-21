/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.send;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 分发服务 send service extends <CODE>CommonServ</CODE>
 * 
 * @author wanglong
 */
public class SendDetailServ extends CommonServ {

    /**
     * 撤回分发
     * 
     * @param paramBean 参数Bean
     * @return 查询结果
     */
    public OutBean undo(ParamBean paramBean) {
        SqlBean sql = new SqlBean();
        sql.andIn("SEND_ID", paramBean.getId().split(","));

        List<Bean> list = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL, sql);
        for (Bean dtlBean : list) {
            undoSingleDtl(dtlBean);
        }
        List<Bean> afterList = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL, sql);
        OutBean outBean = new OutBean(paramBean);
        outBean.set("list", afterList);
        outBean.setOk();
        return outBean;
    }

    /**
     * 对指定分发记录做收回操作
     * @param dtlBean 指定分发记录
     */
    private void undoSingleDtl(Bean dtlBean) {

        if (dtlBean.getInt("SEND_STATUS") != SendConstant.SEND_STATUS_SNEDING) {
            return;
        }

        Bean setBean = new Bean();
        setBean.set("SEND_STATUS", SendConstant.SEND_STATUS_REVOKED);
        setBean.set("RECV_TIME", DateUtils.getDatetime());
        setBean.setId(dtlBean.getId());
        ServDao.update(ServMgr.SY_COMM_SEND_DETAIL, setBean);

        Bean todoBean = new Bean();
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and TODO_OBJECT_ID2 = '");
        strWhere.append(dtlBean.getId()); // TODO_OBJECT_ID2 放的是 分发的ID，这里有可能发的多个
        strWhere.append("' and TODO_OBJECT_ID1 = '");
        strWhere.append(dtlBean.getStr("DATA_ID"));
        strWhere.append("'");

        todoBean.set(Constant.PARAM_WHERE, strWhere.toString());
        TodoUtils.destroys(todoBean);
    }

    /**
     * @param sendBean 分发Bean
     */
    public static void updateSend(Bean sendBean) {
        ServDao.update(ServMgr.SY_COMM_SEND_DETAIL, sendBean);
    }

    @Override
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        super.afterQuery(paramBean, outBean);

        List<Bean> list = outBean.getDataList();
        for (Bean bean : list) {
            final String recvUser = bean.getStr("RECV_USER");
            final String recvRealUser = bean.getStr("RECV_REAL_USER");
            if (!recvRealUser.isEmpty() && !recvRealUser.equals(recvUser)) {
                bean.set("RECV_UNAME", bean.getStr("RECV_UNAME") + " (" + bean.getStr("RECV_REAL_USER_NAME") + ")");
            }
        }
    }

}
