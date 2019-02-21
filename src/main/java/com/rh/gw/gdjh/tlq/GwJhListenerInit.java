package com.rh.gw.gdjh.tlq;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

public class GwJhListenerInit {
	private static final Logger LOGGER = LoggerFactory.getLogger(GwGdListenerInit.class);

	public void start() {
		// TODO Auto-generated method stub
		LOGGER.info("-----------------start JH TLQ MSG listener--------------------");
		try {
			String sql = "SELECT DQI_ID FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_TYPE='1'";
			List<Bean> beanList = Transaction.getExecutor().query(sql);
			String queueName = null;
			if(beanList==null || !beanList.isEmpty()) {
				queueName = beanList.get(0).getStr("DQI_ID");
			}
			if(queueName==null || queueName.equals("")) {
				throw new Exception("初始化监听器，队列名称为空，请先配置队列名称");
			}
			GwJhListener listener = new GwJhListener(queueName);
		}catch(Exception e) {
			LOGGER.error("初始化监听器失败，异常信息： "+e.getMessage(), e);
		}
	}
}
