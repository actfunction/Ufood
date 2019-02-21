package com.rh.core.comm.entity;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.DeptBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.wfe.util.WfTodoProvider;

/**
 * 
 * @author wanglong 
 *
 */
public class EntityServ extends CommonServ {

    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        super.afterSave(paramBean, outBean);
        
        Bean oldBean = paramBean.getSaveOldData();
        if (oldBean != null) {
            final String oldTitle = oldBean.getStr("TITLE");
            String newTitle = outBean.getStr("TITLE");

            if (!newTitle.equals(oldTitle)) { //如果标题有变化，则更改待办标题。
                String piId = oldBean.getStr("S_WF_INST");
                SqlBean sqlBean = new SqlBean();
                sqlBean.and("PI_ID", piId);
                List<Bean>nodeBeanList = ServDao.finds("SY_WFE_NODE_INST", sqlBean);
                //判断是否是拟稿阶段
                if (null != nodeBeanList&&nodeBeanList.size() == 1) {
                	Bean nodeBean = nodeBeanList.get(0);
                	//需多一步node_if_running判断
            		if (nodeBean.getInt("NODE_IF_RUNNING") == 1 && WfTodoProvider.startTodoNode(nodeBean.getStr("NODE_CODE"),nodeBean.getStr("PROC_CODE"),nodeBean.getStr("PRE_NI_ID"))) {
                    	String servId = oldBean.getStr("SERV_ID");
            			Bean servDataBean = ServDao.find(servId, oldBean.getStr("DATA_ID"));
                    	Boolean autoSW = false;
                    	if (null !=servDataBean&&!servDataBean.isEmpty()) {
                    		if (servId.equals(Context.getSyConf("OA_AUTO_SW_SERV", "OA_GW_GONGWEN_ICBCSW"))&&!"2".equals(servDataBean.getStr( Context.getSyConf("OA_AUTO_SW_PARAM", "IS_FW_TO_SW")))) {//自动收文时候，该值不等于2
                    			autoSW = true;
                    		}
                    	}
                    	//自动收文拟稿不需要加草稿
                    	if (!autoSW) {
                    		newTitle = TodoUtils.CAO_GAO + newTitle;
                    	}
            		}
                }
            	
                Bean whereBean = new Bean();
                whereBean.set("TODO_OBJECT_ID1", outBean.getStr("DATA_ID"));
                Bean dataBean = new Bean();
                dataBean.set("TODO_TITLE", newTitle);
                TodoUtils.updates(dataBean, whereBean);
            }
        }
        
    }
    /**
     * 根据tdept 的 code 值 获取相应的tdept name值
     * @param paranBean 参数
     * @return 结果集
     */
    public OutBean getTdeptName(ParamBean paranBean) {
    	OutBean outBean = new OutBean();
    	outBean.set("TDEPT_CODE", "");
    	outBean.set("TDEPT_NAME", "");
        Bean entityBean = EntityMgr.getEntity(paranBean.getStr("DATA_ID"));
        if (entityBean == null) {
        	return outBean;
        }
        
        DeptBean deptBean = OrgMgr.getDept(entityBean.getStr("S_TDEPT"));
        if (deptBean != null) {
            outBean.set("TDEPT_CODE", deptBean.getId());
            outBean.set("TDEPT_NAME", deptBean.getName());
        }
        return outBean;
    }
}
