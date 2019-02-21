package com.rh.gw.gdjh.job;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.gw.gdjh.record.RecordRequest;
import com.rh.gw.gdjh.record.RecordRequestBusiness;
import com.rh.gw.gdjh.record.RecordRequestFields;
import com.rh.gw.gdjh.record.RecordRequestFiles;
import com.rh.gw.gdjh.tlq.GwGdSenderMsg;
import com.rh.gw.gdjh.util.FileToZip;
import com.rh.gw.gdjh.util.FileUtil;
import com.rh.gw.gdjh.util.XStreamUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map.Entry;
import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;

public class GwGdSenderMsgJob extends CommonServ implements Job{
	private final String YWFW = "OA_GW_GONGWEN_ICBC_YWFW";
	private final String XZFW = "OA_GW_GONGWEN_ICBC_XZFW";
	private final String SW = "OA_GW_GONGWEN_ICBCSW";
	private final String QB = "OA_GW_GONGWEN_ICBCQB";
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
//	private final String rootPath = "D:\\GWGD";

	/** log */
	private static Log log = LogFactory.getLog(GwGdSenderMsgJob.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			Date date = new Date();
			log.info("start job");
			String dealDate = sdf2.format(date);
			
			//获取系统根路径
			String rootSql = "select CONF_VALUE from SY_COMM_CONFIG where CONF_KEY='SY_COMM_FILE_ROOT_PATH'";
			List<Bean> confList = Transaction.getExecutor().query(rootSql);
			String rootPath = confList.get(0).getStr("CONF_VALUE");
//			rootPath = rootPath.replaceAll("\\\\\\\\", "/");
			
			//查找公文数据和将附件放到指定路径
			String gwRootPath = "GWGD/"+sdf1.format(date) + "/"+ dealDate + "-";
			Map<String, RecordRequest> gws = findGongWenInfo(date, gwRootPath,rootPath);
			if(gws != null && gws.size()>0) {
					//生成gw文件路径
					for (Map.Entry<String, RecordRequest> entry : gws.entrySet()) {
						String gwPath = gwRootPath + entry.getKey();
						//生成公文路径
						File gwFile = new File(rootPath+File.separator+gwPath);
						if(!gwFile.exists()) {
							gwFile.mkdirs();
						}
						
						//生成xml
						XStreamUtil.stringToXml(gwFile+"/data.xml", entry.getValue().toString(true));
						
						//生成word
						try {
							File filesPath = new File(gwFile+"/files");
							if(!filesPath.exists()) {
								filesPath.mkdir();
							}
							FileToZip.getWordFile(entry.getKey(), filesPath.toString());
						} catch (Exception e) {
							log.error("生成word失败："+e.getMessage(), e);
						}
						String gwId = entry.getKey();
						//生成zip
						try {
							FileToZip.zip(gwFile.toString());
							//删除文件
							FileUtil.deleteFile(gwFile);
							//将生成zip包存到数据库OA_GW_GD_SENDER_INFO表中
							String FILE_PATH = gwFile+".zip";
							String sql = "insert into OA_GW_GD_SENDER_INFO (GW_ID,FILE_PATH,STATUS,DEALING_DATE,INSERT_DATE) "
								+ "values ('"+entry.getKey() +"','"
								+ FILE_PATH +"',"
								+ "'0','"
								+ dealDate +"',"
	                            + "to_date('"+dealDate+"','yyyyMMddHH24miss')" 
	                            +")";
							Transaction.getExecutor().execute(sql);
						} catch (Exception e) {
							log.error("打包zip失败, 公文id="+gwId+",异常信息："+e.getMessage(), e);
						}
					}
			}
			sendGwData(dealDate);
			log.info("over job");
		}catch (Exception e) {
			log.error("发送公文文件异常"+e.getMessage(), e);
		}
	}
	
	public void sendGwData(String dealDate) throws Exception {
		String sql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG WHERE CONF_KEY='GW_GD_QUEUE_NAME'";
		List<Bean> beanList = Transaction.getExecutor().query(sql);
		String localQueueName = null;
		String remoteQueueName = null;
		if(beanList==null || !beanList.isEmpty()) {
			String confValue = beanList.get(0).getStr("CONF_VALUE");
			String[] strarr = confValue.split("\\|");
			remoteQueueName = strarr[0];
			localQueueName = strarr[1];
		}
		if(remoteQueueName==null || remoteQueueName.equals("") || localQueueName==null || localQueueName.equals("")) {
			throw new Exception("初始化监听器，队列名称为空，请先配置队列名称");
		}

		GwGdSenderMsg windqMsg = new GwGdSenderMsg(dealDate);
		String gdSql = "SELECT t1.*,t2.OA_GONGWEN_GW_GW FROM OA_GW_GD_SENDER_INFO t1, OA_GW_GONGWEN t2 WHERE t1.GW_ID=t2.GW_ID and t1.STATUS in ('0','3','4') ORDER BY t1.INSERT_DATE ASC";
		List<Bean> list = Transaction.getExecutor().query(gdSql);
		windqMsg.callMqReq(remoteQueueName, localQueueName, list, 100);
	}
	
	public Map<String,RecordRequest> findGongWenInfo(Date date, String gwRootPath,String rootPath) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String endDate = String.valueOf(c.get(Calendar.YEAR))+"-"+String.valueOf(c.get(Calendar.MONTH)+1)+"-"
				+String.valueOf(c.get(Calendar.DAY_OF_MONTH))+" 00:00:00";
		c.add(Calendar.DATE, -1);
		String startDate = String.valueOf(c.get(Calendar.YEAR))+"-"+String.valueOf(c.get(Calendar.MONTH)+1)+"-"
				+String.valueOf(c.get(Calendar.DAY_OF_MONTH))+" 00:00:00";
		//查询公文信息
		
		String sql = "SELECT GW.GW_ID,DE.DEPT_NAME,GW.TMPL_CODE,GW.GW_SIGN_TIME,GW.GW_TITLE,GW.GW_CODE,GW.GW_CW_TNAME,GW.GW_SW_CNAME,GW.GW_GONGWEN_QBR,"
				+ "GW.GW_GONGWEN_SWSJ,GW.S_EMERGENCY,GW.GW_FILE_TYPE,GW.GW_MAIN_TO,GW.GW_COPY_TO,GW.GW_SRCRET,GW.GW_SECRET_PERIOD,GW.ISOPEN,"
				+ "to_date(GW.S_MTIME,'yyyy-MM-dd HH24:mi:ss') "
				+ "FROM OA_GW_GONGWEN GW, SY_ORG_DEPT DE WHERE GW.S_WF_STATE='2' AND GW.S_ODEPT=DE.DEPT_CODE AND "
				+ "GW.TMPL_CODE IN ('OA_GW_GONGWEN_ICBC_YWFW','OA_GW_GONGWEN_ICBC_XZFW','OA_GW_GONGWEN_ICBCSW','OA_GW_GONGWEN_ICBCQB') "
				+"AND GW.S_MTIME > to_date('"+startDate+"','yyyy-MM-dd HH24:mi:ss') "
				+"AND GW.S_MTIME < to_date('"+endDate+"','yyyy-MM-dd HH24:mi:ss')";
		
		log.info("gwsql="+sql);
