package com.rh.core.wfe.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.db.WfLineDao;
import com.rh.core.wfe.db.WfNodeActDao;
import com.rh.core.wfe.db.WfNodeDefDao;
import com.rh.core.wfe.db.WfProcDefDao;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流流程定义
 * 
 */
public class WfProcDef extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2740049067158005439L;

	private static Log log = LogFactory.getLog(WfProcDef.class);

	/** 公共按钮类型： 流程 **/
	private static final int ACT_TYPE_WF = 1;

	/** 公共按钮类型： 审批单 **/
	private static final int ACT_TYPE_FORM = 2;

	private Map<String, WfNodeDef> nodeDefBeans = new HashMap<String, WfNodeDef>();

	private List<WfLineDef> lines = new ArrayList<WfLineDef>();

	private List<Bean> proActs = new ArrayList<Bean>();

	private String cmpyId;

	private String procCode;

	/** 子流程节点列表 */
	private Map<String, WfNodeDef> subProcNodeDefBeans = new HashMap<String, WfNodeDef>();

	/**
	 * 获取流程定义的绑定标题，从该流程定义使用的服务中获取
	 * 
	 * @return 绑定标题
	 */
	public String getProcTitle() {
		Bean servDef = ServUtils.getServDef(this.getStr("SERV_ID"));
		if (servDef.isEmpty("SERV_DATA_TITLE") || StringUtils.isBlank(servDef.getStr("SERV_DATA_TITLE"))) { // 没有设置服务上的标题
			throw new TipException("请设置服务" + servDef.getStr("SERV_NAME") + "(" + servDef.getId() + ")" + " 的标题");
		}
		// 从服务定义中取出设置的提醒标题
		return servDef.getStr("SERV_DATA_TITLE");
	}

	/**
	 * 
	 * @return 流程定义按钮
	 */
	public List<Bean> getProActs() {
		return proActs;
	}

	/**
	 * 
	 * @param aProcCode
	 *            流程编码 name@cmpyCode
	 */
	protected WfProcDef(String aProcCode) {
		this.procCode = aProcCode;

		Bean procDefBean = WfProcDefDao.getWfProcBeanByProcCode(procCode);
		if (procDefBean != null) {
			this.copyFrom(procDefBean);
			this.cmpyId = this.getStr("S_CMPY");
			initProc();
		}
	}

	/**
	 * 根据节点Code找到指定节点，没有找到返回null
	 * 
	 * @param code
	 *            节点CODE
	 * @return 节点定义
	 */
	public WfNodeDef findNode(String code) {
		WfNodeDef rtnNodeDef = nodeDefBeans.get(code);
		if (rtnNodeDef == null) {
			throw new RuntimeException("流程定义点不存在，NODE=" + code + ";wfDefCode=" + this.procCode);
		}

		return rtnNodeDef;
	}

	/**
	 * 找到工作流的起草点。没有找到返回RuntimeException
	 * 
	 * @return 找到工作流的起草点
	 */
	public WfNodeDef findStartNode() {
		Object[] aNodeDefs = nodeDefBeans.values().toArray();
		for (int i = 0; i < aNodeDefs.length; i++) {
			WfNodeDef node = (WfNodeDef) aNodeDefs[i];
			if (node.getInt("NODE_TYPE") == WfeConstant.NODE_TYPE_DRAFT) {
				return node;
			}
		}

		throw new RuntimeException("工作流没有定义起点！wfDefCode='" + this.procCode);
	}

	/**
	 * 根据起始点和结束点Code，取得两者之间的连线，没有找到返回null。
	 * 
	 * @param srcNodeCode
	 *            起始节点CODE
	 * @param tarNodeCode
	 *            目标节点CODE
	 * @return 连线定义
	 */
	public WfLineDef findLineDef(String srcNodeCode, String tarNodeCode) {

		WfLineDef rtnBean = null;

		for (WfLineDef lineBean : lines) {
			if (lineBean.getStr("SRC_NODE_CODE").equals(srcNodeCode)
					&& lineBean.getStr("TAR_NODE_CODE").equals(tarNodeCode)) {
				rtnBean = lineBean;
			}
		}

		if (rtnBean == null) {
			for (WfLineDef lineBean : lines) {
				// 返回 LINE_IF_RETURN = 1 可以返回
				if (lineBean.getStr("TAR_NODE_CODE").equals(srcNodeCode)
						&& lineBean.getStr("SRC_NODE_CODE").equals(tarNodeCode)
						&& lineBean.getInt("LINE_IF_RETURN") == Constant.YES_INT) {
					rtnBean = lineBean;
				}
			}
		}
		return rtnBean;
	}

	/**
	 * 找到连接起始点的所有TransitionDefForm列表
	 * 
	 * @param srcNode
	 *            起始节点CODE
	 * @return 起始节点的连线列表
	 */
	public List<WfLineDef> findLineDefList(String srcNode) {
		List<WfLineDef> rtnLines = new ArrayList<WfLineDef>();
		for (WfLineDef lineBean : lines) {
			if (lineBean.getStr("SRC_NODE_CODE").equals(srcNode)) {
				rtnLines.add(lineBean);
			}
		}

		return rtnLines;
	}

	/**
	 * @param nodeCode
	 *            节点编码
	 * @return 取得能从指定节点返回的线定义
	 */
	public List<WfLineDef> findReturnLineDefList(String nodeCode) {
		List<WfLineDef> rtnLines = new ArrayList<WfLineDef>();
		for (WfLineDef lineBean : lines) {
			if (lineBean.getStr("TAR_NODE_CODE").equals(nodeCode)
					&& lineBean.getInt("LINE_IF_RETURN") == WfeConstant.LINE_CAN_RETURN) {
				rtnLines.add(lineBean);
			}
		}
		return rtnLines;
	}

	/**
	 * 根据连线code找到连线定义，没有找到返回null。
	 * @param lineCode 连线CODE
	 * @return 连线定义
	 */
	public WfLineDef findLineDefByLineCode(String lineCode) {
		if (StringUtils.isEmpty(lineCode)) {
			return null;
		}
		WfLineDef rtnBean = null;
		for (WfLineDef lineBean : lines) {
			if (lineCode.equals(lineBean.getStr("LINE_CODE"))) {
				rtnBean = lineBean;
				break;
			}
		}
		return rtnBean;
	}
	
	/**
	 * 根据连线Code，取得返回定义的线，没有找到返回null。
	 * @param srcNodeCode 原节点
	 * @param tarNodeCode 目标节点
	 * @return 连线定义列表
	 */
	public WfLineDef findReturnLineDefByLineCode(String srcNodeCode, String tarNodeCode,String lineCode) {
		if (StringUtils.isEmpty(lineCode)) {
			return null;
		}
		WfLineDef rtnBean = null;
		for (WfLineDef lineBean : lines) {
			if (!lineBean.getStr("LINE_CODE").equals(lineCode)) {
				continue;
			}
			// 只找出可以返回的线
			if (lineBean.getStr("SRC_NODE_CODE").equals(srcNodeCode)
					&& lineBean.getStr("TAR_NODE_CODE").equals(tarNodeCode)
					&& lineBean.getInt("LINE_IF_RETURN") == WfeConstant.LINE_CAN_RETURN) {
				rtnBean = lineBean;
			}
		}
		return rtnBean;
	}
	
	/**
	 * @return 整个流程所有的节点定义
	 */
	public List<Bean> getAllNodeDef() {
		Object[] aNodeDefs = nodeDefBeans.values().toArray();
		List<Bean> list = new ArrayList<Bean>();
		for (int i = 0; i < aNodeDefs.length; i++) {
			WfNodeDef nodeDef = (WfNodeDef) aNodeDefs[i];
			list.add(nodeDef);
		}

		return list;
	}

	/**
	 * 初始化相关定义
	 * 
	 * @throws Exception
	 */
	private void initProc() {

		loadLineDef();

		loadNodeDef();

		loadActDataFormList();

		loadProcActBeanList();

		loadCustomVarList();

		loadCustomBlList();
	}

	/**
	 * 装载线定义
	 */
	private void loadLineDef() {
		// 装载线定义
		List<Bean> lineList = WfLineDao.getLineListByProcCode(getProcCode(), cmpyId);

		for (int i = 0; i < lineList.size(); i++) {
			WfLineDef bean = new WfLineDef(lineList.get(i));
			this.lines.add(bean);
		}
	}

	/**
	 * 装载节点定义
	 */
	private void loadNodeDef() {
		// 装载节点定义
		List<Bean> nodeDefBeanList = WfNodeDefDao.getNodeListByProcCode(this.procCode, cmpyId);

		for (Bean nodeDefBean : nodeDefBeanList) {
			WfNodeDef nodeDef = new WfNodeDef(nodeDefBean);
			nodeDefBeans.put(nodeDefBean.getStr("NODE_CODE"), nodeDef);
			// 子流程节点
			if (nodeDef.getInt("NODE_TYPE") == WfeConstant.NODE_TYPE_SUB_PROCESS) {
				subProcNodeDefBeans.put(nodeDefBean.getStr("NODE_CODE"), nodeDef);
			}
		}
	}

	/**
	 * 装载节点变量定义
	 */
	private void loadActDataFormList() {
		List<Bean> nodeActList = WfNodeActDao.findNodeActList(procCode);

		for (Bean nodeActBean : nodeActList) {
			WfNodeDef wfNodeDef = nodeDefBeans.get(nodeActBean.getStr("NODE_CODE"));
			if (wfNodeDef == null) {
				throw new RuntimeException("节点不存在，" + nodeActBean.getStr("NODE_CODE"));
			} else {
				wfNodeDef.addActData(nodeActBean);
				if (nodeActBean.getInt("FIELD_CONTROL") == WfeConstant.WF_FIELD_CONTROL_READ) {
					wfNodeDef.setFieldException(nodeActBean.getStr("FIELD_EXCEPTION"));
				} else {
					wfNodeDef.setEntirelyControl(true);
				}

				wfNodeDef.setFieldHidden(nodeActBean.getStr("FIELD_HIDDEN"));
				wfNodeDef.setFieldMust(nodeActBean.getStr("FIELD_MUST"));
				wfNodeDef.setFileControl(nodeActBean.getStr("FILE_CONTROL"));
				wfNodeDef.setFieldDisplay(nodeActBean.getStr("FIELD_DISPLAY"));

				for (int i = 0; i < WfNodeDef.GROUP_FIELDS.length; i++) {
					wfNodeDef.set(WfNodeDef.GROUP_FIELDS[i], nodeActBean.getStr(WfNodeDef.GROUP_FIELDS[i]));
				}

				List<Bean> paramList = null;
				// 如果按钮参数不为NULL，则
				if (nodeActBean.isNotEmpty("BUTTON_PARAMS")) {
					String params = nodeActBean.getStr("BUTTON_PARAMS");
					paramList = JsonUtils.toBeanList(params);
				}

				String buttonAlias = nodeActBean.getStr("BUTTON_ALIAS");
				String[] btnAlias = buttonAlias.split("~");
				HashMap<String, String> btnAliasMap = getBtnAlias(btnAlias);

				addFormButton(wfNodeDef, nodeActBean, paramList, btnAliasMap);
				addWfButton(wfNodeDef, nodeActBean, paramList, btnAliasMap);

				// 更新表达式列表
				if (nodeActBean.isNotEmpty("FIELD_UPDATE")) {
					List<Bean> updateExpressList = JsonUtils.toBeanList(nodeActBean.getStr("FIELD_UPDATE"));
					wfNodeDef.setFieldUpdateExpressList(updateExpressList);
				}
			}
		}
	}

	/**
	 * 
	 * @param btnAlias
	 *            按钮别名
	 * @return map<actCode, actName> 对应关系的
	 */
	private HashMap<String, String> getBtnAlias(String[] btnAlias) {
		HashMap<String, String> btnAliasMap = new HashMap<String, String>();

		for (int i = 0; i < btnAlias.length; i++) {
			if (btnAlias[i].trim().length() > 0 && btnAlias[i].startsWith("{")) {
				Bean btnAlia = JsonUtils.toBean(btnAlias[i]);
				if (btnAlia.getStr("ACT_CODE").length() > 0) {
					btnAliasMap.put(btnAlia.getStr("ACT_CODE"), btnAlia.getStr("ACT_NAME"));
				}
			}
		}

		return btnAliasMap;
	}

	/**
	 * @param wfNodeDef
	 *            节点定义
	 * @param nodeActBean
	 *            节点Act定义
	 * @param paramList
	 *            参数列表
	 * @param btnAliasMap
	 *            按钮别名的对应关系
	 */
	private void addFormButton(WfNodeDef wfNodeDef, Bean nodeActBean, List<Bean> paramList,
			HashMap<String, String> btnAliasMap) {
		String buttonStr = nodeActBean.getStr("FORM_BUTTONS");
		ServDefBean servDef = ServUtils.getServDef(this.getStr("SERV_ID"));
		log.debug("添加表单按钮到定义中,定义的按钮列表" + buttonStr);
		String[] formButton = buttonStr.split(",");
		for (int i = 0; i < formButton.length; i++) {
			Bean actBean = servDef.getAct(formButton[i]);
			if (null != actBean) {
				// 克隆一个新的ActBean
				Bean newBean = actBean.copyOf();

				String actCode = actBean.getStr("ACT_CODE");
				if (null != btnAliasMap.get(actCode) && btnAliasMap.get(actCode).length() > 0) { // 如果设置了别名，
																									// 则别名覆盖按钮的名称
					newBean.set("ACT_RENAME", btnAliasMap.get(actCode));
				}

				// 取得按钮对应的参数
				String param = getParamValue(paramList, newBean.getStr("ACT_CODE"));
				if (StringUtils.isNotEmpty(param)) {
					newBean.set("WFE_PARAM", param);
				}
				wfNodeDef.addFormButton(newBean);
				log.debug("添加表单按钮到定义中" + newBean.getStr("ACT_CODE"));
			}
		}
	}

	/**
	 * @param paramList
	 *            参数定义
	 * @param btnCode
	 *            按钮Code
	 * @return 指定按钮的参数
	 */
	private String getParamValue(List<Bean> paramList, String btnCode) {
		if (paramList != null) {
			for (Bean bean : paramList) {
				if (btnCode.equals(bean.getStr("name"))) {
					return bean.getStr("value");
				}
			}
		}
		return "";
	}

	/**
	 * @param wfNodeDef
	 *            节点定义
	 * @param nodeActBean
	 *            流程按钮定义
	 * @param paramList
	 *            按钮参数定义
	 * @param btnAliasMap
	 *            按钮别名对应
	 */
	private void addWfButton(WfNodeDef wfNodeDef, Bean nodeActBean, List<Bean> paramList,
			HashMap<String, String> btnAliasMap) {
		String buttonStr = nodeActBean.getStr("WF_BUTTONS");
		log.debug("node add workflow button " + buttonStr);
		String[] formButton = buttonStr.split(",");

		ServDefBean servDef = ServUtils.getServDef(ServMgr.SY_WFE_PROC_DEF_ACT);
		for (int i = 0; i < formButton.length; i++) {
			Bean actBean = servDef.getAct(formButton[i]);
			if (null != actBean) {
				log.debug("node act " + actBean.getStr("ACT_CODE"));
				// 克隆一个新的ActBean
				Bean newBean = actBean.copyOf();

				String actCode = actBean.getStr("ACT_CODE");
				if (null != btnAliasMap.get(actCode) && btnAliasMap.get(actCode).length() > 0) { // 如果设置了别名，
																									// 则别名覆盖按钮的名称
					newBean.set("ACT_RENAME", btnAliasMap.get(actCode));
				}

				// 取得按钮对应的参数
				String param = getParamValue(paramList, actBean.getStr("ACT_CODE"));
				if (StringUtils.isNotEmpty(param)) {
					newBean.set("WFE_PARAM", param);
				}
				wfNodeDef.addWfButton(newBean);
			}
		}
	}

	/**
	 * 装载 流程定义操作类 一个流程对应一条记录
	 */
	private void loadProcActBeanList() {
		// 查询流程公共按钮定义
		SqlBean query = new SqlBean();
		query.set("PROC_CODE", this.getId()); // 设置流程编码
		List<Bean> actList = ServDao.finds(ServMgr.SY_WFE_NODE_PACTS, query);

		ServDefBean wfServDef = ServUtils.getServDef(ServMgr.SY_WFE_PROC_DEF_ACT);
		ServDefBean formServDef = ServUtils.getServDef(this.getStr("SERV_ID"));

		for (Bean bean : actList) {
			if (bean.getInt("ACT_TYPE") == ACT_TYPE_WF) { // 流程按钮
				Bean act = wfServDef.getAct(bean.getStr("ACT_CODE"));
				if (act != null) {
					proActs.add(act);
				}
			} else if (bean.getInt("ACT_TYPE") == ACT_TYPE_FORM) { // 审批单按钮
				Bean act = formServDef.getAct(bean.getStr("ACT_CODE"));
				if (act != null) {
					proActs.add(act);
				}
			}
		}
	}

	/**
	 * 装载自定义变量列表
	 */
	private void loadCustomVarList() {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);

		List<Bean> list = ServDao.finds(WfeConstant.SY_WFE_CUSTOM_VAR, paramBean);

		for (Bean customVar : list) {
			WfNodeDef wfNodeDef = nodeDefBeans.get(customVar.getStr("NODE_CODE"));
			if (wfNodeDef.get(WfeConstant.CUSTOM_VARS) != null) {
				List<Bean> customVars = wfNodeDef.getList(WfeConstant.CUSTOM_VARS);
				customVars.add(customVar);
			} else {
				List<Bean> customVars = new ArrayList<Bean>();
				wfNodeDef.put(WfeConstant.CUSTOM_VARS, customVars);
				customVars.add(customVar);
			}

		}
	}

	/**
	 * 装载扩展办理人
	 */
	private void loadCustomBlList() {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);
		paramBean.set("_ORDER_", "BL_SORT ASC");
		List<Bean> list = ServDao.finds(WfeConstant.SY_WFE_CUSTOM_BL, paramBean);

		for (Bean customBean : list) {
			WfNodeDef wfNodeDef = nodeDefBeans.get(customBean.getStr("NODE_CODE"));
			if (wfNodeDef.get(WfeConstant.CUSTOM_BL) != null) {
				List<Bean> customBl = wfNodeDef.getList(WfeConstant.CUSTOM_BL);
				customBl.add(customBean);
			} else {
				List<Bean> customBl = new ArrayList<Bean>();
				wfNodeDef.put(WfeConstant.CUSTOM_BL, customBl);
				customBl.add(customBean);
			}

		}
	}

	/**
	 * 根据指定参数的值，生成工作流代码 , 规则是name@cmpyCode
	 * 
	 * @param cmpyID
	 *            公司ID
	 * @param name
	 *            名称
	 * @return 工作流代码 ?
	 */
	public static String genrateWfDefCode(String cmpyID, String name) {
		return name + "@" + cmpyID;
	}

	/**
	 * @return 流程编码
	 */
	public String getProcCode() {
		return procCode;
	}

	/**
	 * @return 流程中所有的子流程节点
	 */
	public Map<String, WfNodeDef> getSubProcNodeDefBeans() {
		return subProcNodeDefBeans;
	}

	/**
	 * 
	 * @return 返回服务ID
	 */
	public String getServId() {
		return this.getStr("SERV_ID");
	}

	/**
	 * 返回所有连线定义
	 * @return 连线定义
	 */
	public List<WfLineDef> getLines() {
		return lines;
	}
}
