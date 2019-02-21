package com.rh.gw.gdjh.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseZip {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParseZip.class);
	/**
	 * 解压zip文件
	 * @param zipFileName String 输入zip文件
	 * @Param outputDirectory String 输出目录
	 * @throws Exception
	 */
	public static void  unZip(String zipFileName,String outputDirectory) throws Exception{
		try {
			Charset gbk=Charset.forName("GBK");
			ZipFile zf=new ZipFile(zipFileName,gbk);
			Enumeration e=zf.entries();
			ZipEntry ze=null;
			createDirectory(outputDirectory,"");
			File rootFile = new File(outputDirectory);
			if(!rootFile.exists()) {
				rootFile.mkdirs();
			}
			while(e.hasMoreElements()) {
				ze=(ZipEntry) e.nextElement();
				if(ze.isDirectory()) {
					String name=ze.getName();
					name= name.substring(0,name.length()-1);
					File f=new File(outputDirectory+File.separator+name);
					f.mkdir();
				}
				else {
					String fileName=ze.getName();
					fileName=fileName.replace("\\", "/");
					if(fileName.indexOf("/")!=-1) {
						createDirectory(outputDirectory,fileName.substring(0,fileName.lastIndexOf("/")));
						fileName=fileName.substring(fileName.lastIndexOf("/")+1,fileName.length());
					}
					File f=new File(outputDirectory+File.separator+ze.getName());
					f.createNewFile();
					InputStream inputStream = zf.getInputStream(ze);
					FileOutputStream out=new FileOutputStream(f);
					byte[] by=new byte[1024];
					int c;
					while((c=inputStream.read(by))!=-1) {
						out.write(by,0,c);
					}
					out.close();
					inputStream.close();
				}
			}
			zf.close();
			}catch (Exception ex) {
				LOGGER.error("异常信息："+ex.getMessage(), ex);
		}
	}
	
	
	private static void createDirectory(String directory,String subDirectory) {
		String dir[];
		File fl=new File(directory);
		try {
			if(subDirectory==""&&fl.exists()!=true) {
				fl.mkdir();
			}
				else if(subDirectory!="") {
					dir=subDirectory.replace("\\", "/").split("/");
					for (int i = 0; i < dir.length; i++) {
						File subFile=new File(directory+File.separator+dir[i]);
						if(subFile.exists()==false) {
							subFile.mkdir();
						}
						directory+=File.separator+dir[i];
					}
				}
		}catch (Exception e) {
			LOGGER.error("异常信息："+e.getMessage(), e);
		}
	}
	
	
}
