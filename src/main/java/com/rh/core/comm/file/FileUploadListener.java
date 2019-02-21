package com.rh.core.comm.file;

import java.util.List;

import com.rh.core.base.Bean;

/**
 * 文件上传监听类
 * @author wanglong
 * 
 */
public interface FileUploadListener {
    /**
     * 文件上传配置的前缀
     */
    String CONF_PREFIX = "FILE_UPLOAD_LISTENER_";

    /**
     * 更新文件之前调用
     * @param paramBean request中的获取的参数数据
     */
    void beforeUpdate(Bean paramBean);

    /**
     * 删除文件之前调用此方法
     * @param paramBean request中的获取的参数数据
     */
    void befortAdd(Bean paramBean);

    /**
     * 更新服务器文件之后调用
     * @param paramBean request中的获取的参数数据
     * @param dataList 修改后的数据列表对象
     */
    void afterUpdate(Bean paramBean, List<Bean> dataList);

    /**
     * 上传文件之后调用
     * @param paramBean request中的获取的参数数据
     * @param dataList 修改后的数据列表对象
     */
    void afterAdd(Bean paramBean, List<Bean> dataList);
}
