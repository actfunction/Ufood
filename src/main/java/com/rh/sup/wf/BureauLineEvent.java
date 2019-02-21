package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.util.AbstractLineEvent;
import com.rh.sup.util.SupConstant;
import com.rh.sup.util.SupWfeOperate;

/**
 * 司内督查 推送至下次办理 扩展类
 *
 */
public class BureauLineEvent extends AbstractLineEvent {

    /**
     * 顺序送下一个节点时触发此事件
     *
     * @param preWfAct 前一个节点的实例.
     * @param nextWfAct 下一个节点的实例.
     * @param lineDef 线定义Bean.
     */
    @Override
    public void forward(WfAct preWfAct, WfAct nextWfAct, Bean lineDef) {
        //前流程实例
        String preNId=preWfAct.getNodeInstBean().getId();
        //前节点
        String preNodeCode=preWfAct.getNodeInstBean().getStr("NODE_CODE");
        //后节点
        String nextNodeCode=nextWfAct.getNodeInstBean().getStr("NODE_CODE");
        // 表单数据bean
        Bean dataBean = nextWfAct.getProcess().getServInstBean();
        // 获取表单主键
        String approId = dataBean.getId();
        ParamBean paramBean=new ParamBean();
        paramBean.set("approId",approId);
        if("N27".equals(preNodeCode) && "N214".equals(nextNodeCode)){
            String lineCode=lineDef.getStr("LINE_CODE");
            if("L134".equals(lineCode)){
                //审批通过督查办理
                paramBean.set("curState","2");
                paramBean.set("upState","3");
                paramBean.set("deptCode", Context.getUserBean().getDeptCode());
                ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"updateWfState",paramBean);

                //推送至下月办理
                paramBean.set("servId", SupConstant.OA_SUP_APPRO_BUREAU);
                paramBean.set("nId",preNId);
                SupWfeOperate.waitNextGain(paramBean);
            }else{
                //审批不通过返回填写督查办理
                paramBean.set("curState","1");
                paramBean.set("upState","2");
                paramBean.set("deptCode", Context.getUserBean().getDeptCode());
                paramBean.set("niId",preNId);
                ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"updateWfState",paramBean);
            }
        }else if("N214".equals(preNodeCode) && "N3".equals(nextNodeCode)){
            //推送至办结环节，直接将当次的办理情况制为待审批状态
            paramBean.set("curState","1");
            paramBean.set("upState","2");
            paramBean.set("deptCode", Context.getUserBean().getDeptCode());
            ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"updateWfState",paramBean);
        }else if("N3".equals(preNodeCode) && "N214".equals(nextNodeCode)){
            //取消办结，将当次的办理情况制为新增状态
            paramBean.set("curState","2");
            paramBean.set("upState","1");
            paramBean.set("deptCode",Context.getUserBean().getDeptCode());
            ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"updateWfState",paramBean);
        }else if("N212".equals(nextNodeCode)){
            //流程发送至办结，将立项单所有待审核的办理情况制为审批通过状态
            SupWfeOperate.allGainPass(paramBean);
        }
    }
    /**
     * 返回操作送下一个节点时触发此事件.
     *
     * @param preWfAct 前一个节点的实例.
     * @param nextWfAct 下一个节点的实例.
     * @param lineDef 线定义Bean.
     */
    @Override
    public void backward(WfAct preWfAct, WfAct nextWfAct, Bean lineDef) {
        //前流程实例
        String preNId=preWfAct.getNodeInstBean().getId();
        //前节点
        String preNodeCode=preWfAct.getNodeInstBean().getStr("NODE_CODE");
        //后节点
        String nextNodeCode=nextWfAct.getNodeInstBean().getStr("NODE_CODE");
        // 表单数据bean
        Bean dataBean = nextWfAct.getProcess().getServInstBean();
        // 获取表单主键
        String approId = dataBean.getId();
        ParamBean paramBean=new ParamBean();
        //删掉推送至下月办理
        if("N214".equals(preNodeCode) && "N27".equals(nextNodeCode)){
            paramBean.set("approId",approId);
            paramBean.set("servId", SupConstant.OA_SUP_APPRO_BUREAU);
            paramBean.set("nId",preNId);
            SupWfeOperate.deleteWaitNextGain(paramBean);
        }
    }
}
