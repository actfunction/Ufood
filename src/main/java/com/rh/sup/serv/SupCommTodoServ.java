package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.*;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.sup.util.BatchOperate;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;


/**
 * 督查事项待办扩展类
 * @author zhaosheng
 *
 */
public class SupCommTodoServ extends CommonServ{
   private static final String servId="OA_SUP_COMM_TODO";

    /**
     * 待办是否属于同一个立项
     * @param paramBean
     * @return
     */
    public OutBean findHandleType(ParamBean paramBean) {
        String todoIds=paramBean.getStr("todoIds");
        OutBean outBean=new OutBean();
        String appro="";
        String approId="";
        String useServ="";
        for(String todoId:todoIds.split(",")) {
            Bean todoBean = ServDao.find(servId, todoId);
            approId = todoBean.getStr("TODO_OBJECT_ID1");
            useServ=todoBean.getStr("SERV_ID");
            if(StringUtils.isEmpty(appro)){
                appro=approId;
                continue;
            }
            if (appro.equals(approId)){
                continue;
            }else{
                return new OutBean().setError();
            }
       }
       outBean.set("approId",appro);
        outBean.set("SERV_ID",useServ);
        return outBean.setOk();
    }
    /**
     * 获取下次发送办理情况的时间
     * @param paramBean
     * @return
     */
    public OutBean getNextGainDate(ParamBean paramBean) {
        String currServId=paramBean.getStr("SERV_ID");
        String approId=paramBean.getStr("approId");
        Bean bean=ServDao.find(currServId,approId);
        OutBean outBean=new OutBean();
        Bean nextGainDateDict=ServDao.find("OA_SUP_SERV_DICT_HANDLE_TYPE",bean.getStr("HANDLE_TYPE"));
        outBean.set("handleType",nextGainDateDict.getStr("EXTEND1"));
        if("".equals(bean.getStr("NEXT_GAIN_DATE"))){
            String dateStr=nextGainDateDict.getStr("EXTEND2");
            if(!"".equals(dateStr)){
                outBean.set("gainDate",dateStr);
            }
        }else{
            outBean.set("gainDate",bean.getStr("NEXT_GAIN_DATE"));
        }
        return outBean;
    }
    /**
     * 修改下次发送办理情况的时间
     * @param paramBean
     * @return
     */
    public OutBean modifNextGainDate(ParamBean paramBean) {
        String nextGainDate=paramBean.getStr("gainDate");
        String useServId=paramBean.getStr("SERV_ID");
        String approId=paramBean.getStr("approId");
        Bean bean=ServDao.find(useServId,approId);
        bean.set("NEXT_GAIN_DATE",nextGainDate);
        ServDao.update(useServId,bean);
        return new OutBean().setOk();
    }

    //批量审核通过
    public OutBean lotTodoPass(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        if("ready".equals(paramBean.getStr("next"))){
            outBean= BatchOperate.isSameTodoNode(paramBean);
        }else if("go".equals(paramBean.getStr("next"))){
            outBean=BatchOperate.lotTodoPass(paramBean);
        }
        return outBean;
    }
    //批量审核不通过/退回
    public OutBean lotTodoNoPass(ParamBean paramBean) {
        paramBean.set("servId",servId);
        OutBean outBean=BatchOperate.lotTodoNoPass(paramBean);
        return outBean;
    }


    /**
     *   查询表中自定义字段
     * @param paramBean
     * @param outBean
     */
    @Override
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        // 获取查询后的结果集 如果不为空，循环查询自定义字段
        List<Bean> beanList = outBean.getDataList();
        if(beanList!= null && beanList.size()>0){
            for (int i = 0;i<beanList.size();i++){
                Bean bean = beanList.get(i);
                // 根绝当前节点的流程实例ID查询上一节点的流程实例ID
                String nId = bean.getStr("TODO_OBJECT_ID2");
                Bean nowNodeInst = ServDao.find("SY_WFE_NODE_INST", nId);
                if (nowNodeInst!=null){
                    String preId = nowNodeInst.getStr("PRE_NI_ID");

                    // 立项单ID
                    String dataId = bean.getStr("TODO_OBJECT_ID1");
                    // 利用立项单ID和上一节点实例ID查询计划表
                    String planSql = "SELECT DEPT_CODE,RES_STEP,SPEC_FILL,USER_NAME,PLAN_STATE " +
                            "FROM SUP_APPRO_PLAN WHERE APPRO_ID = '"+dataId+"'and TODO_ID = '"+preId+"'";
                    Bean planBean = Transaction.getExecutor().queryOne(planSql);
                    if (planBean!=null){
                        String deptName = DictMgr.getName("SY_ORG_DEPT",planBean.getStr("DEPT_CODE"));
                        bean.set("PLAN_DEPT",deptName);
                        bean.set("RES_STEP",planBean.getStr("RES_STEP"));
                        bean.set("SPEC_FILL",planBean.getStr("SPEC_FILL"));
                        bean.set("USER_NAME",planBean.getStr("USER_NAME"));
                        bean.set("PLAN_STATE",planBean.getStr("PLAN_STATE"));
                    }
                    String gainSql = "SELECT DEPT_CODE,GAIN_TEXT,GAIN_STATE FROM " +
                            "SUP_APPRO_GAIN WHERE APPRO_ID = '"+dataId+"'and TODO_ID = '"+preId+"'";
                    Bean gainBean = Transaction.getExecutor().queryOne(gainSql);
                    if (gainBean!=null){
                        String deptName = DictMgr.getName("SY_ORG_DEPT",gainBean.getStr("DEPT_CODE"));
                        bean.set("GAIN_DEPT",deptName);
                        bean.set("GAIN_TEXT",gainBean.getStr("GAIN_TEXT"));
                        bean.set("GAIN_STATE",gainBean.getStr("GAIN_STATE"));
                    }
                }
                // 督查办结之前  隐藏填写的办结时间
                String applyState = bean.getStr("APPLY_STATE");
                if(applyState=="7" ||applyState=="8" ){

                }else{
                    bean.set("DEALT_TIME","");
                }
            }
        }
        Collections.reverse(beanList);
        outBean.set(Constant.RTN_DATA,beanList);
    }
}
