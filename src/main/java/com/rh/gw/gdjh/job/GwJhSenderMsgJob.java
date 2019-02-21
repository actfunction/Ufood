package com.rh.gw.gdjh.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rh.gw.gdjh.record.RecordRequest;
import com.rh.gw.gdjh.record.RecordRequestBusiness;
import com.rh.gw.gdjh.record.RecordRequestFields;
import com.rh.gw.gdjh.record.RecordRequestFiles;
import com.rh.gw.gdjh.tlq.GwJhSenderMsg;
import com.rh.gw.gdjh.util.FileToZip;
import com.rh.gw.gdjh.util.FileUtil;
import com.rh.gw.gdjh.util.XStreamUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class GwJhSenderMsgJob extends CommonServ implements Job{
	
	private final String YWFW = "OA_GW_GONGWEN_ICBC_YWFW";
	private final String XZFW = "OA_GW_GONGWEN_ICBC_XZFW";
	private final String SW = "OA_GW_GONGWEN_ICBCSW";
	private final String QB = "OA_GW_GONGWEN_ICBCQB";
	//查询的数量
//	private static final int rownum = 10;
	
	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");

//	private String rootPath = "D:\\GWJH";
	
	/** log */
	private static Log log = LogFactory.getLog(GwJhSenderMsgJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try {
			Date date = new Date();
			log.info("start job");
			//获取系统根路径
			String rootSql = "select CONF_VALUE from SY_COMM_CONFIG where CONF_KEY='SY_COMM_FILE_ROOT_PATH'";
			List<Bean> confList = Transaction.getExecutor().query(rootSql);
			String rootPath = confList.get(0).getStr("CONF_VALUE");
			
			//查找公文数据和将附件放到指定路径
			Map<String, RecordRequest> gws = findGongWenInfo(date,rootPath);
			
			//生成zip包，并入待发送库
			handleGW(gws,date,rootPath);
			
			sendJhData();	
			log.info("over job");
		}catch (Exception e) {
			log.error("发送公文文件异常"+e.getMessage(),e);
		}
	}

	public void sendJhData() throws Exception {
		GwJhSenderMsg windqMsg = new GwJhSenderMsg();
		String sql = "SELECT * FROM OA_GW_JH_SENDER_INFO WHERE STATUS in ('0','3','4') ORDER BY INSERT_DATE ASC";
		List<Bean> list = Transaction.getExecutor().query(sql);
		Map<String, List<Bean>> queueMap = new HashMap<String, List<Bean>>();
		for(Bean bean:list) {
			String qdiId = bean.getStr("DQI_ID");
			if(queueMap.containsKey(qdiId)) {
				List<Bean> msgList = queueMap.get(qdiId);
				msgList.add(bean);
				queueMap.put(qdiId, msgList);
			}
			else {
				List<Bean> msgList = new ArrayList<Bean>();
				msgList.add(bean);
				queueMap.put(qdiId, msgList);
			}
		}
		String deptSql = "SELECT * FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_TYPE='1'";
		List<Bean> deptList = Transaction.getExecutor().query(deptSql);
		if(deptList==null || deptList.isEmpty()) {
			throw new Exception("本地队列不能为空，请配置");
		}
		String localQName = deptList.get(0).getStr("DQI_ID");
		if(localQName==null || localQName.equals("")) { 
			throw new Exception("本地队列不能为空，请配置");
		}
		Iterator<String> iter = queueMap.keySet().iterator();
		while(iter.hasNext()) {
			String qdiId = iter.next();
			try {
				windqMsg.callMqReq(qdiId, localQName, queueMap.get(qdiId), 100);
			}
			catch(Exception e) {
				log.error("发送消息到TLQ失败，交换机构号："+qdiId, e);
			}
		}
	}

	/**
	 * 
	 * @title: findGongWenInfo
	 * @descriptin: 该方法用于查询公文归档生成xml文件的相关数据，将公文归档相关的文件放到指定路径
	 * @param @param newDate 当前系统时间
	 * @return List<Map<String,RecordRequest>> string是gwId RecordRequest公文相关数据
	 * @throws
	 */
	public Map<String,RecordRequest> findGongWenInfo(Date date,String rootPath) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//查找公文数据
		String gwSql = "SELECT GW.GW_ID,DE.DEPT_NAME,GW.TMPL_CODE,GW.GW_SIGN_TIME,GW.GW_TITLE,GW.GW_CODE,GW.GW_CW_TNAME,GW.GW_SW_CNAME,GW.GW_GONGWEN_QBR,"
				+ "GW.GW_GONGWEN_SWSJ,GW.S_EMERGENCY,GW.GW_FILE_TYPE,GW.GW_MAIN_TO,GW.GW_COPY_TO,GW.GW_SRCRET,GW.GW_SECRET_PERIOD,GW.ISOPEN "
				+ "FROM OA_GW_GONGWEN GW, SY_ORG_DEPT DE,OA_GW_JH_SENDER_INFO JH "
				+ "WHERE GW.S_ODEPT=DE.DEPT_CODE AND GW.GW_ID=JH.GW_ID AND JH.STATUS='0'" ;
		log.info("gwSql="+gwSql);
		List<Bean> list = Transaction.getExecutor().query(gwSql);
		if(list==null || list.isEmpty()) {
			log.info("没有查询到公文交换数据");
			return null;
		}
		//查找文件数据
		String filesql = "SELECT F.*,G.GW_ID "
				+ "FROM SY_COMM_FILE F, OA_GW_GONGWEN G, SY_ORG_DEPT DE, OA_GW_JH_SENDER_INFO JH "
				+ "WHERE F.DATA_ID=G.GW_ID AND G.S_DEPT=DE.DEPT_CODE AND G.GW_ID=JH.GW_ID AND JH.STATUS='0'" ;
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

		//查询behavior数据
		String behaviorSql = "SELECT O.*,G.GW_ID,G.GW_TITLE  "  
				+ "FROM SY_WFE_PROC_INST_HIS I, OA_GW_GONGWEN G, SY_ORG_DEPT DE, OA_GW_JH_SENDER_INFO JH ,SY_WFE_NODE_INST_HIS O " 
				+ "WHERE I.PI_ID=O.PI_ID AND I.DOC_ID=G.GW_ID AND G.S_DEPT=DE.DEPT_CODE AND G.GW_ID=JH.GW_ID AND JH.STATUS='0' ";
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
		
		Map<String,RecordRequest> maps = new HashMap<String,RecordRequest>();
		
//		//查找系统根路径
//		String confSql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG  WHERE CONF_NAME= '文件跟路径'";
//		Bean confBean = Transaction.getExecutor().queryOne(confSql);
//		String confStr = confBean.getStr("CONF_VALUE").replaceAll("\\\\\\\\", "/");
		
		for(Bean bean:list) {
			String gwtype = bean.getStr("TMPL_CODE");
			String gwId = bean.getStr("GW_ID");
			RecordRequest d=new RecordRequest();
			List<RecordRequestFiles> files=new ArrayList<RecordRequestFiles>();
			
			//创建路径
			String gwPath = "GWJH/"+sdf1.format(date)+"/"+sdf2.format(date)+"-"+gwId+"/files";
			//生成公文路径
			File filesPath = new File(rootPath+"/" + gwPath);
			if(!filesPath.exists()) {
				filesPath.mkdirs();
			}
			
			List<Bean> fileBeanList = fileMap.get(gwId);
			if(fileBeanList!=null) {
				for(Bean fileBean:fileBeanList) {
					String filepath = fileBean.getStr("FILE_PATH");
					filepath = filepath.replaceAll("@SYS_FILE_PATH@", rootPath);
//					String filepath = "D:\\TESTone\\one.txt";
				    //转移附件到指定目录
					Map<Object,Object> fileMaps = FileUtil.getFile(filepath,filesPath.toString());
					if(fileMaps != null && fileMaps.size()>0) {
						RecordRequestFiles file = new RecordRequestFiles();
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
	        maps.put(gwId, d);
		}
		return maps;
	}
	
	/**
	 * 
	 * @title: handleGW 
	 * @descriptin: 该方法用于将公文数据生成xml文件和将公文数据生成zip
	 * @param @param gws 公文数据
	 * @param @param datestr 系统时间字符 YYYYMMDD
	 * @return void
	 * @throws
	 */
	public void handleGW(Map<String,RecordRequest> gws,Date date,String rootPath) {
		
		if(gws != null && gws.size()>0) {
			//生成gw文件路径
			for (Map.Entry<String, RecordRequest> entry : gws.entrySet()) {
				String gwId = entry.getKey();
				String gwPath = "GWJH/"+sdf1.format(date) + "/"+ sdf2.format(date) + "-" + gwId;
				//生成公文路径
				File gwFile = new File(rootPath+File.separator+gwPath);
				if(!gwFile.exists()) {
					gwFile.mkdirs();
				}
				
				//生成xml
				XStreamUtil.stringToXml(gwFile+"/data.xml", entry.getValue().toString(true));
				
				//生成zip
				try {
					FileToZip.zip(gwFile.toString());
					//删除文件
					FileUtil.deleteFile(gwFile);
					//将生成zip包存到数据库OA_GW_JH_SENDER_INFO表中
					String FILE_PATH = gwFile+".zip";
					String sql = "UPDATE OA_GW_JH_SENDER_INFO set FILE_PATH='"+FILE_PATH+"',"
							+" INSERT_DATE= to_date('"+sdf2.format(date)+"','yyyyMMddHH24miss')"
							+" WHERE GW_ID='"+gwId+"'";
					Transaction.getExecutor().execute(sql);
				} catch (Exception e) {
					log.error("打包zip失败"+e.getMessage(), e);
				}
			}
		}
	}
}
