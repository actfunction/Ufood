package com.rh.core.comm.msg;

import com.rh.core.base.Bean;

/**
 * 消息提醒结果
 * @author wanglong
 * 
 */
public class MsgResultBean extends Bean {
    /**
     * 
     */
    private static final long serialVersionUID = 5182148512410173748L;

    /**
     * 是否成功
     */
    private static final String IS_OK = "_OK";

    /** 执行结果 **/
    private static final String RESULT = "_RESULT";

    /**
     * 
     * @return 是否成功
     */
    public boolean isOk() {
        return this.getBoolean(IS_OK);
    }
    
    /**
     * 
     * @return 执行结果
     */
    public String getExecLog() {
        return this.getStr(RESULT);
    }
    
    /**
     * 
     * @param isOk 是否成功
     */
    public void setOk(boolean isOk) {
        this.set(IS_OK, isOk);
    }
    
    /**
     * 
     * @param execLog 执行结果
     */
    public void setExecLog(String execLog) {
        this.set(RESULT, execLog);
    }
}
