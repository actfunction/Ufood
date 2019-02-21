package com.rh.gw.job;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;


/**
 * 审理司审理时效任务调度
 * 
 * @author kfzx-linll
 * @date 2018/12/26
 */
public class AddEspaceJob implements Job {
	// 记录日志
	private static Log log = LogFactory.getLog(AddEspaceJob.class);

	// Espace表和服务一致
	private static final String OA_GW_INTERFACE_ESPACE = "OA_GW_INTERFACE_ESPACE";

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
		String sql = "SELECT * FROM " + OA_GW_TIME_PRESCRIPTION + " tp WHERE tp.SEND_COUNT < 3 AND tp.S_FLAG = '1' ";
		List<Bean> beans = Transaction.getExecutor().query(sql);

		// 遍历查询出来的发送次数小于3的公文
		for (Bean sqlBean : beans) {
			// 插入到Espace消息表
			ServDao.save(OA_GW_INTERFACE_ESPACE, sqlBean);
		}
		log.debug("从审理时效中间表查询出数据保存到Espace表完成：" + beans);
	}
}
