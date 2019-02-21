package com.rh.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.rh.api.BaseApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.serv.IFileApiServ;
import com.rh.api.serv.IFormInfoApiServ;
import com.rh.api.serv.ISendApiServ;
import com.rh.api.util.ApiConstant;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.CacheMgr;
import com.rh.core.comm.FileMgr;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;

/**
 * 获取服务基本信息服务类
 * 
 * @author wanglong
 *
 */
public class FormInfoApiServImpl extends BaseApiServ implements IFormInfoApiServ {

	/**
	 * 获取关联服务定义
	 * 
	 * @param servDef
	 * @return
	 */
	private List<Bean> getLinkServDef(Bean data) {

		ServDefBean servDef = (ServDefBean) data.get("servDef");

		Bean dataVal = data.getBean("dataVal");

		List<Bean> linkServ = new ArrayList<Bean>();

		List<Bean> list = servDef.getList("SY_SERV_ITEM");

		for (Bean bean : list) {
			if (bean.getInt("ITEM_INPUT_TYPE") == 9) {

				String servId = bean.getStr("ITEM_INPUT_CONFIG");
				ServDefBean linkServDef = (ServDefBean) CacheMgr.getInstance().get(servId, "_CACHE_SY_SERV");

				if (linkServDef == null) {
					Bean linkBean = ServUtils.getServData(servId);
					linkServDef = new ServDefBean(linkBean);
				}

				if (linkServDef != null) {
					String keyCode = linkServDef.getStr("SERV_KEYS");
					Bean linkBean = new Bean();
					StringBuffer itemSql = new StringBuffer();
					List<Bean> linkList = servDef.getList("SY_SERV_LINK");
					if (linkList != null && linkList.size() > 0) {
						List<Bean> linkItemList = linkList.get(0).getList("SY_SERV_LINK_ITEM");
						List<Bean> fkList = new ArrayList<Bean>();
						for (Bean item : linkItemList) {
							String val = dataVal.getStr(item.getStr("ITEM_CODE"));
							String key = item.getStr("LINK_ITEM_CODE");
							if (key.length() > 0 && val.length() > 0) {
								itemSql.append(" AND ");
								itemSql.append(key);
								itemSql.append("='");
								itemSql.append(val);
								itemSql.append("'");
								Bean b = new Bean();
								b.put("ITEM_CODE", key);
								b.put("ITEM_CODE_VALUE", val);
								fkList.add(b);
							}
						}
						linkServDef.set("linkFk", fkList);
						if (itemSql.length() > 0) {
							ParamBean paramBean = new ParamBean();
							paramBean.setSelect("*");
							paramBean.setWhere(itemSql.toString());
							OutBean dataBean = ServMgr.act(servId, ServMgr.ACT_QUERY, paramBean);

							for (Bean lkd : dataBean.getDataList()) {
								lkd.setId(lkd.getStr(keyCode));
							}

							linkBean.set("dataVal", dataBean.getDataList());
						}
						linkBean.set("servDef", linkServDef);

						Map<String, Bean> items = servDef.getAllItems(); // 所有有效项
						Bean dicts = new Bean();
						for (String key : items.keySet()) {
							Bean itemBean = items.get(key);
							String dictId = itemBean.getStr("DICT_ID");
							if (dictId.length() > 0 && !dicts.contains(dictId)) { // 预处理字典项
								Bean dict = DictMgr.getDict(dictId);
								if (dict != null && dict.getInt("DICT_TYPE") == DictMgr.DIC_TYPE_LIST) {
									// 传递所有字典数据供列表及卡片页面共同使用
									dicts.set(dictId, DictMgr.getTreeList(dictId));
								}
							}
						}
						linkBean.set("dicts", dicts);

						linkServ.add(linkBean);
					}
				}
			}
		}
		return linkServ;
	}

