package com.rh.core.wfe.def;

import com.rh.core.util.encoder.Hex;

/**
 * 工作流解析 基础类
 *
 */
public class WFParserBase {
    
    /**
     * 公司ID
     */
    private String cmpyID = "";
    
    /**
     * 流程编码
     */
    private String wfProcCode = "";
    
    /**
     * 
     * @param cmpyId 公司ID
     * @param procCode 流程编码
     */
    protected WFParserBase(String cmpyId , String procCode) {
        this.cmpyID = cmpyId;
        this.wfProcCode = procCode;
    }
    
    /**
     * 得到公司ID
     * @return 公司ID
     */
    public String getCmpyID() {
        return cmpyID;
    }
    
    /**
     * 
     * @param cmpyId 公司ID
     */
    public void setCmpyID(String cmpyId) {
        this.cmpyID = cmpyId;
    }
    
    /**
     * 
     * @return 流程编码
     */
    public String getWfProcCode() {
        return wfProcCode;
    }
    
    /**
     * 
     * @param procCode 流程编码
     */
    public void setWfProcCode(String procCode) {
        this.wfProcCode = procCode;
    }
    
    /**
     * @param hexStr 16进制编码的字符串
     * @return 解编码之后字符串
     */
    protected String decodeHex(String hexStr) {
        try {
            byte[] bytes = Hex.decode(hexStr);
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
