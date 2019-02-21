package com.rh.core.comm.mind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.wfe.db.WfProcInstDao;
import com.rh.core.wfe.db.WfProcInstHisDao;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfProcDefManager;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 意见常用类
 *
 */
public class MindUtils {
	
	/** 批准 */
	public static final int MIND_PIZHUN = 1;
	
	/** 同意 */
	public static final int MIND_TONGYI = 2;
	
	/** 不同意 */
	public static final int MIND_TONGYI_NO = 3;
	
	
	/**
	 * 
	 * @param mindType 意见类型(HG,HQ,QF)
	 * @param servId 服务编码
	 * @param dataId 数据ID
	 * @return 按照类型过滤之后的意见列表
	 */
	public static List<Bean> getMindListByType(String mindType, String servId, String dataId) {
		Bean queryBean = new Bean();
		queryBean.set("SERV_ID", servId);
		queryBean.set("DATA_ID", dataId);
		
		String strWhere = " and MIND_CODE like '" + mindType + "%'";
		queryBean.set(Constant.PARAM_WHERE, strWhere);
		
		return ServDao.finds("SY_COMM_MIND", queryBean);
	}
	
    /**
     * @param servId 服务编码
     * @param dataId 数据ID
     * @param sortType 排序类型
     * @return 按照类型过滤之后的意见列表
     */
    public static List<Bean> getMindList(String servId , String dataId , String sortType) {
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList<Bean>();
        }
        
        if (StringUtils.isEmpty(sortType)) {
            sortType = Context.getSyConf("SY_COMM_MIND_SORT_TYPE", MindServ.MIND_SORT_TYPE);
        }
        
        Bean queryBean = new Bean();
        if (sortType.equalsIgnoreCase(MindServ.MIND_SORT_TYPE)) { // 先按意见类型排序，再按时间排序
            queryBean.set(Constant.PARAM_ORDER, " MIND_CODE, MIND_TIME DESC");
        } else {
            queryBean.set(Constant.PARAM_ORDER, " MIND_TIME DESC");
        }
        
        //queryBean.set("SERV_ID", servId);
        queryBean.set("DATA_ID", dataId);
        //queryBean.set("S_FLAG", Constant.YES);
        
