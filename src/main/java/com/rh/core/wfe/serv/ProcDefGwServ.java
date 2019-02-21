package com.rh.core.wfe.serv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.FileMgr;
import com.rh.core.comm.FileStorage;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Dom4JHelper;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.RequestUtils;
import com.rh.core.wfe.condition.VarResource;
import com.rh.core.wfe.db.WfLineDao;
import com.rh.core.wfe.db.WfNodeDefDao;
import com.rh.core.wfe.db.WfProcDefDao;
import com.rh.core.wfe.db.WfProcInstDao;
import com.rh.core.wfe.def.WFParser;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfProcDefManager;
import com.rh.core.wfe.def.WfServCorrespond;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 流程定义 服务类
 * 
 */
public class ProcDefGwServ extends CommonServ {

	@Override
	protected void beforeSave(ParamBean paramBean) {
		// 判断是更新还是添加
		if (paramBean.getBoolean("_ADD_")) {
			paramBean.set("PROC_VERSION", 1);
			paramBean.set("PROC_IS_LATEST", WfeConstant.PROC_IS_LATEST);
			String procCode = paramBean.getStr("EN_NAME") + WfeConstant.PROC_CMPY_PREFIX
					+ Context.getUserBean().getCmpyCode() + WfeConstant.PROC_VERSION_PREFIX
					+ paramBean.getStr("PROC_VERSION");
			paramBean.set("PROC_CODE", procCode);
			paramBean.setId(procCode);
		}
	}

