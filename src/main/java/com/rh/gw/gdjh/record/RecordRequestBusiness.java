package com.rh.gw.gdjh.record;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@SuppressWarnings("serial")
@XStreamAlias("behavior")
public class RecordRequestBusiness implements Serializable {
	
	@XStreamAlias("action")
	private String action;
	
	@XStreamAlias("description")
	private String description;
	
	@XStreamAlias("time")
	private String time;
	
	@XStreamAlias("jg")
	private String jg;
	
	@XStreamAlias("ry")
	private String ry;
	
	@XStreamAlias("state")
	private String state;

	@XStreamAlias("filename")
	private String filename;
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getJg() {
		return jg;
	}

	public void setJg(String jg) {
		this.jg = jg;
	}

	public String getRy() {
		return ry;
	}

	public void setRy(String ry) {
		this.ry = ry;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
