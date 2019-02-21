package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

/**
 * 主办单位群组维护扩展类
 * 
 * @author guyoucheng
 *
 */
public class SupServOfficeHostDeptGroup extends CommonServ {

	
		
	/**
	 * 查询当前单位编码所在单位的TDEPT_CODE
	 * @param paramBean
	 * @return
	 */
	public OutBean queryOdeptCode(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String hostGroupDeptCodes = paramBean.getStr("hostGroupDeptCodes");
		String [] hostGroupDeptCode = hostGroupDeptCodes.split(",");
		int i = 0;
		int j = 0;
		for (String sp : hostGroupDeptCode) {
			//查SY_ORG_DEPT表的记录
			Bean bean = ServDao.find("OA_SY_ORG_DEPT",sp);
			//获得司编码
			String tDeptCode = bean.getStr("TDEPT_CODE");
			if (sp.equals(tDeptCode)) {
				i=i+1;
			}else{
				j=j+1;
			}
		}
        outBean.set("i", i);
        outBean.set("j", j);
		return outBean;
	}
	
}
