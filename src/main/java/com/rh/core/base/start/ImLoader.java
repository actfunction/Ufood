/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.start;

import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.plug.im.ImListener;
import com.rh.core.plug.im.ImMgr;
import com.rh.core.util.Lang;

/**
 * 处理IM的启动和关闭。
 * 
 * @author wanglong
 * @version $Id$
 */
public class ImLoader {
	
    /**
     * 初始化IM资源
     */
    public void start() {
        String  className = BaseContext.getInitConfig("rh.im", "");
        if (className.length() > 0) {
            ImListener im = (ImListener) Lang.createObject(className);
            if (im.init()) {
                ImMgr.setIm(im);
                BaseContext.setApp(APP.IM, true);
                System.out.println("IM is Ok!  class:" + className);
            }
        }
    }
    
    /**
     * 释放rtx资源
     */
    public void stop() {
        ImListener im = ImMgr.getIm();
        if (im != null) {
            im.close();
        }
    }
}