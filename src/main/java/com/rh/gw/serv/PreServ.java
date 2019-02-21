package com.rh.gw.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;


/**
 * 审理司时效服务的扩展类
 *
 * @author kfzx-linll
 * @date 2018/12/26
 */
public class PreServ extends CommonServ {
	
	
	public OutBean savePre(ParamBean paramBean) {
		OutBean out = new OutBean();
		String preSx = paramBean.getStr("PRE_SX");
		Bean dataBean = getPreSxData();
		dataBean.set("PRE_SX", preSx);
		ServDao.save(paramBean.getServId(), dataBean);
		return out.setOk();
	}
	
	
	/**
	 * 审理司时效
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getPreContent(ParamBean paramBean) {
        // 构建返回值参数
        OutBean outBean = new OutBean();
        Bean preSxBean = getPreSxData();
        String preSx = preSxBean.getStr("PRE_SX");
        
        outBean.set("PRE_SX", preSx);
        outBean.set("PRE_ID", preSxBean.getId());

        return outBean;
    }

	
	/**
	 * 
	 * 
	 * @return
	 */
	private Bean getPreSxData() {
        UserBean userBean = Context.getUserBean();
        String deptCode = userBean.getODeptCode();

        // 构建sql语句
        Bean dataBean = ServDao.find("OA_GW_GONGWEN_PRESCRIPTION", new ParamBean().set("S_ODEPT", deptCode));
        if (null == dataBean) {
        	dataBean = new Bean();
        }
		return dataBean;
	}

}
