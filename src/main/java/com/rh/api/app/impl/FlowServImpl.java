package com.rh.api.app.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rh.api.BaseApiServ;
import com.rh.api.app.IFlowServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.PageBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Strings;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfNodeInstHisDao;
import com.rh.api.util.ApiConstant;
import com.rh.api.util.DataStatsUtil;
import com.rh.api.util.GwTimeHelper;

/**
 * 获取服务基本信息服务类
 * 
 * @author syp
 *
 */
public class FlowServImpl extends BaseApiServ implements IFlowServ {
	/** 常用流程服务id */
	private static final String FLOW_COMM_SERVID = "FM_PROC_COMM";
	/** 流程类型服务id */
	private static final String FLOW_TYPE_SERVID = "SY_WFE_PROC_TYPE";
	/** 流程列表服务id */
	private static final String FLOW_REG_SERVID = "FM_PROC_REG_TJ_V";
	/** 流程中心服务代码 */
	private static final String FLOW_APP_HTML_PATH = "/showForm/";

	/** 获得常用流程列表 */
	@Override
	public ApiOutBean getCommList(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();

		// 参数信息
		String uid = paramBean.getStr("uid");
		int rowNum = paramBean.get(Constant.PAGE_SHOWNUM, 10);// 默认分页,每页*条数据
		int pageNum = paramBean.get(Constant.PAGE_NOWPAGE, 1);// 默认显示第一页

		// 查询字段
		StringBuilder selectCol = new StringBuilder();
		selectCol.append("FM_ID,");
		selectCol.append("PROC_CODE,");
		selectCol.append("REG_PROC_NAME,");
		selectCol.append("REG_ENDS,");
		selectCol.append("REG_PROC_ODEPT");

		// 查询条件
		StringBuilder whereSql = new StringBuilder();
		whereSql.append(" and USER_CODE='" + uid + "' ");

		// 数据
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, selectCol.toString());// 查询字段
		queryBean.set(Constant.PARAM_WHERE, whereSql.toString());// where条件
		queryBean.set(Constant.PARAM_ORDER, " PROC_NUM desc ");// 排序条件
		queryBean.set(Constant.PAGE_SHOWNUM, rowNum);// 每页显示多少
		queryBean.set(Constant.PAGE_NOWPAGE, pageNum);// 当前页

