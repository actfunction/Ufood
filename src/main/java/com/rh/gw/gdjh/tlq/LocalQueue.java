package com.rh.gw.gdjh.tlq;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

public class LocalQueue {
	
	private  String IP ;
	private  String port ;
	private  String queueName ;

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public LocalQueue() throws Exception {
		String sql = "SELECT SERVER_IP AS IP,SERVER_PORT AS PORT,DQI_ID AS QUEUENAME FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_TYPE = '1' ";
		List<Bean> query = Transaction.getExecutor().query(sql);
		if(query!=null && query.size()>0) {
			this.IP = query.get(0).getStr("IP");
			this.port = query.get(0).getStr("PORT");
			this.queueName = query.get(0).getStr("QUEUENAME");
		}else {
			throw new Exception("本地队列未配置");
		}
	}
	
}
