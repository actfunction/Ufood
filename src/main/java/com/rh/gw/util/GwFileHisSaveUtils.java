package com.rh.gw.util;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.RequestUtils;

public class GwFileHisSaveUtils {
	
	private static Log log = LogFactory.getLog(GwFileHisSaveUtils.class);
	private static void hisFileSave(Bean fileBean, List<Bean> hisList, String niid,
			String type, InputStream is) {
		// 定义计数器用来判断是否是是否要覆盖文件
		int count = 0;
		for (Bean hisBean : hisList) {
			// 如果有这个文件则替换
			if (type.equals(hisBean.getStr("HISTFILE_QINGGAO_TYPE"))
					&& niid.equals(hisBean.getStr("WF_NI_ID"))) {
				// 如果有相同的值则 size的Num - 1;
				// 复制文件
				String path = fileBean.getStr("FILE_PATH");
				// 将文件路径转换成为绝对路径
				String zhengWenFilePath = FileMgr.getAbsolutePath(path);
				// 将这个文件复制到一个指定的新路径下
				String newHisFilePath = "";
				// 将文件名进行替换 成hisFileId
				String[] filePaths = zhengWenFilePath.split("/");
				for (int i = 0; i < filePaths.length; i++) {
					if (i == filePaths.length - 1) {
						newHisFilePath += hisBean.get("HISFILE_ID");
					} else {
						newHisFilePath += filePaths[i] + "/";
					}
				}
				FileMgr.copyFile(zhengWenFilePath, newHisFilePath);
				// int histVers = ServDao.count("OA_GW_COMM_FILE_HIS", new
				// Bean().set("SERV_ID",
				// fileBean.getStr("SERV_ID")).set("DATA_ID",
				// fileBean.getStr("DATA_ID"))) + 1;//
				// hisBean.set("HISFILE_VERSION", histVers);
				hisBean.set("FILE_SIZE", fileBean.getStr("FILE_SIZE"));
				hisBean.set("DIS_NAME", fileBean.getStr("DIS_NAME"));
				hisBean.set("FILE_MTYPE", fileBean.getStr("FILE_MTYPE"));
				hisBean.set("FILE_NAME", fileBean.getStr("FILE_NAME"));
				hisBean.set("FILE_CAT", fileBean.getStr("FILE_CAT"));
				hisBean.set("DATA_TYPE", fileBean.getStr("DATA_TYPE"));
				hisBean.set("ITEM_CODE", fileBean.getStr("ITEM_CODE"));
				hisBean.set("FILE_CHECKSUM", fileBean.getStr("FILE_CHECKSUM"));
				hisBean.set("FILE_HIST_COUNT",
						fileBean.getInt("FILE_HIST_COUNT") + 1);
				// 获得当前节点 讲节点放入到数据中
				hisBean.set("WF_NI_ID", niid);
				hisBean.set("S_MTIME", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
				// 在这里进行向数据库经插入清稿文档
				hisBean.set("HISTFILE_QINGGAO_TYPE", type);
				ServDao.save("OA_GW_COMM_FILE_HIS", hisBean);
				count++;
				log.debug("覆盖了一个清稿的历史版本，服务编码为：" + hisBean.getStr("SERV_ID")
						+ ",数据主键为：" + hisBean.getStr("DATA_ID"));
			}
		}
		// 本节点中第一个清稿文件
		if (count == 0) {
			// 如果没有相同的值 则创建这个清稿文件
			fileBean.setId("");
			// 获得版本记录 将版本记录+1 返回 版本号
			int histVers = 0;
			/*
			 * if(0==hisList.size()){ histVers = 1; } else{
			 */
			histVers = ServDao.count(
					"OA_GW_COMM_FILE_HIS",
					new Bean().set("SERV_ID", fileBean.getStr("SERV_ID")).set(
							"DATA_ID", fileBean.getStr("DATA_ID"))) + 1;//
			// }
			fileBean.set("HISFILE_VERSION", histVers);
			// 历史文件的id
			String hisFileId = "OAHIST_" + Lang.getUUID() + "."
					+ FileMgr.getSuffix(fileBean.getStr("FILE_ID"));
			fileBean.set("HISFILE_ID", hisFileId);
			// 在这里获得数据库中存储文件的路径
			String path = fileBean.getStr("FILE_PATH");
			// 将文件路径转换成为绝对路径
			String zhengWenFilePath = FileMgr.getAbsolutePath(path);
			// 将这个文件复制到一个指定的新路径下
			String newHisFilePath = "";
			// 将文件名进行替换 成hisFileId
			String[] filePaths = zhengWenFilePath.split("/");
			for (int i = 0; i < filePaths.length; i++) {
				if (i == filePaths.length - 1) {
					newHisFilePath += hisFileId;
				} else {
					newHisFilePath += filePaths[i] + "/";
				}
			}
			FileMgr.copyFile(zhengWenFilePath, newHisFilePath);
			// 将新路径转换成数据库中存储的路径
			newHisFilePath = "";
			String[] sqlPath = path.split("/");
			for (int i = 0; i < sqlPath.length; i++) {
				if (i == sqlPath.length - 1) {
					newHisFilePath += hisFileId;
				} else {
					newHisFilePath += sqlPath[i] + "/";
				}
			}
			// 将路径上传到数据库
			fileBean.set("FILE_PATH", newHisFilePath);
			// 在这里进行向数据库经插入清稿文档
			fileBean.set("HISTFILE_QINGGAO_TYPE", type);
			// 获得当前节点 讲节点放入到数据中
			fileBean.set("WF_NI_ID", niid);
			fileBean.set("S_MTIME", new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
			ServDao.save("OA_GW_COMM_FILE_HIS", fileBean);
			log.debug("新增一个正文的历史版本，服务编码为：" + fileBean.getStr("SERV_ID")
					+ ",数据主键为：" + fileBean.getStr("DATA_ID"));
		}
	}
	private static void qingGaoFileSave(Bean fileBean, List<Bean> hisList,
			String niid, String type, InputStream is) {
		// 清稿文件有则覆盖

		// 定义计数器用来判断是否是是否要覆盖文件
		int count = 0;
		for (Bean hisBean : hisList) {
			// 如果有这个文件则替换
			if (type.equals(hisBean.getStr("HISTFILE_QINGGAO_TYPE"))
					&& niid.equals(hisBean.getStr("WF_NI_ID"))) {
				// 如果有相同的值则 size的Num - 1;
				// 复制文件
				String path = hisBean.getStr("FILE_PATH");
				// 将文件路径转换成为绝对路径
				String zhengWenFilePath = FileMgr.getAbsolutePath(path);
				// 将这个文件复制到一个指定的新路径下
				String newHisFilePath = "";
				// 将文件名进行替换 成hisFileId
				String[] filePaths = zhengWenFilePath.split("/");
				for (int i = 0; i < filePaths.length; i++) {
					if (i == filePaths.length - 1) {
						newHisFilePath += hisBean.get("HISFILE_ID");
					} else {
						newHisFilePath += filePaths[i] + "/";
					}
				}
				FileMgr.inputStreamToFile(is, newHisFilePath);
				// 设置bean
				// int histVers = ServDao.count("OA_GW_COMM_FILE_HIS", new
				// Bean().set("SERV_ID",
				// fileBean.getStr("SERV_ID")).set("DATA_ID",
				// fileBean.getStr("DATA_ID"))) + 1;//
				// fileBean.set("HISFILE_VERSION", histVers);

				hisBean.set("FILE_HIST_COUNT",
						hisBean.getInt("FILE_HIST_COUNT") + 1);
				// 获得当前节点 讲节点放入到数据中
				hisBean.set("WF_NI_ID", niid);
				fileBean.set("S_MTIME", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
				// 在这里进行向数据库经插入清稿文档
				hisBean.set("HISTFILE_QINGGAO_TYPE", type);
				ServDao.save("OA_GW_COMM_FILE_HIS", hisBean);
				count++;
				log.debug("覆盖了一个清稿的历史版本，服务编码为：" + hisBean.getStr("SERV_ID")
						+ ",数据主键为：" + hisBean.getStr("DATA_ID"));
			}
		}
		// 本节点中第一个清稿文件
		if (count == 0) {
			// 如果没有相同的值 则创建这个清稿文件
			fileBean.setId("");
			// 获得版本记录 将版本记录+1 返回 版本号
			int histVers = 0;
			/*
			 * if(0==hisList.size()){ histVers = 1; }
			 */
			// else{
			histVers = ServDao.count(
					"OA_GW_COMM_FILE_HIS",
					new Bean().set("SERV_ID", fileBean.getStr("SERV_ID")).set(
							"DATA_ID", fileBean.getStr("DATA_ID"))) + 1;//
			// }
			fileBean.set("HISFILE_VERSION", histVers);
			// 历史文件的id
			String hisFileId = "OAHIST_" + Lang.getUUID() + "."
					+ FileMgr.getSuffix(fileBean.getStr("FILE_ID"));
			fileBean.set("HISFILE_ID", hisFileId);

			// ---------------
			fileBean.set("FILE_ID", fileBean.getStr("FILE_ID"));
			// ------------创建他的绝对路径
			// String pathExpr =
			// FileMgr.buildPathExpr(fileBean.getStr("SERV_ID"), hisFileId);
			// 在这里获得数据库中存储文件的路径
			String path = fileBean.getStr("FILE_PATH");
			// 将文件路径转换成为绝对路径
			String zhengWenFilePath = FileMgr.getAbsolutePath(path);
			// 将这个文件复制到一个指定的新路径下
			String newHisFilePath = "";
			// 将文件名进行替换 成hisFileId
			String[] filePaths = zhengWenFilePath.split("/");
			for (int i = 0; i < filePaths.length; i++) {
				if (i == filePaths.length - 1) {
					newHisFilePath += hisFileId;
				} else {
					newHisFilePath += filePaths[i] + "/";
				}
			}

			FileMgr.inputStreamToFile(is, newHisFilePath);
			// 将新路径转换成数据库中存储的路径
			newHisFilePath = "";
			String[] sqlPath = path.split("/");
			for (int i = 0; i < sqlPath.length; i++) {
				if (i == sqlPath.length - 1) {
					newHisFilePath += hisFileId;
				} else {
					newHisFilePath += sqlPath[i] + "/";
				}
			}
			// 将路径上传到数据库
			fileBean.set("FILE_PATH", newHisFilePath);
			// 在这里进行向数据库经插入清稿文档
			fileBean.set("HISTFILE_QINGGAO_TYPE", type);
			// 获得当前节点 讲节点放入到数据中
			fileBean.set("WF_NI_ID", niid);
			fileBean.set("S_MTIME", DateUtils.getDatetimeTS());
			ServDao.save("OA_GW_COMM_FILE_HIS", fileBean);
			log.debug("新增一个正文的历史版本，服务编码为：" + fileBean.getStr("SERV_ID")
					+ ",数据主键为：" + fileBean.getStr("DATA_ID"));
		}
	}
	public static void uploadFile(HttpServletRequest request, ParamBean param, Bean resultBean) {
		String falg = param.getStr("SAVE_FILE");
		String data = resultBean.getStr("_DATA_");
		String his_id = "";
		String[] split = data.split(",");
		for (int i = 0; i < split.length; i++) {
			if (split[i].contains("FILE_ID")) {
				int inde = split[i].indexOf("=");
				his_id = split[i]
						.substring(inde + 1, split[i].length());
			}
		}
		if ("true".equals(falg)) { // 要将这个文件记录到历史版本中
			String niid = param.getStr("WF_NI_ID");
			// 获得节点 id
			// niid = RequestUtils.getStr(request, "NIID");
			if (!"".equals(niid)) {
				// 根据数据查询出系统表中的所有数据
				Bean fileBean = ServDao.find("SY_COMM_FILE", his_id);
				String fileCat = fileBean.getStr("FILE_CAT");
				
				// 在这里 讲历史 版本 插入到数据库中
				List<Bean> hisList = ServDao.finds(
						"OA_GW_COMM_FILE_HIS",
						"AND SERV_ID ='" + fileBean.getStr("SERV_ID")
								+ "'AND DATA_ID='"
								+ fileBean.getStr("DATA_ID")
								+ "' and WF_NI_ID='" + niid + "'");
				hisFileSave(fileBean, hisList, niid, fileCat, null);
			}
		}
	}
	public static void save(Bean paramBean, String fileId, InputStream is, String mType, HttpServletRequest request, Bean data) {

		String qinggao = paramBean.getStr("QINGGAOTYPE");

		String niid = "";
		if ("FEIQINGGAO".equals(qinggao)) {
			data = FileMgr.update(fileId, is, mType);
			niid = data.getStr("WF_NI_ID");
		}
		// if(niid.equals("")){
		// 获得节点 id
		niid = RequestUtils.getStr(request, "NIID");
		// }
		if (!"".equals(data.getStr("SERV_ID"))
				&& !"OA_GW_GONGWEN_ICBCSW".equals(data
						.getStr("SERV_ID"))) {
			if (!"".equals(niid)) {
				// 获得 是否是清稿参数
				// 根据数据查询出系统表中的所有数据
				Bean fileBean = ServDao
						.find("SY_COMM_FILE", fileId);
				// 在这里 讲历史 版本 插入到数据库中
				List<Bean> hisList = ServDao.finds(
						"OA_GW_COMM_FILE_HIS",
						"AND SERV_ID ='"
								+ fileBean.getStr("SERV_ID")
								+ "'AND DATA_ID='"
								+ fileBean.getStr("DATA_ID")
								+ "' and WF_NI_ID='" + niid + "'");
				// 如果是第一个文件
				if ("QINGGAO".equals(qinggao)) {
					String hisFileSaveType = Context.getSyConf(
							"HIS_FILE_SAVE_TYPE", "");
					String[] split = hisFileSaveType.split(",");
					System.out.println(split.toString());
					for (int i = 0; i < split.length; i++) {
						if ("QINGGAO".equals(split[i])) {
							qingGaoFileSave(fileBean, hisList,
									niid, "QINGGAO", is);
						}
					}
				}
				// 非清稿文件
				if ("FEIQINGGAO".equals(qinggao)) {
					String hisFileSaveType = Context.getSyConf(
							"HIS_FILE_SAVE_TYPE",
							System.getProperty("java.io.tmpdir"));
					String[] split = hisFileSaveType.split(",");
					System.out.println(split.toString());

					for (int i = 0; i < split.length; i++) {
						if ("FEIQINGGAO".equals(split[i])) {
							hisFileSave(fileBean, hisList, niid,
									"FEIQINGGAO", null);
						}
					}
				}
			}
		}
	}
}
