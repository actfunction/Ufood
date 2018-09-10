package com.fe.ufood.contraller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fe.ufood.bean.MessageBean;
import com.fe.ufood.mgr.AgentHelper;
import com.fe.ufood.util.Bean;
import com.fe.ufood.util.Constant;
import com.fe.ufood.util.JsonUtils;
import com.fe.ufood.util.RequestUtils;

@RestController
@RequestMapping(value="httpAgent", method={RequestMethod.GET, RequestMethod.POST})
public class HttpAgent {


	/**
	 * 加密需要钥匙
	 */
//	private final String KEY_ID = "keyId";
	
	@ResponseBody
	@RequestMapping(value ="/agent.do")
    public void agent(HttpServletRequest request, HttpServletResponse response) {
//    	Map<String,Object> requestParamter = null;

        String relativePath = null;
        try {
			request.setCharacterEncoding("UTF-8");
			relativePath = AgentHelper.getResource(request);
		} catch (UnsupportedEncodingException e2) {
			//此处是否需要打印日志
			e2.printStackTrace();
		}
        response.setContentType("text/html; charset=UTF-8");
		// 返回消息
        MessageBean result = new MessageBean();
		Bean param = new Bean();
        Bean requestParamter = AgentHelper.parseUri(relativePath);
		// 获取服务名称
		String serviceName = AgentHelper.serviceName(requestParamter);
		// 服务行为
		String action = AgentHelper.getAction(requestParamter);
		// 服务请求参数
		param = RequestUtils.transParam(request);
//		Map<String,Object> serviceParameter = (Map<String, Object>) requestParamter.get(SERVICE_PARAMETER);
		try {
			StringBuffer buffer = new StringBuffer("agent is error,exception is").append("serviceName is ")
					.append(serviceName).append("action is ").append(action).append("serviceParameter is");
			result.setMessage(buffer.toString());
			// 调用路由
//			result = ClientTools.callService(serviceName, action, serviceParameter);
		} catch (Exception e) {
			StringBuffer buffer = new StringBuffer("agent is error,exception is").append("serviceName is ")
					.append(serviceName).append("action is ").append(action).append("serviceParameter is");
			result.setError(e.getMessage());
			result.setMessage(buffer.toString());
			//此处是否需要打印日志
			e.printStackTrace();
		}

		
		// =================处理返回信息===================
        if (!response.isCommitted()) {
            // 返回信息
            String header;
            if (result.contains(Constant.TO_HTML)) {
                header = "text/html; charset=utf-8";
            } else  if (result.contains(Constant.TO_XML)) {
                header = "text/xml; charset=utf-8";
            } else if (requestParamter.getStr(Constant.PARAM_FORMAT).equals("xml")) { //指定xml格式
                header = "text/xml; charset=utf-8";
            } else if (!RequestUtils.getStr(request, "callback").isEmpty()) {
                header = "text/xml; charset=utf-8";
            } else {
                header = "text/html; charset=utf-8";
            }
            response.setContentType(header);
            PrintWriter out = null;
            String content = JsonUtils.toJson(param, true); //支持压缩空值输出
			try {
				out = response.getWriter();
	            out.write(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
            out.flush();
            out.close();
        }
    }
}
