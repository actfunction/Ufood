package com.rh.gw.gdjh.tlq;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import com.rh.gw.gdjh.exception.MqException;
import com.rh.gw.gdjh.factory.TongLinkqQueueConnectionFactory;
import com.rh.gw.gdjh.util.ConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tongtech.jms.Session;

public class GwJhTestSenderMsg {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GwJhTestSenderMsg.class);
	public static final String remoteFactory = "RemoteConnectionFactory";
	public static TongLinkqQueueConnectionFactory tlqc = null;
	// ConnectionFactory ：连接工厂，JMS 用它创建连接
	public static ConnectionFactory connectionFactory = null;

	// Connection ：JMS 客户端到JMS Provider 的连接
	private static Connection teseConnection = null;

	// Session： 一个发送或接收消息的线程
	private Session session = null;
	// MessageProducer：消息发送者
	private MessageProducer producer = null;
	// MessageConsumer：消息消费者
	private MessageConsumer consumer = null;
	// 队列名
	private Queue queue = null;

	// 等待重试的间隔时间
	private static final long RETRY_WAIT_TIME = 50;
	private static final String ERROR_MESSAGE = "，等待重试发生异常:";
	// 默认重试最大次数
	private static final int DEFAULT_REPLY_TIME = 4;
	private static ThreadLocal<Connection> threadlocal = new ThreadLocal<Connection>();

	/**
	 * 初始化连接
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	public Connection initConnection() {
		try {
			tlqc = TongLinkqQueueConnectionFactory.getInstance(ConfigInfo.GW_JH_CONNECTION);
			teseConnection = threadlocal.get();
			connectionFactory = (ConnectionFactory) tlqc.lookup(remoteFactory);
			if (teseConnection == null) {
				teseConnection = connectionFactory.createConnection();
				threadlocal.set(teseConnection);
			}
			session = (Session) teseConnection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);// true是支持事务
			return teseConnection;
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

	/**
	 * 初始化消息发送者
	 * 
	 * @param windqName
	 * @return
	 * @throws Exception
	 */

	private MessageProducer initMessageProducer(String windqName) throws Exception {
		queue = (Queue) tlqc.lookup(windqName);
		// 得到消息【发送者】
		producer = session.createProducer(queue);
		// 设置不持久化
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		return producer;
	}

	/**
	 * 发送消息（公文交换）
	 * 
	 * @param windqQueueName 发送队列
	 * @param localQueueName 回传队列
	 * 
	 */
	public String callMqReq(String windqName, String localQueueName)
			throws Exception {
		LOGGER.info("GwJhSenderMsg start callMqReq to MQ queue,QueueName :{}", windqName);
		teseConnection = initConnection();
		if (teseConnection == null) {
			LOGGER.error(" GwJhSenderMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
			throw new MqException("GwJhSenderMsg MQ createconnection channel failed ");

		}
		// 开启连接，并发送消息
		teseConnection.start();// 一般情况下，如果只是发送消息，而不接收不需要start() ;
		// 但是这个发送消息的客户服务端需要处理客户端反馈回来的消息，所以start()
		return callMqReqAndReply(windqName, localQueueName, DEFAULT_REPLY_TIME);
	}
 
	
	/**
	 * 发送消息（公文交换）
	 * 
	 * @param windqQueueName 发送队列
	 * @param localQueueName 回传队列 本地机构名
	 * @param repeatTimes    重试次数
	 */
	public String callMqReqAndReply(String windqName, String localQueueName,
			int repeatTimes) throws Exception {
		LOGGER.info("GwJhSenderMsg start callMqReqAndReply to MQ queue,QueueName :{}", windqName);
		long start = System.currentTimeMillis();
 		try {
			if (teseConnection == null) {
				LOGGER.error(" GwJhSenderMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
				throw new MqException("GwJhSenderMsg MQ createconnection channel failed ");
 			}
			TextMessage message = session.createTextMessage("测试");
			message.setStringProperty("MSG_TYPE", "3");
			message.setStringProperty("ReplyQueue", localQueueName);
			message.setJMSExpiration(3000);
			initMessageProducer(windqName).send(message);
			long end = 	System.currentTimeMillis();
			System.out.println("发送时间:"+(end-start));
 		} catch (JMSException e) {
			// 报jmsException 存表不用重发
			LOGGER.error(" test:MQ channel   jms connection  failed: "+e.getMessage(),e);
			throw new MqException("MQ send by jms failed !");
		} catch (Exception e) {
 			//如果失败，重试次数加1并重新放回队列
			decrRepeatTimesMqReq(windqName, localQueueName, repeatTimes);
			LOGGER.error(" GwJhSenderMsg callMqReqAndReply channel to MQ  failed,current QueueName:{}:"+e.getMessage(), windqName, e);
		} finally {
			stop();// 关闭资源
			close();
		}

		LOGGER.info("GwJhSenderMsg end callMqReq to MQ queue,QueueName :{}", windqName);
		return "success";
	}

	/**
	 * 回收资源
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

			if (null != teseConnection) {
				teseConnection.close();
			}
		} catch (Exception e) {
			LOGGER.error("GwJhSenderMsg  stop to MQ failed ,failMsg:"+e.getMessage(), e);
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
	 * 返回状态，并发送消息（重发）
	 * 
	 * @param windqName      目标队列
	 * @param replyWindqName 回传队列
	 * @param fileStr        消息
	 * @param ip
	 * @param port
	 * @param repeatTimes    重试次数
	 * @throws Exception
	 */
	private void decrRepeatTimesMqReq(String windqName, String localQueueName, int repeatTimes)
			throws Exception {
		LOGGER.info("GwJhSenderMsg start decrRepeatTimesMqReq 重试剩余{}次  ", repeatTimes);
		repeatTimes--;
		if (repeatTimes > 0) {
			try {
				Thread.sleep(RETRY_WAIT_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("GwJhSenderMsg decrRepeatTimesMqReq" + ERROR_MESSAGE+","+e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
			callMqReqAndReply(windqName, localQueueName, repeatTimes);
		} else {
			 //修改mq状态为发送失败
			
			throw new MqException("GwJhSenderMsg decrRepeatTimesMqReq，MQ请求重试完成，接口调用失败！", "");
		}
	}
}
