/**
 * 审计管理分系统行政办公管理子系统
 * @file:DocumentExchangeServ.java
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
package com.rh.gw.gdjh.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.gw.gdjh.serv.DocumentExchangeServ;
import com.rh.gw.gdjh.tlq.GwJhReplyServer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;

import com.rh.gw.gdjh.tlq.LocalQueue;

/**
  * 审计管理分系统行政办公管理子系统
  * 回传消息 
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
public class GwJhReplyMsgJob extends CommonServ implements Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(GwJhReplyMsgJob.class);
	
	private GwJhReplyServer gw = new GwJhReplyServer();
	private DocumentExchangeServ serv = new DocumentExchangeServ();
	/**
	 * @title: selectReturnData
	 * @descriptin: TODO
	 * @param 
	 * @return void
	 * @throws
	 */
	private List<Bean> selectReturnData() {
		List<Bean> query= null ;
		try {
			String sql = "select OA_GW_JH_RECEIVER_INFO.GW_ID, OA_GW_JH_RECEIVER_INFO.DQI_ID,OA_GW_JH_RECEIVER_INFO.STATUS from "
					+ " OA_GW_JH_RECEIVER_INFO left join OA_GW_DEPT_QUEUE_INFO on OA_GW_JH_RECEIVER_INFO.DQI_ID = OA_GW_DEPT_QUEUE_INFO.DQI_ID"
					+ " where OA_GW_JH_RECEIVER_INFO.MSG_TYPE = 2 and OA_GW_DEPT_QUEUE_INFO.STATUS =1";
			query = Transaction.getExecutor().query(sql);
		} catch (Exception e) {
			
			LOGGER.error("异常信息："+e.getMessage(), e);
		}
		return query;
	}
	
	/**
	 * @title: send
	 * @descriptin: TODO
	 * @param 
	 * @return void
	 * @throws
	 */
	public void retrunSend() {
		LOGGER.info("回传消息开始,Start:{}",System.currentTimeMillis());
		List<Bean> returnData = selectReturnData();
		try {
			LocalQueue localQueue = new LocalQueue();
			String localQueueName = localQueue.getQueueName();
			if(returnData!=null&&returnData.size()>0) {
				for (Bean bean : returnData) {
					String GW_ID = bean.getStr("GW_ID");
					String STATUS = bean.getStr("STATUS");
					String DQI_ID = bean.getStr("DQI_ID");
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(GW_ID+","+localQueueName, STATUS);
					String str = JSON.toJSONString(map);
					try {
						gw.sendMsgs(DQI_ID, str, localQueue.getIP(), localQueue.getPort(), 0);
						serv.updateReceive(GW_ID, DQI_ID);
					} catch (Exception e) {
						LOGGER.error("回传异常:GW_ID:{},DQI_ID:{},STATUS:{},{}:"+e.getMessage(),GW_ID,DQI_ID,STATUS,e);
						e.printStackTrace();
					}
				}
			}
		LOGGER.info("回传消息结束，end:{}",System.currentTimeMillis());
		} catch (Exception e) {
			LOGGER.error("回传异常:"+e.getMessage(), e);
		}
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		retrunSend();
	}
	
}
