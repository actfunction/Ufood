package com.rh.core.plug;

import com.rh.core.base.Bean;

/**
 * XDOC格式化文件流输出接口类
 * @author wanglong
 *
 */
public interface IXdoc {
    
    /**
     * XDOC文件格式化输出流
     * @param paramBean XDOC模板参数,要求提供以下参数：
     * filePath：XDOC模版路径，从WebRoot下的相对路径，例如：sy/xdoc/data_construction.xdoc
     * fileName：生成的文件名，支持中文，不带后缀
     * format：文件格式，支持doc、docx、pdf、xls、xlsx、wps、txt、html
     * data：数据内容，可以是bean也可以是list<bean>，根据模版具体要求的数据提供
     */
    void outputXdoc(Bean paramBean);
}
