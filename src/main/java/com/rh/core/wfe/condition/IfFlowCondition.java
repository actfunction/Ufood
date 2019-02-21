package com.rh.core.wfe.condition;

import java.util.HashMap;


/**
 * 条件流
 *
 */
public interface IfFlowCondition {
	
	/**
	 * 判断条件流中的条件语句。
	 * @param strCondition 条件语句
	 * @return rtn
	 */
    boolean check(String strCondition);
    
    /**
     * 
     * @param aVarMap 参数
     */
    void setVarMap(HashMap<String, Object> aVarMap);
}
