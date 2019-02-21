package com.rh.sup.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

/**
 * 司立承办单位维护扩展类
 * 
 * @author guyoucheng
 *
 */
public class SupServBureauOrgDept extends CommonServ {

	/**
	 * 更新原有督查员
	 * @param paramBean
	 * @return
	 */
	public OutBean updateDefInspector(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String deptCode = paramBean.getStr("DeptCode");
		//更新原有督查员
		String queryIsDefInspector = "SELECT ORG_DEPT_CODE,ORG_USER_CODE FROM SUP_SERV_ORG_DEPT WHERE ORG_DEPT_CODE='"+deptCode+"'AND IS_DEF_INSPECTOR='"+1+"'";
		List<Bean> beanIsDefInspector = Transaction.getExecutor().query(queryIsDefInspector);
		if (beanIsDefInspector.size()>0) {
			String DefInsOrgDeptCode = beanIsDefInspector.get(0).get("ORG_DEPT_CODE").toString();
			String DefInsOrgUserCode = beanIsDefInspector.get(0).get("ORG_USER_CODE").toString();
			String updateDefInspector = "UPDATE SUP_SERV_ORG_DEPT SET IS_DEF_INSPECTOR=2 WHERE ORG_DEPT_CODE='"+DefInsOrgDeptCode+"'AND ORG_USER_CODE='"+DefInsOrgUserCode+"'";
			Transaction.getExecutor().execute(updateDefInspector);	
		}
		return outBean;
		
	}
	
	/**
	 * 查询当前选中单位下角色为督查员的的人
	 * @param paramBean
	 * @return
	 */
	public OutBean queryDeptUser(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String deptCode = paramBean.getStr("DeptCode");
		Bean bean = ServDao.find("OA_SY_ORG_DEPT",deptCode);
		String codePath = bean.getStr("CODE_PATH");
		String sql = "SELECT * FROM SY_BASE_USER_V U LEFT JOIN SY_ORG_ROLE_USER UR ON UR.USER_CODE = U.USER_CODE WHERE U.CODE_PATH LIKE '"+codePath+"%' and UR.ROLE_CODE = 'SUP017'";
		List<Bean> result = Transaction.getExecutor().query(sql);
        outBean.put("list",result);	
		return outBean;
	}
	
	/**
	 * 查询当前id下的维护信息
	 * @param paramBean
	 * @return
	 */
	public OutBean queryResult(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String id = paramBean.getStr("ID");
		Bean find = ServDao.find("OA_SUP_SERV_BUREAU_ORG_DEPT",id);
        
		return new OutBean().set("result", find);
	}
	
		
	/**
	 * 查询当前选中人所在的单位信息
	 * @param paramBean
	 * @return
	 */
	public OutBean queryUserDept(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String userCode = paramBean.getStr("userCode");
		String sql = "SELECT * FROM SY_ORG_USER U,SY_ORG_DEPT D WHERE U.DEPT_CODE = D.DEPT_CODE AND U.USER_CODE = '"+userCode+"'";
		List<Bean> result = Transaction.getExecutor().query(sql);
        outBean.put("list",result);	
		return outBean;
	}
	
	
	/**
	 * 查看数据库表是否已经有此记录
	 * @param paramBean
	 * @return
	 */
	public OutBean queryRecord(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String deptCode = paramBean.getStr("deptCode");
		String userCode = paramBean.getStr("userCode");
		String sql = "SELECT * FROM SUP_SERV_ORG_DEPT WHERE ORG_DEPT_CODE = '"+deptCode+"' AND ORG_USER_CODE = '"+userCode+"'";
		List<Bean> result = Transaction.getExecutor().query(sql);
        outBean.put("list",result);	
		return outBean;
	}
}
