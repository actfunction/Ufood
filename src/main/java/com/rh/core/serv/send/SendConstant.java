package com.rh.core.serv.send;

/**
 * 分发模块相关的常量
 * @author wanglong
 *
 */
public class SendConstant {
    /** 未分发、等待分发 **/
    public static final int SEND_STATUS_WAITTING = 1;
    /** 分发中  **/
    public static final int SEND_STATUS_SNEDING = 2; 
    /** 已收回 */
    public static final int SEND_STATUS_REVOKED = 3;
    /** 已签收 **/
    public static final int SEND_STATUS_RECEIVED = 4;
    /** 已退回 **/
    public static final int SEND_STATUS_RETURNED = 5;  
    
    /**
     * 该分发被 退回了 1
     */
    public static final int SEND_RTN_FLAG = 1;
    
    /**接收人为外机构**/
    public static final String OUTSIDE = "outside";
    
    /**接收人为本机构**/
    public static final String INSIDE = "inside";    
    
    /**分发方案前缀**/
    public static final String PREFIX_SCHM = "SCHM-";
    
    /** 分发部门前缀 **/
    public static final String PREFIX_DEPT = "DEPT-";
    
}