//		String sql = "SELECT GW.GW_ID,DE.DEPT_NAME,GW.TMPL_CODE,GW.GW_SIGN_TIME,GW.GW_TITLE,GW.GW_CODE,GW.GW_CW_TNAME,GW.GW_SW_CNAME,GW.GW_GONGWEN_QBR,"
//				+ "GW.GW_GONGWEN_SWSJ,GW.S_EMERGENCY,GW.GW_FILE_TYPE,GW.GW_MAIN_TO,GW.GW_COPY_TO,GW.GW_SRCRET,GW.GW_SECRET_PERIOD,GW.ISOPEN "
//				+ "FROM OA_GW_GONGWEN_GDJH_TEST GW, SY_ORG_DEPT DE WHERE GW.S_WF_STATE='2' AND GW.S_DEPT=DE.DEPT_CODE AND "
//				+ "GW.TMPL_CODE IN ('OA_GW_GONGWEN_ICBC_YWFW','OA_GW_GONGWEN_ICBC_XZFW','OA_GW_GONGWEN_ICBCSW','OA_GW_GONGWEN_ICBCQB')";
		List<Bean> list = Transaction.getExecutor().query(sql);
		if(list==null || list.isEmpty()) {
			log.info("没有查询到公文归档数据");
			return null;
		}
		
		String filesql = "SELECT F.*,G.GW_ID, to_date(G.S_MTIME,'yyyy-MM-dd HH24:mi:ss') FROM SY_COMM_FILE F, OA_GW_GONGWEN G, SY_ORG_DEPT DE WHERE F.DATA_ID=G.GW_ID AND G.S_DEPT=DE.DEPT_CODE " + 
				"AND G.S_WF_STATE='2' AND G.TMPL_CODE IN ('OA_GW_GONGWEN_ICBC_YWFW','OA_GW_GONGWEN_ICBC_XZFW','OA_GW_GONGWEN_ICBCSW','OA_GW_GONGWEN_ICBCQB')"
				+"AND G.S_MTIME > to_date('"+startDate+"','yyyy-MM-dd HH24:mi:ss') "
				+"AND G.S_MTIME < to_date('"+endDate+"','yyyy-MM-dd HH24:mi:ss')";

