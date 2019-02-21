package com.rh.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;

/*
 * kfzx-xuyj01
 * 用户消息处理类
 */
public class MsgUserData {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgUserData.class);
	private static final String PWD= Context.getSyConf("OA_USER_PWD", "e10adc3949ba59abbe56e057f20f883e");
	private static final int ACTIVE= 1;
	private static final int LOCKED= 2;
	//保存用户数据到临时表
	public static void saveUserToTemp(List<Bean>userList)  {
		Transaction.begin();
		try{
			log.info("MSGDATAMODIFY saveUserToTemp list start ");
			for (Bean payload:userList) {
				SqlBean dataBean = new SqlBean();
				dataBean.set("ORG_ID", payload.getStr("org_id"));
				dataBean.set("LOGIN_NAME", payload.getStr("login_name"));
				dataBean.set("TYPE", payload.getStr("type"));
				dataBean.set("POLITICAL_STATUS", payload.getStr("political_status"));
				dataBean.set("ADDRESS", payload.getStr("address"));
				dataBean.set("NATION", payload.getStr("nation"));
				dataBean.set("MARITAL_STATUS", payload.getStr("marital_status"));
				dataBean.set("GENDER", payload.getStr("gender"));
				dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
				dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
				dataBean.set("NAME", payload.getStr("name"));
				dataBean.set("PINYIN", payload.getStr("pinyin"));
				dataBean.set("SORT",payload.getInt("sort"));
				dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
				dataBean.set("ICON_ID", payload.getStr("icon_id"));
				dataBean.set("CODE",payload.getStr("code"));
				dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
				dataBean.set("IDCARD", payload.getStr("idcard"));
				dataBean.set("CLASSIFICATION", payload.getStr("classification"));
				dataBean.set("EMAIL", payload.getStr("email"));
				dataBean.set("ZHIJI", payload.getStr("zhiji"));
				dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
				dataBean.set("MOBILEPHONE", payload.getStr("mobilephone"));
				dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("USER_STATUS")));
				dataBean.set("TELEPHONE", payload.getStr("telephone"));
				dataBean.set("MEMO", payload.getStr("memo"));
				dataBean.set("ID", payload.getStr("id"));
				dataBean.set("ORG_NAME", payload.getStr("org_name"));
				dataBean.set("STATUS", payload.getStr("status"));
				dataBean.set("PASSPORT", payload.getStr("passport"));
				dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
				dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
				dataBean.set("METHOD",payload.getStr("method"));
				userList.add(dataBean);
			}
			ServDao.creates(MsgModifyUtil.SYS_AUTH_USER_TEMP, userList);
			Transaction.commit();
			log.info("MSGDATAMODIFY saveUserToTemp list end ");
		}catch(Exception e) {
			Transaction.rollback();
			log.info("MSGDATAMODIFY saveUserToTemp list error: "+ e.getMessage());
		}
		Transaction.end();
	}
	//保存用户数据到临时表
	public static void saveUserToTemp(Bean payload,String method)  {
		Transaction.begin();
		try{
			log.info("MSGDATAMODIFY saveUserToTemp start USERCODE:"+payload.getStr("id"));
			SqlBean dataBean = new SqlBean();
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("LOGIN_NAME", payload.getStr("login_name"));
			dataBean.set("TYPE", payload.getStr("type"));
			dataBean.set("POLITICAL_STATUS", payload.getStr("political_status"));
			dataBean.set("ADDRESS", payload.getStr("address"));
			dataBean.set("NATION", payload.getStr("nation"));
			dataBean.set("MARITAL_STATUS", payload.getStr("marital_status"));
			dataBean.set("GENDER", payload.getStr("gender"));
			dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
			dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("PINYIN", payload.getStr("pinyin"));
			dataBean.set("SORT",payload.getInt("sort"));
			dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
			dataBean.set("ICON_ID", payload.getStr("icon_id"));
			dataBean.set("CODE",payload.getStr("code"));
			dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
			dataBean.set("IDCARD", payload.getStr("idcard"));
			dataBean.set("CLASSIFICATION", payload.getStr("classification"));
			dataBean.set("EMAIL", payload.getStr("email"));
			dataBean.set("ZHIJI", payload.getStr("zhiji"));
			dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
			dataBean.set("MOBILEPHONE", payload.getStr("mobilephone"));
			dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("USER_STATUS")));
			dataBean.set("TELEPHONE", payload.getStr("telephone"));
			dataBean.set("MEMO", payload.getStr("memo"));
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("ORG_NAME", payload.getStr("org_name"));
			dataBean.set("STATUS", payload.getStr("status"));
			dataBean.set("PASSPORT", payload.getStr("passport"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_USER_TEMP, dataBean);
			Transaction.commit();
			log.info("MSGDATAMODIFY saveUserToTemp SUCCESS USERCODE:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveUserToTemp ERROR:"+ e.getMessage());
			Transaction.rollback();
		}
		Transaction.end();
	}
	
	//保存用户数据到临时表
	public static void modifyUserData()  {
		Transaction.begin();
		try{

			Boolean isModifyed = false;
			//新增用户
			//获取临时新增用户数据
			SqlBean methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.CREATE_ONE);
			List<Bean>createUsers =  ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
			log.info("MSGDATAMODIFY createUsers number : " + createUsers.size());
			if (createUsers.size()>0){
				addUserToAuth(createUsers);
				addUserToSy(createUsers);
				isModifyed = true;
			}
			//更新用户
			methodBean = new SqlBean();
			methodBean.appendWhere("and METHOD in (?,?)", MsgModifyUtil.UPDATE_ONE,MsgModifyUtil.UPDATE_ONE_4ADMIN);
			List<Bean>updateUsers =  ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
			log.info("MSGDATAMODIFY updateUsers number : " + updateUsers.size());
			if (updateUsers.size()>0){
				updateUserToAuth(updateUsers);
				updateUserToSy(updateUsers);
				isModifyed = true;
			}
			
			//删除用户
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.DELETE_ONE);
			List<Bean>deleteUsers =  ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
			log.info("MSGDATAMODIFY deleteUsers number : " + deleteUsers.size());
			if (deleteUsers.size()>0){
				deleteUserToAuth(deleteUsers);
				deleteUserToSy(deleteUsers);
				isModifyed = true;
			}
			//锁定用户
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.LOCK_ONE);
			List<Bean>lockUsers =  ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
			log.info("MSGDATAMODIFY lockUsers number : " + lockUsers.size());
			if (lockUsers.size()>0){
				lockUserToAuth(lockUsers);
				lockUserToSy(lockUsers);
			}
			//启用用户
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.ACTIVE_ONE);
			List<Bean>activeUsers =  ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
			log.info("MSGDATAMODIFY activeUsers number : " + activeUsers.size());
			if (activeUsers.size()>0){
				activeUserToAuth(activeUsers);
				activeUserToSy(activeUsers);
			}
			
			//清空用户临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_USER_TEMP);
			Transaction.commit();
			log.info("MSGDATAMODIFY modifyUserData End");
			//清楚缓存字典
			if (isModifyed) {
				ServDefBean servDef = ServUtils.getServDef(MsgModifyUtil.OA_SY_ORG_USER);
				servDef.clearDictCache();
			}
		}catch(Exception e){
			Transaction.rollback();
			e.printStackTrace();
			log.error("MSGDATAMODIFY modifyUserData ERROR:"+ e.getMessage());
		}
		Transaction.end();
	}
	
	public static void modifyOneUserData(Bean payload,String method)  {
		String id  = payload.getStr("id");
		Boolean isModifyed = false;
		Transaction.begin();	
		log.info("MSGDATAMODIFY modifyOneUserData start:"+ payload.getStr("ID")+" METHOD" + method);
		try {

			saveUserToTemp(payload,method);
			if (MsgModifyUtil.CREATE_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("USER_CODE", id);
				Bean oaUser = ServDao.find(MsgModifyUtil.OA_SY_ORG_USER, sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authUser =  ServDao.find(MsgModifyUtil.SYS_AUTH_USER, sqlBean);
				if (null ==authUser ||  authUser.isEmpty()) {
					addUserToAuth(payload);
				}
				if (null ==oaUser ||  oaUser.isEmpty()) {
					addUserToSy(payload);
				}
				isModifyed = true;
			}
			if (MsgModifyUtil.UPDATE_ONE.equals(method)||MsgModifyUtil.UPDATE_ONE_4ADMIN.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("USER_CODE", id);
				Bean oaUser = ServDao.find(MsgModifyUtil.OA_SY_ORG_USER, sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authUser =  ServDao.find(MsgModifyUtil.SYS_AUTH_USER, sqlBean);
				if (null ==authUser ||  authUser.isEmpty()) {
					addUserToAuth(payload);
				}else {
					updateUserToAuth(payload);
				}
				if (null ==oaUser ||  oaUser.isEmpty()) {
					addUserToSy(payload);
				}else {
					updateUserToSy(payload);
				}

				isModifyed = true;
			}
			if (MsgModifyUtil.DELETE_ONE.equals(method)) {
				deleteUserToAuth(payload);
				deleteUserToSy(payload);
				isModifyed = true;
			}
			if (MsgModifyUtil.LOCK_ONE.equals(method)) {
				lockUserToAuth(payload);
				lockUserToSy(payload);
			}
			if (MsgModifyUtil.ACTIVE_ONE.equals(method)) {
				activeUserToAuth(payload);
				activeUserToSy(payload);
			}
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_USER_TEMP);
			Transaction.commit();
			log.info("MSGDATAMODIFY modifyOneUserData start:"+ payload.getStr("ID")+" METHOD" + method);
	}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyOneUserData :"+ payload.getStr("ID") +" METHOD" + method
			+" ERROR:"+ e.getMessage());
		}
		Transaction.end();
		
		try {
			//清楚缓存字典
			if (isModifyed) {
				MsgModifyUtil.cleanAllServUserCache(id);
			}
		}catch(Exception e) {
			log.error("modifyOneUserData clean user cache error: "+e.getMessage());
		}
	}
	
	//新增用户数据到统一认证表
	public static void addUserToAuth(List<Bean> createUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start addUserToAuth");
		try{
			//新增用户数据
			if (createUsers.size()>0){
				List<Bean> userList = new ArrayList<Bean>();
				for (Bean payload :createUsers ){
					Bean dataBean = new Bean();
					dataBean.set("ORG_ID", payload.getStr("ORG_ID"));
					dataBean.set("LOGIN_NAME", payload.getStr("LOGIN_NAME"));
					dataBean.set("TYPE", payload.getStr("TYPE"));
					dataBean.set("POLITICAL_STATUS", payload.getStr("POLITICAL_STATUS"));
					dataBean.set("ADDRESS", payload.getStr("ADDRESS"));
					dataBean.set("NATION", payload.getStr("NATION"));
					dataBean.set("MARITAL_STATUS", payload.getStr("MARITAL_STATUS"));
					dataBean.set("GENDER", payload.getStr("GENDER"));
					dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("DATE_CREATED")));
					dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("LAST_UPDATED")));//time格式化
					dataBean.set("NAME", payload.getStr("NAME"));
					dataBean.set("PINYIN", payload.getStr("PINYIN"));
					dataBean.set("SORT",payload.getInt("SORT"));
					dataBean.set("NATIVE_PLACE", payload.getStr("NATIVE_PLACE"));
					dataBean.set("ICON_ID", payload.getStr("ICON_ID"));
					dataBean.set("CODE",payload.getStr("CODE"));
					dataBean.set("CERTIFICATE_TYPE", payload.getStr("CERTIFICATE_TYPE"));
					dataBean.set("IDCARD", payload.getStr("IDCARD"));
					dataBean.set("CLASSIFICATION", payload.getStr("CLASSIFICATION"));
					dataBean.set("EMAIL", payload.getStr("EMAIL"));
					dataBean.set("ZHIJI", payload.getStr("ZHIJI"));
					dataBean.set("SECRET_CODE", payload.getStr("SECRET_CODE"));
					dataBean.set("MOBILEPHONE", payload.getStr("MOBILEPHONE"));
					dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("USER_STATUS")));
					dataBean.set("TELEPHONE", payload.getStr("TELEPHONE"));
					dataBean.set("MEMO", payload.getStr("MEMO"));
					dataBean.set("ID", payload.getStr("ID"));
					dataBean.set("ORG_NAME", payload.getStr("ORG_NAME"));
					dataBean.set("STATUS", payload.getStr("STATUS"));
					dataBean.set("PASSPORT", payload.getStr("PASSPORT"));
					dataBean.set("DOMAIN_ID", payload.getStr("DOMAIN_ID"));
					dataBean.set("DOMAIN_NAME", payload.getStr("DOMAIN_NAME"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					userList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.SYS_AUTH_USER, userList);
			}
			log.info("MSGDATAMODIFY modifyUserData end addUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void addUserToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start addUserToAuth");
		try{
			//新增用户数据
			Bean dataBean = new Bean();
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("LOGIN_NAME", payload.getStr("login_name"));
			dataBean.set("TYPE", payload.getStr("type"));
			dataBean.set("POLITICAL_STATUS", payload.getStr("political_status"));
			dataBean.set("ADDRESS", payload.getStr("address"));
			dataBean.set("NATION", payload.getStr("nation"));
			dataBean.set("MARITAL_STATUS", payload.getStr("marital_status"));
			dataBean.set("GENDER", payload.getStr("gender"));
			dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
			dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("PINYIN", payload.getStr("pinyin"));
			dataBean.set("SORT",payload.getInt("sort"));
			dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
			dataBean.set("ICON_ID", payload.getStr("icon_id"));
			dataBean.set("CODE",payload.getStr("code"));
			dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
			dataBean.set("IDCARD", payload.getStr("idcard"));
			dataBean.set("CLASSIFICATION", payload.getStr("classification"));
			dataBean.set("EMAIL", payload.getStr("email"));
			dataBean.set("ZHIJI", payload.getStr("zhiji"));
			dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
			dataBean.set("MOBILEPHONE", payload.getStr("mobilephone"));
			dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("user_status")));
			dataBean.set("TELEPHONE", payload.getStr("telephone"));
			dataBean.set("MEMO", payload.getStr("memo"));
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("ORG_NAME", payload.getStr("org_name"));
			dataBean.set("STATUS", payload.getStr("status"));
			dataBean.set("PASSPORT", payload.getStr("passport"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());

			ServDao.create(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			
			log.info("MSGDATAMODIFY modifyUserData end addUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	
	
	//新增用户数据到系统用户表
	public static void addUserToSy(List<Bean> createUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start addUserToSy");
		try{
			//新增用户数据
			if (createUsers.size()>0){
				List<Bean> userList = new ArrayList<Bean>();
				for (Bean payload :createUsers ){
					Bean dataBean = new Bean();
					dataBean.set("USER_CODE", payload.getStr("ID"));
					dataBean.set("USER_LOGIN_NAME", payload.getStr("LOGIN_NAME"));
					dataBean.set("USER_NAME", payload.getStr("NAME"));
					dataBean.set("DEPT_CODE", payload.getStr("ORG_ID"));
					dataBean.set("USER_PASSWORD", PWD);
					dataBean.set("USER_SORT",payload.getInt("SORT"));
					dataBean.set("USER_MOBILE", payload.getStr("MOBILEPHONE"));
					dataBean.set("USER_EMAIL", payload.getStr("EMAIL"));
					dataBean.set("USER_POST", payload.getStr("ZHIJI"));//TODO
					dataBean.set("USER_IDCARD", payload.getStr("IDCARD"));
					dataBean.set("USER_HOME_PHONE", payload.getStr("TELEPHONE"));
					dataBean.set("USER_NATION", payload.getStr("NATION"));
					dataBean.set("USER_SEX", MsgModifyUtil.getSex(payload.getStr("GENDER")));
					dataBean.set("USER_MARRIAGE", payload.getStr("MARITAL_STATUS"));
					dataBean.set("USER_POLITICS", payload.getStr("POLITICAL_STATUS"));
					dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("USER_STATUS")));
					//dataBean.set("CMPY_CODE", payload.getStr("DOMAIN_ID"));
					dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
					dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("STATUS")));
					dataBean.set("USER_EN_NAME", payload.getStr("PINYIN"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					dataBean.set("USER_IMG_SRC", payload.getStr("ICON_ID"));
					dataBean.set("USER_EN_NAME",payload.getStr("CODE"));
					//以下为没用的数据
//					dataBean.set("TYPE", payload.getStr("type"));
//					dataBean.set("ADDRESS", payload.getStr("address"));
//					dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
//					dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
//					dataBean.set("CLASSIFICATION", payload.getStr("classification"));
//					dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
//					dataBean.set("MEMO", payload.getStr("memo"));
//					dataBean.set("ORG_NAME", payload.getStr("org_name"));
//					dataBean.set("PASSPORT", payload.getStr("passport"));
//					dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
					userList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.OA_SY_ORG_USER, userList);
			}
			log.info("MSGDATAMODIFY modifyUserData end addUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void addUserToSy(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start addUserToSy");
		try{
			//新增用户数据
			Bean dataBean = new Bean();
			dataBean.set("USER_CODE", payload.getStr("id"));
			dataBean.set("USER_LOGIN_NAME", payload.getStr("login_name"));
			dataBean.set("USER_NAME", payload.getStr("name"));
			dataBean.set("DEPT_CODE", payload.getStr("org_id"));
			dataBean.set("USER_PASSWORD", PWD);
			dataBean.set("USER_SORT",payload.getInt("sort"));
			dataBean.set("USER_MOBILE", payload.getStr("mobilephone"));
			dataBean.set("USER_EMAIL", payload.getStr("email"));
			dataBean.set("USER_POST", payload.getStr("zhiji"));//TODO
			dataBean.set("USER_IDCARD", payload.getStr("idcard"));
			dataBean.set("USER_HOME_PHONE", payload.getStr("telephone"));
			dataBean.set("USER_NATION", payload.getStr("nation"));
			dataBean.set("USER_SEX", MsgModifyUtil.getSex(payload.getStr("gender")));
			dataBean.set("USER_MARRIAGE", payload.getStr("marital_status"));
			dataBean.set("USER_POLITICS", payload.getStr("political_status"));
			dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("user_status")));
			//dataBean.set("CMPY_CODE", payload.getStr("DOMAIN_ID"));
			dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
			dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("status")));
			dataBean.set("USER_EN_NAME", payload.getStr("pinyin"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			dataBean.set("USER_IMG_SRC", payload.getStr("icon_id"));
			dataBean.set("USER_EN_NAME",payload.getStr("code"));
			dataBean.set("USER_SORT", payload.getInt("ext_int"));
			//以下为没用的数据
//					dataBean.set("TYPE", payload.getStr("type"));
//					dataBean.set("ADDRESS", payload.getStr("address"));
//					dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
//					dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
//					dataBean.set("CLASSIFICATION", payload.getStr("classification"));
//					dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
//					dataBean.set("MEMO", payload.getStr("memo"));
//					dataBean.set("ORG_NAME", payload.getStr("org_name"));
//					dataBean.set("PASSPORT", payload.getStr("passport"));
//					dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
	
			ServDao.create(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
			
			log.info("MSGDATAMODIFY modifyUserData end addUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	//修改用户数据到统一认证表
	public static void updateUserToAuth(List<Bean> updateUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start updateUserToAuth");
		try{
			//修改用户数据
			if (updateUsers.size()>0){
				for (Bean payload :updateUsers ){
					SqlBean dataBean = new SqlBean();
					dataBean.and("ID", payload.getStr("ID"));
					dataBean.set("ORG_ID", payload.getStr("ORG_ID"));
					dataBean.set("LOGIN_NAME", payload.getStr("LOGIN_NAME"));
					dataBean.set("TYPE", payload.getStr("TYPE"));
					dataBean.set("POLITICAL_STATUS", payload.getStr("POLITICAL_STATUS"));
					dataBean.set("ADDRESS", payload.getStr("ADDRESS"));
					dataBean.set("NATION", payload.getStr("NATION"));
					dataBean.set("MARITAL_STATUS", payload.getStr("MARITAL_STATUS"));
					dataBean.set("GENDER", payload.getStr("GENDER"));
					dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("DATE_CREATED")));
					dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("LAST_UPDATED")));//time格式化
					dataBean.set("NAME", payload.getStr("NAME"));
					dataBean.set("PINYIN", payload.getStr("PINYIN"));
					dataBean.set("SORT",payload.getInt("SORT"));
					dataBean.set("NATIVE_PLACE", payload.getStr("NATIVE_PLACE"));
					dataBean.set("ICON_ID", payload.getStr("ICON_ID"));
					dataBean.set("CODE",payload.getStr("CODE"));
					dataBean.set("CERTIFICATE_TYPE", payload.getStr("CERTIFICATE_TYPE"));
					dataBean.set("IDCARD", payload.getStr("IDCARD"));
					dataBean.set("CLASSIFICATION", payload.getStr("CLASSIFICATION"));
					dataBean.set("EMAIL", payload.getStr("EMAIL"));
					dataBean.set("ZHIJI", payload.getStr("ZHIJI"));
					dataBean.set("SECRET_CODE", payload.getStr("SECRET_CODE"));
					dataBean.set("MOBILEPHONE", payload.getStr("MOBILEPHONE"));
					dataBean.set("USER_STATE", payload.getStr("USER_STATUS"));
					dataBean.set("TELEPHONE", payload.getStr("TELEPHONE"));
					dataBean.set("MEMO", payload.getStr("MEMO"));
					dataBean.set("ORG_NAME", payload.getStr("ORG_NAME"));
					dataBean.set("STATUS", payload.getStr("STATUS"));
					dataBean.set("PASSPORT", payload.getStr("PASSPORT"));
					dataBean.set("DOMAIN_ID", payload.getStr("DOMAIN_ID"));
					dataBean.set("DOMAIN_NAME", payload.getStr("DOMAIN_NAME"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					ServDao.update(MsgModifyUtil.SYS_AUTH_USER, dataBean);
				}
			}
			log.info("MSGDATAMODIFY modifyUserData end updateUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void updateUserToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start updateUserToAuth");
		try{
			//修改用户数据
		
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID", payload.getStr("id"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("LOGIN_NAME", payload.getStr("login_name"));
			dataBean.set("TYPE", payload.getStr("type"));
			dataBean.set("POLITICAL_STATUS", payload.getStr("political_status"));
			dataBean.set("ADDRESS", payload.getStr("address"));
			dataBean.set("NATION", payload.getStr("nation"));
			dataBean.set("MARITAL_STATUS", payload.getStr("marital_status"));
			dataBean.set("GENDER", payload.getStr("gender"));
			dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
			dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("PINYIN", payload.getStr("pinyin"));
			dataBean.set("SORT",payload.getInt("sort"));
			dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
			dataBean.set("ICON_ID", payload.getStr("icon_id"));
			dataBean.set("CODE",payload.getStr("code"));
			dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
			dataBean.set("IDCARD", payload.getStr("idcard"));
			dataBean.set("CLASSIFICATION", payload.getStr("classification"));
			dataBean.set("EMAIL", payload.getStr("email"));
			dataBean.set("ZHIJI", payload.getStr("zhiji"));
			dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
			dataBean.set("MOBILEPHONE", payload.getStr("mobilephone"));
			dataBean.set("USER_STATE", payload.getStr("user_status"));
			dataBean.set("TELEPHONE", payload.getStr("telephone"));
			dataBean.set("MEMO", payload.getStr("memo"));
			dataBean.set("ORG_NAME", payload.getStr("org_name"));
			dataBean.set("STATUS", payload.getStr("status"));
			dataBean.set("PASSPORT", payload.getStr("passport"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			ServDao.update(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			log.info("MSGDATAMODIFY modifyUserData end updateUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//修改用户数据到系统用户表
	public static void updateUserToSy(List<Bean> updateUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start updateUserToSy");
		try{
			//修改用户数据
			if (updateUsers.size()>0){
				for (Bean payload :updateUsers ){
					SqlBean dataBean = new SqlBean();
					dataBean.and("USER_CODE", payload.getStr("ID"));
					dataBean.set("USER_LOGIN_NAME", payload.getStr("LOGIN_NAME"));
					dataBean.set("USER_NAME", payload.getStr("NAME"));
					dataBean.set("DEPT_CODE", payload.getStr("ORG_ID"));
					dataBean.set("USER_PASSWORD", PWD);
					dataBean.set("USER_SORT",payload.getInt("SORT"));
					dataBean.set("USER_MOBILE", payload.getStr("MOBILEPHONE"));
					dataBean.set("USER_EMAIL", payload.getStr("EMAIL"));
					dataBean.set("USER_POST", payload.getStr("ZHIJI"));//TODO
					dataBean.set("USER_IDCARD", payload.getStr("IDCARD"));
					dataBean.set("USER_HOME_PHONE", payload.getStr("TELEPHONE"));
					dataBean.set("USER_NATION", payload.getStr("NATION"));
					dataBean.set("USER_SEX", MsgModifyUtil.getSex(payload.getStr("GENDER")));
					dataBean.set("USER_MARRIAGE", payload.getStr("MARITAL_STATUS"));
					dataBean.set("USER_POLITICS", payload.getStr("POLITICAL_STATUS"));
					dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("USER_STATUS")));
					dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
					dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("STATUS")));
					dataBean.set("USER_EN_NAME", payload.getStr("PINYIN"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					dataBean.set("USER_IMG_SRC", payload.getStr("ICON_ID"));
					dataBean.set("USER_EN_NAME",payload.getStr("CODE"));
					//以下为没用的数据
//					dataBean.set("TYPE", payload.getStr("type"));
//					dataBean.set("ADDRESS", payload.getStr("address"));
//					dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
//					dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
//					dataBean.set("CLASSIFICATION", payload.getStr("classification"));
//					dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
//					dataBean.set("MEMO", payload.getStr("memo"));
//					dataBean.set("ORG_NAME", payload.getStr("org_name"));
//					dataBean.set("PASSPORT", payload.getStr("passport"));
//					dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
					
					ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
				}
				
			}
			log.info("MSGDATAMODIFY modifyUserData end updateUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void updateUserToSy(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start updateUserToSy");
		try{
			//修改用户数据
			
			SqlBean dataBean = new SqlBean();
			dataBean.and("USER_CODE", payload.getStr("id"));
			dataBean.set("USER_LOGIN_NAME", payload.getStr("login_name"));
			dataBean.set("USER_NAME", payload.getStr("name"));
			dataBean.set("DEPT_CODE", payload.getStr("org_id"));
			dataBean.set("USER_PASSWORD", PWD);
			dataBean.set("USER_SORT",payload.getInt("sort"));
			dataBean.set("USER_MOBILE", payload.getStr("mobilephone"));
			dataBean.set("USER_EMAIL", payload.getStr("email"));
			dataBean.set("USER_POST", payload.getStr("zhiji"));//TODO
			dataBean.set("USER_IDCARD", payload.getStr("idcard"));
			dataBean.set("USER_HOME_PHONE", payload.getStr("telephone"));
			dataBean.set("USER_NATION", payload.getStr("nation"));
			dataBean.set("USER_SEX", MsgModifyUtil.getSex(payload.getStr("gender")));
			dataBean.set("USER_MARRIAGE", payload.getStr("marital_status"));
			dataBean.set("USER_POLITICS", payload.getStr("political_status"));
			dataBean.set("USER_STATE", MsgModifyUtil.getUserStatus(payload.getStr("user_status")));
			dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
			dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("status")));
			dataBean.set("USER_EN_NAME", payload.getStr("pinyin"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			dataBean.set("USER_IMG_SRC", payload.getStr("icon_id"));
			dataBean.set("USER_EN_NAME",payload.getStr("code"));
			dataBean.set("USER_SORT", payload.getInt("ext_int"));
			//以下为没用的数据
//					dataBean.set("TYPE", payload.getStr("type"));
//					dataBean.set("ADDRESS", payload.getStr("address"));
//					dataBean.set("NATIVE_PLACE", payload.getStr("native_place"));
//					dataBean.set("CERTIFICATE_TYPE", payload.getStr("certificate_type"));
//					dataBean.set("CLASSIFICATION", payload.getStr("classification"));
//					dataBean.set("SECRET_CODE", payload.getStr("secret_code"));
//					dataBean.set("MEMO", payload.getStr("memo"));
//					dataBean.set("ORG_NAME", payload.getStr("org_name"));
//					dataBean.set("PASSPORT", payload.getStr("passport"));
//					dataBean.set("DOMAIN_NAME", payload.getStr("domain_name"));
			
			ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
			log.info("MSGDATAMODIFY modifyUserData end updateUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateUserToSy ERROR:"+ e.getMessage());
			throw e;
		}	
	}
	//删除用户数据到统一认证表
	public static void deleteUserToAuth(List<Bean> deleteUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start deleteUserToAuth");
		try{
			//删除用户数据
			if (deleteUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :deleteUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				ServDao.destroy(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			}
			log.info("MSGDATAMODIFY modifyUserData end deleteUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void deleteUserToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start deleteUserToAuth");
		try{
			//删除用户数据
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			ServDao.destroy(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			log.info("MSGDATAMODIFY modifyUserData end deleteUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//删除用户数据到系统用户表
	public static void deleteUserToSy(List<Bean> deleteUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start deleteUserToSy");
		try{
			//删除用户数据
			if (deleteUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :deleteUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("USER_CODE",  idList.toArray());
				ServDao.destroy(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
			}
			log.info("MSGDATAMODIFY modifyUserData end deleteUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void deleteUserToSy(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start deleteUserToSy");
		try{
			//删除用户数据
			SqlBean dataBean = new SqlBean();
			dataBean.and("USER_CODE",  payload.getStr("id"));
			ServDao.destroy(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
			log.info("MSGDATAMODIFY modifyUserData end deleteUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	//锁定用户数据到统一认证表
	public static void lockUserToAuth(List<Bean> lockUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start lockUserToAuth");
		try{
			//锁定用户数据
			if (lockUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :lockUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				dataBean.set("STATUS", MsgModifyUtil.LOCKED_STATE);
				ServDao.update(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			}
			log.info("MSGDATAMODIFY modifyUserData end lockUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void lockUserToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start lockUserToAuth");
		try{
			//锁定用户数据

			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			dataBean.set("STATUS", MsgModifyUtil.LOCKED_STATE);
			ServDao.update(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			
			log.info("MSGDATAMODIFY modifyUserData end lockUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//锁定用户数据到系统用户表
	public static void lockUserToSy(List<Bean> lockUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start lockUserToSy");
		try{
			//锁定用户数据
			if (lockUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :lockUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("USER_CODE", idList.toArray());
				dataBean.set("S_FLAG", LOCKED);
				ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
			}
			log.info("MSGDATAMODIFY modifyUserData end lockUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void lockUserToSy(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start lockUserToSy");
		try{
			//锁定用户数据

			SqlBean dataBean = new SqlBean();
			dataBean.and("USER_CODE", payload.getStr("id"));
			dataBean.set("S_FLAG", LOCKED);
			ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
		
			log.info("MSGDATAMODIFY modifyUserData end lockUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	//启用用户数据到统一认证表
	public static void activeUserToAuth(List<Bean> activeUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start activeUserToAuth");
		try{
			//启用用户数据
			if (activeUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :activeUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				dataBean.set("STATUS", MsgModifyUtil.ACTIVE_STATE);
				ServDao.update(MsgModifyUtil.SYS_AUTH_USER, dataBean);
			}
			log.info("MSGDATAMODIFY modifyUserData end activeUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	
	public static void activeUserToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start activeUserToAuth");
		try{
			//启用用户数据

			SqlBean dataBean = new SqlBean();
			dataBean.and("ID", payload.getStr("id"));
			dataBean.set("STATUS", MsgModifyUtil.ACTIVE_STATE);
			ServDao.update(MsgModifyUtil.SYS_AUTH_USER, dataBean);
		
			log.info("MSGDATAMODIFY modifyUserData end activeUserToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeUserToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	
	//启用用户数据到系统用户表
	public static void activeUserToSy(List<Bean> activeUsers) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start activeUserToSy");
		try{
			//启用用户数据
			if (activeUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :activeUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("USER_CODE",  idList.toArray());
				dataBean.set("S_FLAG", ACTIVE);
				ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
			}
			log.info("MSGDATAMODIFY modifyUserData end activeUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}

	public static void activeUserToSy(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyUserData start activeUserToSy");
		try{
			//启用用户数据

			SqlBean dataBean = new SqlBean();
			dataBean.and("USER_CODE",  payload.getStr("id"));
			dataBean.set("S_FLAG", ACTIVE);
			ServDao.update(MsgModifyUtil.OA_SY_ORG_USER, dataBean);
		
			log.info("MSGDATAMODIFY modifyUserData end activeUserToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeUserToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
}
