package com.rh.core.comm.file.upload;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Bean;
import com.rh.core.util.Constant;

/**
 * ueditor handler
 * @author wanglong
 * 
 */
public class UEditorUploadHandler extends  DefaultUploadHandler {
	

    @Override
	public void handleResponse(HttpServletRequest request, HttpServletResponse response, Bean result)
			throws IOException {
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		
		String fileUrl = "";
		List<Bean> resultList = result.getList(Constant.RTN_DATA);
		Bean ckBean =  resultList.get(0);
		fileUrl = "/file/" + ckBean.getId();
		
		String callback = request.getParameter("CKEditorFuncNum");
        out.println("<script type=\"text/javascript\">");
        out.println("window.parent.CKEDITOR.tools.callFunction(" + callback + ",'" + fileUrl + "',''" + ")");
        out.println("</script>");
		
		out.flush();
		out.close();
	}


}
