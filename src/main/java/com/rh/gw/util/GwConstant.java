package com.rh.gw.util;

/**
 * 公文常量类,所有的常量都定义在这里 然后去调用
 * 
 * @author WeiTl
 * @version 1.0
 */
public class GwConstant {
	/** 盖章非加密正文编码 */
	public static final String NO_ENC_ZHENGWEN = "NOENCZHENGWEN";

	/** 盖章加密正文编码 */
	public static final String ENC_ZHENGWEN = "ENCZHENGWEN";
	/** 附件 **/
	public static final String FUJIAN = "FUJIAN";
	/** 正文 **/
	public static final String ZHENGWEN = "ZHENGWEN";

	/** 字段名：文件名 **/
	public static final String FILE_NAME = "FILE_NAME";
	/** 字段名：正文小类型 **/
	public static final String ITEM_CODE = "ITEM_CODE";
	/** 字段名：文件大类型，如正文、附件、转发原文等 **/
	public static final String FILE_CAT = "FILE_CAT";
	/** 字段名： 显示名称 **/
	public static final String DIS_NAME = "DIS_NAME";
	/** 字段名：文件排序号 **/
	public static final String FILE_SORT = "FILE_SORT";

	/** 公文模板服务 **/
	public static final String OA_COMMON_TMPL = "OA_COMMON_TMPL";

	/** 待办服务 **/
	public static final String SY_TODO = "SY_COMM_TODO";

	/** 成文模板服务 **/
	public static final String OA_COMMON_CW_TMPL = "OA_COMMON_CW_TMPL";

	/** 是否存在盖章文件 **/
	public static final String EXIST_SEAL_PDF_FILE = "EXIST_SEAL_PDF_FILE";
}