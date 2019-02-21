package com.rh.gw.gdjh.serv;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import java.io.File;
import com.tongtech.jms.FileMessage;
import com.tongtech.jms.Session;

import test.OnMessageTopicRollback;


public class QueueFileReceMessageExample {
    public static final String tcf = "tongtech.jms.jndi.JmsContextFactory";/* initial context factory*/

    //public static final String remoteURL = "tlq://84.232.237.221:10024";
    public static final String remoteURL = "tlq://122.67.77.5:10024";
    public static final String remoteFactory = "RemoteConnectionFactory";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConnectionFactory testConnFactory = null;
		Connection myConn = null;
		Session mySession = null;
		Queue testQueue = null;
		MessageProducer testProducer = null;
		MessageConsumer testConsumer = null;
		FileMessage fileMessage = null;

		try {
			Properties pro = new Properties();

		
			pro.setProperty("java.naming.factory.initial", tcf);
			pro.setProperty("java.naming.provider.url", remoteURL);
			
			javax.naming.Context ctx = new javax.naming.InitialContext(pro);

			testConnFactory = (javax.jms.ConnectionFactory) ctx.lookup(remoteFactory);
			testQueue = (javax.jms.Queue) ctx.lookup("lq1");

			myConn = testConnFactory.createConnection();
			mySession = (com.tongtech.jms.Session)myConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			testProducer = mySession.createProducer(testQueue);
			testConsumer = mySession.createConsumer(testQueue);
			myConn.start();
		/*	File file = new File("D:\\newTLQ\\Xshell6_wm.exe");
			if(!file.exists()){
				file.createNewFile();
			}
			fileMessage = mySession.createFileMessage(file.getAbsolutePath());//文件需要在环境变量TLQSNDFILESDIR指向的目录中存在
			System.out.println("发送消息");
			testProducer.send(fileMessage);
			System.out.println("发送完成.");*/
			Message message = testConsumer.receive(3000);
			int i=1;
			while (message != null) {
				if(message instanceof FileMessage){
					FileMessage msg= (FileMessage)message;
					System.out.println("接受到"+i+"条文件消息,文件名:"+msg.getFile());//文件存储在TLQRCVFILESDIR指向的目录中
				}else{
					System.out.println("接受到"+i+"条非文件类型消息");
				}
				i++;
				message = testConsumer.receive(3000);
			}
			/*if(message != null){
				if(message instanceof FileMessage){
					FileMessage msg= (FileMessage)message;
					System.out.println("接受到1条文件消息,文件名:"+msg.getFile());//文件存储在TLQRCVFILESDIR指向的目录中
				}else{
					System.out.println("接受到1条非文件类型消息");
				}
			}else{
					System.out.println("未收到消息");
			}*/

		} catch (Exception jmse) {
			System.out.println("Exception oxxurred :" + jmse.toString());
			jmse.printStackTrace();
		} finally {
			try {
				if (mySession != null) {
					mySession.close();
				}
				if (myConn != null) {
					myConn.close();
				}
			} catch (Exception e) {
				System.out.println("退出时发生错误。");
				e.printStackTrace();
			}
		}
		

	}
	/**
	 * @title: onMess
	 * @descriptin: TODO
	 * @param 
	 * @return void
	 * @throws
	 */
	private void onMess() {
		// TODO Auto-generated method stub

	}
}
