package com.rh.gw.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.JsonUtils;

/***
 * 公文表单工具类
 * 此类专门放置处理公文标签页的方法
 * @author PeiXj
 * @version 1.0
 *
 */
public class GwExtTabUtils {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(GwExtTabUtils.class);
    
	
	// 根据前后页签的不同来删除数据库
	public OutBean deleteRalateTab(ParamBean paramBean) {
		OutBean out = new OutBean();
		SqlExecutor sqlExecutor = Context.getExecutor();
		String mainTabDataId = paramBean.getStr("dataId");// 收文稿纸ID
		String beforeTabs = paramBean.getStr("beforeTabs");// 删除之前的页签，服务编码，服务编码的形式
		String afterTabs = paramBean.getStr("afterTabs");// 删除之后的页签，服务编码，服务编码的形式
		int successCount = 0;// 执行成功的数据条数
		String errorSql = "";

		try {
			String[] deleteTabs = null;
			if (afterTabs != null && !afterTabs.isEmpty()) {
				String[] aftertabArr = afterTabs.split(",");
				for (String str : aftertabArr) {
					beforeTabs = beforeTabs.replace(str, "");// "OA_GW_GONGWEN_HQYJD,OA_GW_GONGWEN_DMSPD,";
				}
				deleteTabs = beforeTabs.split(",");
			} else if (afterTabs != null) {
				deleteTabs = beforeTabs.split(",");
			}
			if (deleteTabs != null) {
				for (String deleteTab : deleteTabs) {
					if (deleteTab != "" && !deleteTab.isEmpty()) {
						StringBuffer sql = new StringBuffer();// 删除该服务编码的数据
						sql.append("delete from ").append(deleteTab);
						sql.append(" where DATA_ID='").append(mainTabDataId).append("'");
						errorSql = sql.toString();
						successCount += sqlExecutor.execute(sql.toString());// 执行成功的数据条数
					}
				}
			}
			out.setOk("执行成功" + successCount + "条");
		} catch (Exception e) {
			log.error(e.getMessage());
			out.setError("\n\ndeleteRalateTab方法执行失败。errorSql:\n" + errorSql + "\n");
			e.printStackTrace();
		}
		return out;
	}

	// 根据流程实例ID和节点ID获得扩展表单的自定义变量
	public OutBean getTabs(ParamBean paramBean) {
		OutBean out = new OutBean();

		UserBean userBean = Context.getUserBean();// 获得当前用户

		String userDept = userBean.getDeptCode();
		List<String> userRoles = userBean.getRoleCodeList();
		

		String getVar = paramBean.getStr("getVar");//需要的变量

		// 获得自定义变量
		String sql = "SELECT cvar.VAR_CONTENT FROM  SY_WFE_CUSTOM_VAR cvar ,SY_WFE_NODE_INST inst WHERE "
				+ " cvar.proc_code = inst.proc_code " + " AND cvar.node_code = inst.node_code "
				+ " AND cvar.var_code = '" + paramBean.getStr("varCode") + "' " + " AND inst.ni_id= '"
				+ paramBean.getStr("niId") + "'";

		SqlExecutor executor = Transaction.getExecutor();
		Bean query = executor.queryOne(sql);
		if(query == null)
		{
			return out;
		}
		String var = query.getStr("VAR_CONTENT"); // 获得自定义变量

		
		List<Bean> beanList = new ArrayList<>();// 将自定义变量转成对象
		try {
			beanList = JsonUtils.toBeanList(var);
		} catch (Exception e) {
			out.setError("自定义变量格式不正确");
			e.printStackTrace();
		}

		try {
			for (Bean bean : beanList) {
				String depts = bean.getStr("depts");
				String roleStr = bean.getStr("roles");
				if (depts.equals("") || depts.contains(userDept)) {
					String[] roles = null;
					if (!roleStr.equals("")) {
						roles = bean.getStr("roles").split(",");
						for (String userRole : userRoles) {
							for (String role : roles) {
								if (userRole.equals(role)) {
									out.set(getVar, bean.get(getVar));
									break;
								}
							}
						}
					} else {
						out.set(getVar, bean.get(getVar));
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}
}
