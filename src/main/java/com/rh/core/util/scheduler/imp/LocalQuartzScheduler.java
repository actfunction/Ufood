/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */

package com.rh.core.util.scheduler.imp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhSchedulerException;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.dict.DictMgr;

/**
 * @author wanglong
 * 
 */
public class LocalQuartzScheduler extends QuartzScheduler {

    /** jvm scheduler job's status cache */
    protected Hashtable<String, String> localJobStatus = new Hashtable<String, String>();

    private static Log log = LogFactory.getLog(LocalQuartzScheduler.class);
//    private static final String QUARTZ_JOB_CLASS = "com.rh.core.util.scheduler.imp.RhQuartzLocalJob";
    /** rh job class */
    public static final String RH_JOB_BEAN = "RH_JOB_BEAN";

    @Override
    public void start() {
        // start jvm scheduler
        Properties localJobProp = null;
        localJobProp = Context.getProperties(Context.app(APP.RESOURCESPATH) + "/properties/local_job.properties");
        SchedulerFactory sf;
        try {
            sf = new StdSchedulerFactory(localJobProp);
            quartzScheduler = sf.getScheduler();
            quartzScheduler.start();
            log.info("the local scheduler started");
            super.start();
        } catch (SchedulerException e) {
            throw new RhSchedulerException(e);
        }

    }

    @Override
    public void addJob(RhJobDetail jobRhJobDetail) {
        checkStatus();
        // add job
        try {
            JobDetail jobDetail = valueOfJobDetail(jobRhJobDetail,  jobRhJobDetail.getJobClass());
            jobDetail.getJobDataMap().put(RH_JOB_BEAN, jobRhJobDetail);
            quartzScheduler.addJob(jobDetail, true);
            log.debug(" add job:" + jobDetail.getKey());

        } catch (SchedulerException e) {
            throw new RhSchedulerException("add job failed", e);
        } catch (Exception e) {
            throw new RhSchedulerException("add job failed", e);
        }

    }

    /**
     * 获取任务列表
     * @param param - 参数bean
     * @return - out bean
     */
    @Override
    public OutBean queryJob(ParamBean param) {
        OutBean outBean = new OutBean();
        Set<JobKey> allJobs;
        List<Bean> dataList = new ArrayList<Bean>();
        try {
            allJobs = getAllJobs();
            for (JobKey jobKey : allJobs) {
                JobDetail job = quartzScheduler.getJobDetail(jobKey);

                JobStatus status = getState(jobKey);
                RhJobDetail jobBean = valueOfBean(job, status);
                jobBean.set("JOB_STATE", status.toString());
                String statusDisplay = DictMgr.getFullName("SY_COMM_SCHED_STATE", status.toString());
                jobBean.set("JOB_STATE__NAME", statusDisplay);

                dataList.add(jobBean);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        outBean.setData(dataList);
        outBean.setData(dataList);
        outBean.setCount(dataList.size());
        outBean.setPage(1);

        return outBean;
    }

    @Override
    public OutBean getJob(ParamBean param) {
        return new OutBean();
    }

    @Override
    public OutBean getTrigger(ParamBean param) {
        return new OutBean();
    }

    @Override
    public void updateState(JobKey job, JobStatus state) {
        setLocalJobStatus(job, state);

    }

    @Override
    public void updateState(Trigger trigger, TriggerStatus state) {

    }

    /**
     * 设置目标本地任务的状态
     * @param job - job key
     * @param status - status
     */
    public void setLocalJobStatus(JobKey job, JobStatus status) {
        localJobStatus.put(job.toString(), status.toString());
    }

    /**
     * 根据<CODE>jobKey</CODE>获取其目前运行状态
     * @param job - job key
     * @return status string
     */
    public String getLocalJobStatus(JobKey job) {
        return localJobStatus.get(job.toString());
    }

}
