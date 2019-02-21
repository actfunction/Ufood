package com.rh.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.JsonUtils;

/*
 * @auther kfzx-xuyj01
 */
public class MsgDataModify {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgDataModify.class);
	//todo是否做成配置项

	public static void saveMsgData(List<String> msgList) {
		try {
			log.info("savemsgdata list start");
			List<Bean>msgUserList = new ArrayList<Bean>();
			List<Bean>msgOrgList = new ArrayList<Bean>();
			List<Bean>msgOrgUserList = new ArrayList<Bean>();
			List<Bean>msgPositionList = new ArrayList<Bean>();
			List<Bean>msgGroupList = new ArrayList<Bean>();
			List<Bean>msgRoleList = new ArrayList<Bean>();
			List<Bean>msgRolePrincipalList = new ArrayList<Bean>();
			for(String msg:msgList) { 
				Bean msgBean = JsonUtils.toBean(msg);
				Bean header = msgBean.getBean("headers");
				Bean payload = JsonUtils.toBean(msgBean.getStr("payload"));
				String method = header.getStr("method");
				String module = header.getStr("module");
				payload.set("method", method);
				if (MsgModifyUtil.USER.equals(module)){
					msgUserList.add(payload);
				}
				if (MsgModifyUtil.ORGANIZATION.equals(module)){
					msgOrgList.add(payload);
				}
				if(MsgModifyUtil.USER_PRINCIPLE.equals(module)){
					msgOrgUserList.add(payload)	;
				}
				if (MsgModifyUtil.POSITION.equals(module)){
					msgPositionList.add(payload);
				}
				if (MsgModifyUtil.ROLE.equals(module)){
					msgRoleList.add(payload);
				}
				if (MsgModifyUtil.ROLE_PRINCIPLE.equals(module)){
					msgRolePrincipalList.add(payload);
				}
				if (MsgModifyUtil.GROUP.equals(module)){
					msgGroupList.add(payload);
				}
			}
			if (msgUserList.size() >0) {
				MsgUserData.saveUserToTemp(msgUserList);
			}
			if (msgOrgList.size() >0) {
				MsgOrgData.saveOrgToTemp(msgUserList);
			}
			if (msgOrgUserList.size() >0) {
				MsgOrgUserData.saveDeptUserToTemp(msgUserList);
			}
			if (msgPositionList.size() >0) {
				MsgPositionData.savePositionToTemp(msgUserList);
			}
			if (msgGroupList.size() >0) {
				MsgGroupData.saveGroupToTemp(msgUserList);
			}
			if (msgRolePrincipalList.size() >0) {
				//MsgUserData.saveUserToTemp(msgUserList);
			}
			if (msgRoleList.size() >0) {
				//MsgUserData.saveUserToTemp(msgUserList);
			}
			
			log.info("savemsgdata list end");
		}catch(Exception e) {
			log.error("MSGDATAMODIFY saveMsgData msglist  ##ERROR:"+ e.getMessage());
		}
	}
	//保存用户数据进入临时表
	public static void saveMsgData(String msg){
		try{
			Bean msgBean = JsonUtils.toBean(msg);
			Bean header = msgBean.getBean("headers");
			Bean payload = JsonUtils.toBean(msgBean.getStr("payload"));
			String method = header.getStr("method");
			String module = header.getStr("module");
			if (MsgModifyUtil.USER.equals(module)){
				MsgUserData.saveUserToTemp(payload,method);
			}
			if (MsgModifyUtil.ORGANIZATION.equals(module)){
				MsgOrgData.saveOrgToTemp(payload,method);
			}
			if(MsgModifyUtil.USER_PRINCIPLE.equals(module)){
				MsgOrgUserData.saveDeptUserToTemp(payload,method);
			}
			if (MsgModifyUtil.POSITION.equals(module)){
				MsgPositionData.savePositionToTemp(payload,method);
			}
			if (MsgModifyUtil.ROLE.equals(module)){
				saveRoleToTemp(payload,method);
			}
			if (MsgModifyUtil.ROLE_PRINCIPLE.equals(module)){
				saveRoleUserToTemp(payload,method);
			}
			if (MsgModifyUtil.GROUP.equals(module)){
				MsgGroupData.saveGroupToTemp(payload,method);
			}
			
			log.error("MSGDATAMODIFY saveMsgData success MSG:"+msg);
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveMsgData MSG:"+msg+" ##ERROR:"+ e.getMessage());
		}
	}
	//处理一条数据
	public static void saveAndmodifyOneMsg(String msg){
		
		try{
			String authRootDept = Context.getSyConf("OA_AUTH_ROOT_DEPT_CODE", "orgRootDomain");
			String syRootDept = Context.getSyConf("OA_SY_ROOT_DEPT_CODE", "cnao0001");
			msg = msg.replace(authRootDept, syRootDept);
			System.out.println("MSGDATAMODIFY saveMsgData start MSG:"+msg);
			log.error("MSGDATAMODIFY saveMsgData start MSG:"+msg);
			Bean msgBean = JsonUtils.toBean(msg);
			Bean header = msgBean.getBean("headers");
			Bean payload = JsonUtils.toBean(msgBean.getStr("payload"));
			String method = header.getStr("method");
			String module = header.getStr("module");
			payload.set("method", payload);
			//处理用户数据
			if (MsgModifyUtil.USER.equals(module)){
				MsgUserData.modifyOneUserData(payload, method);
			}
			//处理机构数据
			if (MsgModifyUtil.ORGANIZATION.equals(module)){
				MsgOrgData.modifyOneOrgData(payload,method);
			}
			//处理用户组织关系数据
			if(MsgModifyUtil.USER_PRINCIPLE.equals(module)){
				MsgOrgUserData.modifyOneOrgUserData(payload,method);
			}
			//处理职级数据
			if (MsgModifyUtil.POSITION.equals(module)){
				MsgPositionData.modifyOnePositionData(payload,method);
			}
			//处理角色数据
			if (MsgModifyUtil.ROLE.equals(module)){
				MsgRoleData.modifyOneRoleData(payload, method);
			}
			//处理角色用户关系数据
			if (MsgModifyUtil.ROLE_PRINCIPLE.equals(module)){
				MsgRolePrincipalData.modifyOneRolePrincipalData(payload, method);
			}
			//处理用户组数据
			if (MsgModifyUtil.GROUP.equals(module)){
				MsgGroupData.modifyOneGroupData(payload,method);
			}
			log.error("MSGDATAMODIFY saveMsgData success MSG:"+msg);
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveMsgData MSG:"+msg+" ##ERROR:"+ e.getMessage());
		}
	}
	//遍历临时表处理基础数据
	public static void modifyData(){
		try{
			//处理用户数据
			MsgUserData.modifyUserData();
			//处理机构数据
			MsgOrgData.modifyOrgData();
			//处理用户机构关系数据
			MsgGroupData.modifyGroupData();;
			//处理职务数据
			MsgPositionData.modifyPositionData();
			//处理用户组数据
			MsgOrgUserData.modifyOrgUserData();
			//处理角色数据
			
			
			//处理用户角色数据			
		}catch(Exception e){
			//todo log
			log.error("MSGDATAMODIFY  ##ERROR:"+ e.getMessage());
		}
	}
	
	
	
	//保存角色数据到临时表
	public static void saveRoleToTemp(Bean payload,String method)  {
		try{
			SqlBean dataBean = new SqlBean();
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("TYPE", payload.getStr("type"));
			dataBean.set("SORT", payload.getStr("sort"));
			dataBean.set("MEMO", payload.getStr("memo"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, dataBean);
			log.error("MSGDATAMODIFY saveRoleToTemp SUCCESS USERCODE:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveRoleToTemp ERROR:"+ e.getMessage());
		}
	}
	//保存角色数据到临时表
	public static void saveRoleUserToTemp(Bean payload,String method)  {
		try{
			SqlBean dataBean = new SqlBean();
			dataBean.set("ID",payload.getStr("id"));
			dataBean.set("ROLE_ID",payload.getStr("role_id"));
			dataBean.set("PRINCIPAL_CLASS",payload.getStr("principal_class"));
			dataBean.set("PRINCIPAL_CLASS_NAME",payload.getStr("principal_class_name"));
			dataBean.set("USER_ID",payload.getStr("user_id"));
			dataBean.set("USER_NAME",payload.getStr("user_name"));
			dataBean.set("PRINCIPAL_VALUE1",payload.getStr("principal_value1"));
			dataBean.set("PRINCIPAL_VALUE1_NAME",payload.getStr("principal_value1_name"));
			dataBean.set("PRINCIPAL_VALUE2",payload.getStr("principal_value2"));
			dataBean.set("PRINCIPAL_VALUE2_NAME",payload.getStr("principal_value2_name"));
			dataBean.set("OPERATION_OWNER",payload.getStr("operation_owner"));
			dataBean.set("DEADLINE_TIME",payload.getStr("deadline_time"));
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_ROLEPRINCIPAL_TEMP, dataBean);
			log.error("MSGDATAMODIFY saveRoleUserToTemp SUCCESS USERCODE:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY saveRoleUserToTemp ERROR:"+ e.getMessage());
		}
	}
	
	
}