//		String filesql = "SELECT F.*,G.GW_ID FROM SY_COMM_FILE F, OA_GW_GONGWEN_GDJH_TEST G, SY_ORG_DEPT DE WHERE F.DATA_ID=G.GW_ID AND G.S_DEPT=DE.DEPT_CODE " + 
//				"AND G.S_WF_STATE='2' AND G.TMPL_CODE IN ('OA_GW_GONGWEN_ICBC_YWFW','OA_GW_GONGWEN_ICBC_XZFW','OA_GW_GONGWEN_ICBCSW','OA_GW_GONGWEN_ICBCQB')";
		log.info("filesql="+filesql);
		List<Bean> fileList = Transaction.getExecutor().query(filesql);
		Map<String, List<Bean>> fileMap = new HashMap<String, List<Bean>>();
		if(fileList!=null && !fileList.isEmpty()) {
			for(Bean fileBean:fileList) {
				String gwId = fileBean.getStr("GW_ID");
				if(fileMap.containsKey(gwId)) {
					List<Bean> beanList = fileMap.get(gwId);
					beanList.add(fileBean);
					fileMap.put(gwId, beanList);
				}
				else {
					List<Bean> beanList = new ArrayList<Bean>();
					beanList.add(fileBean);
					fileMap.put(gwId, beanList);
				}
			}
		}
		String behaviorSql = "SELECT O.*,G.GW_ID,G.GW_TITLE,to_date(G.S_MTIME,'yyyy-MM-dd HH24:mi:ss')  "  
				+ "FROM SY_WFE_PROC_INST_HIS I, OA_GW_GONGWEN G, SY_ORG_DEPT DE, SY_WFE_NODE_INST_HIS O "  
				+ "WHERE I.PI_ID=O.PI_ID AND I.DOC_ID=G.GW_ID AND G.S_DEPT=DE.DEPT_CODE "  
				+ "AND G.S_WF_STATE='2' AND G.TMPL_CODE IN ('OA_GW_GONGWEN_ICBC_YWFW','OA_GW_GONGWEN_ICBC_XZFW','OA_GW_GONGWEN_ICBCSW','OA_GW_GONGWEN_ICBCQB') "
				+ "AND G.S_MTIME > to_date('"+startDate+"','yyyy-MM-dd HH24:mi:ss') "
				+ "AND G.S_MTIME < to_date('"+endDate+"','yyyy-MM-dd HH24:mi:ss')";
		
