package com.rh.core.comm.logs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 日志查看下载
 * @author wanglong
 *
 */
public class LogsMgr {

	/**
	 * 系统配置KEY，逗号分割的日志地址
	 */
	public static final String SY_LOGS_PATH = "SY_LOGS_PATH";
	/**
	 * 日志文件后缀
	 */
	public static final String LOG_FILE_SUFFIX = ".log";
	/**
	 * 文件名
	 */
	public static final String FILE_NAME = "FILE_NAME";
	/**
	 * 文件路径
	 */
	public static final String FILE_PATH = "FILE_PATH";
	/**
	 * 文件大小
	 */
	public static final String FILE_SIZE = "FILE_SIZE";
	/**
	 * 文件更新时间
	 */
	public static final String FILE_MTIME = "FILE_MTIME";
	
	/**
	 * 获取日志地址，解析成列表
	 * @return 返回结果
	 */
	public static String[] getLogsPath() {
		String logsPath = Context.getSyConf(SY_LOGS_PATH, Context.appStr(Context.APP.WEBINF));
		if (StringUtils.isEmpty(logsPath)) {
			return null;
		}
		return logsPath.split(Constant.SEPARATOR);
	}
	
	/**
	 * 获取指定地址下的所有文件名和路径
	 * @param path 文件夹路径
	 * @return 返回结果，文件名为KEY，文件路径为VALUE
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Bean> getLogs(String path) {
		List<Bean> fileList = new ArrayList<Bean>();
		File dir = new File(path);
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				return fileList;
			}
			File[] files = dir.listFiles();
			// 按修改时间倒序
			Arrays.sort(files, new Comparator() {
				@Override
				public int compare(Object arg0, Object arg1) {
					File file1 = (File) arg0;
					File file2 = (File) arg1;
					int ret = 0;
					if (file1.lastModified() > file2.lastModified()) {
						ret = -1;
					} else if (file1.lastModified() < file2.lastModified()) {
						ret = 1;
					}
					return ret;
				}
			});
			int len = files.length;
			for (int i = 0; i < len; i++) {
				File file = files[i];
				String fileName = file.getName();
				String filePath = "";
				if (!path.substring(path.length() - 1).equals(File.separator)) {
					filePath = path + File.separator + fileName;
				} else {
					filePath = path + fileName;
				}
				if (file.isDirectory()) { // 如果是目录则递归
					fileList.addAll(getLogs(filePath));
				} else { // 否则放到Map里
					Bean fileBean = new Bean();
					if (fileName.indexOf(LOG_FILE_SUFFIX) >= 0) { // 只要日志文件
						fileBean.set(FILE_NAME, fileName);
						fileBean.set(FILE_PATH, filePath);
						double length = file.length();
						String size = "";
						// 以下计算四舍五入
						if (length < 1024 / 10) { // 小于0.1KB，则显示为byte
							size = String.format("%.2f", length) + "byte";
						} else if (length < 1024 * 1024 / 10) { // 小于0.1MB，则显示为KB
							size = String.format("%.2f", length / 1024) + "KB";
						} else { // MB
							size = String.format("%.2f", length / 1024 / 1024) + "MB";
						}
						fileBean.set(FILE_SIZE, size);
						String fileMtime = DateUtils.getStringFromDate(new Date(file.lastModified()), 
								DateUtils.FORMAT_DATETIME);
						fileBean.set(FILE_MTIME, fileMtime);
						fileList.add(fileBean);
					}
				}
			}
		} 
		
		return fileList;
	}
	
	/**
	 * 获取所有的日志文件
	 * @return 返回
	 */
	public static List<Bean> getAllLogs() {
		List<Bean> fileList = new ArrayList<Bean>();
		String[] logsPath = LogsMgr.getLogsPath();
		if (logsPath != null) {
			int len = logsPath.length;
			for (int i = 0; i < len; i++) {
				fileList.addAll(LogsMgr.getLogs(logsPath[i]));
			}
		}
		return fileList;
	}
 	
}
