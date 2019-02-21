package com.rh.gw.gdjh.serv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.rh.gw.gdjh.record.RecordRequest;
import com.rh.gw.gdjh.record.RecordRequestBusiness;
import com.rh.gw.gdjh.record.RecordRequestFields;
import com.rh.gw.gdjh.record.RecordRequestFiles;
import com.rh.gw.gdjh.util.XStreamUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.db.Transaction;

public class DealFile{
 
	/** log */
	private static Log log = LogFactory.getLog(DealFile.class);
	
	/**
	 * 解析xml
	 * @param path:xml的文件路径
	 */
	public  boolean XmltoObject(String path,String confStr,String GW_ID) {	
		
		try {
			
			Transaction.begin();
			
			String gwId = GW_ID;
			
			
			
			String xmlToObject = XStreamUtil.xmlToString(path);
		    RecordRequest recordRequest = (RecordRequest) XStreamUtil.xmlToObject(xmlToObject, RecordRequest.class);
		    
		    RecordRequestFields fields = recordRequest.getRecordFields();
			List<RecordRequestBusiness> businesss = recordRequest.getRecordBusiness();
			List<RecordRequestFiles> files = recordRequest.getRecordfiles();
		    
		    executeFields(gwId,fields);
		    executeBusiness(gwId,businesss);
		    executeFiles(gwId,files,path,confStr);
		    
		    Transaction.commit();
		    return true;
		} catch (Exception e) {
			Transaction.rollback();
			log.error("xml数据入库失败,"+e.getMessage(), e);
		}
		finally {
			Transaction.end();
		}
	    return false;
		
	}
	
	public void executeFields(String gwId,RecordRequestFields fields) throws Exception{
		
		if(gwId == null || gwId.isEmpty()) {
			throw new RuntimeException("Fields入库时，gwID不存在");
		}
		
		try {
			if(fields != null) {
				
				String GW_SIGN_TIME = fields.getNd();
				String DEPT_NAME = fields.getJg();
				String GW_TITLE = fields.getTm();
				String ftm = fields.getFtm();
				String bltm = fields.getBltm();
				String GW_CODE = fields.getWjbh();
				String GW_CW_TNAME = fields.getZrz();
				String GW_GONGWEN_SWSJ = fields.getRq();
				String S_EMERGENCY = fields.getJjcd();
				String GW_FILE_TYPE = fields.getWz();
				String GW_MAIN_TO = fields.getZs();
				String GW_COPY_TO = fields.getCs();
				String lbh = fields.getLbh();
				String gb = fields.getGb();
				String ys = fields.getYs();
				String yz = fields.getYz();
				String GW_SRCRET = fields.getMj();
				String GW_SECRET_PERIOD = fields.getBmqx();
				String gg = fields.getGg();
				String zy = fields.getZy();
				String ztc = fields.getZtc();
				String rm = fields.getRm();
				String sjxm = fields.getSjxm();
				String xmssdw = fields.getXmssdw();
				String xmzssdw = fields.getXmzssdw();
				String ISOPEN = fields.getKfkz();
				
				Map<String,Object> map = new HashMap<String,Object>();
				
				map.put("GW_ID", gwId);
				map.put("GW_SIGN_TIME", GW_SIGN_TIME);
//				map.put("DEPT_NAME", DEPT_NAME);
				map.put("GW_TITLE", GW_TITLE);
				map.put("GW_CODE", GW_CODE);
				map.put("GW_CW_TNAME", GW_CW_TNAME);
				map.put("GW_GONGWEN_SWSJ", GW_GONGWEN_SWSJ);
				map.put("S_EMERGENCY", S_EMERGENCY);
				map.put("GW_FILE_TYPE", GW_FILE_TYPE);
				map.put("GW_MAIN_TO", GW_MAIN_TO);
				map.put("GW_COPY_TO", GW_COPY_TO);
				map.put("GW_SRCRET", GW_SRCRET);
				map.put("GW_SECRET_PERIOD", GW_SECRET_PERIOD);
				map.put("ISOPEN", ISOPEN);
				
				String tableName = "OA_GW_GONGWEN";
				execute(map,tableName);
				
			}
		} catch (Exception e) {
			log.error("Fields入库时异常："+e.getMessage(),e);
			throw e;
		}
	}
	
