package com.rh.core.util.msg;

import com.rh.core.base.Bean;

/**
 * 消息
 * @author wanglong
 */
public interface Msg {
    /**
     * 获取名称
     * @return 名称 
     */
    String getName();
    /**
     * 获取类别
     * @return 类别
     */
    String getType();
    /**
     * 获取消息体
     * @return 消息体
     */
    Bean getBody();
}
