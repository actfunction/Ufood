
package com.rh.core.wfe.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

/**
 * 保存指定内容到文件中，如果文件文件存在，且backupBasePath属性的内容不为null，则先备份原文件到指定目录，再覆盖文件。
 * @author wanglong
 *
 */
public class FileWriterHelper {
	private String backupBasePath = "";
	
	/**
	 * 备份文件的路径
	 * @param filePath 备份文件的路径
	 */
	public void setBackupBasePath(String filePath) {
		backupBasePath = filePath;
	}
	
	/**
	 * 写字符串到指定文件中。
	 * @param file 指定文件
	 * @param fileContent 文件内容
	 * @throws IOException IO异常
	 */
	public void write(File file, String fileContent) throws IOException {
		if (file.exists()) {
			backupFile(file);
		} else {
			mkdir(file);
		}
		FileUtils.writeStringToFile(file, fileContent);
	}

	/**
	 * 备份文件
	 * @param srcFile 原文件
 	 * @throws IOException IO异常
	 */
	private void backupFile(File srcFile) throws IOException {
		if (backupBasePath != null && backupBasePath.length() > 0) {
			File backupFile = new File(getBackupFileName(srcFile));
			mkdir(backupFile);
			FileUtils.copyFile(srcFile, backupFile);
		}
	}

	/**
	 * 创建目录
	 * @param file 文件
	 */
	private void mkdir(File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
	}

	/**
	 * 得到备份文件名称
	 * @param srcFile 原文件
	 * @return 备份文件名
	 */
	private String getBackupFileName(File srcFile) {
		return backupBasePath + File.separator + "_tobedeleted"
				+ File.separator + getCurrentDate() + File.separator
				+ srcFile.getName() + "_" + getCurrentTime();
	}

	/**
	 * 当前日期
	 * @return 当前日期
	 */
	private String getCurrentDate() {
		StringBuffer rtn = new StringBuffer();
		Calendar date = Calendar.getInstance();
		rtn.append(date.get(Calendar.YEAR));
		rtn.append(format(date.get(Calendar.MONTH) + 1));
		rtn.append(format(date.get(Calendar.DATE)));
		return rtn.toString();
	}

	/**
	 * 当前时间
	 * @return 当前时间
	 */
	private String getCurrentTime() {
		StringBuffer rtn = new StringBuffer();
		Calendar date = Calendar.getInstance();
		rtn.append(format(date.get(Calendar.HOUR_OF_DAY)));
		rtn.append(format(date.get(Calendar.MINUTE) + 1));
		rtn.append(format(date.get(Calendar.SECOND) + 1));
		return rtn.toString();
	}

	/**
	 * 格式化小于10的数据，前面加0
	 * @param dateField 数字
	 * @return 小于10的数据，前面加0
	 */
	private String format(int dateField) {
		if (dateField < 10) {
			return "0" + dateField;
		}

		return String.valueOf(dateField);
	}
}
