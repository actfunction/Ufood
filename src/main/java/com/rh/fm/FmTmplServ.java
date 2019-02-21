package com.rh.fm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.comm.FileStorage;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;
import com.rh.core.util.file.FileHelper;
import com.rh.core.wfe.db.WfProcDefDao;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfServCorrespond;
import com.rh.core.wfe.util.WfeConstant;

public class FmTmplServ extends CommonServ {
	private static final String SERV_JSON_PATH = FileHelper.getJsonPath() + "SY_SERV" + File.separator;
	private static final String WFE_PATH_DEST = FileHelper.getJsonPath() + "SY_WFE_TMPL" + File.separator;
	private static final String FILE_JSON_PREFIX = "TMPL_";
	private static final String FILE_JSON_SUFFIX = "_UUID";

	/** 在添加的时候保存服务json文件为模板 */
	@Override
	protected void beforeSave(ParamBean paramBean) {
		if (paramBean.getAddFlag()) {
			// 获取服务信息
			String servId = paramBean.getStr("TMPL_SERV_ID");
			String servIdNew = FILE_JSON_PREFIX + servId + getSuffix();

			// 复制json到指定路径
			copyServDef(servId, servIdNew, true);

			// 复制流程定义到指定路径
			String wfeProcCode = copyWfeProc(servId);

			// 设置为新的信息
			paramBean.set("TMPL_SERV_ID", servIdNew);
			paramBean.set("TMPL_PROC_CODE", wfeProcCode);
		}
	}

