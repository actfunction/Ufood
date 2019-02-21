/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */

package com.rh.core.util.scheduler.imp;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.comm.ScheduleHelper;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhSchedulerException;
import com.rh.core.util.scheduler.RhTrigger;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * @author wanglong
 * 
 */
public class GlobalQuartzScheduler extends QuartzScheduler {

    /** log */
    private static Log log = LogFactory.getLog(GlobalQuartzScheduler.class);

//    private static final String QUARTZ_JOB_CLASS = "com.rh.core.util.scheduler.imp.RhQuartzGlobalJob";

    @Override
    public void start() {
        // start
        if (started) {
            log.warn(" start failed, the scheduleMgr is already running.");
        } else {
            Properties prop = null;
            prop = Context.getProperties(Context.app(APP.RESOURCESPATH) + "/properties/quartz.properties");
            String scStartFlag = prop.getProperty("com.rh.sc.startflag");
            if (null == scStartFlag || !scStartFlag.equalsIgnoreCase("false")) {
                // 动态指定系统缺省数据源为任务调度使用的数据源
                // prop.setProperty("org.quartz.dataSource.myDS.jndiURL", Context.getDSBean().getStr(DS.FULL_NAME));
                // First we must get a reference to a scheduler
                try {
                    SchedulerFactory sf = new StdSchedulerFactory(prop);
                    quartzScheduler = sf.getScheduler();
                    quartzScheduler.start();
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    throw new RhSchedulerException(e);
                }
                log.info("the global scheduler started");
                // scheduler listener
                // sched.getListenerManager().addSchedulerListener(schedulerListener)
                super.start();
            }
        }

        // init
        initJobs();
    }

    /**
     * 初始化任务
     */
    private void initJobs() {

        // init job
        Set<JobKey> allJobs = null;
        try {
            allJobs = getAllJobs();
        } catch (SchedulerException e) {
            throw new RuntimeException("schedule manager get all jobs failed.", e);
        }

        if (null != allJobs && 0 < allJobs.size()) {
            return;
        }
        OutBean outBean = ServMgr.act(new ParamBean(ServMgr.SY_COMM_SCHED, ServMgr.ACT_FINDS));
        List<Bean> jobs = null;
        if (outBean != null) {
            jobs = outBean.getList(Constant.RTN_DATA);
        }
        if (null == jobs || 0 == jobs.size()) {
            return;
        }
        log.warn("---Schedule data missing, we start restore ...");
        for (Bean job : jobs) {
            // sync job to schedule manager
//             String classStr = job.getStr(SchedBaseServ.JOB);
//             String jobName = job.get(SchedBaseServ.JOB_PK, Lang.getUUID());
//             String jobDesc = job.getStr(SchedBaseServ.JOB_DESC);
//             boolean recovery = false;
//             if (1 == job.getInt(SchedBaseServ.JOB_RECOVERY)) {
//             recovery = true;
//             }
//             String dataParam = job.getStr(SchedBaseServ.JOB_DATA);
//             JobDataMap data = valueOfMap(dataParam);

            JobDetail jobDetail = valueOfJobDetail(new RhJobDetail(job),  job.getStr("JOB_CLASS_NAME"));
            // build job
            try {
                add(jobDetail);
            } catch (SchedulerException se) {
                throw new RuntimeException("the schedule instance error", se);
            }
            // sync job's triggers
            initTriggersOfJob(jobDetail.getKey());
        }
    }

    /**
     * @param jobKey 任务key
     * 
     */
    private void initTriggersOfJob(JobKey jobKey) {
        Bean outBean = new CommonServ().finds(new ParamBean(ServMgr.SY_COMM_SCHED_TRIGGER).set(RhTrigger.JOB_CODE,
                jobKey.getName()));
        List<Bean> triggers = null;
        if (outBean != null) {
            triggers = outBean.getList(Constant.RTN_DATA);
        }
        if (null == triggers || 0 == triggers.size()) {
            return;
        }
        for (Bean trig : triggers) {
            try {
                Trigger trigger = valueOfTrigger(new RhTrigger(trig));
                add(trigger);
                updateJobAndTriggersState(trigger.getJobKey());
            } catch (SchedulerException se) {
                throw new RuntimeException("add trigger for job failed.", se);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("save failed.", e);
            }

        }
    }