//		String behaviorSql = "SELECT N.*,G.GW_ID FROM SY_WFE_NODE_INST_HIS N, SY_WFE_PROC_INST_HIS I, OA_GW_GONGWEN_GDJH_TEST G, SY_ORG_DEPT DE WHERE I.PI_ID=N.PI_ID AND I.DOC_ID=G.GW_ID AND G.S_DEPT=DE.DEPT_CODE " + 
//				"AND G.S_WF_STATE='2' AND G.TMPL_CODE IN ('OA_GW_GONGWEN_ICBC_YWFW','OA_GW_GONGWEN_ICBC_XZFW','OA_GW_GONGWEN_ICBCSW','OA_GW_GONGWEN_ICBCQB')";
		log.info("behaviorSql="+behaviorSql);
		List<Bean> behaviorList = Transaction.getExecutor().query(behaviorSql);
		Map<String, List<Bean>> behaviorMap = new HashMap<String, List<Bean>>();
		if(behaviorList!=null && !behaviorList.isEmpty()) {
			for(Bean behaviorBean:behaviorList) {
				String gwId = behaviorBean.getStr("GW_ID");
				if(behaviorMap.containsKey(gwId)) {
					List<Bean> beanList = behaviorMap.get(gwId);
					beanList.add(behaviorBean);
					behaviorMap.put(gwId, beanList);
				}
				else {
					List<Bean> beanList = new ArrayList<Bean>();
					beanList.add(behaviorBean);
					behaviorMap.put(gwId, beanList);
				}
			}
			
		}
		
