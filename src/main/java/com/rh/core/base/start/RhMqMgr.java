package com.rh.core.base.start;

import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Context;
import com.ruaho.rhmq.api.RedisDataSource;
import com.ruaho.rhmq.api.SimpleConsumerAgent;
import com.ruaho.rhmq.core.executer.BaseTask;
import com.ruaho.rhmq.core.executer.IExecuter;
import com.ruaho.rhmq.core.executer.ProgressHandler;

public class RhMqMgr {

	/** log 日志 */
	private static Log log = LogFactory.getLog(RhMqMgr.class);

	public static String reidsServer = null;
	public static String reidsPass = null;
	public static String consumerKey = null;
	public static int workerNum = 0;
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static final Condition STOP = LOCK.newCondition();

	/**
	 * rhmq启动
	 */
	public void start() {
		Properties prop = Context.getProperties(Context.app(APP.WEBINF) + "redis.properties");
		reidsServer = prop.getProperty("RHMQ_REDIS_SERVER", "");
		reidsPass = prop.getProperty("RHMQ_REDIS_SERVER_PASS", "");
		consumerKey = prop.getProperty("RHMQ_CONSUMER_KEY", "");
		workerNum = Integer.parseInt(prop.getProperty("RHMQ_WORKER_NUM", "4"));

		SimpleConsumerAgent consumer;

		IExecuter executer = new IExecuter() {
			@Override
			public BaseTask.RESULT execute(String data, ProgressHandler progressHandler) throws Exception {
				System.out.println(data);
				return BaseTask.RESULT.SUCCESSFUL;
			}
		};

		// 初始化消费址
		RedisDataSource ds = new RedisDataSource();
		ds.setPassword(reidsPass);
		ds.setServer(reidsServer);
		ds.setName(consumerKey);
		ds.setProcessingLock(false);
		ds.setBroadcastModel(true);
		consumer = new SimpleConsumerAgent(executer);
		// 设置最大消费线程个数
		consumer.start(workerNum, ds);
		System.out.println("RhMqMgr is OK!..............");
	}

	/**
	 * 销毁
	 */
	public void stop() {

	}
}
