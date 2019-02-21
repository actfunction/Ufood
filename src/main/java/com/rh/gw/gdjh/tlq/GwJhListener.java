package com.rh.gw.gdjh.tlq;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import com.rh.gw.gdjh.exception.MqException;
import com.rh.gw.gdjh.factory.TongLinkqQueueConnectionFactory;
import com.rh.gw.gdjh.serv.DocumentExchangeServ;
import com.rh.gw.gdjh.util.ConfigInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.tongtech.jms.FileMessage;
import com.tongtech.tmqi.jmsclient.MessageConsumerImpl;


public   class GwJhListener implements MessageListener {


	private static final Logger LOGGER = LoggerFactory.getLogger(GwJhListener.class);
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
	private static ThreadLocal<Connection> threadlocal = new ThreadLocal<Connection>();
	private DocumentExchangeServ serv = new DocumentExchangeServ();
	
	/*static {
		try {
			tlqc = TongLinkqQueueConnectionFactory.getInstance();
			connectionFactory = (ConnectionFactory) tlqc.lookup(remoteFactory);
			connection = connectionFactory.createConnection();
			// 开启连接，并发送消息
			connection.start();
			session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);// true是支持事务
 		} catch (JMSException e) {
			LOGGER.error("MQ channel connection failed: ", e.getMessage());
 		} catch (NamingException e) {
			LOGGER.error("MQ channel RemoteConnectionFactory failed: ", e.getMessage());
		}
	}*/
	
	public void initConnection() {
		try {
			connection = threadlocal.get();
			if (connection == null) {
				tlqc = TongLinkqQueueConnectionFactory.getInstance(ConfigInfo.GW_JH_CONNECTION);
				connectionFactory = (ConnectionFactory) tlqc.lookup(remoteFactory);
				connection = connectionFactory.createConnection();
				threadlocal.set(connection);
			}
			if(session ==null) {
				session = (Session) connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);// true是支持事务
			}
			connection.start();
		} catch (JMSException e) {
			LOGGER.error("GwJhSenderMsg MQ channel connection failed: "+e.getMessage(),e);
			throw new MqException("GwJhSenderMsg MQ channel connection failed ");
 		} catch (NamingException e) {
			LOGGER.error("GwJhSenderMsg MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
			throw new MqException("GwJhSenderMsg MQ channel RemoteConnectionFactory failed");
  		} catch (Exception e) {
			// TODO Auto-generated catch block
  			LOGGER.error("GwJhSenderMsg MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
			throw new MqException("GwJhSenderMsg MQ channel RemoteConnectionFactory failed");
		}
	}
	public GwJhListener(String windqName) {
		LOGGER.info("GwJhListener start receiveMsg to MQ queue,QueueName :{}", windqName);
		try {
			initConnection();
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
		String fileStr = null;
 		try {
 			String jmsID = message.getJMSMessageID();
 			LOGGER.info("begin recieve message by GwJhListener JMSMessageID:{}",jmsID);
  			String MSG_TYPE = message.getStringProperty("MSG_TYPE");
			MapMessage mapMessage = null;
			TextMessage textMessage = null;
			FileMessage fileMessage = null;
			if("1".equals(MSG_TYPE)) {
				String DQI_ID =  message.getStringProperty("DQI_ID");
				String GW_ID =  message.getStringProperty("GW_ID");
				if (message instanceof MapMessage) {
					mapMessage = (MapMessage) message;
				}
				if (message instanceof FileMessage) {
					fileMessage = (FileMessage) message;
					CheckRecvPercentage checkPercent = new CheckRecvPercentage((MessageConsumerImpl) consumer);
					new Thread(checkPercent).start();//接收百分比
					fileStr = fileMessage.getFile();//文件名作为公文id值
					
					if(StringUtils.isEmpty(GW_ID)){
						File file = new File(fileStr);
						if(file.exists()) {
							String filename = file.getName();
							String string = filename.split("-")[1];
							GW_ID = string.split("\\.")[0];
						}
					}
					int STATUS = serv.execute(GW_ID, DQI_ID, "", fileStr, 0,jmsID);
					try {
					  Map<String,Object> map = new HashMap<String,Object>();
					  LocalQueue localQueue = new LocalQueue();
					  String queueName = localQueue.getQueueName();		//本地队列
					  if(StringUtils.isNotEmpty(queueName)) {
						  map.put(GW_ID+","+queueName, STATUS);
						  TextMessage tMessage = session.createTextMessage(JSON.toJSONString(map));
						  tMessage.setStringProperty("MSG_TYPE", "2");
						  initMessageProducer(DQI_ID).send(tMessage);
						  serv.updateReceive(GW_ID, DQI_ID);
					  }
					} catch (Exception e) {
						LOGGER.error("公文接收：回传失败：DQI_ID:{},GW_ID:{},jmsID:{}"+e.getMessage(),GW_ID, DQI_ID,jmsID,e);
					}
					
				}
			}else if("2".equals(MSG_TYPE)) {
				if (message instanceof TextMessage) {
					textMessage = (TextMessage) message;
					Map<String,Object> dataMap = JSON.parseObject(textMessage.getText());
					serv.batchUpdate(dataMap);
				}
			}else if("3".equals(MSG_TYPE)) {
				if (message instanceof TextMessage) {
					System.out.println((TextMessage)message);
				}
				String ReplyQueue = message.getStringProperty("ReplyQueue");
				LOGGER.info("测试队列："+ReplyQueue + "，连通成功");
			}
//			session.commit();
 		}catch (JMSException e1) {
 			stop();
			close();
			LOGGER.error("异常信息："+ e1.getMessage(),e1);
 		} catch (Exception e) {
			// 插入mq信息表，进行补发操作
			LOGGER.error("error happens in GwJhListener,and message is :" + e.getMessage(), e);
			
		} 
	}

 	
 	private MessageProducer initMessageProducer(String windqName) throws Exception {
		queue = (Queue) tlqc.lookup(windqName);
		// 得到消息【发送者】
		producer = session.createProducer(queue);
		// 设置不持久化
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		return producer;
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
			return consumer;
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
		} catch (Exception e) {
            LOGGER.error("GwJhListener  stop to MQ failed ,failMsg:"+ e.getMessage(),e);
		}
	}
	
	/**
	 * 关闭当前线程
	 */
	public void close() {
		try {
			threadlocal.get().close();
			threadlocal.remove();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			LOGGER.error(" MQ channel threadlocal connection close failed: "+e.getMessage(), e);
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
		new GwJhListener(queueName);
	}
	/**
	 * 
	  * 审计管理分系统行政办公管理子系统
	 * @author: kfzx-zhanglm
	 * @date: 2018年12月10日 上午10:17:21
	 * @version: V1.0
	 * @description: TODO
	 */
	class CheckRecvPercentage implements Runnable {
	 
 		private MessageConsumerImpl consumer;

		public CheckRecvPercentage(MessageConsumerImpl consumer) {
			this.consumer = consumer;
		}

		public void run() {
			int currentPercent = 0;
				try {
					while (currentPercent != 100) {
						Thread.sleep(1000);
						currentPercent = consumer.getFileProgressPercentage();
						LOGGER.info("已经接收了" + currentPercent + "%");
					}
				} catch (InterruptedException e) {
					LOGGER.error("异常信息："+ e.getMessage(),e);
				}
		}
	}
	
}
