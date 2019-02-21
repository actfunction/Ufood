package com.rh.core.comm.file.upload;

import javax.servlet.http.HttpServletRequest;

import com.rh.core.serv.ParamBean;

/**
 * 红头文件响应类
 * @author wanglong
 * 
 */
public class RedheadUploadHandler extends DefaultUploadHandler {
    
	/** 正文名称 */
    private static final String FILE_CAT_ZHENGWEN = "正文";
	@Override
	public void handleRequest(HttpServletRequest request, ParamBean paramBean)  {
	    paramBean.set("DIS_NAME", FILE_CAT_ZHENGWEN);
	}


}
