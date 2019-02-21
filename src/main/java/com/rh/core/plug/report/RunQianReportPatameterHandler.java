/**
 * 
 */
package com.rh.core.plug.report;

import java.util.HashMap;
import java.util.Map;

/**
 * 润乾报表参数处理器
 * 
 * @author wanglong
 *
 */
public class RunQianReportPatameterHandler implements IReportPatameterHandler {

    /* (non-Javadoc)
     * @see com.rh.core.plug.report.IReportPatameterHandler#handle(com.rh.core.plug.report.ReportConfig)
     */
    @Override
    public Map<String, String> handle(ReportConfig config) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(ReportConfig.REPORT_NAME, config.getReportName());
        
        String reportFile = config.getReportFileName();
        params.put(ReportConfig.REPORT_FILE_NAME, reportFile.endsWith(".raq") ? reportFile : reportFile + ".raq");
        params.put(
                ReportConfig.REPORT_PARAMS,
                ReportConfig.REPORT_SQL_FILTER + "=" + config.getSqlFilter() + ";"
                        + config.getParams().replaceAll("&", ";"));
        params.put(ReportConfig.EXCEL_ABLE, config.isExcelAble() ? "yes" : "no");
        params.put(ReportConfig.PDF_ABLE, config.isPdfAble() ? "yes" : "no");
        return params;
    }
}
