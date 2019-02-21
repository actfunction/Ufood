package com.rh.oss;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class OssServlet extends HttpServlet{
	@Override
	protected void  doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {			
		try {
			String  fileId=req.getParameter("fileId");
			String ossTest = SendFileToOss.OssTest(fileId);				
			res.setCharacterEncoding("UTF-8");
			res.setContentType("text/html;charset=utf-8");		
			res.getWriter().write(ossTest);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, res);
	}
	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(arg0, arg1);
	}
	
}
