/**
 * 
 */
package com.rh.core.wfe.serv;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.WfProcessFactory;
import com.rh.core.wfe.condition.ITransitionCondition;
import com.rh.core.wfe.condition.PercentTransitionCondition;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.resource.GroupBean;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 子流程节点实例对象处理类
 * 
 * @author wanglong
 * 
 */
public class WfSubProcActHandler {

    private static Log log = LogFactory.getLog(WfSubProcActHandler.class);

    /** 子流程节点任务实例 */
    private WfAct subProcAct = null;

    /**
     * 子流程节点实例处理
     * @param subProcAct 子流程节点任务实例
     */
    public WfSubProcActHandler(WfAct subProcAct) {
        this.subProcAct = subProcAct;
    }

    /**
     * 子流程节点任务能否继续处理，使主流程向下流转。子流程节点任务能否继续处理，要看此时是否满足子流程节点任务的流转条件TransitionCondition。
     * @return 子流程节点实例能继续处理，返回true
     */
    @SuppressWarnings("unchecked")
    public boolean canHandle() {
        Bean nodeDef = subProcAct.getNodeDef();
        // 异步
        if (nodeDef.getInt("ASYNC") == Constant.YES_INT) {
            return true;
        }
        // 扩展流转条件  没有配置的话，使用完成百分比判断
        ITransitionCondition transitionCondition = new PercentTransitionCondition();
        String extCls = nodeDef.getStr("TRANSITION_CONDITION_CLASS");
        if (!extCls.isEmpty()) {
            try {
                Class<ITransitionCondition> clazz = (Class<ITransitionCondition>) Class.forName(extCls);
                transitionCondition = clazz.newInstance();
            } catch (Exception e) {
                log.error("Class not found:" + extCls);
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return transitionCondition.check(subProcAct);
    }
    
    
    /**
     * 创建并启动子流程运行实例
     * @param processActors 流程启动时，起草节点处理人
     * @return 启动流程之后的第一个节点实例
     */
    public List<WfAct> startSubProcess(List<GroupBean> processActors) {
        // 返回值
        List<WfAct> wfActList = new ArrayList<WfAct>();
        //处理类
        SubProcessPreparer subProcessPreparer = getSubProcessPreparer();
        // 配置项
        Bean nodeDef = subProcAct.getNodeDef();
        String subServId = nodeDef.getStr("SUB_SERVICE_ID");
        // 初始化子流程需要的数据
        List<Bean> dataBeanList = subProcessPreparer.prepareData(processActors);
        // 保存数据 启动流程
        for (int i = 0; i < dataBeanList.size(); i++) {
            Bean dataBean = dataBeanList.get(i);
            if (nodeDef.getInt("CREATE_DATA_FLAG") == Constant.YES_INT) {
                ParamBean param = new ParamBean().setServId(subServId).setAct(ServMgr.ACT_SAVE).setAddFlag(true);
                param.copyFrom(dataBean);
                Bean data = ServDao.create(subServId, dataBean);
                dataBean = data;
            }
            WfProcDef procDef = subProcessPreparer.getProcDef(subServId, dataBean);
            if (procDef == null) {
                log.info("没有找到子流程定义, servId = " + subServId + " dataId = " + dataBean.getId());
                throw new TipException("没有找到子流程定义");
            } else {
                WfAct startAct = WfProcessFactory.startProcess(procDef, dataBean, processActors.get(i), subProcAct);
                if (startAct != null) {
                    wfActList.add(startAct);
                } else {
                    throw new TipException("子流程启动失败");
                }
            }
        }

        return wfActList;
    }
    
    
    /**
     * @return  获取子流程实例结束扩展处理类　"START_CLASS"中配置的,默认使用SubProcessFinisher
     */
    public SubProcessPreparer getSubProcessPreparer() {
        Bean nodeDef = subProcAct.getNodeDef();
        SubProcessPreparer subProcessPreparer = null;
        if (!nodeDef.getStr("START_CLASS").isEmpty()) {
            Class<SubProcessPreparer> subProcessPreparerClazz = null;
            try {
                subProcessPreparer = (SubProcessPreparer) Lang.createObject(
                        SubProcessPreparer.class, nodeDef.getStr("START_CLASS"));
                
            } catch (Exception ex) {
                log.error("Class not found:" + subProcessPreparerClazz);
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
        
        if (subProcessPreparer == null) {
            subProcessPreparer = new DefaultSubProcessPreparer();
        }
        subProcessPreparer.setParentAct(subProcAct);
        
        return subProcessPreparer;
    }
    
    
    
    /**
     * @param wfProcess 子流程实例
     * @return  获取子流程实例结束扩展处理类　"FINISH_CLASS"中配置的。 没有配置的话，返回null
     */
    @SuppressWarnings("unchecked")
    public SubProcessFinisher getSubProcessFinisher(WfProcess wfProcess) {
        Bean nodeDef = subProcAct.getNodeDef();
        SubProcessFinisher subProcessFinisher = null;
        if (!nodeDef.getStr("FINISH_CLASS").isEmpty()) {
            Class<SubProcessFinisher> subProcessFinisherClazz = null;
            try {
                subProcessFinisherClazz = (Class<SubProcessFinisher>) Class.forName(nodeDef.getStr("FINISH_CLASS"));
                Constructor<SubProcessFinisher> constructor = subProcessFinisherClazz.getConstructor();
                subProcessFinisher = constructor.newInstance();
            } catch (Exception ex) {
                log.error("Class not found:" + subProcessFinisherClazz);
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
        
        return subProcessFinisher;
    }
    
    
    /**
     * 子流程取消办结后，该节点实例是否需要终止 <br>
     * 任务终止条件：1、主流程是运行状态  <br>
     * 2、该子流程节点任务之前是运行状态 <br>
     * 3、子流程节点任务没有后任务，即nextWfAct为空 <br>
     * 4、不能继续运行 ，即canHandle返回false <br>
     * @return 子流程取消办结后，该节点实例是否需要终止
     */
    public boolean needStop() {
        WfProcess process = subProcAct.getProcess();
        List<WfAct> nextWfActs = subProcAct.getNextWfAct();
        return process.isRunning() 
                && subProcAct.isRunning()
                && nextWfActs == null
                && !canHandle();
    }
    
    
    
    /**
     * 子流程办结后，该节点实例是否需要恢复运行。 <br>
     * 恢复条件：1、主流程是运行状态  <br>
     * 2、该子流程节点任务之前被终止了，即DONE_TYPE=2 <br>
     * 3、子流程节点任务没有后任务，即nextWfAct为空 <br>
     * 4、能继续运行 ，即canHandle返回true <br>
     * @return 子流程办结后，该节点实例是否可以继续运行
     */
    public boolean needResume() {
        WfProcess process = subProcAct.getProcess();
        List<WfAct> nextWfActs = subProcAct.getNextWfAct();
        return process.isRunning() 
                && subProcAct.getNodeInstBean().getInt("DONE_TYPE") == WfeConstant.NODE_DONE_TYPE_STOP
                && nextWfActs == null
                && canHandle();
    }
    
    

    /**
     * 获取以该节点为父节点的子流程中，已经办结的数量
     * @return 已经办结的子流程数量
     */
    public int getRunningSubProcessCount() {
        SqlBean sql = new SqlBean();
        sql.selects("COUNT(PI_ID) count");
        sql.and("INST_PARENT_NODE", this.subProcAct.getId());
        Bean countBean = ServDao.find(ServMgr.SY_WFE_PROC_INST, sql);
        return countBean.getInt("COUNT");
    }

    /**
     * 获取以该节点为父节点的子流程中，未办结的数量
     * @return 未办结的子流程数量
     */
    public int getFinishedSubProcessCount() {
        SqlBean sql = new SqlBean();
        sql.selects("COUNT(PI_ID) count");
        sql.and("INST_PARENT_NODE", this.subProcAct.getId());
        Bean countBean = ServDao.find(ServMgr.SY_WFE_PROC_INST_HIS, sql);
        return countBean.getInt("COUNT");
    }
    

    /**
     * @return the subProcAct
     */
    public WfAct getSubProcAct() {
        return subProcAct;
    }
    
    
    /**
     * @return 主子流程是否异步运行
     */
    public boolean isAsync() {
        Bean nodeDef = subProcAct.getNodeDef();
        return nodeDef.getInt("ASYNC") == Constant.YES_INT;
    }

}
