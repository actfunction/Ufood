package com.rh.msg.job;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;

public class MsgOrgPathMinJob extends RhJob{
	
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgOrgPathMinJob.class);
	@Override
	protected void executeJob(RhJobContext arg0) {
		log.info("MsgOrgPathMinJob start ");
		//修复特殊机构
		MsgModifyUtil.modifySpecialDept();
				
		int minusHour = Context.getSyConf("OA_ORG_CODE_MINUS_HOUR", -1);
		int count = 1;
		try {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR, minusHour);
			String date =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
			SqlBean sqlBean = new SqlBean();
			sqlBean.appendWhere(" and to_timestamp(s_mtime,'yyyy-MM-dd HH24:mi:ss') > to_timestamp(?,'yyyy-MM-dd HH24:mi:ss')", date);
			count = ServDao.count("SYS_AUTH_ORG", sqlBean);
			log.info("MsgOrgPathMinJob count :"+ count);
		}catch(Exception e) {
			log.error("MsgOrgPathMinJob error "+e.getMessage());
		}
		if (count >0) {
			String root = Context.getSyConf("OA_MSG_ROOTORG", "cnao0001");
			if ("0".equals(root)) {
				MsgModifyUtil.modifyCodePathByRoot();
			}else {
				MsgModifyUtil.modifyCodePathByRoot(root);
			}
			MsgModifyUtil.cleanAllServDeptCache();
		}
		log.info("MsgOrgPathMinJob end ");

	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

}