	@Override
	public ApiOutBean getServDef(String servId) {
		ApiOutBean outBean = new ApiOutBean();
		ServDefBean servDef = ServUtils.getServDef(servId);
		Bean resData = new Bean();
		resData.set("servDef", servDef);

		OutBean dataBean = ServMgr.act(servId, ServMgr.ACT_BYID, new ParamBean());

		resData.set("dataVal", dataBean);

		Map<String, Bean> items = servDef.getAllItems(); // 所有有效项
		Bean dicts = new Bean();
		for (String key : items.keySet()) {
			Bean itemBean = items.get(key);
			String dictId = itemBean.getStr("DICT_ID");
			if (dictId.length() > 0 && !dicts.contains(dictId)) { // 预处理字典项
				Bean dict = DictMgr.getDict(dictId);
				if (dict != null && dict.getInt("DICT_TYPE") == DictMgr.DIC_TYPE_LIST) {
					// 传递所有字典数据供列表及卡片页面共同使用
					dicts.set(dictId, DictMgr.getTreeList(dictId));
				}
			}
		}
		resData.set("dicts", dicts);

		List<Bean> linkServ = getLinkServDef(resData);

		if (linkServ != null && linkServ.size() > 0) {
			resData.set("linkServ", linkServ);
		}

		Bean gwTmpl = ServDao.find("OA_COMMON_TMPL", new ParamBean().setWhere(" and TMPL_CODE = '" + servId + "'"));
		resData.set("gwTmpl", gwTmpl);

		outBean.setData(resData);
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean getServDefAndData2(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String nid = reqData.getStr("nid");
		String _AGENT_USER_ = reqData.getStr("_AGENT_USER_");

		ServDefBean servDef = ServUtils.getServDef(servId);
		Bean resData = new Bean();
		resData.set("servDef", servDef);
		Bean dataBean = ServMgr.act(servId, ServMgr.ACT_BYID,
				new Bean().setId(dataId).set("NI_ID", nid).set("SEND_ID", reqData.getStr("sendId")).set("_AGENT_USER_", _AGENT_USER_));
		if (dataBean == null) {
			throw new TipException(Context.getSyMsg("SY_BYID_ERROR", dataId));
		}
		resData.set("dataVal", dataBean);

		List<Bean> mindList = MindApiServImpl.getMindListForTime(dataId, servId);
		List<Bean> newlist = new ArrayList<Bean>();
		for (Bean bean : mindList) {
			Bean mindBean = MindApiServImpl.parseMindBean(bean);
			newlist.add(mindBean);
		}
		resData.set("mindList", newlist);

		IFileApiServ fileApiServ = new FileApiServImpl();

		ApiOutBean fileOutBean = fileApiServ.getFileListByDataId(new Bean().set("dataId", dataId));
		List<Bean> fileList = fileOutBean.getData().getList("list");
		resData.set("fileList", fileList);

		ISendApiServ sendApiServ = new SendApiServImpl();
		resData.set("readList",
				sendApiServ.getReadList(new Bean().set("dataId", dataId)).getData().getList("readList"));

		resData.set("distrList",
				sendApiServ.getDistrList(new Bean().set("dataId", dataId)).getData().getList("distrList"));

		Bean gwTmpl = ServDao.find("OA_COMMON_TMPL", new ParamBean().setWhere(" and TMPL_CODE = '" + servId + "'"));
		resData.set("gwTmpl", gwTmpl);

		List<Bean> relateList = ServMgr
				.act("SY_SERV_RELATE", ServMgr.ACT_QUERY, new ParamBean().setWhere(" and DATA_ID = '" + dataId + "'"))
				.getDataList();
		resData.set("relateList", relateList);

		if (reqData.isNotEmpty("sendId")) {
			Bean sendDetail = ServDao.find("SY_COMM_SEND_DETAIL", reqData.getStr("sendId"));
			resData.set("sendDetail", sendDetail);
		}

		outBean.setData(resData);
		return outBean;
	}

	@Override
	public ApiOutBean getDict(String dictId) {
		ApiOutBean outBean = new ApiOutBean();
		Bean dictBean = DictMgr.getDict(dictId);
		if (dictBean == null) {
			throw new TipException("字典不存在");
		}
		dictBean.set("SY_SERV_DICT_ITEM", DictMgr.getTreeList(dictId));
		outBean.setData(dictBean);
		return outBean;
	}

	@Override
	public ApiOutBean getDictTreeData(String dictId, String pid) {
		ApiOutBean outBean = new ApiOutBean();
		Bean dictBean = DictMgr.getDict(dictId);
		Bean dictData = new Bean();
		if (dictBean == null) {
			throw new TipException("字典不存在");
		} else {
			if (pid == null || pid.length() <= 0) {
				// dictData.set("dict", DictMgr.getTreeList(dictId));
				dictData.set("dict", DictMgr.getItemList(dictId));
			} else {
				dictData.set("dict", DictMgr.getTreeList(dictId, pid));
			}

		}
		outBean.setData(dictData);
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean save(Bean reqData) {

		parseLinkServData(reqData);

		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("servId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		String servId = reqData.getStr("servId");
		Bean resBean = ServMgr.act(servId, ServMgr.ACT_SAVE, reqData);
		if (resBean.isEmpty()) {
			throw new TipException("保存失败");
		}
		outBean.setData(resBean);
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean saveResultData(Bean reqData) {

		parseLinkServData(reqData);

		if (reqData.isEmpty("servId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		if (reqData.isNotEmpty("dataId")) {
			reqData.setId(reqData.getStr("dataId"));
		}

		String servId = reqData.getStr("servId");
		Bean resBean = ServMgr.act(servId, ServMgr.ACT_SAVE, reqData);
		if (resBean.isEmpty()) {
			throw new TipException("保存失败");
		}
		String dataId = resBean.getId();
		return this.getServDefAndData2(new Bean().set("servId", servId).set("dataId", dataId));
	}

	@Override
	public ApiOutBean delete(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("servId") || reqData.isEmpty("dataId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		ParamBean paramBean = new ParamBean();
		paramBean.setServId(servId);
		paramBean.setId(dataId);
		Bean resBean = ServMgr.act(servId, ServMgr.ACT_DELETE, paramBean);
		if (resBean.getInt("_OKCOUNT_") == 0) {
			throw new TipException("删除失败");
		}
		return outBean;
	}

	private Bean parseLinkServData(Bean reqData) {

		Iterator<Map.Entry<Object, Object>> entries = reqData.entrySet().iterator();

		Bean dataBean = new Bean();

		while (entries.hasNext()) {

			Map.Entry<Object, Object> entry = entries.next();
			Object kobj = entry.getKey();
			Object kval = entry.getValue();
			if (kobj instanceof String && kval instanceof String) {
				String key = (String) kobj;
				String val = (String) kval;
				if (key.indexOf("__LK__") > 0) {
					String[] array = key.split("__LK__");
					String idx = array[0];
					String column = array[1];

					if (dataBean.containsKey(idx)) {
						Bean b = (Bean) dataBean.get(idx);
						b.put(column, val);
						dataBean.put(idx, b);
					} else {
						Bean b = new Bean();
						b.put(column, val);
						dataBean.put(idx, b);
					}
					// reqData.remove(key);
				}
			}
		}

		List<Object> list = new ArrayList<Object>();
		Iterator<Map.Entry<Object, Object>> entries1 = dataBean.entrySet().iterator();
		while (entries1.hasNext()) {
			Map.Entry<Object, Object> entry = entries1.next();
			list.add(entry.getValue());
		}

		String linkServ = reqData.getStr("__LINK_SERV_ID");

		if (StringUtils.isNotBlank(linkServ)) {
			reqData.put(linkServ, list);
		}
		return reqData;
	}

	@Override
	public ApiOutBean copyZhengwen(Bean reqData) {
		ApiOutBean result = new ApiOutBean();

		if (reqData.isNotEmpty("FILE")) {
			String tmplFileId = reqData.getStr("FILE").split(",")[0];
			Bean fileBean = FileMgr.getFile(tmplFileId);
			if (fileBean == null) {
				throw new RuntimeException("模板文件记录不存在！");
			}
			String fileName = fileBean.getStr("FILE_NAME");
			String surfix = "";
			if (0 < fileName.lastIndexOf(".")) {
				surfix = fileName.substring(fileName.lastIndexOf("."));
			}
			reqData.set("FILE_NAME", reqData.getStr("DIS_NAME") + surfix);
			Bean saveBean = FileMgr.copyFile(fileBean, reqData);

			Bean rtnBean = new Bean();
			UserBean userBean = UserMgr.getUser(saveBean.getStr("S_USER"));
			rtnBean.set("fileId", saveBean.getStr("FILE_ID"));
			rtnBean.set("fileName", saveBean.getStr("FILE_NAME"));
			rtnBean.set("fileDisName", saveBean.getStr("DIS_NAME"));
			rtnBean.set("fileSize", saveBean.getStr("FILE_SIZE"));
			rtnBean.set("fileTime", saveBean.getStr("S_MTIME"));
			rtnBean.set("fileExt",
					saveBean.getStr("FILE_NAME").split("\\.")[saveBean.getStr("FILE_NAME").split("\\.").length - 1]);
			rtnBean.set("fileType", saveBean.getStr("FILE_MTYPE"));
			rtnBean.set("fileURL", Context.getHttpUrl() + "/file/" + saveBean.getStr("FILE_ID"));
			rtnBean.set("target", saveBean.getStr("FILE_CAT"));
			rtnBean.set("uploadUser", saveBean.getStr("S_USER"));
			rtnBean.set("uploadUserName", saveBean.getStr("S_UNAME"));
			rtnBean.set("servId", saveBean.getStr("SERV_ID"));
			rtnBean.set("dataId", saveBean.getStr("DATA_ID"));
			rtnBean.set("wfNid", saveBean.getStr("WF_NI_ID"));
			rtnBean.set("userPost", userBean.getPost());
			rtnBean.set("deptCode", userBean.getDeptCode());
			rtnBean.set("deptName", userBean.getDeptName());
			rtnBean.set("userImg", userBean.getImgSrc());
			rtnBean.set("fileCat", "ZHENGWEN");
			rtnBean.set("itemCode", "ZHENGWEN");

			result.setData(rtnBean);
		} else {
			throw new RuntimeException("模板文件记录不存在！");
		}

		return result;
	}

}
