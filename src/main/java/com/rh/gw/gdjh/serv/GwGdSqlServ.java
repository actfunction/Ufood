/**
 * 审计管理分系统行政办公管理子系统
 * @file:DocumentExchangeServ.java
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
package com.rh.gw.gdjh.serv;

import java.util.Map;

import com.rh.gw.gdjh.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.core.base.db.Transaction;

/**
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
public class GwGdSqlServ{
	private static final Logger LOGGER = LoggerFactory.getLogger(GwGdSqlServ.class);
	
	
	/**
	 * 
	 * @title: batchUpdate 批量更新状态
	 * @descriptin: TODO
	 * @param @param map	key(where条件 ) value 更新值
	 * @return void
	 * @throws
	 */
	public int batchUpdate(Map<String,Object> map) {
		LOGGER.info("公文归档：修改归档结果Start  value:{}",map);
		int code = 0;
		int index = 0;
		try {
			String[] batchsql = new String[map.size()] ;
			for (Map.Entry<String,Object> entry : map.entrySet()) {
				String STATUS = entry.getValue().toString();
				if("2".equals(STATUS)) {
					STATUS = "4";
				}
				if("1".equals(STATUS)) {
					STATUS = "2";
				}
				String INSERT_DATE = DateUtils.getChar19();
				String key = entry.getKey();
				String sql = "update OA_GW_GD_SENDER_INFO set STATUS = '"+ STATUS + "',INSERT_DATE = '"+INSERT_DATE+"' where 1=1 and GW_ID = '"+key+"'";
				batchsql[index] = sql;
				index ++ ;
			}
			code = Transaction.getExecutor().executeBatch(batchsql);
			LOGGER.info("公文归档：修改归档结果end  结果:{}",code);
		} catch (Exception e) {
			code = -1;
			LOGGER.error("公文归档：修改归档结果异常"+e.getMessage(), e);
		}
		return code;
	}
}
