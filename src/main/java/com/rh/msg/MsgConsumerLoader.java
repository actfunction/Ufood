package com.rh.msg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhTrigger;
import com.rh.core.util.scheduler.SchedulerMgr;

public class MsgConsumerLoader  {

	private static Log log = LogFactory.getLog(MsgConsumerLoader.class);
	/**
     * 任务计划管理器启动 启动失败抛出异常
     */
    public void start() {
        // 添加本地任务
        //
        try {
            //非心跳
        	log.info("add MsgConsumerLocalJob ");
            RhJobDetail jobver = new RhJobDetail();
            jobver.setJobCode("MsgConsumerLocalJob");
            jobver.setJobClass("MsgConsumerLocalJob");

            RhTrigger triggerpant = new RhTrigger();
            triggerpant.setCode("MsgConsumerLocalJobTrigger");
            triggerpant.setDescription("every 300s execute");
            triggerpant.setJobCode(jobver.getJobCode());
            triggerpant.setRepeatCount(Context.getSyConf("MSG_CONUSMER_INIT_REPEAT", 0));
            triggerpant.setInterval(Context.getSyConf("MSG_CONUSMER_INIT_INTERVAL", 3600));
            SchedulerMgr.getLocalScheduler().addJob(jobver);
            SchedulerMgr.getLocalScheduler().addTrigger(triggerpant);
        } catch (Exception e) {
        	log.info("error MsgConsumerLocalJob "+e.getMessage());
            e.printStackTrace();
        }

    }

}
