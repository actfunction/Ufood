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
 * 职级消息处理类
 */
public class MsgPositionData {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgPositionData.class);
	
	//保存职级数据到临时表
	public static void savePositionToTemp(Bean payload,String method)  {
		try{
			SqlBean dataBean = new SqlBean();
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("SORT", payload.getStr("sort"));
			dataBean.set("METHOD",method);
			ServDao.create(MsgModifyUtil.SYS_AUTH_POSITION_TEMP, dataBean);
			log.error("MSGDATAMODIFY savePositionToTemp SUCCESS USERCODE:"+payload.getStr("id"));
		}catch(Exception e){
			log.error("MSGDATAMODIFY savePositionToTemp ERROR:"+ e.getMessage());
		}
	}
	public static void savePositionToTemp(List<Bean>positionList)  {
		Transaction.begin();
		try{
			log.info("MSGDATAMODIFY savePositionToTemp list start ");
			for (Bean payload:positionList) {
				SqlBean dataBean = new SqlBean();
				dataBean.set("ID", payload.getStr("id"));
				dataBean.set("CODE", payload.getStr("code"));
				dataBean.set("NAME", payload.getStr("name"));
				dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
				dataBean.set("ORG_ID", payload.getStr("org_id"));
				dataBean.set("SORT", payload.getStr("sort"));
				dataBean.set("METHOD",payload.getStr("method"));
				positionList.add(dataBean);
			}
			ServDao.creates(MsgModifyUtil.SYS_AUTH_POSITION_TEMP, positionList);
			Transaction.commit();
			log.info("MSGDATAMODIFY savePositionToTemp list end ");
		}catch(Exception e) {
			Transaction.rollback();
			log.info("MSGDATAMODIFY savePositionToTemp list error: "+ e.getMessage());
		}
		Transaction.end();
	}
	
	public static void modifyPositionData(){
		Transaction.begin();
		try{
			log.info("MSGDATAMODIFY modifyPositionData start ");
			//新增用户组
			//获取临时新增用户组数据
			SqlBean methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.CREATE_ONE);
			List<Bean>createPositions =  ServDao.finds(MsgModifyUtil.SYS_AUTH_POSITION_TEMP, methodBean);
			log.info("MSGDATAMODIFY createPositions number : " + createPositions.size());
			if (createPositions.size()>0){
				addPositionToAuth(createPositions);
			}
			//更新用户组
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.UPDATE_ONE);
			List<Bean>updatePositions =  ServDao.finds(MsgModifyUtil.SYS_AUTH_POSITION_TEMP, methodBean);
			log.info("MSGDATAMODIFY updatePositions number : " + updatePositions.size());
			if (updatePositions.size()>0){
				updatePositionToAuth(updatePositions);
			}
			//删除用户组
			methodBean = new SqlBean();
			methodBean.and("METHOD",MsgModifyUtil.DELETE_ONE);
			List<Bean>deletePositions =  ServDao.finds(MsgModifyUtil.SYS_AUTH_POSITION_TEMP, methodBean);
			log.info("MSGDATAMODIFY deletePositions number : " + deletePositions.size());
			if (deletePositions.size()>0){
				deletePositionToAuth(deletePositions);
			}
			
			//清空用户机构临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_POSITION_TEMP);
			Transaction.commit();
			
			log.info("MSGDATAMODIFY modifyPositionData End");
		}catch(Exception e){
			e.printStackTrace();
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyPositionData ERROR:"+ e.getMessage());
		}
		Transaction.end();
	}
	
	public static void modifyOnePositionData(Bean payload,String method)  {
		log.info("MSGDATAMODIFY modifyOnePositionData start:"+ payload.getStr("ID")+" METHOD" + method);
		String id =  payload.getStr("id");
		Transaction.begin();
		try{
			savePositionToTemp(payload,method);
			if (MsgModifyUtil.CREATE_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authPosBean = ServDao.find(MsgModifyUtil.SYS_AUTH_POSITION,sqlBean);
				if (null ==authPosBean ||  authPosBean.isEmpty()) {
					addPositionToAuth(payload);
				}
			}
			if (MsgModifyUtil.UPDATE_ONE.equals(method)) {
				updatePositionToAuth(payload);
			}
			if (MsgModifyUtil.DELETE_ONE.equals(method)) {
				deletePositionToAuth(payload);
			}
			//清空用户临时表
			MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_POSITION_TEMP);
			Transaction.commit();
			log.info("MSGDATAMODIFY modifyOnePositionData success:"+ payload.getStr("ID")+" METHOD" + method);
		}catch(Exception e) {
			Transaction.rollback();
			log.error("MSGDATAMODIFY modifyOnePositionData error:"+ payload.getStr("ID")+" METHOD" + method);
			
		}
		Transaction.end();
	}
	//新增职务到统一认证
	public static void addPositionToAuth(List<Bean> createPositions) throws Exception{
		log.info("MSGDATAMODIFY modifyPositionData start addPositionToAuth");
		try{
			//新增职务数据
			if (createPositions.size()>0){
				List<Bean> beanList = new ArrayList<Bean>();
				for (Bean payload :createPositions ){
					Bean dataBean = new Bean();
					dataBean.set("ID", payload.getStr("ID"));
					dataBean.set("CODE", payload.getStr("CODE"));
					dataBean.set("NAME", payload.getStr("NAME"));
					dataBean.set("DOMAIN_ID", payload.getStr("DOMAIN_ID"));
					dataBean.set("ORG_ID", payload.getStr("ORG_ID"));
					dataBean.set("SORT", payload.getStr("SORT"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					beanList.add(dataBean);
				}
				ServDao.creates(MsgModifyUtil.SYS_AUTH_POSITION, beanList);
			}
			log.info("MSGDATAMODIFY modifyPositionData end addPositionToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addPositionToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void addPositionToAuth(Bean payload) throws Exception{
		log.info("MSGDATAMODIFY modifyPositionData start addPositionToAuth");
		try{
			//新增职务数据
		
			Bean dataBean = new Bean();
			dataBean.set("ID", payload.getStr("id"));
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("SORT", payload.getStr("sort"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());

			ServDao.create(MsgModifyUtil.SYS_AUTH_POSITION, dataBean);
	
			log.info("MSGDATAMODIFY modifyPositionData end addPositionToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY addPositionToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//修改职务数据到统一认证表
	public static void updatePositionToAuth(List<Bean> updatePositions) throws Exception  {
		log.info("MSGDATAMODIFY modifyPositionData start updatePositionToAuth");
		try{
			//修改职务数据
			if (updatePositions.size()>0){
				for (Bean payload :updatePositions ){
					SqlBean dataBean = new SqlBean();
					dataBean.and("ID", payload.getStr("ID"));
					dataBean.set("CODE", payload.getStr("CODE"));
					dataBean.set("NAME", payload.getStr("NAME"));
					dataBean.set("DOMAIN_ID", payload.getStr("DOMAIN_ID"));
					dataBean.set("ORG_ID", payload.getStr("ORG_ID"));
					dataBean.set("SORT", payload.getStr("SORT"));
					dataBean.set("S_MTIME", MsgModifyUtil.getTime());
					ServDao.update(MsgModifyUtil.SYS_AUTH_POSITION, dataBean);
				}
			}
			log.info("MSGDATAMODIFY modifyPositionData end updatePositionToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updatePositionToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void updatePositionToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyPositionData start updatePositionToAuth");
		try{
			//修改职务数据
			
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID", payload.getStr("id"));
			dataBean.set("CODE", payload.getStr("code"));
			dataBean.set("NAME", payload.getStr("name"));
			dataBean.set("DOMAIN_ID", payload.getStr("domain_id"));
			dataBean.set("ORG_ID", payload.getStr("org_id"));
			dataBean.set("SORT", payload.getStr("sort"));
			dataBean.set("S_MTIME", MsgModifyUtil.getTime());
			ServDao.update(MsgModifyUtil.SYS_AUTH_POSITION, dataBean);
		
			
			log.info("MSGDATAMODIFY modifyPositionData end updatePositionToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY updatePositionToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	//删除用户组数据到统一认证表
	public static void deletePositionToAuth(List<Bean> deletePositions) throws Exception  {
		log.info("MSGDATAMODIFY modifyPositionData start deletePositionToAuth");
		try{
			//删除用户数据
			if (deletePositions.size()>0){
				List<String> idList = new ArrayList<String>();
				for (Bean payload :deletePositions ){
					idList.add(payload.getStr("ID"));
				}
				SqlBean dataBean = new SqlBean();
				dataBean.andIn("ID",  idList.toArray());
				ServDao.destroy(MsgModifyUtil.SYS_AUTH_POSITION, dataBean);
			}
			log.info("MSGDATAMODIFY modifyPositionData end deletePositionToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deletePositionToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
	public static void deletePositionToAuth(Bean payload) throws Exception  {
		log.info("MSGDATAMODIFY modifyPositionData start deletePositionToAuth");
		try{
			//删除用户数据
		
			SqlBean dataBean = new SqlBean();
			dataBean.and("ID",  payload.getStr("id"));
			ServDao.destroy(MsgModifyUtil.SYS_AUTH_POSITION, dataBean);
		
			log.info("MSGDATAMODIFY modifyPositionData end deletePositionToAuth");
		}catch(Exception e){
			log.error("MSGDATAMODIFY deletePositionToAuth ERROR:"+ e.getMessage());
			throw e;
		}
	}
}
