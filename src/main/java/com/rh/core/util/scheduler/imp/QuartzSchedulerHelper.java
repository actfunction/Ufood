/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler.imp;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * schedule 帮助类
 * @author wanglong
 * 
 */
public class QuartzSchedulerHelper {

	/**
	 * can not new instanll
	 */
	private QuartzSchedulerHelper() {
	}

	/** if durability = false , when job done we remove it */
	private static final boolean DURABLY = true;

	/**
	 * 根据参数构造一个JobDetail
	 * @param classStr 类路径，format:com.rh.core.job.TestJob, 该类必须extends org.quartz.Job
	 * @param jobName job name jobName 在数据库中是唯一标识
	 * @param jobDesc job描述信息
	 * @param data 为job传入的数据参数
	 * @param recovery 当job执行过程中意外中断，服务重启后是否重新执行该任务
	 * @return jobDetail
	 * @throws ClassNotFoundException 如果classStr指定的不正确会抛出该异常
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static JobDetail buildJob(String classStr, String jobName, String jobDesc, JobDataMap data,
			boolean recovery) throws ClassNotFoundException, SchedulerException {
		@SuppressWarnings("unchecked")
		Class<Job> jobClass = (Class<Job>) Class.forName(classStr);
		JobDetail jobDetail = null;
		JobBuilder builder = newJob(jobClass).storeDurably(DURABLY) // default false
				.withIdentity(jobName) // put triggers in group named after the cluster node instance
										// just to distinguish (in logging) what was scheduled from
										// where
				.requestRecovery(recovery); // ask scheduler to re-execute this job if it was in progress when
											// the scheduler went down...
		if (null != data) {
			builder.usingJobData(data);
		}
		if (null != jobDesc) {
			builder.withDescription(jobDesc);
		}
		jobDetail = builder.build();
		return jobDetail;
	}

	/**
	 * 构造一个trigger
	 * @param trigerName triger唯一标识
	 * @param trigerDesc 说明
	 * @param triggerStartTime 首次启动时间，如果该参数为null表示立即启动
	 * @param repeatCount 重复执行次数
	 * @param interval 执行间隔，单位为秒
	 * @return 生成trigger
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static Trigger buildTrigger(String trigerName, String trigerDesc, Date triggerStartTime,
			int repeatCount, int interval) throws SchedulerException {
		return buildTrigger(trigerName, trigerDesc, null, null, triggerStartTime, repeatCount, interval);
	}

	/**
	 * 构造一个trigger
	 * @param triggerName triger唯一标识
	 * @param triggerDesc 说明
	 * @param forJob 该trigger所属job
	 * @param triggerStartTime 首次启动时间，如果该参数为null表示立即启动
	 * @param triggerEndTime 截至时间，如果该参数为null该参数不生效
	 * @param repeatCount 重复执行次数 如果该参数为-1 将会一致循环执行至triggerEndTime
	 * @param interval 执行间隔，单位为秒
	 * @return 生成trigger
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static Trigger buildTrigger(String triggerName, String triggerDesc, JobKey forJob,
			Date triggerStartTime, Date triggerEndTime, int repeatCount, int interval)
			throws SchedulerException {
//		if (0 > repeatCount) {
//			repeatCount = 0;
//		}
		SimpleTrigger trigger = null;
		SimpleScheduleBuilder ssb = simpleSchedule().withIntervalInSeconds(interval);
		if (repeatCount <= -1) {
			ssb.repeatForever();
		} else {
			ssb.withRepeatCount(repeatCount);
		}
		TriggerBuilder<SimpleTrigger> builder = newTrigger().withIdentity(triggerName).withSchedule(ssb);
		// if trggerStartTime not setup, this trigger start at now
		if (null == triggerStartTime) {
			builder.startAt(futureDate(1, IntervalUnit.SECOND));
		} else {
			builder.startAt(triggerStartTime);
		}

		if (null != forJob) {
			builder.forJob(forJob);
		}
		if (null != triggerDesc) {
			builder.withDescription(triggerDesc);
		}
		if (null != triggerEndTime) {
			builder.endAt(triggerEndTime);
		}
		trigger = builder.build();
		return trigger;
	}

	/**
	 * @param triggerName trigger 名称， triger唯一标识
	 * @param forJob 该trigger所属job
	 * @param triggerDesc 说明
	 * @param expression unix crontab 时间表达式 example: 0 0/5 * * * ?
	 * @param triggerStartTime 首次启动时间，如果该参数为null表示立即启动
	 * @param triggerEndTime 截至时间，如果该参数为null该参数不生效
	 * @return crontab Trigger
	 * @throws ParseException 表达式解析错误时抛出该异常
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static Trigger buildTrigger(String triggerName, JobKey forJob, String triggerDesc,
			String expression, Date triggerStartTime, Date triggerEndTime) throws ParseException,
			SchedulerException {
		// CronExpression cronExpr = new CronExpression(expression);
		// CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cronExpr);

		TriggerBuilder<CronTrigger> builder = newTrigger().withIdentity(triggerName).withSchedule(
				cronSchedule(expression));
		if (null == triggerStartTime) {
			builder.startAt(futureDate(1, IntervalUnit.SECOND));
		} else {
			builder.startAt(triggerStartTime);
		}
		if (null != triggerEndTime) {
			builder.endAt(triggerEndTime);
		}
		if (null != forJob) {
			builder.forJob(forJob);
		}
		if (null != triggerDesc) {
			builder.withDescription(triggerDesc);
		}
		return builder.build();
	}

	/**
	 * 获取 <CODE>JobKey</CODE>
	 * 
	 * @param jobNameArray job name array
	 * @return 返回<CODE>JobKey</CODE>Array
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static JobKey[] getJobKeyArray(String[] jobNameArray) throws SchedulerException {
		JobKey[] result = new JobKey[jobNameArray.length];
		for (int i = 0; i < jobNameArray.length; i++) {
			String jobName = jobNameArray[i];
			result[i] = getJobKey(jobName);
		}
		return result;
	}
	
	/**
	 * 获取 <CODE>TriggerKey</CODE>
	 * 
	 * @param triggerNames job name array
	 * @return 返回<CODE>TriggerKey</CODE> array
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static TriggerKey[] getTriggerKeyArray(String[] triggerNames) throws SchedulerException {
		TriggerKey[] result = new TriggerKey[triggerNames.length];
		for (int i = 0; i < triggerNames.length; i++) {
			String trigName = triggerNames[i];
			result[i] = getTriggerKey(trigName);
		}
		return result;
	}


	/**
	 * 获取 <CODE>JobKey</CODE>
	 * 
	 * @param jobName job name
	 * @return 返回<CODE>JobKey</CODE>
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static JobKey getJobKey(String jobName) throws SchedulerException {
		return new JobKey(jobName);
	}

	/**
	 * 获取 <CODE>TriggerKey</CODE>
	 * 
	 * @param triggerName job name
	 * @return 返回<CODE>TriggerKey</CODE>
	 * @throws SchedulerException 如果scheduler instacell异常会抛出该异常
	 */
	public static TriggerKey getTriggerKey(String triggerName) throws SchedulerException {
		return new TriggerKey(triggerName);
	}
	
	   /**
     * if recovery trigger ?
     * @param triggerKey Trigger'key
     * @return true or false
     */
    public static boolean isRecoveryTrigger(TriggerKey triggerKey) {
        return "RECOVERING_JOBS".equals(triggerKey.getGroup());
    }
    
    
  

}
