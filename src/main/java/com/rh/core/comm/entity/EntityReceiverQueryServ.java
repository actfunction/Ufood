package com.rh.core.comm.entity;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.send.SendUtils;

/**
 * 我签收情况查询
 * @author wanglong
 *
 */
public class EntityReceiverQueryServ extends CommonServ {
    
    /**
     * 
     * @param paramBean 参数
     * @return 参数结果
     */
    public OutBean qianShou(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        if (paramBean.isNotEmpty("entityIds")) {
            String entityIds = paramBean.getStr("entityIds");
            String[] ids = entityIds.split(",");
            UserBean userBean = Context.getUserBean();
            for (String id : ids) {
                SendUtils.qianShouAll(id, userBean);
            }
            outBean.setOk();
        } else {
            outBean.setError();
        }
        return outBean;
    }
}
