package com.rh.core.serv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.comm.FileMgr;
import com.rh.core.org.UserBean;
import com.rh.core.serv.util.PkgUtil;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.db.WfProcDefDao;
import com.rh.core.wfe.def.WFParser;
import com.rh.core.wfe.def.WfServCorrespond;
import com.rh.core.wfe.util.WfeConstant;

public class FlowPkgServ extends CommonServ {
	
	
	private final String outFile = Context.appStr(APP.WEBINF).replace("/", File.separator) + "package" + File.separator + Context.getUserBean().getCode() + File.separator;

	private final String appPath = Context.appStr(APP.SYSPATH);

	private final String docPath = Context.appStr(APP.WEBINF).replace("/", File.separator) + "doc" + File.separator;
	
	private static final String SERV_SY_SERV = "SY_SERV";

	public OutBean show(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String user_code = Context.getUserBean().getCode();
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        
        String httpUrl = request.getScheme() + "://" + request.getServerName() + ":" 
                + request.getServerPort();
        try {
			response.sendRedirect(httpUrl+"/sy/base/view/stdCardView.jsp?frameId=FM_WFE_PACKAGE-card-dopkCode"+user_code+"-"+user_code+"-tabFrame&sId=FM_WFE_PACKAGE&areaId=&paramsFlag=false&title=%E6%B5%81%E7%A8%8B%E6%89%93%E5%8C%85&pkCode="+user_code);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return outBean; 
    }
	
	@SuppressWarnings("unchecked")
	public OutBean getExportInfo(ParamBean paramBean) {
		OutBean resultBean = new OutBean();
		String procDefIds = paramBean.getStr("procIds");
		ArrayList<String> arr_serv = new ArrayList<String>();
		ArrayList<String> arr_js = new ArrayList<String>();
		ArrayList<String> arr_dict = new ArrayList<String>();
		for (String proc_code : procDefIds.split(",")) {
			Bean fmProcBean = ServDao.find(ServMgr.SY_WFE_PROC_DEF, proc_code);
			String servId = fmProcBean.getStr("SERV_ID");
			arr_serv.add(servId);
			// 取得服务ID文件路径
			// 取得卡片列表js路径
			ArrayList<String> tmpJsArr = (ArrayList<String>) getServFileStr(servId).getData();
			for (String str : tmpJsArr) {
				arr_js.add(str);
			}
			// 取得服务相关字典文件路径
			ArrayList<String> tmpDictArr = (ArrayList<String>) getDictFileStr(servId).getData();
			for (String str : tmpDictArr) {
				if (!arr_dict.contains(str)) {
					arr_dict.add(str);
				}
			}
		}
		Bean resBean = new Bean();
		resBean.set("arr_serv", arr_serv);
		resBean.set("arr_js", arr_js);
		resBean.set("arr_dict", arr_dict);
		resultBean.setData(resBean);
		return resultBean;
	}

	/**
	 * 根据服务ID 获得服务josn文件 card.js list.js路径
	 * 
	 * @param servId
	 * @return
	 */
	public OutBean getServFileStr(String servId) {
		OutBean result = new OutBean();
		ArrayList<String> arr = new ArrayList<String>();
		String prefix = servId.substring(0, 2);
		prefix = prefix.toLowerCase();
		Bean fmServBean = ServDao.find(ServMgr.SY_SERV, servId);
		if(fmServBean == null) return result;
		int SERV_LIST_LOAD = fmServBean.getInt("SERV_LIST_LOAD");
		int SERV_CARD_LOAD = fmServBean.getInt("SERV_CARD_LOAD");
		// String SERV_CLASS = fmServBean.getStr("SERV_CLASS");
		if (SERV_LIST_LOAD == 1) {
			String tmpPath = prefix + "/servjs/" + servId + "_list.js";
			arr.add(tmpPath);
		}
		if (SERV_CARD_LOAD == 1) {
			String tmpPath = prefix + "/servjs/" + servId + "_card.js";
			arr.add(tmpPath);
		}
		result.setData(arr);
		return result;
	}

	/**
	 * 根据服务ID 获得字典josn文件 路径
	 * 
	 * @param servId
	 * @return
	 */
	public OutBean getDictFileStr(String servId) {
		OutBean result = new OutBean();
		ArrayList<String> arr = new ArrayList<String>();
		List<Bean> fmItemList = ServDao.finds(ServMgr.SY_SERV_ITEM, " and SERV_ID = '" + servId + "'");
		for (Bean bean : fmItemList) {
			// 字段编码
			// String ITEM_CODE = bean.getStr("ITEM_CODE");
			// 输入类型 2.下拉框 3.单选框 4.多选框
			int ITEM_INPUT_TYPE = bean.getInt("ITEM_INPUT_TYPE");
			// 输入模式 3.树形选择
			int ITEM_INPUT_MODE = bean.getInt("ITEM_INPUT_MODE");
			// 输入设置
			String ITEM_INPUT_CONFIG = bean.getStr("ITEM_INPUT_CONFIG");

			if (ITEM_INPUT_TYPE == 2 || ITEM_INPUT_TYPE == 3 || ITEM_INPUT_TYPE == 4) {
				if (!ITEM_INPUT_CONFIG.isEmpty()) {
					arr.add(ITEM_INPUT_CONFIG);
				}
			} else if (ITEM_INPUT_MODE == 3) {
				if (!ITEM_INPUT_CONFIG.isEmpty()) {
					arr.add(ITEM_INPUT_CONFIG.split(",")[0]);
				}
			}
		}
		result.setData(arr);
		return result;
	}

	public OutBean expFile(ParamBean paramBean) {
		OutBean result = new OutBean();
		String PROC_NAME = paramBean.getStr("PROC_NAME");
		String SERV_NAME = paramBean.getStr("SERV_NAME");
		String SERV_FILE = paramBean.getStr("SERV_FILE");
		String DICT_FILE = paramBean.getStr("DICT_FILE");
		//导出文件顺序不能更换
		if(!SERV_NAME.isEmpty())getServZip(SERV_NAME);
		if(!DICT_FILE.isEmpty())getDictZip(DICT_FILE);
		if(!PROC_NAME.isEmpty()) getProcZip(PROC_NAME);
		if(!SERV_FILE.isEmpty())getServJsZip(SERV_FILE);
		
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		String filePath = outFile + ft.format(dNow) + File.separator;
		String zipPath = getTotalZip(filePath);
		if (zipPath != null) {
			String fileName = ft.format(dNow) + ".zip";
			// 设置响应头，控制浏览器下载该文件
			OutputStream out = null;
			try {
				Context.getResponse().setContentType("application/x-download");
				Context.getResponse().setHeader("content-disposition",
						"attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

				// 读取要下载的文件，保存到文件输入流
				FileInputStream in = new FileInputStream(zipPath);
				// 创建输出流
				out = Context.getResponse().getOutputStream();
				// 创建缓冲区
				byte buffer[] = new byte[1024];
				int len = 0;
				// 循环将输入流中的内容读取到缓冲区当中
				while ((len = in.read(buffer)) > 0) {
					// 输出缓冲区的内容到浏览器，实现文件下载
					out.write(buffer, 0, len);
				}
				// 关闭文件输入流
				in.close();
				
				//删除文件
				File delFileDir = new File(filePath);
				FileUtils.deleteDirectory(delFileDir);
				
			} catch (IOException e) {
				log.error("流程导出失败", e);
				e.printStackTrace();
			} finally {
				try {
					// 关闭输出流
					out.close();
					out.flush();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
		return result;
	}

	public void getProcZip(String procDefIds) {
		if (procDefIds.isEmpty())
			return;
		if (procDefIds.indexOf("\n") > -1) {
			procDefIds = procDefIds.replaceAll("\n", "'" + Constant.SEPARATOR + "'");
		}
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE, "AND PROC_CODE IN ('" + procDefIds + "')");
		queryBean.set(Constant.PARAM_ORDER, "EN_NAME DESC, PROC_VERSION DESC");
		List<Bean> procBeanList = ServDao.finds(ServMgr.SY_WFE_PROC_DEF, queryBean);
		if(procBeanList.size() == 0) return;
//		HttpServletRequest request = Context.getRequest();
		HttpServletResponse response = Context.getResponse();
		response.setContentType("application/x-download");
		// RequestUtils.setDownFileName(request, response,
		// ServMgr.SY_WFE_PROC_DEF + ".zip");

		ZipOutputStream zipOut = null;
		try {
			String outZipPath = outFile + DateUtils.getDate() + File.separator + ServMgr.SY_WFE_PROC_DEF + ".zip";
			PkgUtil.checkFile(new File(outZipPath));
			zipOut = new ZipOutputStream(new FileOutputStream(outZipPath));
			for (Bean procBean : procBeanList) {
				zipOut.putNextEntry(new ZipEntry(procBean.getId() + ".json"));
				IOUtils.write(JsonUtils.toJson(procBean, true), zipOut, Constant.ENCODING);
				zipOut.closeEntry();
			}
		} catch (Exception e) {
			log.error("流程导出失败", e);
			e.printStackTrace();
		} finally {
			if (zipOut != null) {
				try {
					zipOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				IOUtils.closeQuietly(zipOut);
			}
		}
	}

	public void getServZip(String servIds) {
		if (servIds.isEmpty())
			return;
		ArrayList<File> fileList = new ArrayList<File>();
		String[] servArr = servIds.split("\n");
		for (String dictName : servArr) {
			File file = new File(docPath + "SY_SERV" + File.separator + dictName + ".json");
			if (file.exists()) {
				fileList.add(file);
			}
		}
		if (fileList.size() > 0) {
			String outZipPath = outFile + DateUtils.getDate() + File.separator + ServMgr.SY_SERV + ".zip";
			fileToZip(fileList, outZipPath, true);
		}
	}

	public void getServJsZip(String filesPath) {
		if (filesPath.isEmpty())
			return;
		ArrayList<File> fileList = new ArrayList<File>();

		String[] fileArg = filesPath.split("\n");
		for (String pathName : fileArg) {
			File file = new File(appPath + pathName);
			if (file.exists()) {
				fileList.add(file);
			}
		}
		if (fileList.size() > 0) {
			String outZipPath = outFile + DateUtils.getDate() + File.separator + "file.zip";
			fileToZip(fileList, outZipPath, false);
		}
	}

	public String getTotalZip(String filePath) {
		if (filePath.isEmpty())
			return null;
		ArrayList<File> fileList = new ArrayList<File>();
		String[] fileArg = { "SY_WFE_PROC_DEF.zip", "SY_SERV.zip", "SY_SERV_DICT.zip", "file.zip" };
		for (String pathName : fileArg) {
			File file = new File(filePath + pathName);
			if (file.exists()) {
				fileList.add(file);
			}
		}
		if (fileList.size() > 0) {
			String outZipPath = outFile + DateUtils.getDate() + ".zip";
			// fileToZip(fileList,outZipPath,false);
			try {
				PkgUtil.checkFile(new File(outZipPath));
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outZipPath));
				for (File file : fileList) {
					String compressFilePath = file.getAbsolutePath().substring(filePath.length());
					compressFile(file, out, compressFilePath);
				}
				out.flush();
				out.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			return outZipPath;
		}
		return null;
	}

	public void getDictZip(String dictCodes) {
		if (dictCodes.isEmpty())
			return;
		ArrayList<File> fileList = new ArrayList<File>();

		String[] dictArr = dictCodes.split("\n");
		for (String dictName : dictArr) {
			File file = new File(docPath + "SY_SERV_DICT" + File.separator + dictName + ".json");
			if (file.exists()) {
				fileList.add(file);
			}
		}
		if (fileList.size() > 0) {
			String outZipPath = outFile + DateUtils.getDate() + File.separator + ServMgr.SY_SERV_DICT + ".zip";
			fileToZip(fileList, outZipPath, true);
		}
	}

	public String fileToZip(ArrayList<File> fileList, String outZipPath, boolean jsonFlag) {
		if (fileList.size() > 0) {
			try {
				PkgUtil.checkFile(new File(outZipPath));
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outZipPath));
				for (File file : fileList) {
					String filePath = file.getAbsolutePath().substring(appPath.length());
					if (jsonFlag) {
						if (file.getAbsolutePath().indexOf("SY_SERV/") != -1) {
							filePath = file.getAbsolutePath()
									.substring((docPath + "SY_SERV" + File.separator).length());
						} else if (file.getAbsolutePath().indexOf("SY_SERV_DICT/") != -1) {
							filePath = file.getAbsolutePath()
									.substring((docPath + "SY_SERV_DICT" + File.separator).length());
						}
						compressFile(file, out, filePath);
					} else {
						compressFile(file, out, filePath);
					}
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
	
	public OutBean impFile(ParamBean paramBean){
		OutBean result = new OutBean();
		String fileId = paramBean.getStr("fileId");
		Bean fileBean = FileMgr.getFile(fileId);
		if (fileBean != null) {
			ZipInputStream zipIn = null;
			InputStream in = null;
			try {
				ZipEntry zipEntry = null;
				if (fileBean.getStr("FILE_MTYPE").equals("application/zip")) {
					zipIn = new ZipInputStream(FileMgr.download(fileBean));
					Bean procBean = null;
					while ((zipEntry = zipIn.getNextEntry()) != null) {
						String zipChildName = zipEntry.getName();
//						System.out.println("-----11111111------------------" + zipChildName);
						if(zipChildName.equals("SY_SERV.zip")){
							BufferedInputStream bis = new BufferedInputStream(zipIn);
					        ZipInputStream zis = new ZipInputStream(bis);
					        while (zis.getNextEntry() != null) {
								in = new BufferedInputStream(zis);
								impJsonBean_Serv(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)));
//								zipIn.closeEntry();
							}
							
						}else if(zipChildName.equals("SY_SERV_DICT.zip")){
							
							BufferedInputStream bis = new BufferedInputStream(zipIn);
					        ZipInputStream zis = new ZipInputStream(bis);
					        while (zis.getNextEntry() != null) {
								in = new BufferedInputStream(zis);
								impJsonBean_Dict(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)));
//								zipIn.closeEntry();
							}
//							
						}else if(zipChildName.equals("SY_WFE_PROC_DEF.zip")){
							
							BufferedInputStream bis = new BufferedInputStream(zipIn);
					        ZipInputStream zis = new ZipInputStream(bis);
					        while (zis.getNextEntry() != null) {
								in = new BufferedInputStream(zis);
								procBean = JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING));
//								impJsonBean_Wf(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)));
//								zipIn.closeEntry();
							}
//							
						}else if(zipChildName.equals("file.zip")){
							
							BufferedInputStream bis = new BufferedInputStream(zipIn);
					        ZipInputStream zis = new ZipInputStream(bis);
					        ZipEntry entry  = zis.getNextEntry();
					        while (entry != null) {
					        	 String entryName = entry.getName();
//					        	 System.out.println("-----222------------------"+entryName);
					        	 if(entry.isDirectory()){
					        		 continue;
					        	 } else {
									int index = entryName.lastIndexOf("\\");
									if (index != -1) {
										File createDirectory = new File(
												appPath  + entryName.substring(0, index));
										createDirectory.mkdirs();
									}
									index = entryName.lastIndexOf("/");
									if (index != -1) {
										File createDirectory = new File(
												appPath + entryName.substring(0, index));
										createDirectory.mkdirs();
									}
									File tmpfile = new File(appPath + entry.getName());
									//如果已存在文件 重命名备份文件
									if (tmpfile.exists()) {
										DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
										tmpfile.renameTo(new File(appPath + entry.getName()+(df.format(new Date()))+".bak"));  
									}
									
									File unpackfile = new File(appPath + entry.getName());
									try {
							            BufferedInputStream bist = new BufferedInputStream(zis);
							            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unpackfile));
							            int b = -1;
							            byte[] buffer = new byte[1024];
							            while((b = bist.read(buffer))!= -1){
							                bos.write(buffer, 0, b);
							            }
							            bos.close();
					        	 	} catch (Exception e) {
					        	 		zis.close();
					        	 		throw new RuntimeException(e);
					        	 	}
								}
					        	 
					        	 entry  = zis.getNextEntry();
					        }
					        
					        
						}
					}
					
					//导入流程最后执行 因如果在服务导入前导入流程会报错
					if(procBean != null){
						Context.setThread(Context.THREAD.SERVID, WfProcDefDao.SY_WFE_PROC_DEF_SERV);
						impJsonBean_Wf(procBean);
					}
				} 
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				if (zipIn != null) {
					IOUtils.closeQuietly(zipIn);
				}
				if (in != null) {
					IOUtils.closeQuietly(in);
				}
			}
		}
		result.setOk();
		return result;
	}
	
	/**
	 * 导入JsonBean格式的服务定义，如果已经存在，则直接覆盖当前服务定义（删除后导入）
	 * 
	 * @param jsonBean
	 *            JsonBean格式的服务定义
	 * @return 是否成功导入
	 */
	private boolean impJsonBean_Serv(Bean jsonBean) {
		ParamBean param = new ParamBean();
		String servId = jsonBean.getStr("SERV_ID");
		//备份服务文件
		//查看是否存在可备份文件
		//如果已存在文件 重命名备份文件
		File servFile = new File(docPath + "SY_SERV" + File.separator + servId + ".json");
		if (servFile.exists()) {
            DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
            String tmpPath = docPath + "SY_SERV" + File.separator + servId + ".json" +".bak" + df.format(new Date());  
            File bakFile = new File(tmpPath);
            try {
				FileUtils.copyFile(servFile, bakFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		
		param.setId(servId).setServId(SERV_SY_SERV).setLinkFlag(true).set("$JSON_FLAG", false); // 不生成文件、级联删除
		delete(param);
		param = new ParamBean(jsonBean).setServId(SERV_SY_SERV).setAddFlag(true);
		return save(param).isOk();
	}
	
	/**
	 * 导入为流程定义
	 * 
	 * @param procDefBean
	 *            流程定义Bean
	 */
	private void impJsonBean_Wf(Bean procDefBean) {
		ParamBean param = new ParamBean(procDefBean);
		param.setServId(ServMgr.SY_WFE_PROC_DEF).set("xmlStr", procDefBean.getStr("PROC_XML"));
		saveWfAsNewVersion(param);
	}
	/**
	 * 保存当前的流程定义为最新版本
	 * 
	 * @param paramBean
	 *            流程定义信息
	 * @return Bean
	 */
	public OutBean saveWfAsNewVersion(ParamBean paramBean) {
		if (paramBean.getAddFlag()) {
			return saveWf(paramBean);
		}

		String procCode = paramBean.getStr("PROC_CODE");
		String procCodeWithoutVersion = procCode.substring(0, procCode.lastIndexOf(WfeConstant.PROC_VERSION_PREFIX));
		Bean latestProcDef = getLatestProcDef(procCodeWithoutVersion);
		if (latestProcDef == null) {
			paramBean.setAddFlag(true);
			return saveWf(paramBean);
		}else{
			//备份流程
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			File dir = new File(docPath + "PROC" + File.separator);
			String procPath = docPath + "PROC" + File.separator + procCodeWithoutVersion + df.format(new Date()) + ".json";
			File procFile = new File(procPath);
			try {
				if(!dir.exists()) dir.mkdirs();
				procFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			OutputStream output = null;
			try {
				output = new FileOutputStream(procPath);
				IOUtils.write(JsonUtils.toJson(latestProcDef, true), output, Constant.ENCODING);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(output != null){
					IOUtils.closeQuietly(output);
				}
			}
		}

		UserBean userBean = Context.getUserBean();
		// 置版本号
		int version = latestProcDef.getInt("PROC_VERSION") + 1;
		paramBean.set("PROC_VERSION", version);
		String newProcCode = procCodeWithoutVersion + WfeConstant.PROC_VERSION_PREFIX + version;
		paramBean.set("PROC_VERSION", version);
		paramBean.set("PROC_CODE", newProcCode);
		paramBean.set("PROC_IS_LATEST", WfeConstant.PROC_IS_LATEST);

		// 先将所有版本置为PROC_IS_LATEST=PROC_IS_NOT_LATEST
		updateProcDefToUnLatest(procCodeWithoutVersion);

		// 将 paramBean 中流程xml编码转换
		String wfXmlStr = paramBean.getStr("xmlStr");
		wfXmlStr = wfXmlStr.replaceAll("gb2312", "UTF-8");

		// 清除流程的业务服务的缓存
		String servId = paramBean.getStr("SERV_ID");
		ParamBean param = new ParamBean(ServMgr.SY_SERV, "clearCache");
		param.setId(servId);
		ServMgr.act(param);

		WFParser myParser = new WFParser(userBean.getCmpyCode(), paramBean);

		// 保存定义文件
		wfXmlStr = wfXmlStr.replaceAll("\r\n", "");
		myParser.setDefContent(wfXmlStr);
		myParser.setProcCode(newProcCode);
		myParser.save();

		OutBean rtnBean = new OutBean();
		rtnBean.setOk(Context.getSyMsg("SY_SAVE_OK"));
		rtnBean.setData(new Bean().set("PROC_CODE", newProcCode).set("PROC_VERSION", version));

		return rtnBean;
	}
	
	/**
	 * 保存流程定义
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return Bean
	 */
	public OutBean saveWf(ParamBean paramBean) {
		// 将 paramBean 中的值 转成ProcDefBean
		String wfXmlStr = paramBean.getStr("xmlStr");
		// wfXmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + wfXmlStr;
		wfXmlStr = wfXmlStr.replaceAll("gb2312", "UTF-8");

		UserBean userBean = Context.getUserBean();
		String newServId = paramBean.getStr("SERV_ID");

		// procCode为流程定义主键
		String procCode = paramBean.getStr("PROC_CODE");
		Bean oldProcBean = WfProcDefDao.getWfProcBeanByProcCode(procCode);
		try{
			if (null != oldProcBean) {
				String oldServId = oldProcBean.getStr("SERV_ID");
				clearCache(oldServId);
			}
			clearCache(newServId);
		}catch(Exception e){
		}
		WFParser myParser = new WFParser(userBean.getCmpyCode(), paramBean);

		myParser.setOldProcCode(procCode);
		// 保存定义文件
		wfXmlStr = wfXmlStr.replaceAll("\r\n", "");
		myParser.setDefContent(wfXmlStr);

		// 判断是更新还是添加
		if (!paramBean.getAddFlag()) {
			// 重新赋值，因为修改时可能修改了EN_NAME字段
			procCode = paramBean.getStr("EN_NAME") + WfeConstant.PROC_CMPY_PREFIX + Context.getUserBean().getCmpyCode()
					+ WfeConstant.PROC_VERSION_PREFIX + paramBean.getStr("PROC_VERSION");
			myParser.setProcCode(procCode);
			myParser.modify();
		} else {
			paramBean.set("PROC_VERSION", 1);
			paramBean.set("PROC_IS_LATEST", WfeConstant.PROC_IS_LATEST);
			procCode = paramBean.getStr("EN_NAME") + WfeConstant.PROC_CMPY_PREFIX + Context.getUserBean().getCmpyCode()
					+ WfeConstant.PROC_VERSION_PREFIX + paramBean.getStr("PROC_VERSION");
			myParser.setProcCode(procCode);
			myParser.save();
		}

		// 刷新流程 服务对应关系的缓存
		WfServCorrespond.removeFromCache(newServId);
		if (null != oldProcBean) {
			WfServCorrespond.removeFromCache(oldProcBean.getStr("SERV_ID"));
		}

		OutBean rtnBean = new OutBean();
		rtnBean.setOk(Context.getSyMsg("SY_SAVE_OK"));
		rtnBean.set(Constant.RTN_DATA, new Bean().set("PROC_CODE", procCode));
		return rtnBean;
	}
	
	/**
	 * 获取指定procCode的流程的最高版本Bean
	 * 
	 * @param procCodeWithoutVersion
	 *            流程code,不含版本信息
	 * @return Bean 最高版本的流程Bean
	 */
	public Bean getLatestProcDef(String procCodeWithoutVersion) {
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE,
				" AND PROC_CODE LIKE '" + procCodeWithoutVersion + WfeConstant.PROC_VERSION_PREFIX + "%'");
		queryBean.set(Constant.PARAM_ORDER, "PROC_VERSION DESC");
		List<Bean> procDefList = ServDao.finds(ServMgr.SY_WFE_PROC_DEF, queryBean);
		if (procDefList.size() > 0) {
			return procDefList.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * 将流程所有版本的PROC_IS_LATEST状态置为否 PROC_IS_NOT_LATEST
	 * 
	 * @param procCodeWithoutVersion
	 *            流程code,不含版本信息
	 */
	private void updateProcDefToUnLatest(String procCodeWithoutVersion) {
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE,
				" AND PROC_CODE LIKE '" + procCodeWithoutVersion + WfeConstant.PROC_VERSION_PREFIX + "%'");
		ServDao.updates(ServMgr.SY_WFE_PROC_DEF, new Bean().set("PROC_IS_LATEST", WfeConstant.PROC_IS_NOT_LATEST),
				queryBean);
	}
	
	/**
	 * 清除指定服务的流程定义缓存， 在流程定义有变动时调用
	 * 
	 * @param servId
	 *            服务Id
	 */
	private void clearCache(String servId) {
		String procKey = "_WF_MAP";
		ServDefBean servDef = ServUtils.getServDef(servId);
		servDef.remove(procKey);
	}
	    
    /**
     * 导入JsonBean格式的服务定义，如果已经存在，则直接覆盖当前服务定义（删除后导入）
     * @param jsonBean JsonBean格式的服务定义
     * @return 是否成功导入
     */
    private boolean impJsonBean_Dict(Bean jsonBean) {
        ParamBean param = new ParamBean();
		String servId = jsonBean.getStr("DICT_ID");
		// 备份字典
		// 查看是否存在可备份文件
		// 如果已存在文件 重命名备份文件
		File servFile = new File(docPath + ServMgr.SY_SERV_DICT + File.separator + servId + ".json");
		if (servFile.exists()) {
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			String tmpPath = docPath + ServMgr.SY_SERV_DICT + File.separator + servId + ".json" + ".bak" + df.format(new Date());
			File bakFile = new File(tmpPath);
			try {
				FileUtils.copyFile(servFile, bakFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		param.setId(servId).setServId(ServMgr.SY_SERV_DICT).setLinkFlag(true).set("$JSON_FLAG", false); // 不生成文件、指定级联删除
		delete(param);
		param = new ParamBean(jsonBean).setServId(ServMgr.SY_SERV_DICT).setAddFlag(true);
		return save(param).isOk();
    }
}
