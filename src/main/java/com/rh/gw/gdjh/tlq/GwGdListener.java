package com.rh.gw.gdjh.tlq;


import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import com.rh.gw.gdjh.factory.TongLinkqQueueConnectionFactory;
import com.rh.gw.gdjh.serv.GwGdSqlServ;
import com.rh.gw.gdjh.util.ConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import javax.jms.MessageListener;

 
public   class GwGdListener implements MessageListener{

	private static final Logger LOGGER = LoggerFactory.getLogger(GwGdListener.class);
	public static final String remoteFactory = "RemoteConnectionFactory";
	public static TongLinkqQueueConnectionFactory tlqc = null;
	// ConnectionFactory ：连接工厂，JMS 用它创建连接
	private static ConnectionFactory connectionFactory = null;
	// Connection ：JMS 客户端到JMS Provider 的连接
	private static Connection connection = null;
	// Session： 一个发送或接收消息的线程
	private static Session session = null;
	// MessageProducer：消息发送者
	private MessageProducer producer = null;
	// MessageConsumer：消息消费者
	private MessageConsumer consumer = null;
	// 队列名
	private Queue queue = null;
	private GwGdSqlServ serv = new GwGdSqlServ();
	static {
		try {
			tlqc = TongLinkqQueueConnectionFactory.getInstance(ConfigInfo.GW_GD_CONNECTION);
			connectionFactory = (ConnectionFactory) tlqc.lookup(remoteFactory);
			connection = connectionFactory.createConnection();
			// 开启连接，并发送消息
			connection.start();
			session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);// true是支持事务
 		} catch (JMSException e) {
			LOGGER.error("MQ channel connection failed: "+e.getMessage(),e);
 		} catch (NamingException e) {
			LOGGER.error("MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
		}
	}
	
	public GwGdListener(String windqName) {
		LOGGER.info("GwJhListener start receiveMsg to MQ queue,QueueName :{}", windqName);
		try {
			initMessageConsumer(windqName).setMessageListener(this);
		} catch (Exception e) {
			LOGGER.error("GwJhListener receiveMsg channel to MQ  failed ,QueueName:{},failMsg:"+e.getMessage(),
					windqName, e);
		}
		LOGGER.info("GwJhListener end receiveMsg to MQ queue,QueueName :{}", windqName);
	}
	/**
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
 	public void onMessage(Message message) {
		LOGGER.info("begin recieve message by GwgdListener ");
 		try {
 			MapMessage mapMessage = null;
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				Map<String,Object> dataMap = JSON.parseObject(textMessage.getText());
				serv.batchUpdate(dataMap);
			}
 		} catch (Exception e) {
			// 插入mq信息表，进行补发操作
			LOGGER.error("error happens in GwJhListener,and message is :" + e.getMessage(), e);
		} 
	}


	/**
	 * 初始化消息接收者
	 * 
	 * @param windqQueueName
	 * @return
	 * @throws Exception
	 */
	private MessageConsumer initMessageConsumer(String windqName) throws Exception {
		if(consumer!=null) {
			consumer.close();
		}
 		queue = (Queue) tlqc.lookup(windqName);
		consumer = session.createConsumer(queue);
		return consumer;
 	}
	/**
	 * 回收资源
	 * 
	 * @author  
	 * @date  
	 * @throws Exception
	 */
	public void stop() {
		try {
			if (null != producer) {
				producer.close();
			}
 
			if (null != consumer) {
				consumer.close();
			}
 
			if (null != session) {
				session.close();
			}
 
			if (null != connection) {
				connection.close();
			}
		} catch (Exception e) {
            LOGGER.error("GwJhListener  stop to MQ failed ,failMsg:"+e.getMessage(),   e);
		}
	}
	/**
	 * @title: restart	重启
	 * @descriptin: TODO
	 * @param 
	 * @return void
	 * @throws
	 */
	public void restart(String queueName) {
		new GwGdListener(queueName);
	}
}
