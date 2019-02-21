package com.rh.api.util;

/**
 * 常量类
 * 
 * @author wanglong
 *
 */
public class ApiConstant {

    public static enum RTN_CODE_ENUM {
	CODE_001("成功", "001"), CODE_002("失败", "002"), CODE_003("接口不存在", "003"), CODE_004("数据格式错误", "004");
	// 成员变量
	private String value;
	private String code;

	// 构造方法
	private RTN_CODE_ENUM(String name, String code) {
	    this.value = name;
	    this.code = code;
	}

	// 普通方法
	public static String getValue(String code) {
	    for (RTN_CODE_ENUM c : RTN_CODE_ENUM.values()) {
		if (c.getCode() == code) {
		    return c.value;
		}
	    }
	    return null;
	}

	// get set 方法
	public String getValue() {
	    return value;
	}

	public void setValue(String value) {
	    this.value = value;
	}

	public String getCode() {
	    return code;
	}

	public void setCode(String code) {
	    this.code = code;
	}
    }
}