	@Override
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		// 清除流程缓存
		WfProcDefManager.removeFromCache(paramBean.getId());

		
	}

	/**
	 * 删除指定流程的所有版本，先删除节点定义，连线定义
	 * 
	 * @param paramBean
	 *            参数
	 * @return 删除状态
	 */
	public OutBean deleteProcDef(ParamBean paramBean) {
		String procDefIds = paramBean.getStr("procIds");
		String[] procDefIdArray = procDefIds.split(",");
		// 删除该流程的所有版本
		for (int i = 0; i < procDefIdArray.length; i++) {
			String procCode = procDefIdArray[i];
			String procCodeWithoutVersion = procCode.substring(0,
					procCode.lastIndexOf(WfeConstant.PROC_VERSION_PREFIX));
			int version = Integer
					.parseInt(procCode.substring(procCode.lastIndexOf(WfeConstant.PROC_VERSION_PREFIX) + 1));
			for (int j = 1; j <= version; j++) {
				deleteProcDef(procCodeWithoutVersion + WfeConstant.PROC_VERSION_PREFIX + j);
			}
		}

		OutBean rtnBean = new OutBean();
		rtnBean.set("rtnstr", "success");

		return rtnBean;
	}

	/**
	 * 删除指定版本的流程定义信息，先删除节点定义，连线定义 将下一版本的置为最新版本
	 * 
	 * @param paramBean
	 *            参数
	 * @return 删除状态
	 */
	public OutBean deleteProcDefOfSpecVersion(ParamBean paramBean) {
		String procDefIds = paramBean.getStr("procIds");
		String[] procDefIdArray = procDefIds.split(",");
		// 删除以procDefId为
		for (int i = 0; i < procDefIdArray.length; i++) {
			// 删除
			String procCode = procDefIdArray[i];
			String procCodeWithoutVersion = procCode.substring(0,
					procCode.lastIndexOf(WfeConstant.PROC_VERSION_PREFIX));
			Bean oldProcBean = WfProcDefDao.getWfProcBeanByProcCode(procCode);
			// 删除流程
			deleteProcDef(procCode);
			// 重新设置流程的最新版本
			if (oldProcBean.getInt("PROC_IS_LATEST") == WfeConstant.PROC_IS_LATEST) {
				setProcDefLatest(procCodeWithoutVersion);
			}
		}

		OutBean rtnBean = new OutBean();
		rtnBean.set("rtnstr", "success");

		return rtnBean;
	}

	/**
	 * 删除指定的procCode的流程定义，先删除节点定义，连线定义
	 * 
	 * @param procCode
	 *            流程定义主键
	 */
	private void deleteProcDef(String procCode) {

		// 删除缓存中的定义
		Bean oldProcBean = WfProcDefDao.getWfProcBeanByProcCode(procCode);

		if (null != oldProcBean) {
			String oldServId = oldProcBean.getStr("SERV_ID");
			ParamBean param = new ParamBean(ServMgr.SY_SERV, "clearCache");
			param.setId(oldServId);
			ServMgr.act(param);

			// 删除服务 流程对应的 缓存
			WfServCorrespond.removeFromCache(oldProcBean.getStr("SERV_ID"));

			// 删除节点
			WfNodeDefDao.deleteNodeDefByProcCode(procCode);

			// 删除连线
			WfLineDao.deleteLineDefByProcCode(procCode);

			// 删除流程
			WfProcDefDao.delWfProcDefBeanByProcCode(procCode);
		}
	}

	/**
	 * 将流程定义导出<br>
	 * 导出的格式为：zip包，里面为各个服务定义的json文件<br>
	 * 
	 * @param paramBean
	 *            参数，用procIds表示要导出的流程的proc_code
	 * @return Bean
	 */
	public OutBean export(ParamBean paramBean) {
		OutBean resultBean = new OutBean();

		String procDefIds = paramBean.getStr("procIds");
		if (procDefIds.indexOf(Constant.SEPARATOR) > -1) {
			procDefIds = procDefIds.replaceAll(Constant.SEPARATOR, "'" + Constant.SEPARATOR + "'");
		}
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE, "AND PROC_CODE IN ('" + procDefIds + "')");
		queryBean.set(Constant.PARAM_ORDER, "EN_NAME DESC, PROC_VERSION DESC");
		List<Bean> procBeanList = ServDao.finds(ServMgr.SY_WFE_PROC_DEF, queryBean);

		HttpServletRequest request = Context.getRequest();
		HttpServletResponse response = Context.getResponse();
		response.setContentType("application/x-download");
		RequestUtils.setDownFileName(request, response, ServMgr.SY_WFE_PROC_DEF + ".zip");
		ZipOutputStream zipOut = null;
		try {
			zipOut = new ZipOutputStream(response.getOutputStream());
			for (Bean procBean : procBeanList) {
				zipOut.putNextEntry(new ZipEntry(procBean.getId() + ".json"));
				IOUtils.write(JsonUtils.toJson(procBean, true), zipOut, Constant.ENCODING);
				zipOut.closeEntry();
			}
		} catch (Exception e) {
			resultBean.setError();
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
				zipOut = null;
			}
		}

		return resultBean;
	}

	/**
	 * 导入流程定义 如果系统中已经有此流程定义，则将导入的流程定义存为新版本
	 * 
	 * @param paramBean
	 *            要导入的文件的fileId
	 * @return Bean
	 */
	public OutBean importProcDef(ParamBean paramBean) {
		OutBean resultBean = new OutBean();
		int count = 0; // 导入的流程数量
		String fileId = paramBean.getStr("fileId");
		Bean fileBean = FileMgr.getFile(fileId);
		if (fileBean != null) {
			ZipInputStream zipIn = null;
			InputStream in = null;
			try {
				if (fileBean.getStr("FILE_MTYPE").equals("application/zip")) {
					zipIn = new ZipInputStream(FileMgr.download(fileBean));
					while (zipIn.getNextEntry() != null) {
						in = new BufferedInputStream(zipIn);
						impJsonBean(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)));
						count++;
						zipIn.closeEntry();
					}
				} else {
					in = FileMgr.download(fileBean);
					impJsonBean(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)));
					count++;
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
		if (count > 0) {
			resultBean.setOk(count + "个流程导入成功！");
			FileMgr.deleteFile(fileBean);
		} else {
			resultBean.setError();
		}

		return resultBean;
	}

	/**
	 * 导入为流程定义
	 * 
	 * @param procDefBean
	 *            流程定义Bean
	 */
	private void impJsonBean(Bean procDefBean) {
		ParamBean param = new ParamBean(procDefBean);
		param.setServId(ServMgr.SY_WFE_PROC_DEF).set("xmlStr", procDefBean.getStr("PROC_XML"));
		saveWfAsNewVersion(param);
	}

	/**
	 * 根据serv_ID 获取服务类别
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 文件类型串
	 */
	public OutBean getServFileType(ParamBean paramBean) {
		String servId = paramBean.getStr("SERV_ID");

		Bean servDefBean = ServUtils.getServDef(servId);

		OutBean rtnBean = new OutBean();

		rtnBean.set("rtnStr", servDefBean.getStr("SERV_FILE_CAT"));

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
	 * 重新设置流程的最新版本
	 * 
	 * @param procCodeWithoutVersion
	 *            流程code,不含版本信息
	 */
	private void setProcDefLatest(String procCodeWithoutVersion) {
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE,
				" AND PROC_CODE LIKE '" + procCodeWithoutVersion + WfeConstant.PROC_VERSION_PREFIX + "%'");
		queryBean.set(Constant.PARAM_ORDER, "PROC_VERSION DESC");
		List<Bean> procDefs = ServDao.finds(ServMgr.SY_WFE_PROC_DEF, queryBean);
		if (procDefs.size() > 0) {
			String procCodeTobeLatestVersion = procDefs.get(0).getStr("PROC_CODE");
			ServDao.update(ServMgr.SY_WFE_PROC_DEF,
					new Bean().setId(procCodeTobeLatestVersion).set("PROC_IS_LATEST", WfeConstant.PROC_IS_LATEST));
		}
	}

	/**
	 * 将工作流定义的json转成xml并保存
	 * 
	 * @param paramBean
	 *            paramBean
	 * @return OutBean
	 */
	public OutBean parseJsonToXmlAndSave(ParamBean paramBean) {
		OutBean out = new OutBean();
		String jsonStr = paramBean.getStr("jsonStr");
		ParamBean wfeBean = new ParamBean(ServDao.find(ServMgr.SY_WFE_PROC_DEF, paramBean.getStr("PROC_CODE")));
		wfeBean.set("xmlStr", parseJsonToXml(jsonStr, wfeBean));
		out = saveWf(wfeBean);
		return out;
	}

	/**
	 * 下载工作流定义文件
	 * 
	 * @param paramBean
	 *            paramBean
	 * @return OutBean
	 */
	public OutBean downLoadWfeJsonFile(ParamBean paramBean) {
		OutBean result = new OutBean();

		String jsonStr = paramBean.getStr("jsonStr");
		String webinfoDoc = BaseContext.appStr(APP.WEBINF_DOC);
		File path = new File(webinfoDoc + File.separator + "wfeJson");
		if (!path.exists()) {
			path.mkdirs();
		}
		String fileName = path.getPath() + File.separator + paramBean.getStr("PROC_CODE") + ".json";

		result.set("wfeJsonName", paramBean.getStr("PROC_CODE") + ".json");

		try {
			FileStorage.saveFile(IOUtils.toInputStream(jsonStr, Constant.ENCODING), fileName);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * 下载文件
	 * 
	 * @param paramBean
	 *            paramBean
	 * @return OutBean
	 */
	public OutBean downloadFile(ParamBean paramBean) {
		OutBean result = new OutBean();
		String fileName = paramBean.getStr("wfeJsonName");
		InputStream is = null;
		try {
			String webinfoDoc = BaseContext.appStr(APP.WEBINF_DOC);
			File path = new File(webinfoDoc + File.separator + "wfeJson");
			is = FileStorage.getInputStream(path.getPath() + File.separator + fileName);
			String downFileType = "attachment;";
			Context.getResponse().setHeader("content-disposition", "attachment;filename=" + fileName);
			Context.getResponse().setContentType(downFileType);
			OutputStream out = Context.getResponse().getOutputStream();
			IOUtils.copyLarge(is, out);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(out);
			out.flush();
			FileStorage.deleteFile(path.getPath() + File.separator + fileName);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				IOUtils.closeQuietly(is);
				Context.getResponse().flushBuffer();
			} catch (Exception e) {

			}
		}
		return result;
	}

	/**
	 * 上传工作流定义文件
	 * 
	 * @param paramBean
	 *            paramBean
	 * @return OutBean
	 */
	public OutBean uploadWfeJsonFile(ParamBean paramBean) {
		OutBean out = new OutBean();

		String fileId = paramBean.getStr("fileId");
		Bean fileBean = FileMgr.getFile(fileId.replaceAll(",", ""));
		InputStream is = null;
		try {
			is = FileMgr.download(fileBean);
			String jsonStr = IOUtils.toString(is, Constant.ENCODING);
			out.set("jsonStr", jsonStr);
			FileMgr.deleteFile(fileBean);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (is != null) {
				IOUtils.closeQuietly(is);
			}
		}
		return out;
	}

	/**
	 * json转成xml
	 * 
	 * @param jsonStr
	 *            jsonStr
	 * @return String
	 */
	private String parseJsonToXml(String jsonStr, ParamBean wfeBean) {
		try {
			Bean json = JsonUtils.toBean(jsonStr);
			Document document = DocumentHelper.createDocument();
			document.setXMLEncoding("UTF-8");
			Element rootElement = document.addElement("ADDFLOW");
			rootElement.addAttribute("processType", "BPEL");
			Element jsonDesign = rootElement.addElement("jsonDesign");
			jsonDesign.setText(jsonStr);
			addNodes(rootElement, json, wfeBean);
			addLines(rootElement, json, wfeBean);
			return Dom4JHelper.doc2String(document);
		} catch (Exception e) {
			throw new TipException(e.getMessage());
		}
	}

	/**
	 * 添加节点
	 * 
	 * @param rootElement
	 * @param json
	 */
	@SuppressWarnings("deprecation")
	private void addNodes(Element rootElement, Bean json, ParamBean wfeBean) {
		@SuppressWarnings("unchecked")
		List<Bean> nodes = (List<Bean>) json.get("nodeDataArray");
		for (Bean node : nodes) {
			if (node.getStr("category").equals("LinkLabel")) {
				continue;
			}
			Element nodeElement = rootElement.addElement("NODEDEF");
			nodeElement.setAttributeValue("id", node.getStr("key"));
			if (node.isEmpty("nodeDef") || node.getBean("nodeDef").getStr("SERV_ID").length() <= 0) {
				Bean nodeDefBean = new Bean();
				nodeDefBean.set("SERV_ID", wfeBean.getStr("SERV_ID")).set("SERV_PID", wfeBean.getStr("SERV_PID"))
				.set("NODE_CODE", node.getStr("key")).set("NODE_NAME", node.getStr("text"))
				.set("NODE_TYPE", (node.getStr("category").equals("start") ? "1" : "2")).set("NODE_IF_AUTOEND", 1);
				nodeElement.setText(JsonUtils.mapsToJson(nodeDefBean));
			} else {
				Bean nodeDefBean = node.getBean("nodeDef");
				nodeDefBean.set("NODE_CODE", node.getStr("key"));
				nodeElement.setText(JsonUtils.mapsToJson(nodeDefBean));
			}
		}
	}

	/**
	 * 添加线
	 * 
	 * @param rootElement
	 * @param json
	 */
	@SuppressWarnings("deprecation")
	private void addLines(Element rootElement, Bean json, ParamBean wfeBean) {
		@SuppressWarnings("unchecked")
		List<Bean> lines = (List<Bean>) json.get("linkDataArray");
		for (Bean line : lines) {
			Element lineElement = rootElement.addElement("LINEDEF");
			@SuppressWarnings("unchecked")
			String key = ((List<String>) line.get("labelKeys")).get(0);
			lineElement.setAttributeValue("id", key);
			if (line.isEmpty("lineDef")) {
				Bean lineBean = new Bean();
				lineBean.set("LINE_CODE", key).set("LINE_NAME", line.getStr("text")).set("LINE_IF_RETURN", 1)
				.set("SRC_NODE_CODE", line.getStr("from")).set("TAR_NODE_CODE", line.getStr("to"));
				lineElement.setText(JsonUtils.mapsToJson(lineBean));
			} else {
				Bean lineBean = line.getBean("lineDef");
				if (lineBean.getStr("LINE_CODE").indexOf("[") >= 0 || lineBean.getStr("LINE_CODE").indexOf("]") >= 0) {
					String lineCode = lineBean.getStr("LINE_CODE").replace("[", "").replace("]", "");
					lineBean.set("LINE_CODE", lineCode);
				}
				lineElement.setText(JsonUtils.mapsToJson(lineBean));
			}
		}
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
		if (null != oldProcBean) {
			String oldServId = oldProcBean.getStr("SERV_ID");
			clearCache(oldServId);
		}
		clearCache(newServId);

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

	@Override
	public OutBean byid(ParamBean paramBean) {
		OutBean outBean = super.byid(paramBean);

		// if (outBean.isEmpty("PROC_XML")) { // 如果是新起的流程，那么默认装载一个流程模板。
		// try {
		// // 默认流程模板的位置为："/sy/wfe/workflow_tmpl.xml"
		// StringBuilder filePath = new StringBuilder();
		// filePath.append(Context.appStr(APP.SYSPATH));
		// filePath.append("sy").append(Constant.PATH_SEPARATOR);
		// filePath.append("wfe").append(Constant.PATH_SEPARATOR);
		// filePath.append("workflow_tmpl.xml");
		// File tmplXml = new File(filePath.toString());
		// String xml = FileUtils.readFileToString(tmplXml, "UTF-8");
		// outBean.set("PROC_XML", xml);
		// } catch (Exception e) {
		// log.error(e.getMessage(), e);
		// }
		// } else {
		// WfProcDef procDefBean =
		// WfProcDefManager.getWorkflowDef(outBean.getStr("PROC_CODE"));
		// outBean.set("BIND_TITLE", procDefBean.getProcTitle());
		// }

		return outBean;
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
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 流程定义的节点列表
	 */
	public OutBean reteieveNodeDefList(ParamBean paramBean) {
		String piId = paramBean.getStr("S_WF_INST");

		Bean procInst = WfProcInstDao.findProcInstById(piId);

		WfProcDef procDef = WfProcDefManager.getWorkflowDef(procInst.getStr("PROC_CODE"));
		List<Bean> nodeDefList = procDef.getAllNodeDef();
		for (Bean node : nodeDefList) {
			node.set("ID", node.getStr("NODE_CODE"));
			node.set("NAME", node.getStr("NODE_NAME"));
		}

		BeanUtils.sort(nodeDefList, "NODE_NAME");

		OutBean out = new OutBean();
		out.put(Constant.RTN_DATA, nodeDefList);

		return out;
	}

	/**
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 返回前台
	 */
	public OutBean getLineCondVars(ParamBean paramBean) {
		OutBean rtnBean = new OutBean();

		VarResource varRes = new VarResource();

		List<Bean> list = new ArrayList<Bean>();
		// 流程变量
		list.addAll(varRes.getParamList());
		// 服务变量
		list.add(varRes.getServParams(paramBean.getStr("SERV_ID")));

		// 添加树的标题
		Bean rootBean = new Bean();
		rootBean.set("ID", "lineConTitle");
		rootBean.set("NAME", "条件流变量");
		rootBean.set("NODETYPE", "DIR");
		rootBean.set("CHILD", list);

		String treeDataStr = JsonUtils.toJson(rootBean);

		String operatorList = varRes.getOperatorList();

		rtnBean.set("treeData", "[" + treeDataStr + "]"); // 树的数据
		rtnBean.set("operatorList", operatorList); // 操作集合

		return rtnBean;
	}

}
