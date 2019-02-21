/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler.imp;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Lang;

/**
 * @author wanglong
 * 
 */
public class RhQuartzGlobalJob implements org.quartz.InterruptableJob {

    /** rh任务code */
    public static final String RHJOB_CODE = "RHJOB_CODE";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // get job code
        String jobCode = context.getJobDetail().getKey().getName();
        
        // get job detail
        ParamBean param = new ParamBean();
        param.setId(jobCode);
        param.setServId(ServMgr.SY_COMM_SCHED);
        OutBean outBean = new CommonServ().byid(param);
        // set up rhcontext
        RhJobDetail jobBean = new RhJobDetail(outBean);
        String classStr = jobBean.getJobClass();
        RhJob rhJob = (RhJob) Lang.createObject(RhJob.class, classStr);
        RhJobContext rhContext = new RhJobContext();
        rhContext.setRhJobDetail(jobBean);

        // execute job
        rhJob.execute(rhContext);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {

    }

}
