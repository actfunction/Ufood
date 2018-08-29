package com.fe.ufood.dao;

import java.util.List;

import com.fe.ufood.entity.Area;


public interface AreaDao {

	/***
	 * 列出区域列表
	 * @return areaList
	 */
	List<Area> queryArea();
}