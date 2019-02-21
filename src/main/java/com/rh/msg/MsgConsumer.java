package com.rh.msg;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
/*
 * kfzx-xuyj01
 * tlq jms 消费者
 */
public class MsgConsumer {
	private static Log log = LogFactory.getLog(MsgConsumer.class);
	public static final String tongJCF = Context.getSyConf("TLQ_JCF", "tongtech.jms.jndi.JmsContextFactory");
	// TopicRemoteConnectionFactory TLQ的tlqjndi.conf中
	public static final String remoteFactory =Context.getSyConf("TLQ_FACTORY", "TopicRemoteConnectionFactory");
	
	public static byte[] readBytesContent(Message msg) {
		byte[] buf;
		int len;
		ByteArrayOutputStream content;
		BytesMessage message = (BytesMessage) msg;
		buf = new byte[1024];

		try {
			content = new ByteArrayOutputStream();
			while ((len = message.readBytes(buf)) > 0) {
				content.write(buf, 0, len);
			}
			return content.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) {
		startConsumer();
	}
	
	public static void startConsumer() {
		
		// TODO Auto-generated method stub
		//获取消费者连接参数
		String remoteURL = Context.getSyConf("TLQ_HOST", "tlq://122.67.77.5:10024");
		String IP = Context.getSyConf("TLQ_IP", "122.67.77.5");
		String Port = Context.getSyConf("TLQ_PORT", "10024");
		String Topic = Context.getSyConf("TLQ_TOPIC", "topic4aa");
		String clientId = Context.getSyConf("TLQ_OA_CLIENT_ID", "TLQOACLIENTID");
		System.out.println("IP=" + IP +" Port= " + Port + " Topic=" +Topic);
		 
		remoteURL  = "tlq://" + IP +":" +Port;
		log.info("start consumer url:"+remoteURL);
		// Topic连接工厂
		TopicConnectionFactory tcf = null;

		// Topic
		javax.jms.Topic topic = null;
		
		try {
			Properties pro = new Properties();
			pro.setProperty("java.naming.factory.initial", tongJCF);
			pro.setProperty("java.naming.provider.url", remoteURL);
			
			
			/* 初始化上下文 */
			javax.naming.Context ctx = new javax.naming.InitialContext(pro);
			tcf = (javax.jms.TopicConnectionFactory) ctx.lookup(remoteFactory);
			/* 获得Topic */
			topic = (javax.jms.Topic) ctx.lookup(Topic);
			/* 创建TopicConnection */
			TopicConnection conn = tcf.createTopicConnection();
			conn.setClientID(clientId);
			/* 启动连接 */
			conn.start();
			/* 创建TopicSession */
			TopicSession session = conn.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			/* 创建TopicSubscriber */
			TopicSubscriber subA = session.createDurableSubscriber(topic, "durable");
			//TopicSubscriber subA = session.createSubscriber(topic);	
	
			/* 接收发布的消息 */
		
			while(true)
			{
				
				try{
					Message message =  subA.receive(2000);
					if(message != null)
					{
						if(message instanceof BytesMessage)
						{
							BytesMessage msg = (BytesMessage) message;
							MsgDataModify.saveAndmodifyOneMsg(new String(readBytesContent(msg)));
							System.out.println("Sub BytesMessage ==" + new String(readBytesContent(msg)));
					
						}
						if(message instanceof TextMessage)
						{
							TextMessage msg = (TextMessage)message;
							MsgDataModify.saveAndmodifyOneMsg(msg.getText());
							System.out.println("Sub Message Text = " + msg.getText());
						}
					}
				}catch(Exception e)
				{
					e.printStackTrace();
					break;
					
				}
			
			}//End of while
		// 关闭使用完毕的对象
			subA.close();
			session.close();
			conn.close();
			ctx.close();
			log.info("close consumer url:"+remoteURL);
		} catch (Exception ex) {
			log.error("consumber error:" +ex.getMessage());
			ex.printStackTrace();
		}
	}
}
