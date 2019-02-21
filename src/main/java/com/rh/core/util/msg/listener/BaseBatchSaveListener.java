/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.util.msg.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.TimeCountMsgListener;

/**
 * batch save listener 批量保存监听
 * @author wanglong
 * 
 */
public abstract class BaseBatchSaveListener extends TimeCountMsgListener {

    /** log */
    private static Log log = LogFactory.getLog(BaseBatchSaveListener.class);

    @Override
    public void onMsg(List<Msg> msgs) {
        if (null == msgs || 0 == msgs.size()) {
            return;
        }
        List<Bean> batch = new ArrayList<Bean>();
        for (Msg msg : msgs) {
            batch.add(msg.getBody());
        }
        batchSave(batch);
    }

    /**
     * get Service id
     * @return service Id
     */
    protected abstract String getServId();

    /**
     * 批量保存数据
     * @param batch bean list
     */
    private void batchSave(List<Bean> batch) {
        log.debug("batch save data.. size:" + batch.size());
        try {
            ServDao.creates(getServId(), batch);
            // context.setSuccessfulNum(context.getSuccessfulNum() + batch.size());
        } catch (Exception e) {
            e.printStackTrace();
            // context.setFailedNum(context.getFailedNum() + batch.size());
        }
        batch.clear();
    }
}
