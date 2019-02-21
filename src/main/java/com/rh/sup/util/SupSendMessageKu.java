package com.rh.sup.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.gw.gdjh.exception.MqException;
import com.rh.gw.util.GwEspaceUtil;


/**
 * 督查发送消息枚举库
 * 所有消息内容都需在此处定义
 * 流程相关消息命名以WFE开头，非流程相关的消息命名以NOWFE开发，如果有参数的话，命名后面以_P结尾,
 * 消息内容里面的参数以%s通配符适配
 * 注意！注意！注意！%s占位符的各位必须和调用该方法是传入的参数个数
 * @author kfzx-xuqin
 *
 */
enum SupMsgKu{
	WFE_SUP_APPRO_NOTICE("您新增有1条督查通知单，请登入行政办公子系统进行处理。","WFE_SUP_APPRO_NOTICE"),
	WFE_SUP_APPRO_URGE("您新增有1条督查催办单，请登入行政办公子系统进行处理。","WFE_SUP_APPRO_URGE"),
	WFE_SUP_APPRO_NOTICE_P("您新增有%s条%s自动%s立项%s的督查%s事项，请登入行政办公子系统进行处理。","WFE_SUP_APPRO_NOTICE_P");
	
	private String msg;
	private String msgCode;
	
	private SupMsgKu(String msg,String msgCode){
		this.msg = msg;
		this.msgCode = msgCode;
	}
	
	public String getMsg(){
		return this.msg;
	}
	public void setMsg(String msg){
		this.msg = msg;
	}
	public String getMsgCode(){
		return this.msgCode;
	}
	public void setMsgCode(String msgCode){
		this.msgCode = msgCode;
	}
	
	public static String getMsg(String msgCode){
		for(SupMsgKu supMsgKu : SupMsgKu.values()){
			if(supMsgKu.getMsgCode().equals(msgCode)){
				return supMsgKu.getMsg();
			}
		}
		throw new MqException("SupMsgKu 中" + msgCode + " is undefined."); 
	}
	
}


/**
 * 督查消息发送--消息内容资源类
 * @author kfzx-xuqin
 *
 */
public class SupSendMessageKu {
	
	private static Log log = LogFactory.getLog(SupSendMessageKu.class);
	
	/**
	 * 获取发送消息内容方法
	 * @param msgCode:消息模板类型
	 * @param paramBean：发送消息的参数/参考GwEspaceUtil类的sendESpaceMsg方法
	 * @param msgParam
	 * @return
	 */
	public static String getSupMsg(String msgCode,String... msgParam){
		//根据msgCode获取消息模板
		String sendMsg = SupMsgKu.getMsg(msgCode);
		//将消息模板里面占位符转化为参数
		for(String arr:msgParam){
			try {
				sendMsg = String.format(sendMsg, msgParam);
			} catch (Exception e) {
				// TODO: handle exception
				log.error("参数个数与消息常量中占位符个数不匹配！");
			}
		}
		return sendMsg;
	}
	/**
	 * 发送消息
	 * @param msgCode
	 * @param paramBean
	 * @param msgParam
	 * @return
	 */
	public static OutBean sendSupMsg(String msgCode ,ParamBean paramBean,String... msgParam){
		OutBean outbean = new OutBean();
		//获取消息内容
		String sendMsg = getSupMsg(msgCode, msgParam);
		//赋值消息内容
		paramBean.set("SEND_MESSAGE", sendMsg);
		//发送消息，并返回状态值
		outbean = GwEspaceUtil.sendESpaceMsg(paramBean);
		
		return outbean;
	}
	//测试类
//	public static void main(String[] args) {
//		
//		String ss = "WFE_SUP_APPRO_NOTICE_P";
//		ParamBean paramBean = new ParamBean();
//		OutBean sendMsgResult = sendSupMsg(ss, paramBean,"2","canshu3","canshu4");
//		System.out.println(sendMsgResult);
//	}
	
}
