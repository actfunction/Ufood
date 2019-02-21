package com.rh.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
/*
 * kfzx-xuyj01
 * 用户机构消息处理类
 */
public class MsgOrgUserData {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgOrgUserData.class);
	private static final String PT_ORGANIZATION= "pt_organization";
	private static final int ACTIVE= 1;
	
	//保存用户机构关系数据到临时表
	public static void saveDeptUserToTemp(Bean payload,String method)  {
		try{
			log.error("MSGDATAMODIFY saveDeptUserToTemp start USERCODE:"+payload.getStr("id"));
			SqlBean dataBean = new SqlBean();
			dataBean.set("USER_ID", payload.getStr("user_id"));
			dataBean.set("EXT_INT", payload.getInt("ext_int"));
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("PRINCIPAL_CLASS", payload.getStr("principal_class"));
			dataBean.set("PRINCIPAL_VALUE2", payload.getStr("principal_value2"));
			dataBean.set("PRINCIPAL_VALUE1", payload.getStr("principal_value1"));
			dataBean.set("EXT_STR", payload.getStr("ext_str"));
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP, dataBean);
			log.error("MSGDATAMODIFY saveDeptUserToTemp SUCCESS USERCODE:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveDeptUserToTemp ERROR:"+ e.getMessage());
		}
	}
	public static void saveDeptUserToTemp(List<Bean>orgUserList)  {
		Transaction.begin();
		try{
			log.error("MSGDATAMODIFY saveDeptUserToTemp list start ");
			for (Bean payload:orgUserList) {
				SqlBean dataBean = new SqlBean();
				dataBean.set("USER_ID", payload.getStr("user_id"));
				dataBean.set("EXT_INT", payload.getInt("ext_int"));
				dataBean.set("ID", payload.getStr("id"));
				dataBean.set("PRINCIPAL_CLASS", payload.getStr("principal_class"));
				dataBean.set("PRINCIPAL_VALUE2", payload.getStr("principal_value2"));
				dataBean.set("PRINCIPAL_VALUE1", payload.getStr("principal_value1"));
				dataBean.set("EXT_STR", payload.getStr("ext_str"));dataBean.set("METHOD",payload.getStr("method"));
				orgUserList.add(dataBean);
			}
			ServDao.creates(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP, orgUserList);
			Transaction.commit();
			log.error("MSGDATAMODIFY saveDeptUserToTemp list end ");
		}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY saveDeptUserToTemp list error: "+ e.getMessage());
		}
		Transaction.end();
	}
	public static void modifyOrgUserData(){
		Transaction.begin();
		try{
			log.error("MSGDATAMODIFY modifyOrgUserData start ");
			//新增机构用户
			//获取临时新增机构用户数据
			List<Bean>createOrgUsers =  Transaction.getExecutor().query(getSqlByMethod(MsgModifyUtil.ADD_ONE));
			log.error("MSGDATAMODIFY createOrgUsers number : " + createOrgUsers.size());
			if (createOrgUsers.size()>0){
				addOrgUserToAuth(createOrgUsers);
				addOrgUserToSy(createOrgUsers);
			}
			//删除机构用户
			SqlBean methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.DELETE_ONE);
			List<Bean>deleteOrgUsers =  ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP, methodBean);
			log.error("MSGDATAMODIFY deleteOrgUsers number : " + deleteOrgUsers.size());
			if (deleteOrgUsers.size()>0){
				deleteOrgUserToAuth(deleteOrgUsers);
				deleteOrgUserToSy(deleteOrgUsers);
			}
			
			//清空用户机构临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP);
			Transaction.commit();
			
			log.error("MSGDATAMODIFY modifyOrgUserData End");
		}catch(Exception e){
			e.printStackTrace();
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyOrgUserData ERROR:"+ e.getMessage());
		}
		Transaction.end();
	}

	public static void modifyOneOrgUserData(Bean payload,String method)  {
		log.error("MSGDATAMODIFY modifyOneOrgUserData start:"+ payload.getStr("ID")+" METHOD" + method);
		String id = payload.getStr("id");
		Transaction.begin();
		try{
			saveDeptUserToTemp(payload,method);
			if (MsgModifyUtil.ADD_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authDepUserBean = ServDao.find(MsgModifyUtil.SYS_AUTH_USER_ORG,sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("UD_CODE", id);
				Bean oaDeptUserBean = ServDao.find(MsgModifyUtil.OA_SY_ORG_DEPT_USER,sqlBean);
				if (null ==authDepUserBean ||  authDepUserBean.isEmpty()) {
					addOrgUserToAuth(payload);
				}
				if (null ==oaDeptUserBean ||  oaDeptUserBean.isEmpty()) {
					addOrgUserToSy(payload);
				}
			}

			if (MsgModifyUtil.DELETE_ONE.equals(method)) {
				deleteOrgUserToAuth(payload);
				deleteOrgUserToSy(payload);
			}
			//清空用户临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP);
			Transaction.commit();
			log.error("MSGDATAMODIFY modifyOneOrgUserData success:"+ payload.getStr("ID")+" METHOD" + method);
		}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyOneOrgUserData error:"+ payload.getStr("ID")+" METHOD" + method);
			
		}
		Transaction.end();
	}
	
	//新增用户组织机构关系到统一认证
	public static void addOrgUserToAuth(List<Bean> createOrgUsers) throws Exception{
		log.error("MSGDATAMODIFY modifyOrgUserData start addOrgUserToAuth");
		try{
			//新增用户机构关系数据
			if (createOrgUsers.size()>0){
				List<Bean> beanList = new ArrayList<Bean>();
				for (Bean payload :createOrgUsers ){
					Bean dataBean = new Bean();
					dataBean.set("USER_ID", payload.getStr("USER_ID"));
					dataBean.set("EXT_INT", payload.getInt("EXT_INT"));
					dataBean.set("ID", payload.getStr("ID"));
					dataBean.set("PRINCIPAL_CLASS", payload.getStr("PRINCIPAL_CLASS"));
					dataBean.set("PRINCIPAL_VALUE2", payload.getStr("PRINCIPAL_VALUE2"));
					dataBean.set("PRINCIPAL_VALUE1", payload.getStr("PRINCIPAL_VALUE1"));
					dataBean.set("EXT_STR", payload.getStr("EXT_STR"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					beanList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.SYS_AUTH_USER_ORG, beanList);
			}
			log.error("MSGDATAMODIFY modifyOrgUserData end addOrgUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void addOrgUserToAuth(Bean payload) throws Exception{
		log.error("MSGDATAMODIFY modifyOrgUserData start addOrgUserToAuth");
		try{
			//新增用户机构关系数据

			Bean dataBean = new Bean();
			dataBean.set("USER_ID", payload.getStr("user_id"));
			dataBean.set("EXT_INT", payload.getInt("ext_int"));
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("PRINCIPAL_CLASS", payload.getStr("principal_class"));
			dataBean.set("PRINCIPAL_VALUE2", payload.getStr("principal_value2"));
			dataBean.set("PRINCIPAL_VALUE1", payload.getStr("principal_value1"));
			dataBean.set("EXT_STR", payload.getStr("ext_str"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());

			ServDao.create(MsgModifyUtil.SYS_AUTH_USER_ORG, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgUserData end addOrgUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//新增用户组织机构关系到系统
	public static void addOrgUserToSy(List<Bean> createOrgUsers) throws Exception{
		log.error("MSGDATAMODIFY modifyOrgUserData start addOrgUserToSy");
		try{
			//新增用户机构关系数据
			if (createOrgUsers.size()>0){
				List<Bean> beanList = new ArrayList<Bean>();
				for (Bean payload :createOrgUsers ){
					String principalClass = payload.getStr("PRINCIPAL_CLASS");
					//如果不是用户组织关系直接跳过（是用户组，只需要保存至统一认证就可以）
					if (!PT_ORGANIZATION.equals(principalClass)) {
						continue;
					}
					Bean dataBean = new Bean();		
					dataBean.set("UD_CODE", payload.getStr("ID"));
					dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
					dataBean.set("USER_CODE", payload.getStr("USER_ID"));
					dataBean.set("USER_POST", payload.getStr("EXT_STR"));
					dataBean.set("DEPT_CODE", payload.getStr("PRINCIPAL_VALUE1"));
					dataBean.set("CODE_PATH", payload.getStr("CODE_PATH"));
					dataBean.set("ODEPT_CODE", payload.getStr("ODEPT_CODE"));
					//dataBean.set("EXT_INT", payload.getInt("ext_int"));
					dataBean.set("S_FLAG", ACTIVE);
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					beanList.add(dataBean);
					
					SqlBean updateBean = new SqlBean();
					updateBean.and("USER_CODE", payload.getStr("USER_ID"));
					updateBean.set("USER_SORT", payload.getInt("EXT_INT"));
					ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, updateBean);
				}
				ServDao.creates(MsgModifyUtil.OA_SY_ORG_DEPT_USER, beanList);
			}
			log.error("MSGDATAMODIFY modifyOrgUserData end addOrgUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void addOrgUserToSy(Bean payload) throws Exception{
		log.error("MSGDATAMODIFY modifyOrgUserData start addOrgUserToSy");
		try{
			//新增用户机构关系数据
			
			String principalClass = payload.getStr("principal_class");
			//如果不是用户组织关系直接跳过（是用户组，只需要保存至统一认证就可以）
			if (!PT_ORGANIZATION.equals(principalClass)) {
				return;
			}
			Bean dataBean = new Bean();	
			SqlBean sql = new SqlBean();
			sql.and("DEPT_CODE", payload.getStr("principal_value1"));
			Bean dept = ServDao.find(MsgModifyUtil.OA_SY_ORG_DEPT, sql);
			if (!dept.isEmpty()) {
				dataBean.set("CODE_PATH", dept.getStr("CODE_PATH"));
				dataBean.set("ODEPT_CODE", dept.getStr("ODEPT_CODE"));
			}else {
				dataBean.set("CODE_PATH", payload.getStr("code_path"));
				dataBean.set("ODEPT_CODE", payload.getStr("odept_code"));
			}
			dataBean.set("UD_CODE", payload.getStr("id"));
			dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
			dataBean.set("USER_CODE", payload.getStr("user_id"));
			dataBean.set("USER_POST", payload.getStr("ext_str"));
			dataBean.set("DEPT_CODE", payload.getStr("principal_value1"));
			dataBean.set("CODE_PATH", payload.getStr("code_path"));
			dataBean.set("ODEPT_CODE", payload.getStr("odept_code"));
			//dataBean.set("EXT_INT", payload.getInt("ext_int"));
			dataBean.set("S_FLAG", ACTIVE);
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
		
			ServDao.create(MsgModifyUtil.OA_SY_ORG_DEPT_USER, dataBean);
			
			SqlBean updateBean = new SqlBean();
			updateBean.and("USER_CODE", payload.getStr("user_id"));
			updateBean.set("USER_SORT", payload.getInt("ext_int"));
			ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, updateBean);
			
			log.error("MSGDATAMODIFY modifyOrgUserData end addOrgUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//删除用户机构数据到统一认证表
		public static void deleteOrgUserToAuth(List<Bean> deleteOrgUsers) throws Exception  {
			log.error("MSGDATAMODIFY modifyOrgUserData start deleteOrgUsers");
			try{
				//删除用户数据
				if (deleteOrgUsers.size()>0){
					List<String> idList = new ArrayList<String>();
					for (Bean payload :deleteOrgUsers ){
						idList.add(payload.getStr("ID"));
					}
					SqlBean dataBean = new SqlBean();
					dataBean.andIn("ID",  idList.toArray());
					ServDao.destroy(MsgModifyUtil.SYS_AUTH_USER_ORG, dataBean);
				}
				log.error("MSGDATAMODIFY modifyOrgUserData end deleteOrgUsers");
			}catch(Exception e){
				log.error("MSGDATAMODIFY deleteOrgUsers ERROR:"+ e.getMessage());
				throw e;
			}
		}
		public static void deleteOrgUserToAuth(Bean payload) throws Exception  {
			log.error("MSGDATAMODIFY modifyOrgUserData start deleteOrgUsers");
			try{
				//删除用户数据
				
				SqlBean dataBean = new SqlBean();
				dataBean.and("ID",  payload.getStr("id"));
				ServDao.destroy(MsgModifyUtil.SYS_AUTH_USER_ORG, dataBean);
				log.error("MSGDATAMODIFY modifyOrgUserData end deleteOrgUsers");
			}catch(Exception e){
				log.error("MSGDATAMODIFY deleteOrgUsers ERROR:"+ e.getMessage());
				throw e;
			}
		}
		
		//删除用户机构数据到系统用户表
		public static void deleteOrgUserToSy(List<Bean> deleteOrgUsers) throws Exception  {
			log.error("MSGDATAMODIFY modifyOrgUserData start deleteOrgUserToSy");
			try{
				//删除用户数据
				if (deleteOrgUsers.size()>0){
					List<String> idList = new ArrayList<String>();
					for (Bean payload :deleteOrgUsers ){
						idList.add(payload.getStr("ID"));
					}
					SqlBean dataBean = new SqlBean();
					dataBean.andIn("UD_CODE",  idList.toArray());
					ServDao.destroy(MsgModifyUtil.OA_SY_ORG_DEPT_USER, dataBean);
				}
				log.error("MSGDATAMODIFY modifyOrgUserData end deleteOrgUserToSy");
			}catch(Exception e){
				log.error("MSGDATAMODIFY deleteOrgUserToSy ERROR:"+ e.getMessage());
				throw e;
			}
			
		}
		public static void deleteOrgUserToSy(Bean payload) throws Exception  {
			log.error("MSGDATAMODIFY modifyOrgUserData start deleteOrgUserToSy");
			try{
				//删除用户数据
				SqlBean dataBean = new SqlBean();
				dataBean.and("UD_CODE",  payload.getStr("id"));
				ServDao.destroy(MsgModifyUtil.OA_SY_ORG_DEPT_USER, dataBean);
			
				log.error("MSGDATAMODIFY modifyOrgUserData end deleteOrgUserToSy");
			}catch(Exception e){
				log.error("MSGDATAMODIFY deleteOrgUserToSy ERROR:"+ e.getMessage());
				throw e;
			}
			
		}
	public static String getSqlByMethod(String method) {
		String sql ="select t1.USER_ID USER_ID," + 
				"       t1.EXT_INT EXT_INT," + 
				"       t1.ID ID," + 
				"       t1.PRINCIPAL_CLASS PRINCIPAL_CLASS," + 
				"       t1.PRINCIPAL_VALUE2 PRINCIPAL_VALUE2," + 
				"       t1.PRINCIPAL_VALUE1 PRINCIPAL_VALUE1," + 
				"       t1.EXT_INT EXT_INT," + 
				"       t1.EXT_STR EXT_STR," + 
				"       t2.CODE_PATH CODE_PATH," + 
				"       t2.ODEPT_CODE ODEPT_CODE" + 
				"  from sys_auth_user_org_temp t1, sy_org_dept t2" + 
				" where t1.METHOD = '"+method + "'" + 
				"   and t1.PRINCIPAL_VALUE1 = t2.DEPT_CODE" + 
				"   and t1.PRINCIPAL_CLASS = '"+ PT_ORGANIZATION+ "'" + 
				"union all " + 
				"select t1.USER_ID USER_ID," + 
				"       t1.EXT_INT EXT_INT," + 
				"       t1.ID ID," + 
				"       t1.PRINCIPAL_CLASS PRINCIPAL_CLASS," + 
				"       t1.PRINCIPAL_VALUE2 PRINCIPAL_VALUE2," + 
				"       t1.PRINCIPAL_VALUE1 PRINCIPAL_VALUE1," + 
				"       t1.EXT_INT EXT_INT," + 
				"       t1.EXT_STR EXT_STR," + 
				"       '' CODE_PATH," + 
				"       '' ODEPT_CODE" + 
				"  from sys_auth_user_org_temp t1" + 
				" where t1.METHOD = '" +method + "'" + 
				"   and t1.PRINCIPAL_CLASS != '"+ PT_ORGANIZATION+ "'";
		return sql;
	}
}
