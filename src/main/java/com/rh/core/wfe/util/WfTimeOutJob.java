package com.rh.core.wfe.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.remind.RemindMgr;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfProcInstDao;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfProcDefManager;
import com.rh.core.wfe.remind.RemindServ;
import com.rh.core.wfe.remind.RemindUtils;
import com.rh.core.wfe.remind.WfeRemindItem;

/**
 * 节点超时处理
 * 
 * @author wanglong
 * 
 */
public class WfTimeOutJob extends RhJob {

    private Log log = LogFactory.getLog(WfTimeOutJob.class);

    @Override
    public void executeJob(RhJobContext context) {
        doWorks();
    }

    /**
	 * 
	 */
    public void doWorks() {
        Transaction.begin();

        try {
            StringBuilder baseWhere = getBaseWhere();

            List<Bean> procInstList = getProcList(baseWhere); // 查到需要催办的流程
            List<Bean> nodeInstList = getNodeInstList(baseWhere); // 查到需要催办的节点

            // 流程实例ID, 流程实例Bean
            Map<String, Bean> procInstMap = getProcInstMap(procInstList);

            Map<String, Bean> entiryMap = getEntityMap(procInstMap);

            for (Bean nodeInst : nodeInstList) {
                String procCode = nodeInst.getStr("PROC_CODE");

                WfProcDef procDef = WfProcDefManager.getWorkflowDef(procCode);

                if (procDef != null) {
                    String nodeCode = nodeInst.getStr("NODE_CODE");

                    try {
                        WfNodeDef nodeDef = procDef.findNode(nodeCode);

                        Bean procInst = procInstMap.get(nodeInst.getStr("PI_ID"));

                        Bean entity = entiryMap.get(nodeInst.getStr("PI_ID"));

                        doRemind(procInst, nodeDef, nodeInst, entity);

                        Transaction.commit();
                    } catch (RuntimeException e) {
                        log.debug("没找到节点定义", e);
                    }
                }
            }
        } finally {
            Transaction.end();
        }
    }

    /**
     * 
     * @param procInst 流程实例ID
     * @param nodeDef 节点定义
     * @param nodeInst 节点实例
     * @param entity 实体表
     */
    private void doRemind(Bean procInst, WfNodeDef nodeDef, Bean nodeInst, Bean entity) {
        if (null == entity) { // 没对应上实体表
            return;
        }

        int emercyValue = entity.getInt("S_EMERGENCY");

        Bean emergenBean = nodeDef.getEmerGency(emercyValue);

        // 节点上定义的超时时间
        if (null == emergenBean || emergenBean.isEmpty("TIMEOUT") || emergenBean.getInt("TIMEOUT") == 0) {
            return;
        }

        String userTodo = nodeInst.getStr("TO_USER_ID");

        String remindTypes = emergenBean.getStr("REMIND"); // 提醒
        addRemind(entity, remindTypes, userTodo);

        String operTypes = emergenBean.getStr("OPER"); // 操作
        String[] operTypeArray = operTypes.split(",");
        for (int i = 0; i < operTypeArray.length; i++) {
            if (operTypeArray[i].equals("CUIBAN")) { // 催办
                addCuiBan(entity, nodeInst);

            } else if (operTypeArray[i].equals("BACK")) { // 退回

            }
        }

        String remindLog = "{'REMIND':'" + remindTypes + "','OPER':'" + operTypes + "'}";
        updateNodeRemindState(nodeInst.getId(), remindLog);
    }

    /**
     * 
     * @param nid 节点实例ID
     * @param remindLog 记录已经提醒的串
     */
    private void updateNodeRemindState(String nid, String remindLog) {
        Bean nodeInst = new Bean(nid);
        nodeInst.set("REMIND_LOG", remindLog);
        ServDao.update(ServMgr.SY_WFE_NODE_INST, nodeInst);
    }

    /**
     * @param entity 实体表信息
     * @param nodeInst 节点实例
     */
    private void addCuiBan(Bean entity, Bean nodeInst) {
        String userTodo = nodeInst.getStr("TO_USER_ID");

        ParamBean paramBean = new ParamBean();
        UserBean userBean = UserMgr.getUser(userTodo);

        String cuiBanCode = Context.getSyConf("SY_WFE_REMIND_CODE", "催办");
        paramBean.set("REMD_CODE", cuiBanCode);
        paramBean.set("REMD_YEAR", DateUtils.getYear());

        Bean codeBean = RemindUtils.getMaxCode(RemindServ.SERV_ID, paramBean, WfeRemindItem.REMD_NUM);
        paramBean.set("REMD_NUM", codeBean.getStr(WfeRemindItem.REMD_NUM));

        paramBean.set("ACPT_USER", userTodo);
        paramBean.set("ACPT_DEPT", userBean.getDeptCode());
        paramBean.set("ACPT_TDEPT", userBean.getTDeptCode());
        paramBean.set("ACPT_PHONE", userBean.getMobile());
        paramBean.set("REMD_TITLE", "催办-" + entity.getStr("TITLE"));
        paramBean.set("REMD_REASON", "超时");
        paramBean.set("DEADLINE", nodeInst.getStr("NODE_LIMIT_TIME"));
        paramBean.set("S_EMERGENCY", entity.getInt("S_EMERGENCY"));
        paramBean.set("REMD_STATUS", RemindServ.STATE_NOSEND);
        paramBean.set("DATA_ID", entity.getStr("DATA_ID"));
        paramBean.set("SERV_ID", entity.getStr("SERV_ID"));
        paramBean.set("WF_NODE_ID", nodeInst.getStr("NI_ID"));

        String userLoginName = Context.getSyConf("SY_WFE_REMIND_USER", "admin");
        UserBean adminUser = UserMgr.getUser(userLoginName, entity.getStr("S_CMPY"));
        paramBean.set("S_USER", adminUser.getCode());

        paramBean.set(Constant.PARAM_SERV_ID, RemindServ.SERV_ID);
        paramBean.set(Constant.PARAM_ACT_CODE, "save");

        // 添加催办
        OutBean outBean = ServMgr.act(paramBean);

        // 发送
        paramBean.setId(outBean.getId());
        paramBean.set(Constant.PARAM_ACT_CODE, "sendTodo");
        ServMgr.act(paramBean);
    }

