package com.rh.core.base.start;

import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhTrigger;
import com.rh.core.util.scheduler.SchedulerMgr;

/**
 * 任务计划装载类
 * @author wanglong
 * 
 */
public class ScheduleLoader {

    /**
     * 任务计划管理器启动 启动失败抛出异常
     */
    public void start() {
        if (SchedulerMgr.getGlobalScheduler() != null) {
            SchedulerMgr.getGlobalScheduler().start();
        }

        SchedulerMgr.getLocalScheduler().start();

        // 添加本地任务
        //
        try {
            // JobDetail job = ScheduleHelper.buildJob("com.rh.core.serv.dict.DictCacheJob", "dictJob", "", null,
            // false);

            RhJobDetail job = new RhJobDetail();
            job.setJobCode("dictJob");
            job.setJobClass("com.rh.core.serv.dict.DictCacheJob");

            RhTrigger trigger = new RhTrigger();
            trigger.setCode("5mTrigger");
            trigger.setDescription("every 5 minute execute");
            trigger.setJobCode(job.getJobCode());
            trigger.setRepeatCount(-1);
            trigger.setInterval(300);

            // Trigger trigger = ScheduleHelper.buildTrigger("5mTrigger", "every 5 minute execute", job.getKey(), null,
            // null, -1, 300); //每5分钟300秒执行一次

            SchedulerMgr.getLocalScheduler().addJob(job);
            SchedulerMgr.getLocalScheduler().addTrigger(trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 任务计划管理器停止 停止失败抛出异常
     */
    public void stop() {
        SchedulerMgr.getGlobalScheduler().shutdown();

        SchedulerMgr.getLocalScheduler().shutdown();
    }

}
