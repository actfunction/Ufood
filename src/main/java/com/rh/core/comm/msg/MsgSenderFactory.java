package com.rh.core.comm.msg;

import com.rh.core.comm.ConfMgr;
import com.rh.core.util.Lang;
import com.rh.core.util.lang.Assert;

/**
 * 消息发送器工厂，根据指定类型创建符合条件的消息发送实现类。
 * @author wanglong
 * 
 */
public class MsgSenderFactory {
    /**
     * 系统配置中，提醒方式的实现类的配置项名称前缀
     */
    public static final String MSG_SENDER_IMPL_PREFIX = "MSG_SENDER_IMPL_PREFIX";

    /**
     * 
     * @param type 提醒方式编码
     * @return 返回提醒方式对应的实现类
     */
    public static MsgSender getMsgSender(String type) {
        Assert.notNull(type);
        String key = MSG_SENDER_IMPL_PREFIX + "_" + type;
        String conf = ConfMgr.getConf(key, "");

        Assert.hasLength(conf, "提醒方式" + type + "的实现类不能为空，必须在系统配置中指定。");

        return (MsgSender) Lang.createObject(MsgSender.class, conf);
    }
}
