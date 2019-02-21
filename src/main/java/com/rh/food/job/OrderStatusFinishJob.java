package com.rh.food.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.base.db.Transaction;

public class OrderStatusFinishJob implements Job{
	/** log */
	private static Log log = LogFactory.getLog(OrderStatusFinishJob.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
//		JobDataMap jobData=context.getJobDetail().getJobDataMap();
//		if(jobData.containsKey("")) {
//			String job=jobData.getString("");
//		}
		System.out.println("--------------------------------");
     	boolean result = Transaction.getExecutor().execute(" UPDATE OA_SV_FOOD_ORDER_INFO foi SET foi.ORDER_STATUS ='2' WHERE EXISTS(\r\n" + 
 				"select foi.MAINTAIN_ID  FROM   OA_SV_FOOD_MAINTAIN_INFO fai WHERE 1=1 \r\n" + 
 				"AND to_Date(fai.ORDER_END_DATE||' ' ||fai.ORDER_END_TIME,'YYYY-MM-DD HH24:MI') <=sysdate \r\n" + 
 				"AND foi.MAINTAIN_ID=fai.MAINTAIN_ID) AND  foi.ORDER_STATUS='1' ")> 0;
        if(result) {
        	log.debug("更新副食品预定表订单状态成功！");
        }
	}
 
}
