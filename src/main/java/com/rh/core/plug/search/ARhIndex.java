package com.rh.core.plug.search;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;

/**
 * 索引信息抽象类
 * @author wanglong
 *
 */
public abstract class ARhIndex extends Bean {

    /** index id key */
    public static final String INDEX_ID = "id";

    /** display title key */
    public static final String DISPLAY_TITLE = "title";

    /** content key */
    public static final String CONTENT = "content";

    /** keywords */
    public static final String KEYWORDS = "keywords";

    /** abstract key */
    public static final String DISPLAY_ABSTRACT = "abstract";

    /** preview key */
    public static final String DISPLAY_PREVIEW = "preview";

    /** owner key */
    public static final String OWNER = "owner";

    /** company key */
    public static final String COMPANY = "company";

    /** department key */
    public static final String DEPARTMENT = "department";

    /** service key */
    public static final String SERVICE = "service";

    /** create time key */
    public static final String CREATE_TIME = "create_time";

    /** last modified time key */
    public static final String LAST_MODIFIED = "last_modified";

    /** grantee key */
    public static final String GRANTEE = "grantee";

    /** attachment key */
    public static final String ATTACHMENT = "attachment";

    /** relative data */
    public static final String RELATIVE = "relative";

    /** attachment title key */
    public static final String ATTACHMENT_TITLE = "attachment_title";

    /** attachment id key */
    public static final String ATTACHMENT_ID = "attachment_id";

    /** attachment path key */
    public static final String ATTACHMENT_PATH = "attachment_path";

    /** attachment mime type key */
    public static final String ATTACHMENT_MTYPE = "attachment_mtype";

    /** dynamic string field */
    public static final String DYNAMIC_STR_FIELD = "_strfield";

    /** dynamic number field */
    public static final String DYNAMIC_NUM_FIELD = "_numfield";

    /** dynamic date field */
    public static final String DYNAMIC_DATE_FIELD = "_datefield";

    /** url key */
    public static final String URL = "url";
    
    /** update model */
    public static final String OVERWRITE_UPDATE_INDEX_MODEL = "update_model";

    /**
     * relative type
     */
    public enum RELATIVE_TYPE {
        /** data from search server */
        SY_PLUG_SEARCH_SERV,
        /** data from service */
        SERVICE,
    }

	/**
     * sid
     */
    private static final long serialVersionUID = -5657991437579836597L;

    /**
	 * add keyword
	 * @param keyword - keyword string
	 */
	public abstract void addKeyword(String keyword);

	/**
	 * get keywords list
	 * @return keywords
	 */
	public abstract List<String> getKeywords();

	/**
	 * set boost value(default is 1.0)
	 * @param f boost value
	 */
	public abstract void setBoost(float f);

	/**
	 * get boost value
	 * @return boost value
	 */
	public abstract float getBoost();

	/**
	 * set result abstract <br>
	 * @param disAbstract abstract text support html script <br>
	 *            internal expresstion: ${dynamic-content} base conent
	 */
	public abstract void setAbstract(String disAbstract);

	/**
	 * is over write model for update ?
	 * @return boolean value
	 */
	public abstract boolean isOverwriteModel();

	/**
	 * set search relative data
	 * @param relative - relative query
	 */
	public abstract void addSearchRelative(String relative);

	/**
	 * set relative data
	 * @param dataType - relative type service id
	 * @param relative - relative query
	 */
	public abstract void addRelative(String dataType, String relative);

	/**
	 * get relative
	 * @return relative string
	 */
	public abstract List<String> getRelatives();

	/**
	 * get abstract
	 * @return abstract string
	 **/
	public abstract String getAbstract();

	/**
	 * set preview text
	 * @param preview support html script <br>
	 */
	public abstract void setPreview(String preview);

	/**
	 * get preview text
	 * @return preview text
	 */
	public abstract String getPreview();

	/**
	 * replace preview
	 * @param target - The sequence of char values to be replaced
	 * @param replacement - The replacement sequence of char values
	 * 
	 */
	public abstract void replacePreview(String target, String replacement);

	/**
	 * replace preview
	 * @param regex - the regular expression to which this string is to be matched
	 * @param replacement - The replacement sequence of char values
	 * 
	 */
	public abstract void replaceAllPreview(String regex, String replacement);

	/**
	 * replace abstract
	 * @param target - The sequence of char values to be replaced
	 * @param replacement - The replacement sequence of char values
	 * 
	 */
	public abstract void replaceAbstract(String target, String replacement);

	/**
	 * replace abstract
	 * @param regex - the regular expression to which this string is to be matched
	 * @param replacement - The replacement sequence of char values
	 * 
	 */
	public abstract void replaceAllAbstract(String regex, String replacement);

	/**
	 * Authorized all users (public grantee) for this data
	 * 
	 */
	public abstract void grantAllUsers();

	/**
	 * Authorized users for this data
	 * @param cmpy company id
	 * @param dept deparment id
	 * @param role role id
	 */
	public abstract void grant(String cmpy, String dept, String role);

