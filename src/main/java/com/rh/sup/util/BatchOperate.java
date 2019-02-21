package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.*;
import com.rh.core.serv.base.BaseServ;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.sup.serv.SuSnlxApplyServ;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作工具类
 * @author zhaosheng
 * @date 2018/11/24
 */
public class BatchOperate extends BaseServ{
    //批量处理司局长补登意见后删除代办，添加已办
    public static OutBean lotNTodoPass(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        ParamBean param=new ParamBean();
        String todoIds=paramBean.getStr("todoIds");
        SuSnlxApplyServ suSnlxApplyServ=new SuSnlxApplyServ();
        for(String todoId:todoIds.split(",")){
            param.set("todoId",todoId);
            suSnlxApplyServ.deleteTodoAndSaveTodoHis(param);
        }
        return outBean.setOk();
    }

    //批量处理通过待办
    public static OutBean lotTodoPass(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        List<String> noNextTodoList=new ArrayList<String>();
        String todoIds=paramBean.getStr("todoIds");
        String nextNode=paramBean.getStr("nextNode");
        for(String todoId:todoIds.split(",")){
            Bean todoBean= ServDao.find("SY_COMM_TODO",todoId);
            String niId = todoBean.getStr("TODO_OBJECT_ID2");
            //获取节点nodeInstBean
            Bean nodeInstBean= WfNodeInstDao.findNodeInstById(niId);
            paramBean.set("TO_USERS",paramBean.getStr("nextUser"));
            paramBean.set("PI_ID",nodeInstBean.getStr("PI_ID"));

            paramBean.set("NODE_CODE",nextNode);
            paramBean.set("NI_ID",nodeInstBean.getStr("NI_ID"));
            //流程是否正常使用 1正常，2已停止
            paramBean.set("INST_IF_RUNNING",nodeInstBean.get("INST_IF_RUNNING","1"));
            //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
            //paramBean.set("TO_TYPE",nodeInstBean.getStr("TO_TYPE"));
            paramBean.set("TO_TYPE","3");
            paramBean.set("serv","SY_WFE_PROC");
            paramBean.set("act","toNext");
            paramBean.set("_TRANS_",false);
            ServMgr.act("SY_WFE_PROC","toNext",paramBean);
        }
        outBean.setData(noNextTodoList);
        return outBean;
    }
    //批量处理不通过/退回待办
    public static OutBean lotTodoNoPass(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        List<String> noNextTodoList=new ArrayList<String>();
        String todoIds=paramBean.getStr("todoIds");
        String servId=paramBean.getStr("servId");
        for(String todoId:todoIds.split(",")){
            Bean todoBean=ServDao.find(servId,todoId);
            String todoUrl=todoBean.getStr("TODO_URL");
            String niId = todoBean.getStr("TODO_OBJECT_ID2");
            //获取节点nodeInstBean
            Bean nodeInstBean=WfNodeInstDao.findNodeInstById(niId);
            paramBean.set("PI_ID",nodeInstBean.getStr("PI_ID"));
            //获取上个流程实例
            String preId=nodeInstBean.getStr("PRE_NI_ID");
            Bean preNodeInstBean=WfNodeInstDao.findNodeInstById(preId);
            paramBean.set("TO_USERS",preNodeInstBean.getStr("DONE_USER_ID"));
            //获取上个节点
            String nowNode=nodeInstBean.getStr("NODE_CODE");
            List<Bean> lineBeanList=ServDao.finds("SY_WFE_LINE_DEF"," and PROC_CODE like 'SUP_APPRO_BUREAU%' and TAR_NODE_CODE='"+nowNode+"'");
            // TODO: 2018/11/15 用户选取一个节点来提交
            /*if(lineBeanList.size()>1){
                noNextTodoList.add(todoBean.getStr("TODO_TITLE"));
                continue;
            }*/
            String nextNode=lineBeanList.get(0).getStr("SRC_NODE_CODE");
            paramBean.set("NODE_CODE",nextNode);
            paramBean.set("NI_ID",nodeInstBean.getStr("NI_ID"));
            //流程是否正常使用 1正常，2已停止
            paramBean.set("INST_IF_RUNNING",nodeInstBean.get("INST_IF_RUNNING","1"));
            //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
            paramBean.set("TO_TYPE",3);
            paramBean.set("serv","SY_WFE_PROC");
            paramBean.set("act","toNext");
            paramBean.set("_TRANS_",false);
            ServMgr.act("SY_WFE_PROC","toNext",paramBean);
        }
        outBean.setData(noNextTodoList);
        return outBean;
    }

