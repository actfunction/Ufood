package com.rh.sup.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

/**
 * 属发承办单位维护扩展类
 * 
 * @author gyc
 *
 */
public class SupServOfficeOrgDept extends CommonServ {

	public OutBean updateLeaderInspector(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String orgDeptCode = paramBean.getStr("ORG_DEPT_CODE");
		String defLeader = paramBean.getStr("IS_DEF_LEADER");
		String defInspector = paramBean.getStr("IS_DEF_INSPECTOR");
		
		//判断这个是否是默认领导，如果是，更新原有领导
		if (defLeader.equals("1")) {
			//查询本单位下是否有默认领导
			String queryDefLeader = "SELECT ORG_DEPT_CODE,ORG_USER_CODE FROM SUP_SERV_ORG_DEPT WHERE ORG_DEPT_CODE='"+orgDeptCode+"' AND IS_DEF_LEADER='"+1+"'";
			List<Bean> beanDefLeader = Transaction.getExecutor().query(queryDefLeader);
			if (beanDefLeader.size()>0) {
				String defLeaderDeptCode = beanDefLeader.get(0).get("ORG_DEPT_CODE").toString();
				String defLeaderUserCode = beanDefLeader.get(0).get("ORG_USER_CODE").toString();
				//将之前本单位的默认领导设为非默认
				String updateDefLeader = "UPDATE SUP_SERV_ORG_DEPT SET IS_DEF_LEADER=2 WHERE ORG_DEPT_CODE='"+defLeaderDeptCode+"'AND ORG_USER_CODE='"+defLeaderUserCode+"'";
				Transaction.getExecutor().execute(updateDefLeader);	
			}
		}
		//判断这个是否是默认督查员，如果是，更新原有督查员
		if (defInspector.equals("1")) {
			//查询本单位下是否有默认督查员
			String queryDefInspector = "SELECT ORG_DEPT_CODE,ORG_USER_CODE FROM SUP_SERV_ORG_DEPT WHERE ORG_DEPT_CODE='"+orgDeptCode+"' AND IS_DEF_INSPECTOR='"+1+"'";
			List<Bean> beanDefInspector = Transaction.getExecutor().query(queryDefInspector);
			if (beanDefInspector.size()>0) {
				String defInspectorDeptCode = beanDefInspector.get(0).get("ORG_DEPT_CODE").toString();
				String defInspectorUserCode = beanDefInspector.get(0).get("ORG_USER_CODE").toString();
				//将之前本单位的默认督查员设为非默认
				String updateDefInspector = "UPDATE SUP_SERV_ORG_DEPT SET IS_DEF_INSPECTOR=2 WHERE ORG_DEPT_CODE='"+defInspectorDeptCode+"'AND ORG_USER_CODE='"+defInspectorUserCode+"'";
				Transaction.getExecutor().execute(updateDefInspector);	
			}
		}
		return outBean;
		
	}
	
	
	/**
	 * 查询选中单位，若为司级，则返回领导和机构督查员；若为处级，则返回处室督查员
	 * @param paramBean
	 * @return
	 */
	public OutBean queryDeptUser(ParamBean paramBean){
		OutBean outBean = new OutBean();
		//获取所属机构类型
		int deptType = Integer.parseInt(paramBean.getStr("DeptType"));
		String deptCode = paramBean.getStr("DeptCode");
		//查SY_ORG_DEPT表的记录
		Bean bean = ServDao.find("OA_SY_ORG_DEPT",deptCode);
		//获得CODE_PATH
		String codePath = bean.getStr("CODE_PATH");
		
		if (deptType==1) {
			//督查系统管理员查询角色为领导和机构督查员
			String sql = "SELECT * FROM SY_BASE_USER_V U LEFT JOIN SY_ORG_ROLE_USER UR ON UR.USER_CODE = U.USER_CODE WHERE U.CODE_PATH LIKE '"+codePath+"%' and ((UR.ROLE_CODE = 'SUP003' OR UR.ROLE_CODE in('SUP006','SUP012')) OR ((UR.ROLE_CODE = 'SUP025' OR UR.ROLE_CODE in('SUP023','SUP024'))))";
			List<Bean> result = Transaction.getExecutor().query(sql);
			 outBean.put("list",result);
		}else if(deptType==2){
			//督查机构管理员和特派办，省厅督查系统管理员查询处室督查员
			String sql = "SELECT * FROM SY_BASE_USER_V U LEFT JOIN SY_ORG_ROLE_USER UR ON UR.USER_CODE = U.USER_CODE WHERE U.CODE_PATH LIKE '"+codePath+"%' and (UR.ROLE_CODE = 'SUP017' OR UR.ROLE_CODE = 'SUP026')";
			List<Bean> result = Transaction.getExecutor().query(sql);
			outBean.put("list",result);	
		}
		return outBean;
	}
	
	
	/**
	 * 查询单位下选中人员的角色
	 * @param paramBean
	 * @return
	 */
	public OutBean queryUserRole(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String userCode = paramBean.getStr("ORG_USER_CODE");
		
		String sql = "SELECT UR.ROLE_CODE FROM SY_ORG_ROLE_USER UR LEFT JOIN SY_BASE_USER_V U ON UR.USER_CODE = U.USER_CODE WHERE U.USER_CODE='"+userCode+"'";
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
		Bean find = ServDao.find("OA_SUP_SERV_OFFICE_ORG_DEPT",id);
   
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
		String sql = " SELECT * FROM SY_ORG_USER U,SY_ORG_DEPT D WHERE U.DEPT_CODE = D.DEPT_CODE AND U.USER_CODE = '"+userCode+"'";
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