    /**
     * 
     * @param entity 实体表
     * @param remindType 提醒方式
     * @param userTodo 需要办理的用户
     */
    private void addRemind(Bean entity, String remindType, String userTodo) {
        Bean attBean = new Bean();
        attBean.set("TYPE", remindType);
        attBean.set("REM_CONTENT", entity.getStr("TITLE"));
        attBean.set("REM_TITLE", entity.getStr("TITLE"));
        attBean.set("SERV_ID", entity.getStr("SERV_ID"));
        attBean.set("DATA_ID", entity.getStr("DATA_ID"));

        attBean.set("EXECUTE_TIME", DateUtils.getDatetime());

        String urlStr = entity.getStr("SERV_ID") + ".byid.do?data={_PK_:" + entity.getStr("DATA_ID") + "}";
        attBean.set("REM_URL", urlStr);

        RemindMgr.add(attBean, userTodo);
    }

    /**
     * 
     * @param procInstMap 流程实例列表
     * @return 实体表信息
     */
    private Map<String, Bean> getEntityMap(Map<String, Bean> procInstMap) {
        StringBuilder pids = new StringBuilder();
        Iterator<String> it = procInstMap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();

            pids.append(key).append(",");
        }
        if (pids.length() > 0) {
            pids.setLength(pids.length() - 1);
        }

        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, " and S_WF_INST in ('" + pids.toString().replace(",", "','") + "')");
        List<Bean> entitys = ServDao.finds(ServMgr.SY_COMM_ENTITY, queryBean);

        Map<String, Bean> entityMap = new HashMap<String, Bean>();
        for (Bean entity : entitys) {
            entityMap.put(entity.getStr("S_WF_INST"), entity);
        }

        return entityMap;
    }

    /**
     * 
     * @param procInstList 流程实例列表
     * @return <流程ID, 流程实例Bean>
     */
    private Map<String, Bean> getProcInstMap(List<Bean> procInstList) {
        // 找到 有 值的 列表
        Map<String, Bean> procInstMap = new HashMap<String, Bean>();
        for (Bean procInst : procInstList) {
            procInstMap.put(procInst.getStr("PI_ID"), procInst);
        }
        return procInstMap;
    }

    /**
     * 
     * @param baseWhere 查询条件
     * @return 获取需要提醒的节点列表
     */
    private List<Bean> getNodeInstList(StringBuilder baseWhere) {
        Bean nodeQuery = new Bean();
        nodeQuery.set(Constant.PARAM_WHERE, baseWhere);
        List<Bean> nodeInstList = ServDao.finds(WfNodeInstDao.SY_WFE_NODE_INST_SERV, nodeQuery);
        return nodeInstList;
    }

    /**
     * 
     * @param baseWhere 查询条件
     * @return 获取需要提醒的流程列表
     */
    private List<Bean> getProcList(StringBuilder baseWhere) {
        Bean paramBean = new Bean();
        StringBuilder procWhere = new StringBuilder();
        procWhere.append(" and PI_ID in (");
        procWhere.append(" select PI_ID from SY_WFE_NODE_INST where 1 = 1");
        procWhere.append(baseWhere);
        procWhere.append(")");

        paramBean.set(Constant.PARAM_WHERE, procWhere.toString());

        List<Bean> procInstList = ServDao.finds(
                WfProcInstDao.SY_WFE_PROC_INST_SERV, paramBean);
        return procInstList;
    }

    /**
     * @return 查询需要提醒的语句
     */
    private StringBuilder getBaseWhere() {
        String dateStr = DateUtils.getDatetime();

        StringBuilder baseWhere = new StringBuilder();
        baseWhere.append(" and NODE_IF_RUNNING =").append(WfeConstant.NODE_IS_RUNNING); // 运行状态的节点
        // baseWhere.append(" and NODE_ETIME is null"); // 结束时间还没有
        // baseWhere.append(" and NODE_LIMIT_TIME is not null"); //限定时间不为空
        baseWhere.append(" and NODE_LIMIT_TIME < '").append(dateStr).append("'"); // 当前时间大于限定的时间
        baseWhere.append(" and REMIND_LOG is null"); // 没被提醒过

        // baseWhere.append(" and NI_ID = '2F_P-mmjt1vXbQp59NMtVe'"); //临时测试添加条件
        return baseWhere;
    }

    @Override
    public void interrupt() {
        // Auto-generated method stub
    }
}
