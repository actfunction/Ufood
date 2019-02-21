/**
 * 
 */
package com.rh.core.plug.report;

import java.io.Serializable;

/**
 * 报表配置
 * 
 * @author wanglong
 *
 */
public class ReportConfig implements Serializable {
	private static final long serialVersionUID = 2907020530150769004L;
	
	/**
	 * 报表名称
	 */
	public static final String REPORT_NAME = "REPORT_NAME";
	/**
	 * 报表文件名称
	 */
	public static final String REPORT_FILE_NAME = "REPORT_FILE_NAME";
	
	/**
     * 允许保存为pdf
     */
	public static final String PDF_ABLE = "PDF_ABLE";
    
    /**
     * 允许保存为excel
     */
	public static final String EXCEL_ABLE = "EXCEL_ABLE";
	
	/**
	 * 报表参数
	 */
	public static final String REPORT_PARAMS = "REPORT_PARAMS";
	
	/**
	 * 报表sql过滤，实现数据权限
	 */
	public static final String REPORT_SQL_FILTER = "SQL_FILTER";

	/**
	 * 报表名称
	 */
	private String reportName = null;
	
	/**
	 * 报表文件名称
	 */
	private String reportFileName = null;
	
	/**
	 * sql数据过滤语句
	 */
	private String sqlFilter = null;
	
	/**
	 * 是否允许保存为pdf
	 */
	private boolean pdfAble = false;
	
	/**
	 * 是否允许保存为excel
	 */
	private boolean excelAble = false;
	
	/**
	 * 报表参数,格式为：key1=value1&key2=value2...
	 */
	private String params = null;

	/**
	 * @return 报表名称
	 */
	public String getReportName() {
		return reportName;
	}

	/**
	 * 
	 * 
	 * @param reportName 报表名称
	 */
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	/**
	 * @return 报表文件名称
	 */
	public String getReportFileName() {
		return reportFileName;
	}

	/**
	 * @param reportFileName 报表文件名称
	 */
	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}

	/**
	 * @return 参数
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @param params 参数
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @return 是否允许导出为pdf
	 */
	public boolean isPdfAble() {
		return pdfAble;
	}

	/**
	 * @param pdfAble  是否允许导出为pdf
	 */
	public void setPdfAble(boolean pdfAble) {
		this.pdfAble = pdfAble;
	}

	/**
	 * @return 是否允许导出为excel
	 */
	public boolean isExcelAble() {
		return excelAble;
	}

	/**
	 * @param excelAble 是否允许导出为excel
	 */
	public void setExcelAble(boolean excelAble) {
		this.excelAble = excelAble;
	}

	/**
	 * @return sql过滤语句
	 */
	public String getSqlFilter() {
		return sqlFilter;
	}

	/**
	 * @param sqlFilter sql过滤语句
	 */
	public void setSqlFilter(String sqlFilter) {
		this.sqlFilter = sqlFilter;
	}
}
