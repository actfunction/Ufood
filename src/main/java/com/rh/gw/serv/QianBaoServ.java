package com.rh.gw.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

public class QianBaoServ extends GwExtServ{
    /**
     * 
     * 会签人签名的方法
     * 
     * */
	public OutBean cosignUserSign(ParamBean param){
		OutBean out = new OutBean();
		UserBean userBean = Context.getUserBean();//当前登录人
		String tDeptCode = userBean.getTDeptCode();//当前登录人司局名称
		String currentUser = userBean.getStr("USER_CODE");
		String oldCosignUser = param.getStr("oldCosignUser");//旧的会签人签名
		String replaceStr = oldCosignUser.replace(",", "','");
		
		//判断之前的签名中有没有和当前登录人司局相同的人
		String findUserSql = "SELECT USER_CODE " +
							 "FROM PLATFORM.SY_ORG_USER_V  " +
							 "WHERE USER_CODE IN ('" + replaceStr + "') " +
							 "AND TDEPT_CODE='" + tDeptCode + "'";
		
		try {
			String newCosignUser;//新的签名
			SqlExecutor executor = Transaction.getExecutor();
			
			if(oldCosignUser.length() > 0){//旧的签名存在				
				Bean one = executor.queryOne(findUserSql);
				if(one != null){//之前的签名中有和当前登录人司局相同的人
					String userCode = one.getStr("USER_CODE");
					newCosignUser = oldCosignUser.replace(userCode, currentUser);
				}
				else{
					newCosignUser = oldCosignUser + "," + currentUser;
				}
			}
			else{//旧的签名不存在
				newCosignUser = currentUser;
			}			
			
			String dataId = param.getStr("dataId");
			String updateSql = "UPDATE OA_GW_GONGWEN " + 
							   "SET GW_COSIGN_USER='" + newCosignUser + "' " + 
							   "WHERE GW_ID='" + dataId + "'";
			int i = executor.execute(updateSql);
			System.out.println(i);
			if(i > 0){
				out.setOk("签名成功");
			}
		} catch (Exception e) {
			out.setError("签名失败");
		}
		return out;
	}
	
	public OutBean getUserTDept(ParamBean param){
		OutBean out = new OutBean();
		UserBean userBean = Context.getUserBean();//当前登录人
		String tDeptName = userBean.getTDeptName();
		out.set("tDeptName", tDeptName);
		return out;
	}
}
