/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util.file;


import java.io.InputStream;

/**
 * 文件行处理器
 * @author wanglong
 */
public interface FileHandler {
    /**
     * 处理行数据
     * @param fileName 文件名
     * @param in 输入流
     */
    void handle(String fileName, InputStream in);
}
