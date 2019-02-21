package com.rh.sup.query;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.wfe.db.WfNodeInstDao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kfzx-zhaosheng
 *
 * 督查管理查询父服务扩展类
 */
public class SupApproQuery extends CommonServ {
    public OutBean getTodoList(ParamBean paramBean){
        OutBean outBean=new OutBean();
        List<String> operSups=new ArrayList<String>();
        List<String> noOperSups=new ArrayList<String>();
        String approIds=paramBean.getStr("approIds");
        String supervItem=paramBean.getStr("supervItems");
        List<Bean> nodeInstBeans=new ArrayList<Bean>();
        List<Bean> todoList=new ArrayList<Bean>();
        UserBean userBean= Context.getUserBean();
        String userCode=userBean.getCode();
        String[] approArray=approIds.split(",");
        String[] supervArray=supervItem.split(",");
        int countTime=0;
        for(String approId:approArray){
            List<Bean> todoBeanList= ServDao.finds("SY_COMM_TODO"," and TODO_OBJECT_ID1='"+approId+"' and OWNER_CODE='"+userCode+"'");
            if(todoBeanList.size()==0){
               noOperSups.add(supervArray[countTime]);
            }else if(todoBeanList.size()==1){
                Bean nodeInstBean= WfNodeInstDao.findNodeInstById(todoBeanList.get(0).getStr("TODO_OBJECT_ID2"));
                if("".equals(outBean.getStr("SERV_ID"))){
                    outBean.set("servId",todoBeanList.get(0).getStr("SERV_ID"));
                    outBean.set("approId",approId);
                    outBean.set("currNodeCode",nodeInstBean.getStr("NODE_CODE"));
                }
                operSups.add(supervArray[countTime]);
                nodeInstBeans.add(nodeInstBean);
                todoList.add(todoBeanList.get(0));
            }else{
                //todo 处理同一立项，同一待办处理人的情况
            }
            countTime++;
        }
        outBean.set("noOperSups",noOperSups);
        outBean.set("operSups",operSups);
        outBean.set("nodeInstBeans",nodeInstBeans);
        outBean.set("todoList",todoList);
        return outBean;
    }
    public String getApplyState(String currServId){
        String applyState="";
        //立项中
        if(currServId.contains("REGISTRATION")){
            applyState="'1','2'";
            //事项计划中
        }else if(currServId.contains("ARRANGED")){
            applyState="'3','4'";
            //事项办理中
        }else if(currServId.contains("HANDLING")){
            applyState="5";
        //事项待审核
        }else if(currServId.contains("PENDINGREVIEW")){
            applyState="5";
        }
        return applyState;
    }
}