    /**
     * 批量处理通过立项
     * @param paramBean approId,servId
     * @return
     */
    public static OutBean lotApproPass(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        List<String> noNextTodoList = new ArrayList<String>();
        String approIds=paramBean.getStr("approIds");
        String servId=paramBean.getStr("servId");
        UserBean userBean= Context.getUserBean();
        String userCode=userBean.getCode();
        for (String approId:approIds.split(",")) {
            List<Bean> todoList = getOwerApproTodo(userCode, approId);
            for (Bean todo : todoList) {
                String todoId=todo.getStr("TODO_ID");
                Bean todoBean = ServDao.find(servId, todoId);
                String todoUrl = todoBean.getStr("TODO_URL");
                String niId = todoBean.getStr("TODO_OBJECT_ID2");
                //获取节点nodeInstBean
                Bean nodeInstBean = WfNodeInstDao.findNodeInstById(niId);
                paramBean.set("TO_USERS", nodeInstBean.getStr("TO_USER_ID"));
                paramBean.set("PI_ID", nodeInstBean.getStr("PI_ID"));

                //获取下个节点
                String nowNode = nodeInstBean.getStr("NODE_CODE");
                List<Bean> lineBeanList = ServDao.finds("SY_WFE_LINE_DEF", " and PROC_CODE like 'SUP_APPRO_BUREAU%' and SRC_NODE_CODE='" + nowNode + "'");
                // TODO: 2018/11/15 用户选取一个节点来提交
                /*if(lineBeanList.size()>1){
                    noNextTodoList.add(todoBean.getStr("TODO_TITLE"));
                    continue;
                }*/
                String nextNode = lineBeanList.get(0).getStr("TAR_NODE_CODE");
                paramBean.set("NODE_CODE", nextNode);
                paramBean.set("NI_ID", nodeInstBean.getStr("NI_ID"));
                //流程是否正常使用 1正常，2已停止
                paramBean.set("INST_IF_RUNNING", nodeInstBean.get("INST_IF_RUNNING", "1"));
                //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
                paramBean.set("TO_TYPE", nodeInstBean.getStr("TO_TYPE"));
                paramBean.set("serv", "SY_WFE_PROC");
                paramBean.set("act", "toNext");
                paramBean.set("_TRANS_", false);
                ServMgr.act("SY_WFE_PROC", "toNext", paramBean);
            }
        }
        outBean.setData(noNextTodoList);
        return outBean;
    }

