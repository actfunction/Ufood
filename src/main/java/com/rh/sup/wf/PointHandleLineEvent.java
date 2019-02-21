package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.AbstractLineEvent;
import com.rh.sup.serv.SupApproGain;

/**
 * 要点类 督查处审批办理情况 扩展类
 * 送交督查处审批办理情况的同时  送交机构负责人审批 （阅办）
 * 修改办理情况的状态
 */
public class PointHandleLineEvent extends AbstractLineEvent {

    // 机构负责人角色code
    private final String ROLE_FZR = "SUP024";
    // 插入待办的当前节点
    private final String OPERATION = "机构负责人审批办理";
    //办理状态 办理中 1
    private final String GAIN_ING_STATE = "1";
    //办理状态 待审核 2
    private final String GAIN_CHECK_STATE = "2";

    @Override
    public void forward(WfAct wfAct, WfAct wfAct1, Bean bean) {

        WfProcess process = wfAct.getProcess();
        Bean nodeBean = wfAct.getNodeInstBean(); // 获取节点实例
        Bean servBean = process.getServInstBean(); // 获取主单实例
        String approId = servBean.getId();// 获取主单id
        String title = servBean.getStr("TITLE");
        // 修改办理状态  将办理中修改为待审核状态
        SupApproGain gain = new SupApproGain();
        ParamBean paramBean = new ParamBean();
        paramBean.set("approId",approId);
        paramBean.set("curState",GAIN_ING_STATE);
        paramBean.set("upState",GAIN_CHECK_STATE);
        gain.updateWfState(paramBean);
        gain.updateHostGainUpdate(paramBean);//更新主办单位更新时间

//        // 查找省厅负责人
//        SqlBean todoSqlBean = new SqlBean();
//        todoSqlBean.and("TDEPT_CODE", servBean.getStr("S_TDEPT"));
//        todoSqlBean.and("ROLE_CODE", ROLE_FZR);
//        Bean userbean = ServDao.find("SY_ORG_ROLE_USER", todoSqlBean);
//        String userCode = userbean.getStr("USER_CODE");
//        try {
//            //给省厅负责人插入一条代办
//            TodoServ serv = new TodoServ();
//            TodoBean dataBean = new TodoBean();
//            dataBean.setTitle("要点类督查_"+title);
//            dataBean.setDataCode(servBean.getStr("S_CODE"));
//            dataBean.setOperation(OPERATION);
//            dataBean.setSender(process.getSUserId());
//            dataBean.setCodeName("要点类立项申请单");
//            dataBean.setEmergency(1);
//            dataBean.setOwner(userCode);
//            dataBean.setCode(process.getServId());
//            dataBean.setObjectId1(servBean.getId());
//            dataBean.setBenchFlag(1);
//            dataBean.setCatalog(1);
//            dataBean.setServId(process.getServId());
//            dataBean.setFrom("wf");
//            dataBean.setUrl("OA_SUP_APPRO_POINT.byid.do?data={_PK_:" + servBean.getId() + ",NI_ID:" + nodeBean.getId() + "}");
//            serv.addToDo(new ParamBean(dataBean));
//        } catch (Exception e) {
//            throw new RuntimeException("插入待办异常");
//        }
    }

    @Override
    public void backward(WfAct wfAct, WfAct wfAct1, Bean bean) {
        // 修改办理状态
        WfProcess process = wfAct.getProcess();
        Bean servBean = process.getServInstBean();
        String approId = servBean.getId();
        //  将待审核修改为办理中状态
        SupApproGain gain = new SupApproGain();
        ParamBean paramBean = new ParamBean();
        paramBean.set("approId",approId);
        paramBean.set("curState",GAIN_CHECK_STATE);
        paramBean.set("upState",GAIN_ING_STATE);
        gain.updateWfState(paramBean);
    }


}