	/** 在修改了模板名称时同步修改服务json的服务名称 */
	@Override
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		if (paramBean.isNotEmpty("TMPL_NAME")) {
			// 得到模板名称
			String tmplName = paramBean.getStr("TMPL_NAME");
			// 读取模板信息获取对应的服务json文件
			Bean procReg = ServDao.find("FM_COMM_TMPL", paramBean.getId());
			if (procReg != null) {
				// 获取服务信息
				String servId = procReg.getStr("TMPL_SERV_ID");
				String servFile = SERV_JSON_PATH + servId + ".json";
				if (!FileHelper.exists(servFile)) {
					throw new RuntimeException("无法找到服务配置文件");
				}

				// 同时修改服务名称及标题
				Bean servBean = JsonUtils.toBean(FileHelper.readFile(servFile));
				servBean.set("SERV_RED_HEAD", "{\"title\":\"" + tmplName + "\"}");
				servBean.set("SERV_NAME", tmplName);

				// 保存配置到文件
				FileHelper.toJsonFile(servBean, servFile);
			}
		}
	}

	/** 导入指定服务为模板,需要传入流程登记id */
	public OutBean addTmpl(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		Bean tmplBean = new Bean();

		// 根据上线登记编码得到上线登记信息
		String applyCode = paramBean.getStr("APPLY_CODE");
		Bean searchBean = new Bean();
		searchBean.set("APPLY_CODE", applyCode);
		Bean procReg = ServDao.find("FM_PROC_ONLINE_REG", searchBean);
		if (procReg == null) {
			outBean.setError("编码无效");
			return outBean;
		}
		// 获取服务信息
		String servId = procReg.getStr("REG_PROC_SERVID");
		String servIdNew = FILE_JSON_PREFIX + servId + getSuffix();

		// 复制json到指定路径
		copyServDef(servId, servIdNew, true);

		// 复制流程定义到指定路径
		String wfeProcCode = copyWfeProc(servId);

		// 保存模板信息
		tmplBean.set("TMPL_NAME", procReg.getStr("REG_PROC_NAME"));
		tmplBean.set("TMPL_REMARK", procReg.getStr("REG_ENDS"));
		tmplBean.set("TMPL_TYPE_ONE", procReg.getStr("REG_PROC_TYPE_ONE"));
		tmplBean.set("TMPL_TYPE_TWO", procReg.getStr("REG_PROC_TYPE_TWO"));
		tmplBean.set("TMPL_ODEPT_LEVEL", procReg.getStr("REG_ODEPT_LEVEL"));
		tmplBean.set("TMPL_PROC_LEVEL", procReg.getStr("REG_PROC_LEVEL"));
		tmplBean.set("TMPL_SERV_ID", servIdNew);
		tmplBean.set("TMPL_PROC_CODE", wfeProcCode);
		ServDao.save("FM_COMM_TMPL", tmplBean);
		return outBean;
	}

	/**
	 * 复制服务定义信息为json文件
	 * @param servId 原服务id
	 * @param servIdNew 新的服务id,当做文件名称保存在SY_SERV下
	 * @param copyParent 是否复制父服务,如果为true则会复制一份
	 */
	private void copyServDef(String servId, String servIdNew, boolean copyParent) {
		ServDefBean servDefBean = new ServDefBean(ServUtils.getServDef(servId));

		// 复制json到指定路径
		try {
			// =====>复制父服务定义
			if (copyParent && servDefBean.isNotEmpty("SERV_PID")) {
				String servPid = servDefBean.getStr("SERV_PID");
				// 为了保证所有相同父服务只保存一份,因此不再加后缀 +getSuffix();
				String servPidNew = FILE_JSON_PREFIX + servPid;
				copyServDef(servPid, servPidNew, copyParent);
				servDefBean.set("SERV_PID", servPidNew);// 修改父服务的引用
			}

			// =====>复制服务定义
			// 目标路径,由于服务信息会优先从这个路径读取json,因此这里保持路径不变
			String servFileDest = SERV_JSON_PATH + servIdNew + ".json";
			FileHelper.toJsonFile(servDefBean, servFileDest);

			// =====>复制卡片js
			if (servDefBean.getInt("SERV_CARD_LOAD") == 1) {
				copyServJs(servId, servIdNew, "card");
			}
			// =====>复制列表js
			if (servDefBean.getInt("SERV_LIST_LOAD") == 1) {
				copyServJs(servId, servIdNew, "list");
			}

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/** 复制卡片、列表js */
	private void copyServJs(String srcServID, String destServID, String type) {
		String srcModule = srcServID.substring(0, srcServID.indexOf("_")).toLowerCase();
		String destModule = destServID.substring(0, destServID.indexOf("_")).toLowerCase();
		String srcPath = Context.appStr(APP.SYSPATH) + srcModule + "/servjs/" + srcServID + "_" + type + ".js";
		String destPath = Context.appStr(APP.SYSPATH) + destModule + "/servjs/" + destServID + "_" + type + ".js";
		File srcFile = new File(srcPath);
		if (srcFile.exists()) {
			File destFile = new File(destPath);
			try {
				FileUtils.copyFile(srcFile, destFile);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}

	/** 复制流程定义实例信息为json文件 */
	private String copyWfeProc(String servId) {
		String wfeProcCode = "";
		try {
			// 查找服务对应的流程
			WfProcDef wfProcDef = WfServCorrespond.getProcDef(servId, new Bean());
			if (wfProcDef != null) {
				wfeProcCode = wfProcDef.getProcCode();
				// 得到流程定义
				Bean queryBean = new Bean();
				queryBean.set(Constant.PARAM_WHERE, "AND PROC_CODE = '" + wfeProcCode + "' ");
				queryBean.set(Constant.PARAM_ORDER, "EN_NAME DESC, PROC_VERSION DESC");
				Bean procBean = ServDao.find(ServMgr.SY_WFE_PROC_DEF, queryBean);
				if (procBean != null) {
					wfeProcCode = FILE_JSON_PREFIX + wfeProcCode + getSuffix();// 流程定义code增加前缀
					String wfeFileDest = WFE_PATH_DEST + wfeProcCode + ".json";// json文件保存路径
					String procBeanStr = JsonUtils.toJson(procBean, true);// json对象转为字符串
					FileStorage.saveFile(IOUtils.toInputStream(procBeanStr, Constant.ENCODING), wfeFileDest);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return wfeProcCode;
	}

	/** 根据流程编码读取流程配置文件 */
	public OutBean getFmProcXml(ParamBean paramBean) {
		OutBean result = new OutBean();
		String procCode = paramBean.getStr("procCode");
		// 读取json文件
		String wfeFile = WFE_PATH_DEST + procCode + ".json";
		if (!FileHelper.exists(wfeFile)) {
			return result;
		}
		// 读取文件并转为bean对象
		Bean servBean = JsonUtils.toBean(FileHelper.readFile(wfeFile));
		result.setData(servBean.getStr("PROC_XML"));
		return result;
	}

	/** 使用模板,会导入服务、流程定义 */
	public OutBean useTmpl(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String servId = paramBean.getStr("servId");
		String procCode = paramBean.getStr("procCode");
		String newServId = paramBean.getStr("newServId").toUpperCase();

		// 验证输入的服务编码是否重复
		Bean servBean = ServDao.find(ServMgr.SY_SERV, newServId);
		if (servBean != null) {
			throw new RuntimeException("服务编码重复");
		}
		// 流程的主键由当前用户及服务编码决定,这里也需要验证
		String newProcCode = newServId + WfeConstant.PROC_CMPY_PREFIX;
		newProcCode += Context.getUserBean().getCmpyCode() + WfeConstant.PROC_VERSION_PREFIX + "1";
		Bean wfeBean = ServDao.find(ServMgr.SY_WFE_PROC_DEF, newProcCode);
		if (wfeBean != null) {
			throw new RuntimeException("流程绑定失败,编码重复");
		}

		// 由于流程需要关联服务,导入流程时需要读取服务定义,因此服务与流程的事务必须是分开的
		// 导入服务定义
		impServ(servId, newServId);

		// 导入流程定义
		impWfe(procCode, newServId);
		return outBean;
	}

	/** 导入服务定义 */
	private void impServ(String servId, String newServId) {
		String servFile = SERV_JSON_PATH + servId + ".json";
		if (!FileHelper.exists(servFile)) {
			throw new RuntimeException("无法找到服务配置文件");
		}

		Bean servBean = JsonUtils.toBean(FileHelper.readFile(servFile));
		ParamBean param = new ParamBean(servBean).setServId(ServMgr.SY_SERV).setAddFlag(true);
		// 需要重新设置服务主键
		param.setId(newServId);
		param.set("SERV_ID", newServId);
		param.set("S_USER", Context.getUserBean().getCode());
		param.set("SERV_SQL_WHERE", "");
		// 重新设置关联的父服务
		if (param.isNotEmpty("SERV_PID")) { // 存在父服务定义
			String servPid = param.getStr("SERV_PID");
			// 去掉服务的前后缀,改为服务实际的编码
			if (servPid.startsWith(FILE_JSON_PREFIX)) {
				servPid = servPid.substring(FILE_JSON_PREFIX.length());
			}
			if (servPid.indexOf(FILE_JSON_SUFFIX) > 0) {
				servPid = servPid.substring(0, servPid.indexOf(FILE_JSON_SUFFIX));
			}
			param.set("SERV_PID", servPid);

			// 如果数据库中没有父服务的定义,则导入父服务
			Bean fBean = ServDao.find(ServMgr.SY_SERV, servPid);
			if (fBean == null) {
				impServ(param.getStr("SERV_PID"), servPid);
			}
		}
		// 重新设置字段关联服务ID
		List<Bean> dataList = param.getList(ServUtils.TABLE_SERV_ITEM);
		for (Bean bean : dataList) {
			bean.setId("");
			bean.set("ITEM_ID", "");
			bean.set("SERV_ID", newServId);
			// 重写流程类别
			if ("APPLY_CATALOG".equals(bean.getStr("ITEM_CODE"))) {
				bean.set("ITEM_INPUT_DEFAULT", newServId);
			}
		}
		// 重新设置act关联服务ID
		dataList = param.getList(ServUtils.TABLE_SERV_ACT);
		for (Bean data : dataList) {
			data.setId("");
			data.set("ACT_ID", "");
			data.set("SERV_ID", newServId);
		}
		// 重新设置where规则关联服务ID
		dataList = param.getList(ServUtils.TABLE_SERV_WHERE);
		for (Bean data : dataList) {
			data.setId("");
			data.set("WHERE_ID", "");
			data.set("SERV_ID", newServId);
		}
		// 重新设置link关联服务ID
		dataList = param.getList(ServUtils.TABLE_SERV_LINK);
		for (Bean data : dataList) {
			data.setId("");
			data.set("LINK_ID", "");
			data.set("SERV_ID", newServId);
		}
		// 重新设置常用查询关联服务ID
		dataList = param.getList(ServUtils.TABLE_SERV_QUERY);
		for (Bean data : dataList) {
			data.setId("");
			data.set("QUERY_ID", "");
			data.set("SERV_ID", newServId);
		}
		ServMgr.act(ServMgr.SY_SERV, "save", param);// 导入操作

		// =====>复制卡片js
		if (servBean.getInt("SERV_CARD_LOAD") == 1) {
			copyServJs(servId, newServId, "card");
		}
		// =====>复制列表js
		if (servBean.getInt("SERV_LIST_LOAD") == 1) {
			copyServJs(servId, newServId, "list");
		}
	}

	/** 导入流程定义 */
	private void impWfe(String procCode, String newServId) {
		if (procCode == null || procCode.length() == 0) {
			return;
		}
		// 读取流程定义配置
		String servFile = WFE_PATH_DEST + procCode + ".json";
		if (!FileHelper.exists(servFile)) {
			throw new RuntimeException("无法找到流程配置文件");
		}
		// 读取json并转为bean
		Bean procDefBean = JsonUtils.toBean(FileHelper.readFile(servFile));
		ParamBean param = new ParamBean(procDefBean);
		param.set("xmlStr", procDefBean.getStr("PROC_XML"));

		// 需要重新设置服务主键等信息
		String procCodeNew = newServId + WfeConstant.PROC_CMPY_PREFIX;
		procCodeNew += Context.getUserBean().getCmpyCode() + WfeConstant.PROC_VERSION_PREFIX;
		procCodeNew += param.getStr("PROC_VERSION");
		param.set("EN_NAME", newServId);
		param.set("SERV_ID", newServId);
		param.set("PROC_CODE", procCodeNew);
		param.set(Bean.KEY_ID, "");
		param.setAddFlag(true);

		// 导入操作
		Context.setThread(Context.THREAD.SERVID, WfProcDefDao.SY_WFE_PROC_DEF_SERV);
		ServMgr.act(ServMgr.SY_WFE_PROC_DEF, "saveWfAsNewVersion", param);
	}

	/** 删除信息之后删除服务文件、列表卡片js、流程配置文件 */
	public void afterDelete(ParamBean paramBean, OutBean outBean) {
		if (!outBean.getMsg().startsWith(Constant.RTN_MSG_OK)) {
			return;
		}
		List<Bean> deleteBeans = paramBean.getList("_DELETE_DATAS_");
		for (Bean deleteBean : deleteBeans) {
			String servId = deleteBean.getStr("TMPL_SERV_ID");
			String procCode = deleteBean.getStr("TMPL_PROC_CODE");

			// 删除服务配置文件
			deleteServJsonFile(servId);

			// 删除流程定义文件
			String wfeFileSrc = WFE_PATH_DEST + procCode + ".json";// json文件保存路径
			FileHelper.delete(wfeFileSrc);
		}
	}

	/** 删除服务配置文件 */
	private void deleteServJsonFile(String servId) {
		// 删除服务配置文件
		String servFileSrc = SERV_JSON_PATH + servId + ".json";
		if (!FileHelper.exists(servFileSrc)) {
			return;
		}

		Bean servBean = JsonUtils.toBean(FileHelper.readFile(servFileSrc));
		// 删除父服务文件
		/*if (servBean.isNotEmpty("SERV_PID")) { // 存在父服务定义
			String servPid = servBean.getStr("SERV_PID");
			// 如果是以TMPL开头则删除服务文件
			if (servPid.startsWith(FILE_JSON_PREFIX)) {
				deleteServJsonFile(servPid);
			}
		}*/

		// 删除服务文件
		FileHelper.delete(servFileSrc);

		// 删除js文件
		String jsFilePrefix = servId.substring(0, servId.indexOf("_")).toLowerCase();
		String jsFileSrc1 = Context.appStr(APP.SYSPATH) + jsFilePrefix + "/servjs/" + servId + "_card.js";
		String jsFileSrc2 = Context.appStr(APP.SYSPATH) + jsFilePrefix + "/servjs/" + servId + "_list.js";
		FileHelper.delete(jsFileSrc1);
		FileHelper.delete(jsFileSrc2);
	}

	/** 新生成后缀,用来拼到服务json后 */
	private String getSuffix() {
		return FILE_JSON_SUFFIX + Lang.getUUID();
	}

	/** 编辑模板,会导出模板并打开服务编辑页面 */
	public OutBean updateTmpl(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String tmplServId = paramBean.getStr("tmplServId");// 模板服务编码
		String realServId = tmplServId;// 实际服务编码
		// 去掉服务编码的前后缀
		if (realServId.indexOf(FILE_JSON_PREFIX) == 0) {
			realServId = realServId.substring(FILE_JSON_PREFIX.length());
		}
		if (realServId.indexOf(FILE_JSON_SUFFIX) > 0) {
			realServId = realServId.substring(0, realServId.indexOf(FILE_JSON_SUFFIX));
		}
		// 如果已有服务则进行导入操作
		Bean servBean = ServDao.find(ServMgr.SY_SERV, realServId);
		if (servBean == null) {
			impServ(tmplServId, realServId);// 导入服务定义
		}

		outBean.set("tmplServId", tmplServId);// 返回服务编码
		outBean.set("realServId", realServId);// 返回新服务编码
		return outBean;
	}

	/** 恢复模板，会覆盖指定的模板文件 */
	public OutBean revokeTmpl(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String tmplServId = paramBean.getStr("tmplServId");// 模板服务编码
		String realServId = paramBean.getStr("realServId");// 实际服务编码
		if (Strings.isBlank(tmplServId)) {
			tmplServId = FILE_JSON_PREFIX + realServId + FILE_JSON_SUFFIX;
			Bean queryBean = new Bean();
			queryBean.set(Constant.PARAM_SELECT, " ID,TMPL_SERV_ID ");// 查询字段
			queryBean.set(Constant.PARAM_WHERE, " and TMPL_SERV_ID like '" + tmplServId + "%' ");// 查找有指定模板文件的数据
			List<Bean> list = ServDao.finds("FM_COMM_TMPL", queryBean);
			if (list == null || list.size() != 0) {
				throw new RuntimeException("没有找到指定的模板文件");
			}
			Bean bean = list.get(0);
			tmplServId = bean.getStr("TMPL_SERV_ID");// 得到实际的模板文件名称
		}
		// 判断模板文件名称是否正确
		String tmplFileDest = SERV_JSON_PATH + tmplServId + ".json";
		if (!FileHelper.exists(tmplFileDest)) {
			throw new RuntimeException("模板文件名称错误");
		}
		// 复制服务配置
		copyServDef(realServId, tmplServId, false);

		outBean.set("tmplServId", tmplServId);// 返回服务编码
		outBean.set("realServId", realServId);// 返回新服务编码
		return outBean;
	}
}
