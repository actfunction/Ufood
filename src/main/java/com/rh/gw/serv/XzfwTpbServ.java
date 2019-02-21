package com.rh.gw.serv;

import com.rh.core.comm.FileMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.gw.util.GwExtTabUtils;
import com.rh.gw.util.GwOfdUtil;

/**
 * 行政发文特派办扩展类
 * 
 * @author kfzx-huangshuai
 *
 */
public class XzfwTpbServ extends GwExtServ {
	/**
	 * 根据前后页签的不同来删除数据库
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean deleteRalateTab(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.deleteRalateTab(paramBean);
	}

	/**
	 * 根据流程实例ID和节点ID获得自定义变量
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean getTabs(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.getTabs(paramBean);
	}

	/**
	 * DOC文件转OFD
	 *
	 * @param paramBean
	 *            传入参数 需要包含 Bean: zhengwen 和 zhengwen的fileId
	 * @return
	 */
	public OutBean cov(ParamBean paramBean) {
		// 获得正文ID
		String fileId = paramBean.getBean("zhengwen").getStr("fileId");
		// 调用转OFD方法
		OutBean cov = GwOfdUtil.cov(paramBean);
		// 将原文件信息迁入历史版本表中
		FileMgr.hongTouFileSave(fileId);
		// FileMgr.deleteFile(fileId);
		return cov;
	}
}