    /**
     * 获取当前办理人的待办
     * @param owner,approId
     * @return
     */
    public static List<Bean> getOwerApproTodo(String owner,String approId){
        List<Bean> beanList=new ArrayList<Bean>();
        beanList=ServDao.finds("SY_COMM_TODO"," and OWNER_CODE='"+owner+"' and TODO_OBJECT_ID1='"+approId+"'");
        return beanList;
    }
    //批量处理不通过/退回立项
    public static OutBean lotApproNoPass(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        List<String> noNextTodoList=new ArrayList<String>();
        String approIds=paramBean.getStr("approIds");
        String servId=paramBean.getStr("servId");
        UserBean userBean= Context.getUserBean();
        String userCode=userBean.getCode();
        for (String approId:approIds.split(",")) {
            List<Bean> todoList = getOwerApproTodo(userCode, approId);
            for (Bean todo : todoList) {
                String todoId = todo.getStr("TODO_ID");
                Bean todoBean = ServDao.find(servId, todoId);
                String todoUrl = todoBean.getStr("TODO_URL");
                String niId = todoBean.getStr("TODO_OBJECT_ID2");
                //获取节点nodeInstBean
                Bean nodeInstBean = WfNodeInstDao.findNodeInstById(niId);
                paramBean.set("PI_ID", nodeInstBean.getStr("PI_ID"));
                //获取上个流程实例
                String preId = nodeInstBean.getStr("PRE_NI_ID");
                Bean preNodeInstBean = WfNodeInstDao.findNodeInstById(preId);
                paramBean.set("TO_USERS", preNodeInstBean.getStr("DONE_USER_ID"));
                //获取上个节点
                String nowNode = nodeInstBean.getStr("NODE_CODE");
                List<Bean> lineBeanList = ServDao.finds("SY_WFE_LINE_DEF", " and PROC_CODE like 'SUP_APPRO_BUREAU%' and TAR_NODE_CODE='" + nowNode + "'");
                // TODO: 2018/11/15 用户选取一个节点来提交
                /*if(lineBeanList.size()>1){
                    noNextTodoList.add(todoBean.getStr("TODO_TITLE"));
                    continue;
                }*/
                String nextNode = lineBeanList.get(0).getStr("SRC_NODE_CODE");
                paramBean.set("NODE_CODE", nextNode);
                paramBean.set("NI_ID", nodeInstBean.getStr("NI_ID"));
                //流程是否正常使用 1正常，2已停止
                paramBean.set("INST_IF_RUNNING", nodeInstBean.get("INST_IF_RUNNING", "1"));
                //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
                paramBean.set("TO_TYPE", 3);
                paramBean.set("serv", "SY_WFE_PROC");
                paramBean.set("act", "toNext");
                paramBean.set("_TRANS_", false);
                ServMgr.act("SY_WFE_PROC", "toNext", paramBean);
            }
        }
        outBean.setData(noNextTodoList);
        return outBean;
    }

