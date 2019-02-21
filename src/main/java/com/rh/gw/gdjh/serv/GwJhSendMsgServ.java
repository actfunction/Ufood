package com.rh.gw.gdjh.serv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.gw.gdjh.tlq.GwGdSenderMsg;

/**
 * 
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhangheng1
 * @date: 2018-12-6 下午6:12:03
 * @version: V1.0
 * @description: 该类用于公文交换定GwId和DeptCode的校验，校验通过后将待交换的公文插入的OA_GW_JH_SENDER_INFO表中
 */
public class GwJhSendMsgServ extends CommonServ { 

	/** log */
	private static Log log = LogFactory.getLog(GwJhSendMsgServ.class);
	
	public void sendMsgTest(ParamBean paramBean) { 
		String gwId = paramBean.getStr("GW_ID");
		String dqiId = paramBean.getStr("DQI_ID");
		try {
			List<String> list = sendMsg(gwId, dqiId);
			if(list!=null && !list.isEmpty()) {
				System.out.println(list.get(0)+","+list.get(1));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void deleteTestInfo(ParamBean paramBean) {
		String[] sql = new String[3];
		sql[0] = "delete from OA_GW_JH_SENDER_INFO";
		sql[1] = "delete from OA_GW_JH_RECEIVER_INFO";
		sql[2] = "delete from OA_GW_GD_SENDER_INFO";
		Transaction.getExecutor().executeBatch(sql);
	}
	
	public void deleteQueueInfo(ParamBean paramBean) {
		String sql = "delete from OA_GW_DEPT_QUEUE_INFO";
		Transaction.getExecutor().execute(sql);
	}
	
	/**
	 * 
	 * @title: SendMsg
	 * @descriptin: 该方法用于公文交换定GwId和DeptCode的校验，校验通过后将待交换的公文插入的OA_GW_JH_SENDER_INFO表中
	 * @param GwId 公文主键
	 * @param DeptCode 公文交换机构主键
	 * @return List<String> 第一个参数：状态 1--成功消息 2--失败消息，第二个参数：返回消息
	 * @throws
	 */
	public List<String> sendMsg(String GwId, String DeptCode) {
//		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String dateStr = sdf2.format(date);

		List<String> list = new ArrayList<String>();
		try {
			//GwId为空
			if(GwId == null || GwId.isEmpty()) {
				list.add("2");
				list.add("公文ID不存在");
				return list;
			}
			//判断数据库是否有GwId
			String GwIdSql = "select count(*) from OA_GW_GONGWEN where GW_ID = '"+GwId+"'";
			int num1 = Transaction.getExecutor().count(GwIdSql);
			if(num1 <=0) {
				list.add("2");
				list.add("公文不存在");
				return list;
			}
			
			//DeptCode为空
			if(DeptCode == null || DeptCode.isEmpty()) {
				list.add("2");
				list.add("公文交换机构ID不存在");
				return list;
			}
			//判断数据库是否有DeptCode
			String DeptCodeSql = "select count(*) from OA_GW_DEPT_QUEUE_INFO where DQI_ID = '"+DeptCode+"'";
			int num2 = Transaction.getExecutor().count(DeptCodeSql);
			if(num2 <=0) {
				list.add("2");
				list.add("公文交换机构未配置");
				return list;
			}
			
			
			//判断GwId是否入库
			String GwIdJHSql = "select count(*) from OA_GW_JH_SENDER_INFO where GW_ID='"+GwId+"' and DQI_ID='"+DeptCode+"'";
			int num3 = Transaction.getExecutor().count(GwIdJHSql);
			if(num3 > 0) {
				list.add("1");
				list.add("公文已经入库");
				return list;
			}else {
		        //GwId入库
				String sql = "insert into OA_GW_JH_SENDER_INFO (GW_ID,DQI_ID,STATUS,DEALING_DATE,INSERT_DATE) "
					+ "values ('"+GwId+"','"
					+ DeptCode+"',"
					+ "'0','"
					+ dateStr+"'," 
					+ "to_date('"+dateStr+"','yyyyMMddHH24miss')"
					+ ")";
				int num4 = Transaction.getExecutor().execute(sql);
				if(num4 > 0 ) {
					list.add("1");
					list.add("公文入库成功");
					return list;
				}else {
					list.add("2");
					list.add("公文入库失败");
					return list;
				}
			}
		} catch (Exception e) {
			log.error("校验失败"+e.getMessage()+","+e.getCause().getMessage());
			list.add("2");
			list.add("校验失败:"+e.getMessage());
		}
//		list.add("2");
//		list.add("校验失败");
		return list;
	}
	
	public void sendJhBfData(ParamBean paramBean) throws Exception {
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
		int len= Integer.parseInt(paramBean.getStr("i"));
		List<Bean> list = new ArrayList<Bean>();
		for(int i=0;i<len;i++){
			String gwId = paramBean.getStr("arrayObj["+i+"][GW_ID]");
			String filePath = paramBean.getStr("arrayObj["+i+"][FILE_PATH]");
			String dqiId = paramBean.getStr("arrayObj["+i+"][DQI_ID]");
			Bean b = new Bean();
			b.set("GW_ID", gwId);
			b.set("FILE_PATH", filePath);
			b.set("DQI_ID", dqiId);
			list.add(b);
		}
		GwGdSenderMsg sendMsg = new GwGdSenderMsg(sdf2.format(new Date()));
		sendMsg.callMqReq(GwGdSenderMsg.windqName, GwGdSenderMsg.localQueueName, list, 100);
	}
	
//	public void sendJhBfData(ParamBean param) throws Exception {
//		String datastr = param.getStr("DATA_LIST");
//		Map<String, List<Bean>> map = new HashMap<String, List<Bean>>();
//		List<Bean> beanList = JsonUtils.toBeanList(datastr);
//		for(Bean bean:beanList) { 
//			String dqiId = bean.getStr("DQI_ID");
//			if(map.containsKey(dqiId)) {
//				List<Bean> temp = map.get(dqiId);
//				temp.add(bean);
//				map.put(dqiId, temp);
//			}
//			else {
//				List<Bean> temp = new ArrayList<Bean>();
//				temp.add(bean);
//				map.put(dqiId, temp);
//			}
//		}
//		GwJhSenderMsg sendMsg = new GwJhSenderMsg();
//		String deptSql = "SELECT * FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_TYPE='1'";
//		Bean bean = Transaction.getExecutor().queryOne(deptSql);
//		String localQueue = bean.getStr("DQI_ID");
//		Iterator<String> iter = map.keySet().iterator();
//		while(iter.hasNext()) {
//			String dqiId = iter.next();
//			sendMsg.callMqReq(dqiId, localQueue, map.get(dqiId), 100);
//		}
//	}
	
	public OutBean upStatus(ParamBean paramBean) {
		String gwId = paramBean.getStr("GW_ID");
		String dqiId = paramBean.getStr("DQI_ID");
		String sql="update OA_GW_JH_SENDER_INFO set STATUS='5' where GW_ID = '"+gwId +"' and DQI_ID='"+dqiId+"'";
		 int execute = Transaction.getExecutor().execute(sql);
		 OutBean outBean = new OutBean();
		 outBean.set("msg", "ok");
	    return outBean;
	}

	public void downFile(ParamBean paramBean) {
		
		FileInputStream in = null;
		 OutputStream out = null;
		try {
			HttpServletResponse response = Context.getResponse();
		    HttpServletRequest request = Context.getRequest();
			
		    String FILE_PATH = paramBean.getStr("FILE_PATH");
		    if(FILE_PATH != null && !FILE_PATH.isEmpty()) {
		    	File file = new File(FILE_PATH);
		    	String fileName = file.getName();
		    	
		    	response.resetBuffer();
		        response.setContentType("application/x-msdownload");
		        response.setCharacterEncoding("UTF-8");
		        com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName);
		        
		        //获取文件流
		        in = new FileInputStream(file);
		        out = response.getOutputStream();
		    	
		        byte[] by=new byte[1024];
				int c;
				while((c=in.read(by))!=-1) {
					out.write(by,0,c);
				}
				response.flushBuffer();
		    	
		    }
		} catch (Exception e) {
			log.error("文件下载异常："+e.getMessage()+","+e.getCause().getMessage());
		}finally {
			
			if(out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in!=null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
