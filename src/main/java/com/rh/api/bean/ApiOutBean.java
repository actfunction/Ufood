package com.rh.api.bean;

import com.rh.api.util.ApiConstant;
import com.rh.core.base.Bean;
import com.rh.core.util.JsonUtils;

public class ApiOutBean extends Bean {

    /**
     * UID
     */
    private static final long serialVersionUID = 2148779393574713672L;

    /**
     * CODE:返回编码
     */
    public static final String CODE = "code";
    /**
     * MESSAGE:返回信息
     */
    public static final String MESSAGE = "message";
    /**
     * DATA:返回数据
     */
    public static final String DATA = "data";

    /**
     * ERR_INFO:具体错误信息
     */
    public static final String ERR_INFO = "info";

    public String output() {
        StringBuilder outStr = new StringBuilder();
        outStr.append("{\"").append(CODE).append("\" : \"").append(this.getCode()).append("\", \"").append(MESSAGE)
                .append("\" : \"").append(this.getMessage()).append("\", \"").append(DATA).append("\" : ")
                .append(JsonUtils.toJson(this.getData())).append("}");
        return outStr.toString();
    }

    public void setCode(String code) {
        this.set(CODE, code);
    }

    public String getCode() {
        return this.get(CODE, ApiConstant.RTN_CODE_ENUM.CODE_001.getCode());
    }

    public void setMessage(String message) {
        this.set(MESSAGE, message);
    }

    public String getMessage() {
        return this.get(MESSAGE, ApiConstant.RTN_CODE_ENUM.CODE_001.getValue());
    }

    public void setErrorInfo(String errorInfo) {
        this.set(ERR_INFO, errorInfo);
    }

    public String getErrorInfo() {
        return this.get(ERR_INFO, ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
    }

    public void setData(Bean data) {
        this.set(DATA, data);
    }

    public Bean getData() {
        return this.getBean(DATA);
    }

}