    /**
     * 当前待办是否属于同一个流程环节
     * @param paramBean approId,servId
     * @return
     */
    public static OutBean isSameTodoNode(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        List<Bean> nodeInstBeans=new ArrayList<Bean>();
        List<Bean> todoList=new ArrayList<Bean>();
        String todoIds=paramBean.getStr("todoIds");
        String currServId="";
        String currNodeCode="";
        for(String todoId:todoIds.split(",")){
            Bean todoBean=new Bean();
            Bean nodeInstBean=new Bean();
            try {
                todoBean = ServDao.find("SY_COMM_TODO", todoId);
                nodeInstBean = WfNodeInstDao.findNodeInstById(todoBean.getStr("TODO_OBJECT_ID2"));
                todoList.add(todoBean);
            }catch (Exception e){
                return outBean.setError("暂不支持的审批类型！");
            }
            nodeInstBeans.add(nodeInstBean);
            //校验是否可审批
            /*ParamBean beforeParam=new ParamBean();
            beforeParam.set("approId",todoBean.getStr("TODO_OBJECT_ID1"));
            beforeParam.set("currNodeCode",nodeInstBean.getStr("NODE_CODE"));
            beforeParam.set("nextNodeCode",nodeInstBean.getStr(""));
            beforeParam.set("nid",todoBean.getStr("TODO_OBJECT_ID2"));
            ServMgr.act(todoBean.getStr("SERV_ID"),"beforeSendToNode",beforeParam);*/
            if("".equals(currServId)){
                currServId=todoBean.getStr("SERV_ID");
                currNodeCode=nodeInstBean.getStr("NODE_CODE");
                outBean.set("servId",currServId);
                outBean.set("approId",todoBean.getStr("TODO_OBJECT_ID1"));
                outBean.set("currNodeCode",currNodeCode);
                continue;
            }
            if(currServId.equals(todoBean.getStr("SERV_ID")) && currNodeCode.equals(nodeInstBean.getStr("NODE_CODE"))){
                continue;
            }else{
                return outBean.setError("所选待办处于不同的办理阶段，不能同时审批！");
            }
        }
        outBean.set("todoList",todoList);
        outBean.set("nodeInstBeans",nodeInstBeans);
        return outBean;
    }
    /**
     * 获取下个节点信息
     * @param todoBean
     * @return nextNodeCodeList
     */
    public static List<Bean> getNextNode(Bean todoBean,Boolean tuiHui){
        List<Bean> nodeList=new ArrayList<Bean>();
        Bean nodeBean=new Bean();
        String servId=todoBean.getStr("SERV_ID");
        String niId = todoBean.getStr("TODO_OBJECT_ID2");
        //获取节点nodeInstBean
        Bean nodeInstBean = WfNodeInstDao.findNodeInstById(niId);
        //获取下个节点
        String nowNode = nodeInstBean.getStr("NODE_CODE");
        String nowNodeItem="SRC_NODE_CODE";
        String nextNodeItem="TAR_NODE_CODE";
        //是否为退回
        if(tuiHui){
            nowNodeItem="TAR_NODE_CODE";
            nextNodeItem="SRC_NODE_CODE";
        }
        List<Bean> lineBeanList = ServDao.finds("SY_WFE_LINE_DEF", " and PROC_CODE like '"+servId+"%' and "+nowNodeItem+"='" + nowNode + "'");
        for(Bean bean:lineBeanList){
            nodeBean = ServDao.finds("SY_WFE_NODE_DEF", " and PROC_CODE like '"+servId+"%' and NODE_CODE='" +  bean.getStr(nextNodeItem) + "'").get(0);
            ParamBean requdata =new ParamBean();
            requdata.set("INST_IF_RUNNING","1");
            requdata.set("NI_ID",niId);
            requdata.set("NODE_CODE",nodeBean.getStr("NODE_CODE"));
            requdata.set("PI_ID",nodeInstBean.getStr("PI_ID"));
            OutBean nodeUserList=ServMgr.act("SY_WFE_PROC","getNextStepUsersForSelect",requdata);

            nodeList.add(nodeBean);
        }
        return nodeList;
    }
    /**
     * 当前督查事项是否属于同一个流程环节
     * @param paramBean approId,servId
     * @return
     */
    public static OutBean isSameApproNode(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        List<String> noNextTodoList = new ArrayList<String>();
        String approIds=paramBean.getStr("approIds");
        String servId=paramBean.getStr("servId");
        UserBean userBean= Context.getUserBean();
        String userCode=userBean.getCode();
        for (String approId:approIds.split(",")) {
            List<Bean> todoList = getOwerApproTodo(userCode, approId);
            for (Bean todo : todoList) {
                String todoId=todo.getStr("TODO_ID");
                Bean todoBean = ServDao.find(servId, todoId);
                String todoUrl = todoBean.getStr("TODO_URL");
                String niId = todoBean.getStr("TODO_OBJECT_ID2");
                //获取节点nodeInstBean
                Bean nodeInstBean = WfNodeInstDao.findNodeInstById(niId);
                paramBean.set("TO_USERS", nodeInstBean.getStr("TO_USER_ID"));
                paramBean.set("PI_ID", nodeInstBean.getStr("PI_ID"));

                //获取下个节点
                String nowNode = nodeInstBean.getStr("NODE_CODE");
                List<Bean> lineBeanList = ServDao.finds("SY_WFE_LINE_DEF", " and PROC_CODE like 'SUP_APPRO_BUREAU%' and SRC_NODE_CODE='" + nowNode + "'");
                // TODO: 2018/11/15 用户选取一个节点来提交
                /*if(lineBeanList.size()>1){
                    noNextTodoList.add(todoBean.getStr("TODO_TITLE"));
                    continue;
                }*/
                String nextNode = lineBeanList.get(0).getStr("TAR_NODE_CODE");
                paramBean.set("NODE_CODE", nextNode);
                paramBean.set("NI_ID", nodeInstBean.getStr("NI_ID"));
                //流程是否正常使用 1正常，2已停止
                paramBean.set("INST_IF_RUNNING", nodeInstBean.get("INST_IF_RUNNING", "1"));
                //发送指点人，送交类型：1：送部门+角色；2：送角色；3：送用户
                paramBean.set("TO_TYPE", nodeInstBean.getStr("TO_TYPE"));
                paramBean.set("serv", "SY_WFE_PROC");
                paramBean.set("act", "toNext");
                paramBean.set("_TRANS_", false);
                ServMgr.act("SY_WFE_PROC", "toNext", paramBean);
            }
        }
        outBean.setData(noNextTodoList);
        return outBean;
    }
}
