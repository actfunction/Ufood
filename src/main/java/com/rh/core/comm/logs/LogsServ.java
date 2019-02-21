package com.rh.core.comm.logs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.RequestUtils;

/**
 * 日志查看下载服务类
 * @author wanglong
 *
 */
public class LogsServ extends CommonServ {
	
	/**
	 * 重载父类的query方法，这里的数据并非来自数据库
	 * @param paramBean 参数
	 * @return 返回
	 */
	@Override
	public OutBean query(ParamBean paramBean) {
		String servId = paramBean.getServId();
        ServDefBean servDef = ServUtils.getServDef(servId);
		OutBean outBean = new OutBean();
		List<Bean> list = LogsMgr.getAllLogs();
		outBean.setPage(list.size()).setData(list).setCols(servDef.getAllItems());
		return outBean;
	}
	
	/**
	 * 日志下载
	 * @param paramBean 参数
	 * @return 返回下载流
	 */
	public OutBean download(ParamBean paramBean) {
		String filePath = paramBean.getStr(LogsMgr.FILE_PATH);
		String fileName = paramBean.getStr(LogsMgr.FILE_NAME);
		HttpServletRequest request = Context.getRequest();
		HttpServletResponse response = Context.getResponse();
		response.reset();
		response.setContentType("application/x-msdownload");
		RequestUtils.setDownFileName(request, response, fileName);
		InputStream in = null;
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			in = new FileInputStream(filePath);
			IOUtils.copy(in, out);
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException("下载日志文件异常----" + e.getStackTrace());
		} finally {
			try {
				response.flushBuffer();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
			IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
		}
		return new OutBean();
	}
	
}
