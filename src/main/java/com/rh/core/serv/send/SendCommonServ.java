/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.send;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;

/**
 * send scheme service extends <CODE>CommonServ</CODE>
 * 
 * @author wanglong
 */
public class SendCommonServ extends CommonServ {
    /** 所选择的角色KEY */
    protected static final String TARGET_ROLE = "TARGET_ROLE";

    /** 本机构用户 KEY */
    protected static final String TARGET_USERS = "TARGET_USERS";

    /** 所选择的本机构部门 KEY */
    protected static final String TARGET_DEPTS = "TARGET_DEPTS";

    /**
     * get target send users params
     * 
     * @param paramBean paran Bean
     * @return String[] target sends user ids
     */
    protected String[] getUsersParam(Bean paramBean) {
        String users = paramBean.getStr(TARGET_USERS).trim();
        String[] usersArray = users.split(",");
        return usersArray;
    }

    /**
     * get target send departments params
     * 
     * @param paramBean paran Bean
     * @return String[] target sends department ids
     */
    protected String[] getDeptsParam(Bean paramBean) {
        if (paramBean.isEmpty(TARGET_DEPTS)) {
            return null;
        }
        
        String depts = paramBean.getStr(TARGET_DEPTS);
        String[] deptsArray = depts.split(",");
        return deptsArray;
    }

    /**
     * get target send role param
     * 
     * @param paramBean paran Bean
     * @return String[] target send role id
     */
    protected String getRoleParam(Bean paramBean) {
        String roleId = paramBean.getStr(TARGET_ROLE);
        return roleId;
    }

    /**
     * set out bean
     * 
     * @param outBean outBean
     * @param details scheme details
     */
    protected void setOutput(Bean outBean, List<Bean> details) {
        // set into scheme bean
        String users = "";
        String depts = "";
        String role = "";
        for (Bean detail : details) {
            int type = detail.getInt("DETAIL_TYPE");
            String code = detail.getStr("ROLE_USER_CODE");
            if (1 == type) {
                users += code + ",";
            } else if (2 == type) {
                String dept = detail.getStr("S_DEPT");
                depts += dept + ",";
                role = code;
            }
        }
        // delete the last ','
        if (users.endsWith(",")) {
            users = users.substring(0, users.length() - 1);
        }
        if (depts.endsWith(",")) {
            depts = depts.substring(0, depts.length() - 1);
        }
        outBean.set(TARGET_USERS, users);
        outBean.set(TARGET_DEPTS, depts);
        outBean.set(TARGET_ROLE, role);
    }
}
