package com.rh.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.api.BaseApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.serv.IMindApiServ;
import com.rh.api.util.ApiConstant;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.mind.MindServ;
import com.rh.core.comm.mind.MindUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
//import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;

public class MindApiServImpl extends BaseApiServ implements IMindApiServ {
	// 常用意见服务
	private static final String SY_COMM_MIND_USUAL = "SY_COMM_MIND_USUAL";

	/** 意见显示规则 部门 1 */
	public static final int MIND_RULE_DEPT = 1;

	/** 意见显示规则 机构内 2 */
	public static final int MIND_RULE_ORG_INNER = 2;

	/** 意见显示规则 机构外 3 */
	public static final int MIND_RULE_ORG_OUTTER = 3;

	/** 意见显示规则 机构外 4 */
	public static final int MIND_RULE_ORG_TOP = 4;


	@Override
	public ApiOutBean getMindListByDataId(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("dataId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		String dataId = reqData.getStr("dataId");

		// 根据时间
		List<Bean> listOrderByTime = MindUtils.getMindList(null, dataId, MindServ.MIND_SORT_TIME);
		List<Bean> newlist_time = new ArrayList<Bean>();
		for (Bean bean : listOrderByTime) {
			Bean dataBean = new Bean();
			UserBean user = UserMgr.getUser(bean.getStr("S_USER"));
			dataBean.set("mindId", bean.getStr("MIND_ID"));
			dataBean.set("mindContent", bean.getStr("MIND_CONTENT"));
			dataBean.set("mindType", bean.getStr("MIND_TYPE"));
			dataBean.set("mindDisRule", bean.getStr("MIND_DIS_RULE"));
			dataBean.set("mindCode", bean.getStr("MIND_CODE"));
			dataBean.set("mindCodeName", bean.getStr("MIND_CODE_NAME"));
			dataBean.set("mindTime", bean.getStr("S_MTIME"));
			dataBean.set("userCode", bean.getStr("S_USER"));
			dataBean.set("userName", bean.getStr("S_UNAME"));
			dataBean.set("userPost", user.getPost());
			dataBean.set("userImg", user.getImgSrc());
			dataBean.set("deptCode", bean.getStr("S_DEPT"));
			dataBean.set("deptName", bean.getStr("S_DNAME"));
			dataBean.set("wfNId", bean.getStr("WF_NI_ID"));
			dataBean.set("mindTypeName", bean.getStr("MIND_CODE_NAME"));
			dataBean.set("wfNName", bean.getStr("WF_NI_NAME"));
			dataBean.set("mindFile", bean.getStr("MIND_FILE"));
			dataBean.set("userAutoGraph", user.getStr("USER_AUTOGRAPH"));
			newlist_time.add(dataBean);
		}
		// 根据类型
		List<Bean> listOrderByType = MindUtils.getMindList(null, dataId, MindServ.MIND_SORT_TYPE);
		List<Bean> list_type = new ArrayList<Bean>();
		// Bean typeBean = new Bean();
		List<Bean> tmpList = new ArrayList<Bean>();
		Bean tmpBean = new Bean();
		String initMindCode = "";
		String initMindName = "";
		for (int i = 0; i < listOrderByType.size(); i++) {
			Bean dataBean = new Bean();
			UserBean user = UserMgr.getUser(listOrderByType.get(i).getStr("S_USER"));
			dataBean.set("mindId", listOrderByType.get(i).getStr("MIND_ID"));
			dataBean.set("mindContent", listOrderByType.get(i).getStr("MIND_CONTENT"));
			dataBean.set("mindType", listOrderByType.get(i).getStr("MIND_TYPE"));
			dataBean.set("mindDisRule", listOrderByType.get(i).getStr("MIND_DIS_RULE"));
			dataBean.set("mindCode", listOrderByType.get(i).getStr("MIND_CODE"));
			dataBean.set("mindCodeName", listOrderByType.get(i).getStr("MIND_CODE_NAME"));
			dataBean.set("mindTime", listOrderByType.get(i).getStr("S_MTIME"));
			dataBean.set("userCode", listOrderByType.get(i).getStr("S_USER"));
			dataBean.set("userName", listOrderByType.get(i).getStr("S_UNAME"));
			dataBean.set("userImg", user.getImgSrc());
			dataBean.set("userPost", user.getPost());
			dataBean.set("userAutoGraph", user.getStr("USER_AUTOGRAPH"));
			dataBean.set("deptCode", listOrderByType.get(i).getStr("S_DEPT"));
			dataBean.set("deptName", listOrderByType.get(i).getStr("S_DNAME"));
			dataBean.set("wfNId", listOrderByType.get(i).getStr("WF_NI_ID"));
			dataBean.set("mindTypeName", listOrderByType.get(i).getStr("MIND_CODE_NAME"));
			dataBean.set("wfNName", listOrderByType.get(i).getStr("WF_NI_NAME"));
			dataBean.set("mindFile", listOrderByType.get(i).getStr("MIND_FILE"));
			if (initMindCode.equals(listOrderByType.get(i).getStr("MIND_CODE"))) {
				if (i == listOrderByType.size() - 1) {
					// 最后一条数据
					tmpList.add(dataBean);
					tmpBean.set("name", initMindName);
					tmpBean.set("code", initMindCode);
					tmpBean.set("list", tmpList);
					// typeBean.set(initMindCode, tmpBean);
					list_type.add(tmpBean);
				} else {
					tmpList.add(dataBean);
				}
			} else {
				if (i == 0) {
					// 第一条数据
					tmpList.add(dataBean);
				} else {
					tmpBean.set("name", initMindName);
					tmpBean.set("code", initMindCode);
					tmpBean.set("list", tmpList);
					// typeBean.set(initMindCode, tmpBean);
					list_type.add(tmpBean);
					tmpBean = new Bean();
					tmpList = new ArrayList<Bean>();
					tmpList.add(dataBean);
				}
				initMindCode = listOrderByType.get(i).getStr("MIND_CODE");
				initMindName = listOrderByType.get(i).getStr("MIND_CODE_NAME");
				if (i == listOrderByType.size() - 1) {
					tmpBean.set("name", initMindName);
					tmpBean.set("list", tmpList);
					// typeBean.set(initMindCode, tmpBean);
					list_type.add(tmpBean);
				}
			}
		}

		// 根据部门
		List<Bean> listOrderByDept = getMindListByDept(dataId);
		List<Bean> list_dept = new ArrayList<Bean>();
		// Bean deptBean = new Bean();
		tmpList = new ArrayList<Bean>();
		tmpBean = new Bean();
		String initDeptCode = "";
		String initDeptName = "";
		for (int i = 0; i < listOrderByDept.size(); i++) {
			Bean dataBean = new Bean();
			UserBean user = UserMgr.getUser(listOrderByDept.get(i).getStr("S_USER"));
			dataBean.set("mindId", listOrderByDept.get(i).getStr("MIND_ID"));
			dataBean.set("mindContent", listOrderByDept.get(i).getStr("MIND_CONTENT"));
			dataBean.set("mindType", listOrderByDept.get(i).getStr("MIND_TYPE"));
			dataBean.set("mindDisRule", listOrderByType.get(i).getStr("MIND_DIS_RULE"));
			dataBean.set("mindCode", listOrderByType.get(i).getStr("MIND_CODE"));
			dataBean.set("mindCodeName", listOrderByType.get(i).getStr("MIND_CODE_NAME"));
			dataBean.set("mindTime", listOrderByDept.get(i).getStr("S_MTIME"));
			dataBean.set("userCode", listOrderByDept.get(i).getStr("S_USER"));
			dataBean.set("userName", listOrderByDept.get(i).getStr("S_UNAME"));
			dataBean.set("userPost", user.getPost());
			dataBean.set("userImg", user.getImgSrc());
			dataBean.set("userAutoGraph", user.getStr("USER_AUTOGRAPH"));
			dataBean.set("deptCode", listOrderByDept.get(i).getStr("S_DEPT"));
			dataBean.set("deptName", listOrderByDept.get(i).getStr("S_DNAME"));
			dataBean.set("wfNId", listOrderByDept.get(i).getStr("WF_NI_ID"));
			dataBean.set("mindTypeName", listOrderByType.get(i).getStr("MIND_CODE_NAME"));
			dataBean.set("wfNName", listOrderByType.get(i).getStr("WF_NI_NAME"));
			dataBean.set("mindFile", listOrderByType.get(i).getStr("MIND_FILE"));
			if (initDeptCode.equals(listOrderByDept.get(i).getStr("S_DEPT"))) {
				if (i == listOrderByType.size() - 1) {
					// 最后一条数据
					tmpList.add(dataBean);
					tmpBean.set("name", initDeptName);
					tmpBean.set("code", initDeptCode);
					tmpBean.set("list", tmpList);
					// deptBean.set(initDeptCode, tmpBean);
					list_dept.add(tmpBean);
				} else {
					tmpList.add(dataBean);
				}
			} else {
				if (i == 0) {
					// 第一条数据
					tmpList.add(dataBean);
				} else {
					tmpBean.set("name", initDeptName);
					tmpBean.set("code", initDeptCode);
					tmpBean.set("list", tmpList);
					// deptBean.set(initDeptCode, tmpBean);
					list_dept.add(tmpBean);
					tmpList = new ArrayList<Bean>();
					tmpBean = new Bean();
					tmpList.add(dataBean);
				}
				initDeptCode = listOrderByDept.get(i).getStr("S_DEPT");
				initDeptName = listOrderByDept.get(i).getStr("S_DNAME");
				if (i == listOrderByType.size() - 1) {
					tmpBean.set("name", initDeptName);
					tmpBean.set("list", tmpList);
					// deptBean.set(initDeptCode, tmpBean);
					list_dept.add(tmpBean);
				}
			}
		}

		Bean resBean = new Bean();
		resBean.set("timeType", newlist_time);
		// resBean.set("mindType", typeBean);
		// resBean.set("deptType", deptBean);
		resBean.set("mindType", list_type);
		resBean.set("deptType", list_dept);
		outBean.setData(resBean);
		return outBean;
	}

	/**
	 * 根据部门返回意见列表
	 * 
	 * @param dataId
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private List<Bean> getMindListByDept(String dataId) {
		if (StringUtils.isEmpty(dataId)) {
			return new ArrayList<Bean>();
		}
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_ORDER, "S_DEPT,MIND_TIME DESC");
		queryBean.set("DATA_ID", dataId);
		// return ServDao.finds("SY_COMM_MIND", queryBean);
		List<Bean> list = (List<Bean>) ServMgr.act("SY_COMM_MIND", ServMgr.ACT_FINDS, queryBean).getData();
		return list;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean inputMind(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		UserBean userBean = Context.getUserBean();
		String mindId = reqData.getStr("mindId");
		boolean addFlag = reqData.getBoolean("_ADD_");
		String mindContent = reqData.getStr("mindContent");
		String mindCode = reqData.getStr("mindCode");
		String userCode = userBean.getCode();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String wfNId = reqData.getStr("wfNId");
		// String wfPId = reqData.getStr("wfPId");

		if (reqData.isEmpty("mindContent") || reqData.isEmpty("mindType") || reqData.isEmpty("servId")
				|| reqData.isEmpty("dataId") || reqData.isEmpty("wfNId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		/*
		 * 数据例子 {DATA_ID=0ez18mPz2t1Qa3Furz5MTtA, serv=SY_COMM_MIND, S_FLAG=2,
		 * _PK_=, MIND_CONTENT=sss, SERV_ID=OA_APPLY,
		 * WF_NI_ID=1pk6STKAtaIpTe1JLWaSpj, WF_NI_NAME=部门审核,
		 * MIND_CODE_NAME=领导意见, MIND_CODE=SD-0001, act=save, MIND_TYPE=1,
		 * MIND_DIS_RULE=3, _TRANS_=false}
		 */
		Bean dataBean = new Bean();
		if (reqData.isNotEmpty("mindId")) {
			dataBean.set("MIND_ID", mindId);
			dataBean.setId(mindId);
		}

		if (addFlag) {
			dataBean.set("_ADD_", addFlag);
		}
		dataBean.set("DATA_ID", dataId);
		dataBean.set("SERV_ID", servId);
		dataBean.set("S_USER", userCode);
		dataBean.set("MIND_CONTENT", mindContent);
		dataBean.set("MIND_FILE", reqData.getStr("mindFile"));
		dataBean.set("WF_NI_ID", wfNId);
		dataBean.set("WF_NI_NAME",
				ServMgr.act("SY_WFE_NODE_INST", ServMgr.ACT_BYID, new ParamBean().setId(wfNId)).getStr("NODE_NAME"));// 流程节点名称
		dataBean.set("MIND_CODE", mindCode);
		Bean codeBean = ServMgr.act("SY_COMM_MIND_CODE", ServMgr.ACT_BYID, new ParamBean().setId(mindCode));
		dataBean.set("MIND_CODE_NAME", codeBean.getStr("CODE_NAME"));
		dataBean.set("MIND_TYPE", 1);// 1:文字意见;2:手写意见
		dataBean.set("MIND_DIS_RULE", codeBean.getStr("MIND_DIS_RULE"));// MIND_DIS_RULE:意见显示规则：1,部门内可见,2,机构内可见,3,机构外可见
		Bean saveBean = ServMgr.act("SY_COMM_MIND", ServMgr.ACT_SAVE, dataBean);
		Bean resBean = new Bean();
		resBean.set("mindId", saveBean.getStr("MIND_ID"));
		resBean.set("mindContent", saveBean.getStr("MIND_CONTENT"));
		resBean.set("mindType", saveBean.getStr("MIND_TYPE"));
		resBean.set("mindCode", saveBean.getStr("MIND_CODE"));
		resBean.set("mindCodeName", saveBean.getStr("MIND_CODE_NAME"));
		resBean.set("mindTypeName", saveBean.getStr("MIND_CODE_NAME"));
		resBean.set("mindDisRule", saveBean.getStr("MIND_DIS_RULE"));
		resBean.set("mindTime", saveBean.getStr("S_MTIME"));
		resBean.set("userName", saveBean.getStr("S_UNAME"));
		resBean.set("userCode", saveBean.getStr("S_USER"));
		resBean.set("userPost", userBean.getPost());
		resBean.set("deptCode", saveBean.getStr("S_DEPT"));
		resBean.set("deptName", saveBean.getStr("S_DNAME"));
		resBean.set("mindFile", saveBean.getStr("MIND_FILE"));
		resBean.set("wfNId", saveBean.getStr("WF_NI_ID"));
		resBean.set("wfNName", saveBean.getStr("WF_NI_NAME"));
		resBean.set("userImg", userBean.getImgSrc());
		resBean.set("userAutoGraph", userBean.getStr("USER_AUTOGRAPH"));
		
		outBean.setData(resBean);
		return outBean;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ApiOutBean getOftenUseMindList(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		ParamBean whereBean = new ParamBean();
		whereBean.set("S_USER", Context.getUserBean().getCode());
		whereBean.set("_SELECT_", "MIND_CONTENT");
		// List<Bean> list = ServDao.finds(SY_COMM_MIND_USUAL, whereBean);
		List<Bean> list = (List<Bean>) ServMgr.act("SY_COMM_MIND_USUAL", ServMgr.ACT_FINDS, whereBean).getData();
		List<Bean> newlist = new ArrayList<Bean>();
		for (Bean bean : list) {
			Bean dataBean = new Bean();
			dataBean.set("mindContent", bean.getStr("MIND_CONTENT"));
			newlist.add(dataBean);
		}
		Bean resBean = new Bean();
		resBean.set("list", newlist);
		outBean.setData(resBean);
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean addOftenMind(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String mindContent = reqData.getStr("mindContent");
		String mindTime = reqData.getStr("mindTime");
		Bean dataBean = new Bean();
		dataBean.set("MIND_CONTENT", mindContent);
		dataBean.set("S_MTIME", mindTime);
		dataBean.set("S_USER", Context.getUserBean().getCode());
		// ServDao.create(SY_COMM_MIND_USUAL, dataBean);
		ServMgr.act(SY_COMM_MIND_USUAL, ServMgr.ACT_SAVE, dataBean);
		return outBean;
	}

	@Override
	public ApiOutBean getDiscuss(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		ParamBean whereBean = new ParamBean();
		UserBean userBean = Context.getUserBean();
		String nodes = reqData.getStr("nodes").replaceAll(",", "','");
		String strWhere = " and S_TDEPT = '" + userBean.getTDeptCode() + "' and NODE_CODE in ('" + nodes
				+ "') and (IS_USED = 0 or (IS_USED = 1 and NI_ID = '" + reqData.getStr("nid") + "')) and DATA_ID = '"
				+ reqData.getStr("dataId") + "'";
		whereBean.setWhere(strWhere);
		whereBean.setOrder("S_ATIME desc");
		List<Bean> disList = ServDao.finds("OA_COMMON_MIND_DISCUSS", whereBean);
		if (disList != null && disList.size() > 0) {
			outBean.setData(disList.get(0));
		} else {
			outBean.setData(new Bean());
		}
		return outBean;
	}

	@Override
	public ApiOutBean disToMind(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		String disId = null;

		if (reqData.isNotEmpty("mindId")) {
			disId = reqData.getStr("mindId");
			ParamBean disBean = new ParamBean();
			disBean.setId(disId);
			disBean.set("DIS_CONTENT", reqData.getStr("mindContent"));
			ServMgr.act("OA_COMMON_MIND_DISCUSS", ServMgr.ACT_SAVE, disBean);
		} else {
			disId = Lang.getUUID();
			ParamBean disBean = new ParamBean();
			disBean.setId(disId);
			disBean.set("DIS_ID", disId);
			disBean.setAddFlag(true);
			disBean.set("DIS_CONTENT", reqData.getStr("mindContent"));
			disBean.set("DATA_ID", reqData.getStr("dataId"));
			disBean.set("NI_ID", reqData.getStr("wfNId"));
			disBean.set("PI_ID", reqData.getStr("PI_ID"));
			disBean.set("IS_USED", 1);
			disBean.set("NODE_CODE", reqData.getStr("NODE_CODE"));
			ServMgr.act("OA_COMMON_MIND_DISCUSS", ServMgr.ACT_SAVE, disBean);
		}

		ServDao.updates("OA_COMMON_MIND_DISCUSS", new Bean().set("IS_USED", 1),
				new Bean().set("DATA_ID", reqData.getStr("dataId")).set("IS_USED", 0));

		Bean mindBean = ServDao.find("SY_COMM_MIND", disId);
		if (mindBean == null) {
			reqData.set("mindId", disId);
			reqData.set("_ADD_", true);
		} else {
			reqData.set("_ADD_", false);
		}
		outBean = this.inputMind(reqData);

		return outBean;
	}
	
	public ApiOutBean getMindListByDataIdForRule(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		if (reqData.isEmpty("dataId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		String dataId = reqData.getStr("dataId");
		String servId = reqData.getStr("servId");
		UserBean userBean = Context.getUserBean();
		// 根据时间
		Bean params = new Bean();
		params.set(Constant.PARAM_ORDER, " MIND_TIME DESC");
		StringBuilder mindWhere = new StringBuilder();
		mindWhere.append(" and ((MIND_DIS_RULE =");
		mindWhere.append(MIND_RULE_DEPT);
		mindWhere.append(" and S_TDEPT = '");
		mindWhere.append(userBean.getTDeptCode());
		mindWhere.append("') or MIND_DIS_RULE = ");
		mindWhere.append(MIND_RULE_ORG_OUTTER);
		mindWhere.append(" or MIND_DIS_RULE = ");
		mindWhere.append(MIND_RULE_ORG_TOP);
		mindWhere.append(" or (MIND_DIS_RULE =");
		mindWhere.append(MIND_RULE_ORG_INNER);
		mindWhere.append(" and S_ODEPT = '");
		mindWhere.append(userBean.getODeptCode());
		mindWhere.append("')) and SERV_ID = '");
		mindWhere.append(servId);
		mindWhere.append("' and DATA_ID = '");
		mindWhere.append(dataId);
		mindWhere.append("'");
		params.set(Constant.PARAM_WHERE, mindWhere.toString());
		List<Bean> listOrderByTime = this.getMindListForTime(dataId, servId);

		List<Bean> newlist_time = new ArrayList<Bean>();
		List<Bean> list_type = new ArrayList<Bean>();
		List<Bean> list_dept = new ArrayList<Bean>();

		if (listOrderByTime == null || listOrderByTime.size() <= 0) {
			Bean resBean = new Bean();
			resBean.set("timeType", newlist_time);
			resBean.set("mindType", list_type);
			resBean.set("deptType", list_dept);
			outBean.setData(resBean);
			return outBean;
		}

		for (Bean bean : listOrderByTime) {
			Bean dataBean = this.parseMindBean(bean);
			newlist_time.add(dataBean);
		}

		// 根据类型
		StringBuilder codeStrs = new StringBuilder();
		for (Bean codeBean : listOrderByTime) {
			codeStrs.append("'");
			codeStrs.append(codeBean.getStr("MIND_CODE"));
			codeStrs.append("',");
		}
		if (codeStrs.length() > 1) {
			codeStrs.append("''");
		}

		Bean sortQuery = new Bean();
		String mindCodeWhere = " and CODE_ID in (" + codeStrs.toString() + ")";
		sortQuery.set(Constant.PARAM_WHERE, mindCodeWhere);
		sortQuery.set(Constant.PARAM_ORDER, " CODE_SORT asc");
		List<Bean> codeMindList = ServDao.finds("SY_COMM_MIND_CODE", sortQuery);

		for (Bean codeMindBean : codeMindList) {
			List<Bean> tmpList = new ArrayList<Bean>();
			Bean tmpBean = new Bean();
			tmpBean.set("name", codeMindBean.getStr("CODE_NAME"));
			tmpBean.set("code", codeMindBean.getStr("CODE_ID"));
			for (Bean mindBean : listOrderByTime) {
				if (codeMindBean.getStr("CODE_ID").equalsIgnoreCase(mindBean.getStr("MIND_CODE"))) {
					Bean dataBean = this.parseMindBean(mindBean);
					tmpList.add(dataBean);
				}
			}
			tmpBean.set("list", tmpList);
			list_type.add(tmpBean);
		}

		// 根据部门
		StringBuilder deptStrs = new StringBuilder();
		for (Bean deptBean : listOrderByTime) {
			deptStrs.append("'");
			deptStrs.append(deptBean.getStr("S_DEPT"));
			deptStrs.append("',");
		}
		if (deptStrs.length() > 1) {
			deptStrs.append("''");
		}
		Bean sortDeptQuery = new Bean();
		String deptWhere = " and DEPT_CODE in (" + deptStrs.toString() + ")";
		sortDeptQuery.set(Constant.PARAM_WHERE, deptWhere);
		sortDeptQuery.set(Constant.PARAM_ORDER, " DEPT_LEVEL, DEPT_SORT asc");
		List<Bean> deptMindList = ServDao.finds("SY_ORG_DEPT", sortDeptQuery);
		for (Bean deptMindBean : deptMindList) {
			List<Bean> tmpList = new ArrayList<Bean>();
			Bean tmpBean = new Bean();
			tmpBean.set("name", deptMindBean.getStr("DEPT_NAME"));
			tmpBean.set("code", deptMindBean.getStr("DEPT_CODE"));
			for (Bean mindBean : listOrderByTime) {
				if (deptMindBean.getStr("DEPT_CODE").equalsIgnoreCase(mindBean.getStr("S_DEPT"))) {
					Bean dataBean = this.parseMindBean(mindBean);
					tmpList.add(dataBean);
				}
			}
			tmpBean.set("list", tmpList);
			list_dept.add(tmpBean);
		}
		Bean resBean = new Bean();
		resBean.set("timeType", newlist_time);
		resBean.set("mindType", list_type);
		resBean.set("deptType", list_dept);
		outBean.setData(resBean);

		return outBean;
	}

	public static Bean parseMindBean(Bean mind) {
		Bean dataBean = new Bean();
		UserBean user = UserMgr.getUser(mind.getStr("S_USER"));
		dataBean.set("mindId", mind.getStr("MIND_ID"));
		dataBean.set("mindContent", mind.getStr("MIND_CONTENT"));
		dataBean.set("mindType", mind.getStr("MIND_TYPE"));
		dataBean.set("mindDisRule", mind.getStr("MIND_DIS_RULE"));
		dataBean.set("mindCode", mind.getStr("MIND_CODE"));
		dataBean.set("mindCodeName", mind.getStr("MIND_CODE_NAME"));
		dataBean.set("mindTime", mind.getStr("MIND_TIME"));
		dataBean.set("userCode", mind.getStr("S_USER"));
		dataBean.set("userName", mind.getStr("S_UNAME"));
		dataBean.set("userPost", user.getPost());
		dataBean.set("userImg", user.getImgSrc());
		dataBean.set("deptCode", mind.getStr("S_DEPT"));
		dataBean.set("deptName", mind.getStr("S_DNAME"));
		dataBean.set("wfNId", mind.getStr("WF_NI_ID"));
		dataBean.set("mindTypeName", mind.getStr("MIND_CODE_NAME"));
		dataBean.set("wfNName", mind.getStr("WF_NI_NAME"));
		dataBean.set("mindFile", mind.getStr("MIND_FILE"));
		dataBean.set("userAutoGraph", user.getStr("USER_AUTOGRAPH"));
		dataBean.set("isBD", mind.getStr("IS_BD"));
		dataBean.set("bdUser", mind.getStr("BD_USER"));
		dataBean.set("bdUserName", mind.getStr("BD_UNAME"));
		dataBean.set("group", mind.getStr("PRE_NI_ID"));
		return dataBean;
	}

	public static List<Bean> getMindListForTime(String dataId, String servId) {
		UserBean userBean = Context.getUserBean();
		// 根据时间
		Bean params = new Bean();
		params.set(Constant.PARAM_TABLE, "OA_COMM_MIND_WFNI_V");
		params.set(Constant.PARAM_ORDER, " MIND_TIME DESC");
		params.set(Constant.PARAM_SELECT, "*");
		StringBuilder mindWhere = new StringBuilder();
		mindWhere.append(" and ((MIND_DIS_RULE =");
		mindWhere.append(MIND_RULE_DEPT);
		mindWhere.append(" and S_TDEPT = '");
		mindWhere.append(userBean.getTDeptCode());
		mindWhere.append("') or MIND_DIS_RULE = ");
		mindWhere.append(MIND_RULE_ORG_OUTTER);
		mindWhere.append(" or MIND_DIS_RULE = ");
		mindWhere.append(MIND_RULE_ORG_TOP);
		mindWhere.append(" or (MIND_DIS_RULE =");
		mindWhere.append(MIND_RULE_ORG_INNER);
		mindWhere.append(" and S_ODEPT = '");
		mindWhere.append(userBean.getODeptCode());
		mindWhere.append("')) and SERV_ID = '");
		mindWhere.append(servId);
		mindWhere.append("' and DATA_ID = '");
		mindWhere.append(dataId);
		mindWhere.append("'");
		params.set(Constant.PARAM_WHERE, mindWhere.toString());
		List<Bean> mindList = ServDao.finds("SY_COMM_MIND", params);

		return mindList;
	}
}
