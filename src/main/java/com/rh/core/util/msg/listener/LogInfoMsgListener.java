package com.rh.core.util.msg.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.MsgListener;


/**
 * 监听消息，输出日志信息
 * @author wanglong
 */
public class LogInfoMsgListener implements MsgListener {
    private Log log = LogFactory.getLog(LogInfoMsgListener.class);
    @Override
    public void init(String conf) {
    }
    @Override
    public void onMsg(Msg msg) {
        Bean bean = new Bean();
        bean.set("name", msg.getName());
        bean.set("type", msg.getType());
        bean.set("body", msg.getBody());
        log.info("收到消息：" + bean);
    }
}
