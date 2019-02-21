package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.AbstractLineEvent;


/**
 *  要点类 督查处审批办结 扩展类
 *  送交督查处审批办结的同时  送交机构负责人审批办结 （阅办）
 */
public class PointKnotLineEvent extends AbstractLineEvent {

    // 机构负责人角色code
    private final String ROLE_FZR = "SUP024";
    // 插入待办的当前节点
    private final String OPERATION = "机构负责人审批办结";

    @Override
    public void forward(WfAct wfAct, WfAct wfAct1, Bean bean) {

        WfProcess process = wfAct.getProcess();
        Bean nodeBean = wfAct.getNodeInstBean();
        Bean servBean = process.getServInstBean();
        String title = servBean.getStr("TITLE");
        SqlBean todoSqlBean = new SqlBean();
        todoSqlBean.and("TDEPT_CODE", servBean.getStr("S_TDEPT"));
        todoSqlBean.and("ROLE_CODE", ROLE_FZR);
        Bean userbean = ServDao.find("SY_ORG_ROLE_USER", todoSqlBean);
        String userCode = userbean.getStr("USER_CODE");
        try {
            //给省厅负责人插入一条代办
            TodoServ serv = new TodoServ();
            TodoBean dataBean = new TodoBean();
            dataBean.setTitle("要点类督查_"+title);
            dataBean.setDataCode(servBean.getStr("S_CODE"));
            dataBean.setOperation(OPERATION);
            dataBean.setSender(process.getSUserId());
            dataBean.setCodeName("要点类立项申请单");
            dataBean.setEmergency(1);
            dataBean.setOwner(userCode);
            dataBean.setCode(process.getServId());
            dataBean.setObjectId1(servBean.getId());
            dataBean.setBenchFlag(1);
            dataBean.setCatalog(1);
            dataBean.setServId(process.getServId());
            dataBean.setFrom("wf");
            dataBean.setUrl("OA_SUP_APPRO_POINT.byid.do?data={_PK_:"+servBean.getId()+",NI_ID:"+nodeBean.getId()+"}");
            serv.addToDo(new ParamBean(dataBean));
        } catch (Exception e) {
            throw new RuntimeException("插入待办异常");
        }
    }

    @Override
    public void backward(WfAct wfAct, WfAct wfAct1, Bean bean) {

    }
}
