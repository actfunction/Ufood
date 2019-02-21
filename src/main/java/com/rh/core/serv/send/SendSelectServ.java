package com.rh.core.serv.send;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * 分发选择服务
 *
 */
public class SendSelectServ extends CommonServ {

    @Override
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        
        List<Bean> result = outBean.getList(Constant.RTN_DATA);
        for (int i = 0; i < result.size(); i++) {
            Bean bean = result.get(i);
            bean.set("SEND_MEMO", "分发方案");
        }
        
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(" and DEPT_PCODE = '").append(Context.getUserBean().getODeptCode());
        strBuf.append("' and S_FLAG = ").append(Constant.YES_INT);
        strBuf.append(" and DEPT_TYPE = ").append(Constant.YES_INT);

        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, strBuf.toString());
        queryBean.set(Constant.PARAM_ORDER, " DEPT_SORT ");

        List<Bean> list = ServDao.finds(ServMgr.SY_ORG_DEPT_ALL, queryBean);
        for (int i = 0; i < list.size(); i++) {
            Bean deptBean = list.get(i);
            Bean bean = new Bean();
            bean.set("SEND_NAME", deptBean.get("DEPT_NAME"));
            bean.set("SEND_ID", "DEPT-" + deptBean.get("DEPT_CODE"));
            bean.set("SEND_ORDER", deptBean.get("DEPT_SORT"));
            bean.set("SEND_MEMO", "部门");
            result.add(bean);
        }
        
        Bean pageBean = outBean.getPage();
        pageBean.set("ALLNUM", result.size());
    }

}
