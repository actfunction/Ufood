package com.rh.core.util;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.rh.core.comm.FileMgr;
import com.rh.core.comm.FileStorage;
import com.rh.core.util.lang.Assert;

/**
 * 锁定任务。使集群中多个应用系统之间不会同时执行指定任务，锁定速度及效率比较低，仅用于长期的定时任务。
 * 允许环境可分为单机模式和集群模式。单机模式下，如果发现JVM启动之前的锁文件，则自动清理。集群模式下，超过2小时未完成的任务会被清理。
 * 
 * @author wanglong
 * 
 */
public class TaskLock {
    /** 开始时间 **/
    private static final String BEGIN_TIME = "TaskLock.beginTime";
    
    private static final long TIME_INTERVAL = 2 * 3600 * 1000;
    /**
     * 日志
     * */
    private Logger log = Logger.getLogger(getClass());
    
    /****
     * 任务锁文件的默认保存路径
     */
    private static final String PATH = "taskLock/";

    private String dir = null;

    private String lockName = null;

    /**
     * 是否成功锁定
     */
    private boolean lockSuccess = false;

    /**
     * 
     * @param dir 目录名，如：meeting。为了避免多个任务之间，锁文件名称重复，可以增加一级目录。
     * @param lockName 锁文件的名称。
     */
    public TaskLock(String dir, String lockName) {
        super();
        this.dir = dir;
        this.lockName = lockName;
        Assert.hasText(lockName);
    }

    /**
     * 增加任务锁。
     * @return 如果成功加锁，则返回true，否则返回false。
     */
    public synchronized boolean lock() {
        lockSuccess = false;
        final String path = getPath();
        try {
            if (FileStorage.exists(path)) { //锁已存在，则不允许再加锁
                if (canRemove(path)) {
                    return true;
                }
                return false;
            }
            lockSuccess = FileStorage.createFile(path);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        return lockSuccess;
    }

    /**
     * 释放任务锁。
     */
    public synchronized void release() {
        if (lockSuccess) {
            try {
                FileStorage.deleteFile(getPath());
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }

    /**
     * 
     * @return 获取锁文件路径
     */
    private String getPath() {
        StringBuilder str = new StringBuilder();
        str.append(FileMgr.getRootPath());
        str.append(PATH);
        if (StringUtils.isNotEmpty(dir)) {
            str.append(dir);
            str.append("/");
        }
        str.append(lockName);

        return str.toString();
    }
    
    /**
     * 是否可以移除锁文件。目前只支持单机模式，暂时不支持集群模式。
     * @param path 文件路径
     * @return 是否可以移除锁文件
     */
    private boolean canRemove(String path) {
        String time = System.getProperty(BEGIN_TIME, "");
        long jvmLastModified = 0;
        if (time.length() == 0) {
            jvmLastModified = System.currentTimeMillis();
            System.setProperty(BEGIN_TIME, String.valueOf(jvmLastModified));
        } else {
            jvmLastModified = Long.parseLong(time);
        }

        long fileLastModified = 0;
        try {
            // 文件的产生时间。
            fileLastModified = FileStorage.lastModified(path);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

        long interval = jvmLastModified - fileLastModified;
        // 如果是JVM启动前2小时产生的文件，则允许清除
        if (interval > TIME_INTERVAL) {
            try {
                // 修改文件的时间到当前时间
                FileStorage.touch(path);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
            return true;
        }

        return false;
    }
}
