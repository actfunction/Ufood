package com.rh.core.plug.search;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 搜索处理接口类
 * @author wanglong
 *
 */
public interface ISearchServ {
    /**
     * 显示搜索页面
     * 
     * @param paramBean 参数信息
     * @return outBean 输出结果
     */
    OutBean show(ParamBean paramBean);
    
    
    /**
     * 搜索方法
     * 
     * @param paramBean 参数信息
     * @return outBean 输出结果
     */
    OutBean query(ParamBean paramBean);
    
    /**
     * 根据索引ID删除索引
     * @param paramBean 参数信息，要是设置ID
     * @return 删除结果信息
     */
    OutBean delete(ParamBean paramBean);
    
    /**
     * 右侧分组信息
     * @param paramBean 参数
     * @return 分组信息
     */
    OutBean groupBy(ParamBean paramBean);
    
    /**
     * 预览信息
     * @param paramBean 参数
     * @return 预览信息
     */
    OutBean preview(ParamBean paramBean);
    
    /**
     * 相关检索
     * @param paramBean 参数
     * @return 相关检索信息
     */
    OutBean relevantSearch(ParamBean paramBean);
}