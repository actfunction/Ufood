/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.comm.portal;

import com.rh.core.serv.ParamBean;

/**
 * 模版的基本监听类
 * 
 * @author wanglong
 */
public interface PortalListenInterface {
    /**
     * 模版展示处理前的的参数监听处理
     * @param paramBean 参数信息
     * @return paramBean 处理后的参数
     */
    ParamBean beforeInputParamBean(ParamBean paramBean);
}
