package com.rh.gw.gdjh.tlq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.rh.gw.gdjh.exception.MqException;
import com.rh.gw.gdjh.factory.TongLinkqQueueConnectionFactory;
import com.rh.gw.gdjh.util.ConfigInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tongtech.jms.FileMessage;
import com.tongtech.jms.Session;

public class GwJhReissueMsg {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GwJhReissueMsg.class);
	public static final String remoteFactory = "RemoteConnectionFactory";
	public static TongLinkqQueueConnectionFactory tlqc = null;
	// ConnectionFactory ：连接工厂，JMS 用它创建连接
	public static ConnectionFactory connectionFactory = null;

	// Connection ：JMS 客户端到JMS Provider 的连接
	private static Connection connection = null;

	// Session发送或接收消息的线程
	private Session session = null;
	// MessageProducer：消息发送
	private MessageProducer producer = null;
	// MessageConsumer：消息消费
	private MessageConsumer consumer = null;
	// 队列
	private Queue queue = null;
	// 消息回复到这个Queue
	private Queue replyQueue = null;
	// mq发送时间间隔
	private Integer warnTimeThreshold = 0;

	// 等待重试的间隔时间
	private static final long RETRY_WAIT_TIME = 50;
	private static final String ERROR_MESSAGE = "，等待重试发生异常";
	// 默认重试最大次数
	private static final int DEFAULT_REPLY_TIME = 4;
	private static ThreadLocal<Connection> threadlocal = new ThreadLocal<Connection>();

	/**
	 * 初始化连接
	 * 
	 * @param  
	 * @param  
	 * @return
	 */
	public Connection initConnection() {
		try {
			tlqc = TongLinkqQueueConnectionFactory.getInstance(ConfigInfo.GW_JH_CONNECTION);
			connection = threadlocal.get();
			connectionFactory = (ConnectionFactory) tlqc.lookup(remoteFactory);
			if (connection == null) {
				connection = connectionFactory.createConnection();
				threadlocal.set(connection);
				LOGGER.error(" GwWindqMessage MQ createConnection channel failed  ");
			}
			session = (Session) connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);// true是支持事务
			return connection;
		} catch (JMSException e) {
			LOGGER.error("GwJhReissueMsg MQ channel connection failed: "+e.getMessage(),e);
		} catch (NamingException e) {
			LOGGER.error("GwJhReissueMsg MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("GwJhReissueMsg MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 初始化消息发送
	 * 
	 * @param windqName
	 * @return
	 * @throws Exception
	 */

	private MessageProducer initMessageProducer(String windqName) throws Exception {
		queue = (Queue) tlqc.lookup(windqName);
		// 得到消息
		producer = session.createProducer(queue);
		// 设置不持久化
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		return producer;
	}

	/**
	 * 初始化消息接收
	 * 
	 * @param windqName
	 * @return
	 * @throws Exception
	 */
	private MessageConsumer initMessageConsumer(String windqName) throws Exception {
		// 消费
		return session.createConsumer((Queue) tlqc.lookup(windqName));

	}

//	/**
//	 * 回传队列
//	 * 
//	 * @param localQueue
//	 * @return
//	 * @throws Exception
//	 */
//	private Queue getReplyQueue(String localQueue) throws Exception {
//		// 回传队列
//		return (Queue) tlqc.lookup(localQueue);
//	}

	/**
	 * 发送消息（公文交换）
	 * 
	 * @param windqQueueName 发送队列
	 * @param localQueueName 回传队列
	 * @param fileStr        文件信息
	 * @param mqTimeMaxval   报警间隔
	 * 
	 */
	public String callMqReq(String windqName,    String reqStr, Integer mqTimeMaxval)
			throws Exception {
		LOGGER.info("GwJhReissueMsg start callMqReq to MQ queue,QueueName :{}", windqName);
		connection = initConnection();
		if (connection == null) {
			LOGGER.error(" GwJhReissueMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
			throw new MqException("GwJhReissueMsg MQ createconnection channel failed ");

		}
		// �?启连接，并发送消息
		connection.start();//一般情况下，如果只是发送消息，而不接收不需要start() ;
		// 但是这个发送消息的客户服务端需要处理客户端反馈回来的消息，以start()
		return callMqReqAndReply(windqName,   reqStr, mqTimeMaxval, DEFAULT_REPLY_TIME);
	}
	/**
	 * 接收回传消息（公文交换）
	 * 
	 * @param windqQueueName 发送队列
	 * @param localQueueName 回传队列
	 * @param fileStr        文件信息
	 * @param mqTimeMaxval   报警间隔
	 * 
	 */
	public String callMqReqByReply(String windqName,   String reqStr, Integer mqTimeMaxval)
			throws Exception {
		LOGGER.info("GwJhReissueMsg start callMqReq to MQ queue,QueueName :{}", windqName);
		connection = initConnection();
		if (connection == null) {
			LOGGER.error(" GwJhReissueMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
			throw new MqException("GwJhReissueMsg MQ createconnection channel failed ");

		}
		// �?启连接，并发送消息
		connection.start();//一般情况下，如果只是发送消息，而不接收不需要start() ;
		// 但是这个发送消息的客户服务端需要处理客户端反馈回来的消息，以start()
		return callMqReqAndReply(windqName, reqStr, mqTimeMaxval, DEFAULT_REPLY_TIME);
	}
	/**
	 * 发�?�消息（公文交换�?
	 * 
	 * @param windqQueueName 发�?�队�?
	 * @param localQueueName 回传队列 本地机构�?
	 * @param fileStr        文件信息
	 * @param mqTimeMaxval   报警�?�?
	 * @param repeatTimes    重试次数
	 */
	public String callMqReqAndReply(String windqName,  String reqStr, Integer mqTimeMaxval,
			int repeatTimes) throws Exception {
		LOGGER.info("GwJhReissueMsg start callMqReqAndReply to MQ queue,QueueName :{}", windqName);
		long start = System.currentTimeMillis();
		warnTimeThreshold = mqTimeMaxval;// 发送mq的警告间隔
		try {
			if (connection == null) {
				LOGGER.error(" GwJhReissueMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
				throw new MqException("GwJhReissueMsg MQ createconnection channel failed ");
 			}
			MapMessage mapMessage = session.createMapMessage();
  			initMessageProducer(windqName).send(mapMessage);
  			session.commit();
 		} catch (JMSException e) {
			// 报jmsException 存表不用重发
			LOGGER.error(" MQ channel   jms connection  failed: "+ e.getMessage(),e);
			throw new MqException("MQ send by jms failed !");
		} catch (Exception e) {
			//如果失败，重试次数加1并重新放回队列
			decrRepeatTimesMqReq(windqName, reqStr, repeatTimes);
			LOGGER.error(" GwJhReissueMsg callMqReqAndReply channel to MQ  failed,current QueueName:{}:"+e.getMessage(), windqName, e);
		} finally {
			long cost = System.currentTimeMillis() - start;
			if ((warnTimeThreshold != null) && (warnTimeThreshold.intValue() > 0)) {
				if (cost >= warnTimeThreshold.intValue()) {
					LOGGER.warn(" The MQ GwJhReissueMsg [callMqReqAndReply] cost " + cost + " ms .");
				} else {
					LOGGER.debug(" The MQ  GwJhReissueMsg [callMqReqAndReply] cost " + cost + " ms .");
				}
			}
			stop();// 关闭资源
			close();
		}

		LOGGER.info("GwJhReissueMsg end callMqReq to MQ queue,QueueName :{}", windqName);
		return "success";
	}

//	/**
//	 * 接收消息
//	 * 
//	 * @param windqName
//	 * @param messageListener
//	 */
//	public void receiveMsg(String windqName, MessageListener messageListener) {
//		LOGGER.info("GwJhReissueMsg start receiveMsg to MQ queue,QueueName :{}", windqName);
//		try {
//			initMessageConsumer(windqName).setMessageListener(messageListener);
//		} catch (Exception e) {
//			LOGGER.error("GwJhReissueMsg receiveMsg channel to MQ  failed ,QueueName:{}", windqName, e);
//		}
//		LOGGER.info("GwJhReissueMsg end receiveMsg to MQ queue,QueueName :{}", windqName);
//	}

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

//			if (null != connection) {
//				connection.close();
//			}
		} catch (Exception e) {
			LOGGER.error("GwJhReissueMsg  stop to MQ failed ,failMsg:"+e.getMessage(), e);
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
	 * 返回状�?�，并发送消息（重发�?
	 * 
	 * @param windqName      目标队列
	 * @param replyWindqName 回传队列
	 * @param fileStr        消息
	 * @param ip
	 * @param port
	 * @param repeatTimes    重试次数
	 * @throws Exception
	 */
	private void decrRepeatTimesMqReq(String windqName,String reqStr, int repeatTimes)
			throws Exception {
		LOGGER.info("GwJhReissueMsg start decrRepeatTimesMqReq 重试剩余", repeatTimes);
		repeatTimes--;
		if (repeatTimes > 0) {
			try {
				Thread.sleep(RETRY_WAIT_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("GwJhReissueMsg decrRepeatTimesMqReq" + ERROR_MESSAGE+","+e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
			callMqReqAndReply(windqName, reqStr, null, repeatTimes);
		} else {
			throw new MqException("GwJhReissueMsg decrRepeatTimesMqReq，MQ请求重试完成，接口调用失败！", "");
		}
	}
}
