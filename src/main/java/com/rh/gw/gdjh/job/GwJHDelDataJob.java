package com.rh.gw.gdjh.job;



import java.io.File;
import java.util.List;

import com.rh.gw.gdjh.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

/**
 * 
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhangheng1
 * @date: 2018年12月7日 上午10:59:09
 * @version: V1.0
 * @description: 定时任务，用于定时删除陈旧的公文交换数据
 */
public class GwJHDelDataJob implements Job{
	
	private final static String NUM = "3";

	/** log */
	private static Log log = LogFactory.getLog(GwJHDelDataJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		log.info("开始定时公文交换数据");
		delDates();
		log.info("公文交换数据删除结束");
	}


	public void delDates() {
		try {
			//查询需要删除的数据
			String sql1 = "select FILE_PATH from OA_GW_JH_SENDER_INFO where INSERT_DATE <=(sysdate-"+NUM+") and STATUS in ('2', '5')";
//					+ "union "
//					+ "select FILE_PATH from OA_GW_JH_SENDER_INFO where INSERT_DATE <=(sysdate-30) and STATUS='3'";
			List<Bean> list = Transaction.getExecutor().query(sql1);
			//删除zip包
			for(int i=0;i<list.size();i++) {
				Bean bean = list.get(i);
				String FILE_PATH = bean.getStr("FILE_PATH");
				if(FILE_PATH != null && !FILE_PATH.isEmpty()) {
					File file = new File("FILE_PATH");
					if(file.exists()) {
						FileUtil.deleteFile(file);
					}
				}
			}
			
			//删除OA_GW_JH_SENDER_INFO过时数据
			String sql2 = "delete from OA_GW_JH_SENDER_INFO where INSERT_DATE <=(sysdate-"+NUM+") and STATUS in ('2', '5')";
//			String sql3 = "delete from OA_GW_JH_SENDER_INFO where INSERT_DATE <=(sysdate-30) and STATUS='3'";
			
			Transaction.getExecutor().execute(sql2);
//			Transaction.getExecutor().execute(sql3);
		} catch (Exception e) {
			log.error("定时删除公文交换数据失败:"+e.getMessage(), e);
		}
	}
		
}
