package com.rh.core.comm.msg;

import java.util.List;

import org.apache.log4j.Logger;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.ConfMgr;
import com.rh.core.org.UserBean;
import com.rh.core.plug.IMailSender;
import com.rh.core.util.Lang;
import com.rh.core.util.lang.Assert;

/**
 * 邮件系统消息发送实现类
 * @author wanglong
 * 
 */
public class MailMsgSender extends AbstractMsgSender {
    private Logger log = Logger.getLogger(this.getClass());
    /**
     * 发送邮件提醒的邮件服务器地址
     */
    private static final String HOST = "MAIL_MESSAGE_HOST";

    /**
     * 发送邮件提醒的邮箱用户名
     */
    private static final String USER_NAME = "MAIL_MESSAGE_USER_NAME";

    /**
     * 发送邮件系统的邮箱密码
     */
    private static final String PASSWORD = "MAIL_MESSAGE_PASSWORD";

    /**
     * 发送邮件提醒的邮箱地址
     */
    private static final String SENDER = "MAIL_MESSAGE_SENDER";

    /**
     * 发送邮件提醒的邮箱是否要求用户验证
     */
    private static final String IF_AUTH = "MAIL_MESSAGE_IF_AUTH";
    
    @Override
    public void sendMsg(Bean msgBean) {
        IMailSender mailSender = createMailSender();
        List<UserBean> receivers = msgBean.getList(MsgItem.RECEIVER_LIST);

        StringBuilder str = new StringBuilder();
        for (UserBean userBean : receivers) {
            str.append(userBean.getEmail()).append(",");
        }
        String mailTo = "";
        if (str.length() > 0) {
            mailTo = str.substring(0, str.length() - 1);
        }

        Assert.hasText(mailTo, "接收人的邮箱不能为空！");
        // 接收人
        mailSender.setMailTo(mailTo);

        // 标题
        mailSender.setSubject(msgBean.getStr(MsgItem.REM_TITLE));
        // 内容
        mailSender.setBody(msgBean.getStr(MsgItem.REM_CONTENT));
        
        try {
            mailSender.send();
            addLog(receivers, true, mailTo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addLog(receivers, false, mailTo);
        }
       
    }
    
    /**
     * 增加日志
     * @param receivers 邮件接收用户
     * @param isOk 是否成功
     * @param mailTo 操作日志
     */
    private void addLog(List<UserBean> receivers, boolean isOk, String mailTo) {
        for (UserBean userBean : receivers) {
            if (isOk) {
                super.addSuccessExecResult(userBean.getId(), "[mail:" + mailTo + "]");
            } else {
                super.addFailtureExecResult(userBean.getId(), "[mail:" + mailTo + "]");
            }
        }
    }

    /**
     * 
     * @return 邮件发送对象
     */
    private IMailSender createMailSender() {

        String userName = ConfMgr.getConf(USER_NAME, "");
        String password = ConfMgr.getConf(PASSWORD, "");
        String host = ConfMgr.getConf(HOST, "");
        String senderAddress = ConfMgr.getConf(SENDER, "");
        boolean ifAuth = ConfMgr.getConf(IF_AUTH, false);
        IMailSender mailSender = (IMailSender) Lang.createObject(IMailSender.class, Context.getInitConfig("rh.mail"));
        mailSender.setHost(host);
        mailSender.setUser(userName);
        mailSender.setPassword(password);
        mailSender.setMailFrom(senderAddress);
        mailSender.setAuth(ifAuth);
        mailSender.setBodyIsHTML(true);

        return mailSender;
    }

}
