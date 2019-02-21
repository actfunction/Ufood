package com.rh.api.impl;

import java.util.ArrayList;
import java.util.List;

import com.rh.api.BaseApiServ;
import com.rh.api.serv.IFileApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.api.util.ApiConstant;

public class FileApiServImpl extends BaseApiServ implements IFileApiServ {

	@Override
	public ApiOutBean getFileListByDataId(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String dataId = reqData.getStr("dataId");
		if (reqData.isEmpty("dataId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		String sql = "and data_id = '" + dataId + "' order by FILE_SORT";
		List<Bean> list = ServDao.finds(ServMgr.SY_COMM_FILE, sql);
		List<Bean> newlist = new ArrayList<Bean>();
		for (Bean fileBean : list) {
			Bean resBean = new Bean();
			UserBean userBean = UserMgr.getUser(fileBean.getStr("S_USER"));
			/**
			 * fileId 文件ID fileName 文件名称 fileSize 文件大小 fileTime 文件日期 fileExt
			 * 文件扩展名 fileType 文件类型 fileURL 文件地址
			 */
			resBean.set("fileId", fileBean.getStr("FILE_ID"));
			resBean.set("fileName", fileBean.getStr("FILE_NAME"));
			resBean.set("fileDisName", fileBean.getStr("DIS_NAME"));
			resBean.set("fileCat", fileBean.getStr("FILE_CAT"));
			resBean.set("itemCode", fileBean.getStr("ITEM_CODE"));
			resBean.set("fileSize", fileBean.getStr("FILE_SIZE"));
			resBean.set("fileTime", fileBean.getStr("S_MTIME"));
			resBean.set("fileMemo", fileBean.getStr("FILE_MEMO"));
			resBean.set("fileExt",
					fileBean.getStr("FILE_NAME").split("\\.")[fileBean.getStr("FILE_NAME").split("\\.").length - 1]);
			resBean.set("fileType", fileBean.getStr("FILE_MTYPE"));
			resBean.set("fileURL", Context.getHttpUrl() + "/file/" + fileBean.getStr("FILE_ID"));
			resBean.set("target", fileBean.getStr("FILE_CAT"));
			resBean.set("uploadUser", fileBean.getStr("S_USER"));
			resBean.set("uploadUserName", fileBean.getStr("S_UNAME"));
			resBean.set("servId", fileBean.getStr("SERV_ID"));
			resBean.set("dataId", fileBean.getStr("DATA_ID"));
			resBean.set("wfNid", fileBean.getStr("WF_NI_ID"));
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
