package com.rh.core.serv.send;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;

/**
 * 
 * @author wanglong 
 *
 */
public class SendSchemeMgr {
    /**
     * 
     * @return 查找分发方案
     */
    public static List<Bean> findScheme() {
        UserBean userBean = Context.getUserBean();
        SqlBean sql = new SqlBean();
        sql.appendWhere(" AND (S_PUBLIC = 1 OR S_USER = ? ) ", userBean.getCode());
        sql.and("S_FLAG", Constant.YES_INT);
        sql.and("O_DEPT", userBean.getODeptCode());
        sql.orders("SEND_NAME");

        return ServDao.finds(ServMgr.SY_COMM_SEND, sql);
    }
}
