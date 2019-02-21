package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;


/**
 *  要点类事项立项  扩展类   实现跨节点退回功能
 */
public class PointAddWfBinder implements ExtendBinder {

    @Override
    public ExtendBinderResult run(WfAct wfAct, WfNodeDef wfNodeDef, UserBean userBean) {
        ExtendBinderResult result = new ExtendBinderResult();
        // 获取当前节点的bean实例
        Bean nodeBean = wfAct.getNodeInstBean();
        // 当前节点由那个节点推送过来
        String nodeCode = nodeBean.getStr("NODE_CODE");
        // 如果是"N2"节点（其他人员审批要点） 推送过来的 寻找省厅督察员
        if ("N23".equals(nodeCode)){  // 如果是"N23"(督查处审批要点) 推送过来 跨节点退回
            // 获得上一节点 的 id
            String preNodeId = nodeBean.getStr("PRE_NI_ID");
            // 上一节点实例
            Bean preNodeBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", preNodeId));
            // 上上节点的id
            String ppNid = preNodeBean.getStr("PRE_NI_ID");
            // 上上节点的bean实例
            Bean ppNode = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", ppNid));
            String doneUserId = ppNode.getStr("DONE_USER_ID");
            result.setUserIDs(doneUserId);
            result.setAutoSelect(true);
        }
        return result;
    }
}
