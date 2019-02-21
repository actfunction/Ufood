package com.rh.core.plug;


/**
 * 邮件发送接口类，可以通过扩展实现
 * @author wanglong
 *
 */
public interface IMailSender {

	/**
	 * 上传附件 文件名称 文件路径
	 * 
	 * @param fileName 文件名
	 * @param filePath 文件路径
	 * 
	 */
	void addFile(String fileName, String filePath);

	/**
	 * 发送邮件
	 */
	void send();

	/**
	 * 发送邮件的SMTP服务器的地址
	 * 
	 * @return 邮件服务器地址
	 */
	String getHost();

	/**
	 * 发送邮件的SMTP服务器的地址
	 * 
	 * @param host 邮件服务器地址
	 */
	void setHost(String host);

	/**
	 * @return 是否允许调试
	 */
	boolean isDebug();

	/**
	 * @param b 设置参数是否允许调试
	 */
	void setDebug(boolean b);

	/**
	 * 发送邮件的SMTP服务器是否要求身份验证
	 * 
	 * @return 是否要求身份验证
	 */
	boolean isAuth();

	/**
	 * @param b 发送邮件的SMTP服务器是否要求身份验证
	 */
	void setAuth(boolean b);

	/**
	 * 设置发件人名称
	 * 
	 * @param user 发件人名称
	 */
	void setUser(String user);

	/**
	 * 设置发件人连接服务器密码
	 * 
	 * @param password 发件人密码
	 */
	void setPassword(String password);

	/**
	 * 收件人地址
	 * 
	 * @param recipients 收件人地址
	 */
	void setMailTo(String recipients);

	/**
	 * 发件人地址
	 * 
	 * @param sender 发件人地址
	 */
	void setMailFrom(String sender);

	/**
	 * 设置邮件标题
	 * 
	 * @param subject 设置邮件标题
	 */
	void setSubject(String subject);

	/**
	 * 设置抄收人地址
	 * 
	 * @param cc 抄收人地址
	 */
	void setCc(String cc);

	/**
	 * 设置邮件正文
	 * 
	 * @param body 邮件正文
	 */
	void setBody(String body);

	/**
	 * 设置是否发送邮件为HTML格式
	 * 
	 * @param value 如果为Html格式邮件，则设置为真
	 */
	void setBodyIsHTML(boolean value);

}