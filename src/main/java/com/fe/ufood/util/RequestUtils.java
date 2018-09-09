/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.fe.ufood.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


/**
 * <p>
 * 处理对于Request的一些得到客户提交参数和设置数据的方法
 * </p>
 * 
 * @author 
 * @version $Id$
 */
public class RequestUtils {

	/**
	 * 
	 * @param request 客户端请求
	 * @return  参数Bean
	 */
    public static Bean transParam(HttpServletRequest request) {
        Bean param;
        String data = request.getParameter("data");
        if (data != null) {
            param = new Bean(JsonUtils.toBean(data));
        } else {
            param = new Bean();
        }
        //获取其他参数信息
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String pName = paramNames.nextElement();
            if (!pName.equals("data")) { // 不再处理data中的数据
                String value = getStr(request, pName);
                if (pName.equals(Constant.KEY_ID)) { // 预处理直接传主键的机制
                    param.setId(value);
                } else {
                    param.set(pName, value);
                }
            }
        }
        param.remove(Constant.PARAM_JSON_RANDOM); //去掉客户端的 随机参数	    
	    return param;
	}


	/**
	 * 获取request参数
	 * @param request http request
	 * @param param 参数
	 * @return 参数值
	 */
	public static String getStr(HttpServletRequest request, String param) {
		String value = request.getParameter(param);
		if (value == null) {
			value = "";
		}
		return value;
	}
}