//		//查找系统根路径
//		String confSql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG  WHERE CONF_NAME= '文件跟路径'";
//		Bean confBean = Transaction.getExecutor().queryOne(confSql);
//		String confStr = confBean.getStr("CONF_VALUE").replaceAll("\\\\\\\\", "/");
		
		Map<String,RecordRequest> maps = new HashMap<String,RecordRequest>();
		for(Bean bean:list) {
			String gwtype = bean.getStr("TMPL_CODE");
			String gwId = bean.getStr("GW_ID");
			RecordRequest d=new RecordRequest();
			List<RecordRequestFiles> files=new ArrayList<RecordRequestFiles>();
			//String filesql = "SELECT * FROM SY_COMM_FILE F WHERE F.DATA_ID='"+gwId+"'";
			//List<Bean> fileList = Transaction.getExecutor().query(filesql);
			//创建路径
			String gwPath = gwRootPath + gwId+"/files";
			//生成公文路径
			File filesPath = new File(rootPath + "/" + gwPath);
			if(!filesPath.exists()) {
				filesPath.mkdirs();
			}
			
			List<Bean> fileBeanList = fileMap.get(gwId);
			if(fileBeanList!=null) {
				for(Bean fileBean:fileBeanList) {
					String filepath = fileBean.getStr("FILE_PATH");
					filepath = filepath.replaceAll("@SYS_FILE_PATH@", rootPath);
//					String filepath = "D:\\TESTone\\one.txt";
					Map<Object,Object> fileMaps = FileUtil.getFile(filepath,filesPath.toString());
					if(fileMaps != null && fileMaps.size()>0) {
						RecordRequestFiles file=new RecordRequestFiles();
						file.setSoft(fileMaps.get("fileSoft").toString());
//						file.setFilename(fileMaps.get("fileName").toString());
						file.setFilename(fileBean.getStr("FILE_NAME"));
						file.setDocType(fileMaps.get("docType").toString()); 
//						file.setSize(fileMaps.get("fileSize").toString()); 
						file.setSize(fileBean.getStr("FILE_SIZE"));
						file.setPageCount("");
						String sMtime = sdf1.format(sdf.parse(fileBean.getStr("S_MTIME")));
						file.setCreateTime(sMtime);
						file.setUpdateTime(sMtime);
						files.add(file);
					}
				}
			}
			List<RecordRequestBusiness>	business=new ArrayList<RecordRequestBusiness>();
//			String behaviorSql = "SELECT * FROM SY_WFE_NODE_INST_HIS N, SY_WFE_PROC_INST_HIS I "
//								+"WHERE I.PI_ID=N.PI_ID AND I.DOC_ID='"+gwId+"'";
//			List<Bean> behaviorList = Transaction.getExecutor().query(behaviorSql);
			List<Bean> behaviorBeanList = behaviorMap.get(gwId);
			if(behaviorBeanList!=null) {
				for(Bean behaviorBean:behaviorBeanList) {
			        RecordRequestBusiness behavior=new RecordRequestBusiness();
			        behavior.setAction(behaviorBean.getStr("NODE_NAME"));
			        behavior.setState("历史行为");
			        behavior.setJg(behaviorBean.getStr("DONE_DEPT_NAMES"));
			        behavior.setRy(behaviorBean.getStr("DONE_USER_NAME"));
			        behavior.setDescription("");
			        behavior.setTime(behaviorBean.getStr("NODE_BTIME"));
			        behavior.setFilename(behaviorBean.getStr("GW_TITLE"));
			        business.add(behavior);
				}
			}

			RecordRequestFields fields=new RecordRequestFields();
			fields.setBltm("123");
			fields.setNd(bean.getStr("GW_SIGN_TIME"));
			fields.setJg(bean.getStr("DEPT_NAME"));
			fields.setTm(bean.getStr("GW_TITLE"));
			fields.setBltm("");
			fields.setFtm("");
			fields.setRq(bean.getStr("GW_GONGWEN_SWSJ"));
			fields.setJjcd(bean.getStr("S_EMERGENCY"));
			fields.setWz(bean.getStr("GW_FILE_TYPE"));
			fields.setLbh("");
			fields.setGb("");
			fields.setYs("");
			fields.setYz("中文");
			fields.setMj(bean.getStr("GW_SRCRET"));
			fields.setBmqx(bean.getStr("GW_SECRET_PERIOD"));
			fields.setGg("");
			fields.setZy("");
			fields.setZtc("");
			fields.setRm("");
			fields.setSjxm("");
			fields.setXmssdw("");
			fields.setXmzssdw("");
			
			if(gwtype.equals(YWFW)) {
				fields.setWjbh(bean.getStr("gw_code"));
				fields.setZrz(bean.getStr("GW_CW_TNAME"));
				fields.setZs(bean.getStr("GW_MAIN_TO"));
				fields.setCs(bean.getStr("GW_COPY_TO"));
				fields.setKfkz("");
			}
			else if(gwtype.equals(XZFW)) {
				fields.setWjbh(bean.getStr("gw_code"));
				fields.setZrz(bean.getStr("GW_CW_TNAME"));
				fields.setZs(bean.getStr("GW_MAIN_TO"));
				fields.setCs(bean.getStr("GW_COPY_TO"));
				fields.setKfkz(bean.getStr("ISOPEN"));
			}
			else if(gwtype.equals(SW)) {
				fields.setWjbh(bean.getStr("gw_code"));
				fields.setZrz(bean.getStr("GW_SW_CNAME"));
				fields.setZs("");
				fields.setCs("");
				fields.setKfkz("");
			}
			else if(gwtype.equals(QB)) {
				fields.setWjbh(bean.getStr("GW_MAIN_HANDLE")+"/"+bean.getStr("GW_YEAR_CODE")+"/"+bean.getStr("GW_YEAR")+"/"+bean.getStr("GW_YEAR_NUMBER"));
				fields.setZrz(bean.getStr("GW_GONGWEN_QBR"));
				fields.setZs("");
				fields.setCs("");
				fields.setKfkz("");
			}
			
	        d.setSource("行政办公管理子系统");
	        d.setEntity("文书");
	        d.setId(bean.getStr("GW_ID"));
	        d.setParentId("");
	        d.setRecordFields(fields);
	        d.setRecordBusiness(business);
	        d.setRecordfiles(files);
//	        System.out.println(d.toString(true));
	        //生成xml
//	        XStreamUtil.stringToXml("D:\\newFile\\data.xml", d.toString(true));
//	        String xmlToString = XStreamUtil.xmlToString("D:\\newFile\\data.xml");
//	        Object xmlToObject = XStreamUtil.xmlToObject(xmlToString,RecordRequest.class);
//			System.out.println(xmlToObject);
	        maps.put(gwId, d);
		}
		return maps;
	}
}
