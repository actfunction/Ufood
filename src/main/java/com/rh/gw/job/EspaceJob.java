package com.rh.gw.job;

import com.rh.gw.util.GwEspaceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * 客户端消息接口任务调度
 * 
 * @author kfzx-linll
 * @date 2018/11/22
 */
public class EspaceJob implements Job {
	/** 记录日志 */
	private static Log log = LogFactory.getLog(EspaceJob.class);

	/**Espace表和服务名一致*/
	private static final String OA_GW_INTERFACE_ESPACE = "OA_GW_INTERFACE_ESPACE";


	/**
	 * 定时查询Espace表
	 *
	 * @param context
	 * @throws JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//String sql = "SELECT * FROM OA_GW_INTERFACE_ESPACE ie WHERE 1=1 AND ie.SEND_COUNT < 3 AND ie.S_FLAG = '1' ";
		//List<Bean> beans = Transaction.getExecutor().query(sql);

		// 调用工具类中发送消息的方法
		GwEspaceUtil.sendMessageAuto();
		log.debug("定时发送客户端消息成功！");

		//if (beans != null && beans.size() > 0) {
		//	log.debug("定时调度查询OA_GW_INTERFACE_ESPACE表成功！记录数为：" + beans.size());
		//} else {
		//	log.debug("定时调度查询OA_GW_INTERFACE_ESPACE表记录数为：" + beans.size());
		//}
	}
}
