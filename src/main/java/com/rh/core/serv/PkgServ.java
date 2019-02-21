package com.rh.core.serv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.util.PkgUtil;
import com.rh.core.util.DateUtils;
import com.rh.core.util.file.Unzip;
import com.rh.core.util.file.Zip;

public class PkgServ extends CommonServ {

	private Logger log = Logger.getLogger(getClass());

	private final String outFile = Context.appStr(APP.WEBINF).replace("/", File.separator) + "update" + File.separator ;

	private final String appPath = Context.appStr(APP.SYSPATH);

	private final String fileExt[] = {".jsp", ".js", ".css", ".gif", ".jpg", ".ftl", ".json",".jsonx", ".java", ".class", ".jar", ".html",".htm"};

	private ArrayList<File> fileList = new ArrayList<File>();
	
	private ArrayList<String> listPath = new ArrayList<String>();

	public void addFileList(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files) {
					String ext = f.getName().substring(f.getName().indexOf("."));
					if (Arrays.asList(fileExt).contains(ext)) {
						fileList.add(f);
					}
				}
			}
		}
	}

	public OutBean doPackage(ParamBean paramBean) {
		fileList = new ArrayList<File>();
		OutBean result = new OutBean();
		String fileStr = paramBean.getStr("filelist");
		String[] fileArg = fileStr.split("\n");

		for (String pathName : fileArg) {
			File file = new File(appPath + pathName);

			if (file.exists()) {
				fileList.add(file);
			} else if (pathName.endsWith("*")) {
				addFileList(file.getParentFile());
			}
		}

		if (fileList.size() > 0) {
			String packagePath = fileToZip(fileList);
			result.set("packagePath", packagePath);
		}

		return result;
	}

	public OutBean downloadPackageFile(ParamBean paramBean) {
		return download(paramBean.getStr("packageFile"));
	}

	public OutBean download(String path) {
		OutBean result = new OutBean();
		File zipFile = new File(path);
		if (zipFile != null) {
			String fileName = zipFile.getName();
			InputStream is = null;
			try {
				is = new FileInputStream(zipFile);
				String downFileType = "attachment;";
				Context.getResponse().setHeader("content-disposition", "attachment;filename=" + fileName);
				Context.getResponse().setContentType(downFileType);
				OutputStream out = Context.getResponse().getOutputStream();
				IOUtils.copyLarge(is, out);
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(out);
				out.flush();
				zipFile.delete();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				try {
					IOUtils.closeQuietly(is);
					Context.getResponse().flushBuffer();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		} else {
			result.setError("文件不存在");
		}
		return result;
	}

	public OutBean search(ParamBean paramBean) {
		fileList = new ArrayList();
		OutBean result = new OutBean();
		String zipPath = searchFile2Zip(paramBean);
		download(zipPath);
		return result;
	}

	private String searchFile2Zip(ParamBean paramBean) {
		String begin = paramBean.getStr("START");
		String end = paramBean.getStr("END");

		PkgUtil util = new PkgUtil();
		util.addExFolder(outFile);
		util.addExFolder(Context.appStr(APP.WEBINF).replace("/", File.separator)+"work");
		fileList = util.searchFile(new File(appPath), begin, end, fileExt);

		return fileToZip(fileList);
	}
	
	public String fileToZip1(ArrayList<File> fileList) {
		String outZipPath = outFile + DateUtils.getDate() + ".zip";
		
		try {
			Zip zip = new Zip(new File(outZipPath));
			for (File file : fileList) {
				
				String basePath = file.getParent().replace(appPath, "");
				
				zip.addFile(file,basePath);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return outZipPath;
	}
	
	public String fileToZip(ArrayList<File> fileList) {
		String outZipPath = outFile + DateUtils.getDate() + ".zip";
		if (fileList.size() > 0) {
			try {
				PkgUtil.checkFile(new File(outZipPath));
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outZipPath));
				for (File file : fileList) {
					String filePath = file.getAbsolutePath().substring(appPath.length());
					compressFile(file, out, filePath);
				}
				out.flush();
				out.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return outZipPath;
	}

	private void compressFile(File file, ZipOutputStream out, String filePath) {
		if (!file.exists()) {
			return;
		}
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			ZipEntry entry = new ZipEntry(filePath);
			out.putNextEntry(entry);
			int count;
			byte data[] = new byte[2048];
			while ((count = bis.read(data, 0, 2048)) != -1) {
				out.write(data, 0, count);
			}
			bis.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public OutBean uploadPackage(ParamBean paramBean) {
		OutBean out = new OutBean();
		
		String fileId = paramBean.getStr("fileId");
		
		Bean fileBean = FileMgr.getFile(fileId.replaceAll(",", ""));
		
		String disName = fileBean.getStr("DIS_NAME").trim();
		
		String unzipPath = outFile + disName +  File.separator + fileId;
		try {
			Unzip unzip = new Unzip();
			String filePath = FileMgr.getAbsolutePath(fileBean.getStr("FILE_PATH"));
			unzip.unzip(filePath, unzipPath);
			FileMgr.deleteFile(fileBean);
			
			listPath = new ArrayList<String>();

			eachFilelist(new File(unzipPath));
			
			updatePackage(unzipPath);
			
			out.setOk("更新完成");
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			
		}
		return out;
	}
	
	private void updatePackage(String unzipPath) throws IOException {
		
		for(String filePath : listPath){
			//应用文件路径
			String appFile = filePath.replace(unzipPath, appPath);
//			System.out.println(appFile);
			//备份文件路径
			String backFile = filePath.replace(unzipPath, unzipPath + File.separator + "backup");
//			System.out.println(backFile);
			//备份文件
			FileUtils.copyFile(new File(appFile), new File(backFile));
			//更新文件
			FileUtils.copyFile(new File(filePath), new File(appFile));
		}
	}

	private void eachFilelist(File files) {
		for (File file : files.listFiles()) {
			if (file.isDirectory()) {
				eachFilelist(file);
			} else if (file.exists()) {
				listPath.add(file.getAbsolutePath());
			}
		}
	}
	
}
