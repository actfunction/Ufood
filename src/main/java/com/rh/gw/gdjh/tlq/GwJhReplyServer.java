package com.rh.gw.gdjh.tlq;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import com.rh.gw.gdjh.exception.MqException;
import com.rh.gw.gdjh.factory.TongLinkqQueueConnectionFactory;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import com.tongtech.jms.FileMessage;
import com.tongtech.jms.Session;
 
  
public class GwJhReplyServer {
     /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GwJhReplyServer.class);
    public static final String remoteFactory = "RemoteConnectionFactory";
    public static TongLinkqQueueConnectionFactory tlqc=null;
     // ConnectionFactory ：连接工厂，JMS 用它创建连接
    public static ConnectionFactory connectionFactory = null;
 
     // Connection ：JMS 客户端到JMS Provider 的连接
    private static Connection connection = null;
 
    // Session： 一个发送或接收消息的线程
    private Session session = null;
    // MessageProducer：消息发送者
    private MessageProducer producer = null;
    // MessageConsumer：消息消费者
 	private MessageConsumer consumer = null;
    //队列名
    private Queue queue = null;
    //消息回复到这个Queue  
    private  Queue replyQueue   = null;
    //mq发送时间阈值
    private Integer warnTimeThreshold = 0;
    //临时监听
	private Destination tempDest;
	// 等待重试的间隔时间
	private static final long RETRY_WAIT_TIME = 1000;
	private static final String ERROR_MESSAGE = "，等待重试发生异常:";
	// 默认重试最大次数
	private static  int DEFAULT_REPLY_TIME = 4;
	
	
     /**
     * 根据ip地址初始化连接
     * @param ip
     * @param port
     * @return
     */
    public  Connection initConnection(String ip,String port) {
    	try {
        	tlqc=TongLinkqQueueConnectionFactory.getInstance(ip,port);
			connectionFactory = (ConnectionFactory) tlqc.lookup(remoteFactory);
 		    connection = connectionFactory.createConnection();
 	        session = (Session) connection.createSession(Boolean.TRUE, Session.CLIENT_ACKNOWLEDGE);//true是支持事务
  		    return connection;
         } catch (JMSException e) {
            LOGGER.error("GwJhWindqMessage MQ channel connection failed: "+e.getMessage(),e);
         } catch (NamingException e) {
            LOGGER.error("GwJhWindqMessage MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
 		 } catch (Exception e) {
			// TODO Auto-generated catch block
 			LOGGER.error("GwJhWindqMessage MQ channel RemoteConnectionFactory failed: "+e.getMessage(),e);
		}
	    return null; 
     }
     /**
     * 初始化消息发送者
     * @param windqQueueName
     * @return
     * @throws Exception
     */

    private MessageProducer initMessageProducer(String windqName) throws Exception{
        queue=(Queue) tlqc.lookup(windqName);
        // 得到消息【发送者】
        producer = session.createProducer(queue);
        // 设置不持久化
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }
 
    /**
     * 初始化消息接收者
     * @param windqQueueName
     * @return
     * @throws Exception
     */
     private MessageConsumer initMessageConsumer(String windqName) throws Exception {
         // 使用临时目的地创建消费者，及消费者的临听程序
        if(windqName==null) {
           tempDest = session.createTemporaryQueue();
        }else {
        tempDest=(Queue) tlqc.lookup(windqName);
           return  session.createConsumer(tempDest);
        }

        return session.createConsumer(tempDest);
        
    }
     /**
      * 发送消息（公文交换）
 * @param windqQueueName
 * @param replyWindqName
 * @param fileMessage
 * @param ip
 * @param port
 * @param mqTimeMaxval
 *  
 */
// public String  sendMsg(String windqName,String fileStr, String ip,String port,Integer mqTimeMaxval,int repeatTimes) throws Exception {
//   	 LOGGER.info("GwJhServerWindqMsg start callMqReq to MQ queue,QueueName :{}", windqName);
// //    System.out.println("GwJhServerWindqMsg start callMqReq to MQ queue,QueueName :{}"+windqName);
//	
//   	 connection=initConnection(ip,port);
//     if(connection==null) {
//        LOGGER.error(" GwJhServerWindqMsg MQ createconnection channel failed,current QueueName:{} ", windqName );
//     }
//	 // 开启连接，并发送消息
//	 connection.start();//一般情况下，如果只是发送消息，而不接收不需要start() ;
//	 //但是这个发送消息的客户服务端需要处理客户端反馈回来的消息，所以start()  
//     return callMqReqAndReply(windqName, fileStr, ip, port, mqTimeMaxval, repeatTimes);
// }
 
 public String  sendMsgs(String windqName,String str, String ip,String port,Integer mqTimeMaxval) throws Exception {
   	 LOGGER.info("GwJhServerWindqMsg start callMqReq to MQ queue,QueueName :{}", windqName);
 //    System.out.println("GwJhServerWindqMsg start callMqReq to MQ queue,QueueName :{}"+windqName);
	
   	 connection=initConnection(ip,port); 
     if(connection==null) {
        LOGGER.error(" GwJhServerWindqMsg MQ createconnection channel failed,current QueueName:{} ", windqName );
     }
	 // 开启连接，并发送消息
	 connection.start(); //一般情况下，如果只是发送消息，而不接收不需要start() ;
	 //但是这个发送消息的客户服务端需要处理客户端反馈回来的消息，所以start()  
     return callMqReqAndReplys(windqName, str, ip, port,DEFAULT_REPLY_TIME);
 }
     
     
     
     
     public String callMqReqAndReplys(String windqName, String str, String ip,String port,int num) throws Exception {
       	 LOGGER.info("GwJhServerWindqMsg start callMqReqAndReply to MQ queue,QueueName :{}", windqName );
         long start = System.currentTimeMillis();
         warnTimeThreshold=(int) RETRY_WAIT_TIME;//发送mq的警告阈值
         try {
   			String correlationId = UUID.randomUUID().toString();//值为公文交换Id
  			//consumer=initMessageConsumer("MyQueue");
  			MapMessage mapMessage = session.createMapMessage();
  			mapMessage.setStringProperty("MSG_TYPE","2");
  			mapMessage.setString("content",str);
  			//mapMessage.setJMSMessageID(correlationId);
  			initMessageProducer(windqName).send(mapMessage);
  			
           //System.err.println("[INFO] send mssage:"+mapMessage.getString("content")+"MSG_TYPE:"+mapMessage.getStringProperty("MSG_TYPE"));
  			/*StopWatch obj = new StopWatch();
  			obj.start();*/
           	session.commit();
 			LOGGER.info(" client MQ  return message is :"   );
         } catch (Exception e) {
        	e.printStackTrace();
   			LOGGER.error(" GwJhServerWindqMsg callMqReqAndReply channel to MQ  failed,current QueueName:{}:"+e.getMessage(), windqName ,   e);
			decrRepeatTimesMqReq(windqName, str, ip, port, num);
         } finally {
        	 long cost = System.currentTimeMillis() - start;
             if ((warnTimeThreshold != null) && (warnTimeThreshold.intValue() > 0)) {
               if (cost >= warnTimeThreshold.intValue()) {
            	   LOGGER.warn(" The MQ GwJhServerWindqMsg [callMqReqAndReply] cost " + cost + " ms .");
               }else {
            	  LOGGER.debug(" The MQ  GwJhServerWindqMsg [callMqReqAndReply] cost " + cost + " ms .");
               }
             }
             LOGGER.debug("成功");
             stop();//关闭资源
           }
         
        LOGGER.info("GwJhServerWindqMsg end callMqReq to MQ queue,QueueName :{}", windqName );
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
   
  			if (null != connection) {
  				connection.close();
  			}
  		} catch (Exception e) {
            LOGGER.error("GwJhServerWindqMsg  stop to MQ failed ,failMsg:"+e.getMessage(),   e);
  		}
  	}
  	 
  	/**
  	 * 返回状态，并发送消息（重发）
  	 * 
  	 * @param windqName    目标队列
  	 * @param replyWindqName  回传队列
  	 * @param fileStr  消息
  	 * @param ip   
  	 * @param port    
  	 * @param repeatTimes 重试次数
  	 * @throws Exception
  	 */
  	private void decrRepeatTimesMqReq(String windqName,String fileStr,String ip, String port,int repeatTimes)
  			throws Exception {
  		LOGGER.info("GwJhServerWindqMsg start decrRepeatTimesMqReq 重试剩余{}次  ", repeatTimes);
   		repeatTimes--;
  		if (repeatTimes > 0) {
  			try {
  				Thread.sleep(RETRY_WAIT_TIME);
  			} catch (InterruptedException e) {
  				LOGGER.error("GwJhServerWindqMsg decrRepeatTimesMqReq" + ERROR_MESSAGE+","+e.getMessage(), e);
  				Thread.currentThread().interrupt();
  				
  			}
  			callMqReqAndReplys(windqName, fileStr,ip,port,repeatTimes);
  		} else {
  			throw new MqException("GwJhServerWindqMsg decrRepeatTimesMqReq，MQ请求重试完成，接口调用失败！", "");
  		}
  	}
}
 
