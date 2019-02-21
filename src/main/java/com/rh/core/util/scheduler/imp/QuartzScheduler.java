/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler.imp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.comm.ScheduleHelper;
import com.rh.core.util.scheduler.IScheduler;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhSchedulerException;
import com.rh.core.util.scheduler.RhTrigger;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * 基于<code>quartz</code>任务调度实现
 * @author wanglong
 * 
 */
public abstract class QuartzScheduler extends IScheduler {

    /** log */
    private static Log log = LogFactory.getLog(QuartzScheduler.class);

    /** milliseconds scends rate */
    private static final int RATE = 1000;

    /** status */
    protected boolean started = false;
    /** global scheduler */
    protected Scheduler quartzScheduler = null;
    
    @Override
    public void start() {
        started = true;
    }


    @Override
    public void shutdown() {
        if (!started) {
            log.warn(" Is ScheduleMgr running? Stop aborted..");
        } else {
            try {
                quartzScheduler.shutdown();
            } catch (SchedulerException e) {
                throw new RhSchedulerException(e);
            }
            started = false;
        }
    }

    @Override
    public void queryTriggersOfJob(ParamBean param, OutBean result) {
        int size = 0;
        Bean pageBean = result.getPage();
        size = pageBean.getInt("ALLNUM");
        List<Trigger> triggersOfJob;
        try {
            JobKey job = QuartzSchedulerHelper.getJobKey(param.getId());
            triggersOfJob = getTriggersOfJob(job);
            if (size != triggersOfJob.size()) {
                log.warn("the triggers size of job in db not match with schedule manager, " + "we attempt to restore");
                syncTriggers(triggersOfJob, size);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getSchedulerName() {
        String name = "";
        try {
            name = quartzScheduler.getSchedulerName();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return name;
    }

    @Override
    public abstract void addJob(RhJobDetail jobRhJobDetail);

    @Override
    public void updateJob(RhJobDetail jobRhJobDetail) {
        addJob(jobRhJobDetail);
    }

    @Override
    public void removeJob(String job) {
        try {
            JobKey jobKey = ScheduleHelper.getJobKey(job);
            // remove job
            checkStatus();
            quartzScheduler.resumeJob(jobKey);
            quartzScheduler.deleteJob(jobKey);

        } catch (SchedulerException e) {
            throw new RuntimeException("remove job failed", e);
        }

    }

    @Override
    public void removeJobs(String[] jobs) {
        for (String job : jobs) {
            removeJob(job);
        }
    }

    @Override
    public void pauseJobs(String[] keys) {
        checkStatus();
        try {
            JobKey[] jobs = ScheduleHelper.getJobKeyArray(keys);
            for (JobKey job : jobs) {
                quartzScheduler.pauseJob(job);
            }
            updateJobAndTriggersState(jobs);
        } catch (Exception e) {
            throw new RhSchedulerException("pause job failed", e);
        }
    }

    @Override
    public void resumeJobs(String[] keys) {
        checkStatus();
        try {
            JobKey[] jobs = ScheduleHelper.getJobKeyArray(keys);
            for (JobKey job : jobs) {
                quartzScheduler.resumeJob(job);
            }

            updateJobAndTriggersState(jobs);
        } catch (Exception e) {
            throw new RhSchedulerException("resume job failed", e);
        }

    }

    @Override
    public void interruptJobs(String[] jobKeys) {
        checkStatus();
        try {
            JobKey[] jobs = ScheduleHelper.getJobKeyArray(jobKeys);

            for (JobKey job : jobs) {
                quartzScheduler.resumeJob(job);
            }

            updateJobAndTriggersState(jobs);
        } catch (Exception e) {
            throw new RhSchedulerException("interrupt job failed", e);
        }
    }

    @Override
    public void addTrigger(RhTrigger triggerBean) {
        checkStatus();

        try {
            // must has startTime
            Trigger trigger = valueOfTrigger(triggerBean);

            quartzScheduler.scheduleJob(trigger);

            updateJobAndTriggersState(trigger.getJobKey());

        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void removeTrigger(String trigger) {
        checkStatus();

        try {
            TriggerKey trigKey = ScheduleHelper.getTriggerKey(trigger);
            quartzScheduler.unscheduleJob(trigKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTriggers(String[] triggers) {

        try {
            TriggerKey[] trigKeys = ScheduleHelper.getTriggerKeyArray(triggers);
            // cache the job
            Trigger target = quartzScheduler.getTrigger(trigKeys[0]);
            JobKey job = null;
            if (null != target) {
                job = target.getJobKey();
            }
            // remove all triggers
            checkStatus();
            for (TriggerKey trigger : trigKeys) {
                quartzScheduler.unscheduleJob(trigger);
            }
            // update job state
            if (null != job) {
                updateState(job);
            }
        } catch (Exception e) {
            throw new RuntimeException("remove trigger failed", e);
        }

    }

    @Override
    public void updateTrigger(RhTrigger triggerBean) {
        checkStatus();

        try {
            TriggerKey key = QuartzSchedulerHelper.getTriggerKey(triggerBean.getCode());
            quartzScheduler.unscheduleJob(key);
            addTrigger(triggerBean);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseTriggers(String[] triggers) {
        checkStatus();

        try {
            TriggerKey[] trigKeys = ScheduleHelper.getTriggerKeyArray(triggers);
            for (TriggerKey trigger : trigKeys) {
                quartzScheduler.pauseTrigger(trigger);
            }

            // cache the job
            JobKey job = null;

            for (TriggerKey triggerKey : trigKeys) {
                Trigger trigger = quartzScheduler.getTrigger(triggerKey);
                if (null == job) {
                    job = trigger.getJobKey();
                }

                // update trigger state
                updateState(trigger);
            }
            // update jobState
            updateState(job);
        } catch (Exception e) {
            throw new RuntimeException("pause trigger failed", e);
        }

    }

    @Override
    public void resumeTriggers(String[] triggers) {
        checkStatus();
        try {
            TriggerKey[] triggerKeys = ScheduleHelper.getTriggerKeyArray(triggers);
            JobKey job = null;

            for (TriggerKey trigger : triggerKeys) {
                quartzScheduler.resumeTrigger(trigger);
            }

            for (TriggerKey triggerKey : triggerKeys) {
                // get trigger
                Trigger trigger = quartzScheduler.getTrigger(triggerKey);
                if (null == job) {
                    job = trigger.getJobKey();
                }
                // update trigger state
                updateState(trigger);
            }
            // update jobState
            updateState(job);
        } catch (Exception e) {
            throw new RuntimeException("resume job failed", e);
        }

    }

    /**
     * 更新状态
     * @param job - 任务
     * @param state - 状态
     */
    public abstract void updateState(JobKey job, JobStatus state);

    /**
     * 更新状态
     * @param trigger - 触发器
     * @param state - 状态
     */
    public abstract void updateState(Trigger trigger, TriggerStatus state);

    // /////////////////////////protected methed//////////////////////////////////////////////////////

    /**
     * value of bean
     * @param job <CODE>JobDetail</CODE>
     * @param state state string
     * @return bean object
     */
    protected RhJobDetail valueOfBean(JobDetail job, JobStatus state) {
        RhJobDetail rhjobDetail = new RhJobDetail();
        rhjobDetail.setJobCode(job.getKey().getName());
        rhjobDetail.setJobGroup(job.getKey().getGroup());
        // rhjobDetail.setJobClass(job.getJobClass().toString().replace("class ", ""));
        rhjobDetail.setJobDesc(job.getDescription());
        String jobData = job.getJobDataMap().getWrappedMap().toString();
        jobData = jobData.substring(1, jobData.length() - 1);
        rhjobDetail.setJobData(jobData);
        if (job.requestsRecovery()) {
            rhjobDetail.setRecovery(1);
        } else {
            rhjobDetail.setRecovery(2);
        }
        rhjobDetail.set("SCHED_NAME", getSchedulerName());
        job.requestsRecovery();
        if (null != state) {
            rhjobDetail.set("JOB_STATE", state.toString());
        }
        return rhjobDetail;

    }

    /**
     * value of bean
     * @param rhJobDetail <CODE>JobDetail</CODE>
     * @param quartzJobClass - job class
     * @return bean object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected JobDetail valueOfJobDetail(RhJobDetail rhJobDetail, String quartzJobClass) {
        String jobCode = rhJobDetail.getJobCode();
        // String jobClass = rhJobDetail.getJobClass();
        String jobDesc = rhJobDetail.getJobDesc();
        Bean dataMap = rhJobDetail.getJobData();
        JobDataMap data = new JobDataMap();
        data.putAll((Map) dataMap);

        boolean recovery = rhJobDetail.getRecovery();
        JobDetail job = null;
        try {
            job = ScheduleHelper.buildJob(quartzJobClass, jobCode, jobDesc, data, recovery);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return job;
    }

    /**
     * value of Trigger
     * @param rhTrigger - rhtrigger bean
     * @return - Quartz Trigger
     */
    protected Trigger valueOfTrigger(RhTrigger rhTrigger) {
        String jobPk = rhTrigger.getJobCode();
        // if param not has pk, we create and set it
        String trigCode = rhTrigger.getCode();

        String trigDesc = rhTrigger.getDescription();
        int repeatCount = rhTrigger.getRepeatCount();
        int trigInterval = rhTrigger.getInterval();
        Date startTime = rhTrigger.getStartTime();
        Date endTime = rhTrigger.getEndTime();
        String cronExpr = rhTrigger.getCronExpression();
        // 1:simple trigger,2:cronatab trigger
        int type = rhTrigger.getType();

        Trigger trigger = null;
        // build trigger
        try {
            JobKey jobKey = QuartzSchedulerHelper.getJobKey(jobPk);
            if (type == 1) {
                trigger = ScheduleHelper.buildTrigger(trigCode, trigDesc, jobKey, startTime, endTime, repeatCount,
                        trigInterval);
            } else {
                trigger = ScheduleHelper.buildTrigger(trigCode, jobKey, trigDesc, cronExpr, startTime, endTime);
            }
        } catch (SchedulerException se) {
            throw new RuntimeException("add trigger for job failed.", se);
        } catch (ParseException e) {
            throw new TipException(" unix crontab expresstion format error.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("save failed.", e);
        }
        return trigger;
    }

    /**
     * valueOf map
     * @param dataStr format key1=value1,key2=value2
     * @return map
     */
    protected JobDataMap valueOfMap(String dataStr) {
        JobDataMap data = new JobDataMap();
        if (null != dataStr && 0 < dataStr.length()) {
            String[] items = dataStr.split(",");
            for (String item : items) {
                String[] tmp = item.split("=");
                if (2 == tmp.length) {
                    data.put(tmp[0].trim(), tmp[1].trim());
                }
            }
        }
        return data;
    }

    /**
     * 根据参数，提取jobname 并返回<CODE>JobKey</CODE> array
     * @param param 参数Bean
     * @return 根据参数提取到的<CODE>JobKey</CODE> array
     */
    protected JobKey[] getJobKeyArray(Bean param) {
        String[] jobNames = getIds(param);
        if (0 >= jobNames.length) {
            throw new TipException("'PK' can not be null");
        }
        JobKey[] result = null;
        try {
            result = ScheduleHelper.getJobKeyArray(jobNames);
        } catch (SchedulerException e) {
            throw new RuntimeException("get sched id error", e);
        }
        return result;
    }

    /**
     * get ids
     * @param paramBean param bean
     * @return id array
     */
    protected String[] getIds(Bean paramBean) {
        String[] ids = paramBean.getId().trim().split(Constant.SEPARATOR);
        return ids;
    }

    /**
     * bean and trigger compare the main attributes
     * @param bean Bean
     * @param target Trigger
     * @return is matched?
     */
    protected boolean equals(Bean bean, JobDetail target) {
        Bean exitsJobBean = valueOfBean(target, null);
        // if (!equals(bean, exitsJobBean, RhJobDetail.JOB)) {
        // log.warn(RhJobDetail.JOB + " job field :" + bean.get(RhJobDetail.JOB) + " != "
        // + exitsJobBean.get(RhJobDetail.JOB) + "["
        // + bean.get(RhJobDetail.JOB_PK) + "]");
        // return false;
        // }
        if (!equals(bean, exitsJobBean, RhJobDetail.JOB_DESC)) {
            log.warn(RhJobDetail.JOB_DESC + " job field :" + bean.get(RhJobDetail.JOB_DESC) + " != "
                    + exitsJobBean.get(RhJobDetail.JOB_DESC)
                    + "[" + bean.get(RhJobDetail.JOB_PK) + "]");
            return false;
        }
        if (!equals(bean, exitsJobBean, RhJobDetail.JOB_RECOVERY)) {
            log.warn(RhJobDetail.JOB_RECOVERY + " job field :" + bean.get(RhJobDetail.JOB_RECOVERY) + " != "
                    + exitsJobBean.get(RhJobDetail.JOB_RECOVERY) + "[" + bean.get(RhJobDetail.JOB_PK) + "]");
            return false;
        }
        // if (!equals(bean, exitsJobBean, JOB_DATA, "MAP")) {
        // log.warn(JOB_DATA + " job field :" + bean.get(JOB_DATA) + " != " + exitsJobBean.get(JOB_DATA)
        // + "[" + bean.get(JOB_PK) + "]");
        // return false;
        // }

        return true;
    }

    /**
     * a'value equals b'value with the key
     * @param a bean
     * @param b bean
     * @param key key
     * @return is matched
     */
    protected boolean equals(Bean a, Bean b, String key) {
        return a.getStr(key).equals(b.getStr(key));
    }

    /**
     * update all of job's triggers state
     * @param jobs <CODE>Job</CODE>
     * @throws SchedulerException throws this exception, if schedule instance error
     */
    public void updateJobAndTriggersState(JobKey[] jobs) throws SchedulerException {
        for (JobKey job : jobs) {
            updateJobAndTriggersState(job);
        }
    }

    /**
     * update all of job's triggers state
     * @param job <CODE>Job</CODE>
     * @throws SchedulerException throws this exception, if schedule instance error
     */
    public void updateJobAndTriggersState(JobKey job) throws SchedulerException {
        // 已完成trigger会自动移出，triggers状态仅包括活动、暂停状态
        List<Trigger> triggers = getTriggersOfJob(job);
        int pausedIndex = 0;
        JobStatus jobState = JobStatus.STOP;
        for (Trigger trig : triggers) {
            TriggerStatus trigStatus = TriggerStatus.STOP;
            TriggerState trigState = quartzScheduler.getTriggerState(trig.getKey());
            if (trigState == TriggerState.PAUSED) {
                trigStatus = TriggerStatus.PAUSED;
                pausedIndex++;
            } else if (trigState == TriggerState.NORMAL) {
                trigStatus = TriggerStatus.ALIVE;
                jobState = JobStatus.ALIVE;
            }
            updateState(trig, trigStatus);
        }
        if (pausedIndex == triggers.size() && 0 < triggers.size()) {
            jobState = JobStatus.PAUSED;
        }
        updateState(job, jobState);
    }

    /**
     * update job state base on it's triggers
     * @param job job key
     * @throws SchedulerException throws this exception,if schedule instance error
     */
    public void updateState(JobKey job) throws SchedulerException {
        JobStatus status = getState(job);
        updateState(job, status);
    }

    /**
     * @param trigger triggerKey
     * @return stateStr
     * @throws SchedulerException throw this exception, if schedule instance error
     */
    public TriggerStatus getState(TriggerKey trigger) throws SchedulerException {
        // 已完成trigger会自动移出，triggers状态仅包括活动、暂停状态
        TriggerStatus state = TriggerStatus.UNKNOW;
        TriggerState trigState = quartzScheduler.getTriggerState(trigger);
        if (trigState == TriggerState.PAUSED) {
            state = TriggerStatus.PAUSED;
        } else if (trigState == TriggerState.NORMAL) {
            state = TriggerStatus.ALIVE;
        }
        return state;
    }

    /**
     * @param job jobKey
     * @return stateStr
     * @throws SchedulerException throw this exception, if schedule instance error
     */
    public JobStatus getState(JobKey job) throws SchedulerException {
        // 已完成trigger会自动移出，triggers状态仅包括活动、暂停状态
        List<Trigger> triggers = getTriggersOfJob(job);
        int pausedIndex = 0;
        JobStatus jobState = JobStatus.STOP;
        for (Trigger trig : triggers) {
            // TODO if is recovery trigger set state is recoverying...
            // if (isRecoveryTrigger(trig.getKey())) {
            TriggerState trigState = quartzScheduler.getTriggerState(trig.getKey());
            if (trigState == TriggerState.PAUSED) {
                pausedIndex++;
            } else if (trigState == TriggerState.NORMAL) {
                jobState = JobStatus.ALIVE;
            }
        }
        if (pausedIndex == triggers.size() && 0 < triggers.size()) {
            jobState = JobStatus.PAUSED;
        }
        return jobState;
    }

    /**
     * update trigger state
     * @param trigger Trigger
     * @throws SchedulerException throws this exception,if schedule instance error
     */
    public void updateState(Trigger trigger) throws SchedulerException {
        TriggerStatus stateStr = getState(trigger.getKey());
        updateState(trigger, stateStr);
    }

    /**
     * 获取指定job所关联的trigger
     * @param jobKey jobkey
     * @return 该job所关联的trigger list
     * @throws SchedulerException 若当前scheduler服务异常时会抛出该异常
     */
    private List<Trigger> getTriggersOfJob(JobKey jobKey) throws SchedulerException {
        checkStatus();
        @SuppressWarnings("unchecked")
        List<Trigger> triggers = (List<Trigger>) quartzScheduler.getTriggersOfJob(jobKey);
        if (null == triggers) {
            return triggers;
        }

        // ignore recovery trigger
        List<Trigger> recoTrigs = new ArrayList<Trigger>();
        for (Trigger trig : triggers) {
            if (QuartzSchedulerHelper.isRecoveryTrigger(trig.getKey())) {
                recoTrigs.add(trig);
            }
        }
        // delete from triggers
        if (0 < recoTrigs.size()) {
            triggers.removeAll(recoTrigs);
            recoTrigs.clear();
        }
        return triggers;
    }

    /**
     * 根据参数，提取jobname 并返回<CODE>JobKey</CODE>
     * @param param 参数Bean
     * @return 根据参数提取到的<CODE>JobKey</CODE>
     */
    protected JobKey getJobKey(ParamBean param) {
        // TODO jobGroup
        String jobName = null;
        Map<String, String> paramInLinkData = extractParamFromLink(param);
        if (null != paramInLinkData && 0 < paramInLinkData.size()) {
            jobName = paramInLinkData.get(RhJobDetail.JOB_PK);
        }
        if (null != jobName) {
            JobKey jobKey = null;
            try {
                jobKey = ScheduleHelper.getJobKey(jobName);
            } catch (SchedulerException e) {
                throw new RuntimeException("get sched id error", e);
            }
            return jobKey;
        } else {
            return getJobKeyArray(param)[0];
        }
    }

    /**
     * to Bean
     * @param trig <CODE>Trigger</CODE>
     * @return <CODE>Trigger</CODE> CODE>
     */
    protected Bean valueOfBean(Trigger trig) {
        return valueOfBean(trig, null);
    }

    /**
     * to Bean
     * @param trig <CODE>Trigger</CODE>
     * @param state <CODE>TriggerState</CODE>
     * @return <CODE>Trigger</CODE> CODE>
     */
    @SuppressWarnings("static-access")
    protected Bean valueOfBean(Trigger trig, TriggerState state) {
        RhTrigger trigBean = new RhTrigger();
        trigBean.setCode(trig.getKey().getName());
        if (null == trig.getKey().getGroup() || 0 == trig.getKey().getGroup().length()) {
            trigBean.setGroup("DEFAULT");
        } else {
            trigBean.setGroup(trig.getKey().getGroup());
        }
        trigBean.setDescription(trig.getDescription());
        if (null != trig.getNextFireTime()) {
            trigBean.setNextFireTime(trig.getNextFireTime());
        }
        if (null != trig.getPreviousFireTime()) {
            trigBean.setPrevFireTime(trig.getPreviousFireTime());
        }
        trigBean.setJobCode(trig.getJobKey().getName());
        if (null == trig.getJobKey().getGroup() || 0 == trig.getJobKey().getGroup().length()) {
            trigBean.setGroup("DEFAULT");
        } else {
            trigBean.setGroup(trig.getJobKey().getGroup());
        }
        trigBean.setStartTime(trig.getStartTime());
        if (null != trig.getEndTime()) {
            trigBean.setEndTime(trig.getEndTime());
        }
        if (state == state.PAUSED) {
            trigBean.setState("PAUSED");
        } else if (state == state.COMPLETE) {
            trigBean.setState("COMPLETE");
        } else {
            trigBean.setState("ALIVE");
        }
        trigBean.set("SCHED_NAME", getSchedulerName());

        if (trig instanceof SimpleTrigger) {
            SimpleTrigger sTrig = (SimpleTrigger) trig;
            trigBean.setRepeatCount(sTrig.getRepeatCount());
            // store with milliseconds
            trigBean.setInterval((int) (sTrig.getRepeatInterval() / RATE));
            trigBean.setType(1);
        } else if (trig instanceof CronTrigger) {
            CronTrigger cTrig = (CronTrigger) trig;
            trigBean.setCronExpression(cTrig.getCronExpression());
            trigBean.setType(2);
        }
        return trigBean;
    }

    /**
     * sync all triggers of one job with schedule manager
     * @param allTriggers all triggers
     * @param currentSize current jobs size in the database
     * @throws SchedulerException throws this exception, if schedule instance error
     */
    protected void syncTriggers(List<Trigger> allTriggers, int currentSize) throws SchedulerException {
        int increment = 0;
        String allIds = "";
        String jobName = "";
        if (null != allTriggers) {
            for (Trigger trig : allTriggers) {
                Bean exits = ServDao.find(ServMgr.SY_COMM_SCHED_TRIGGER, trig.getKey().getName());
                if (null == exits) {
                    // if not exits, we insert this trigger into database
                    TriggerState state = quartzScheduler.getTriggerState(trig.getKey());
                    Bean addBean = valueOfBean(trig, state);
                    ServDao.create(ServMgr.SY_COMM_SCHED_TRIGGER, addBean);
                    jobName = trig.getJobKey().getName();
                    increment++;
                }
                allIds += ("'" + trig.getKey().getName() + "',");
            }
        }
        allIds += "'-1' ";
        if ((currentSize + increment) > allTriggers.size()) {
            Bean delParam = new Bean();
            String condition = " AND TRIGGER_CODE NOT IN (" + allIds + ")";
            condition += "  AND JOB_CODE ='" + jobName + "'";
            delParam.set(Constant.PARAM_WHERE, condition);
            ServDao.deletes(ServMgr.SY_COMM_SCHED_TRIGGER, delParam);
        }

    }

    /**
     * bean and trigger compare the main attributes
     * @param bean Bean
     * @param target Trigger
     * @return is matched?
     */
    protected boolean equals(Bean bean, Trigger target) {
        Bean exitsTrigBean = valueOfBean(target);
        if (!equals(bean, exitsTrigBean, RhTrigger.TRIG_TYPE)) {
            log.warn(RhTrigger.TRIG_TYPE + " trigger field :" + bean.get(RhTrigger.TRIG_TYPE) + " != "
                    + exitsTrigBean.get(RhTrigger.TRIG_TYPE) + "["
                    + bean.get(RhTrigger.TRIG_PK) + "]");
            return false;
        } else {
            if (1 == bean.getInt(RhTrigger.TRIG_TYPE)) {
                if (!equals(bean, exitsTrigBean, RhTrigger.TRIG_REPEAT_COUNT)) {
                    log.warn(RhTrigger.TRIG_REPEAT_COUNT + " trigger field :"
                            + bean.get(RhTrigger.TRIG_REPEAT_COUNT) + " != "
                            + exitsTrigBean.get(RhTrigger.TRIG_REPEAT_COUNT) + "["
                            + bean.get(RhTrigger.TRIG_PK) + "]");
                    return false;
                }
                if (!equals(bean, exitsTrigBean, RhTrigger.TRIG_INTERVAL)) {
                    log.warn(RhTrigger.TRIG_INTERVAL + " trigger field :" + bean.get(RhTrigger.TRIG_INTERVAL)
                            + " != "
                            + exitsTrigBean.get(RhTrigger.TRIG_INTERVAL) + "[" + bean.get(RhTrigger.TRIG_PK)
                            + "]");
                    return false;
                }
            } else {
                if (!equals(bean, exitsTrigBean, RhTrigger.TRIG_CRON_EXPRESSION)) {
                    log.warn(RhTrigger.TRIG_CRON_EXPRESSION + " trigger field :"
                            + bean.get(RhTrigger.TRIG_CRON_EXPRESSION) + " != "
                            + exitsTrigBean.get(RhTrigger.TRIG_CRON_EXPRESSION) + "["
                            + bean.get(RhTrigger.TRIG_PK) + "]");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * extracting data from link param
     * @param param param of link
     * @return param map
     */
    protected Map<String, String> extractParamFromLink(Bean param) {
        String data = param.getStr("_linkWhere");
        Map<String, String> paramInData = null;
        if (0 < data.length()) {
            paramInData = new HashMap<String, String>();
            String[] paramArray = data.split(" and");
            // for each param item
            for (String item : paramArray) {
                String[] tmp = item.replace("'", "").trim().split("=");
                if (2 == tmp.length) {
                    paramInData.put(tmp[0], tmp[1]);
                }
            }
        }
        return paramInData;
    }

    /**
     * check status
     */
    protected void checkStatus() {
        if (!started) {
            throw new RhSchedulerException("Before using this method ,must be started first by calling start");
        }
    }

    /**
     * get all jobs
     * @return return all job
     * @throws SchedulerException throws this exception, if schedule invalid
     */
    protected Set<JobKey> getAllJobs() throws SchedulerException {
        checkStatus();
        Set<JobKey> result = new HashSet<JobKey>();
        List<String> groupList = quartzScheduler.getJobGroupNames();
        for (String group : groupList) {
            Set<JobKey> jobs = quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
            result.addAll(jobs);
            jobs.clear();
        }
        // LinkedList list = new LinkedList();
        return result;
    }

    /**
     * add job Schedule the given <code>{@link org.quartz.Trigger}</code> with the <code>Job</code> identified by the
     * <code>Trigger</code>'s settings.
     * @param trigger trigger
     * @throws SchedulerException if schedule job failed, throws this exception
     */
    protected void add(Trigger trigger) throws SchedulerException {
        checkStatus();
        quartzScheduler.scheduleJob(trigger);
    }

    /**
     * add job
     * @param jobDetail jobdetail
     * @throws SchedulerException if schedule job failed, throws this exception
     */
    protected void add(JobDetail jobDetail) throws SchedulerException {
        checkStatus();
        quartzScheduler.addJob(jobDetail, true);
        log.debug(" add job:" + jobDetail.getKey());
    }

}
