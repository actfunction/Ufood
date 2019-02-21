package com.rh.gw.gdjh.serv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.rh.food.util.GenUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

import com.rh.gw.gdjh.tlq.GwJhTestSenderMsg;

/**
 * 审计管理分系统行政办公管理子系统
 * @author: kfzx-cuiyc
 * @date: 2018年12月6日 下午2:39:51
 * @version: V1.0
 * @description: 公文交换机构信息维护类
 */
public class GwJhDeptQueueInfoServ extends CommonServ{

	private static Log log = LogFactory.getLog(GwJhDeptQueueInfoServ.class);
	/**
	 * @title: getNewId
	 * @descriptin: 获取公文交换id
	 * @param @return
	 * @return String
	 * @throws
	 */
	public String getNewId() {
		return GenUtil.getAutoIdNumner("GDQIS", "OA_GW_DEPT_QUEUE_INFO_SEQ", "NUM");
	}
	
	/**
	 * @title: maintainQueueInfo
	 * @descriptin: 维护公文交换机构信息
	 * @param @param paramBean
	 * @param @return
	 * @return OutBean
	 * @throws
	 */
	public OutBean maintainQueueInfo(ParamBean paramBean){
		OutBean outBean = new OutBean();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			int addNum = Integer.parseInt(paramBean.getStr("ADD_NUM"));
			int updNum = Integer.parseInt(paramBean.getStr("UPD_NUM"));
			String serverIp = paramBean.getStr("SERVER_IP");
			String serverPort = paramBean.getStr("SERVER_PORT");
			//事务打开
			Transaction.begin();
			//本地队列根据ID是否为空判断是新增还是修改
			//远程队列根据DEPT_INFO_ADD、DEPT_INFO_UPD来分别判断数据需要新增还是修改
			if(StringUtils.isBlank(paramBean.getStr("ID"))) {
				StringBuilder sql = new StringBuilder();
				sql.append("INSERT INTO OA_GW_DEPT_QUEUE_INFO").append(
						"(ID, S_ODEPT, DEPT_NAME, DQI_TYPE, DQI_ID, SERVER_IP, ")
						.append("SERVER_PORT, STATUS, S_USER, S_ATIME, S_MTIME)").append("VALUES('")
						.append(getNewId()).append("', '").append(Context.getUserBean().get("ODEPT_CODE"))
						.append("', '").append(paramBean.getStr("DEPT_NAME")).append("', '").append("1")
						.append("', '").append(paramBean.getStr("DQI_ID")).append("', '")
						.append(serverIp).append("', '").append(serverPort).append("', '")
						.append("0").append("', '").append(Context.getUserBean().get("USER_CODE"))
						.append("', '").append(formatter.format(new Date())).append("', '")
						.append(formatter.format(new Date())).append("');");
				Transaction.getExecutor().execute(sql.toString());
				log.debug("新增公文交换本地队列成功");
			}else {
				int num = Transaction.getExecutor().count("SELECT COUNT(1) FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_ID='"+
						paramBean.getStr("DQI_ID")+"' AND ID!='"+paramBean.getStr("ID")+"'");
				if(num>0) {
					outBean.set("message", "队列名"+paramBean.getStr("DQI_ID")+"已存在，请更换队列名后重新保存！");
					return outBean;
				}
				StringBuilder sql = new StringBuilder();
				sql.append("update OA_GW_DEPT_QUEUE_INFO set ")
				.append("S_ODEPT='" + Context.getUserBean().get("ODEPT_CODE") + "',")
				.append("DEPT_NAME='" + paramBean.getStr("DEPT_NAME") + "',")
				.append("DQI_ID='" + paramBean.getStr("DQI_ID") + "',")
				.append("SERVER_IP='" + serverIp + "',")
				.append("SERVER_PORT='" + serverPort + "',")
				.append("S_MTIME='" + formatter.format(new Date()) + "'")
				.append("where ID='" + paramBean.getStr("ID") + "'");
				Transaction.getExecutor().execute(sql.toString());
				log.debug("修改公文交换本地队列成功");
			}
			
			if(addNum>0) {
				for(int i=0;i<addNum;i++) {
					String odeptCode = paramBean.getStr("DEPT_INFO_ADD["+i+"][S_ODEPT]");
					String deptName = paramBean.getStr("DEPT_INFO_ADD["+i+"][DEPT_NAME]");
					String dqiId = paramBean.getStr("DEPT_INFO_ADD["+i+"][DQI_ID]");
					int num = Transaction.getExecutor().count("SELECT COUNT(1) FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_ID='"+dqiId+"'");
					if(num>0) {
						outBean.set("message", "队列名"+dqiId+"已存在，请更换队列名后重新保存！");
						return outBean;
					}else{
						StringBuilder addSql = new StringBuilder();
						addSql.append("INSERT INTO OA_GW_DEPT_QUEUE_INFO").append(
								"(ID, S_ODEPT, DEPT_NAME, DQI_TYPE, DQI_ID, SERVER_IP, ")
						.append("SERVER_PORT, STATUS, S_USER, S_ATIME, S_MTIME)").append("VALUES('")
						.append(getNewId()).append("', '").append(odeptCode)
						.append("', '").append(deptName).append("', '").append("2")
						.append("', '").append(dqiId).append("', '")
						.append(serverIp).append("', '").append(serverPort).append("', '")
						.append("0").append("', '").append(Context.getUserBean().get("USER_CODE"))
						.append("', '").append(formatter.format(new Date())).append("', '")
						.append(formatter.format(new Date())).append("');");
						Transaction.getExecutor().execute(addSql.toString());
					}
				}
				log.debug("批量新增公文交换远程队列成功");
			}
			
			if(updNum>0) {
				for(int i=0;i<updNum;i++) {
					String id = paramBean.getStr("DEPT_INFO_UPD["+i+"][ID]");
					String odeptCode = paramBean.getStr("DEPT_INFO_UPD["+i+"][S_ODEPT]");
					String deptName = paramBean.getStr("DEPT_INFO_UPD["+i+"][DEPT_NAME]");
					String dqiId = paramBean.getStr("DEPT_INFO_UPD["+i+"][DQI_ID]");
					
					int num = Transaction.getExecutor().count("SELECT COUNT(1) FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_ID='"+dqiId+"' AND ID!='"+id+"'");
					if(num>0) {
						outBean.set("message", "队列名"+dqiId+"已存在，请更换队列名后重新保存！");
						return outBean;
					}else{
						StringBuilder updSql = new StringBuilder();
						updSql.append("update OA_GW_DEPT_QUEUE_INFO set ")
						.append("S_ODEPT='" + odeptCode + "',")
						.append("DEPT_NAME='" + deptName + "',")
						.append("DQI_ID='" + dqiId + "',")
						.append("SERVER_IP='" + serverIp + "',")
						.append("SERVER_PORT='" + serverPort + "',")
						.append("S_MTIME='" + formatter.format(new Date()) + "'")
						.append("where ID='" + id + "'");
						Transaction.getExecutor().execute(updSql.toString());
					}
				}
				log.debug("批量修改公文交换远程队列成功");
			}
			//事务提交
			Transaction.commit();
		} catch (Exception e) {
			//事务回滚
			Transaction.rollback();
			outBean.set("message", "保存异常");
			log.error("保存公文交换机构队列信息异常，异常信息为：" + e.getMessage(),e);
			return outBean;
		}
		outBean.set("message", "保存成功");
		return outBean;
	}

	/**
	 * @title: maintainQueueInfo
	 * @descriptin: 获取队列信息
	 * @param @param paramBean
	 * @param @return
	 * @return OutBean
	 * @throws
	 */
	public OutBean getQueueInfos(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		SqlBean sqlBeanOne = new SqlBean();
		sqlBeanOne.selects("ID,S_ODEPT,DEPT_NAME,DQI_TYPE,DQI_ID,SERVER_IP,SERVER_PORT,STATUS");
		sqlBeanOne.and("DQI_TYPE", "1");
		List<Bean> beanOne = ServDao.finds("OA_GW_DEPT_QUEUE_INFO", sqlBeanOne);
		
		SqlBean sqlBeanTwo = new SqlBean();
		sqlBeanTwo.selects("ID,S_ODEPT,DEPT_NAME,DQI_TYPE,DQI_ID,SERVER_IP,SERVER_PORT,STATUS");
		sqlBeanTwo.and("DQI_TYPE", "2");
		sqlBeanTwo.orders("STATUS");
		List<Bean> beanTwo = ServDao.finds("OA_GW_DEPT_QUEUE_INFO", sqlBeanTwo);
		
		outBean.set("localQueue", beanOne);
		outBean.set("remoteQueues", beanTwo);
		return outBean;
	}
	/**
	 * @title: testQueue
	 * @descriptin: 测试队列
	 * @param @return
	 * @return OutBean
	 * @throws
	 */
	public OutBean testQueue(ParamBean paramBean) {
		OutBean bean = new OutBean();
		String localDqiId = paramBean.getStr("localDqiId");
		String remoteDqiId = paramBean.getStr("remoteDqiId");
		try {
			GwJhTestSenderMsg test = new GwJhTestSenderMsg();
			test.callMqReq(remoteDqiId, localDqiId);
		} catch (Exception e) {
			bean.set("message", "测试异常");
			log.error("测试队列异常，异常信息为：" + e.getMessage(),e);
			return bean;
		}
		String sql = "UPDATE OA_GW_DEPT_QUEUE_INFO SET STATUS='1' WHERE DQI_ID='"+remoteDqiId+"'";
		Transaction.getExecutor().execute(sql);
		bean.set("message", "测试成功，请稍后刷新页面查看测试结果");
		return bean;
	}
	
	/**
	 * @title: deleteQueueInfo
	 * @descriptin: 删除队列信息
	 * @param @param paramBean
	 * @param @return
	 * @return OutBean
	 * @throws
	 */
	public OutBean deleteQueueInfo(ParamBean paramBean) {
		OutBean bean = new OutBean();
		try {
			String sql = "DELETE FROM OA_GW_DEPT_QUEUE_INFO WHERE ID='"+paramBean.getStr("ID")+"'";
			Transaction.getExecutor().execute(sql);
			log.debug("删除id为"+paramBean.getStr("ID")+"的队列信息成功！");
			bean.set("message", "删除成功");
		} catch (Exception e) {
			bean.set("message", "删除异常");
			log.error("删除异常，异常信息为：" + e.getMessage(), e);
			return bean;
		}
		return bean;
	}
	
}
