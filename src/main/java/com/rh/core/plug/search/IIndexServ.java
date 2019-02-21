package com.rh.core.plug.search;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDefBean;

/**
 * 索引处理接口类
 * @author wanglong
 *
 */
public interface IIndexServ {

	/**
	 * 保存数据的全文索引到消息队列
	 * @param servDef 服务定义
	 * @param dataBean 数据信息
	 */
	void saveIndexMsg(ServDefBean servDef, Bean dataBean);

	/**
     * 保存数据的全文索引到消息队列
     * @param index 索引数据
     */
    void saveIndexMsg(ARhIndex index);
    
	/**
	 * 删除服务指定数据的全文索引到消息队列
	 * @param servDef 服务定义
	 * @param dataId 数据主键
	 */
	void deleteIndexMsg(ServDefBean servDef, String dataId);
	
	/**
     * 删除指定的全文索引到消息队列
     * @param index 索引数据
     */
    void deleteIndexMsg(ARhIndex index);
    
    /**
     * 删除指定服务对应的索引的全文索引
     * @param servId 指定服务
     */
    void deleteAll(String servId);
    /**
     * 清除所有的全文索引
     */
    void deletAll();

	/**
	 * 索引增加用户权限
	 * @param servDef 服务定义
	 * @param dataId 数据主键
	 * @param userCode 用户编码
	 */
	void updateIndexGrantUser(ServDefBean servDef, String dataId,
			String userCode);

	/**
	 * 索引增加角色权限
	 * @param servDef 服务定义
	 * @param dataId 数据主键
	 * @param roleCode 角色编码
	 */
	void updateIndexGrantRole(ServDefBean servDef, String dataId,
			String roleCode);

	/**
	 * 索引增加部门权限
	 * @param servDef 服务定义
	 * @param dataId 数据主键
	 * @param deptCode 部门编码
	 */
	void updateIndexGrantDept(ServDefBean servDef, String dataId,
			String deptCode);

	/**
	 * 将数据信息转换为索引需要的消息信息
	 * @param servDef 服务定义
	 * @param dataBean 数据信息
	 * @return 转为索引格式的消息
	 */
	IndexMsg indexMgs(ServDefBean servDef, Bean dataBean);


    /**
     * 保存数据的全文索引
     * @param index 索引数据
     */
    void saveIndex(ARhIndex index);
    /**
     * 删除数据的全文索引
     * @param index 索引数据
     */
    void deleteIndex(ARhIndex index);
}