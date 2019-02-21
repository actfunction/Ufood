package com.rh.core.plug.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.plug.IMailSender;

public class MailSender implements IMailSender {

	/** log */
	private static Log log = LogFactory.getLog(MailSender.class);

	/**
	 * 附件Bean字段信息
	 */
	private static enum FILE_ITEM {
		/** 文件名 **/
		FILE_NAME,
		/** 文件路径 **/
		FILE_PATH;
	}

	/** 邮件正文 */
	private String body = null;

	/** 抄收人地址 */
	private String cc = null;

	/** 邮件服务器地址 */
	private String host = null;

	/** 收件人地址 */
	private String mailTo = null;

	/** 发件人地址 */
	private String mailFrom = null;

	/** 邮件主题 */
	private String subject = null;

	/** 以html格式发送正文类型邮件 */
	private boolean bodyIsHTML = false;

	/** 邮件用户名 */
	private String userName = null;

	/** 邮件用户密码 */
	private String password = null;

	/** 是否显示debug */
	private boolean debug = false;

	/** 邮件的附件 */
	private List<Bean> fileList = new ArrayList<Bean>();

	/** SMTP服务器是否要求身份验证 */
	private boolean isAuth = false;
	
	public MailSender() {

	}

	/**
	 * 创建邮件Message对象
	 * 
	 * @param session
	 *            Session对象
	 * 
	 * @return 创建后的Message对象
	 * @throws MessagingException
	 *             异常
	 * 
	 */
	private Message createMsg(Session session) throws MessagingException {
		MimeMessage msg = new MimeMessage(session);
		InternetAddress[] toAddrs = null;
		InternetAddress[] ccAddrs = null;

		if (mailTo != null) {
			toAddrs = InternetAddress.parse(mailTo, false);
		}

		msg.setRecipients(Message.RecipientType.TO, toAddrs);

		if (mailFrom != null) {
			msg.setFrom(new InternetAddress(mailFrom));
		}

		if (cc != null) {
			ccAddrs = InternetAddress.parse(cc, false);
			msg.setRecipients(Message.RecipientType.CC, ccAddrs);
		}

		// 设置发件日期
		msg.setSentDate(new java.util.Date());

		if (subject != null) {
			msg.setSubject(subject, "UTF-8");
		}

		addMailContent(msg);

		msg.saveChanges();

		return msg;
	}

	/**
	 * 为邮件体对象增加内容
	 * 
	 * @param msg
	 *            邮件体对象
	 * @exception MessagingException
	 *                exception
	 */
	private void addMailContent(Message msg) throws MessagingException {
		if (this.fileList.size() > 0) { // 如果有附件
			Multipart mp = new MimeMultipart();

			mp.addBodyPart(createMailBody());

			for (int i = 0; i < fileList.size(); i++) { // 张贴

				MimeBodyPart mbp2 = new MimeBodyPart();
				Bean ai = fileList.get(i);

				File aFile = new File(ai.getStr(FILE_ITEM.FILE_PATH));

				if (aFile.exists()) {
					FileDataSource fds = new FileDataSource(ai.getStr(FILE_ITEM.FILE_PATH));
					mbp2.setDataHandler(new DataHandler(fds));
					mbp2.setFileName(ai.getStr(FILE_ITEM.FILE_NAME));
				}

				mp.addBodyPart(mbp2);
			}

			msg.setContent(mp);
		} else {
			if (bodyIsHTML) {
				msg.setContent(body, "text/html; charset=UTF-8");
			} else {
				msg.setContent(body, "text/plain; charset=UTF-8");
			}
		}
	}

	/**
	 * 
	 * @return 把正文放到MimeBodyPart对象中
	 * @throws MessagingException
	 *             exception
	 */
	private MimeBodyPart createMailBody() throws MessagingException {
		MimeBodyPart mbp1 = new MimeBodyPart();

		if (bodyIsHTML) {
			mbp1.setContent(body, "text/html;charset=UTF-8");
		} else {
			mbp1.setContent(body, "text/plain;charset=UTF-8");
		}

		return mbp1;
	}

	/**
	 * 上传附件 文件名称 文件路径
	 * 
	 * @param fileName
	 *            文件名
	 * @param filePath
	 *            文件路径
	 * 
	 */
	public void addFile(String fileName, String filePath) {
		Bean bean = new Bean();
		bean.set(FILE_ITEM.FILE_NAME, fileName);
		bean.set(FILE_ITEM.FILE_PATH, filePath);
		this.fileList.add(bean);
	}

	/**
	 * 发送邮件
	 * 
	 * @throws MessagingException
	 *             异常
	 */
	public void send() {
		Session session = createSession();
		Message message;
		try {
			message = createMsg(session);
			Transport.send(message);
		} catch (MessagingException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @return 取得发送邮件所需要的属性
	 **/
	private Properties getMailProperties() {
		Properties props = new Properties();
		props.put("file.encoding", "UTF-8");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.connectiontimeout", "60000"); // timeout 1 mins
		props.put("mail.smtp.timeout", "120000"); // timeout 2 mins

		if (isAuth) {
			props.put("mail.smtp.auth", "true"); // 是否需要密码验证
		}

		return props;
	}

	/**
	 * @return 取得发送邮件所需要的Mail Session
	 **/
	private Session createSession() {
		Properties props = getMailProperties();
		Session session = null;

		if (isAuth) {
			Authenticator authenticator = new SmtpAuthenticator(userName, password);

			session = Session.getInstance(props, authenticator);
		} else {
			session = Session.getInstance(props, null);
		}

		session.setDebug(debug);

		return session;
	}

	/**
	 * 发送邮件的SMTP服务器的地址
	 * 
	 * @return 邮件服务器地址
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * 发送邮件的SMTP服务器的地址
	 * 
	 * @param host
	 *            邮件服务器地址
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return 是否允许调试
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param b
	 *            设置参数是否允许调试
	 */
	public void setDebug(boolean b) {
		debug = b;
	}

	/**
	 * 发送邮件的SMTP服务器是否要求身份验证
	 * 
	 * @return 是否要求身份验证
	 */
	public boolean isAuth() {
		return isAuth;
	}

	/**
	 * @param b
	 *            发送邮件的SMTP服务器是否要求身份验证
	 */
	public void setAuth(boolean b) {
		isAuth = b;
	}

	/**
	 * 设置发件人名称
	 * 
	 * @param user
	 *            发件人名称
	 */
	public void setUser(String user) {
		this.userName = user;
	}

	/**
	 * 设置发件人连接服务器密码
	 * 
	 * @param password
	 *            发件人密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 收件人地址
	 * 
	 * @param recipients
	 *            收件人地址
	 */
	public void setMailTo(String recipients) {
		this.mailTo = recipients;
	}

	/**
	 * 发件人地址
	 * 
	 * @param sender
	 *            发件人地址
	 */
	public void setMailFrom(String sender) {
		this.mailFrom = sender;
	}

	/**
	 * 设置邮件标题
	 * 
	 * @param subject
	 *            设置邮件标题
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * 设置抄收人地址
	 * 
	 * @param cc
	 *            抄收人地址
	 */
	public void setCc(String cc) {
		this.cc = cc;
	}

	/**
	 * 设置邮件正文
	 * 
	 * @param body
	 *            邮件正文
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * 设置是否发送邮件为HTML格式
	 * 
	 * @param value
	 *            如果为Html格式邮件，则设置为真
	 */
	public void setBodyIsHTML(boolean value) {
		bodyIsHTML = value;
	}

}
