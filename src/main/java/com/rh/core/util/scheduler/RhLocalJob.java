/*
 * Copyright (c) 2014 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.util.scheduler.imp.LocalQuartzScheduler;

/**
 * @author wanglong 计划任务job
 */
public abstract class RhLocalJob implements org.quartz.InterruptableJob {

    /**
     * 任务执行
     * <p>
     * 由触发器触发执行
     * 
     * @param context - job context
     */
    protected abstract void executeJob(RhJobContext context);

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        RhJobDetail jobBean = (RhJobDetail) context.getJobDetail().getJobDataMap()
                .get(LocalQuartzScheduler.RH_JOB_BEAN);
//        String classStr = jobBean.getJobClass();
//        RhJob rhJob = (RhJob) Lang.createObject(RhJob.class, classStr);
        RhJobContext rhContext = new RhJobContext();
        rhContext.setRhJobDetail(jobBean);

        // execute job
        executeJob(rhContext);
    }

    /**
     * 任务停止
     * <p>
     * 当接收到停止任务指令后，该函数触发执行
     * 
     */
    public abstract void interrupt();
}
