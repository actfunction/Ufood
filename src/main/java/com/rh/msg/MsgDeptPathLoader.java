package com.rh.msg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhTrigger;
import com.rh.core.util.scheduler.SchedulerMgr;

public class MsgDeptPathLoader {
	private static Log log = LogFactory.getLog(MsgConsumerLoader.class);
	/**
     * 任务计划管理器启动 启动失败抛出异常
     */
    public void start() {
        // 添加本地任务
        //
        try {
            //非心跳
        	log.info("add MsgDeptPathLocalJob ");
            RhJobDetail jobver = new RhJobDetail();
            jobver.setJobCode("MsgDeptPathLocalJob");
            jobver.setJobClass("MsgDeptPathLocalJob");

            RhTrigger triggerpant = new RhTrigger();
            triggerpant.setCode("MsgDeptPathLoaderTrigger");
            triggerpant.setDescription("every 1800s execute");
            triggerpant.setJobCode(jobver.getJobCode());
            triggerpant.setRepeatCount(-1);
            triggerpant.setInterval(Context.getSyConf("MSG_DEPT_PATH_INTERVAL", 1800));
            SchedulerMgr.getLocalScheduler().addJob(jobver);
            SchedulerMgr.getLocalScheduler().addTrigger(triggerpant);
        } catch (Exception e) {
        	log.info("error MsgDeptPathLocalJob "+e.getMessage());
            e.printStackTrace();
        }

    }
}
