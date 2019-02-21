/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.comm.schedule.serv;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import com.rh.core.base.Bean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.scheduler.RhJobDetail;

/**
 * 本地任务执行历史 服务类
 * @author wanglong
 */
public class SchedLocalHistoryServ extends SchedBaseServ {

    /** 任务执行日志 */
    private static Hashtable<String, LinkedBlockingDeque<Bean>> executeLogStorageTable =
            new Hashtable<String, LinkedBlockingDeque<Bean>>();

    /**
     * @see com.rh.core.serv.CommonServ#query(com.rh.core.base.Bean)
     * @param param bean
     * @return result bean
     */
    public OutBean query(ParamBean param) {
        String queryJob = param.getStr("JOB_NAME");

        OutBean outBean = new OutBean();
        List<Bean> dataList = new ArrayList<Bean>();
        if (0 == queryJob.length()) {
            Set<String> allJob = executeLogStorageTable.keySet();
            for (String job : allJob) {
                LinkedBlockingDeque<Bean> logStorage = getJobHisStorage(job);
                addToDataList(logStorage, dataList);
            }
        } else {
            LinkedBlockingDeque<Bean> logStorage = getJobHisStorage(queryJob);
            addToDataList(logStorage, dataList);
        }

        final ServDefBean serv = ServUtils.getServDef(ServMgr.SY_COMM_LOCAL_SCHED_HIS);
        final LinkedHashMap<String, Bean> cols = new LinkedHashMap<String, Bean>();
        LinkedHashMap<String, Bean> items = serv.getAllItems();
        boolean bKey = true;
        for (String key : items.keySet()) {
            Bean item = items.get(key);
            int listFlag = item.getInt("ITEM_LIST_FLAG");
            if (bKey && item.getStr("ITEM_CODE").equals(serv.getPKey())) { // 主键无论是否列表显示都输出
                if (listFlag == ServConstant.ITEM_LIST_FLAG_HIDDEN) { // 如果定义为隐藏有数据，则提供给前端时设为不显示
                    listFlag = ServConstant.ITEM_LIST_FLAG_NO;
                }
                addCols(cols, item, listFlag);
                bKey = false;
            } else if (listFlag != ServConstant.ITEM_LIST_FLAG_NO) {
                if (item.getInt("ITEM_TYPE") == ServConstant.ITEM_TYPE_TABLE
                        || item.getInt("ITEM_TYPE") == ServConstant.ITEM_TYPE_VIEW) {
                }
                if (listFlag == ServConstant.ITEM_LIST_FLAG_HIDDEN) { // 如果定义为隐藏有数据，则提供给前端时设为不显示
                    listFlag = ServConstant.ITEM_LIST_FLAG_NO;
                }
                addCols(cols, item, listFlag);
            }

        } // end for

        outBean.setData(dataList);
        outBean.setCount(dataList.size());
        outBean.setPage(1);
        outBean.setCols(cols);
        return outBean;
    }

    /**
     * 添加一个任务
     * @param param 参数bean，包含job信息
     * @return 添加结果
     */
    public OutBean save(ParamBean param) {
        String queryJob = param.getStr("JOB_NAME");
        LinkedBlockingDeque<Bean> logStorage = getJobHisStorage(queryJob);
        int maxSize = 100;
        if (logStorage.size() >= maxSize) {
            logStorage.removeFirst();
        }
        logStorage.addLast(param);
        return new OutBean().setOk();
    }

    /**
     * 删除指定job
     * @param param 参数Bean， 包含JobName
     * @return 删除结果
     */
    public OutBean delete(ParamBean param) {
        return new OutBean().setError(" not implemented yet");
    }

    /**
     * 查询指定job
     * @param param 参数Bean， 包含JobName
     * @return 查询结果
     */
    public OutBean byid(ParamBean param) {
        OutBean outBean = new OutBean();
        outBean.set(RhJobDetail.JOB_PK, param.getId());
        return outBean;
    }

    /**
     * 获取指定任务的执行日志
     * @param job - 任务代码
     * @return 执行日志
     */
    private LinkedBlockingDeque<Bean> getJobHisStorage(String job) {
        if (!executeLogStorageTable.containsKey(job)) {
            executeLogStorageTable.put(job, new LinkedBlockingDeque<Bean>());
        }
        return executeLogStorageTable.get(job);
    }

    /**
     * add to dataList
     * @param logStorage - log storage
     * @param dataList - datalist
     */
    private void addToDataList(LinkedBlockingDeque<Bean> logStorage, List<Bean> dataList) {
        for (Object b : logStorage.toArray()) {
            Bean bean = (Bean) b;
            String status = bean.getStr("STATUS");
            String statusDisplay = DictMgr.getFullName("SY_COMM_SCHED_JOB_RESULT", status);
            bean.set("STATUS__NAME", statusDisplay);
            dataList.add(bean);
        }
    }
}