   public void executeBusiness(String gwId,List<RecordRequestBusiness> businesss) throws Exception{
		
		if(gwId == null || gwId.isEmpty()) {
			throw new RuntimeException("business入库时，gwID不存在");
		}
		try {
			String PI_ID = UUID.randomUUID().toString();
			Map<String,Object> procMap = new HashMap<String,Object>();
			procMap.put("PI_ID", PI_ID);
			procMap.put("DOC_ID", gwId);
			execute(procMap,"SY_WFE_PROC_INST_HIS");
			
			if(businesss != null && businesss.size()>0) {
				
				for (RecordRequestBusiness business : businesss) {
					
					String action = business.getAction();
					String description = business.getDescription();
					String NODE_BTIME = business.getTime();
					String DONE_DEPT_NAMES = business.getJg();
					String DONE_USER_NAME = business.getRy();
					String NODE_NAME = business.getState();
//					String GW_TITLE = business.getFilename();
					
					Map<String,Object> businessMap = new HashMap<String,Object>();
					
					businessMap.put("NODE_BTIME", NODE_BTIME);
					businessMap.put("DONE_DEPT_NAMES", DONE_DEPT_NAMES);
					businessMap.put("DONE_USER_NAME", DONE_USER_NAME);
					businessMap.put("NODE_NAME", NODE_NAME);
//					businessMap.put("GW_TITLE", GW_TITLE);
					
					String NI_ID = UUID.randomUUID().toString();
					businessMap.put("NI_ID", NI_ID);
					businessMap.put("PI_ID", PI_ID);
					execute(businessMap,"SY_WFE_NODE_INST_HIS");
				}
			}
		} catch (Exception e) {
			log.error("business入库时异常："+e.getMessage(),e);
			throw e;
		}
		
	}

   public void executeFiles(String gwId,List<RecordRequestFiles> files,String path,String confStr) throws Exception{
		
		if(gwId == null || gwId.isEmpty()) {
			throw new RuntimeException("files入库时，gwID不存在");
		}
		
		//获取系统根路径
//		String sql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG WHERE CONF_NAME='文件跟路径'";
//		List<Bean> confList = Transaction.getExecutor().query(sql);
//		String confStr = confList.get(0).getStr("CONF_VALUE").replaceFirst("d", "D");
//		path = path.replaceAll("\\\\", "/");
		path = path.replace(confStr, "@SYS_FILE_PATH@");
		
		try {
			
//		     String sql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG WHERE CONF_NAME='文件跟路径'";
//		     List<Bean> beans = Transaction.getExecutor().query(sql);
//			 path = 
			
			if(files != null && files.size()>0) {
				
				for (RecordRequestFiles file : files) {
					
					String soft = file.getSoft();
					String filename = file.getFilename();
					String docType = file.getDocType();
					String size = file.getSize();
					String pageCount = file.getPageCount();
					String createTime = file.getCreateTime();
					String updateTime = file.getUpdateTime();
					
					Map<String,Object> fileMap = new HashMap<String,Object>();
					
					fileMap.put("DATA_ID", gwId);
					fileMap.put("FILE_ID", UUID.randomUUID().toString());
					fileMap.put("S_MTIME", updateTime);
					fileMap.put("FILE_NAME", filename);
					
					fileMap.put("FILE_SIZE", size);
					fileMap.put("DATA_TYPE", docType);
//					String filePath = "@SYS_PATH@/GWJH/"+filename;
					path = path.substring(0,path.lastIndexOf("/")+1)+"files/"+filename;
//					path = path.substring(path.lastIndexOf("\\")+1)+filename;
					
					fileMap.put("FILE_PATH", path);
					
					execute(fileMap,"SY_COMM_FILE");
				}
			}
		} catch (Exception e) {
			log.error("files入库时异常："+e.getMessage(),e);
			throw e;
		}
		
	}
		
	/**
	 * 
	 * @title: execute
	 * @descriptin: TODO
	 * @param @param map  key-value  key(数据库字段名) value 值
	 * @param @param tableName 数据库表名
	 * @return void
	 * @throws
	 */
	public int execute(Map<String,Object> map,String tableName) throws Exception{
		int code = 0;
		try {
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("insert into "+tableName+" (");
			StringBuffer valueBuffer = new StringBuffer();
			valueBuffer.append(" values (");
			for (Map.Entry<String,Object> entry : map.entrySet()) {
				sqlBuffer.append(entry.getKey()+",");
				valueBuffer.append("'"+entry.getValue()+"',");
			}
			sqlBuffer.deleteCharAt(sqlBuffer.length()-1);
			valueBuffer.deleteCharAt(valueBuffer.length()-1);
			sqlBuffer.append(") ");
			valueBuffer.append(")");
			String sql = sqlBuffer.append(valueBuffer).toString();
			System.out.println(sql);
			code = Transaction.getExecutor().execute(sql);
		} catch (Exception e) {
			code = -1;
			log.error("异常："+e.getMessage(),e);
			throw e;
		}
		return code;
	}		
		
}
