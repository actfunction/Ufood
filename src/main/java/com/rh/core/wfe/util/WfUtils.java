package com.rh.core.wfe.util;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;

/**
 * 工作流常用类
 *
 */
public class WfUtils {
	/**
	 * 得到工作流定义的保存路径 
	 * '/home/zotn-oa/webapps/product/public-html/WEB-INF/doc/company/compyID/workflow/defxml/'
	 * 
	 * @param cmpyID
	 *            公司ID
	 * 
	 * @return the path of gongwen for http useage
	 */
	public static String getCmpyWfDefXMLPath(String cmpyID) {
		StringBuffer path = new StringBuffer(Context.appStr(APP.WEBINF_DOC_CMPY));
		path.append(cmpyID);
		path.append(Constant.PATH_SEPARATOR);
		path.append("workflow");
		path.append(Constant.PATH_SEPARATOR);
		path.append("defxml");

		return path.toString();
	}

	/**
	 * 得到工作流 定义的 文件  完整路径
	 * @param cmpyId 公司ID
	 * @param enName 英文名称
	 * @return 工作流文件的保存路径
	 */
	public static String getSavedDefFileName(String cmpyId , String enName) {
		return getCmpyWfDefXMLPath(cmpyId)
				+ Constant.PATH_SEPARATOR
				+ enName + ".xml";
	}
	
    /**
     * 执行返回结果为bool值的js脚本
     * @param script 脚本
     * @param bean 包含脚本中需使用变量数据的bean
     * @return 执行结果
     */
    public static boolean execCondScript(String script , Bean bean) {
        if (StringUtils.isEmpty(script)) {
            return true;
        }
        script = ServUtils.replaceSysAndData(script, bean);
        
        return Lang.isTrueScript(script);
    }	
}
