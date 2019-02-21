package com.rh.core.util.var;

import java.util.HashMap;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.ConfMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;


/**
 * 应用配置实现变量的接口类，变量格式为"@C_ + 应用配置键值@"
 * 
 * @author wanglong
 * @version $Id$
 */
public class ConfVar implements Var {
    /** 单例 */
    private static ConfVar inst = null;
    /**
     * 私有构建体，单例模式
     */
    private ConfVar() {
    }
    
    /**
     * 单例方法
     * @return 获取应用配置变量类
     */
    public static ConfVar getInst() {
        if (inst == null) {
            inst = new ConfVar();
        }
        return inst;
    }
    
    /**
     * 获取变量值
     * @param key 键值
     * @return 变量值
     */
    public String get(String key) {
        return Context.getSyConf(key.substring(3, key.length() - 1), key);
    }
    
    
    /**
     * 获取应用配置Map集合，私有配置和公用配置合并的集合,缺省只生成前端用到的应用配置map
     * @return 指定key的value
     */
    public Map<String, String> getMap() {
        return getMap(true);
    }
    
    /**
     * 获取应用级别配置Map集合，私有配置和公用配置合并的集合
     * @param onlyClient true：只生成给前端的应用配置， false：生成所有的应用配置
     * @return 指定key的value
     */
    public Map<String, String> getMap(boolean onlyClient) {
        Map<String, String> confMap = new HashMap<String, String>();
        Map<String, Bean> itemMap = DictMgr.getItemMap(DictMgr.getDict(ConfMgr.SY_COMM_CONFIG_PUBLIC)); //公有配置
        for (String key : itemMap.keySet()) {
            Bean item = itemMap.get(key);
            if (item != null && (item.getInt("FLAG") != Constant.NO_INT)
                    && !(onlyClient && item.getInt("CONF_FLAG") != Constant.YES_INT)) {
                confMap.put("@C_" + key + "@", item.getStr("NAME"));
            }
        }
        itemMap = DictMgr.getItemMap(DictMgr.getDict(ConfMgr.SY_COMM_CONFIG)); //私有配置
        for (String key : itemMap.keySet()) {
            Bean item = itemMap.get(key);
            if (item != null && (item.getInt("FLAG") != Constant.NO_INT)
                    && !(onlyClient && item.getInt("CONF_FLAG") != Constant.YES_INT)) {
                confMap.put("@C_" + key + "@", item.getStr("NAME"));
            }
        }
        return confMap;
    }
}