        return ServDao.finds("SY_COMM_MIND", queryBean);
    }	
	
	/**
	 * 查找指定意见编码在指定部门内的意见列表
	 * @param mindCode 意见类型编码
	 * @param servId   服务ID
	 * @param dataId   数据ID
	 * @param tDeptCode    部门CODE。允许为NULL，如果为null则不按部门查询。
	 * @return 符合条件的意见列表
	 */
    public static List<Bean> getMindListByCodeInDept(String mindCode , String servId 
            , String dataId , String tDeptCode) {
        
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList<Bean>();
        }
        
        Bean queryBean = new Bean();
        
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and DATA_ID = '").append(dataId).append("'");
        strWhere.append(" and MIND_CODE = '").append(mindCode).append("'");
        if (StringUtils.isNotEmpty(tDeptCode)) {
            strWhere.append(" and S_TDEPT = '").append(tDeptCode).append("'");
        }
        
        strWhere.append(" order by MIND_TIME DESC");
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        
        return ServDao.finds("SY_COMM_MIND", queryBean);
    }
    
    /**
     * 查找指定意见编码的意见列表
     * @param mindCode  意见类型编码
     * @param servId    服务ID
     * @param dataId    数据ID
     * @return 符合条件的意见列表
     */
    public static List<Bean> getMindListByCode(String mindCode , String servId , String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList<Bean>();
        }
        
        Bean queryBean = new Bean();
        
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and DATA_ID = '").append(dataId).append("'");
        strWhere.append(" and MIND_CODE = '").append(mindCode).append("'");
        
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        
        return ServDao.finds("SY_COMM_MIND", queryBean);
    }
		
	/**
	 * @param mindId 意见Id
	 * @return 意见是否同意的
	 */
	public static int getMindValue(String mindId) {
		Bean mindBean = ServDao.find("SY_COMM_MIND", mindId);
		
		if (mindBean.isEmpty("USUAL_ID")) {
			return MIND_TONGYI_NO;
		} 
		
		String usualId = mindBean.getStr("USUAL_ID");
		
		Bean usualBean = ServDao.find(ServMgr.SY_COMM_MIND_USUAL, usualId);
		
		return usualBean.getInt("MIND_VALUE");
	}
	
	/**
	 * @param wfActId 节点ID
	 * @return 意见是否同意的
	 */
	public static Bean getMindByWfActId(String wfActId) {
		Bean queryBean = new Bean();
		queryBean.set("WF_NI_ID", wfActId);

		Bean mindBean = ServDao.find("SY_COMM_MIND", queryBean);
		
		return mindBean;
	}
	
    /**
     * 取得指定审批单
     * @param servId 服务ID
     * @param dataId 数据ID
     * @param mindCode 意见代码
     * @param tDeptId 部门名称
     * @return 最新的固定意见Bean
     */
    public static Bean getLastRegularMind(String servId , String dataId 
        , String mindCode , String tDeptId) {
        if (StringUtils.isEmpty(dataId)) {
            return null;
        }
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and USUAL_ID is not null "); // 固定意见
        // 设置当前审批单的ID
        //strWhere.append(" and SERV_ID = '").append(servId).append("'");
        strWhere.append(" and DATA_ID = '").append(dataId).append("'");
        //
        strWhere.append(" and MIND_CODE = '").append(mindCode).append("'");
        
        if (!StringUtils.isEmpty(tDeptId)) {
            strWhere.append(" and S_TDEPT = '").append(tDeptId).append("'");
        }
        
        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        queryBean.set(Constant.PARAM_ORDER, " MIND_TIME desc"); // 按时间倒排
        
        List<Bean> mindList = ServDao.finds(ServMgr.SY_COMM_MIND, queryBean);
        
        if (mindList.size() > 0) {
            return mindList.get(0);
        }
        
        return null;
    }
	
	
	/**
	 * 收回意见
	 * @param nodeInstId 节点ID
	 * @param newInstId 新节点ID
	 */
	public static void withDrawMind(String nodeInstId, String newInstId) {
		StringBuilder strSql = new StringBuilder();
		strSql.append("update SY_COMM_MIND set S_FLAG = 2, WF_NI_ID = '"); 
		strSql.append(newInstId);
		strSql.append("' where WF_NI_ID = '");
		strSql.append(nodeInstId);
		strSql.append("'");
		
		Context.getExecutor().execute(strSql.toString());
	}
	
    /**
     * @param tdeptCode 有效部门编码
     * @param dataId 数据ID
     * @return 取得本部门未启用的最终意见列表
     */
    public static List<Bean> getDisabledMindInDept(String tdeptCode , String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList<Bean>();
        }
        
        Bean queryBean = new Bean();
        queryBean.set("DATA_ID", dataId);
        queryBean.set("S_TDEPT", tdeptCode);
        queryBean.set("S_FLAG", Constant.NO);
        
        return ServDao.finds(ServMgr.SY_COMM_MIND, queryBean);
    }
	
	/**
	 * @param dataId 数据ID
	 * @return 固定意见的值
	 */
	public static int getLastRegularMindValue(String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return 0;
        }
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_SELECT, "MIND_VALUE");
		
		StringBuilder strWhere = new StringBuilder();
		strWhere.append(" and DATA_ID = '");
		strWhere.append(dataId);
		strWhere.append("' and USUAL_ID is not null");

		queryBean.set(Constant.PARAM_WHERE, strWhere.toString());
		queryBean.set(Constant.PARAM_ORDER, "S_MTIME DESC");
		
		List<Bean> mindList = ServDao.finds(ServMgr.SY_COMM_MIND, queryBean);
		
		if (mindList.size() > 0) {
			return mindList.get(0).getInt("MIND_VALUE");
		}
		
		return 0;
	}
	
	/**
	 * 启用审批单上指定部门未启用的意见。常用于文件办结时。
	 * @param dataId 审批单ID
	 * @param tDeptCode 部门CODE
	 */
    public static void enableMindInDept(String dataId , String tDeptCode) {
        if (StringUtils.isEmpty(dataId)) {
            return;
        }
	    Bean bean = new Bean();
	    bean.set("S_FLAG", Constant.YES_INT);
	    
	    Bean whereBean = new Bean();
	    whereBean.set("DATA_ID", dataId);
	    whereBean.set("S_TDEPT", tDeptCode);
	    whereBean.set("S_FLAG", Constant.NO_INT);
	    ServDao.updates(ServMgr.SY_COMM_MIND, bean, whereBean);
	}
    
    /**
     * 
     * @param mindList 当前节点上的意见列表
     * @return Map<mindCode, mindBean>
     */
    public static Map<String, Bean> getMindMap(List<Bean> mindList) {
        Map<String, Bean> mindMap = new HashMap<String, Bean>();
        
        for (Bean mind: mindList) {
            String mindCode = mind.getStr("MIND_CODE");
            
            if (!mindMap.containsKey(mindCode)) {
                mindMap.put(mindCode, mind);
            }
        }
        
        return mindMap;
    }
    
    /**
     * 
     * @param wfInst 流程实例ID
     * @param wfState 流程状态
     * @return 数据对应的流程定义上的意见类型
     */
    public static HashSet<String> getMindCodes(String wfInst, int wfState) {
        HashSet<String> mindCodes = new HashSet<String>();
        
        Bean procInst = new Bean();
        //根据dataId 获取 流程定义
        if (wfState == WfeConstant.WFE_PROC_INST_IS_RUNNING) {
            procInst = WfProcInstDao.findProcInstById(wfInst);
        } else {
            procInst = WfProcInstHisDao.findProcInstById(wfInst);
        }
        
        String procCode = procInst.getStr("PROC_CODE");
        
        WfProcDef procDef = WfProcDefManager.getWorkflowDef(procCode);
        
        List<Bean> nodeDefList = procDef.getAllNodeDef();
        
        for (Bean nodeDef: nodeDefList) {
            String mindCode = nodeDef.getStr("MIND_CODE");
            String mindTerminal = nodeDef.getStr("MIND_TERMINAL");
            String mindCodeReguler = nodeDef.getStr("MIND_REGULAR"); 
            
            if (!mindCodes.contains(mindCode)) {
                mindCodes.add(mindCode);
            }
            
            if (!mindCodes.contains(mindTerminal)) {
                mindCodes.add(mindTerminal);
            }
            
            if (!mindCodes.contains(mindCodeReguler)) {
                mindCodes.add(mindCodeReguler);
            }
        }
        
        return mindCodes;
    }
}
