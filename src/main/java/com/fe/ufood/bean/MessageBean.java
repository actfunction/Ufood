package com.fe.ufood.bean;
import java.io.Serializable;
import java.util.HashMap; 
/** 
* 
* 更加服务平台快发的网络返回基类 
* @author anran 
* @version 0.0.1 响应参数 
*/ 
public class MessageBean extends HashMap<Object, Object> implements Serializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6559317378968051695L;
	/** 
	* 数据载体 
	*/ 
	private Object data; 
	/** 
	* 状态码 
	*/ 
	private String code; 
	/** 
	* 状态信息 
	*/ 
	private String message; 
	public Object getData() { 
		return data; 
	} 
	/** 
	* 设置成功数据，并且填充状态码，和状态信息 
	* @param data 
	*/ 
	public void setData(Object data) { 
		setCode("800"); 
		setMessage("success"); 
		this.data = data; 
	} 
	/** 
	* 设置错误信息，并设置错误信息 
	* @param info 
	*/ 
	public MessageBean setError(String info){ 
		setCode("500"); 
		setMessage(info); 
		return this; 
	} 
    
    /**
     * 是否存在指定值
     * @param key 键值
     * @return 存在返回true，不存在返回false
     */
    public final boolean contains(Object key) {
        return containsKey(key);
    }
	/** 
	* 将OutBean对象转换成JSON字符串 
	* @return 
	*/ 
	public String toJsonStr() { 
		return ""; 
		//return JSONObject.toJSON(this).toString(); 
	} 
	public String getCode() { 
		return code; 
	} 
	public void setCode(String code) { 
		this.code = code; 
	} 
	public String getMessage() { 
		return message; 
	} 
	public void setMessage(String message) { 
		this.message = message; 
	} 
}