		OutBean resultBean = queryServ(FLOW_COMM_SERVID, queryBean);// 列表查询
		if (resultBean.getDataList() != null) {// 设置所属机构名称
			for (Bean bean : resultBean.getDataList()) {
				String deptCode = bean.getStr("REG_PROC_ODEPT");
				String deptName = DictMgr.getFullNames("SY_ORG_DEPT_ALL", deptCode);
				bean.set("REG_PROC_ODEPT__NAME", deptName);
			}
		}
		outBean.setData(resultBean);
		return outBean;
	}

	/** 新增常用流程 */
	@Override
	public ApiOutBean addComm(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();

		String uid = paramBean.getStr("uid");
		String applyCode = paramBean.getStr("APPLY_CODE");// 流程编码

		// 新增前验证
		ParamBean whereBean = new ParamBean();
		whereBean.set("USER_CODE", uid);
		whereBean.set("PROC_CODE", applyCode);
		Bean resBean = ServDao.find(FLOW_COMM_SERVID, whereBean);
		// 只有数据库没有的情况下才会新增
		if (resBean == null) {
			Bean dataBean = new Bean();
			dataBean.set("USER_CODE", uid);
			dataBean.set("PROC_CODE", applyCode);
			dataBean.set("PROC_NUM", 1);
			ServDao.save(FLOW_COMM_SERVID, dataBean);
		}
		return outBean;
	}

	/** 删除常用流程 */
	@Override
	public ApiOutBean delCommList(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();
		// 参数信息
		String pkCode = paramBean.getId();
		// 删除操作
		ServDao.delete(FLOW_COMM_SERVID, pkCode);

		return outBean;
	}

	/** 获得流程类型列表 */
	@Override
	public ApiOutBean getFlowTypeList(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();

		// 参数信息
		// int rowNum = queryBean.get(Constant.PAGE_SHOWNUM, 10);// 默认分页,每页*条数据
		// int pageNum = queryBean.get(Constant.PAGE_NOWPAGE, 1);// 默认显示第一页
		int rowNum = -1;// 不分页
		int pageNum = 1;

		// 查询字段
		StringBuilder selectCol = new StringBuilder();
		selectCol.append("ID,");
		selectCol.append("TYPE_NAME,");
		selectCol.append("TYPE_CODE,");
		selectCol.append("TYPE_PCODE,");
		selectCol.append("TYPE_SORT");

		// 查询条件
		String whereSql = " and S_FLAG=1 ";
		// 数据
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, selectCol.toString());// 查询字段
		queryBean.set(Constant.PARAM_WHERE, whereSql);// where条件
		queryBean.set(Constant.PARAM_ORDER, " TYPE_PCODE desc,TYPE_SORT asc ");// 排序条件
		queryBean.set(Constant.PAGE_SHOWNUM, rowNum);// 每页显示多少
		queryBean.set(Constant.PAGE_NOWPAGE, pageNum);// 当前页
		OutBean resultBean = queryServ(FLOW_TYPE_SERVID, queryBean);// 列表查询

		List<Bean> result = resultBean.getDataList();
		List<Bean> newResult = new ArrayList<Bean>();
		// 一级分类
		for (Bean bean : result) {
			if (Strings.isBlank(bean.getStr("TYPE_PCODE"))) {
				bean.set("CHILD", new ArrayList<Bean>());
				newResult.add(bean);
			}
		}
		// 二级分类
		for (Bean bean : result) {
			if (!Strings.isBlank(bean.getStr("TYPE_PCODE"))) {
				for (Bean bean2 : newResult) {
					if (bean2.getStr("TYPE_CODE").equals(bean.getStr("TYPE_PCODE"))) {
						bean2.getList("CHILD").add(bean);
						break;
					}
				}
			}
		}
		resultBean.setData(newResult);
		outBean.setData(resultBean);
		return outBean;
	}

	/** 获得流程列表 */
	@Override
	public ApiOutBean getFlowList(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();

		// 参数信息
		String uid = paramBean.getStr("uid");
		int rowNum = paramBean.get(Constant.PAGE_SHOWNUM, 10);// 默认分页,每页*条数据
		int pageNum = paramBean.get(Constant.PAGE_NOWPAGE, 1);// 默认显示第一页
		String typeOne = paramBean.getStr("TYPE_ONE");// 一级分类
		String typeTwo = paramBean.getStr("TYPE_TWO");// 二级分类
		String keyWord = paramBean.getStr("KEY_WORD");// 搜索关键字

		UserBean userBean = UserMgr.getUser(uid);
		String odeptCode = userBean.getODeptCode();
		int odeptLevel = userBean.getODeptLevel();

		// 查询字段
		StringBuilder selectCol = new StringBuilder();
		selectCol.append("REG_PROC_NAME,");
		selectCol.append("REG_PROC_SERVID,");
		selectCol.append("REG_PROC_TYPE_ONE,");
		selectCol.append("REG_PROC_TYPE_TWO,");
		selectCol.append("REG_ENDS,");
		selectCol.append("REG_PROC_ODEPT,");
		selectCol.append("TJ_USE_NUMS,");
		selectCol.append("TJ_OVER_NUMS,");
		selectCol.append("TJ_TOTAL_TIME");

		// 查询条件
		StringBuilder whereSql = new StringBuilder();
		whereSql.append(" and REG_STATE='2' ");// 上线登记是上线状态
		whereSql.append(" and exists (select 1 from FM_PROC_AUTH fpa ");// 关联权限
		whereSql.append(" where fpa.PROC_CODE = REG_PROC_SERVID ");
		whereSql.append(" and (fpa.PROC_ODEPT_CODE = '" + odeptCode + "' ");
		whereSql.append(" or (fpa.PUBLIC_FLAG = '1' and fpa.SHARE_LEVEL = '" + odeptLevel + "'))) ");
		if (!Strings.isBlank(typeOne)) {// 流程分类
			whereSql.append(" and REG_PROC_TYPE_ONE='" + typeOne + "' ");
		}
		if (!Strings.isBlank(typeTwo)) {// 流程分类
			whereSql.append(" and REG_PROC_TYPE_TWO='" + typeTwo + "' ");
		}
		if (!Strings.isBlank(keyWord)) {// 搜索
			whereSql.append(" and REG_PROC_NAME like '%" + keyWord + "%' ");
		}

		// 数据
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, selectCol.toString());// 查询字段
		queryBean.set(Constant.PARAM_WHERE, whereSql.toString());// where条件
		queryBean.set(Constant.PARAM_ORDER, "");// 排序条件
		queryBean.set(Constant.PAGE_SHOWNUM, rowNum);// 每页显示多少
		queryBean.set(Constant.PAGE_NOWPAGE, pageNum);// 当前页

		OutBean resultBean = queryServ(FLOW_REG_SERVID, queryBean);// 列表查询
		if (resultBean.getDataList() != null) {// 设置所属机构名称
			for (Bean bean : resultBean.getDataList()) {
				String deptCode = bean.getStr("REG_PROC_ODEPT");
				String deptName = DictMgr.getFullNames("SY_ORG_DEPT_ALL", deptCode);
				bean.set("REG_PROC_ODEPT__NAME", deptName);
				bean.set("APP_PATH", FLOW_APP_HTML_PATH + bean.getStr("REG_PROC_SERVID"));// 移动端起草地址
			}
		}
		outBean.setData(resultBean);
		return outBean;
	}

	/** 获得流程跟踪数据 */
	public ApiOutBean getWfeTrack(ApiParamBean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		// pc端对应方法WfeApiServImpl.getWfeTrackForPC
		String servId = reqData.getStr("SERV_ID");
		String dataId = reqData.getStr("DATA_ID");

		if (reqData.isEmpty("SERV_ID") || reqData.isEmpty("DATA_ID")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		// 取得SERV_ID服务相关表中S_WF_INST字段值
		// Bean wfBean = ServDao.find(servId, dataId);
		// Bean wfBean = ServMgr.act(servId, ServMgr.ACT_BYID, new
		// Bean().setId(dataId));
		Bean wfBean = ServDao.find(servId, dataId);// 查询数据

		// 查询流程办理详情
		String wfInst = wfBean.getStr("S_WF_INST");// 得到流程节点实例
		String procRunning = wfBean.getStr("S_WF_STATE");// 得到流程状态
		String queryTable = "";
		ParamBean paramBean = new ParamBean();
		paramBean.set("PI_ID", wfInst);
		if (procRunning.equals("2")) { // 流程已办结
			queryTable = WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV;
		} else { // 流程未办结
			queryTable = WfNodeInstDao.SY_WFE_NODE_INST_SERV;
		}
		paramBean.setSelect("*");
		// List<Bean> list = ServDao.finds(queryTable, paramBean);
		@SuppressWarnings("unchecked")
		List<Bean> list = (List<Bean>) ServMgr.act(queryTable, ServMgr.ACT_FINDS, paramBean).getData();

		// 查询各个环节的意见
		HashMap<String, Bean> map = new HashMap<String, Bean>();
		for (Bean nodeInstBean : list) {
			map.put(nodeInstBean.getId(), nodeInstBean);
		}
		ParamBean mindQueryBean = new ParamBean();
		mindQueryBean.set("_WHERE_", " and DATA_ID = '" + dataId + "'");
		@SuppressWarnings("unchecked")
		List<Bean> mindList = (List<Bean>) ServMgr.act("SY_COMM_MIND", ServMgr.ACT_FINDS, mindQueryBean).getData();
		// 列表改为map格式,key为流程节点id
		HashMap<String, Bean> mindMap = new HashMap<String, Bean>();
		for (Bean mindBean : mindList) {
			mindMap.put(mindBean.getStr("WF_NI_ID"), mindBean);
		}

		// 转换流程流转信息
		List<Bean> newlist = new ArrayList<Bean>();
		for (Bean bean : list) {
			Bean preNode = map.get(bean.getStr("PRE_NI_ID"));
			Bean mindNode = mindMap.get(bean.getId());
			String mindContent = "";
			if (mindNode != null) {
				mindContent = mindNode.getStr("MIND_CONTENT");
			}
			UserBean doneUserBean = UserMgr.getUser(bean.getStr("TO_USER_ID"));
			UserBean sendUserBean = UserMgr.getUser(bean.getStr("TO_USER_ID"));
			if (bean.isNotEmpty("PRE_NI_ID")) {
				doneUserBean = UserMgr.getUser(bean.getStr("TO_USER_ID"));
				sendUserBean = UserMgr.getUser(preNode.getStr("DONE_USER_ID"));
			}
			Bean resBean = new Bean();
			resBean.set("wfNId", bean.getStr("NI_ID"));
			resBean.set("nodeName", bean.getStr("NODE_NAME"));
			resBean.set("doneUserCode", doneUserBean.getId());
			resBean.set("doneUserName", doneUserBean.getName());
			resBean.set("doneUserPost", doneUserBean.getPost());
			resBean.set("doneDeptCode", doneUserBean.getDeptCode());
			resBean.set("doneDeptName", doneUserBean.getDeptName());
			resBean.set("doneUserImg", doneUserBean.getImgSrc());
			resBean.set("doneTime", bean.getStr("NODE_ETIME"));
			resBean.set("sendUserCode", sendUserBean.getId());
			resBean.set("sendUserName", sendUserBean.getName());
			resBean.set("sendUserPost", sendUserBean.getPost());
			resBean.set("sendDeptCode", sendUserBean.getDeptCode());
			resBean.set("sendDeptName", sendUserBean.getDeptName());
			resBean.set("sendUserImg", sendUserBean.getImgSrc());
			resBean.set("sendTime", bean.getStr("NODE_BTIME"));
			resBean.set("duration", bean.getInt("NODE_DAYS") > 0 ? DataStatsUtil.division(bean.getInt("NODE_DAYS"), 60) : 0);
			resBean.set("mindContent", mindContent);
			resBean.set("doneType", bean.getStr("DONE_TYPE"));
			resBean.set("xdtime", bean.getStr("NODE_LIMIT_TIME"));
			newlist.add(resBean);
		}

		Bean dataBean = new Bean();
		dataBean.set("list", newlist);
		// 流程办理情况
		Bean chartData = new Bean();
		chartData.set("CompleteData", wfeCompleteChartData(wfBean, list));
		dataBean.set("chartData", chartData);

		// dataBean.set("imgURL", Context.getHttpUrl() + "/api/getWfeImg?noAuth=yes&servId=" +
		// servId);
		// dataBean.set("aniURL", Context.getHttpUrl() +
		// "/sy/comm/page/SY_WFE_TRACK_FIGURE.showWfeAniForMob.do?PI_ID=" + wfInst +
		// "&INST_IF_RUNNING=" + procRunning + "&S_FLAG=" + wfBean.getStr("S_FLAG"));

		outBean.setData(dataBean);
		return outBean;
	}

	// 得到流程办理情况
	private Bean wfeCompleteChartData(Bean dataBean, List<Bean> instList) {
		Bean rtnBean = new Bean();
		List<Bean> nodeAll = ServDao.finds("SY_WFE_NODE_DEF", "and PROC_CODE = '" + instList.get(0).getStr("PROC_CODE") + "' order by NODE_SORT");

		List<Bean> completeList = new ArrayList<Bean>();
		List<Bean> noCompleteList = new ArrayList<Bean>();
		Bean existTmp = new Bean();

		for (Bean inst : instList) {
			if (!existTmp.containsKey(inst.getStr("NODE_CODE"))) {
				existTmp.put(inst.getStr("NODE_CODE"), inst);
				completeList.add(new Bean().set("nodeName", inst.getStr("NODE_NAME")).set("beginTime", inst.getStr("NODE_BTIME")));
			}
		}

		for (Bean node : nodeAll) {
			if (!existTmp.containsKey(node.getStr("NODE_CODE"))) {
				noCompleteList.add(new Bean().set("nodeName", node.getStr("NODE_NAME")));
			}
		}

		float percentData = DataStatsUtil.divFunc(completeList.size() * 100, nodeAll.size());

		Bean doneInfo = GwTimeHelper.getNodeDoneInfo(nodeAll, instList, dataBean.getStr("TMPL_CODE"));

		rtnBean.set("nodeList", doneInfo.getList("NODE_LIST"));
		// rtnBean.set("nodeNameList", doneInfo.getList("nodeNameList"));
		rtnBean.set("percentData", percentData);
		rtnBean.set("totalTime", doneInfo.getDouble("totalTime"));
		rtnBean.set("currTime", doneInfo.getDouble("currTime"));
		// rtnBean.set("currNodeTimeList", doneInfo.getList("currNodeTimeList"));
		// rtnBean.set("avgNodeTimeList", doneInfo.getList("avgNodeTimeList"));

		return rtnBean;
	}

	/** 查询数据列表的方法 */
	private OutBean queryServ(String servId, ParamBean queryBean) {
		OutBean resultBean = new OutBean();

		// 参数信息
		int rowNum = queryBean.get(Constant.PAGE_SHOWNUM, 10);
		int pageNum = queryBean.get(Constant.PAGE_NOWPAGE, 1);

		// 数据
		List<Bean> dataList = ServDao.finds(servId, queryBean);

		// 分页信息
		int allNum = 0;
		if (rowNum <= 0) {// 没有分页的情况
			allNum = dataList.size();
		} else if ((pageNum == 1) && (dataList.size() < rowNum)) { // 数据量少，无需计算分页
			allNum = dataList.size();
		} else {
			ServDefBean servDef = ServUtils.getServDef(servId);
	        List<Object> preValue = getPreValueClone(queryBean);
	        String psql = Transaction.getBuilder().select(servDef, queryBean, preValue);
	        allNum = Transaction.getExecutor().count(psql, preValue);
			//allNum = ServDao.count(servId, queryBean);
		}
		PageBean page = new PageBean();
		page.setShowNum(rowNum);
		page.setNowPage(pageNum);
		page.setAllNum(allNum);
		// page.setPages(allNum);

		resultBean.setData(dataList);
		resultBean.setPage(page);
		return resultBean;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> getPreValueClone(Bean dataBean) {
        List<Object> preValue; //处理prevalue,存在就复制一份
        if (dataBean.contains(Constant.PARAM_PRE_VALUES)) {
            preValue = (List<Object>) ((ArrayList<Object>) dataBean.get(Constant.PARAM_PRE_VALUES)).clone();
        } else {
            preValue = new ArrayList<Object>();
        }
        return preValue;
    }
}
