package com.rh.gw.gdjh.tlq;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

public class GwGdListenerInit {
	private static final Logger LOGGER = LoggerFactory.getLogger(GwGdListenerInit.class);

	public void start() {
		// TODO Auto-generated method stub
		LOGGER.info("----------------start Gd TLQ MSG listener-------------------");
		try {
			String sql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG WHERE CONF_KEY='GW_GD_QUEUE_NAME'";
			List<Bean> beanList = Transaction.getExecutor().query(sql);
			String queueName = null;
			if(beanList==null || !beanList.isEmpty()) {
				String confValue = beanList.get(0).getStr("CONF_VALUE");
				String[] strarr = confValue.split("\\|");
				queueName = strarr[1];
			}
			if(queueName==null || queueName.equals("")) {
				throw new Exception("初始化监听器，队列名称为空，请先配置队列名称");
			}
			GwGdListener listener = new GwGdListener(queueName);
		}catch(Exception e) {
			LOGGER.error("初始化监听器失败，异常信息： "+e.getMessage(), e);
		}
	}
}
