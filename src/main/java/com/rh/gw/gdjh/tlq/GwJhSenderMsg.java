package com.rh.gw.gdjh.tlq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.rh.gw.gdjh.exception.MqException;
import com.rh.gw.gdjh.factory.TongLinkqQueueConnectionFactory;
import com.rh.gw.gdjh.util.ConfigInfo;
import com.rh.gw.gdjh.util.CopyUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.tongtech.jms.FileMessage;
import com.tongtech.jms.Session;

public class GwJhSenderMsg {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GwJhSenderMsg.class);
	public static final String remoteFactory = "RemoteConnectionFactory";
	public static TongLinkqQueueConnectionFactory tlqc = null;
	// ConnectionFactory ：连接工厂，JMS 用它创建连接
	public static ConnectionFactory connectionFactory = null;

	// Connection ：JMS 客户端到JMS Provider 的连接
	private static Connection connection = null;

	// Session�? �?个发送或接收消息
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

	private final int COMMIT_NUM_EACHTIME = 10;
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

	/**
	 * 回传队列
	 * 
	 * @param localQueue
	 * @return
	 * @throws Exception
	 */
	private Queue getReplyQueue(String localQueue) throws Exception {
		//回传队列
		return (Queue) tlqc.lookup(localQueue);
	}

	/**
	 * 发送消息（公文交换）
	 * 
	 * @param windqQueueName 发送队列
	 * @param localQueueName 回传队列
	 * @param fileStrList        文件信息
	 * @param mqTimeMaxval   报警间隔
	 * 
	 */
	public String callMqReq(String windqName, String localQueueName, List<Bean> fileStrList, Integer mqTimeMaxval)
			throws Exception {
		LOGGER.info("GwJhSenderMsg start callMqReq to MQ queue,QueueName :{}", windqName);
		connection = initConnection();
		if (connection == null) {
			LOGGER.error(" GwJhSenderMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
			throw new MqException("GwJhSenderMsg MQ createconnection channel failed ");

		}
		// 重启连接，并发送消息 
		connection.start();// 一般情况下，如果只是发送消息，而不接收不需要start() ;
		// 但是这个发送消息的客户服务端需要处理客户端反馈回来的消息，以start()
		return callMqReqAndReply(windqName, localQueueName, fileStrList, mqTimeMaxval, DEFAULT_REPLY_TIME);
	}
 
	
	/**
	 * 发�?�消息（公文交换�?
	 * 
	 * @param windqQueueName 发�?�队�?
	 * @param localQueueName 回传队列 本地机构�?
	 * @param fileStrList        文件信息
	 * @param mqTimeMaxval   报警�?�?
	 * @param repeatTimes    重试次数
	 */
	public String callMqReqAndReply(String windqName, String localQueueName, List<Bean> fileStrList, Integer mqTimeMaxval,
			int repeatTimes) throws Exception {
		LOGGER.info("GwJhSenderMsg start callMqReqAndReply to MQ queue,QueueName :{}", windqName);
		long start = System.currentTimeMillis();
		warnTimeThreshold = mqTimeMaxval;// 发送mq的警告间隔
		int n_num=0;
		String gwId="";
		List<Bean> replyList=new ArrayList<Bean>();//重发集合
		List<Bean> del=new ArrayList<Bean>();//已提交集合
		try {
			if (connection == null) {
				LOGGER.error(" GwJhSenderMsg MQ createconnection channel failed,current QueueName:{} ", windqName);
				throw new MqException("GwJhSenderMsg MQ createconnection channel failed ");
 			}
			Iterator<Bean> it= fileStrList.iterator();//发送集合
			replyList=  CopyUtil.deepCopyList(fileStrList) ;//复制集合给重发集合
 			while(it.hasNext()){
				Bean bean =it.next();
  			    gwId = bean.getStr("GW_ID");
				String filePath =  bean.getStr("FILE_PATH");
				if(filePath==null || filePath.equals("")) {
				   throw new MqException("GW_ID="+gwId+", FILE_PATH is empty" );
				}
				FileMessage filemessage = session.createFileMessage(filePath);
				filemessage.setStringProperty("MSG_TYPE", "1");
				filemessage.setStringProperty("GW_ID", gwId);
				filemessage.setStringProperty("DQI_ID", localQueueName);
//				if(!StringUtils.isEmpty(localQueueName)) {//不需要回复
//					replyQueue = getReplyQueue(localQueueName);// 消费接受回传消息
//					filemessage.setJMSReplyTo(replyQueue);	
//				}
				initMessageProducer(windqName).send(filemessage);
   				n_num++;
   				//修改mq信息，状态为已发送
				
				del.add(bean);
				if( n_num % COMMIT_NUM_EACHTIME==0) {//20条提交一次
                    //提交数据库 事务
					
					//tlq  session 提交
 					session.commit();
 					replyList.removeAll(del);
 					//提交的记录存表
 					updateJhMsgStatus(del, "1");
 					del.clear();
 					del=new ArrayList<Bean>();
				}
 			
				LOGGER.info("公文交换队列"+windqName+"处理结束"+ n_num +"条数据" );

  		    }
   		    session.commit();
   		    LOGGER.info("公文交换数据全部发送成功");
  	    	//插入mq信息表，记入有发送结束没有报异常，都是已发送
   		    updateJhMsgStatus(del, "1");
 		} catch (JMSException e) {
			// 报jmsException 存表不用重发
 			updateJhMsgStatus(del, "3");
			LOGGER.error(" MQ channel   jms connection  failed: "+e.getMessage(),e);
			throw new MqException("MQ send by jms failed !");
 		} catch(NamingException e) {
 			updateJhMsgStatus(replyList, "3");
 			LOGGER.error("公文交换队列"+windqName+"不存在:"+e.getMessage(),e);
 			throw new MqException("公文交换队列"+windqName+"不存在！");
		} catch (Exception e) {
			//数据库事务回滚  
			updateJhMsgStatus(replyList, "3");
			//tlq session回滚 
			session.rollback();
			//记入发送过程中第几条发送失败
			LOGGER.error("公文交换队列"+windqName+"处理中第"+ n_num +"条出错： "+e.getMessage(),e);
 			//如果失败，重试次数加1并重新放回队列
			decrRepeatTimesMqReq(windqName, localQueueName, replyList, repeatTimes);
			LOGGER.error(" GwJhSenderMsg callMqReqAndReply channel to MQ  failed,current QueueName:{}:"+e.getMessage(), windqName, e);
		} finally {
			long cost = System.currentTimeMillis() - start;
			if ((warnTimeThreshold != null) && (warnTimeThreshold.intValue() > 0)) {
				if (cost >= warnTimeThreshold.intValue()) {
					LOGGER.warn(" The MQ GwJhSenderMsg [callMqReqAndReply] cost " + cost + " ms .");
				} else {
					LOGGER.debug(" The MQ  GwJhSenderMsg [callMqReqAndReply] cost " + cost + " ms .");
				}
			}
			stop();// 关闭资源
			close();
		}

		LOGGER.info("GwJhSenderMsg end callMqReq to MQ queue,QueueName :{}", windqName);
		return "success";
	}

	/**
	 * 接收消息
	 * 
	 * @param windqName
	 * @param messageListener
	 */
	public void receiveMsg(String windqName, MessageListener messageListener) {
		LOGGER.info("GwJhSenderMsg start receiveMsg to MQ queue,QueueName :{}", windqName);
		try {
			initMessageConsumer(windqName).setMessageListener(messageListener);
		} catch (Exception e) {
			LOGGER.error("GwJhSenderMsg receiveMsg channel to MQ  failed ,QueueName:{}:"+e.getMessage(), windqName, e);
		}
		LOGGER.info("GwJhSenderMsg end receiveMsg to MQ queue,QueueName :{}", windqName);
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

//			if (null != connection) {
//				connection.close();
//			}
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
			LOGGER.error(" MQ channel threadlocal connection close failed: "+ e.getMessage(),e);
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
	private void decrRepeatTimesMqReq(String windqName, String localQueueName, List<Bean> fileStrList, int repeatTimes)
			throws Exception {
		LOGGER.info("GwJhSenderMsg start decrRepeatTimesMqReq 重试剩余", repeatTimes);
		repeatTimes--;
		if (repeatTimes > 0) {
			try {
				Thread.sleep(RETRY_WAIT_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("GwJhSenderMsg decrRepeatTimesMqReq" + ERROR_MESSAGE+","+e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
			callMqReqAndReply(windqName, localQueueName, fileStrList, null, repeatTimes);
		} else {
			 //修改mq状态为发�?�失�?
			
			throw new MqException("GwJhSenderMsg decrRepeatTimesMqReq，MQ请求重试完成，接口调用失败！", "");
		}
	}
	
	public void updateJhMsgStatus(List<Bean> del, String status) {
		for(Bean bean:del) {
			String gwId = bean.getStr("GW_ID");
			String dqiId = bean.getStr("DQI_ID");
			try {
				String sql = "UPDATE OA_GW_JH_SENDER_INFO SET STATUS='"+status+"' WHERE GW_ID='"+gwId+"' and DQI_ID='"+dqiId+"'";
				Transaction.getExecutor().execute(sql);
			}catch(Exception e) {
				LOGGER.error("UPDATE OA_GW_JH_SENDER_INFO STATUS ERROR GW_ID=" + gwId + ", Exception:" + ERROR_MESSAGE+","+e.getMessage(), e);
			}
		}
	}
}
