package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author kfzx-zhaosheng
 * 督查使用的流程操作公共方法
 * 包括流程发送后改变状态、推送下次办理、办结合并流程等
 */
public class SupWfeOperate {
    private static Logger log= Logger.getLogger(SupWfeOperate.class);

    /**
     * 已知当前节点实例nId，获取流经节点nodeCode的nodeInstBean
     * @param nId
     * @param nodeCode
     * @return nodeInstBean
     */
    /*public static Bean getAppointNodeInst(String nId,String nodeCode){
        Bean nodeInstBean=new Bean();
        Bean currNodeInstBean= WfNodeInstDao.findNodeInstById(nId);
        String preId=currNodeInstBean.getStr("PRE_NI_ID");
        List<Bean> nodeInstList= WfNodeInstDao.getNodeInstHisByNoFinishPiId(currNodeInstBean.getStr("PI_ID"));
        for(int i=0;i<nodeInstList.size();i++){

        }
        return nodeInstBean;
    }*/

    /**
     * 办结时将所有待审核的办理情况制为通过
     *
     * @return
     */
    public static void allGainPass(ParamBean paramBean) {
        String approId = paramBean.getStr("approId");// 立项单主键
        SqlBean sql = new SqlBean();
        sql.and("APPRO_ID", approId);
        sql.and("GAIN_STATE", "2");
        List<Bean> gainList = ServDao.finds(SupConstant.OA_SUP_APPRO_GAIN, sql);
        for(Bean gainBean:gainList){
            gainBean.set("GAIN_STATE","3");
            ServDao.update(SupConstant.OA_SUP_APPRO_GAIN,gainBean);
        }
    }
    /**
     *  推送至下月办理时，删除办理人待办
     *  @param paramBean
     */
    public static void waitNextGain(ParamBean paramBean) {
        //系统配置项，可关闭下月推送功能
        Boolean waitNextMonth= Context.getSyConf("WAIT_NEXT_GAIN",true);
        if(!waitNextMonth){
            log.debug("-------------------------推送至下月办理功能已关闭，本次推送后，处理人会立即收到督查办理的待办-------------");
            return;
        }
        Bean gainBean=new Bean();
        String currServId=paramBean.getStr("servId");
        String approId=paramBean.getStr("approId");
        gainBean.set("SERV_ID",currServId);
        gainBean.set("APPRO_ID",approId);
        String niId=paramBean.getStr("niId");
        List<Bean> nextNodeInstList= ServDao.finds("SY_WFE_NODE_INST"," and PRE_NI_ID='"+niId+"'");
        for(Bean nextNodeInst:nextNodeInstList){
            String currNiId=nextNodeInst.getId();
            List<Bean> nextTodoList= ServDao.finds("SY_COMM_TODO"," and TODO_OBJECT_ID2='"+currNiId+"'");
            for(Bean nextTodo:nextTodoList){
                gainBean.set("TODO_ID",nextTodo.getId());
                gainBean.set("OWNER_CODE",nextTodo.getStr("OWNER_CODE"));
                gainBean.set("NI_ID",nextNodeInst.getId());
                ServDao.create("OA_SUP_NEXT_GAIN_PUSH",gainBean);
                ServDao.delete("SY_COMM_TODO",nextTodo.getId());
            }
        }
    }


    /**
     * 删除在待下月推送清单中的待办
     * @param paramBean
     */
    public static void deleteWaitNextGain(ParamBean paramBean) {
        //系统配置项，可关闭下月推送功能
        Boolean waitNextMonth= Context.getSyConf("WAIT_NEXT_GAIN",true);
        if(!waitNextMonth){
            log.debug("-------------------------推送至下月办理功能已关闭，本次推送后，处理人会立即收到督查办理的待办-------------");
            return;
        }
        String currServId=paramBean.getStr("servId");
        String approId=paramBean.getStr("approId");
        String nId=paramBean.getStr("niId");
        Bean deleteBean=new Bean();
        deleteBean.set("_WHERE_"," and NI_ID='"+nId+"' and APPRO_ID='"+approId+"' and SERV_ID='"+currServId+"'");
      //  ServDao.deletes(SupConstant.OA_SUP_NEXT_GAIN_PUSH,deleteBean);
    }
}
