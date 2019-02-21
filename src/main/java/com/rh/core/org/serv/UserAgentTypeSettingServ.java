package com.rh.core.org.serv;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.dict.DictMgr;

/**
 * 用户业务委托办理服务类（支持按业务类型分别委托）
 * 
 * @author wanglong
 */
public class UserAgentTypeSettingServ extends CommonServ {
	/** 用户委托事务类型-字典编码 */
	private static final String AGENT_TYPE_DICT = "SY_ORG_USER_AGT_TYPE";
	/** 用户委托事务类型详细-字典编码 */
	private static final String AGENT_TYPE_DETAIL_DICT = "SY_ORG_USER_AGT_TYPE_DETAIL";

	@Override
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		// 清除指定字典的缓存
		DictMgr.clearAllCache(AGENT_TYPE_DICT, false);
		DictMgr.clearAllCache(AGENT_TYPE_DETAIL_DICT, false);
	}

}