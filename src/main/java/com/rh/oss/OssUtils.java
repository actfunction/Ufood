package com.rh.oss;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
public class OssUtils {
	private static final String AUTH = "Authorization";
    private static final String DATE = "Date";
    private static final String BUCKETACL = "x-oss-acl";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String OSS_ACL = "x-oss-object-acl";
    private static String uri = "http://122.67.93.57:8080";
	private static String accessKey = "superuser";
	private static String securityKey = "4e143106880ad166f3f17496548ad278"; 
	public static String authorization =null;
	public static String authorization1 =null;
	public static String url = null;
	
	static {
		try {
			authorization = new String(Base64.encode((accessKey + ":" + securityKey).getBytes("UTF-8")),"UTF-8");	
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	public enum Acl{
		ACL_DEFAULT,ACL_PUBLIC_READ_WRITER,ACL_PUBLIC_READ,ACL_PRIVATE
	}
	
	/**
	 * 获取当前时间
	 * @return
	 */
	private static String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return sdf.format(date);
    }

	/**
	 * 对应的文档5.2.9.获取BucketInfo
	 * @param bucket
	 * @return
	 * @throws Exception
	 */
	public static int getBucketInfo(String bucket) throws Exception {	
        String date = getDate();
        HttpURLConnection httpConnection = null;
        int code = 0;
        try {
            URL url = new URL(uri + "?bucketInfo&bucketName=" + URLEncoder.encode(bucket, "UTF-8"));
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(AUTH, authorization);
            httpConnection.setRequestProperty(DATE, date);
            httpConnection.connect();
            code = httpConnection.getResponseCode();   
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null)
                httpConnection.disconnect();
        }
		return code;
    }
	
