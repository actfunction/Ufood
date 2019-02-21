package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;

import java.util.Collections;
import java.util.List;

public class SupPointTodo extends CommonServ {


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
                if(nowNodeInst!=null){
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
