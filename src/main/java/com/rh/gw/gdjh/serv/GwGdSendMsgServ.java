package com.rh.gw.gdjh.serv;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.gw.gdjh.tlq.GwGdSenderMsg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Context;

public class GwGdSendMsgServ extends CommonServ {
	/** log */
	private static Log log = LogFactory.getLog(GwJhSendMsgServ.class);
	
	public void sendGdBfData(ParamBean paramBean) throws Exception {
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
		int len= Integer.parseInt(paramBean.getStr("i"));
		List<Bean> list = new ArrayList<Bean>();
		for(int i=0;i<len;i++){
			String gwId = paramBean.getStr("arrayObj["+i+"][GW_ID]");
			String filePath = paramBean.getStr("arrayObj["+i+"][FILE_PATH]");
			Bean b = new Bean();
			b.set("GW_ID", gwId);
			b.set("FILE_PATH", filePath);
			list.add(b);
		}
		GwGdSenderMsg sendMsg = new GwGdSenderMsg(sdf2.format(new Date()));
		sendMsg.callMqReq(GwGdSenderMsg.windqName, GwGdSenderMsg.localQueueName, list, 100);
	}
	
	public OutBean upStatus(ParamBean paramBean) {
		String GW_ID = paramBean.getStr("GW_ID");
		String sql="update OA_GW_GD_SENDER_INFO set STATUS='5' where GW_ID = '"+GW_ID +"'";
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
