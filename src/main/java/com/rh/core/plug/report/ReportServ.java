/**
 * 
 */
package com.rh.core.plug.report;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;

/**
 * 报表服务
 * 
 * @author wanglong
 * 
 */
public class ReportServ extends CommonServ {

    /**
     * 报表定义服务名称
     */
    private static final String REPORT_DEFINE_SERVICE_CODE = "SY_PLUG_REPORT";

    /**
     * 跳转到报表查询展示页面
     * 
     * @param paramBean 参数
     * @return  返回
     */
    public OutBean show(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        outBean.setToDispatcher("/sy/plug/report/stdReportView.jsp?code=" + paramBean.getServId());
        return outBean;
    }
    
    /**
     * 获取报表参数
     * 
     * @param paramBean 参数bean
     * @return  报表参数
     */
    public OutBean getReportParameters(ParamBean paramBean) {
        ParamBean param = new ParamBean(REPORT_DEFINE_SERVICE_CODE, ServMgr.ACT_BYID, paramBean.getServId());
        ServDefBean reportDefinition = new ServDefBean(ServMgr.act(param));
        //构建报表配置
        ReportConfig config = new ReportConfig();
        config.setReportName(reportDefinition.getName());
        
        //设置报表文件名称，当文件名为空时，设其值为报表编码
        String reportFile = reportDefinition.getTableView();
        if (reportFile.length() == 0) {
            reportFile = reportDefinition.getId();
        }
        config.setReportFileName(reportFile);
        
        config.setParams(paramBean.getStr(ReportConfig.REPORT_PARAMS));
        config.setSqlFilter(reportDefinition.getServExpressionWhere());
        config.setPdfAble(reportDefinition.getStr("SERV_DICT_CACHE").equals("2") ? false : true);
        config.setExcelAble(reportDefinition.getStr("SERV_SQL_ORDER").equals("2") ? false : true);

        //调用报表参数处理
        String reportType = reportDefinition.getStr("TABLE_ACTION");
        IReportPatameterHandler handler = null;
        OutBean outBean = new OutBean();
        if (reportType.equals("SY_PLUG_REPORT_RQ")) {
            handler = new RunQianReportPatameterHandler();
        } else if (reportType.equals("SY_PLUG_REPORT_FR")) {
            handler = new FineReportParameterHandler();
        } else if (reportType.equals("SY_PLUG_REPORT_CHART")) {
            handler = new DefaultParameterHandler();
        }
        outBean.setData(handler.handle(config)).setOk();
        return outBean;
    }
}
