package com.rh.api.impl;

import java.util.ArrayList;
import java.util.List;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.serv.IGwApiServ;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.workday.WorkTime;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

public class GwApiServImpl implements IGwApiServ {

	@Override
	public ApiOutBean getMaxNum(Bean reqData) {

		ApiOutBean result = new ApiOutBean();

		String dataId = reqData.getStr("dataId");
		String servId = reqData.getStr("servId");
		String gwYearCode = reqData.getStr("gwYearCode");
		String gwYear = reqData.getStr("gwYear");
		String gwYearNumber = reqData.getStr("gwYearNumber");

		ParamBean saveBean = new ParamBean();
		saveBean.setId(dataId);
		saveBean.set("GW_YEAR_CODE", gwYearCode);
		saveBean.set("GW_YEAR", gwYear);

		if (reqData.isEmpty("gwYearNumber")) {
			Bean param = new Bean();
			param.set("GW_YEAR_CODE", gwYearCode);
			param.set("GW_YEAR", gwYear);
			param.set(Constant.PARAM_SELECT, "max(GW_YEAR_NUMBER) MAX_").setId(""); // 清除ID保查询
			Bean maxBean = ServDao.find(servId, param);
			int maxNum = 0;
			if (maxBean != null) {
				maxNum = maxBean.getInt("MAX_") + 1;
			} else {
				maxNum = 1;
			}
			saveBean.set("GW_YEAR_NUMBER", maxNum);
			result.setData(new Bean().set("maxNum", maxNum));
		} else {
			saveBean.set("GW_YEAR_NUMBER", gwYearNumber);
			result.setData(new Bean().set("maxNum", gwYearNumber));
		}

		saveBean.set("GW_FULL_CODE", gwYearCode + "(" + gwYear + ")" + saveBean.getStr("GW_YEAR_NUMBER"));

		ServMgr.act(servId, ServMgr.ACT_SAVE, saveBean);

		return result;
	}

	@Override
	public ApiOutBean cmRedHead(Bean reqData) {

		ApiOutBean result = new ApiOutBean();
		String odept = reqData.getStr("ODEPT_CODE");
		String servId = reqData.getStr("servId");
		String gwYearCode = reqData.getStr("gwYearCode");

		ParamBean param = new ParamBean();
		param.setTable("OA_COMMON_CODE_CW_TMPL_V");
		param.set("CODE_NAME", gwYearCode);
		param.set("CODE_BELONG_ODEPT", odept);
		param.setSelect("*");
		Bean cwTmplBean = ServDao.find(servId, param);
		String modelID = "";
		if (cwTmplBean != null) {
			String cwTmplFeild = cwTmplBean.getStr("CW_FILE_PATH");
			if (cwTmplFeild != null && cwTmplFeild.length() > 0) {
				modelID = cwTmplFeild.split(",")[0];
			}
		}

		result.setData(new Bean().set("CW_TMPL_ID", modelID));

		return result;
	}

	public ApiOutBean getWfeDelayInfo(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String dataId = reqData.getStr("dataId");
		String servId = reqData.getStr("servId");
		String pid = reqData.getStr("pid");
		String fileType = reqData.getStr("fileType");
		String fileChildType = reqData.getStr("fileChildType");
		String emergency = reqData.getStr("emergency");
		int ifRunning = reqData.getInt("ifRunning");

		if (ifRunning == 1) {

			Bean param = new Bean();
			param.set("GW_TMPL", servId);
			param.set(Constant.PARAM_SELECT, "*");
			List<Bean> ruleList = ServDao.finds("OA_TIME_LIMIT_RULES", param);

			Bean ruleTimeMap = new Bean();
			for (Bean rule : ruleList) {
				ruleTimeMap.set(rule.getStr("FILE_TYPE") + rule.getStr("FILE_CHILD_TYPE") + rule.getStr("SCENE_ROLE")
						+ rule.getStr("EMERGENCY"), rule.getInt("TIME_LIMIT"));
			}

			OutBean recordOutBean = ServMgr.act("OA_GW_DELAY_RECORD", ServMgr.ACT_DELETE,
					new ParamBean().setWhere(" and DATA_ID = '" + dataId + "'"));

			Bean recordMap = new Bean();

			if (recordOutBean.getDataList() != null && recordOutBean.getDataList().size() > 0) {
				for (Bean record : recordOutBean.getDataList()) {
					String key = record.getStr("S_TDEPT") + record.getStr("SCENE_ROLE");
					if (record.getInt("IS_DELAY") == 1) {
						recordMap.set(key, record.getInt("IS_DELAY"));
					}

				}
			}

			List<Bean> wfeHisList = ServDao.finds("SY_WFE_NODE_INST",
					" and PI_ID = '" + pid + "' and NODE_CHILD_TYPE != 0");
			WorkTime worktime = new WorkTime();
			for (Bean wfeHis : wfeHisList) {
				ParamBean recordBean = new ParamBean();
				recordBean.set("SERV_ID", servId);
				recordBean.set("DATA_ID", dataId);
				recordBean.set("NID", wfeHis.getStr("NI_ID"));
				recordBean.set("PID", pid);
				recordBean.set("NODE_CODE", wfeHis.getStr("NODE_CODE"));
				UserBean doneUser = UserMgr.getUser(wfeHis.getStr("TO_USER_ID"));
				recordBean.set("S_USER", doneUser.getId());
				recordBean.set("S_DEPT", doneUser.getDeptCode());
				recordBean.set("S_ODEPT", doneUser.getODeptCode());
				recordBean.set("S_TDEPT", doneUser.getTDeptCode());
				recordBean.set("S_CMPY", doneUser.getCmpyCode());
				recordBean.set("BEGIN_TIME", wfeHis.getStr("NODE_BTIME"));
				String recordKey = doneUser.getTDeptCode() + wfeHis.getInt("NODE_CHILD_TYPE");
				String ruleKey = fileType + fileChildType + wfeHis.getInt("NODE_CHILD_TYPE") + emergency;
				long useTime = 0;
				if (wfeHis.isNotEmpty("NODE_ETIME")) {
					useTime = wfeHis.getLong("NODE_DAYS");
					recordBean.set("DONE_TIME", wfeHis.getStr("NODE_DAYS"));
					recordBean.set("END_TIME", wfeHis.getStr("NODE_ETIME"));
				} else {
					String endTime = DateUtils.getDatetime();
					useTime = worktime.calWorktime("", wfeHis.getStr("NODE_BTIME"), endTime);
					recordBean.set("DONE_TIME", useTime);
					recordBean.set("END_TIME", endTime);
				}
				if (recordMap.containsKey(recordKey)) {
					recordBean.set("IS_DELAY", recordMap.getInt(recordKey));
				} else {
					if (useTime > (ruleTimeMap.getInt(ruleKey) * 60)) {
						recordBean.set("IS_DELAY", 2);
					} else {
						recordBean.set("IS_DELAY", 0);
					}
				}

				recordBean.set("REG_TIME", ruleTimeMap.getInt(ruleKey) * 60);
				recordBean.set("DEPT_NAME", doneUser.getDeptName());
				recordBean.set("TDEPT_NAME", doneUser.getTDeptName());
				recordBean.set("ODEPT_NAME", doneUser.getODeptName());
				recordBean.set("ODEPT_NAME", doneUser.getODeptName());
				recordBean.set("SCENE_ROLE", wfeHis.getInt("NODE_CHILD_TYPE"));
				recordBean.set("DEPT_SORT", doneUser.getTDeptBean().getSort());

				ServMgr.act("OA_GW_DELAY_RECORD", ServMgr.ACT_SAVE, recordBean);
			}
		}

		List<Bean> recordList = Transaction.getExecutor()
				.query("select  (reg_time - SUM (done_time))/60 DONE_TIME,S_TDEPT,TDEPT_NAME,IS_DELAY,SCENE_ROLE "
						+ "from OA_GW_DELAY_RECORD where DATA_ID = '" + dataId
						+ "' GROUP BY S_TDEPT, TDEPT_NAME, IS_DELAY,reg_time,SCENE_ROLE "
						+ "ORDER BY SCENE_ROLE,TDEPT_NAME DESC");

		outBean.setData(new Bean().set("recordList", recordList));

		return outBean;
	}

