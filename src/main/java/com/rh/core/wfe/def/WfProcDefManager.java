package com.rh.core.wfe.def;

import org.apache.log4j.Logger;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.comm.CacheMgr;
import com.rh.core.wfe.db.WfProcDefDao;

/**
 * 流程定义管理， 将流程定义存放在cache中
 *
 */
public class WfProcDefManager {
    
	/**
	 * 缓存类型
	 */
    private static final String CACHE_TYPE = "WORKFOLOW_DEF";
    
    private static Logger log = Logger.getLogger(WfProcDefManager.class);
    
    /**
     * @param wfDefCode 流程编码
     * @return 流程定义
     */
    public static WfProcDef getWorkflowDef(String wfDefCode) {
        WfProcDef def = getObjFromCache(wfDefCode);
        
        if (def == null) {
            try {
                // 新建流程定义对象，并保持到缓存
                def = new WfProcDef(wfDefCode);
                if (def != null) {
                    putObjToCache(def);
                }
            } catch (Exception e) {
                log.error(
                        "取得工作流定义失败，wfDefCode=" + wfDefCode + ":"
                                + e.getMessage(), e);
            }
        }
        
        return def;
    }
    
    /**
     * 通过流程定义名称和公司ID取得流程定义对象。先取本单位流程，本单位流程不存在，则取公共流程。
     * 
     * @param cmpyID 指定公司ID
     * @param procEnName 流程定义名称。
     * @return 流程定义
     */
    public static WfProcDef getWorkflowDef(String cmpyID , String procEnName) {
        // 构造本公司的对应流程的wfDefCode
        final String selfWfDefCode = WfProcDef.genrateWfDefCode(cmpyID,
                procEnName);
        WfProcDef def = getWorkflowDef(selfWfDefCode);
        if (def == null) {
            // 流程定义中没有取到本公司的流程，则取公共流程
            Bean pubProcDefBean = WfProcDefDao.getWfPubProcDefBean(procEnName);
            
            // 得到公共流程编码
            String pubWfProcCode = WfProcDef.genrateWfDefCode(
                    pubProcDefBean.getStr("S_CMPY"), procEnName);
            
            // 取得公共流程
            def = getWorkflowDef(pubWfProcCode);
        }
        
        if (def == null) {
            throw new TipException("找不到指定流程，procEnName=" + procEnName
                    + ";cmpyID=" + cmpyID);
        }
        
        return def;
    }
    
    /**
     * @param wfDefCode 流程编码
     * @return 流程定义
     */
    private static WfProcDef getObjFromCache(String wfDefCode) {
        return (WfProcDef) CacheMgr.getInstance().get(wfDefCode, CACHE_TYPE);
    }
    
    /**
     *  将流程定义 放到缓存
     * @param procDef 流程定义
     */
    private static void putObjToCache(WfProcDef procDef) {
        CacheMgr.getInstance().set(procDef.getProcCode(), procDef, CACHE_TYPE);
    }
    
    /**
     * 从新将指定对象装载到内存中
     * 
     * @param wfDefCode 流程编码
     */
    public static void removeFromCache(String wfDefCode) {
        try {
            CacheMgr.getInstance().remove(wfDefCode, CACHE_TYPE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
