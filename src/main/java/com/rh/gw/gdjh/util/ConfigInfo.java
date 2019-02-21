package com.rh.gw.gdjh.util;

 import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

public class ConfigInfo { 
	private static final String tcf = "tongtech.jms.jndi.JmsContextFactory";
	private static final String TLQ="tlq://";
 	private static final String DEFAULT_PORT="10024";
	private static final String DEFAULT_REMOTEIP = "127.0.0.1";
 
	public static final String GW_JH_CONNECTION = "1";
	public static final String GW_GD_CONNECTION = "2";

	//公文交换使用
	public static InitialContext getJhInitialContext() throws Exception {
		  Properties pro = new Properties();
          pro.setProperty(Context.INITIAL_CONTEXT_FACTORY, tcf);
          String sql = "SELECT SERVER_IP,SERVER_PORT FROM OA_GW_DEPT_QUEUE_INFO WHERE DQI_TYPE='1'";
          List<Bean> bean = Transaction.getExecutor().query(sql);
          String port = bean.get(0).getStr("SERVER_PORT");
          String ip = bean.get(0).getStr("SERVER_IP");
          if(ip==null || ip.equals("")) {
        	  throw new Exception("初始化TLQ连接，ip地址为空，请先配置ip地址");
          }
          if(port==null || port.equals("")) {
        	  throw new Exception("初始化TLQ连接，端口为空，请先配置端口");
          }
          pro.setProperty(Context.PROVIDER_URL, TLQ+ip+":"+port);
 	      return new InitialContext(pro);
	}
	
	//公文归档使用
		@SuppressWarnings("unchecked")
		public static InitialContext getGdInitialContext() throws Exception {
			  Properties pro = new Properties();
	          pro.setProperty(Context.INITIAL_CONTEXT_FACTORY, tcf);
	          String ip = null;
	          String port = null;
	          String sql = "SELECT CONF_VALUE FROM SY_COMM_CONFIG WHERE CONF_KEY='GW_GD_SERVER_CONFIG'";
	          List<Bean> beanList = Transaction.getExecutor().query(sql);
	          if(beanList==null || !beanList.isEmpty()) {
	        	  String confValue = beanList.get(0).getStr("CONF_VALUE");
	        	  String[] strarr = confValue.split("\\|");
	        	  ip = strarr[0];
	        	  port = strarr[1];
	          }
	          if(ip==null || ip.equals("")) {
	        	  throw new Exception("初始化TLQ连接，ip地址为空，请先配置ip地址");
	          }
	          if(port==null || port.equals("")) {
	        	  throw new Exception("初始化TLQ连接，端口为空，请先配置端口");
	          }
	          pro.setProperty(Context.PROVIDER_URL, TLQ+ip+":"+port);
	 	      return new InitialContext(pro);
		}
	
	//公文归档使用
	@SuppressWarnings("unchecked")
	public static InitialContext getInitialContext(String ip,String port) throws Exception {
		  Properties pro = new Properties();
		  pro.setProperty(Context.INITIAL_CONTEXT_FACTORY, tcf);
		  if(port==null || port.equals("")) {
			  port=DEFAULT_PORT;
		  }
		  if(ip==null || ip.equals("")) {
			  ip=DEFAULT_REMOTEIP;
		  }
		  
		  pro.setProperty(Context.PROVIDER_URL, TLQ+ip+":"+port);
	      return new InitialContext(pro);
	}
}
