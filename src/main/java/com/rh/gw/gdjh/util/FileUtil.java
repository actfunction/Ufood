package com.rh.gw.gdjh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParseZip.class);
	public static Map<Object, Object> getFile(String path,String outPath)  {
		
		Map<Object, Object>  FileDetil=new HashMap<>();
		File file=new File(path);	
		String suffix = file.getPath().substring(file.getPath().lastIndexOf(".")+1);
		if ("ofd".equals(suffix)) {
				//正文
				FileDetil.put("fileName",file.getName());
				FileDetil.put("fileSoft","");
				FileDetil.put("docType",3);
				FileDetil.put("fileSize", FileUtil.getFilesize(file)+"字节");
			}else {
				//附件
				FileDetil.put("fileName",file.getName());
				FileDetil.put("fileSoft",FileUtil.getFilesoft(file.getPath()));
				FileDetil.put("docType",4);
				FileDetil.put("fileSize", FileUtil.getFilesize(file)+"字节");
				File outfile=new File(outPath+"/"+file.getName());
				//附件路径地址
				
				FileInputStream fis=null;
				
				FileOutputStream fos=null;
				
				try {
					 fis = new FileInputStream(file);
					 fos=new FileOutputStream(outfile);
					    
						byte[] bytes=new byte[1024];
						int b=0;
						while((b=fis.read())!=-1){
							fos.write(bytes, 0, b);
						}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					try {
						fis.close();
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("异常信息："+e.getMessage(), e);
					}
				}
			
			}
			
			
	
		return FileDetil;
	}
    /**获取文件名称
		 * @param filepath 文件完整路径，包括文件名
		 * @return
		 */
//	public static String getFilename(File file){
//		return file.getName();
//	}
	
	 /**创建程序
     * @param filepath 文件完整路径，包括文件名
     * @return
     */
	public static String getFilesoft(String filePath){
		String soft="";
		String suffix = filePath.substring(filePath.lastIndexOf(".")+1);
		if (suffix.equals("doc")||suffix.equals("xls")||suffix.equals("ppt")) {
			 soft="WPS";							
		}			
		return soft;
	}
	
	 /**获取文件大小 返回 KB   没有文件时返回
		 * @param filepath 文件完整路径，包括文件名
		 * @return
		 */
	public static Integer getFilesize(File file){
		DecimalFormat df=new DecimalFormat("0");
		return Integer.parseInt(df.format(Double.valueOf(file.length())/1000));
	}
	
	/**
	 * 
	 * @title: delete
	 * @descriptin: 删除文件夹，包括文件夹下的所有文件
	 * @param @param path 删除文件夹路径
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	/**
	 * 删除文件
	 */
	public static void deleteFile(File file) {
			if(file.isDirectory()) {
				File[] files=file.listFiles();
				if(files.length!=0) {
					for (int i = 0; i < files.length; i++) {
						deleteFile(files[i]);
					}
				}else {
					file.delete();
				}
			}else {
				file.delete();
			}
			if(file.exists()) {
				file.delete();
			}
	}
	
}
