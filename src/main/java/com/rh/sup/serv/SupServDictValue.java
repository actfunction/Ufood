package com.rh.sup.serv;

import java.util.List;
import org.pentaho.di.core.util.StringUtil;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

/**
 * 基本字典项维护扩展类
 * 
 * @author guyoucheng
 *
 */
public class SupServDictValue extends CommonServ {

	/**
	 * 查询选中记录用户的角色
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean queryUserRole(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String id = paramBean.getStr("ID");
		Bean find = ServDao.find("OA_SUP_SERV_DICT_VALUE", id);
		String user = find.getStr("S_USER");
		// 查询当前用户的角色
		String sql = "SELECT UR.ROLE_CODE FROM SY_ORG_ROLE_USER UR LEFT JOIN SY_ORG_USER U ON UR.USER_CODE=U.USER_CODE WHERE U.USER_CODE='"
				+ user + "'";
		List<Bean> result = Transaction.getExecutor().query(sql);
		outBean.put("result", result);
		return outBean;
	}

	/**
	 * 重写方法，修改查询条件，只查询督查事项类型的数据
	 */
	@Override
	protected void beforeQuery(ParamBean paramBean) {
		// 获取当前登录用户信息
		Bean userBean = Context.getUserBean();
		// 权限集合
		String roleCodes = userBean.getStr("ROLE_CODES");
		// 判断当前登录用户是否只是督查机构管理员
		if ((roleCodes.contains("SUP099")) && (!(roleCodes.contains("SUP999")))) {
			String search = "and DICT_KINDS != '004' and DICT_KINDS != '006' and DICT_KINDS != '007' and DICT_KINDS != '008' and DICT_KINDS != '009'";
			paramBean.set("_WHERE_", search);
		}
	}

	/**
	 * 批量删除列表数据
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean deleteAll(ParamBean paramBean) {
		int execute = 0;
		String pkCodes = paramBean.getStr("ITEM_TYPE_ID");
		OutBean outBean = new OutBean();
		String[] pkCodeArr = pkCodes.split(",");
		for (int i = 0; i < pkCodeArr.length; i++) {

			if (deleteItemType(pkCodeArr[i])) {
				execute++;
				outBean.setOk("成功删除" + execute + "条数据");
			} else {
				outBean.setError("你没有权限删除督查系统管理员维护的数据");
			}
		}
		return outBean;
	}

	/**
	  * 删除一条数据
	  * @param orderId
	  * @return
	  */
	 public boolean deleteItemType(String orderId) {
		 ParamBean paramBean = new ParamBean();
		 OutBean userRoles = new OutBean();
		 int execute = 0 ;
		 String roleCodes="";
		 if(StringUtil.isEmpty(orderId)) {
			 return false;
		 }else{
			paramBean.set("ID", orderId);
		    userRoles = queryUserRole(paramBean);
		   List<Bean> beans = (List<Bean>)userRoles.get("result");
		   for (Bean bean : beans) {
			 roleCodes += bean.getStr("ROLE_CODE");
		   }
		   //获取当前登录用户信息
	    	Bean userBean = Context.getUserBean();
	       //权限集合
	    	String roleCode = userBean.getStr("ROLE_CODES");
	       //判断当前登录用户是否只是督查机构管理员
	    	if ((roleCode.contains("SUP099"))&&(!(roleCode.contains("SUP999")))) {
	    		//判断该条记录是否是督查系统管理员维护的，如果是，没权限删除
	    		 if (roleCodes.contains("SUP999")) {
	 				return false;
	 			}
	    	}
			
		 }
		 try {
			 String sql="DELETE FROM SUP_SERV_DICT WHERE ID ='"+orderId+"' ";
			 execute = Transaction.getExecutor().execute(sql);
			 return true; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	 } 
	 
	 /**
	  * 判断当前登录人所在机构是否为审计署机关
	  * @param paramBean
	  * @return
	  */
	 public OutBean queryDept(ParamBean paramBean) {
		 OutBean outBean = new OutBean();
		 String tdeptCode = paramBean.getStr("tdeptCode");
		 String sql = "SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE  dept_sign != 'OT30' and dept_grade = '10' and dept_pcode = odept_code and dept_type = 1 and tdept_code = dept_code";
		 List<Bean> beans = Transaction.getExecutor().query(sql);
		 for (Bean bean : beans) {
			String deptCode = bean.getStr("DEPT_CODE");
			if (tdeptCode.equals(deptCode)) {
				return outBean.set("flag", 1);
			}
		}
		 return outBean.set("flag", 2);
	 }
	 
}
