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
 * 用户组消息处理类
 */
public class MsgGroupData {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgGroupData.class);
	//保存用户组数据到临时表
	public static void saveGroupToTemp(Bean payload,String method)  {
		try{
			SqlBean dataBean = new SqlBean();
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("SORT", payload.getInt("sort"));
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_GROUP_TEMP, dataBean);
			log.error("MSGDATAMODIFY saveGroupToTemp SUCCESS USERCODE:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveGroupToTemp ERROR:"+ e.getMessage());
		}
	}

	public static void saveGroupToTemp(List<Bean>groupList)  {
		Transaction.begin();
		try{
			log.info("MSGDATAMODIFY saveGroupToTemp list start ");
			for (Bean payload:groupList) {
				SqlBean dataBean = new SqlBean();
				dataBean.set("ID", payload.getStr("id"));
				dataBean.set("CODE", payload.getStr("code"));
				dataBean.set("NAME", payload.getStr("name"));
				dataBean.set("ORG_ID", payload.getStr("org_id"));
				dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
				dataBean.set("SORT", payload.getInt("sort"));
				dataBean.set("METHOD",payload.getStr("method"));
				groupList.add(dataBean);
			}
			ServDao.creates(MsgModifyUtil.SYS_AUTH_GROUP_TEMP, groupList);
			Transaction.commit();
			log.info("MSGDATAMODIFY saveGroupToTemp list end ");
		}catch(Exception e) {
			Transaction.rollback();
			log.info("MSGDATAMODIFY saveGroupToTemp list error: "+ e.getMessage());
		}
		Transaction.end();
	}
	
	public static void modifyGroupData(){
		Transaction.begin();
		try{
			log.info("MSGDATAMODIFY MsgGroupData start ");
			//新增用户组
			//获取临时新增用户组数据
			SqlBean methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.CREATE_ONE);
			List<Bean>createGroups =  ServDao.finds(MsgModifyUtil.SYS_AUTH_GROUP_TEMP, methodBean);
			log.info("MSGDATAMODIFY createGroups number : " + createGroups.size());
			if (createGroups.size()>0){
				addGroupToAuth(createGroups);
			}
			//更新用户组
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.UPDATE_ONE);
			List<Bean>updateGroups =  ServDao.finds(MsgModifyUtil.SYS_AUTH_GROUP_TEMP, methodBean);
			log.info("MSGDATAMODIFY updateGroups number : " + updateGroups.size());
			if (updateGroups.size()>0){
				updateGroupToAuth(updateGroups);
			}
			//删除用户组
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.DELETE_ONE);
			List<Bean>deleteGroups =  ServDao.finds(MsgModifyUtil.SYS_AUTH_GROUP_TEMP, methodBean);
			log.info("MSGDATAMODIFY deleteGroups number : " + deleteGroups.size());
			if (deleteGroups.size()>0){
				deleteGroupToAuth(deleteGroups);
			}
			
			//清空用户机构临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_GROUP_TEMP);
			Transaction.commit();
			
			log.info("MSGDATAMODIFY MsgGroupData End");
		}catch(Exception e){
			e.printStackTrace();
			Transaction.rollback();
			log.error("MSGDATAMODIFY MsgGroupData ERROR:"+ e.getMessage());
		}
		Transaction.end();
	}
	
	public static void modifyOneGroupData(Bean payload,String method)  {
		log.info("MSGDATAMODIFY modifyOneGroupData start:"+ payload.getStr("ID")+" METHOD" + method);
		String id =  payload.getStr("id");
		Transaction.begin();
		try{
			saveGroupToTemp(payload,method);
			if (MsgModifyUtil.CREATE_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authGroupBean = ServDao.find(MsgModifyUtil.SYS_AUTH_GROUP,sqlBean);
				if (null ==authGroupBean ||  authGroupBean.isEmpty()) {
					addGroupToAuth(payload);
				}
			}
			if (MsgModifyUtil.UPDATE_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authGroupBean = ServDao.find(MsgModifyUtil.SYS_AUTH_GROUP,sqlBean);
				if (null ==authGroupBean ||  authGroupBean.isEmpty()) {
					addGroupToAuth(payload);
				}else {
					updateGroupToAuth(payload);
				}
				
			}
			if (MsgModifyUtil.DELETE_ONE.equals(method)) {
				deleteGroupToAuth(payload);
			}
			//清空用户临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_GROUP_TEMP);
			Transaction.commit();
			log.info("MSGDATAMODIFY modifyOneGroupData success:"+ payload.getStr("ID")+" METHOD" + method);
		}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyOneGroupData error:"+ payload.getStr("ID")+" METHOD" + method);
			
		}
		Transaction.end();
	}
	
	//新增用户组到统一认证
	public static void addGroupToAuth(List<Bean> createGroups) throws Exception{
		log.info("MSGDATAMODIFY MsgGroupData start addGroupToAuth");
		try{
			//新增用户组数据
			if (createGroups.size()>0){
				List<Bean> beanList = new ArrayList<Bean>();
				for (Bean payload :createGroups ){
					Bean dataBean = new Bean();
					dataBean.set("ID", payload.getStr("ID"));
					dataBean.set("CODE", payload.getStr("CODE"));
					dataBean.set("NAME", payload.getStr("NAME"));
					dataBean.set("ORG_ID", payload.getStr("ORG_ID"));
					dataBean.set("DOMAIN_ID", payload.getStr("DOMAIN_ID"));
					dataBean.set("SORT", payload.getInt("SORT"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					beanList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.SYS_AUTH_GROUP, beanList);
			}
			log.info("MSGDATAMODIFY MsgGroupData end addGroupToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addGroupToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	
	public static void addGroupToAuth(Bean payload) throws Exception{
		log.info("MSGDATAMODIFY MsgGroupData start addGroupToAuth");
		try{
			//新增用户组数据
			
			Bean dataBean = new Bean();
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("SORT", payload.getInt("sort"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			ServDao.create(MsgModifyUtil.SYS_AUTH_GROUP, dataBean);

			log.info("MSGDATAMODIFY MsgGroupData end addGroupToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addGroupToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//修改用户组数据到统一认证表
	public static void updateGroupToAuth(List<Bean> updateGroups) throws Exception  {
		log.info("MSGDATAMODIFY MsgGroupData start updateGroupToAuth");
		try{
			//修改用户数据
			if (updateGroups.size()>0){
				for (Bean payload :updateGroups ){
					SqlBean dataBean = new SqlBean();
					dataBean.and("ID", payload.getStr("ID"));
					dataBean.set("CODE", payload.getStr("CODE"));
					dataBean.set("NAME", payload.getStr("NAME"));
					dataBean.set("ORG_ID", payload.getStr("ORG_ID"));
					dataBean.set("DOMAIN_ID", payload.getStr("DOMAIN_ID"));
					dataBean.set("SORT", payload.getInt("SORT"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					ServDao.update(MsgModifyUtil.SYS_AUTH_GROUP, dataBean);
				}
			}
			log.info("MSGDATAMODIFY MsgGroupData end updateGroupToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateGroupToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void updateGroupToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY MsgGroupData start updateGroupToAuth");
		try{
			//修改用户数据
			
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID", payload.getStr("id"));
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("SORT", payload.getInt("sort"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			ServDao.update(MsgModifyUtil.SYS_AUTH_GROUP, dataBean);

			log.info("MSGDATAMODIFY MsgGroupData end updateGroupToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updateGroupToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//删除用户组数据到统一认证表
	public static void deleteGroupToAuth(List<Bean> deleteGroups) throws Exception  {
		log.info("MSGDATAMODIFY MsgGroupData start deleteGroupToAuth");
		try{
			//删除用户数据
			if (deleteGroups.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :deleteGroups ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				ServDao.destroy(MsgModifyUtil.SYS_AUTH_GROUP, dataBean);
			}
			log.info("MSGDATAMODIFY MsgGroupData end deleteGroupToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteGroupToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void deleteGroupToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY MsgGroupData start deleteGroupToAuth");
		try{
			//删除用户数据
	
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			ServDao.destroy(MsgModifyUtil.SYS_AUTH_GROUP, dataBean);
			
			log.info("MSGDATAMODIFY MsgGroupData end deleteGroupToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deleteGroupToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
}
