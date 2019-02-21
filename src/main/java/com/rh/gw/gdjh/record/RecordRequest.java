package com.rh.gw.gdjh.record;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import com.rh.gw.gdjh.util.XStreamUtil;

@SuppressWarnings("serial")
@XStreamAlias("record")
public class RecordRequest implements Serializable{

	@XStreamAlias("source")
	private String source;
	
	@XStreamAlias("entity")
	private String entity;
	
	@XStreamAlias("id")
	private String id;
	
	@XStreamAlias("parentId")
	private String parentId;
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public RecordRequestFields getRecordFields() {
		return recordFields;
	}

	public void setRecordFields(RecordRequestFields recordFields) {
		this.recordFields = recordFields;
	}

	public List<RecordRequestBusiness> getRecordBusiness() {
		return recordBusiness;
	}

	public void setRecordBusiness(List<RecordRequestBusiness> recordBusiness) {
		this.recordBusiness = recordBusiness;
	}

	public List<RecordRequestFiles> getRecordfiles() {
		return recordfiles;
	}

	public void setRecordfiles(List<RecordRequestFiles> recordfiles) {
		this.recordfiles = recordfiles;
	}

	@XStreamAlias("fields")
	private RecordRequestFields recordFields;
	
	@XStreamAlias("business")
	private  List<RecordRequestBusiness> recordBusiness;
	
	@XStreamAlias("files")
	private  List<RecordRequestFiles> recordfiles;
	
	public String toString() {
		return toString(false);
	}

	public  String  toString(boolean replaceBlank) {
	    String outputStr=XStreamUtil.objectToXml(this);
	    String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + outputStr;
	    		
//	    if(replaceBlank) {
//	    	return StringUtil.replaceBlank();
//	    
//	    }
		return result;
	}

    	
	
}
