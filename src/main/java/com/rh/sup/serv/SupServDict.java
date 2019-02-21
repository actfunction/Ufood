package com.rh.sup.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 
 * 查询不同机构下维护的某类字典项值
 * @author guyoucheng
 *
 */
public class SupServDict extends CommonServ {

	/**
	 * 查询机构下维护的某类字典项值
	 * @param paramBean
	 * @return
	 */
	public OutBean queryDicts(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String odeptCode = paramBean.getStr("ODEPT_CODE");
		String dictKinds = paramBean.getStr("DICT_KINDS");
		
		String sql = "SELECT * FROM SUP_SERV_DICT WHERE S_ODEPT = '"+odeptCode+"' AND DICT_KINDS ='"+dictKinds+"' ORDER BY DICT_ORDER DESC";
		List<Bean> result = Transaction.getExecutor().query(sql);
        outBean.put("list",result);	
		return outBean;
	}
}
