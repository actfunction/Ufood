package com.rh.serv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.comm.CacheMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.msg.MsgModifyUtil;

public class MsgJobListener extends CommonServ{
	 protected static Log log = LogFactory.getLog(MsgJobListener.class);
		/**
		 * 地址：HOST/icbc/oa/MsgJobListener.cleanUserDictCache.do?USER_CODE=0000956635
		 * 取得当前用户的设备列表
		 */
		public OutBean cleanUserDictCache(ParamBean paramBean) {
			OutBean outBean = new OutBean();
			try {
				String userCode = paramBean.getStr("USER_CODE");
				if ("".equals(userCode)||userCode.isEmpty()) {
					outBean.setError("null user_code");
				}else {
					CacheMgr.getInstance().remove(userCode, UserMgr.CACHE_TYPE_USER);
					outBean.setOk();
				}
			}catch(Exception e) {
				outBean.setError("cleanUserDictCache ERROR"+e.getMessage());
				log.error("cleanUserDictCache ERROR"+e.getMessage());
			}
			return outBean;
		}
		
		public OutBean cleanDeptDictCache(ParamBean paramBean) {
			OutBean outBean = new OutBean();
			try {
				MsgModifyUtil.cleanDeptDictCache();
				outBean.setOk();
				log.info("cleanUserDictCache success");
			}catch(Exception e) {
				outBean.setError("cleanDeptDictCache ERROR"+e.getMessage());
				log.error("cleanDeptDictCache ERROR"+e.getMessage());
			}
			return outBean;
		}
}