    @Override
    public void addJob(RhJobDetail rhJobDetail) {
        checkStatus();
        // add job
        try {
            JobDetail jobDetail = valueOfJobDetail(rhJobDetail, rhJobDetail.getJobClass());
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
        checkStatus();

        param.setServId(ServMgr.SY_COMM_SCHED);
        OutBean result = new CommonServ().query(param);

        Set<JobKey> allJobs = new HashSet<JobKey>();
        try {
            // get current all jobs size from db
            int currentSize = ServDao.count(ServMgr.SY_COMM_SCHED, new Bean());

            // get current all jobs from quartz
            List<String> groupList = quartzScheduler.getJobGroupNames();
            for (String group : groupList) {
                Set<JobKey> jobs = quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
                allJobs.addAll(jobs);
                jobs.clear();
            }

            // if schedule manager' jobs size != databases's jobs size
            // we sync job with schedule manager
            if (currentSize != allJobs.size()) {
                log.warn("the jos's size in db not match with schedule manager, we attempt to restore");
                syncJobs(allJobs, currentSize);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public OutBean getJob(ParamBean param) {
        checkStatus();

        param.setServId(ServMgr.SY_COMM_SCHED);
        OutBean outBean = new CommonServ().byid(param);

        RhJobDetail rhJobDetail = new RhJobDetail(outBean);
        boolean matched = false;
        JobDetail exitsJob = null;
        try {
            JobKey job = ScheduleHelper.getJobKey(rhJobDetail.getJobCode());
            exitsJob = quartzScheduler.getJobDetail(job);
        } catch (JobPersistenceException je) {
            log.warn("job env error:" + je.getMessage(), je);
            outBean.setError("job env error:" + je.getMessage());
        } catch (SchedulerException e) {
            throw new RuntimeException(Context.getSyMsg("SY_SCHED_ERROR") + ":" + e.getMessage(), e);
        }
        if (null != exitsJob) {
            matched = equals(rhJobDetail, exitsJob);
        }
        if (!matched) {
            ServDao.deletes(ServMgr.SY_COMM_SCHED, new ParamBean().setId(rhJobDetail.getJobCode()));
            log.warn(" the job not matched with schedule manager, we delete it and revert at next query.");
        }
        return outBean;
    }

    @Override
    public OutBean getTrigger(ParamBean param) {
        param.setServId(ServMgr.SY_COMM_SCHED_TRIGGER);
        OutBean outBean = new CommonServ().byid(param);

        RhTrigger rhTrigger = new RhTrigger(outBean);

        boolean matched = false;
        Trigger exitsTrig = null;
        try {
            String pk = rhTrigger.getCode();
            TriggerKey triggerKey = ScheduleHelper.getTriggerKey(pk);
            exitsTrig = quartzScheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("get trigger failed.", e);
        }
        if (null != exitsTrig) {
            matched = equals(rhTrigger, exitsTrig);
        }
        // if not found this job,we delete it
        if (!matched) {
            Bean delBean = new Bean();
            delBean.setId(rhTrigger.getCode());
            ServDao.deletes(ServMgr.SY_COMM_SCHED_TRIGGER, delBean);
            log.warn(" the trigger not matched with schedule manager, we delete it!");

            throw new RhSchedulerException("the trigger not found. Please check the log of job history.");
        }
        return outBean;
    }

    /**
     * update all of job state
     * @param job <CODE>JobKey</CODE>
     * @param state the state String
     */
    @Override
    public void updateState(JobKey job, JobStatus state) {
        Bean param = new Bean();
        param.set("JOB_NAME", job.getName());
        param.set("JOB_STATE", state.toString());
        param.setId(job.getName());
        ServDao.update("SY_COMM_SCHED", param);
    }

    /**
     * update trigger state
     * @param trigger Trigger
     * @param state state String
     */
    @Override
    public void updateState(Trigger trigger, TriggerStatus state) {
        Bean param = new Bean().setId(trigger.getKey().getName());
        param.set(RhTrigger.TRIG_STATE, state.toString());
        RhTrigger trigBean = new RhTrigger(param);
        if (null != trigger.getNextFireTime()) {
            trigBean.setNextFireTime(trigger.getNextFireTime());
        }
        if (null != trigger.getPreviousFireTime()) {
            trigBean.setPrevFireTime(trigger.getPreviousFireTime());
        }
        if (null != trigger.getStartTime()) {
            trigBean.setStartTime(trigger.getStartTime());
        }
        if (null != trigger.getEndTime()) {
            trigBean.setEndTime(trigger.getEndTime());
        }
        ServDao.update(ServMgr.SY_COMM_SCHED_TRIGGER, param);

    }

    /**
     * sync all jobs with schedule manager
     * @param allJobs all jobs key
     * @param currentSize current jobs size in the database
     * @throws SchedulerException throws this exception, if schedule instance error
     */
    protected void syncJobs(Set<JobKey> allJobs, int currentSize) throws SchedulerException {
        int increment = 0;
        String allIds = "";
        for (JobKey job : allJobs) {
            Bean exits = ServDao.find(ServMgr.SY_COMM_SCHED, job.getName());
            if (null == exits) {
                JobStatus state = getState(job);
                // if not exits, we insert this job into database
                Bean addBean = valueOfBean(quartzScheduler.getJobDetail(job), state);
                ServDao.create(ServMgr.SY_COMM_SCHED, addBean);
                increment++;
            }
            allIds += ("'" + job.getName() + "',");
        }
        if ((currentSize + increment) > allJobs.size()) {
            Bean delParam = new Bean();
            allIds = allIds.substring(0, allIds.length() - 1);
            String condition = " AND JOB_CODE NOT IN (" + allIds + ")";
            condition += "  AND SCHED_NAME='" + getSchedulerName() + "'";
            delParam.set(Constant.PARAM_WHERE, condition);
            ServDao.deletes(ServMgr.SY_COMM_SCHED, delParam);
        }
    }

}
