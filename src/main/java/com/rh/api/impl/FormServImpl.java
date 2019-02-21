package com.rh.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.api.serv.IFormServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.core.base.BaseContext;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.CacheMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;

public class FormServImpl implements IFormServ {

	private static Log log = LogFactory.getLog(FormServImpl.class);

	@Override
	public ApiOutBean getServDef(String servId) {
		ApiOutBean outBean = new ApiOutBean();
		ServDefBean servDef = ServUtils.getServDef(servId);
		Bean resData = new Bean();
		resData.set("servDef", servDef);

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

		outBean.setData(resData);
		return outBean;
	}

	public ApiOutBean getDictDef(String dictId) {
		ApiOutBean outBean = new ApiOutBean();
		Bean dict = DictMgr.getDict(dictId);
		outBean.setData(dict);
		return outBean;
	}

	private List<Bean> getLinkServDef(Bean data) {

		ServDefBean servDef = (ServDefBean) data.get("servDef");

		Bean dataVal = data.getBean("dataVal");

		List<Bean> linkServ = new ArrayList<Bean>();

		List<Bean> list = servDef.getList("SY_SERV_ITEM");

		for (Bean bean : list) {
			if (bean.getInt("ITEM_INPUT_TYPE") == 9) {

				String servId = bean.getStr("ITEM_INPUT_CONFIG");
				if (servId == null || servId.length() == 0) {
					continue;
				}

				servId = servId.split(",")[0];

				ServDefBean linkServDef = (ServDefBean) CacheMgr.getInstance().get(servId, "_CACHE_SY_SERV");

				if (linkServDef == null) {
					Bean linkBean = ServUtils.getServData(servId);
					if (linkBean == null) {
						continue;
					}
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

	public ApiOutBean saveServDef(ApiParamBean bean) {
		bean.set("_TRANS_", true);
		ApiOutBean outBean = new ApiOutBean();
		try {
			BaseContext.getExecutor().execute("drop table " + bean.getStr("servId"));
		} catch (Exception se) {
			log.error(se.getMessage());
		}

		List<Bean> itemList = bean.getList("SY_SERV_ITEM");
		String sql = null;
		try {
			for (Bean item : itemList) {
				if (item.getInt("ITEM_TYPE") == 1) {
					String colType = "";
					if (item.getStr("ITEM_FIELD_TYPE").equals("NUM")) {
						colType += "number";
					} else {
						colType += "varchar2";
					}
					if (item.getStr("ITEM_FIELD_LENGTH").length() > 0
							&& !item.getStr("ITEM_FIELD_LENGTH").equals("0")) {
						colType += "(" + item.getStr("ITEM_FIELD_LENGTH") + ")";
					} else {
						colType += "(400)";
					}
					sql = "DECLARE num NUMBER; BEGIN SELECT COUNT(1) INTO num from cols where table_name = upper('"
							+ item.getStr("SERV_ID") + "') and column_name = upper('" + item.getStr("ITEM_CODE")
							+ "'); IF num <= 0 THEN execute immediate 'alter table " + item.getStr("SERV_ID") + " add ("
							+ item.getStr("ITEM_CODE") + " " + colType + " )'; EXECUTE IMMEDIATE 'COMMENT ON COLUMN "
							+ item.getStr("SERV_ID") + "." + item.getStr("ITEM_CODE") + " IS \''"
							+ item.getStr("ITEM_NAME") + "\'''; END IF; END;";
					BaseContext.getExecutor().execute(sql);
				}
			}
		} catch (Exception e) {
			throw e;
		}
		ServMgr.act("SY_SERV", "saveDefFromDesigner", bean);
		return outBean;
	}

	public ApiOutBean getServList() {
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> servList = ServDao.finds("SY_SERV", " and 1 = 1");
		Bean list = new Bean();
		list.put("list", servList);
		outBean.setData(list);
		return outBean;
	}

	public ApiOutBean createServ(ApiParamBean bean) {
		ApiOutBean outBean = new ApiOutBean();

		try {
			BaseContext.getExecutor().execute("drop table " + bean.getStr("servId"));
		} catch (Exception se) {
			log.error(se.getMessage());
		}

		String sqlText = this.genSql(bean);

		String[] sqls = sqlText.split(";"); // ;加上换行符作为分隔符
		for (String sql : sqls) {
			if (sql.matches("\\s*(--|commit;).*")) { // 忽略无效语句
				continue;
			}

			try {
				BaseContext.getExecutor().execute(sql);
			} catch (Exception se) {
				throw se;
			}
		}

		ServMgr.act("SY_SERV", "delete", new Bean().setId(bean.getStr("servId")).set("_TRANS_", true));

		ServMgr.act("SY_SERV", "fromTable", new Bean().set("TABLE_VIEW", bean.getStr("servId")).set("_TRANS_", true));

		ParamBean saveBean = new ParamBean();
		saveBean.setId(bean.getStr("servId"));
		saveBean.set("SERV_MEMO", bean.getStr("servMemo"));
		saveBean.set("SERV_WF_FLAG", bean.getBoolean("isWfe") ? 1 : 2);
		if (bean.getBoolean("isWfe")) {
			saveBean.set("SERV_DATA_TITLE", "#APPLY_TITLE#");
			saveBean.set("SERV_DATA_CODE", "#APPLY_CODE#");
		}
		saveBean.set("_TRANS_", true);
		ServMgr.act("SY_SERV", "save", saveBean);

		return outBean;
	}

	private String genSql(ApiParamBean bean) {

		StringBuilder tableSql = new StringBuilder();

		StringBuilder commentSql = new StringBuilder();

		List<Bean> itemList = bean.getList("items");

		tableSql.append("create table ").append(bean.getStr("servId")).append(" (");

		if (bean.getBoolean("isWfe")) {
			tableSql.append(" APPLY_ID varchar2(40) not null,");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".APPLY_ID is '主键';");
		} else {
			tableSql.append(" ID varchar2(40) not null,");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".ID is '主键';");
		}
		commentSql.append("comment on table ").append(bean.getStr("servId")).append(" is '")
				.append(bean.getStr("servName")).append("';");
		for (Bean item : itemList) {
			tableSql.append(item.getStr("code")).append(" ").append(item.getStr("type")).append("(")
					.append(item.getStr("length")).append(")").append(" ")
					.append(item.getInt("isNull") == 1 ? "not null" : "").append(",");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".")
					.append(item.getStr("code")).append(" is '").append(item.getStr("name")).append("';");
		}

		// 插入默认字段
		tableSql.append("S_FLAG number(1),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_FLAG is '有效标志';");
		tableSql.append("S_MTIME varchar2(24),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_MTIME is '更细时间';");
		tableSql.append("S_ATIME varchar2(24),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_ATIME is '创建时间';");
		tableSql.append("S_USER varchar2(40),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_USER is '创建人编码';");
		// tableSql.append("USER_NAME varchar2(100),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".USER_NAME is '创建人名称';");
		tableSql.append("S_DEPT varchar2(40),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_DEPT is '创建部门编码';");
		// tableSql.append("DEPT_NAME varchar2(100),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".DEPT_NAME is '创建部门名称';");
		tableSql.append("S_TDEPT varchar2(40),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_TDEPT is '上级部门编码';");
		// tableSql.append("TDEPT_NAME varchar2(100),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".TDEPT_NAME is '上级部门名称';");
		tableSql.append("S_ODEPT varchar2(40),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_ODEPT is '机构编码';");
		// tableSql.append("ODEPT_NAME varchar2(100),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".ODEPT_NAME is '机构名称';");
		// tableSql.append("ODEPT_LEVEL number(4),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".ODEPT_LEVEL is '机构级别';");
		// tableSql.append("S_YEAR varchar2(4),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".S_YEAR is '年度';");
		// tableSql.append("S_YEARMONTH varchar2(10),");
		// commentSql.append("comment on column
		// ").append(bean.getStr("servId")).append(".S_YEARMONTH is '年月';");
		tableSql.append("S_CMPY varchar2(40),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_CMPY is '公司';");
		tableSql.append("S_UNAME varchar2(100),");
		commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_UNAME is '起草人';");
		if (bean.getBoolean("isWfe")) {
			tableSql.append("YYYYMMDD varchar2(24),");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".YYYYMMDD is '当前日期';");
			tableSql.append("APPLY_CODE varchar2(400),");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".APPLY_CODE is '流水号';");
			tableSql.append("APPLY_TITLE varchar2(400),");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".APPLY_TITLE is '申请单标题';");
			tableSql.append("S_WF_STATE number(4),");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_WF_STATE is '流程实例状态';");
			tableSql.append("S_WF_INST varchar2(40),");
			commentSql.append("comment on column ").append(bean.getStr("servId")).append(".S_WF_INST is '流程实例编码';");
			tableSql.append("S_WF_NODE varchar2(2000),");
			commentSql.append("comment on column ").append(bean.getStr("servId"))
					.append(".S_WF_NODE is '当前节点ID，多个用逗号分隔';");
			tableSql.append("S_WF_USER varchar2(2000),");
			commentSql.append("comment on column ").append(bean.getStr("servId"))
					.append(".S_WF_USER is '当前用户ID，多个用逗号分隔';");
			tableSql.append("primary key (APPLY_ID)");
		} else {
			tableSql.append("primary key (ID)");
		}

		tableSql.append(");");

		return tableSql.toString() + commentSql.toString();
	}

	public ApiOutBean getServerUrl(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();
		String serverURL = Context.getSyConf(paramBean.getStr("CONF_KEY"), "SY_HTTP_URL");
		outBean.setData(new Bean().set("serverURL", serverURL));
		return outBean;
	}

}
