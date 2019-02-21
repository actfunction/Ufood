/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.comm.remind;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.ConfMgr;
import com.rh.core.util.DateUtils;
import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

/**
 * 与任务调度配合，定期进行发送提醒的工作
 * 
 * @author wanglong
 * 
 */
public class RemindJob extends RhJob {

    /** 按时间提醒数据的查询时间范围，即与当前时间相差几分钟的数据能被查询出来，准备发送，单位为分钟 **/
    private static final String CONF_TIME_RANGE = "SY_COMM_REMIND_TIME_RANGE";

    /** 实时发送数据的过期时间，用于去掉很久之前提交的已失去时效的提醒数据，单位为分钟 **/
    private static final String CONF_TIME_VALID = "SY_COMM_REMIND_TIME_VALID";

    private Logger log = Logger.getLogger(getClass());

    /**
     * 构造函数
     */
    public RemindJob() {
    }

    /**
     * 实现Job方法，进行定义调度处理
     * @param context 调度上下文信息
     */
    @Override
    public void executeJob(RhJobContext context) {
        Transaction.begin();
        try {
            Date nowTime = DateUtils.createDate();
            //取设定的时间范围，默认值为5分钟
            int rangeVal = ConfMgr.getConf(CONF_TIME_RANGE, 5);
            //取得提醒有效时间，默认值为30分钟
            int validVal = ConfMgr.getConf(CONF_TIME_VALID, 30);
            Date maxExecTime = DateUtils.addMinutes(nowTime, rangeVal);
            Date minExecTime = DateUtils.addMinutes(nowTime, -rangeVal);
            Date minAddTime = DateUtils.addMinutes(nowTime, -validVal);

            /** 取与当前时间相差设定时间范围的未发送的数据 以及 EXECUTE_TIME为NULL提醒添加时间处于有效时间范围内的数据**/
            StringBuilder str = new StringBuilder();
            str.append("select * from SY_COMM_REMIND where ");
            str.append(" EXECUTE_TIME >= '").append(DateUtils.formatDatetime(minExecTime));
            str.append("' and EXECUTE_TIME <= '").append(DateUtils.formatDatetime(maxExecTime));
            str.append("' and STATUS = 'WATING' ");
            str.append(" union ");
            str.append(" select * from SY_COMM_REMIND where ");
            str.append(" EXECUTE_TIME = '' ");
            str.append(" and STATUS = 'WATING' ");
            str.append(" and S_ATIME >= '").append(DateUtils.formatDatetime(minAddTime)).append("'");

            List<Bean> sendList = Context.getExecutor().query(str.toString());

            for (final Bean sendBean : sendList) {
                try {
                    RemindMsgSender sender = new RemindMsgSender();
                    sender.send(sendBean);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    RemindMgr.finish(sendBean);
                }
                Transaction.commit();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Transaction.end();
        }
    }



    @Override
    public void interrupt() {
        // TODO Auto-generated method stub
        
    }
}
