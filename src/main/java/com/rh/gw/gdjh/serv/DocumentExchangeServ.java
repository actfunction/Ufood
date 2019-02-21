/**
 * 审计管理分系统行政办公管理子系统
 * @file:DocumentExchangeServ.java
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
package com.rh.gw.gdjh.serv;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.gw.gdjh.util.DateUtils;
import com.rh.gw.gdjh.util.FileUtil;
import com.rh.gw.gdjh.util.ParseZip;
import com.rh.gw.gdjh.util.SqlUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

/**
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
public class DocumentExchangeServ{
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentExchangeServ.class);
	
	/**
	 *  本方法根据zip包路径进行解压和解析文件内部的data.xml文件
	 * @param FILE_PATH zip路径
	 * @return
	 */
	public boolean dealFile(String FILE_PATH,String GW_ID) {
		
		try {
			//获取系统根路径
			String sql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG WHERE CONF_KEY='SY_COMM_FILE_ROOT_PATH'";
			List<Bean> confList = Transaction.getExecutor().query(sql);
			String confStr = confList.get(0).getStr("CONF_VALUE");
//			confStr = confStr.replaceAll("\\\\\\\\", "/");
			
			File zipFile = new File(FILE_PATH);
			if(zipFile.exists()) {
				String fileName = zipFile.getName();
                String rootpath = confStr+"/GWJHJX/"+fileName.substring(0, fileName.lastIndexOf("."));
				try {
					File rootFile = new File(rootpath);
					if(!rootFile.exists()) {
						rootFile.mkdirs();
					}
					ParseZip.unZip(FILE_PATH,rootpath);//解压
					FileUtil.deleteFile(zipFile);//解压成功后，删除zip包
					//解析文件并入库和处理文件
					File file = new File(rootpath+"/data.xml");
					boolean flag = false;
					if(file.exists()) {
						DealFile deal = new DealFile();
						flag = deal.XmltoObject(file.getPath(),confStr,GW_ID);
					}
					return flag;
				} catch (Exception e) {
					LOGGER.error("解压失败："+e.getMessage(),e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("zip包处理失败"+e.getMessage(), e);
		}
		
		return false;
	}
	
	/**
	 * 
	 * @title: execute
	 * @descriptin: TODO
	 * @param @param GW_ID  公文ID  通过文件名获取
	 * @param @param DQI_ID	公文交换机构id  发送方交换结构id
	 * @param @param MSG_TYPE 1-未回传，2-已回传 默认1
	 * @param @param FILE_PATH	文件路径
	 * @param @param RETRY_NUM	重试次数
	 * @param @param STATUS		消息状态 1-成功，2-失败
	 * @param @param DEALING_DATE	交易日期
	 * @param @param INSERT_DATE	新增日期
	 * @param @param ERROR_MSG		错误日志
	 * @param @param MARK_FOR_DELETE 删除标志 
	 * @return void
	 * @throws
	 */
	public int execute(String GW_ID,String DQI_ID,String MSG_TYPE,String FILE_PATH,int RETRY_NUM,String jmsID) {
		int code = 2;		//默认接收失败
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			LOGGER.info("公文接收 :Start GW_ID:{},DQI_ID:{},MSG_TYPE:{},FILE_PATH:{},RETRY_NUM{},STATUS{}",GW_ID,DQI_ID,MSG_TYPE,FILE_PATH,RETRY_NUM);
			if(StringUtils.isEmpty(GW_ID)) {
				LOGGER.info("公文接收：公文id为空 JMSID:{}",jmsID);
				return code;
			}
			if(StringUtils.isEmpty(DQI_ID)) {
				LOGGER.info("公文接收：机构id为空 JMSID:{}",jmsID);
				return code;
			}
			
			if(StringUtils.isEmpty(MSG_TYPE)) {
				MSG_TYPE = "1";
			}
			map.put("MSG_TYPE", MSG_TYPE);
			if(StringUtils.isEmpty(FILE_PATH)) {
				LOGGER.info("公文接收：文件为空 JMSID:{}",jmsID);
				return code;
			}
			boolean dealFile = dealFile(FILE_PATH,GW_ID);
			String STATUS = dealFile ? "1":"2";
			String sql = "select count(*) from OA_GW_JH_RECEIVER_INFO where GW_ID = '"+GW_ID+"' and DQI_ID = '"+DQI_ID+"'";
			int count = Transaction.getExecutor().count(sql);
			
			map.put("FILE_PATH", FILE_PATH);
			String INSERT_DATE = DateUtils.getChar19();
			String DEALING_DATE = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			map.put("DEALING_DATE", DEALING_DATE);
			map.put("STATUS", STATUS);
			if(count > 0) {
				Map<String,Object> wheremap = new HashMap<String,Object>();
				RETRY_NUM = count +1;
				map.put("RETRY_NUM", RETRY_NUM);
				wheremap.put("GW_ID", GW_ID);
				wheremap.put("DQI_ID", DQI_ID);
				int update = SqlUtil.update(map, wheremap, "OA_GW_JH_RECEIVER_INFO");
				if(update == -1) {
					return code;
				}
			}else {
				map.put("DQI_ID", DQI_ID);
				map.put("INSERT_DATE", INSERT_DATE);
				map.put("GW_ID", GW_ID);
				int execute = SqlUtil.execute(map, "OA_GW_JH_RECEIVER_INFO");
				if(execute == -1) {
					return code;
				}
			}
			return dealFile ? 1:2;
		} catch (Exception e) {
			LOGGER.error("公文接收：保存异常  ："+e.getMessage(),e);
		}
		return code;
	}
	/**
	 * 
	 * @title: batchUpdate 批量更新状态
	 * @descriptin: TODO
	 * @param @param map	key(where条件 ) value 更新值
	 * @return void
	 * @throws
	 */
	@SuppressWarnings("null")
	public int batchUpdate(Map<String,Object> map) {
		LOGGER.info("公文交换：修改交换结果Start  value:{}",map);
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
				String[] key = entry.getKey().split(",");
				String sql = "update OA_GW_JH_SENDER_INFO set STATUS = '"+ STATUS + "',INSERT_DATE = '"+INSERT_DATE+"' where 1=1 and GW_ID = '"+key[0]+"' and DQI_ID = '"+key[1]+"'";
				batchsql[index] = sql;
				index ++ ;
			}
			code = Transaction.getExecutor().executeBatch(batchsql);
			LOGGER.info("公文交换：修改交换结果end  结果:{}",code);
		} catch (Exception e) {
			code = -1;
			LOGGER.error("公文交换：修改交换结果异常"+e.getMessage(),e);
		}
		return code;
	}
	
	/**
	 * 更新公文交换更新回传状态值
	 * @param GW_ID
	 * @param DQI_ID
	 */
	public void updateReceive(String GW_ID,String DQI_ID) {
		try {
			String sql = "select count(*) from OA_GW_JH_RECEIVER_INFO where GW_ID = '"+GW_ID+"' and DQI_ID = '"+DQI_ID+"'";
			int count = Transaction.getExecutor().count(sql);
			String INSERT_DATE = DateUtils.getChar19();
			int RETRY_NUM = 1;
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("MSG_TYPE", 2);
			Map<String,Object> wheremap = new HashMap<String,Object>();
			RETRY_NUM = count +1;
			map.put("RETRY_NUM", RETRY_NUM);
			map.put("INSERT_DATE", INSERT_DATE);
			wheremap.put("GW_ID", GW_ID);
			wheremap.put("DQI_ID", DQI_ID);
			int update = SqlUtil.update(map, wheremap, "OA_GW_JH_RECEIVER_INFO");
		} catch (Exception e) {
			LOGGER.error("公文接收：回传异常。GW_ID:{},DQI_ID:{}:"+e.getMessage(),GW_ID,DQI_ID,e);
		}
	}
}
