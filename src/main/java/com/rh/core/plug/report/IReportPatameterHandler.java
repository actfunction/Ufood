/**
 * 
 */
package com.rh.core.plug.report;

import java.util.Map;


/**
 * 报表参数处理器接口
 * 
 * @author wanglong
 *
 */
public interface IReportPatameterHandler {
    /**
     * 将报表配置参数处理成报表可直接接受的参数键值对
     * 
     * @param config    报表配置
     * @return  参数键值对
     */
    Map<String, String> handle(ReportConfig config);
}
