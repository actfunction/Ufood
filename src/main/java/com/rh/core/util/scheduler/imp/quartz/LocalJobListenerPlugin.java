/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */

package com.rh.core.util.scheduler.imp.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.SchedulerPlugin;

import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.scheduler.IScheduler;
import com.rh.core.util.scheduler.IScheduler.JobStatus;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.util.scheduler.SchedulerMgr;
import com.rh.core.util.scheduler.imp.QuartzScheduler;
import com.rh.core.util.scheduler.imp.QuartzSchedulerHelper;

/**
 * job history for quartz
 * @author wanglong
 */
public class LocalJobListenerPlugin implements SchedulerPlugin, JobListener {

    /** plugin name */
    private String name;
    /** current service */
    private static final String CURRENT_SERV = ServMgr.SY_COMM_LOCAL_SCHED_HIS;

    /** log */
    private static Log log = LogFactory.getLog(LocalJobListenerPlugin.class);

    /** can new instance */
    public LocalJobListenerPlugin() {
    }

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give the <code>SchedulerPlugin</code> a chance
     * to initialize.
     * </p>
     * @param pname plugin name
     * @param scheduler schedule instance
     * @throws SchedulerException if there is an error initializing.
     */
    public void initialize(String pname, Scheduler scheduler) throws SchedulerException {
        this.name = pname;
        scheduler.getListenerManager().addJobListener(this, EverythingMatcher.allJobs());
    }

    /**
     * when this plugin start call this method
     */
    public void start() {
        // do nothing...
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it should free up all of it's resources because
     * the scheduler is shutting down.
     * </p>
     */
    public void shutdown() {
        // nothing to do...
    }

    /**
     * get this plugin name
     * @return plugin name
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.quartz.JobListener#jobToBeExecuted(JobExecutionContext)
     * @param context jobContext
     */
    public void jobToBeExecuted(JobExecutionContext context) {
//        log.debug("jobToBeExecuted, update job state...");
        getQuartzScheduler().updateState(context.getJobDetail().getKey(), JobStatus.RUNNING);
        
        log.debug("jobToBeExecuted, update job state...");
        getQuartzScheduler().updateState(context.getJobDetail().getKey(), JobStatus.RUNNING);
    }

    /**
     * 
     * @see org.quartz.JobListener#jobWasExecuted(JobExecutionContext, JobExecutionException)
     * @param context job Context
     * @param jobException job Exception
     */
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Object jobDesc = context.get(RhJobContext.CONTEXT_JOB_EXECUTED_DESC_KEY);
        // TODO recovery job, set state is recovery
        if (jobException != null) {
            // execute failed
            String errMsg = jobException.getMessage();
            // save history
            saveHistory(context, false, errMsg);
        } else {
            // execute successed
            // String result = String.valueOf(context.getResult());
            // save history
            if (null == jobDesc) {
                saveHistory(context, true, "");
            } else {
                saveHistory(context, true, jobDesc.toString());
            }
        }

        // update trigger

        // update job state
        getQuartzScheduler().updateState(context.getJobDetail().getKey(), JobStatus.ALIVE);
//        log.debug("jobWasExecuted, update job state...");
    }

    /**
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     * @param context job context
     */
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    /**
     * save job history log to database
     * @param context context param
     * @param successed successed ?
     * @param desc description
     */
    private void saveHistory(JobExecutionContext context, boolean successed, String desc) {
        Trigger trigger = context.getTrigger();

        // args =
        // new Object[] {
        // context.getJobDetail().getKey().getName(),
        // context.getJobDetail().getKey().getGroup(), new java.util.Date(),
        // trigger.getKey().getName(), trigger.getKey().getGroup(),
        // trigger.getPreviousFireTime(), trigger.getNextFireTime(),
        // Integer.valueOf(context.getRefireCount()), errMsg
        // };

        ParamBean paramBean = new ParamBean(CURRENT_SERV, ServMgr.ACT_SAVE);
        paramBean.set("ID", Lang.getUUID());
        paramBean.set("JOB_NAME", context.getJobDetail().getKey().getName());
        paramBean.set("JOB_GROUP", context.getJobDetail().getKey().getGroup());
        paramBean.set("TRIGGER_NAME", trigger.getKey().getName());
        if (successed) {
            paramBean.set("STATUS", "1");
        } else {
            paramBean.set("STATUS", "2");
        }
            paramBean.set("SCHED_NAME", SchedulerMgr.getLocalScheduler().getSchedulerName());
        
        String fireTime = DateUtils.getStringFromDate(trigger.getPreviousFireTime(), DateUtils.FORMAT_TIMESTAMP);
        paramBean.set("FIRE_TIME", fireTime);
        paramBean.set("END_TIME", DateUtils.getDatetimeTS());
        paramBean.set("INSTANCE", getInstanceId(context));
        paramBean.set("REFIRE_COUNT", context.getRefireCount());
        // is recovery job
        if (QuartzSchedulerHelper.isRecoveryTrigger(trigger.getKey())) {
            paramBean.set("JOB_TYPE", 2);
        } else {
            paramBean.set("JOB_TYPE", 1);
        }
        paramBean.set("DESCRIPTION", desc);
        ServMgr.act(paramBean);
    }

    /**
     * get schedule instance id
     * @param context job context
     * @return instance id
     */
    private String getInstanceId(JobExecutionContext context) {
        String instanceId = "unknow";
        try {
            instanceId = context.getScheduler().getSchedulerInstanceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instanceId;
    }
    
    /**
     * get quartz scheduler
     * @return - quartz scheduler impl
     */
    private QuartzScheduler getQuartzScheduler() {
        IScheduler obj = SchedulerMgr.getLocalScheduler();
        return (QuartzScheduler) obj;
    }

}
