package com.rh.gw.gdjh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.render.RenderAPI;
import com.rh.core.base.BaseContext;
import com.rh.core.base.Bean;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
//import com.deepoove.poi.XWPFTemplate;
//import com.deepoove.poi.render.RenderAPI;
//import com.sun.xml.internal.ws.policy.privateutil.PolicyLogger;


public class FileToZip extends CommonServ{
	private static Log log = LogFactory.getLog(FileToZip.class);
	
	public static void main(String[] args) throws Exception {
//		zip("D:\\testZip");
		System.out.println("1111");
//		getFileWord();
		
	}
	
	
	//处理传入的文件或者文件名
	public static void zip(String inputDirectoryFile) throws Exception {
		String zipFileName=inputDirectoryFile;
		String dir=zipFileName.substring(0,zipFileName.lastIndexOf("\\")+1);
		zipFileName=zipFileName.substring(zipFileName.lastIndexOf("\\")+1);
		if(zipFileName.indexOf(".")!=-1) {
			zipFileName=zipFileName.substring(0,zipFileName.lastIndexOf("."));
		}
		zip(inputDirectoryFile,dir+zipFileName+".zip");
	}
	
	//如果传入文件夹，则进行循环文件夹内文件
	public static void zip(String inputDirectoryFile,String zipFileName) throws IOException {
		File inputFile=new File(inputDirectoryFile);
		ZipOutputStream out=new ZipOutputStream(new FileOutputStream(zipFileName));
		String base="";
		if(inputFile.isFile()) {
			base=inputDirectoryFile.substring(inputDirectoryFile.lastIndexOf("\\")+1);
		}
		zip(out,inputFile,base);
		out.close();
	}
	
	//进行文件写入
	public static void zip(ZipOutputStream out,File f,String base) {
		try {
			if(f.isDirectory()) {
				File[] fl=f.listFiles();
				out.putNextEntry(new ZipEntry(base+"/"));
				base=base.length()==0?"":base+"/";
				for(int i=0;i<fl.length;i++) {
					zip(out,fl[i],base+fl[i].getName());
				}
			}else {
				out.putNextEntry(new ZipEntry(base));
				FileInputStream in=new FileInputStream(f);
				int b;
				while((b=in.read())!=-1) {
					out.write(b);
				}
				in.close();
			}
			
		}catch (Exception e) {
			log.error("异常信息："+e.getMessage(), e);
		}
	}
	
	//生成归档word文件
			public  static String getWordFile(String gdid,String gwPath) throws Exception {
				String wordFile="";
				String tcsql="select TMPL_CODE from OA_GW_GONGWEN where GW_ID='"+gdid+"'";
				List<Bean> query = Transaction.getExecutor().query(tcsql);
				if(query.size()>0) {
					Bean find=query.get(0);
				
				String tc=find.getStr("TMPL_CODE");
				String viewName="";
				String mbfile="";
				
				String rootPath = BaseContext.appStr(APP.WEBINF)+"/GwTmpl/";
				String path = FileToZip.class.getResource("/").getPath();
				switch (tc) {
				//业务发文
				case "OA_GW_GONGWEN_ICBC_YWFW":
					viewName="gd_oa_ywfw_view";
					mbfile=rootPath+"ywfw.docx";
					break;
				//行政发文
		        case "OA_GW_GONGWEN_ICBC_XZFW":
					viewName= "gd_oa_xzfw_view";
					mbfile=rootPath+"xzfw.docx";
					break;
				//收文
				case "OA_GW_GONGWEN_ICBCSW":
					viewName="gd_oa_sw_view";
					mbfile=rootPath+"sw.docx";
					break;
				//签报
		        case "OA_GW_GONGWEN_ICBCQB":
					viewName="gd_oa_qb_view";
					mbfile=rootPath+"qb.docx";
					break;
				}
				String wfsql="select * from "+viewName+" where gw_id='"+gdid+"'";
				List<Bean> wflist = Transaction.getExecutor().query(wfsql);
				if(wflist.size()>0) {
				Bean data = wflist.get(0);
				wordFile=gwPath+File.separator+gdid+".docx";
				getFileWord(mbfile,wordFile,data);
				}
				}
				return wordFile;
			}
			
			public  static void getFileWord(String mbPath,String wordPath,Bean bean) throws Exception {
				
				//读取模板进行渲染
				XWPFTemplate doc=XWPFTemplate.compile(mbPath);
				RenderAPI.render(doc, bean);
				//输出渲染后的文件
				FileOutputStream out=new FileOutputStream(wordPath);
				doc.write(out);
				out.flush();
				out.close();
			}
}
