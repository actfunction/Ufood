package com.rh.gw.job;

import java.util.List;

import com.rh.gw.util.GwEspaceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;


/**
 * 审理司审理时效任务调度
 * 
 * @author kfzx-linll
 * @date 2018/11/22
 */
public class ShiXiaoJob implements Job {
	// 记录日志
	private static Log log = LogFactory.getLog(ShiXiaoJob.class);

	// 时效表和服务一致
	private static final String OA_GW_GONGWEN_PRESCRIPTION = "OA_GW_GONGWEN_PRESCRIPTION";

	// 定时调度-审理时效表和服务名一致
	private static final String OA_GW_TIME_PRESCRIPTION = "OA_GW_TIME_PRESCRIPTION";


	/**
	 * 定时查询 审理时效中间表
	 *
	 * @param context
	 * @throws JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String sql = "SELECT * FROM " + OA_GW_TIME_PRESCRIPTION + " tp WHERE 1=1 AND tp.SEND_COUNT < 3 AND tp.S_FLAG = '1' ";
		List<Bean> beans = Transaction.getExecutor().query(sql);

		Bean bean = new Bean();

		// 遍历查询出来的发送次数小于3的公文
		for (Bean sqlBean : beans) {
			
			// 获取时效
			Bean shiXiaoBean = ServDao.find(OA_GW_GONGWEN_PRESCRIPTION, bean.getStr("TIME_ID"));
			int shiXiao = Integer.parseInt(shiXiaoBean.getStr("PRE_ID"));
			
			if ((Integer.parseInt(DateUtils.getTime()) - Integer.parseInt(sqlBean.getStr("START_TIME"))) > shiXiao) {

				// 调用工具类中发送消息的方法
				OutBean outbean = GwEspaceUtil.sendMessageAuto();
				if ("ok".equalsIgnoreCase((String) outbean.get("msg"))) {
					
					log.debug("发送成功！");
				}

				// 发送失败,则修改OA_GW_TIME_PRESCRIPTION表的发送次数字段 + 1
				sqlBean.set("SEND_COUNT", sqlBean.getStr("SEND_COUNT") + 1);
				ServDao.save(OA_GW_TIME_PRESCRIPTION, sqlBean);
				log.debug("修改发送次数");
			}
		}


	}
}