	/**
	 * 创建bucket
	 * @param bucket
	 * @return
	 * @throws Exception
	 */
	public static int putBucket(String bucket) throws Exception{
		
		return putBucket(bucket,(String) null);
	}
	
	
	/**
	 * 创建bucket
	 * Put bucket的功能是创建具体名字的存储桶bucket
	 * @param bucket
	 * @param acl
	 * @return
	 * @throws Exception
	 */
	public static int putBucket(String bucket,String acl) throws Exception{
		String date = getDate();
        HttpURLConnection httpConnection = null;
        int code = 0;
        try {
            URL url = new URL(uri + "?bucketName=" + URLEncoder.encode(bucket, "UTF-8"));
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("PUT");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(AUTH, authorization);
            httpConnection.setRequestProperty(DATE, date);
            if(acl!=null) {
            	httpConnection.setRequestProperty(BUCKETACL, acl);        	
            }
            httpConnection.setFixedLengthStreamingMode(0);         
            httpConnection.connect();
            code = httpConnection.getResponseCode();
           // System.out.println("code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null)
                httpConnection.disconnect();
        }
		return code;
	}
	/**
	 * 创建object对象
	 * @param bucket
	 * @param object
	 * @param acl
	 * @param metas
	 * @param contentType
	 * @param contentLength
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static int putObject(String bucket , String object ,Acl acl,Map<String,String> metas,String contentType,long contentLength,InputStream is) throws Exception{
		String date = getDate();
		OutputStream os = null;
        HttpURLConnection httpConnection = null;
        int code = 0;
        try {
            URL url = new URL(uri + "/" + getObjectInUrl(object) +"?bucketName=" + URLEncoder.encode(bucket, "UTF-8"));
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("PUT");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(AUTH, authorization);
            httpConnection.setRequestProperty(DATE, date);
            httpConnection.setFixedLengthStreamingMode(contentLength);
            if(contentType != null) 
            	 httpConnection.setRequestProperty(CONTENT_TYPE, contentType);          
            if(acl != null) 
            	 httpConnection.setRequestProperty(OSS_ACL, acl.ordinal()+ "");        
            if(metas != null) 
            	for(Map.Entry<String, String> e : metas.entrySet()) {
               	 httpConnection.setRequestProperty(e.getKey(), e.getValue());           	
            }
            httpConnection.connect();
            os = httpConnection.getOutputStream();
            if(is != null) 
            	writeData(is,os);
            	os.flush();
            code = httpConnection.getResponseCode();
            //System.out.println("code:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if(os != null && is != null) 
        		try {
					is.close();
					os.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
        	}
            if (httpConnection != null)
                httpConnection.disconnect();
        }
		return code;
	}
	/**
	    * 获取对象
	 * @param bucket
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static int getObject(String bucket , String object) throws Exception{
		String date = getDate();
        HttpURLConnection httpConnection = null;
        InputStream is = null;
        int code = 0;
        try {
            URL url = new URL(uri + "?bucketName=" + URLEncoder.encode(bucket, "UTF-8"));
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(AUTH, authorization);
            httpConnection.setRequestProperty(DATE, date);
            httpConnection.connect();
            code = httpConnection.getResponseCode();
            System.out.println("code:" + code);           
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if(is != null) {
        		try {
					is.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
            if (httpConnection != null)
                httpConnection.disconnect();
        }
		return code;
	}
	 /**
	      * 获取对象
	  * @param bucket
	  * @param object
	  * @param downFile
	  * @return
	  * @throws Exception
	  */
	public static int getObject(String bucket , String object,File downFile) throws Exception{
		String date = getDate();
        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream output = null;
        int code = 0;
        try {
            URL url = new URL(uri + "/" + getObjectInUrl(object) +"?bucketName=" + URLEncoder.encode(bucket, "UTF-8"));
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(AUTH, authorization);
            httpConnection.setRequestProperty(DATE, date);

            //http://122.67.93.57:9998/test.html
            
            httpConnection.connect();           
            code = httpConnection.getResponseCode();
            System.out.println("code:" + code);
            if(code == 200) {
            	is = httpConnection.getInputStream();
            	output = new FileOutputStream(downFile);
            	writeData(is,output);       
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if(is != null) {
        		try {
					is.close();
					output.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
            if (httpConnection != null)
                httpConnection.disconnect();
        }
		return code;
	}
	
	public static int deleteObject(String bucket,String object) throws Exception {
		String date = getDate();
        HttpURLConnection httpConnection = null;     
        int code = 0;
        try {
            URL url = new URL(uri + "/" + getObjectInUrl(object) +"?bucketName=" + URLEncoder.encode(bucket, "UTF-8"));
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("DELETE");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(AUTH, authorization);
            httpConnection.setRequestProperty(DATE, date);
            httpConnection.connect();
            code = httpConnection.getResponseCode();
            System.out.println("code:" + code);        
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null)
                httpConnection.disconnect();
        }
		return code;
	}
	/**
	 * 写数据
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	private static void writeData(InputStream is,OutputStream os) throws IOException{
		byte[] b = new byte[4096];
		int len = 0;
		while((len = is.read(b))>0) {
			os.write(b,0,len);
		}
	}
	/**
	 *获取文件对象路径
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private static String getObjectInUrl(String path) throws Exception{
		String objectRebuild = "";
		try {		
			path = path.replace('/', '|');
			objectRebuild = URLEncoder.encode(path, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		return objectRebuild;		
	}
	/**
	 * 
	 * @param bucketName
	 * @param objectName
	 * @param auth
	 * @return
	 * @throws Exception
	 */
    public static String getUrl(String bucketName,String objectName,String auth) throws Exception {	
        authorization1 ="8569cf7b074367763e6f6778dc22ab9f29d327fda34e30bde6b85dac7718bfdb09c590a93fd8780c42d14407adba55a9dd8f26778d9f645a15d9d2cf53103454";
		url = uri + "/" +"download"+"?bucketName=" + URLEncoder.encode(bucketName, "UTF-8")+"&objectName="+objectName+"&auth="+authorization1;		
		return url;
	}
    private static String StringTo16(String string) {
		char[] chars = string.toCharArray();
		StringBuilder sb = new StringBuilder();
		byte[] bs = string.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i]&0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i]&0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString().trim();
	}
    public static Object getFile() {
    	String fileId2 = "37WuZ0z49blGXuAnkTavdg.doc";
		String fileSql = "SELECT FILE_PATH FROM PLATFORM.SY_COMM_FILE WHERE FILE_ID ='"+fileId2+"'";
		List<Bean> query = Transaction.getExecutor().query(fileSql); 
		for(Bean file : query) {
			String filePath = file.getStr("FILE_PATH");
			System.out.println(filePath);
		}
		return query;
	}
    public static void OssHelper(String address, String userName, String passWord) {
        uri = "http://" + address;
        accessKey = userName;
        securityKey = passWord;
        try {
			authorization = new String(Base64.encode((accessKey + ":" + securityKey).getBytes("UTF-8")),"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
	public static void main(String[] args) {
		try {
			//System.out.println(md5("gls"));
			String bucket = "oos1";			
//			String object = "任务分配.xls";
			String object = "正文.doc";
			String pathname = "D:/data/upload_files/cnao/OA_GW_GONGWEN_ICBC_XZFW/2018/2018-12-18/37WuZ0z49blGXuAnkTavdg.doc";
			File file = new File(pathname);	
			//putBucket(bucket,"");
			getBucketInfo(bucket);
			putObject(bucket, object, Acl.ACL_PRIVATE, null, "text/plain", file.length(), new FileInputStream(file));
			getObject(bucket, object);
			File downFile = null;
			getObject(bucket, object,downFile);
			deleteObject(bucket, object);
			getObjectInUrl(pathname);
		} catch (Exception e) {
			// TODO Auto-generated catch block                                                                       
			e.printStackTrace();
		}
		
	}

	public static String md5(String str) {
		try {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(str.getBytes());
		byte b[] = md.digest();

		int i;

		StringBuffer buf = new StringBuffer("");
		for (int offset = 0; offset < b.length; offset++) {
		i = b[offset];
		if (i < 0)
		i += 256;
		if (i < 16)
		buf.append("0");
		buf.append(Integer.toHexString(i));
		}
		str = buf.toString();
		} catch (Exception e) {
		e.printStackTrace();

		}
		return str;
		}
	
}
