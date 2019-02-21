package com.rh.gw.gdjh.record;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@SuppressWarnings("serial")
@XStreamAlias("file")
public class RecordRequestFiles implements Serializable{
	
	@XStreamAlias("soft")
	private String soft;
	
	@XStreamAlias("filename")
	private String filename;
	
	
	@XStreamAlias("docType")
	private String docType;
	
	@XStreamAlias("size")
	private String size;
	
	@XStreamAlias("pageCount")
	private String pageCount;
	
	@XStreamAlias("createTime")
	private String createTime;
	
	@XStreamAlias("updateTime")
	private String updateTime;

	public String getSoft() {
		return soft;
	}

	public void setSoft(String soft) {
		this.soft = soft;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getPageCount() {
		return pageCount;
	}

	public void setPageCount(String pageCount) {
		this.pageCount = pageCount;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	
	
	
	
	
	
	
	

}
