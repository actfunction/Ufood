package com.rh.sup.serv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 参数维护类
 * 
 * @author guyoucheng
 *
 */
public class SupServSettingValueServ extends CommonServ {

	/**
	 * 保存参数值方法
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean saveSettingValue(ParamBean paramBean) {

		OutBean outBean = new OutBean();

		String HostDeptLimitTime = paramBean.getStr("HostDeptLimitTime");

		String MgrBureauRead = paramBean.getStr("MgrBureauRead");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("HostDeptLimitTime", HostDeptLimitTime);
		map.put("MgrBureauRead", MgrBureauRead);	
		
		
		for(String key:map.keySet()){
			paramBean.setServId("OA_SUP_SERV_SETTING_VALUE");
			String id = UUID.randomUUID().toString().substring(0, 22);
			paramBean.set("ID", id);
			paramBean.set("ST_KEY", key);
			paramBean.set("ST_VALUE", map.get(key));
			if (key.equals("HostDeptLimitTime")) {
				paramBean.set("ST_NAME", "牵头主办单位独立办理时限");
			}
			if (key.equals("MgrBureauRead")) {
				paramBean.set("ST_NAME", "按归口管理司局进行阅知");
			}
			save(paramBean);	
		}

	return outBean;

	}

	/**
	 * 查询参数
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean querySettingValue(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		
		String HostDeptLimitTime = paramBean.getStr("HostDeptLimitTime");
		String MgrBureauRead = paramBean.getStr("MgrBureauRead");

		String sql = "SELECT COUNT(1) as count FROM SUP_SERV_SETTING_VALUE WHERE ST_KEY = 'HostDeptLimitTime'";
		List<Bean> beans = Transaction.getExecutor().query(sql);
		if (Integer.parseInt(beans.get(0).get("COUNT").toString())>0) {
			// 更新参数信息
			updateSettingValue(paramBean);		
			
		}else {
			//保存参数信息
			saveSettingValue(paramBean);		
		}
		
		outBean.set("message", "执行成功");
		
		return outBean;

	}

	/**
	 * 修改参数值
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean updateSettingValue(ParamBean paramBean) {
		OutBean outBean = new OutBean();

		String HostDeptLimitTime = paramBean.getStr("HostDeptLimitTime");
		String MgrBureauRead = paramBean.getStr("MgrBureauRead");
		//系统变量
		String cmpyCode = paramBean.getStr("cmpyCode");
		String tdeptCode = paramBean.getStr("tdeptCode");
		String deptCode = paramBean.getStr("deptCode");
		String userCode = paramBean.getStr("userCode");
		String userName = paramBean.getStr("userName");
		String date = paramBean.getStr("date");
		String odeptCode = paramBean.getStr("odeptCode");
		
		String sql1 = "update SUP_SERV_SETTING_VALUE SET ST_VALUE='" + HostDeptLimitTime+"',S_CMPY='"+cmpyCode+"',S_TDEPT='"+tdeptCode
				+"',S_DEPT='"+deptCode+"',S_USER='"+userCode+"',S_UNAME='"+userName+"',S_MTIME='"+date+"',S_ODEPT='"+odeptCode
				+ "' WHERE ST_KEY='HostDeptLimitTime'";
		String sql2 = "update SUP_SERV_SETTING_VALUE SET ST_VALUE='" + MgrBureauRead+"',S_CMPY='"+cmpyCode+"',S_TDEPT='"+tdeptCode
				+"',S_DEPT='"+deptCode+"',S_USER='"+userCode+"',S_UNAME='"+userName+"',S_MTIME='"+date+"',S_ODEPT='"+odeptCode
				+ "' WHERE ST_KEY='MgrBureauRead'";

		String[] sql = { sql1, sql2};

		Transaction.getExecutor().executeBatch(sql);

		return outBean;

	}
	
	/**
	 * 查询参数回显
	 * @param paramBean
	 * @return
	 */
	public OutBean queryValues(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String sql = "SELECT ST_KEY,ST_VALUE FROM SUP_SERV_SETTING_VALUE";
		List<Bean> result = Transaction.getExecutor().query(sql);
        outBean.put("list",result);	
		return outBean;
	}
}
