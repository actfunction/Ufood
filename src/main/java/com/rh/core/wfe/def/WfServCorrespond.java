package com.rh.core.wfe.def;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.CacheMgr;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;

/**
 * 流程 服务 对应关系
 * 
 * @author wanglong
 * 
 */
public class WfServCorrespond {
	/**
	 * 缓存类型
	 */
	private static final String CACHE_TYPE = "WORKFOLOW_SERV_CORRESPOND";

	private static Logger log = Logger.getLogger(WfServCorrespond.class);

	/**
	 * 获取某个服务挂接的可用的流程定义<br>
	 * 可用的概念：该流程已启用且已生效（生效日期为空或小于当前时间）
	 * 
	 * @param servId
	 *            业务服务Id
	 * @return List<Bean> 流程定义列表（包括公共的及本公司的） 已经按照BIND_SORT顺序排序,先本公司后公共流程排序
	 */
	public static List<Bean> getAvailableProcDef(String servId) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("SERV_ID", servId).and("S_FLAG", 1)
				.appendWhere(" AND (EFFECTIVE_DATE IS NULL OR EFFECTIVE_DATE <= ?)", DateUtils.getDatetimeTS())
				.appendWhere(
						" AND (S_PUBLIC=1 OR S_CMPY=?)  AND (PROC_CODE in (SELECT PROC_CODE FROM SY_WFE_NODE_DEF))",
						Context.getCmpy())
				.orders("PROC_VERSION DESC, BIND_SORT, PROC_IS_LATEST, EN_NAME, S_PUBLIC DESC");

		return ServDao.finds(ServMgr.SY_WFE_PROC_DEF, sqlBean);
	}

	/**
	 * 
	 * @param servId
	 *            服务ID
	 * @param dataBean
	 *            数据Bean
	 * @return 流程定义
	 */
	public static WfProcDef getProcDef(String servId, Bean dataBean) {
		List<WfProcDef> procDefList = getObjFromCache(servId);
		if (null == procDefList) { // 缓存中没找到流程定义的列表 ， 从库里查，并放到缓存中去
			List<Bean> procDefBeans = getAvailableProcDef(servId);
			if (procDefBeans.size() > 0) {
				procDefList = new ArrayList<WfProcDef>();
				for (Bean procDefBean : procDefBeans) {
					procDefList.add(WfProcDefManager.getWorkflowDef(procDefBean.getStr("PROC_CODE")));
				}
				addToCache(servId, procDefList); // 放到缓存中去
			}
		}

		if (null != procDefList) { // 该服务上关联有流程
			return getMeetConditionProc(procDefList, dataBean);
		}

		return null;
	}

	/**
	 * 
	 * @param procDefList
	 *            流程定义列表
	 * @param dataBean
	 *            数据Bean
	 * @return 满足条件的流程定义
	 */
	private static WfProcDef getMeetConditionProc(List<WfProcDef> procDefList, Bean dataBean) {
		WfProcDef rtnProcDef = null;

		UserBean currUser = Context.getUserBean();

		for (WfProcDef procDef : procDefList) {
			if (currUser.getODeptCode().equals(procDef.getStr("BELONG_ODEPT"))) {
				if (procDef.isEmpty("BIND_SCRIPT")) {
					rtnProcDef = procDef;
					break;
				} else if (Lang.isTrueScript(ServUtils.replaceSysAndData(procDef.getStr("BIND_SCRIPT"), dataBean))) {
					rtnProcDef = procDef;
					break;
				}
			}
		}

		if (rtnProcDef != null) {
			return rtnProcDef;
		}
		for (WfProcDef procDef : procDefList) {
			if (procDef.isEmpty("BIND_SCRIPT")) {
				rtnProcDef = procDef;
				break;
			} else if (Lang.isTrueScript(ServUtils.replaceSysAndData(procDef.getStr("BIND_SCRIPT"), dataBean))) {
				rtnProcDef = procDef;
				break;
			}
		}

		return rtnProcDef;
	}

	/**
	 * @param servId
	 *            服务编码
	 * @return 流程定义
	 */
	@SuppressWarnings("unchecked")
	private static List<WfProcDef> getObjFromCache(String servId) {
		return (List<WfProcDef>) CacheMgr.getInstance().get(servId, CACHE_TYPE);
	}

	/**
	 * 将流程定义 放到缓存
	 * 
	 * @param servId
	 *            服务ID
	 * @param procDefList
	 *            流程定义
	 */
	private static void addToCache(String servId, List<WfProcDef> procDefList) {
		CacheMgr.getInstance().set(servId, procDefList, CACHE_TYPE);
	}

	/**
	 * 从新将指定对象装载到内存中
	 * 
	 * @param servId
	 *            服务编码
	 */
	public static void removeFromCache(String servId) {
		try {
			CacheMgr.getInstance().remove(servId, CACHE_TYPE);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
