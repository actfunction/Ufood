package com.rh.core.util.msg.listener;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 服务器操作外部URL监听
 * @author wanglong
 */
public class ServActURLMsgListener extends ServActMsgListener {
    private static Log log = LogFactory.getLog(ServActURLMsgListener.class);
    private String url;
    /**
     * 服务操作消息监听
     * @param serv 服务
     * @param act 操作
     * @param url url
     */
    public ServActURLMsgListener(String serv, String act, String url) {
        super(serv, act);
        this.url = url;
    }
    @Override
    protected void onActMsg(ServActMsg actMsg) {
        try {
            new URL(this.url).openStream().close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
