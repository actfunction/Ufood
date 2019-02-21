package com.rh.core.comm;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;

/**
 * 对应服务：SY_COMM_TODO_AGENT
 */
public class AgentToDoServ extends CommonServ {

    /**
     * 查询前添加查询条件
     */
    protected void beforeQuery(ParamBean paramBean) {
        StringBuilder strWhere = new StringBuilder(" and (1=2"); //方便OR语句

        //1.取得委托人列表
        OutBean userBean = ServMgr.act(ServMgr.SY_ORG_USER_TYPE_AGENT,
                "getAgtUser", paramBean);

        //2.取得每一个委托人的where条件，合并多个人的条件
        for (Bean user : userBean.getDataList()) {
            OutBean agtWhereBean = ServMgr.act(ServMgr.SY_ORG_USER_TYPE_AGENT,
                    "getTodoAgentWhereByUserCode",
                    new ParamBean().set("AGT_USER_CODE", user.getStr("aCode")));
            if (agtWhereBean != null && agtWhereBean.isOk()) {
                strWhere.append(" or (1=1 ").append(agtWhereBean.getData()).append(")");
            }
        }
        strWhere.append(")");

        strWhere.append(" and TODO_CATALOG <= 1");

        String extWhere = paramBean.getStr("_extWhere");
        extWhere = extWhere + strWhere.toString();

        paramBean.setServId(ServMgr.SY_COMM_TODO);
        paramBean.set("_extWhere", extWhere);
    }
}
