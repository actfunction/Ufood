package com.rh.core.comm.todo;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * 用户待办提醒方式
 * @author wanglong
 * 
 */
public class TodoRemindTypeMgr {
    /**
     * 
     * @return 当前用户设置的待办提醒方式
     */
    public static String getUserRemindType() {
        String userCode = Context.getUserBean().getCode();

        // 用户编码
        return getUserRemindType(userCode);
    }

    /**
     * 
     * @param userCode 用户编码
     * @return 指定用户的提醒方式字符串
     */
    public static String getUserRemindType(String userCode) {
        Bean bean = ServDao.find(ServMgr.SY_COMM_MSG_TYPE, userCode);
        if (bean != null) {
            return bean.getStr("RT_TYPE");
        }
        return "";
    }
}
