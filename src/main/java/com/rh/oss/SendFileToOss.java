package com.rh.oss;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;

public class SendFileToOss{ 
	
	public static Map<String,String> metas = null;
	public static String contentType = null;
	public static long contentLength = 0;
	public static String authorization1 ="8569cf7b074367763e6f6778dc22ab9f29d327fda34e30bde6b85dac7718bfdb09c590a93fd8780c42d14407adba55a9dd8f26778d9f645a15d9d2cf53103454";
	
	public static String OssTest(String fileId) throws Exception {		
		SimpleDateFormat date=new SimpleDateFormat("MM-dd-HH-ss-mm");
		String d = date.format(new Date());
		
		String fileSql = "SELECT * FROM PLATFORM.SY_COMM_FILE WHERE FILE_ID ='"+fileId+"'";		
		 List<Bean> list = Transaction.getExecutor().query(fileSql);		 		
		Bean fileBean = list.get(0);					
		String filePath = FileMgr.getAbsolutePath(fileBean.getStr("FILE_PATH"));		
		String repFile1 = filePath.replaceAll("//", "/");
		String repFile2 = repFile1.replaceAll("\\\\", "/");		
		String repFile3 =  repFile2.replaceAll("upload_files", "data//upload_files");	
 		String fileName =  FileMgr.getAbsolutePath(fileBean.getStr("FILE_NAME"));
		String[] str = fileName.split("/");
		String strr = d+"-"+str[str.length-1];		
		String bucket = "gw-file-storage"; 
		File file = new File(repFile3);		
		InputStream file1 = new FileInputStream(file);
		int info = OssUtils.getBucketInfo(bucket);
		if(info!=200) {
			OssUtils.putBucket(bucket);
		}				
			OssUtils.putObject(bucket, strr, OssUtils.Acl.ACL_PRIVATE, metas,"text/plain", file.length(), file1);
		String url = OssUtils.getUrl(bucket, strr, authorization1);
		return url;		
	}		
}
