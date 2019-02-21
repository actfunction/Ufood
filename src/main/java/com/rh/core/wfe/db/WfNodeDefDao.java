package com.rh.core.wfe.db;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 节点定义 数据库操作类
 * @author wanglong
 *
 */
public class WfNodeDefDao {
    /**
     * 工作流节点定义 服务 code
     */
    public static final String SY_WFE_NODE_DEF_SERV = "SY_WFE_NODE_DEF";
    
	
	/**
	 * 插入新的节点定义
	 * 
	 * @param nodeDefBean
	 *            节点定义
	 * @return 流程实例对象
	 */
	public static Bean insertNodeDef(Bean nodeDefBean) {
	    Bean aNodeDefBean = null;
	    if (nodeDefBean.getInt("NODE_TYPE") == WfeConstant.NODE_TYPE_SUB_PROCESS) {
	        nodeDefBean.set("NODE_DEF", JsonUtils.toJson(nodeDefBean));
	        aNodeDefBean = ServDao.create(ServMgr.SY_WFE_PROC_NODE_DEF, nodeDefBean);
	        nodeDefBean.remove("NODE_DEF");
	        nodeDefBean.setId(aNodeDefBean.getId());
        } else {
            aNodeDefBean = ServDao.create(ServMgr.SY_WFE_NODE_DEF, nodeDefBean);
        }
	    
		return aNodeDefBean;
	}
	
	
	/**
	 * 
	 * @param procCode 流程编码
	 */
	public static void deleteNodeDefByProcCode(String procCode) {
		ServDao.deletes(ServMgr.SY_WFE_NODE_DEF, new Bean().set("PROC_CODE", procCode));
		//删除子流程节点
		ServDao.deletes(ServMgr.SY_WFE_PROC_NODE_DEF, new Bean().set("PROC_CODE", procCode));
	}
	    
	/**
	 * 根据流程编码得到节点列表
	 * 
	 * @param procCode
	 *            流程编码
	 * @param cmpyId
	 *            公司ID
	 * @return 流程节点列表
	 */
	public static List<Bean> getNodeListByProcCode(String procCode, String cmpyId) {
	    List<Bean> nodeDefList = ServDao.finds(ServMgr.SY_WFE_NODE_DEF, new Bean().set("PROC_CODE", procCode));
        //查询子流程节点
        List<Bean> subProcNodeDefList = ServDao.finds(ServMgr.SY_WFE_PROC_NODE_DEF, 
                new Bean().set("PROC_CODE", procCode));
        for (Bean subProcNodeDef : subProcNodeDefList) {
            Bean nodeDef = JsonUtils.toBean(subProcNodeDef.getStr("NODE_DEF"));
            subProcNodeDef.remove("NODE_DEF");
            subProcNodeDef.copyFrom(nodeDef);
            nodeDefList.add(subProcNodeDef);
        }
        
        return nodeDefList;
	}
}
