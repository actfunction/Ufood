package com.rh.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.util.DateUtils;

/**
 * 有返回值的线程
 */
public class DisThreadPool {
	/**
	 * 日志
	 */
    protected static Log log = LogFactory.getLog(DisThreadPool.class);
    /**
     * 线程数
     */
    private static int threadNum = 3;
    /**
     * list
     */
    private static List<Bean> disList = null;
    /**
     * 要执行的对象
     */
    private static DistrClientServ disClientServ = null;
    private static DisThreadPool defaultPool;
    private static ExecutorService threadPool = null;


    static {
    	disList = new ArrayList<Bean>();
        defaultPool = new DisThreadPool(threadNum, disList, disClientServ);
    }

    /**
     * 构造方法
     * @param thNum 线程数
     * @param list 要循环的list
     * @param disClient 要执行的类对象
     */
    public DisThreadPool(int thNum, List<Bean> list, DistrClientServ disClient) {
    	disList = list;
    	threadNum = thNum;
    	disClientServ = disClient;	
        threadPool = Executors.newFixedThreadPool(threadNum);
    }

    /**
     * execute 方法
     * @return 执行结果
     */
    public List<Future<Object>> execute() {
    	String beginTime = DateUtils.getDatetimeTS();
		// 创建多个有返回值的任务
		List<Future<Object>> list = new ArrayList<Future<Object>>();
		for (Bean dis : disList) {
			RhCallableTask callTask = new RhCallableTask(dis, disClientServ);
			Future<Object> f = threadPool.submit(callTask);
			list.add(f);
			
		}
//		for (int i = 0; i < 10; i++) {
//			// 执行任务并获取Future对象
//			Future<Object> f = threadPool.submit(callTask);
//			// System.out.println(">>>" + f.get().toString());
//			list.add(f);
//		}
		// 关闭线程池
		shutdown();



		// 获取所有并发任务的运行结果
		for (Future<Object> f : list) {
			try {
				// 从Future对象上获取任务的返回值，并输出到控制台
				log.debug("======>>>" + f.get().toString());
			} catch (InterruptedException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			} catch (ExecutionException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}

    	String endTime = DateUtils.getDatetimeTS();
    	long diffTime = DateUtils.getDiffTime(beginTime, endTime);
		log.debug("----程序结束运行----，程序运行时间【" + diffTime + "毫秒】");

		return list;
    }


    /**
     * 关闭线程池
     */
	public void shutdown() {
        threadPool.shutdown();
    }

    public static DisThreadPool getDefaultPool() {
        return defaultPool;
    }
}
