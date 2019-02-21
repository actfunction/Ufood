package com.rh.core.wfe.condition;

import java.util.HashMap;
import java.util.Set;

import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.org.UserBean;
import com.rh.core.util.JavaScriptEngine;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.util.WfContextHelper;

/**
 * @author wanglong 处理简单条件流的判断。
 */
public class SimpleFlowCondition implements IfFlowCondition {
	private static Log log = LogFactory.getLog(SimpleFlowCondition.class);
	
	private StringBuffer buffCondition = new StringBuffer(1024);

	private WfAct wfAct = null;

	private UserBean user = null;

	/**
	 * 
	 * @param aWfAct 节点实例 
	 * @param currentUser 当前用户
	 */
	public SimpleFlowCondition(WfAct aWfAct, UserBean currentUser) {
		wfAct = aWfAct;
		user = currentUser;
	}

	private HashMap<String, Object> varMap =  null;
	
	/**
	 * 
	 * @param aVarMap 变量Map
	 */
	public void setVarMap(HashMap<String, Object> aVarMap) {
		this.varMap = aVarMap;
	}

	/**
	 * @param strCondition 条件
	 * @return 执行是否成功
	 */
	public boolean check(String strCondition) {
		try {
			this.buffCondition.append(strCondition);
			return eval();
		} catch (ScriptException e) {
			log.error("脚本语言执行错误:", e);
            throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @return 执行是否成功
	 * @throws ScriptException 
	 * @throws ScriptException 执行错误
	 */
	private boolean eval() throws ScriptException {
	    JavaScriptEngine engine = getScriptEngine(wfAct, user);
	    initVariable(engine);
	    
		String conditionAll = this.buffCondition.toString();
		
		if (conditionAll.length() > 0) {
			return engine.isTrueScript(conditionAll);
		}
		
		

		return false;
	}
	
	
	/**
     * 初始化脚本中使用的变量
     * @param engine 脚本引擎 
     */
    private void initVariable(JavaScriptEngine engine) {
        if (varMap != null) {
            Set<String> keySet = varMap.keySet();
            for (String key:keySet) {
                engine.addVar(key, varMap.get(key));
            } 
        }
    }
	
	/**
	 * 获取JavaScript的脚本引擎
	 * @param aWfAct 节点实例 
	 * @param currentUser 当前用户
	 * @return ScriptEngine
	 */
	private JavaScriptEngine getScriptEngine(WfAct aWfAct, UserBean currentUser) {
	    JavaScriptEngine jsEngine = new JavaScriptEngine(aWfAct.getProcess().getServInstBean());
	    
	    jsEngine.addVar("wfAct", aWfAct);
        jsEngine.addVar("wfContext", new WfContextHelper(aWfAct, currentUser));
        jsEngine.addVar("mindContext", new MindContextHelper(aWfAct, currentUser));
        
        return jsEngine;
	}
}