	public ApiOutBean saveDelayInfo(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		String dataId = reqData.getStr("DATA_ID");
		String tDept = reqData.getStr("S_TDEPT");
		String sceneRole = reqData.getStr("SCENE_ROLE");
		int isDelay = reqData.getInt("IS_DELAY");

		String sql = "update OA_GW_DELAY_RECORD set IS_DELAY = " + isDelay + " where DATA_ID = '" + dataId
				+ "' and S_TDEPT = '" + tDept + "' and SCENE_ROLE = " + sceneRole;

		int count = Transaction.getExecutor().execute(sql);

		outBean.setData(new Bean().set("count", count));

		return outBean;
	}

	public ApiOutBean getZhengwenList(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		String dataId = reqData.getStr("dataId");

		String sql = "and data_id = '" + dataId + "' and FILE_CAT = 'ZHENGWEN'";
		List<Bean> list = ServDao.finds(ServMgr.SY_COMM_FILE, sql);

		List<Bean> newlist = new ArrayList<Bean>();
		for (Bean bean : list) {
			Bean resBean = new Bean();
			UserBean userBean = UserMgr.getUser(bean.getStr("S_USER"));
			/**
			 * fileId 文件ID fileName 文件名称 fileSize 文件大小 fileTime 文件日期 fileExt
			 * 文件扩展名 fileType 文件类型 fileURL 文件地址
			 */
			resBean.set("fileId", bean.getStr("FILE_ID"));
			resBean.set("fileName", bean.getStr("FILE_NAME"));
			resBean.set("fileDisName", bean.getStr("DIS_NAME"));
			resBean.set("fileCat", bean.getStr("FILE_CAT"));
			resBean.set("itemCode", bean.getStr("ITEM_CODE"));
			resBean.set("fileSize", bean.getStr("FILE_SIZE"));
			resBean.set("fileTime", bean.getStr("S_MTIME"));
			resBean.set("fileExt",
					bean.getStr("FILE_NAME").split("\\.")[bean.getStr("FILE_NAME").split("\\.").length - 1]);
			resBean.set("fileType", bean.getStr("FILE_MTYPE"));
			resBean.set("fileURL", Context.getHttpUrl() + "/file/" + bean.getStr("FILE_ID"));
			resBean.set("target", bean.getStr("FILE_CAT"));
			resBean.set("uploadUser", bean.getStr("S_USER"));
			resBean.set("uploadUserName", bean.getStr("S_UNAME"));
			resBean.set("servId", bean.getStr("SERV_ID"));
			resBean.set("dataId", bean.getStr("DATA_ID"));
			resBean.set("wfNid", bean.getStr("WF_NI_ID"));
			resBean.set("userPost", userBean.getPost());
			resBean.set("deptCode", userBean.getDeptCode());
			resBean.set("deptName", userBean.getDeptName());
			resBean.set("userImg", userBean.getImgSrc());
			newlist.add(resBean);
		}
		Bean dataBean = new Bean();
		dataBean.set("list", newlist);
		outBean.setData(dataBean);
		return outBean;
	}
}
