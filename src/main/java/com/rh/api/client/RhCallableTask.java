package com.rh.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;

public class RhCallableTask implements Callable<Object> {
	/**
	 * 日志
	 */
    protected static Log log = LogFactory.getLog(RhCallableTask.class);
    /**
     * 任务接收的数据Bean
     */
	private Bean taskBean;
	/***
	 * 已经执行过的list
	 */
	private List<Bean> hasList = null;
	/**
	 * 需要执行的类对象
	 */
    private DistrClientServ disClient = null;
    /**
     * 读写锁
     */
    private ReadWriteLock rwl = new ReentrantReadWriteLock();   

    /**
     * 构造函数
     * @param disBean 分发Bean
     * @param disClient 类对象
     */
	public RhCallableTask(Bean disBean, DistrClientServ disClient) {
		this.taskBean = disBean;
		this.disClient = disClient;
	}

	public Object call() throws Exception {
		List<Bean> rtnList = new ArrayList<Bean>();
		log.error("======>>>" + taskBean.getStr("code") + "任务启动");
		rtnList = this.disClient.send(this.taskBean);
		log.error("======>>>" + taskBean.getStr("code") + "任务结束");
		return rtnList;
//		return hasList = new ArrayList<Bean>();
//		log.debug(">>>" + taskBean + "任务启动");
//		Date dateTmp1 = new Date();
//		int sleepNum = (int) (Math.random() * 30 + 1000); // 注意不要写成(int)Math.random()*3，这个结果为0，因为先执行了强制转换
//		Thread.sleep(sleepNum);
//		Date dateTmp2 = new Date();
//		long time = dateTmp2.getTime() - dateTmp1.getTime();
//		log.debug(">>>" + taskBean + "任务终止");
//		return taskBean + "任务返回运行结果,当前任务时间【" + time + "毫秒】";
	}


	/**
	 * 写入锁
	 * @param hasList
	 */
    public void set(List<Bean> hasList) {  
        rwl.writeLock().lock();// 取到写锁  
        try {  
            log.debug(Thread.currentThread().getName() + "准备写入数据");  
            try {  
                Thread.sleep(20);  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
            this.hasList = hasList;  
            log.debug(Thread.currentThread().getName() + "写入" + this.hasList);  
        } finally {  
            rwl.writeLock().unlock();// 释放写锁  
        }  
    }     
    
    /**
     * 获取锁
     */
    public void get() {  
        rwl.readLock().lock();// 取到读锁  
        try {  
            log.debug(Thread.currentThread().getName() + "准备读取数据");  
            try {  
                Thread.sleep(20);  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
            log.debug(Thread.currentThread().getName() + "读取" + this.hasList);  
        } finally {  
            rwl.readLock().unlock();// 释放读锁  
        }  
    }  
}
