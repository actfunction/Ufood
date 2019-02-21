package com.rh.core.wfe.db;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;

/**
 * 节点实例历史表 数据库操作
 * @author wanglong
 *
 */
public class WfNodeInstHisDao {
	
	private static Log log = LogFactory.getLog(WfNodeInstHisDao.class);
	
	/**
	 * 工作流节点实例历史 服务 code
	 */
	public static final String SY_WFE_NODE_INST_HIS_SERV = "SY_WFE_NODE_INST_HIS";
	
	/**
	 * 取消办结的时候，通过流程实例对象 删除节点实例历史对象 ， 真删
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 */
	public static void destroyNodeInstHisBeans(String procInstId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", procInstId);

		ServDao.destroys(SY_WFE_NODE_INST_HIS_SERV, paramBean);
	}
	
	
	
	
    /**
     * 不同用户的用户ID 列表
     * @param pid 流程实例ID
     * @return 不同用户的 DONE_USER_ID
     */
    public static List<Bean> getDistinctUserByProcId(String pid) {
    	Bean paramBean = new Bean();
    	paramBean.set("PI_ID", pid);
    	paramBean.set(Constant.PARAM_SELECT, "distinct DONE_USER_ID");
    	
    	List<Bean> userIDList = ServDao.finds(SY_WFE_NODE_INST_HIS_SERV, paramBean);
    	
    	
    	return userIDList;
    }
    
	/**
	 * 
	 * @param preNodeInst
	 *            当前节点实例ID
	 * @return 由前一个节点实例  送出 的 所有 节点实例
	 */
	public static List<Bean> getNodeInstListByPreNodeInst(String preNodeInst) {
		Bean paramBean = new Bean();
		paramBean.set("PRE_NI_ID", preNodeInst);

		List<Bean> nodeInstList = ServDao.finds(SY_WFE_NODE_INST_HIS_SERV, paramBean);

		return nodeInstList;
	}
	
    /**
     * 不同流经部门的部门ID 列表
     * @param pid 流程实例ID
     * @return 不同流经部门的部门 DONE_DEPT_IDS
     */
    public static List<Bean> getDistinctDeptByProcId(String pid) {
    	Bean paramBean = new Bean();
    	paramBean.set("PI_ID", pid);
    	paramBean.set(Constant.PARAM_SELECT, "distinct DONE_DEPT_IDS");
    	
    	List<Bean> deptIDList = ServDao.finds(SY_WFE_NODE_INST_HIS_SERV, paramBean);
    	
    	
    	return deptIDList;
    }
    
	/**
	 * 已办结 取得 节点实例的 历史信息
	 * 
	 * @param piId
	 *            流程实例ID
	 * @return 节点实例的 历史信息
	 */
	public static List<Bean> getNodeInstHisByFinishPiId(String piId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", piId);

		paramBean.set(Constant.PARAM_ORDER, " NODE_ETIME DESC");

		return ServDao.finds(SY_WFE_NODE_INST_HIS_SERV, paramBean);

	}
	
	/**
	 * 取消办结的时候，复制节点实例历史表 列表 到 节点实例表
	 * 
	 * @param procInstId
	 *            节点实例对象ID
	 */
	public static void copyNodeInstHisBeansToInst(String procInstId) {
		log.debug("copy the node inst history data to the inst table");

		String sqlStr = "insert into "
				+ WfNodeInstDao.SY_WFE_NODE_INST_SERV
				+ " (select * from " + SY_WFE_NODE_INST_HIS_SERV
				+ " where PI_ID = '" + procInstId + "')";

		Context.getExecutor().execute(sqlStr);
		
	}
	
    /**
     * 通过节点实例ID 取得 节点实例对象
     * 
     * @param niId
     *            节点实例ID
     * @return 节点实例对象
     */
    public static Bean findNodeInstById(String niId) {
        Bean aNodeInstBean = ServDao.find(SY_WFE_NODE_INST_HIS_SERV, niId);
        
        if (null == aNodeInstBean) {
            String errorMsg = Context.getSyMsg("SY_WF_NODE_INSTHIS_ID_ERROR", niId);
        	
            throw new RuntimeException(errorMsg);
        }

        return aNodeInstBean;
    }	
    
	/**
	 * 送给我办理的最后一条记录（离当前时间最近的记录）
	 * @param procInstId 流程实例ID
	 * @param userCode 用户编码
	 * @return 用户最近的节点实例Bean
	 */
	public static Bean findUserLastToDo(String procInstId, String userCode) {
        SqlBean sqlBean = new SqlBean();
        sqlBean.and("PI_ID", procInstId);
        sqlBean.and("TO_USER_ID", userCode);
        sqlBean.desc("S_ATIME");

        List<Bean> nodeInstBeanList = ServDao.finds(ServMgr.SY_WFE_NODE_USERS_HIS, sqlBean);

        if (nodeInstBeanList.size() > 0) {
            Bean rtn = nodeInstBeanList.get(0);
            Bean nodeInst = findNodeInstById(rtn.getStr("NI_ID"));
            
            return nodeInst;
        }
		
		
		return null;
	}
}
