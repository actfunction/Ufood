package com.rh.core.util;

import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;

/**
 * 在代码中执行Javascript脚本的辅助类
 * 
 * @author wanglong
 */
public class JavaScriptEngine {
    /** log */
    protected Log log = LogFactory.getLog(JavaScriptEngine.class);
    
    /**
     * 执行Javascript脚本的引擎
     */
    private ScriptEngine engine = null;
    
    /**
     * 初始化引擎对象
     * 
     * @param baseBean 直接注入引擎的数据Bean。这个Bean中的元素，在脚本中可直接使用Key来访问。
     */
    public JavaScriptEngine(Bean baseBean) {
        initScriptEngine(baseBean);
    }
    
    /**
     * 初始化执行Javascript脚本的引擎
     * 
     * @param baseBean 直接注入引擎的数据Bean。这个Bean中的元素，在脚本中可直接使用Key来访问。
     */
    private void initScriptEngine(Bean baseBean) {
        if (engine != null) {
            return;
        }
        ScriptEngineManager sem = new ScriptEngineManager();
        this.engine = sem.getEngineByName("rhino");
        
        Set<Object> keys = baseBean.keySet();
        for (Object key : keys) {
            if (key instanceof String) {
                String strKey = (String) key;
                engine.put(strKey, baseBean.get(key));
            }
        }
    }
    
    /**
     * 在引擎中加入变量及其值，执行脚本时，可以使用这些变量。
     * 
     * @param varName 变量名称
     * @param varObj 变量对象
     */
    public void addVar(String varName , Object varObj) {
        this.engine.put(varName, varObj);
    }
    
    /**
     * @param script 脚本代码
     * @return 脚本执行结果
     */
    public String runScript(String script) {
        String out = "";
        try {
            Object result = engine.eval(script);
            if (result != null) {
                out = String.valueOf(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return out;
    }
    
    /**
     * 执行js格式的条件表达式，返回表达式执行的结果
     * 
     * @param jsCondition js格式的条件表达式
     * @return 表达式条件执行结果，报错或者不满足条件返回false，满足条件返回true
     */
    public boolean isTrueScript(String jsCondition) {
        try {
            Object jsResult = engine.eval(jsCondition);
            return jsResult.toString().equalsIgnoreCase("true");
        } catch (Exception e) {
            log.error(e.getMessage() + ": " + jsCondition, e);
            return false;
        }
    }
}
