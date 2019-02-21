package com.rh.sup.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 立项单下载，导出Word
 */
public class UrgeWord extends CommonServ {

	private static Configuration configuration = null;
	// 导出字段
	private static String[] strings = { "ITEM_NUM", "S_ATIMT", "DEPT_NAME", "SUPERV_ITEM", "STATIS_ITEM_SOURCE",
			"LIMIT_DATE", "OVERDUE_DAY", "USER_NAME", "USER_TEL" };

	/**
	 * 导出方法
	 * 
	 * @param list
	 * @param ITEM_NUM
	 */
	public static void createWord(Bean bean, String ITEM_NUM, String type) {
		configuration = new Configuration();
		configuration.setDefaultEncoding("UTF-8");
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// 获取导出字段
		getData(dataMap, bean);
		// 获取导出流
		HttpServletResponse response = Context.getResponse();
		HttpServletRequest request = Context.getRequest();
		response.resetBuffer();
		response.setContentType("application/x-msdownload");
		// 在流中创建Word
		com.rh.core.util.RequestUtils.setDownFileName(request, response, ITEM_NUM + ".doc");
		Template t = null;
		Writer out = null;
		try {
			// 引入模板
			configuration.setDirectoryForTemplateLoading(new File(request.getRealPath("/sup/imp_template")));
			if ("1".equals(type)) {
				t = configuration.getTemplate("督查通知单下载.ftl");
			} else if ("2".equals(type)) {
				t = configuration.getTemplate("督查催办单下载.ftl");
			}

			out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
			// 将数据传递给低层导出并生成Word
			t.process(dataMap, out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (response != null) {
					response.flushBuffer();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 处理导出字段
	 * 
	 * @param dataMap
	 * @param list
	 */
	private static void getData(Map<String, Object> dataMap, Bean bean) {
		for (int i = 0; i < strings.length; i++) {
			dataMap.put(strings[i], bean.getStr(strings[i]));
		}
	}
}
