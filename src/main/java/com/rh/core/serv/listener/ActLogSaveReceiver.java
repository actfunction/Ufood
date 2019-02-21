package com.rh.core.serv.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.util.msg.listener.BaseBatchSaveListener;

/**
 * log batch save handler
 * @author wanglong
 * 
 */
public class ActLogSaveReceiver extends BaseBatchSaveListener {
    /** log */
    private static Log log = LogFactory.getLog(ActLogSaveReceiver.class);
    /** service id */
    private static final String SERV_ID = "SY_SERV_LOG_ACT";
    
    /** 构建体方法 */
    public ActLogSaveReceiver() {
        int interval =  Context.getSyConf("SY_COMM_BATCH_SAVE_LOG_INTERVAL", 60);  //1分钟
        int maxSize =  Context.getSyConf("SY_COMM_BATCH_SAVE_LOG_MAX_SIZE", 10);  //10条
        super.init(interval, maxSize);
        log.info("ActLogSaveReceiver start...");
    }

    @Override
    protected String getServId() {
        return SERV_ID;
    }
}
