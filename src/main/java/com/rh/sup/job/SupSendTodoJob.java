package com.rh.sup.job;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;
import com.rh.core.util.msg.MsgCenter;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.util.scheduler.RhLocalJob;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfContext;
import com.rh.core.wfe.WfParam;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.attention.AttentionMsg;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.resource.GroupBean;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author kfzx-zhaosheng
 */
public class SupSendTodoJob extends RhLocalJob {
    //获取当前时间2018-11-11格式
    String date= DateUtils.getDate();

    private static Logger log= Logger.getLogger(SupSendTodoJob.class);
    @Override
    public void executeJob(RhJobContext context) {
        try {
            startJob();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void interrupt(){};

    //业务实现
    public void startJob(){
        String dateOfMonth=date.substring(date.length()-2);
        String monthDayNow=date.substring(date.length()-5);
        log.error("------------------------督查定时推送办理情况任务--------------------SupSendTodoJob.java"+new Date().toString());
        //查询持续性
        List<Bean> listBean=getWaitTodo("1",dateOfMonth);
        if(listBean.size()>0){
            log.info("开始推送持续性工作---------------------`------");
            //推送持续性工作
            int count=sendGainTodo(listBean);
            log.info("成功推送"+count+"条持续性工作-------------------------------");
        }else {
            log.info("本次没有需要推送的持续性工作--------------------------");
        }

        //查询非持续性
        listBean=getWaitTodo("2",monthDayNow);
        if(listBean.size()>0){
            log.info("开始推送非持续性工作---------------------------");
            //推送非持续性工作
            int count=sendGainTodo(listBean);
            log.info("成功推送"+count+"条非持续性工作-------------------------------");
        }else{
            log.info("本次没有需要推送的非持续性工作--------------------------");
        }
    }
    //发送待办
    public int sendGainTodo(List<Bean> nextGain){
        //创建待办
        int count=0;
        for(Bean gain:nextGain){
            String niId=gain.getStr("NI_ID");
            Bean nextNodeInst= WfNodeInstDao.findNodeInstById(niId);
            WfAct nextWfAct = new WfAct(nextNodeInst.getId(),true);
            try{
                nextWfAct.sendTodo(gain.getStr("OWNER_CODE"));
                log.error("督查待办已推送给"+gain.getStr("OWNER_CODE"));
                ServDao.delete("OA_SUP_NEXT_GAIN_PUSH",gain.getId());
            }catch (Exception e){
                log.error("推送督查办理失败！请确认用户是否获取到，或是否存在，用户编码："+gain.getStr("OWNER_CODE"));
            }
            count++;
        }
        return count;
    }
    //获取待推送的待办数据
    public List<Bean> getWaitTodo(String handleType,String dateStr){
        List<Bean> waitTodoList=new ArrayList<Bean>();
        //司内督查
        List<Bean> bureauListBean= getWaitTodo("SUP_APPRO_BUREAU",handleType,dateStr);
        waitTodoList.addAll(bureauListBean);
        //署发督查
        List<Bean> officeListBean= getWaitTodo("SUP_APPRO_OFFICE",handleType,dateStr);
        waitTodoList.addAll(officeListBean);
        //要点类督查
        List<Bean> pointListBean= getWaitTodo("SUP_APPRO_POINT",handleType,dateStr);
        waitTodoList.addAll(pointListBean);
        return waitTodoList;
    }
    public List<Bean> getWaitTodo(String table,String handleType,String dateStr){
        //获取当月
        String monthOfYear=date.substring(0,7);
        List<Bean> waitTodoList=new ArrayList<Bean>();
        String sql="select push.* from OA_SUP_NEXT_GAIN_PUSH push left join "+table+" appro on push.APPRO_ID=appro.ID " +
                "left join SUP_SERV_DICT dict on appro.HANDLE_TYPE=dict.ID  " +
                "where dict.EXTEND1 ='"+handleType+"'";
        //完成时限最后一个月01号发送办理情况
        if("01".equals(date.substring(date.length()-2))){
            sql+=" and (appro.NEXT_GAIN_DATE like '%"+dateStr+"%' or appro.DATE_LIMIT like '"+monthOfYear+"%')";
        }else{
            sql+=" and appro.NEXT_GAIN_DATE like '%"+dateStr+"%'";
        }
        log.error("++++查询待推送待办+++++"+sql+"+++++++++++");
        waitTodoList=Transaction.getExecutor().query(sql);
        return waitTodoList;
    }
    //业务实现
    /*public void startJob(){
        //获取当前时间
        String date= DateUtils.getDate();
        String dateOfMonth=date.substring(date.length()-2);
        String monthDayNow=date.substring(date.length()-5);
        log.error("------------------------督查定时推送办理情况任务--------------------SupSendTodoJob.java"+new Date().toString());
        //查询持续性
        List<Bean> listBean=getNodeInstList(dateOfMonth,"1");
        if(listBean.size()>0){
            log.info("开始推送持续性工作---------------------`------");
            //推送持续性工作
            autoSend(listBean);
            log.info("成功推送持续性工作-------------------------------");
        }else {
            log.info("本次没有需要推送的持续性工作--------------------------");
        }

        //查询非持续性
        listBean=getNodeInstList(monthDayNow,"2");
        if(listBean.size()>0){
            log.info("开始推送非持续性工作---------------------------");
            //推送非持续性工作
            autoSend(listBean);
            log.info("成功推送所有非持续性工作-------------------------------");
        }else{
            log.info("本次没有需要推送的非持续性工作--------------------------");
        }
    }*/

    //从虚拟节点自动送交到督查办理节点
    protected void autoSend(List<Bean> bureauListBean){
        ParamBean paramBean=new ParamBean();
        //循环推送待办
        for(Bean bean:bureauListBean){
            //下节点人
            paramBean.set("TO_USERS",bean.getStr("TO_USER_ID"));
            paramBean.set("PI_ID",bean.getStr("PI_ID"));
            paramBean.set("NODE_CODE",bean.getStr("NODE_CODE"));
            paramBean.set("NI_ID",bean.getStr("NI_ID"));
            toNext(paramBean);
        }
    }

    /**
     *
     * @param dateStr 下次发送日期
     * @param handleType 督查事项类型
     * @return
     */
    private List<Bean> getNodeInstList(String dateStr,String handleType){
        List<Bean> listBean=new ArrayList<Bean>();
        //司内督查
        List<Bean> bureauListBean= getNodeInstList("SUP_APPRO_BUREAU","N34","N214",handleType,dateStr);
        listBean.addAll(bureauListBean);
        //署发督查
        List<Bean> officeListBean= getNodeInstList("SUP_APPRO_OFFICE","N32","N45",handleType,dateStr);
        listBean.addAll(officeListBean);
        //要点类督查
        List<Bean> pointListBean= getNodeInstList("SUP_APPRO_POINT","N36","N3",handleType,dateStr);
        listBean.addAll(pointListBean);
        return listBean;
    }

    /**
     *
     * @param approTable 服务名
     * @param nowNode 当前节点
     * @param nextNode 下个节点
     * @param handleType 立项类型
     * @param dateStr 下次发送时间
     * @return 需要自动转到下次办理情况
     */
    private List<Bean> getNodeInstList(String approTable,String nowNode,String nextNode,String handleType,String dateStr){
        String sql ="select node.*,'"+nextNode+"' as NODE_CODE from sy_wfe_node_inst node left join sy_wfe_proc_inst proc on node.PI_ID=proc.PI_ID " +
                " left join "+approTable+" appro on proc.DOC_ID=appro.ID" +
                " left join SUP_SERV_DICT dict on appro.HANDLE_TYPE=dict.ID " +
                " where node.NODE_CODE='"+nowNode+"' and node.PROC_CODE like '%"+approTable+"%' " +
                " and node.NODE_IF_RUNNING='1' and appro.NEXT_GAIN_DATE='%"+dateStr+"'";
        log.error("++++++++++++"+sql+"+++++++++++++");
        return  Transaction.getExecutor().query(sql);
    }
    /**
     * 送交下一节点
     * @param paramBean 参数
     * @return 节点实例ID
     */
    public OutBean toNext(ParamBean paramBean) {
        OutBean var6;
        //流程是否正常使用 1正常，2已停止
        paramBean.set("INST_IF_RUNNING",1);
        //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
        paramBean.set("TO_TYPE",3);
        //送交人(机器人，暂定admin)
        paramBean.set("_AGENT_USER_","admin");
        paramBean.set("serv","SY_WFE_PROC");
        paramBean.set("act","toNext");
        paramBean.set("_TRANS_",false);
        try {
            WfProcess process = new WfProcess(paramBean.getStr("PI_ID"), paramBean.getBoolean("INST_IF_RUNNING"));
            WfParam wfParam = new WfParam();
            wfParam.copyFrom(paramBean);
            int typeTo = paramBean.getInt("TO_TYPE");
            wfParam.setTypeTo(typeTo);
            wfParam.setDoneUser(getDoUserBean1(paramBean));
            wfParam.setToUser(paramBean.getStr("TO_USERS"));
            /*wfParam.setToDept(paramBean.getStr("TO_DEPT"));
            wfParam.setToRole(paramBean.getStr("TO_ROLE"));*/
            if (typeTo != 3 && typeTo != 1) {
                throw new TipException("需要设置送交类型");
            }
            /*if (typeTo == 3) {*/
            if (paramBean.getStr("TO_USERS").isEmpty()) {
                throw new TipException("需要设置送交用户");
            }
            /*} else if (typeTo == 1 && (paramBean.getStr("TO_DEPT").isEmpty() || paramBean.getStr("TO_ROLE").isEmpty())) {
                throw new TipException("需要设置送交部门、角色");
            }*/

            wfParam.set("TO_USERS", process.getNextActors(wfParam));
            log.info("获取下个节点人成功--------------------------");
            addMsg(paramBean.getStr("PI_ID"), process.getNextActors(wfParam), paramBean.getStr("NODE_CODE"), process.getProcInstTitle());
            process.toNext(wfParam);
            var6 = (new OutBean()).setOk();
            log.info("成功推送一条待办-------------------------------");
        } finally {
            destoryWfContext();
        }

        return var6;
    }


    /**
     *
     * just test
     *
     * 测试自动处理待办*//*
    public OutBean testContinueSend(ParamBean paramBean1){
        ParamBean paramBean=new ParamBean();
        SqlBean sqlBean=new SqlBean();
        //
        sqlBean.appendWhere(" and NODE_CODE=? and PROC_CODE=? and NODE_IF_RUNNING=?","N2","SUP_APPRO_BUREAU@ruaho@1",1);
        List<Bean> listBean= ServDao.finds(ServMgr.SY_WFE_NODE_INST,sqlBean);

        for(Bean bean:listBean){
            //下节点人
            paramBean.set("TO_USERS","2PKtpLyIh56H8YUmdcQsdf");
            //流程是否正常使用 1正常，2已停止
            paramBean.set("INST_IF_RUNNING",1);
            //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
            paramBean.set("TO_TYPE",3);
            //送交人admin
            paramBean.set("_AGENT_USER_","0LfMBfIdhaSb38hiPRLfr7");
            paramBean.set("PI_ID",bean.getStr("PI_ID"));
            paramBean.set("NODE_CODE","N1");
            paramBean.set("NI_ID",bean.getStr("NI_ID"));
            paramBean.set("serv","SY_WFE_PROC");
            paramBean.set("act","toNext");
            paramBean.set("_TRANS_",false);
            textToNext(paramBean);
        }
        return new OutBean().setOk();
    }
      /**
     * 送交下一节点
     * @param paramBean 参数
     * @return 节点实例ID
     *//*
    public static OutBean textToNext(ParamBean paramBean) {
        OutBean var6;
        try {
            WfProcess process = new WfProcess(paramBean.getStr("PI_ID"), paramBean.getBoolean("INST_IF_RUNNING"));
            WfParam wfParam = new WfParam();
            wfParam.copyFrom(paramBean);
            int typeTo = paramBean.getInt("TO_TYPE");
            wfParam.setTypeTo(typeTo);
            wfParam.setDoneUser(getDoUserBean1(paramBean));
            wfParam.setToUser(paramBean.getStr("TO_USERS"));
            *//*wfParam.setToDept(paramBean.getStr("TO_DEPT"));
            wfParam.setToRole(paramBean.getStr("TO_ROLE"));*//*
            if (typeTo != 3 && typeTo != 1) {
                throw new TipException("需要设置送交类型");
            }

            *//*if (typeTo == 3) {*//*
                if (paramBean.getStr("TO_USERS").isEmpty()) {
                    throw new TipException("需要设置送交用户");
                }
            *//*} else if (typeTo == 1 && (paramBean.getStr("TO_DEPT").isEmpty() || paramBean.getStr("TO_ROLE").isEmpty())) {
                throw new TipException("需要设置送交部门、角色");
            }*//*

            wfParam.set("TO_USERS", process.getNextActors(wfParam));//// TODO: 2018/11/10 未获取到下一节点处理人
            addMsg(paramBean.getStr("PI_ID"), process.getNextActors(wfParam), paramBean.getStr("NODE_CODE"), process.getProcInstTitle());
            process.toNext(wfParam);
            var6 = (new OutBean()).setOk();
        } finally {
            destoryWfContext();
        }

        return var6;
    }*/
    public UserBean getDoUserBean1(ParamBean paramBean) {
        if (!paramBean.isEmpty("_AGENT_USER_")) {
            String realUserId = paramBean.getStr("_AGENT_USER_");
            UserBean doUserBean = UserMgr.getUser(realUserId);
            return doUserBean;
        } else {
            return Context.getUserBean();
        }
    }
    private void addMsg(String pid, List<GroupBean> list, String nodeCode, String title) {
        StringBuilder userIds = new StringBuilder();
        Iterator var7 = list.iterator();

        while(var7.hasNext()) {
            GroupBean groupBean = (GroupBean)var7.next();
            Set<String> userIdSet = groupBean.getUserIds();
            Iterator var10 = userIdSet.iterator();

            while(var10.hasNext()) {
                String userId = (String)var10.next();
                userIds.append(userId).append(",");
            }
        }

        if (userIds.length() > 0) {
            userIds.setLength(userIds.length() - 1);
        }

        Bean msgBean = new Bean();
        msgBean.set("PI_ID", pid);
        msgBean.set("TO_USERS", userIds.toString());
        msgBean.set("NEXT_NODE", nodeCode);
        msgBean.set("TITLE", title);
        AttentionMsg attentionMsg = new AttentionMsg(msgBean);
        MsgCenter.getInstance().addMsg(attentionMsg);
    }
    private void destoryWfContext() {
        WfContext.cleanThreadData();
    }
}
