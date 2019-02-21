/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.plug.im;




/**
 * 处理IM相关的操作。
 * 
 * @author wanglong
 * @version $Id$
 */
public class ImMgr {
    /** im listener 对象 */
    private static ImListener im = null;    
    /**
     * 设置IM实现类
     * @param imListener IM实现类
     */
    public static void setIm(ImListener imListener) {
        im = imListener;
    }
    /**
     * 获取IM实现类
     * @return IM实现类
     */
    public static ImListener getIm() {
        return im;
    }
}