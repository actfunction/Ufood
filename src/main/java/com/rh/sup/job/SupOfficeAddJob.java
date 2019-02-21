package com.rh.sup.job;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

/**
 *  署内　自动批量立项　定时任务
 */
public class SupOfficeAddJob extends RhJob{
    @Override
    protected void executeJob(RhJobContext rhJobContext) {
        SupOfficeAndServ serv = new SupOfficeAndServ();
        serv.startJob();
    }

    @Override
    public void interrupt() {

    }
}
