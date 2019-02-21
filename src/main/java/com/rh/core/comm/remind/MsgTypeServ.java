package com.rh.core.comm.remind;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

/**
 * 
 * @author wanglong
 * 
 */
public class MsgTypeServ extends CommonServ {

    @Override
    public OutBean byid(ParamBean paramBean) {
        Bean dataBean = ServDao.find(paramBean.getServId(), paramBean.getId());
        if (dataBean == null) {
            dataBean = new Bean();
            dataBean.set("USER_CODE", paramBean.getId());
            ServDao.save(paramBean.getServId(), dataBean);
        }

        OutBean outBean = super.byid(paramBean);
        return outBean;
    }

}
