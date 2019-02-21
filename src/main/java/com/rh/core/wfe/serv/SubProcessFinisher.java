/**
 * 
 */
package com.rh.core.wfe.serv;

import com.rh.core.wfe.WfProcess;

/**
 * 子流程办结、取消办结后的处理，可以用于处理主流程中的数据。
 * @author wanglong
 *
 */
public interface SubProcessFinisher {
    
    
    /**
     * 子流程办结后的处理 
     * @param wfProcess 子流程实例
     */
    void afterFinish(WfProcess wfProcess);
    
    
    
    /**
     * 子流程取消办结后的处理 
     * @param wfProcess 子流程实例
     */
    void afterUndoFinish(WfProcess wfProcess);

}
