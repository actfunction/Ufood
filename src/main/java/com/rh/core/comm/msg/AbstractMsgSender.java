package com.rh.core.comm.msg;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rh.core.base.Bean;
import com.rh.core.util.lang.Assert;

/**
 * 消息发送接口的抽象类
 * @author wanglong
 * 
 */
public abstract class AbstractMsgSender implements MsgSender {
    /**日志**/
    protected Logger log = Logger.getLogger(getClass());
    
    /***
     * 记录发送状态
     */
    protected Map<String, MsgResultBean> resultMap = new HashMap<String, MsgResultBean>();

    /**
     * 
     * @param msgBean 验证MsgBean中的数据是否符合条件。
     */
    protected void validateBeanData(Bean msgBean) {
        Assert.notNull(msgBean.getList(MsgItem.RECEIVER_LIST),
                "接收人列表" + MsgItem.RECEIVER_LIST + "的值不能为NULL");

        Assert.hasText(msgBean.getStr(MsgItem.REM_TITLE), "提醒标题不能为空！");
        Assert.notNull(msgBean.getStr(MsgItem.SEND_USER), "发送人" + MsgItem.RECEIVER_LIST + "的值不能为NULL");
    }

    @Override
    public void send(Bean msgBean) {
        this.validateBeanData(msgBean);

        this.sendMsg(msgBean);
    }

    /**
     * 发送消息
     * @param msgBean 消息数据载体
     */
    public abstract void sendMsg(Bean msgBean);
    
    @Override
    public MsgResultBean getExecResult(String userCode) {
        MsgResultBean bean = resultMap.get(userCode);
        return bean;
    }
    
    /**
     * 
     * @param userCode 用户编号
     * @param log 日志
     */
    public void addSuccessExecResult(String userCode, String log) {
        MsgResultBean bean = resultMap.get(userCode);
        if (bean == null) {
            bean = new MsgResultBean();
            resultMap.put(userCode, bean);
        }

        bean.setOk(true);
        bean.setExecLog(log);
    }
    
    /**
     * 
     * @param userCode 用户编号
     * @param log 日志
     */
    public void addFailtureExecResult(String userCode, String log) {
        MsgResultBean bean = resultMap.get(userCode);
        if (bean == null) {
            bean = new MsgResultBean();
            resultMap.put(userCode, bean);
        }

        bean.setOk(false);
        bean.setExecLog(log);
    }
}