	/**
	 * Authorized users for this data
	 * @param user user id string
	 */
	public abstract void grantUser(String user);

	/**
	 * Authorized groups for this data
	 * @param group - group id string
	 */
	public abstract void grantGroup(String group);

	/**
	 * Authorized deparment for this data
	 * @param deparment deparment id sting
	 */
	public abstract void grantDept(String deparment);

	/**
	 * Authorized role for this data
	 * @param role role id string
	 */
	public abstract void grantRole(String role);

	/**
	 * Authorized company for this data
	 * @param cmpy company id string
	 */
	public abstract void grantCmpy(String cmpy);

	/**
	 * get trantee list <br>
	 * example: u_david (user: david) <br>
	 * d_product (deparment: product) <br>
	 * r_manager (role: manager)
	 * @return grantee list
	 */
	public abstract List<String> getGranteeList();

	/**
	 * set index id
	 * 
	 * @param rrn Ruaho resource name
	 */
	public abstract void setIndexId(String rrn);

	/**
	 * get index id
	 * 
	 * @return index id
	 */
	public abstract String getIndexId();

	/**
	 * put user defined key-value field, string value(options)
	 * 
	 * @param key field key
	 * @param value field value
	 */
	public abstract void putStrField(String key, String value);

	/**
	 * get user defined key-value field
	 * 
	 * @param key field key
	 * @return string field
	 */
	public abstract String getStrField(String key);

	/**
	 * put user defined key-value field, Numeric Value (options)
	 * 
	 * @param key key
	 * @param numValue numeric value
	 */
	public abstract void putNumField(String key, double numValue);

	/**
	 * put user defined key-value field, Datetime Value (options)
	 * 
	 * @param key key
	 * @param dateValue date value<CODE>Date</CODE>
	 */
	public abstract void putDateField(String key, Date dateValue);

	/**
	 * add attachment
	 * 
	 * @param attId attachment id
	 * @param attPath attachment expression format: protocol//path <BR>
	 *            example: internal://dff-2323-dfdf2-dfdfdf.doc <BR>
	 *            http://localhost:8080/file.doc <BR>
	 *            ftp://localhost:21/ftpfile <BR>
	 * @param attTitle display title
	 */
	public abstract void addAtt(String attId, String attPath, String attTitle);
	
	
	/**
	 * 添加附件
	 * @param attBean - 附件bean
	 * bean里可包含 TITLE, CONTENT, PATH, ID 
	 * 
	 */
	public abstract void addAtt(Bean attBean);
    
    

    /**
     * get attachment list
     * @return attachment list
     */
    public abstract List<Bean> getAttList();

	/**
	 * get dynamic field
	 * 
	 * @return fields map
	 */
	public abstract Map<String, Object> getFields();

	/**
	 * get title
	 * 
	 * @return title string
	 */
	public abstract String getTitle();

	/**
	 * get content
	 * 
	 * @return content string
	 */
	public abstract String getContent();

	/**
	 * get owner id
	 * 
	 * @return user id
	 */
	public abstract String getOwner();

	/**
	 * get department id
	 * 
	 * @return dept id
	 */
	public abstract String getDept();

	/**
	 * get company
	 * 
	 * @return cmpy
	 */
	public abstract String getCmpy();
	
	/**
     * get act
     *    ServMgr.ACT_ADD: 添加
     *    ServMgr.ACT_DELETE:删除
     *    ServMgr.ACT_UPDATE:修改
     * @return act
     */
    public abstract String getAct();

	/**
	 * get create time
	 * 
	 * @return create time
	 */
	public abstract Date getCreateTime();

	/**
	 * get last modified time
	 * 
	 * @return modified time
	 */
	public abstract Date getLastModified();

	/**
	 * get serv
	 * 
	 * @return service str
	 */
	public abstract String getService();

	/**
	 * set data url
	 * 
	 * @param url url string example: http://new.sina.com/abcd.html <BR>
	 *            internal://SY_TEST.pk001 <BR>
	 *            ftp://172.16.0.4:21/abc/eft.doc <BR>
	 */
	public abstract void setUrl(String url);

	/**
	 * get data url
	 * 
	 * @return url string
	 */
	public abstract String getUrl();

	/**
	 * set file expresstion
	 * @param path - file expression format: protocol//path <BR>
	 *            example: internal://dff-2323-dfdf2-dfdfdf.doc <BR>
	 *            ${rh.file.server}/file.png http://localhost:8080/file.doc <BR>
	 *            ftp://localhost:21/ftpfile <BR>
	 */
	public abstract void setFilePath(String path);

	/**
	 * get file path
	 * @return text content
	 */
	public abstract String getFilePath();
	
	   /**
     * get index transformer
     * @return this index's transformer
     */
    public abstract IndexTransformer getTransformer();
    /**
     * set this index transformer This method will be a callback
     * @param tf transformer
     */
    public abstract void setTransformer(IndexTransformer tf);

}