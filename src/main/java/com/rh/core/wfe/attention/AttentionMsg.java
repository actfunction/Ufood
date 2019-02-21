package com.rh.core.wfe.attention;

import com.rh.core.base.Bean;
import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.MsgCenter;

/**
 * 关注的消息类
 * @author wanglong
 *
 */
public class AttentionMsg implements Msg {

    private Bean attention = null;
    
    /**
     * 
     * @param attention 关注的内容
     */
    public AttentionMsg(Bean attention) {
        this.attention = attention;
    }
    
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getType() {
        return MsgCenter.ATTENTION_MSG_TYPE;
    }

    @Override
    public Bean getBody() {
        return attention;
    }

}
