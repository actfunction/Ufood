package com.rh.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.util.StringUtil;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
/*
 * kfzx-xuyj01
 * 机构消息处理类
 */

public class MsgOrgData {	
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgOrgData.class);
	private static final int ACTIVE= 1;
	private static final int LOCKED= 2;
	
	//保存机构数据进入临时表
	public static void saveOrgToTemp(Bean payload,String method){
		try{
			log.error("MSGDATAMODIFY saveOrgToTemp start ORG:"+payload.getStr("id"));
			SqlBean dataBean = new SqlBean();
			dataBean.set("CODE",payload.getStr("code"));
			dataBean.set("TYPE",payload.getStr("type"));
			dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
			dataBean.set("ID",payload.getStr("id"));
			dataBean.set("IS_OPERATION",payload.getStr("is_operation"));
			dataBean.set("MEMO",payload.getStr("memo"));
			dataBean.set("NAME",payload.getStr("name"));
			dataBean.set("ORG_CATEGORY",payload.getStr("org_category"));
			dataBean.set("ORG_LEVEL",payload.getStr("org_level"));
			dataBean.set("PARENT",payload.getStr("parent"));
			dataBean.set("PATH",payload.getStr("path"));
			dataBean.set("SHORT_NAME",payload.getStr("short_name"));
			dataBean.set("SORT",payload.getInt("sort"));
			dataBean.set("STATUS",payload.getStr("status"));
			dataBean.set("TELEPHONE",payload.getStr("telephone"));
			dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
			dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_ORG_TEMP, dataBean);
			log.error("MSGDATAMODIFY saveOrgToTemp SUCCESS ORG:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveOrgToTemp ERROR:"+ e.getMessage());
		}
	}
	
	public static void saveOrgToTemp(List<Bean>orgList)  {
		Transaction.begin();
		try{
			log.error("MSGDATAMODIFY saveOrgToTemp list start ");
			for (Bean payload:orgList) {
				SqlBean dataBean = new SqlBean();
				dataBean.set("CODE",payload.getStr("code"));
				dataBean.set("TYPE",payload.getStr("type"));
				dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
				dataBean.set("ID",payload.getStr("id"));
				dataBean.set("IS_OPERATION",payload.getStr("is_operation"));
				dataBean.set("MEMO",payload.getStr("memo"));
				dataBean.set("NAME",payload.getStr("name"));
				dataBean.set("ORG_CATEGORY",payload.getStr("org_category"));
				dataBean.set("ORG_LEVEL",payload.getStr("org_level"));
				dataBean.set("PARENT",payload.getStr("parent"));
				dataBean.set("PATH",payload.getStr("path"));
				dataBean.set("SHORT_NAME",payload.getStr("short_name"));
				dataBean.set("SORT",payload.getInt("sort"));
				dataBean.set("STATUS",payload.getStr("status"));
				dataBean.set("TELEPHONE",payload.getStr("telephone"));
				dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
				dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
				dataBean.set("METHOD",payload.getStr("method"));
				orgList.add(dataBean);
			}
			ServDao.creates(MsgModifyUtil.SYS_AUTH_ORG_TEMP, orgList);
			Transaction.commit();
			log.error("MSGDATAMODIFY saveOrgToTemp list end ");
		}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY saveOrgToTemp list error: "+ e.getMessage());
		}
		Transaction.end();
	}
	//处理机构数据
	public static void modifyOrgData()  {
		Transaction.begin();
		try{
			Boolean isModifyed = false;
			//新增机构
			//获取临时新增机构数据
			SqlBean methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.CREATE_ONE);
			List<Bean>createOrgs =  ServDao.finds(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
			log.error("MSGDATAMODIFY createOrgs number : " + createOrgs.size());
			if (createOrgs.size()>0){
				addOrgToAuth(createOrgs);
				addOrgToSy(createOrgs);
				isModifyed = true;
			}
			//更新机构
			//获取临时更新机构数据
		    methodBean = new SqlBean();
		    methodBean.appendWhere("and METHOD in (?,?)", MsgModifyUtil.UPDATE_ONE,MsgModifyUtil.UPDATE_ONE_4ADMIN);
			List<Bean>updateOrgs =  ServDao.finds(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
			log.error("MSGDATAMODIFY createOrgs number : " + updateOrgs.size());
			if (updateOrgs.size()>0){
				updateOrgToAuth(updateOrgs);
				updateOrgToSy(updateOrgs);
				isModifyed = true;
			}
			//删除机构
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.DELETE_ONE);
			List<Bean>deleteOrgs = ServDao.finds(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
			log.error("MSGDATAMODIFY deleteOrgs number : " + deleteOrgs.size());
			if (deleteOrgs.size()>0){
				deleteOrgToAuth(deleteOrgs);
				deleteOrgToSy(deleteOrgs);
				isModifyed = true;
			}
			//锁定机构
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.LOCK_ONE);
			List<Bean>lockOrgs =  ServDao.finds(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
			log.error("MSGDATAMODIFY lockOrgs number : " + lockOrgs.size());
			if (lockOrgs.size()>0){
				lockOrgToAuth(lockOrgs);
				lockOrgToSy(lockOrgs);
			}
			//启用机构
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.ACTIVE_ONE);
			List<Bean>activeOrgs =  ServDao.finds(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
			log.error("MSGDATAMODIFY activeOrgs number : " + activeOrgs.size());
			if (activeOrgs.size()>0){
				activeOrgToAuth(activeOrgs);
				activeOrgToSy(activeOrgs);
			}
			//清空机构临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_ORG_TEMP);
			Transaction.commit();
			log.error("MSGDATAMODIFY modifyUserData End");
			//清楚缓存字典
			if (isModifyed) {
				MsgModifyUtil.modifyCodePathByRoot();
				ServDefBean servDef = ServUtils.getServDef(MsgModifyUtil.OA_SY_ORG_DEPT);
				servDef.clearDictCache();
			}
		}catch(Exception e){
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyUserData ERROR:"+ e.getMessage());
		}
		Transaction.end();
	}
	
	public static void modifyOneOrgData(Bean payload,String method)  {
		String id  = payload.getStr("id");
		log.error("MSGDATAMODIFY modifyOneOrgData start:"+ payload.getStr("ID")+" METHOD" + method);
		Transaction.begin();
		try{
			saveOrgToTemp(payload,method);
		
			if (MsgModifyUtil.CREATE_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authDeptBean = ServDao.find(MsgModifyUtil.SYS_AUTH_ORG,sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("DEPT_CODE", id);
				Bean oaDeptBean = ServDao.find(MsgModifyUtil.OA_SY_ORG_DEPT,sqlBean);
				if (null ==authDeptBean ||  authDeptBean.isEmpty()) {
					addOrgToAuth(payload);
				}
				if (null ==oaDeptBean ||  oaDeptBean.isEmpty()) {
					addOrgToSy(payload);
				}
			}
			if (MsgModifyUtil.UPDATE_ONE.equals(method)||MsgModifyUtil.UPDATE_ONE_4ADMIN.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authDeptBean = ServDao.find(MsgModifyUtil.SYS_AUTH_ORG,sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("DEPT_CODE", id);
				Bean oaDeptBean = ServDao.find(MsgModifyUtil.OA_SY_ORG_DEPT,sqlBean);
				if (null ==authDeptBean ||  authDeptBean.isEmpty()) {
					addOrgToAuth(payload);
				}else {
					updateOrgToAuth(payload);
				}
				if (null ==oaDeptBean ||  oaDeptBean.isEmpty()) {
					addOrgToSy(payload);
				}else {
					updateOrgToSy(payload);
				}
				
			}
			if (MsgModifyUtil.DELETE_ONE.equals(method)) {
				deleteOrgToAuth(payload);
				deleteOrgToSy(payload);
			}
			if (MsgModifyUtil.LOCK_ONE.equals(method)) {
				lockOrgToAuth(payload);
				lockOrgToSy(payload);
			}
			if (MsgModifyUtil.ACTIVE_ONE.equals(method)) {
				activeOrgToAuth(payload);
				activeOrgToSy(payload);
			}

			//清空用户临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_ORG_TEMP);

			Transaction.commit();
			log.error("MSGDATAMODIFY modifyOneOrgData success:"+ payload.getStr("ID")+" METHOD" + method);
			

		}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyOneOrgData error:"+ payload.getStr("ID")+" METHOD" + method);
			
		}
		Transaction.end();
	}
	//新增机构到统一认证表
	public static void addOrgToAuth(List<Bean> createOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start addOrgToAuth");
		try{
			//新增机构数据
			if (createOrgs.size()>0){
				List<Bean> beanList = new ArrayList<Bean>();
				for (Bean payload :createOrgs ){
					Bean dataBean = new Bean();
					dataBean.set("CODE",payload.getStr("CODE"));
					dataBean.set("TYPE",payload.getStr("TYPE"));
					dataBean.set("DOMAIN_ID",payload.getStr("DOMAIN_ID"));
					dataBean.set("ID",payload.getStr("ID"));
					dataBean.set("IS_OPERATION",payload.getStr("IS_OPERATION"));
					dataBean.set("MEMO",payload.getStr("MEMO"));
					dataBean.set("NAME",payload.getStr("NAME"));
					dataBean.set("ORG_CATEGORY",payload.getStr("ORG_CATEGORY"));
					dataBean.set("ORG_LEVEL",payload.getStr("ORG_LEVEL"));
					dataBean.set("PARENT",payload.getStr("PARENT"));
					dataBean.set("PATH",payload.getStr("PATH"));
					dataBean.set("SHORT_NAME",payload.getStr("SHORT_NAME"));
					dataBean.set("SORT",payload.getInt("SORT"));
					dataBean.set("STATUS",payload.getStr("STATUS"));
					dataBean.set("TELEPHONE",payload.getStr("TELEPHONE"));
					dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("DATE_CREATED")));
					dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("LAST_UPDATED")));//time格式化
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					beanList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.SYS_AUTH_ORG, beanList);
			}
			log.error("MSGDATAMODIFY modifyOrgData end addOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void addOrgToAuth(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start addOrgToAuth");
		try{
			//新增机构数据

			Bean dataBean = new Bean();
			dataBean.set("CODE",payload.getStr("code"));
			dataBean.set("TYPE",payload.getStr("type"));
			dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
			dataBean.set("ID",payload.getStr("id"));
			dataBean.set("IS_OPERATION",payload.getStr("is_operation"));
			dataBean.set("MEMO",payload.getStr("memo"));
			dataBean.set("NAME",payload.getStr("name"));
			dataBean.set("ORG_CATEGORY",payload.getStr("org_category"));
			dataBean.set("ORG_LEVEL",payload.getStr("org_level"));
			dataBean.set("PARENT",payload.getStr("parent"));
			dataBean.set("PATH",payload.getStr("path"));
			dataBean.set("SHORT_NAME",payload.getStr("short_name"));
			dataBean.set("SORT",payload.getInt("sort"));
			dataBean.set("STATUS",payload.getStr("status"));
			dataBean.set("TELEPHONE",payload.getStr("telephone"));
			dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
			dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			
			ServDao.create(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
			log.error("MSGDATAMODIFY modifyOrgData end addOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	
	//新增机构数据到系统机构表
	public static void addOrgToSy(List<Bean> createOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start addOrgToSy");
		try{
			//新增机构数据
			if (createOrgs.size()>0){
				List<Bean> beanList = new ArrayList<Bean>();
				for (Bean payload :createOrgs ){
					Bean dataBean = new Bean();
					dataBean.set("DEPT_CODE",payload.getStr("ID"));
					dataBean.set("DEPT_NAME",payload.getStr("NAME"));
					dataBean.set("DEPT_FULL_NAME",payload.getStr("NAME"));
					dataBean.set("DEPT_PCODE",payload.getStr("PARENT"));
					dataBean.set("DEPT_SORT",payload.getInt("SORT"));
					dataBean.set("DEPT_MEMO",payload.getStr("MEMO"));
					dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
					dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("STATUS")));
					dataBean.set("CODE_PATH",MsgModifyUtil.getCodePath(payload.getStr("PATH"), payload.getStr("ID")) );
					dataBean.set("DEPT_SIGN",MsgModifyUtil.getDeptSign(payload.getStr("TYPE")));
					dataBean.set("DEPT_LEVEL_OA",MsgModifyUtil.getOADeptLevel(payload.getStr("ORG_LEVEL")));
					dataBean.set("DEPT_LEVEL",MsgModifyUtil.getDeptLevel(payload.getStr("PATH")));
					dataBean.set("DEPT_SHORT_NAME",payload.getStr("SHORT_NAME"));
					dataBean.set("DEPT_ENNAME",payload.getStr("CODE"));
					dataBean.set("DEPT_TYPE",MsgModifyUtil.getDeptType(payload.getStr("IS_OPERATION"),payload.getStr("TYPE")));
					dataBean.set("DEPT_GRADE",MsgModifyUtil.getDeptGrade( payload.getStr("ORG_CATEGORY")));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					//以下为没用的数据
					//dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
					//dataBean.set("TELEPHONE",payload.getStr("telephone"));
					beanList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.OA_SY_ORG_DEPT, beanList);
			}
			log.error("MSGDATAMODIFY modifyOrgData end addOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void addOrgToSy(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start addOrgToSy");
		try{
			//新增机构数据
			Bean dataBean = new Bean();
			dataBean.set("DEPT_CODE",payload.getStr("id"));
			dataBean.set("DEPT_NAME",payload.getStr("name"));
			dataBean.set("DEPT_FULL_NAME",payload.getStr("name"));
			dataBean.set("DEPT_PCODE",payload.getStr("parent"));
			dataBean.set("DEPT_SORT",payload.getInt("sort"));
			dataBean.set("DEPT_MEMO",payload.getStr("memo"));
			dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
			dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("status")));
			dataBean.set("CODE_PATH",MsgModifyUtil.getCodePath(payload.getStr("path"), payload.getStr("id")) );
			dataBean.set("DEPT_SIGN",MsgModifyUtil.getDeptSign(payload.getStr("type")));
			dataBean.set("DEPT_LEVEL_OA",MsgModifyUtil.getOADeptLevel(payload.getStr("org_level")));
			dataBean.set("DEPT_LEVEL",MsgModifyUtil.getDeptLevel(payload.getStr("path")));
			dataBean.set("DEPT_SHORT_NAME",payload.getStr("short_name"));
			dataBean.set("DEPT_ENNAME",payload.getStr("code"));
			dataBean.set("DEPT_TYPE",MsgModifyUtil.getDeptType(payload.getStr("is_operation"),payload.getStr("type")));
			dataBean.set("DEPT_GRADE",MsgModifyUtil.getDeptGrade( payload.getStr("org_category")));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			//以下为没用的数据
			//dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
			//dataBean.set("TELEPHONE",payload.getStr("telephone"));
		
			ServDao.create(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
			
			log.error("MSGDATAMODIFY modifyOrgData end addOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	//修改用户数据到统一认证表
	public static void updateOrgToAuth(List<Bean> updateOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start updateOrgToAuth");
		try{
			//修改用户数据
			if (updateOrgs.size()>0){
				for (Bean payload :updateOrgs ){
					SqlBean dataBean = new SqlBean();
					dataBean.and("ID", payload.getStr("ID"));
					dataBean.set("CODE",payload.getStr("CODE"));
					dataBean.set("TYPE",payload.getStr("TYPE"));
					dataBean.set("DOMAIN_ID",payload.getStr("DOMAIN_ID"));
					dataBean.set("IS_OPERATION",payload.getStr("IS_OPERATION"));
					dataBean.set("MEMO",payload.getStr("MEMO"));
					dataBean.set("NAME",payload.getStr("NAME"));
					dataBean.set("ORG_CATEGORY",payload.getStr("ORG_CATEGORY"));
					dataBean.set("ORG_LEVEL",payload.getStr("ORG_LEVEL"));
					dataBean.set("PARENT",payload.getStr("PARENT"));
					dataBean.set("PATH",payload.getStr("PATH"));
					dataBean.set("SHORT_NAME",payload.getStr("SHORT_NAME"));
					dataBean.set("SORT",payload.getInt("SORT"));
					dataBean.set("STATUS",payload.getStr("STATUS"));
					dataBean.set("TELEPHONE",payload.getStr("TELEPHONE"));
					dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("DATE_CREATED")));
					dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("LAST_UPDATED")));//time格式化
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					ServDao.update(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
				}
			}
			log.error("MSGDATAMODIFY modifyOrgData end updateOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void updateOrgToAuth(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start updateOrgToAuth");
		try{
			//修改用户数据
			
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID", payload.getStr("id"));
			dataBean.set("CODE",payload.getStr("code"));
			dataBean.set("TYPE",payload.getStr("type"));
			dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
			dataBean.set("IS_OPERATION",payload.getStr("is_operation"));
			dataBean.set("MEMO",payload.getStr("memo"));
			dataBean.set("NAME",payload.getStr("name"));
			dataBean.set("ORG_CATEGORY",payload.getStr("org_category"));
			dataBean.set("ORG_LEVEL",payload.getStr("org_level"));
			dataBean.set("PARENT",payload.getStr("parent"));
			dataBean.set("PATH",payload.getStr("path"));
			dataBean.set("SHORT_NAME",payload.getStr("short_name"));
			dataBean.set("SORT",payload.getInt("sort"));
			dataBean.set("STATUS",payload.getStr("status"));
			dataBean.set("TELEPHONE",payload.getStr("telephone"));
			dataBean.set("DATA_CREATED", MsgModifyUtil.getTime(payload.getStr("date_created")));
			dataBean.set("LAST_UPDATED", MsgModifyUtil.getTime(payload.getStr("last_updated")));//time格式化
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			ServDao.update(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgData end updateOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//修改用户数据到系统用户表
	public static void updateOrgToSy(List<Bean> updateOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start updateOrgToSy");
		try{
			//修改用户数据
			if (updateOrgs.size()>0){
				for (Bean payload :updateOrgs ){
					SqlBean dataBean = new SqlBean();
					dataBean.and("DEPT_CODE",payload.getStr("ID"));
					dataBean.set("DEPT_NAME",payload.getStr("NAME"));
					dataBean.set("DEPT_FULL_NAME",payload.getStr("NAME"));
					dataBean.set("DEPT_PCODE",payload.getStr("PARENT"));
					dataBean.set("DEPT_SORT",payload.getInt("SORT"));
					dataBean.set("DEPT_MEMO",payload.getStr("MEMO"));
					dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
					dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("STATUS")));
					dataBean.set("CODE_PATH",MsgModifyUtil.getCodePath(payload.getStr("PATH"), payload.getStr("ID")) );
					dataBean.set("DEPT_SIGN",MsgModifyUtil.getDeptSign(payload.getStr("TYPE")));
					dataBean.set("DEPT_LEVEL_OA",MsgModifyUtil.getOADeptLevel(payload.getStr("ORG_LEVEL")));
					dataBean.set("DEPT_LEVEL",MsgModifyUtil.getDeptLevel(payload.getStr("PATH")));
					dataBean.set("DEPT_SHORT_NAME",payload.getStr("SHORT_NAME"));
					dataBean.set("DEPT_ENNAME",payload.getStr("CODE"));
					dataBean.set("DEPT_TYPE",MsgModifyUtil.getDeptType(payload.getStr("IS_OPERATION"),payload.getStr("TYPE")));
					dataBean.set("DEPT_GRADE",MsgModifyUtil.getDeptGrade( payload.getStr("ORG_CATEGORY")));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					//以下为没用的数据
					//dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
					//dataBean.set("TELEPHONE",payload.getStr("telephone"));
					ServDao.update(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
				}
			}
			log.error("MSGDATAMODIFY modifyOrgData end updateOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void updateOrgToSy(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start updateOrgToSy");
		try{
			//修改用户数据
			
			SqlBean dataBean = new SqlBean();
			dataBean.and("DEPT_CODE",payload.getStr("id"));
			dataBean.set("DEPT_NAME",payload.getStr("name"));
			dataBean.set("DEPT_FULL_NAME",payload.getStr("name"));
			dataBean.set("DEPT_PCODE",payload.getStr("parent"));
			dataBean.set("DEPT_SORT",payload.getInt("sort"));
			dataBean.set("DEPT_MEMO",payload.getStr("memo"));
			dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
			dataBean.set("S_FLAG", MsgModifyUtil.getSFlag( payload.getStr("status")));
			dataBean.set("CODE_PATH",MsgModifyUtil.getCodePath(payload.getStr("path"), payload.getStr("id")) );
			dataBean.set("DEPT_SIGN",MsgModifyUtil.getDeptSign(payload.getStr("type")));
			dataBean.set("DEPT_LEVEL_OA",MsgModifyUtil.getOADeptLevel(payload.getStr("org_level")));
			dataBean.set("DEPT_LEVEL",MsgModifyUtil.getDeptLevel(payload.getStr("path")));
			dataBean.set("DEPT_SHORT_NAME",payload.getStr("short_name"));
			dataBean.set("DEPT_ENNAME",payload.getStr("code"));
			dataBean.set("DEPT_TYPE",MsgModifyUtil.getDeptType(payload.getStr("is_operation"),payload.getStr("type")));
			dataBean.set("DEPT_GRADE",MsgModifyUtil.getDeptGrade( payload.getStr("org_category")));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			//以下为没用的数据
			//dataBean.set("DOMAIN_ID",payload.getStr("domain_id"));
			//dataBean.set("TELEPHONE",payload.getStr("telephone"));
			ServDao.update(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
			log.error("MSGDATAMODIFY modifyOrgData end updateOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	//删除机构数据到统一认证表
	public static void deleteOrgToAuth(List<Bean> deleteOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start deleteOrgToAuth");
		try{
			//删除机构数据
			if (deleteOrgs.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :deleteOrgs ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				ServDao.destroy(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
			}
			log.error("MSGDATAMODIFY modifyOrgData end deleteOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void deleteOrgToAuth(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start deleteOrgToAuth");
		try{
			//删除机构数据
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			ServDao.destroy(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
			log.error("MSGDATAMODIFY modifyOrgData end deleteOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}	
	//删除用户数据到系统用户表
	public static void deleteOrgToSy(List<Bean> deleteUsers) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start deleteOrgToSy");
		try{
			//删除用户数据
			if (deleteUsers.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :deleteUsers ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("DEPT_CODE",  idList.toArray());
				ServDao.destroy(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
			}
			log.error("MSGDATAMODIFY modifyOrgData end deleteOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	public static void deleteOrgToSy(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start deleteOrgToSy");
		try{
			//删除用户数据

			SqlBean dataBean = new SqlBean();
			dataBean.and("DEPT_CODE",  payload.getStr("id"));
			ServDao.destroy(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgData end deleteOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
		
	}
	//锁定用户数据到统一认证表
	public static void lockOrgToAuth(List<Bean> lockOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start lockOrgToAuth");
		try{
			//锁定用户数据
			if (lockOrgs.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :lockOrgs ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				dataBean.set("STATUS", MsgModifyUtil.LOCKED_STATE);
				ServDao.update(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
			}
			log.error("MSGDATAMODIFY modifyOrgData end lockOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void lockOrgToAuth(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start lockOrgToAuth");
		try{
			//锁定用户数据
			
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			dataBean.set("STATUS", MsgModifyUtil.LOCKED_STATE);
			ServDao.update(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgData end lockOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
		
	//锁定用户数据到系统用户表
	public static void lockOrgToSy(List<Bean> lockOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start lockOrgToSy");
		try{
			//锁定用户数据
			if (lockOrgs.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :lockOrgs ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("DEPT_CODE", idList.toArray());
				dataBean.set("S_FLAG", LOCKED);
				ServDao.update(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
			}
			log.error("MSGDATAMODIFY modifyOrgData end lockOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void lockOrgToSy(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start lockOrgToSy");
		try{
			//锁定用户数据

			SqlBean dataBean = new SqlBean();
			dataBean.and("DEPT_CODE",payload.getStr("id"));
			dataBean.set("S_FLAG", LOCKED);
			ServDao.update(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgData end lockOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY lockOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//启用用户数据到统一认证表
	public static void activeOrgToAuth(List<Bean> activeOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start activeOrgToAuth");
		try{
			//启用用户数据
			if (activeOrgs.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :activeOrgs ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				dataBean.set("STATUS", MsgModifyUtil.ACTIVE_STATE);
				ServDao.update(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
			}
			log.error("MSGDATAMODIFY modifyOrgData end activeOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void activeOrgToAuth(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start activeOrgToAuth");
		try{
			//启用用户数据
	
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			dataBean.set("STATUS", MsgModifyUtil.ACTIVE_STATE);
			ServDao.update(MsgModifyUtil.SYS_AUTH_ORG, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgData end activeOrgToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeOrgToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}	
	//启用用户数据到系统用户表
	public static void activeOrgToSy(List<Bean> activeOrgs) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start activeOrgToSy");
		try{
			//启用用户数据
			if (activeOrgs.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :activeOrgs ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("DEPT_CODE",  idList.toArray());
				dataBean.set("S_FLAG", ACTIVE);
				ServDao.update(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
			}
			log.error("MSGDATAMODIFY modifyOrgData end activeOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
	}
	
	public static void activeOrgToSy(Bean payload) throws Exception  {
		log.error("MSGDATAMODIFY modifyOrgData start activeOrgToSy");
		try{
			//启用用户数据
			SqlBean dataBean = new SqlBean();
			dataBean.and("DEPT_CODE",  payload.getStr("id"));
			dataBean.set("S_FLAG", ACTIVE);
			ServDao.update(MsgModifyUtil.OA_SY_ORG_DEPT, dataBean);
		
			log.error("MSGDATAMODIFY modifyOrgData end activeOrgToSy");
		}catch(Exception e){
			log.error("MSGDATAMODIFY activeOrgToSy ERROR:"+ e.getMessage());
			throw e;
		}
	}
}
