package com.rh.gw.util;



import java.util.ArrayList;
import java.util.List;

/**
 * 分页工具类
 * @author kfzz-yxb
 * @param <>
 */
public class GwPageObject<T> {
	private List<T> beanList = new ArrayList<T>();
	
	private Integer pageCurrent;
	private Integer pageCount;
	private Integer rowCount;
	private Integer pageSize;
	
	

	public Integer getPageCurrent() {
		return pageCurrent;
	}

	public void setPageCurrent(Integer pageCurrent) {
		this.pageCurrent = pageCurrent;
	}

	public Integer getPageCount() {
		pageCount = rowCount/pageSize;
		if(rowCount%pageSize!=0){
			pageCount++;
		}
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public List<T> getList() {
		return beanList;
	}

	public void setList(List<T> list) {
		this.beanList = list;
	}
	
}
