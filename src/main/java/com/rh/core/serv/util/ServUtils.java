/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.RowHandler;
import com.rh.core.base.db.SqlBuilder;
import com.rh.core.base.db.TableBean;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.CacheMgr;
import com.rh.core.org.UserBean;
import com.rh.core.plug.search.IIndexServ;
import com.rh.core.plug.search.IndexListener;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDef;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServDefSearch;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;
import com.rh.core.util.file.FileHelper;
import com.rh.core.util.lang.ValidCallback;
import com.rh.core.util.msg.CommonMsg;
import com.rh.core.util.msg.MsgCenter;
import com.rh.core.util.var.VarMgr;
import com.rh.core.util.ws.ServiceMgr;
import com.rh.resource.Resource;

/**
 * 处理服务的一些辅助方法。
 * 
 * @author wanglong
 * @version $Id$
 */
public class ServUtils {
	/**
	 * 服务定义信息的常用项
	 * 
	 * @author wanglong
	 */
	public static enum SERV_DEF {
		/** 服务定义类参数 */
		$SERV_ID, $ITEMS, $ITEMS_VIEW, $ITEMS_TABLE, $ACTS, $LINKS, $WFMAP, $QUERIES,
		/** 服务对应实现类 */
		$CLASS,
		/** 数据项是否启用log跟踪的标志 */
		$ITEM_LOG_FLAG,
		/** 数据项是否启用唯一约束判断 */
		$ITEM_UNIQUE_GROUP,
		/** 文件上传字段项列表 */
		$FILE_FIELD_ITEMS,
		/** 自定义附件项列表 */
		$FILE_SELF_ITEMS,
		/** 组合值字段项列表 */
		$COMBINE_ITEMS
	};

	/**
	 * 过程参数
	 * 
	 * @author wanglong
	 */
	public static enum SERV_PARAM {
		/** 参数：大文本 */
		$BIGTEXT,
		/** 缓存信息 */
		$SERV_CACHES,
	};

	/** 服务缓存类型 */
	public static final String CACHE_TYPE_SERV = "_CACHE_SY_SERV";

	/** 表名称：服务 */
	public static final String TABLE_SERV = "SY_SERV";
	/** 表名称：服务项 */
	public static final String TABLE_SERV_ITEM = "SY_SERV_ITEM";
	/** 表名称：按钮 */
	public static final String TABLE_SERV_ACT = "SY_SERV_ACT";
	/** 表名称：参数 */
	public static final String TABLE_SERV_ACT_PARAM = "SY_SERV_ACT_PARAM";
	/** 表名称：关联 */
	public static final String TABLE_SERV_LINK = "SY_SERV_LINK";
	/** 表名称：关联明细 */
	public static final String TABLE_SERV_LINK_ITEM = "SY_SERV_LINK_ITEM";
	/** 表名称：全文检索定义 */
	public static final String TABLE_SERV_SEARCH = "SY_SERV_SEARCH";
	/** 表名称：过滤规则 */
	public static final String TABLE_SERV_WHERE = "SY_SERV_WHERE";
	/** 表名称：常用查询 */
	public static final String TABLE_SERV_QUERY = "SY_SERV_QUERY";

	/** log */
	private static Log log = LogFactory.getLog(ServUtils.class);

	/** json src path */
	private static final String JSON_PATH_SRC = FileHelper.getJsonPath() + "SY_SERV/";

	/**
	 * 根据服务ID获取定义信息
	 * 
	 * @param servId
	 *            服务Id
	 * @return 定义信息
	 */
	public static ServDefBean getServDef(String servId) {
		if ((servId == null) || (servId.length() == 0)) { // 判断数据有效性
			throw new RuntimeException(Context.getSyMsg("SY_SERV_ID_ERROR", servId));
		}
		ServDefBean servDef = (ServDefBean) CacheMgr.getInstance().get(servId, CACHE_TYPE_SERV);
		if (servDef == null) {
			Bean bean = getServData(servId); // 重新读取服务信息
			if (bean == null) {
				throw new RuntimeException(Context.getSyMsg("SY_SERV_ID_ERROR", servId));
			}
			servDef = new ServDefBean(bean);
		} else if (servDef.contains("SERV_INIT")) { // 已初始化过，不再初始化
			return servDef;
		}
		if (servDef.isNotEmpty("SERV_PID")) { // 存在父服务定义
			appendParent(servDef); // 导入父定义的相关设定
		} else {
			if (servDef.getInt("SERV_LIST_LOAD") == Constant.YES_INT) { // 启用卡片自定义JS
				servDef.set("SERV_LIST_LOAD_NAMES", servDef.getStr("SERV_ID"));
			}
			if (servDef.getInt("SERV_CARD_LOAD") == Constant.YES_INT) { // 启用卡片自定义JS
				servDef.set("SERV_CARD_LOAD_NAMES", servDef.getStr("SERV_ID"));
			}
		}
		if (servDef.isEmpty("SERV_SRC_ID")) {
			servDef.set("SERV_SRC_ID", servId); // 缺省设置引用自身方便附件直接调用
		}
		if (servDef.getInt("SERV_CARD_TMPL") == Constant.YES_INT) { // 启用自定义模版
			servDef.set("SERV_CARD_TMPL_NAME", servDef.getStr("SERV_ID"));
		}

		List<Bean> defaultList = new ArrayList<Bean>();
		List<Bean> itemList = (List<Bean>) servDef.get(TABLE_SERV_ITEM, defaultList); // 所有数据项
		List<Bean> actList = (List<Bean>) servDef.get(TABLE_SERV_ACT, defaultList); // 操作定义
		List<Bean> linkList = (List<Bean>) servDef.get(TABLE_SERV_LINK, defaultList); // 关联定义
		List<Bean> queryList = (List<Bean>) servDef.get(TABLE_SERV_QUERY, defaultList); // 关联定义
		List<Bean> tableItems = new ArrayList<Bean>(itemList.size()); // 数据表列
		List<Bean> viewItems = new ArrayList<Bean>(itemList.size()); // 视图列
		List<Bean> fileFieldItems = new ArrayList<Bean>(); // 文件字段
		List<Bean> fileSelfItems = new ArrayList<Bean>(); // 文件字段
		List<Bean> combineItems = new ArrayList<Bean>(); // 组合值
		HashMap<Object, List<String>> uniqueMap = new HashMap<Object, List<String>>();
		boolean bFirst = true;
		for (Bean itemBean : itemList) {
			if (itemBean.getInt("S_FLAG") != Constant.YES_INT) { // 忽略无效字段
				continue;
			}
			boolean isKey = false;
			if (itemBean.get("ITEM_TYPE", ServConstant.ITEM_TYPE_TABLE) == ServConstant.ITEM_TYPE_TABLE) {
				if (servDef.getPKey().equals(itemBean.getStr("ITEM_CODE"))) {
					isKey = true;
				} else if (bFirst && servDef.isEmpty("SERV_KEYS")) { // 如果没有主键自动用第一个字段为主键
					servDef.set("SERV_KEYS", itemBean.getStr("ITEM_CODE"));
					isKey = true;
				}
				if (itemBean.getInt("ITEM_LOG_FLAG") == Constant.YES_INT) { // 启用留痕
					servDef.set(SERV_DEF.$ITEM_LOG_FLAG, Constant.YES_INT);
				}
				if (itemBean.getInt("ITEM_UNIQUE_GROUP") > 0) { // 设定了唯一分组
					List<String> uList = uniqueMap.get(itemBean.get("ITEM_UNIQUE_GROUP"));
					if (uList == null) {
						uList = new ArrayList<String>();
						uniqueMap.put(itemBean.get("ITEM_UNIQUE_GROUP"), uList);
					}
					uList.add(itemBean.getStr("ITEM_CODE"));
				}
				if (itemBean.getInt("ITEM_INPUT_MODE") == ServConstant.ITEM_INPUT_MODE_COMBINE) {
					combineItems.add(itemBean);
				}
				if (itemBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_FILE) { // 文件上传字段
					fileFieldItems.add(itemBean);
				}
				if (isKey) { // 将主键放在第一个位置
					tableItems.add(0, itemBean);
					viewItems.add(0, itemBean);
				} else {
					tableItems.add(itemBean);
					viewItems.add(itemBean);
				}
			} else if (itemBean.getInt("ITEM_TYPE") == ServConstant.ITEM_TYPE_VIEW) {
				if (itemBean.getInt("ITEM_INPUT_MODE") == ServConstant.ITEM_INPUT_MODE_COMBINE) {
					combineItems.add(itemBean);
				}
				viewItems.add(itemBean);
			} else { // 自定义类型
				if (itemBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_RELATE) { // 相关数据
					String relateConfig = itemBean.getStr("ITEM_INPUT_CONFIG");
					if (relateConfig.length() > 0) {
						List<String> relateIds = servDef.getList("SERV_RELATE_IDS");
						List<Bean> relateList = JsonUtils.toBeanList(relateConfig);
						for (Bean relate : relateList) {
							relateIds.add(relate.getStr("servId"));
						}
						servDef.set("SERV_RELATE_IDS", relateIds);
					}
				} else if (itemBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_LABEL) { // 自定label
					if (itemBean.getStr("ITEM_CODE").startsWith("_MIND_")) { // 启用了意见label展示
						servDef.set("SERV_MIND_LABLE_FLAG", true); // 设置意见label展示标志
					}
				} else if (itemBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_ATTACHMENT) {
					fileSelfItems.add(itemBean);
				} else if (itemBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_COMMENT) { // 启用了评论功能
					servDef.set("SERV_COMMENT_FLAG", Constant.YES); // 系统启用了评论服务
				}
			}
			if (fileFieldItems.size() > 0 || fileFieldItems.size() > 0) { // 包含文件处理，设置文件标志
				servDef.set("SERV_FILE_FLAG", Constant.YES);
			}
			if (bFirst) { // 第一行规则处理
				bFirst = false;
			}
		}
		if (!uniqueMap.isEmpty()) {
			servDef.set(SERV_DEF.$ITEM_UNIQUE_GROUP, uniqueMap);
		}
		servDef.set(SERV_DEF.$SERV_ID, servId);
		servDef.set(SERV_DEF.$ITEMS, BeanUtils.toLinkedMap(itemList, "ITEM_CODE", new ValidCallback() {
			public boolean valid(Bean data) {
				return (data.getInt("S_FLAG") == Constant.YES_INT);
			}
		})); // 表单项，过滤禁用的
		servDef.set(SERV_DEF.$ITEMS_TABLE, tableItems); // 表对应字段列表
		servDef.set(SERV_DEF.$ITEMS_VIEW, viewItems); // 表+视图对应字段列表
		servDef.set(SERV_DEF.$FILE_FIELD_ITEMS, fileFieldItems); // 文件上传对应的字段
		servDef.set(SERV_DEF.$FILE_SELF_ITEMS, fileSelfItems); // 自定义附件上传
		servDef.set(SERV_DEF.$COMBINE_ITEMS, combineItems); // 组合值对应的字段
		// 用于通用按钮重载名称处理，在web.xml中配置通用按钮名称设定
		final Bean actNames = JsonUtils.toBean(Context.appStr("SY_SERV_ACT_NAMES"));
		servDef.set(SERV_DEF.$ACTS, BeanUtils.toLinkedMap(actList, "ACT_CODE", new ValidCallback() {
			public boolean valid(Bean data) {
				String actCode = data.getStr("ACT_CODE");
				if (actNames.contains(actCode)) {
					data.set("ACT_NAME", actNames.getStr(actCode));
				}
				return (data.getInt("S_FLAG") == Constant.YES_INT);
			}
		})); // 操作按钮，过滤禁用的
		servDef.set(SERV_DEF.$LINKS, BeanUtils.toLinkedMap(linkList, "LINK_SERV_ID", new ValidCallback() {
			public boolean valid(Bean data) {
				return (data.getInt("S_FLAG") == Constant.YES_INT);
			}
		})); // 关联列表，过滤禁用的
		servDef.set(SERV_DEF.$QUERIES, BeanUtils.toLinkedMap(queryList, "QUERY_ID", new ValidCallback() {
			public boolean valid(Bean data) {
				return (data.getInt("S_FLAG") == Constant.YES_INT);
			}
		})); // 关联列表，过滤禁用的
		if (servDef.isNotEmpty("SERV_CLASS")) { // 定义的扩展类
			try {
				servDef.set(SERV_DEF.$CLASS, Lang.loadClass(servDef.getStr("SERV_CLASS")).newInstance());
			} catch (Throwable e) {
				log.error(e.getMessage() + " " + servId, e);
				servDef.set(SERV_DEF.$CLASS, new CommonServ()); // 设置缺省的服务类
			}
		} else {
			servDef.set(SERV_DEF.$CLASS, new CommonServ()); // 设置缺省的服务类
		}
		if (servDef.isNotEmpty("SERV_CARD_TMPL_NAME")) { // 加载模版文件
			servDef.set("SERV_CARD_TMPL_CONTENT", servDef.getCardTmplContent());
		}
		servDef.set("SERV_INIT", 1);
		// 放入缓存
		CacheMgr.getInstance().set(servId, servDef, CACHE_TYPE_SERV);
		return servDef;
	}

	/**
	 * 根据服务编码从数据库导入服务基础设定信息
	 * 
	 * @param servId
	 *            服务编码（同时作为操作表编码、查询表编码）
	 * @return 导入的item数量
	 */
	public static int impServDef(String servId) {
		return impServDef(servId, servId, servId, true);
	}

	/**
	 * 根据服务编码从数据库导入服务基础设定信息
	 * 
	 * @param servId
	 *            服务编码
	 * @param tableAction
	 *            操作表名
	 * @param tableView
	 *            查询表名
	 * @param createFlag
	 *            添加标注，如果为true，则根据数据库字段导入全部信息，false增补导入字段
	 * @return 导入的item数量
	 */
	@SuppressWarnings("unchecked")
	public static int impServDef(String servId, String tableAction, String tableView, boolean createFlag) {
		Bean servDef = new Bean();
		TableBean actBean = Transaction.getExecutor().getDBTable(tableAction);
		int count = 0;
		if (actBean == null) {
			throw new RuntimeException(Context.getSyMsg("SY_TABLE_CODE_ERROR", tableAction)); // 无效的表编码
		}
		List<Bean> itemList = actBean.getItemList();
		String time = DateUtils.getDatetimeTS();
		if (!tableAction.equals(tableView)) { // 为查询使用了单独的表或者视图，判断增加的字段为视图字段
			List<Bean> viewItemList = Transaction.getExecutor().getDBTable(tableView).getItemList();
			int order = itemList.size() * 10;
			LinkedHashMap<String, Bean> itemMap = BeanUtils.toLinkedMap(itemList, "ITEM_CODE");
			for (Bean viewItem : viewItemList) {
				if (!itemMap.containsKey(viewItem.getStr("ITEM_CODE"))) {
					viewItem.set("ITEM_TYPE", ServConstant.ITEM_TYPE_VIEW);
					viewItem.set("ITEM_ORDER", order);
					order = order + 10;
					itemList.add(viewItem);
				}
			}
		}
		SqlBuilder sqlBuilder = Context.getBuilder();
		if (createFlag) { // 已经确认不存在服务定义，直接根据数据库定义插入服务定义
			servDef.set("SERV_ID", servId);
			servDef.set("TABLE_ACTION", tableAction);
			servDef.set("TABLE_VIEW", tableView);
			servDef.set("S_FLAG", Constant.YES_INT);
			servDef.set("SERV_NAME", actBean.getTableName());
			servDef.set("SERV_MEMO", actBean.getTableMemo());
			servDef.set("SERV_KEYS", actBean.getPKey());
			servDef.set("SERV_CMPY", actBean.getServCmpy());
			servDef.set("SERV_PAGE_COUNT", 50); // 每页50条
			servDef.set("SERV_CACHE_FLAG", ServConstant.CACHE_FLAG_NO); // 不装载缓存
			servDef.set("SERV_QUERY_MODE", 1); // 简洁查询模式
			servDef.set("SERV_LOCK_FLAG", Constant.NO_INT); // 不判断锁
			servDef.set("SERV_DELETE_FLAG", Constant.NO_INT); // 不启用假删除
			servDef.set("SERV_AUTH_FLAG", ServConstant.AUTH_FLAG_SESSION); // 判断Session权限
			servDef.set("SERV_CARD_STYLE", 2); // 卡片页面列数
			servDef.set("SERV_SEARCH_FLAG", Constant.NO_INT); // 不启用全文搜索
			servDef.set("S_PUBLIC", Constant.YES_INT); // 公共配置
			servDef.set("S_MTIME", time);
			servDef.set("SERV_TYPE", ServConstant.SERV_TYPE_SERV); // 缺省创建时为数据表类型
			servDef.set("SERV_DATA_SOURCE", Transaction.getDsName()); // 设置其他数据源
			servDef.set("S_CMPY", Context.getCmpy()); // 设置公司编码
			servDef.set("S_USER", Context.getUserBean().getId()); // 设置人员编码
			servDef.set("PRO_FLAG", Constant.YES_INT); // 设置产品标识
			String psql = sqlBuilder.insertByBean(TABLE_SERV, servDef);
			if (Context.getExecutor().execute(null, psql, servDef.getList(Constant.PARAM_PRE_VALUES)) > 0) {
				List<List<Object>> params = new ArrayList<List<Object>>(itemList.size());
				for (int i = 0; i < itemList.size(); i++) {
					Bean itemBean = itemList.get(i);
					itemBean.set("SERV_ID", servId);
					itemBean.set("S_MTIME", time);
					initItem(itemBean);
					if (itemBean.getStr("ITEM_CODE").equals("APPLY_CODE")) {
						String[] temp = servId.split("_");
						if (temp.length >= 3) {
							itemBean.set("ITEM_INPUT_CONFIG", temp[2] + "#YYYYMMDD##_SN,4#");
						}
						itemBean.set("ITEM_INPUT_MODE", "6");
					} else {
						itemBean.set("ITEM_INPUT_CONFIG", "");
					}

					
					psql = sqlBuilder.insertByBean(TABLE_SERV_ITEM, itemBean);
					params.add(itemBean.getList(Constant.PARAM_PRE_VALUES));
				}
				count = Context.getExecutor().executeBatch(null, psql, params); // 批量插入列表数据
			}
		} else { // 说明已经存在服务定义，导入补充的数据项
			servDef = getServDataByDB(servId);
			Map<String, Bean> oldMap = BeanUtils.toLinkedMap((List<Bean>) servDef.get(TABLE_SERV_ITEM), "ITEM_CODE");
			List<List<Object>> params = new ArrayList<List<Object>>();
			String psql = null;
			for (Bean itemBean : itemList) {
				if (!oldMap.containsKey(itemBean.getStr("ITEM_CODE"))) { // 新的数据库字段
					itemBean.set("SERV_ID", servId);
					itemBean.set("S_MTIME", time);
					initItem(itemBean);
					psql = sqlBuilder.insertByBean(TABLE_SERV_ITEM, itemBean);
					params.add(itemBean.getList(Constant.PARAM_PRE_VALUES));
				}
			}
			if (psql != null) {
				count = Context.getExecutor().executeBatch(null, psql, params); // 批量插入列表数据
			}
			if (!servDef.getStr("SERV_KEYS").equals(actBean.getPKey())) { // 主键字段变更
				List<Object> preValue = new ArrayList<Object>();
				;
				Bean servBean = new Bean().set("SERV_KEYS", actBean.getPKey());
				servBean.setId(servId);
				ServDefBean syServDef = getServDef(TABLE_SERV);
				psql = Context.getBuilder().update(syServDef, servBean, preValue);
				Context.getExecutor().execute(null, psql, preValue);
			}
		}
		// 清除缓存
		clearServCache(servId);
		return count;
	}

	/**
	 * 替换字符串中所有的系统变量和时间日期变量。
	 * 
	 * @param src
	 *            需要被替换的系统变量
	 * @return 返回替换后的结果
	 */
	public static String replaceSysVars(String src) {
		return VarMgr.replaceSysVar(src);
	}

	/**
	 * 替换字符串中所有的系统变量及数据变量：@XX@（系统变量），#XX#（数据变量）。
	 * 
	 * @param src
	 *            需要被替换的系统变量
	 * @param dataBean
	 *            需要被替换内容的数据Bean
	 * @return 返回替换后的结果
	 */
	public static String replaceSysAndData(String src, Bean dataBean) {
		return BeanUtils.replaceValues(replaceSysVars(src), dataBean);
	}

	/**
	 * 根据服务数据拼装prepared sql where语句
	 * 
	 * @param servId
	 *            服务主键
	 * @param dataBean
	 *            参数信息
	 * @return 带and的where语句
	 */
	public static String where(String servId, Bean dataBean) {
		ServDefBean servDef = getServDef(servId);
		return Transaction.getBuilder().where(servDef, dataBean);
	}

	/**
	 * 生成组合字段的具体内容，支持流水号。
	 * 
	 * @param servId
	 *            服务主键
	 * @param itemCode
	 *            组合字段名
	 * @param pattern
	 *            组合规则，例如#SERV_ID#-#CMPY_CODE#-#_SN,4# _SN,4表示4位的流水自增
	 * @param dataBean
	 *            数据信息
	 * @return 新生成的组合字符串
	 */
	public static String genCombineItem(String servId, String itemCode, String pattern, Bean dataBean) {
		if ((itemCode.length() == 0) || (pattern.length() == 0)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		StringBuilder reg = new StringBuilder();
		String[] pats = pattern.split("#");
		int snSize = 0;
		String repPattern = "==<>==<>==";

		List<String> combs = new ArrayList<String>();
		for (int i = 0; i < pats.length; i++) {
			if ((i % 2) == 0) {
				sb.append(pats[i]);
				// reg.append(pats[i].replaceAll("([-_^])", "\\\\$1")); 暂时不用精准匹配
				reg.append(".*");
			} else if (pats[i].startsWith("_SN,")) { // 处理序列值
				snSize = Integer.parseInt(pats[i].substring(4));
				sb.append(repPattern);
				reg.append("(\\d{").append(snSize).append("})");
			} else { // 处理字段
				sb.append(dataBean.getStr(pats[i]));
				combs.add(pats[i]);
				reg.append(".{").append(dataBean.getStr(pats[i]).length()).append("}");
			}
		}
		String out = sb.toString();
		if (snSize > 0) { // 单独处理序列值，从数据库查询最大值
			Bean param = dataBean.copyOf(combs.toArray());
			param.set(Constant.PARAM_SELECT, "max(" + itemCode + ") MAX_").setId(""); // 清除ID保查询
			Bean queryBean = ServDao.find(servId, param);
			String max = queryBean.getStr("MAX_");
			boolean genMax;
			if ((dataBean.getStr(itemCode).length() > 0) && (dataBean.getStr(itemCode).equals(max))) {
				genMax = false;
			} else {
				genMax = true;
			}
			if (genMax) {
				String newMax = null;
				if (max.length() > 0) {
					Pattern pn = Pattern.compile(reg.toString());
					Matcher mat = pn.matcher(max);
					while (mat.find()) {
						newMax = Strings.fillDigit(Integer.parseInt(mat.group(1)) + 1, snSize);
					}
				}
				if (newMax == null) {
					newMax = Strings.fillDigit(1, snSize); // 如果没有最大值，直接从1开始生成
				}
				out = out.replaceAll(repPattern, newMax);
			} else {
				out = max;
			}
		}
		return out;
	}

	/**
	 * 判断唯一约束是否有已存在的数据
	 * 
	 * @param servDef
	 *            服务定义信息
	 * @param paramBean
	 *            参数信息
	 * @param createFlag
	 *            新建标志，是否为新记录
	 * @return 如果存在唯一约束，却数据存在，则返回约束的错误信息，如果不存在，则返回null
	 */
	public static String checkUniqueExists(ServDefBean servDef, Bean paramBean, boolean createFlag) {
		String uniqueStr = null;
		HashMap<Object, List<String>> uniqueMap = servDef.getUniqueGroups();
		if (uniqueMap != null) {
			StringBuilder sb = new StringBuilder(" and (");
			StringBuilder sbName = new StringBuilder("(");
			List<Object> params = new ArrayList<Object>();
			boolean first = true;
			for (Object key : uniqueMap.keySet()) {
				if (!first) {
					sb.append(" or ");
					sbName.append(") 或 ");
				} else {
					first = false;
				}
				sb.append("(");
				List<String> uList = uniqueMap.get(key);
				boolean bFirst = true;
				for (String code : uList) {
					if (!bFirst) {
						sb.append(" and ");
						sbName.append("、");
					} else {
						bFirst = false;
					}
					sb.append(code).append("=?");
					params.add(paramBean.get(code));
					Bean item = servDef.getItem(code);
					String value = paramBean.getStr(code);
					if (item.isNotEmpty("DICT_ID")) { // 字典项，动态处理名称
						value = DictMgr.getFullName(item.getStr("DICT_ID"), value);
					}
					sbName.append(servDef.getItemName(code)).append("=").append(value);
				}
				sbName.append(")");
				sb.append(")");
			}
			sb.append(")");
			if (!createFlag) { // 修改
				sb.append(" and ").append(servDef.getPKey()).append("!=?");
				params.add(paramBean.getId());
			}
			Bean queryBean = new Bean();
			queryBean.set(Constant.PARAM_WHERE, sb.toString());
			queryBean.set(Constant.PARAM_PRE_VALUES, params);
			if (ServDao.count(servDef.getId(), queryBean) > 0) {
				uniqueStr = sbName.toString();
			}
		}
		return uniqueStr;
	}

	/**
	 * 记录操作历史及时效
	 * 
	 * @param servDef
	 *            服务定义信息
	 * @param act
	 *            操作编码
	 * @param keys
	 *            主键列表，多个逗号分隔
	 * @param time
	 *            操作执行时间
	 */
	public static void actLog(ServDefBean servDef, String act, String keys, long time) {
		String actId = null;
		Bean actBean = servDef.getAct(act);
		if (actBean == null) {
			if (Context.appBoolean("LOG_ALL_SERV_ACT_TIME")) { // web.xml启用了系统所有服务操作跟踪
				actId = act;
			}
		} else if (actBean.getInt("ACT_LOG_FLAG") == Constant.YES_INT) { // 或者服务按钮定义启用的操作历史
			actId = actBean.getStr("ACT_ID");
		}
		if (actId != null) {
			CommonMsg msg = new CommonMsg(MsgCenter.ACTLOG_MSG_TYPE);
			msg.set("ACT_ID", actId);
			msg.set("ACT_KEYS", keys);
			msg.set("ACT_TIME", time);
			msg.set("SERV_ID", servDef.getId());
			msg.set("S_ATIME", DateUtils.getDatetimeTS());
			UserBean userBean = Context.getUserBean();
			if (userBean != null) { // 设置用户信息
				msg.set("S_DEPT", userBean.getDeptCode());
				msg.set("S_TDEPT", userBean.getTDeptCode());
				msg.set("S_ODEPT", userBean.getODeptCode());
				msg.set("S_USER", userBean.getCode()).set("S_CMPY", userBean.getCmpyCode()).set("ACT_IP",
						userBean.getCurrentIpAddress());
			}
			MsgCenter.getInstance().addMsg(msg);
		}
	}

	/**
	 * 记录数据项变更历史
	 * 
	 * @param changeList
	 *            变更项列表
	 */
	public static void itemLog(List<Bean> changeList) {
		String sql = null;
		List<List<Object>> params = new ArrayList<List<Object>>(changeList.size());
		UserBean userInfo = Context.getUserBean();
		String tm = DateUtils.getDatetimeTS();
		for (Bean data : changeList) {
			data.set("ILOG_ID", Lang.getUUID());
			data.set("S_MTIME", tm);
			if (userInfo != null) { // 设置用户信息
				data.set("S_USER", userInfo.getCode()).set("S_CMPY", userInfo.getCmpyCode()).set("ILOG_IP",
						userInfo.getCurrentIpAddress());
			}
			sql = Context.getBuilder().insertByBean("SY_SERV_LOG_ITEM", data);
			params.add(data.getList(Constant.PARAM_PRE_VALUES));
		}
		if (sql != null) {
			Context.getExecutor().executeBatch(null, sql, params);
		}
	}

	/**
	 * 初始化ITEM的缺省数据
	 * 
	 * @param itemBean
	 *            ITEM信息
	 */
	private static void initItem(Bean itemBean) {
		itemBean.set("ITEM_INPUT_TYPE", 1); // 文本框
		itemBean.set("ITEM_INPUT_MODE", 1); // 自动
		itemBean.set("ITEM_INPUT_FLAG", Constant.NO); // 非可选可输入
		itemBean.set("ITEM_HIDDEN", Constant.YES); // 非隐藏
		itemBean.set("ITEM_READONLY", Constant.NO); // 非只读
		itemBean.set("ITEM_CARD_DISABLE", Constant.NO); // 非卡片禁用
		itemBean.set("ITEM_LIST_WIDTH", 0); // 0为自动
		itemBean.set("ITEM_CARD_WIDTH", 0); // 卡片宽度,0为自动
		itemBean.set("ITEM_CARD_ROWS", 1); // 卡片行数
		itemBean.set("ITEM_CARD_COLS", 1); // 卡片列数
		itemBean.set("ITEM_SEARCH_FLAG", 1); // 搜索标志
		itemBean.set("ITEM_MOBILE_TYPE", 3); // 移动类型：移动卡片
		itemBean.set("ITEM_LIST_FLAG", Constant.NO); // 卡片展示
		itemBean.set("ITEM_LIST_EDIT", Constant.NO); // 列表编辑
		itemBean.set("ITEM_LOG_FLAG", Constant.NO); // 记录历史
		itemBean.set("S_FLAG", Constant.YES);
		itemBean.set("ITEM_CARD_ORDER", itemBean.get("ITEM_ORDER"));
		itemBean.set("ITEM_LIST_ORDER", itemBean.get("ITEM_ORDER"));
		if (itemBean.getStr("ITEM_FIELD_TYPE").equals(ServConstant.ITEM_FIELD_TYPE_NUM)) { // 数字类型
			if (itemBean.getStr("ITEM_FIELD_LENGTH").indexOf(",") > 0) {
				itemBean.set("ITEM_LIST_ALIGN", ServConstant.ITEM_LIST_ALIGN_LEFT); // 右对齐
			} else if (itemBean.getInt("ITEM_FIELD_LENGTH") > 4) {
				itemBean.set("ITEM_LIST_ALIGN", ServConstant.ITEM_LIST_ALIGN_LEFT); // 右对齐
			} else {
				itemBean.set("ITEM_LIST_ALIGN", ServConstant.ITEM_LIST_ALIGN_LEFT); // 左对齐
			}
		} else {
			itemBean.set("ITEM_LIST_ALIGN", ServConstant.ITEM_LIST_ALIGN_LEFT); // 缺省左对齐
		}
		itemBean.remove("ITEM_ORDER");
	}

	/**
	 * 根据服务主键获取服务定义数据信息
	 * 
	 * @param servId
	 *            服务主键
	 * @return 服务定义数据信息
	 */
	public static Bean getServData(String servId) {
		Bean servData = getServDataByFile(servId);
		if (servData == null) { // 尝试从数据库生成一份
			servData = ServUtils.toJsonFile(servId);
		}
		return servData;
	}

	/**
	 * 从JSON文本文件获取服务定义信息，先从文件定义的路径获取，如果不存在从资源文件中获取
	 * 
	 * @param servId
	 *            服务定义主键
	 * @return 服务定义信息
	 */
	public static Bean getServDataByFile(String servId) {
		Bean servDef = null;
		try {
			String fileName = JSON_PATH_SRC + servId + ".json";
			if (FileHelper.exists(fileName + "x")) { // 先取扩展定义
				servDef = FileHelper.fromJsonFile(fileName + "x");
			} else if (FileHelper.exists(fileName)) {
				servDef = FileHelper.fromJsonFile(fileName);
			} else { // 如果文件不存在从资源文件中获取
				servDef = Resource.getServ(servId);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return servDef;
	}

	/**
	 * 根据服务主键获取服务定义数据信息
	 * 
	 * @param servId
	 *            服务主键
	 * @return 服务定义数据信息
	 */
	public static Bean getServDataByDB(String servId) {
		String servSql = "select * from SY_SERV where SERV_ID=? and S_FLAG=1";
		String itemSql = "select * from SY_SERV_ITEM where SERV_ID=? order by ITEM_LIST_ORDER";
		String actSql = "select * from SY_SERV_ACT where SERV_ID=? order by ACT_ORDER";
		final String actParamSql = "select * from SY_SERV_ACT_PARAM where ACT_ID=? order by PARAM_ORDER";
		String whereSql = "select * from SY_SERV_WHERE where SERV_ID=? order by WHERE_ORDER";
		String querySql = "select * from SY_SERV_QUERY where SERV_ID=? order by QUERY_ORDER";
		String linkSql = "select * from SY_SERV_LINK where SERV_ID=? order by LINK_ORDER";

		final Bean paramBean = new Bean();
		final String linkItemSql = "select * from SY_SERV_LINK_ITEM where LINK_ID=?";
		List<Object> values = Lang.asList(servId);
		Bean serv = Context.getExecutor().queryById(null, servSql, values);
		if (serv == null) { // 如果不存在则直接返回
			return null;
		}
		serv.set(TABLE_SERV_ITEM, Context.getExecutor().query(null, itemSql, values));
		serv.set(TABLE_SERV_ACT, Context.getExecutor().query(null, actSql, values, new QueryCallback() { // 取所有子数据内容
			public void call(List<Bean> columns, Bean bean) {
				bean.set(TABLE_SERV_ACT_PARAM,
						Context.getExecutor().query(null, actParamSql, Lang.asList(bean.getId())));
			}
		}));
		serv.set(TABLE_SERV_WHERE, Context.getExecutor().query(null, whereSql, values));
		serv.set(TABLE_SERV_QUERY, Context.getExecutor().query(null, querySql, values));
		serv.set(TABLE_SERV_LINK, Context.getExecutor().query(null, linkSql, values, new QueryCallback() { // 取所有子数据内容
			public void call(List<Bean> columns, Bean bean) {
				if (bean.getInt("LINK_READONLY") != Constant.YES_INT) { // 进行级联处理
					paramBean.set("SERV_LINK_FLAG", Constant.YES);
				}
				bean.set(TABLE_SERV_LINK_ITEM,
						Context.getExecutor().query(null, linkItemSql, Lang.asList(bean.getId())));
			}
		}));
		if (paramBean.contains("SERV_LINK_FLAG")) {
			serv.set("SERV_LINK_FLAG", Constant.YES);
		}
		return serv;
	}

	/**
	 * 从文件读取所有服务的定义信息
	 * 
	 * @return 服务定义列表
	 */
	public static List<Bean> getFileServDataList() {
		return FileHelper.getJsonListByFile(JSON_PATH_SRC);
	}

	/**
	 * 清除服务定义对应的缓存，同时清除定义文件
	 * 
	 * @param servId
	 *            服务定义主键
	 */
	public static void clearServCache(String servId) {
		clearServCache(servId, true);
	}

	/**
	 * 清除服务定义对应的缓存
	 * 
	 * @param servId
	 *            服务定义主键
	 * @param fileFlag
	 *            是否生成定义文件
	 */
	public static void clearServCache(String servId, boolean fileFlag) {
		Bean servDef = (Bean) CacheMgr.getInstance().get(servId, CACHE_TYPE_SERV);
		if (fileFlag) {
			Transaction.commit(); // 提交已有的保存
			toJsonFile(servId);
		}
		if (servDef != null) {
			if (servDef.getInt("SERV_TYPE") == ServConstant.SERV_TYPE_PSERV) { // 如果为父服务，则清除所有子服务
				Bean param = new Bean();
				param.set("SERV_PID", servId);
				ServDao.findsCall(TABLE_SERV, param, new RowHandler() {
					public void handle(List<Bean> columns, Bean data) {
						clearServCache(data.getId(), false); // 清除子服务的缓存，但是不更新子服务的定义文件
					}
				});
			}
			CacheMgr.getInstance().remove(servId, CACHE_TYPE_SERV);
			ServiceMgr.remove(servId); // 清除webservice定义缓存
		}
	}

	/**
	 * 将一个服务定义生成为JSON文本文件
	 * 
	 * @param servId
	 *            服务定义主键
	 * @return 服务定义信息
	 */
	public static Bean toJsonFile(String servId) {
		Bean serv = null;
		try {
			serv = getServDataByDB(servId);
			if (serv != null) {
				String fileName = JSON_PATH_SRC + servId + ".json";
				// 如果是混合模式采用扩展定义
				if (serv.getInt("PRO_FLAG") == ServDef.PRO_FLAG_MIX) {
					fileName = fileName + "x";
				}
				FileHelper.toJsonFile(serv, fileName);

				if (Context.isDebugMode()) {
					if (Context.app(APP.PRO_SRC_DOC, "") != null && Context.app(APP.PRO_SRC_DOC, "").length() > 0) {
						String fileNameSrc = Context.app(APP.PRO_SRC_DOC, "") + "/SY_SERV/" + servId + ".json";
						// 如果是混合模式采用扩展定义
						if (serv.getInt("PRO_FLAG") == ServDef.PRO_FLAG_MIX) {
							fileNameSrc = fileNameSrc + "x";
						}
						FileHelper.toJsonFile(serv, fileNameSrc);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage() + servId, e);
		}
		return serv;
	}

	/**
	 * 将一个服务定义JSON文件删除
	 * 
	 * @param servId
	 *            服务定义主键
	 * @return 是否删除成功
	 */
	public static boolean deleteJsonFile(String servId) {
		String fileName = JSON_PATH_SRC + servId + ".json";
		if (FileHelper.exists(fileName + "x")) { // 有限判断是否为混合模式
			if (Context.app(APP.PRO_SRC_DOC, "") != null && Context.app(APP.PRO_SRC_DOC, "").length() > 0) {
				String fileNameSrc = Context.app(APP.PRO_SRC_DOC, "") + "/SY_SERV/" + servId + ".json";
				if (FileHelper.exists(fileNameSrc + "x")) {
					FileHelper.delete(fileNameSrc + "x");
				} else {
					FileHelper.delete(fileNameSrc);
				}
			}
			return FileHelper.delete(fileName + "x");
		} else {
			if (Context.app(APP.PRO_SRC_DOC, "") != null && Context.app(APP.PRO_SRC_DOC, "").length() > 0) {
				String fileNameSrc = Context.app(APP.PRO_SRC_DOC, "") + "/SY_SERV/" + servId + ".json";
				if (FileHelper.exists(fileNameSrc + "x")) {
					FileHelper.delete(fileNameSrc + "x");
				} else {
					FileHelper.delete(fileNameSrc);
				}
			}
			return FileHelper.delete(fileName);
		}
	}

	/**
	 * 删除数据对应的全文索引的全文索引
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataId
	 *            数据主键
	 */
	public static void deleteIndex(ServDefBean servDef, String dataId) {
		if (Context.getSyConf("SY_HUB_SEARCH", false) && servDef.getSearchFlag()) { // 启用了全文检索服务才进行索引处理
			Bean searchDef = servDef.getSearchDef();
			if ((searchDef != null) && !searchDef.isEmpty("SEARCH_TITLE")) { // 设置启用了全文搜索
				getIndexServ().deleteIndexMsg(servDef, dataId);
			}
		}
	}

	/**
	 * 索引增加用户权限
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataId
	 *            数据主键
	 * @param userCode
	 *            用户编码
	 */
	public static void updateIndexGrantUser(ServDefBean servDef, String dataId, String userCode) {
		if (Context.getSyConf("SY_HUB_SEARCH", false) && servDef.getSearchFlag()) { // 启用了全文检索服务才进行索引处理
			Bean searchDef = servDef.getSearchDef();
			if ((searchDef != null) && !searchDef.isEmpty("SEARCH_TITLE")) { // 设置启用了全文搜索
				getIndexServ().updateIndexGrantUser(servDef, dataId, userCode);
			}
		}
	}

	/**
	 * 索引增加角色权限
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataId
	 *            数据主键
	 * @param roleCode
	 *            角色编码
	 */
	public static void updateIndexGrantRole(ServDefBean servDef, String dataId, String roleCode) {
		if (Context.getSyConf("SY_HUB_SEARCH", false) && servDef.getSearchFlag()) { // 启用了全文检索服务才进行索引处理
			Bean searchDef = servDef.getSearchDef();
			if ((searchDef != null) && !searchDef.isEmpty("SEARCH_TITLE")) { // 设置启用了全文搜索
				getIndexServ().updateIndexGrantRole(servDef, dataId, roleCode);
			}
		}
	}

	/**
	 * 索引增加部门权限
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataId
	 *            数据主键
	 * @param deptCode
	 *            部门编码
	 */
	public static void updateIndexGrantDept(ServDefBean servDef, String dataId, String deptCode) {
		if (Context.getSyConf("SY_HUB_SEARCH", false) && servDef.getSearchFlag()) { // 启用了全文检索服务才进行索引处理
			Bean searchDef = servDef.getSearchDef();
			if ((searchDef != null) && !searchDef.isEmpty("SEARCH_TITLE")) { // 设置启用了全文搜索
				getIndexServ().updateIndexGrantDept(servDef, dataId, deptCode);
			}
		}
	}

	/**
	 * 对原始字符串中以##包含的字段名称进行替换，替换的值来自于数据bean中的数据，替换规则为键值对照。
	 * 例如src为：“你好，#TEST_NAME#”，bean中TEST_NAME键值为"world"，替换后为：“你好，world”
	 * 支持字典值转为名称的替换，字典名称替换统一为字段名+"__NAME"
	 * 
	 * @param src
	 *            需要被替换的字符串
	 * @param servId
	 *            服务定义主键
	 * @param dataBean
	 *            包含替换数据的bean
	 * @return 替换后的字符串
	 */
	public static String replaceValues(String src, String servId, Bean dataBean) {
		if ((src == null) || (src.length() == 0)) {
			return "";
		}
		if (dataBean == null) {
			return src;
		}
		Pattern pattern = Pattern.compile(BeanUtils.KEY_PATTERN, Pattern.CASE_INSENSITIVE); // 不区分大小写
		ServDefBean servDef = getServDef(servId);

		Matcher mt = pattern.matcher(src);
		StringBuffer sb = new StringBuffer();
		while (mt.find()) {
			String value = mt.group(1);
			int pos = value.indexOf("__NAME");
			if (pos > 0) {
				String name = value.substring(0, pos);
				Bean item = servDef.getItem(name);
				if ((item != null) && !item.isEmpty("DICT_ID")) { // 字典项替换名称
					value = DictMgr.getFullName(item.getStr("DICT_ID"), dataBean.getStr(name));
				}
				mt.appendReplacement(sb, value);
			} else {
				mt.appendReplacement(sb, dataBean.getStr(value));
			}
		}
		mt.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 保存数据的全文索引，先判断是否启用了全文索引，启用了再进行保存处理
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataBean
	 *            数据信息
	 */
	public static void saveIndex(ServDefBean servDef, Bean dataBean) {
		if (Context.getSyConf("SY_HUB_SEARCH", false) && servDef.getSearchFlag()) { // 启用了全文检索服务才进行索引处理
			Bean searchDef = servDef.getSearchDef();
			if ((searchDef != null) && !searchDef.isEmpty("SEARCH_TITLE")) { // 设置启用了全文搜索
				getIndexServ().saveIndexMsg(servDef, dataBean);
			}
		}
	}

	/**
	 * 获取索引分组字段列表，多个逗号分隔
	 * 
	 * @param servDef
	 *            服务定义
	 * @return 分组字段列表
	 */
	public static String getIndexGroupItmes(ServDefBean servDef) {
		String group;
		Bean searchDef = servDef.getSearchDef();
		if (searchDef != null) { // 设置启用了全文搜索
			group = searchDef.getStr("SEARCH_INDEX");
		} else {
			group = "";
		}
		return group;
	}

	/**
	 * 获取搜索关联定义信息
	 * 
	 * @param servDef
	 *            服务定义
	 * @param linkServId
	 *            关联服务主键
	 * @return 关联检索定义
	 */
	public static Bean getSearchLinkDef(ServDefBean servDef, String linkServId) {
		Bean linkDef = null;
		Bean searchDef = servDef.getSearchDef();
		List<Bean> linkList = searchDef.getList("SY_PLUG_SEARCH_LINK");
		for (Bean link : linkList) {
			if (link.getStr("LINK_SERV_ID").equals(linkServId)) {
				linkDef = link;
				break;
			}
		}
		return linkDef;
	}

	/**
	 * 执行索引处理
	 * 
	 * @param search
	 *            检索定义信息
	 * @param count
	 *            要求索引最大数量，0为不限制数量
	 * @return 实际索引数据量
	 */
	public static int doIndex(final Bean search, int count) {
		if (search.isEmpty("SEARCH_TITLE")) {
			return 0;
		}
		SqlBean param = new SqlBean();
		param.appendWhere(search.getStr("SEARCH_WHERE"));
		String servId = search.getStr("SERV_ID");
		final ServDefBean serv = ServUtils.getServDef(servId);
		StringBuilder select = new StringBuilder(serv.getPKey());
		String order = search.getStr("SEARCH_ORDER").toUpperCase();

		String lastItem = search.getStr("SEARCH_LAST_FIELD");
		Bean item = serv.getItem(lastItem);
		String type = item.getStr("ITEM_FIELD_TYPE");
		if (!search.isEmpty("SEARCH_LAST_DATA")) {
			Object value;
			if (ServConstant.ITEM_FIELD_TYPE_TIME.equals(type)) {
				value = DateUtils.getTimestamp(search.getStr("SEARCH_LAST_DATA"));
			} else if (ServConstant.ITEM_FIELD_TYPE_DATE.equals(type)) {
				value = DateUtils.getDateFromString(search.getStr("SEARCH_LAST_DATA"));
			} else {
				value = search.getStr("SEARCH_LAST_DATA");
			}
			String symbol;
			if (order.indexOf("DESC") >= 0) { // 支持正序索引以及倒叙索引
				symbol = "<";
			} else {
				symbol = ">";
			}
			param.and(lastItem, symbol, value);
		}
		select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_TITLE")));
		if ((search.getInt("SEARCH_CONT_TYPE") == ServConstant.SEARCH_CONT_FIELD)
				&& (!search.isEmpty("SEARCH_CONT_FIELD"))) { // 正文字段
			select.append(",").append(search.getStr("SEARCH_CONT_FIELD"));
		}
		if (!search.isEmpty("SEARCH_INDEX")) { // 分组字段
			select.append(",").append(search.getStr("SEARCH_INDEX"));
		}
		if (!search.isEmpty("SEARCH_TIME")) { // 时间字段
			select.append(",").append(search.getStr("SEARCH_TIME"));
		}
		if (!search.isEmpty("SEARCH_USER")) { // 用户字段
			select.append(",").append(search.getStr("SEARCH_USER"));
		}
		if (!search.isEmpty("SEARCH_DEPT")) { // 部门字段
			select.append(",").append(search.getStr("SEARCH_DEPT"));
		}
		if (!search.isEmpty("SEARCH_CMPY")) { // 公司字段
			select.append(",").append(search.getStr("SEARCH_CMPY"));
		}
		if (!search.isEmpty("SEARCH_ALL_CONDITION")) { // 全部可看规则
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_ALL_CONDITION")));
		}
		if (!search.isEmpty("SEARCH_CMPY_CONDITION")) { // 公司内可看规则
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_CMPY_CONDITION")));
		}
		if (!search.isEmpty("SEARCH_USER_SQL")) { // 权限-用户
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_USER_SQL")));
		}
		if (!search.isEmpty("SEARCH_GROUP_SQL")) { // 权限-群组
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_GROUP_SQL")));
		}
		if (!search.isEmpty("SEARCH_DEPT_SQL")) { // 权限-部门
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_DEPT_SQL")));
		}
		if (!search.isEmpty("SEARCH_ROLE_SQL")) { // 权限-角色
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_ROLE_SQL")));
		}
		if (!search.isEmpty("SEARCH_DEPT_ROLE_SQL")) { // 权限-部门角色
			select.append(",").append(BeanUtils.getFieldCodes(search.getStr("SEARCH_DEPT_ROLE_SQL")));
		}
		String filePathField = BeanUtils.getFieldCodes(search.getStr("SEARCH_FILE_SQL"));
		if (filePathField.length() > 0) { // 附件sql
			select.append(",").append(filePathField);
		}
		if (!search.isEmpty("SEARCH_OTHER_FIELDS")) { // 其他字段
			select.append(",").append(search.getStr("SEARCH_OTHER_FIELDS"));
		}

		// param.selects(Strings.removeSame(select.toString()));
		param.orders(order);
		if (count > 0) {
			param.set(Constant.PARAM_ROWNUM, count); // 设定了每次取指定条目数
		}
		if (!serv.isEmpty("SERV_DATA_SOURCE")) {
			Transaction.begin(serv.getStr("SERV_DATA_SOURCE"));
		}
		if (!search.isEmpty("SEARCH_CLASS")) { // 处理检索监听类
			try {
				IndexListener iListener = (IndexListener) Lang.createObject(search.getStr("SEARCH_CLASS"));
				search.set("SEARCH_CLASS_INST", iListener);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		try {
			search.set("SEARCH_LAST_COUNT", 0); // 初始化检索设定
			ServDao.finds(servId, param, new QueryCallback() {
				public void call(List<Bean> columns, Bean data) {
					try {
						String cmpy = search.getStr("SEARCH_CMPY");
						if ((cmpy.length() > 0) && (!data.isEmpty(cmpy))) { // 设置公司确保字段获取正确
							Context.setThread(Context.THREAD.CMPYCODE, data.getStr(cmpy));
						}
						for (Bean column : columns) { // 预处理字典数据
							String name = column.getStr("NAME");
							Bean item = serv.getItem(name);
							if (item != null && !item.isEmpty("DICT_ID")) { // 数据字典项
								data.set(name, DictMgr.getFullNames(item.getStr("DICT_ID"), data.getStr(name)));
							}
						}
						saveIndex(serv, data);
						search.set("SEARCH_LAST_DATA", data.getStr(search.getStr("SEARCH_LAST_FIELD")));
						search.set("SEARCH_LAST_COUNT", data.getInt(Constant.PARAM_ROWNUM) + 1);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (!serv.isEmpty("SERV_DATA_SOURCE")) {
				Transaction.end();
			}
		}
		count = search.getInt("SEARCH_LAST_COUNT");
		if (count > 0) { // 执行了索引处理，动态更新最后索引时间
			// 更新上次索引时间，便于增量索引
			Bean servDef = new Bean(servId);
			servDef.set("SEARCH_LAST_DATA", search.getStr("SEARCH_LAST_DATA"));
			servDef.set("SEARCH_LAST_COUNT", search.getStr("SEARCH_LAST_COUNT"));
			servDef.set("SEARCH_ALL_COUNT", search.getInt("SEARCH_ALL_COUNT") + search.getInt("SEARCH_LAST_COUNT"));
			ServDao.update(ServDefSearch.SERV_ID_SEARCH, servDef);
		}
		return count;
	}

	/**
	 * 将数据中字典项数据的值替换为字典对应的值
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataBean
	 *            数据信息
	 * @return 替换后的数据信息
	 */
	public static Bean dictToName(ServDefBean servDef, Bean dataBean) {
		Bean newBean = new Bean();
		Set<?> keys = dataBean.keySet();
		for (Object key : keys) {
			String name = String.valueOf(key);
			Bean item = servDef.getItem(name);
			if (item != null && !item.isEmpty("DICT_ID")) { // 数据字典项
				newBean.set(name, DictMgr.getFullNames(item.getStr("DICT_ID"), dataBean.getStr(name)));
			} else {
				newBean.set(name, dataBean.getStr(name));
			}
		}
		if (dataBean.getId().length() > 0) {
			newBean.setId(dataBean.getId());
		}
		return newBean;
	}

	/**
	 * 得到某条记录指定关联定义下的过滤条件
	 * 
	 * @param servId
	 *            当前服务主键
	 * @param linkDef
	 *            关联定义信息
	 * @param dataBean
	 *            当前数据信息
	 * @param delFlag
	 *            主服务真删除标志，如果启用真删除，子服务不再动态过滤S_FLAG
	 * @return 关联过滤sql
	 */
	public static String getLinkSql(String servId, Bean linkDef, Bean dataBean, boolean delFlag) {
		StringBuilder where = new StringBuilder();
		ServDefBean linkDefBean = ServUtils.getServDef(linkDef.getStr("LINK_SERV_ID"));
		if (delFlag && linkDefBean.hasFalseDelete()) { // 子数据启用了假删除
			where.append(" and S_FLAG=1");
		}
		if (linkDef.getInt("LINK_SHOW_TYPE") != ServConstant.LINK_SHOW_TYPE_URL) {
			where.append(replaceSysAndData(linkDef.getStr("LINK_WHERE"), dataBean)); // 特定where
		}

		Bean param = new Bean();
		List<Bean> linkItems = linkDef.getList("SY_SERV_LINK_ITEM");
		if (linkItems == null) {
			return where.toString();
		}
		for (Bean item : linkItems) {
			if (item.getInt("LINK_WHERE_FLAG") == Constant.YES_INT) { // 过滤条件
				if (item.getInt("LINK_VALUE_FLAG") == Constant.YES_INT) { // 字段传值
					param.set(item.get("LINK_ITEM_CODE"), dataBean.get(item.get("ITEM_CODE")));
				} else { // 传常量值
					param.set(item.get("LINK_ITEM_CODE"), item.get("ITEM_CODE"));
				}
			}
		}
		where.append(where(linkDef.getStr("LINK_SERV_ID"), param)); // 增补上关联明细拼接的where
		return where.toString();
	}

	/**
	 * 得到某条记录指定关联定义下对应的主键列表
	 * 
	 * @param servId
	 *            当前服务主键
	 * @param linkDef
	 *            关联定义信息
	 * @param dataBean
	 *            当前数据信息
	 * @param delFlag
	 *            主服务是否真删除，true则子服务如果为假删除，则忽略掉S_FLAG为1的过滤条件
	 * @return 对应关联主键列表
	 */
	public static List<String> getLinkDataIds(String servId, Bean linkDef, Bean dataBean, boolean delFlag) {
		String linkWhere = getLinkSql(servId, linkDef, dataBean, delFlag);
		final List<String> ids = new ArrayList<String>();
		if (linkWhere.length() > 0) { // 必须有过滤条件设定
			ServDefBean servDef = getServDef(linkDef.getStr("LINK_SERV_ID"));
			StringBuilder sql = new StringBuilder("select ");
			sql.append(servDef.getPKey()).append(" from ").append(servDef.getTableView()).append(" where 1=1 ")
					.append(linkWhere);
			Transaction.getExecutor().queryCall(sql.toString(), null, new RowHandler() {
				public void handle(List<Bean> columns, Bean data) {
					ids.add(data.getId());
				}
			});
		}
		return ids;
	}

	/**
	 * 得到某条记录指定关联定义下对应的数据列表
	 * 
	 * @param servId
	 *            当前服务主键
	 * @param linkDef
	 *            关联定义信息
	 * @param dataBean
	 *            当前数据信息
	 * @param level
	 *            层级
	 * @return 对应关联列表数据
	 */
	public static List<Bean> getLinkDataList(String servId, Bean linkDef, Bean dataBean, int level) {
		String linkWhere = getLinkSql(servId, linkDef, dataBean, false);
		List<Bean> dataList;
		if (linkWhere.length() > 0) { // 必须有过滤条件设定
			ParamBean param = new ParamBean();
			// 设置where条件及级联参数（强制级联查询）
			param.set(Constant.PARAM_WHERE, linkWhere).setLinkFlag(true).set(Constant.PARAM_LINK_LEVEL, level + 1);
			Bean outBean = ServMgr.act(linkDef.getStr("LINK_SERV_ID"), ServMgr.ACT_FINDS, param);
			dataList = outBean.getList(Constant.RTN_DATA);
		} else {
			dataList = new ArrayList<Bean>();
		}
		return dataList;
	}

	/**
	 * 得到前端提交的查询过滤条件，支持字典名称和编码的自动转换
	 * 
	 * @param servDef
	 *            服务定义
	 * @param searchWhere
	 *            查询过滤条件
	 * @return 过滤条件
	 */
	public static String getSearchWhere(ServDefBean servDef, String searchWhere) {
		if (searchWhere.length() == 0) { // 不存在字典过滤，直接返回
			return searchWhere;
		} else {
			if (searchWhere.indexOf("@@") >= 0) {
				String pn = "@@(.+)@(.+)@@";
				Pattern pattern = Pattern.compile(pn);
				Matcher mt = pattern.matcher(searchWhere);
				while (mt.find()) {
					Bean item = servDef.getItem(mt.group(1));
					if (item != null && !item.isEmpty("DICT_ID")) {
						String dictWhere = DictMgr.getDictLikeSql(item.getStr("DICT_ID"), mt.group(2));
						if (dictWhere.length() > 0) {
							dictWhere = mt.group(1) + " in (" + dictWhere + ")";
						} else {
							dictWhere = "1=2";
						}
						searchWhere = searchWhere.replace(mt.group(0), dictWhere);
					}
				}
			}
			if (searchWhere.indexOf("@") >= 0) {
				searchWhere = replaceSysVars(searchWhere);
			}
		}
		return searchWhere;
	}

	/**
	 * 得到前端提交的查询过滤条件，支持字典名称和编码的自动转换
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dictList
	 *            导航选中信息
	 * @return 过滤条件
	 */
	public static String getTreeWhere(ServDefBean servDef, List<Bean> dictList) {
		StringBuilder sb = new StringBuilder();
		for (Bean item : dictList) {
			String itemCode = item.getStr("DICT_ITEM");
			String value = item.getStr("DICT_VALUE");
			boolean withSubs = item.get("DICT_SUBS", true);
			Bean itemDef = servDef.getItem(itemCode);
			if (itemDef != null && !itemDef.isEmpty("DICT_ID")) {
				Bean dict = DictMgr.getDict(itemDef.getStr("DICT_ID"));
				if (withSubs && DictMgr.isSingleTree(dict) && dict.isNotEmpty("DICT_F_PATH")) { // 查询所有子
					String where = DictMgr.getDictSubSql(dict, value);
					if (where.length() > 0) {
						sb.append(" and ").append(itemCode).append(" in (").append(where).append(")");
					}
				} else {
					sb.append(" and ").append(itemCode).append("='").append(value).append("'");
				}
			} else {
				sb.append(" and ").append(itemCode).append("='").append(value).append("'");
			}
		}
		return sb.toString();
	}

	/**
	 * 根据服务设定的父服务主键，将子服务与父服务的定义进行合并
	 * 
	 * @param servDef
	 *            服务定义
	 */
	private static void appendParent(Bean servDef) {
		// 获取父的相关定义信息
		Bean pDef = getServDef(servDef.getStr("SERV_PID"));
		List<Bean> defaultList = new ArrayList<Bean>();
		List<Bean> pitemList = pDef.get(TABLE_SERV_ITEM, defaultList);
		List<Bean> pactList = pDef.get(TABLE_SERV_ACT, defaultList);
		List<Bean> plinkList = pDef.get(TABLE_SERV_LINK, defaultList);
		List<Bean> pwhereList = pDef.get(TABLE_SERV_WHERE, defaultList);
		List<Bean> pqueryList = pDef.get(TABLE_SERV_QUERY, defaultList);
		// 获取当前服务的定义信息
		List<Bean> itemList = servDef.get(TABLE_SERV_ITEM, defaultList);
		List<Bean> actList = servDef.get(TABLE_SERV_ACT, defaultList);
		List<Bean> whereList = servDef.get(TABLE_SERV_WHERE, defaultList);
		List<Bean> linkList = servDef.get(TABLE_SERV_LINK, defaultList);
		List<Bean> queryList = servDef.get(TABLE_SERV_QUERY, defaultList);
		servDef.set(TABLE_SERV_ITEM, BeanUtils.mergeList(pitemList, itemList, "ITEM_CODE", "ITEM_LIST_ORDER"));
		servDef.set(TABLE_SERV_ACT, BeanUtils.mergeList(pactList, actList, "ACT_CODE", "ACT_ORDER"));
		servDef.set(TABLE_SERV_WHERE, BeanUtils.mergeList(pwhereList, whereList, "WHERE_NAME", "WHERE_ORDER"));
		servDef.set(TABLE_SERV_LINK, BeanUtils.mergeList(plinkList, linkList, "LINK_SERV_ID", "LINK_ORDER"));
		servDef.set(TABLE_SERV_QUERY, BeanUtils.mergeList(pqueryList, queryList, "QUERY_NAME", "QUERY_ORDER"));
		if (servDef.isEmpty("SERV_CLASS") && (!pDef.isEmpty("SERV_CLASS"))) { // 继承父的类定义
			servDef.set("SERV_CLASS", pDef.get("SERV_CLASS"));
		}
		if (servDef.isEmpty("SERV_KEYS") && (!pDef.isEmpty("SERV_KEYS"))) {
			servDef.set("SERV_KEYS", pDef.get("SERV_KEYS"));
		}
		if (servDef.isEmpty("SERV_SRC_ID") && (!pDef.isEmpty("SERV_SRC_ID"))) {
			servDef.set("SERV_SRC_ID", pDef.get("SERV_SRC_ID"));
		}
		if (servDef.isEmpty("TABLE_VIEW") && (!pDef.isEmpty("TABLE_VIEW"))) {
			servDef.set("TABLE_VIEW", pDef.get("TABLE_VIEW"));
		}
		if (servDef.isEmpty("TABLE_ACTION") && (!pDef.isEmpty("TABLE_ACTION"))) {
			servDef.set("TABLE_ACTION", pDef.get("TABLE_ACTION"));
		}
		if (servDef.isEmpty("SERV_CARD_JSP") && (!pDef.isEmpty("SERV_CARD_JSP"))) {
			servDef.set("SERV_CARD_JSP", pDef.get("SERV_CARD_JSP"));
		}
		if (servDef.isEmpty("SERV_DICT_CACHE") && (!pDef.isEmpty("SERV_DICT_CACHE"))) {
			servDef.set("SERV_DICT_CACHE", pDef.get("SERV_DICT_CACHE"));
		}
		if (servDef.isEmpty("SERV_SQL_WHERE") && (!pDef.isEmpty("SERV_SQL_WHERE"))) {
			servDef.set("SERV_SQL_WHERE", pDef.get("SERV_SQL_WHERE"));
		}
		if (servDef.isEmpty("SERV_SQL_ORDER") && (!pDef.isEmpty("SERV_SQL_ORDER"))) {
			servDef.set("SERV_SQL_ORDER", pDef.get("SERV_SQL_ORDER"));
		}
		if (servDef.isEmpty("SERV_LIST_STYLE") && (!pDef.isEmpty("SERV_LIST_STYLE"))) {
			servDef.set("SERV_LIST_STYLE", pDef.get("SERV_LIST_STYLE"));
		}
		if (servDef.isEmpty("SERV_MOBILE_LIST") && (!pDef.isEmpty("SERV_MOBILE_LIST"))) {
			servDef.set("SERV_MOBILE_LIST", pDef.get("SERV_MOBILE_LIST"));
		}
		if (servDef.isEmpty("SERV_PAGE_COUNT") && (!pDef.isEmpty("SERV_PAGE_COUNT"))) {
			servDef.set("SERV_PAGE_COUNT", pDef.get("SERV_PAGE_COUNT"));
		}
		if (servDef.isEmpty("SERV_LINK_FLAG") && (!pDef.isEmpty("SERV_LINK_FLAG"))) {
			servDef.set("SERV_LINK_FLAG", pDef.get("SERV_LINK_FLAG"));
		}
		if (servDef.isEmpty("SERV_NAV_ITEMS") && (!pDef.isEmpty("SERV_NAV_ITEMS"))) {
			servDef.set("SERV_NAV_ITEMS", pDef.get("SERV_NAV_ITEMS"));
		}
		if (servDef.isEmpty("SERV_DATA_SOURCE") && (!pDef.isEmpty("SERV_DATA_SOURCE"))) {
			servDef.set("SERV_DATA_SOURCE", pDef.get("SERV_DATA_SOURCE"));
		}
		if (servDef.isEmpty("SERV_MEMO") && (!pDef.isEmpty("SERV_MEMO"))) {
			servDef.set("SERV_MEMO", pDef.get("SERV_MEMO"));
		}
		if (servDef.isEmpty("SERV_JS") && (!pDef.isEmpty("SERV_JS"))) {
			servDef.set("SERV_JS", pDef.get("SERV_JS"));
		}
		if (servDef.isEmpty("SERV_DATA_TITLE") && (!pDef.isEmpty("SERV_DATA_TITLE"))) {
			servDef.set("SERV_DATA_TITLE", pDef.get("SERV_DATA_TITLE"));
		}
		if (pDef.getInt("SERV_CARD_TMPL") == Constant.YES_INT) {
			servDef.set("SERV_CARD_TMPL", Constant.YES);
			servDef.set("SERV_CARD_TMPL_NAME", pDef.getStr("SERV_CARD_TMPL_NAME"));
		}
		// JS层层继承父是否启用了JS设定，将所有启用的服务变量逗号分隔提供给前端
		StringBuilder jsNames = new StringBuilder(pDef.getStr("SERV_CARD_LOAD_NAMES"));
		if (servDef.getInt("SERV_CARD_LOAD") == Constant.YES_INT) {
			if (jsNames.length() > 0) {
				jsNames.append(",");
			}
			jsNames.append(servDef.getId());
		}
		servDef.set("SERV_CARD_LOAD_NAMES", jsNames.toString());
		jsNames = new StringBuilder(pDef.getStr("SERV_LIST_LOAD_NAMES"));
		if (servDef.getInt("SERV_LIST_LOAD") == Constant.YES_INT) {
			if (jsNames.length() > 0) {
				jsNames.append(",");
			}
			jsNames.append(servDef.getId());
		}
		servDef.set("SERV_LIST_LOAD_NAMES", jsNames.toString());
		if (pDef.getInt("SERV_CACHE_FLAG") == Constant.YES_INT) { // 继承父的缓存启用设定
			servDef.set("SERV_CACHE_FLAG", Constant.YES);
		}
		if (pDef.getInt("SERV_DELETE_FLAG") == Constant.YES_INT) { // 继承父的假删除设定
			servDef.set("SERV_DELETE_FLAG", Constant.YES);
		}
		if (pDef.getInt("SERV_SEARCH_FLAG") == Constant.YES_INT) { // 继承父的全文检索设定
			servDef.set("SERV_SEARCH_FLAG", Constant.YES);
		}
		if (servDef.isEmpty("SERV_DATA_TITLE") && (pDef.isNotEmpty("SERV_DATA_TITLE"))) {
			servDef.set("SERV_DATA_TITLE", pDef.get("SERV_DATA_TITLE"));
		}
		if (servDef.isEmpty("SERV_DATA_CODE") && (pDef.isNotEmpty("SERV_DATA_CODE"))) {
			servDef.set("SERV_DATA_CODE", pDef.get("SERV_DATA_CODE"));
		}
		if (servDef.isEmpty("TODO_TYPE") && (pDef.isNotEmpty("TODO_TYPE"))) {
			servDef.set("TODO_TYPE", pDef.get("TODO_TYPE"));
		}
	}

	/**
	 * 删除数据对应的文件信息（包含实体文件）
	 * 
	 * @param servDef
	 *            服务定义
	 * @param dataId
	 *            数据主键
	 */
	public static void deleteFile(ServDefBean servDef, String dataId) {
		ParamBean param = new ParamBean(ServMgr.SY_COMM_FILE, ServMgr.ACT_DELETE);
		param.set("SERV_ID", servDef.getSrcId());
		param.set("DATA_ID", dataId);
		ServMgr.act(param);
	}

	/**
	 * 修改更新时间
	 * 
	 * @param id
	 *            服务编码
	 * @param mtime
	 *            更新时间
	 */
	public static void udpateMtime(String id, String mtime) {
		if (mtime == null || mtime.isEmpty()) {
			mtime = DateUtils.getDatetimeTS();
		}
		ParamBean param = new ParamBean(ServMgr.SY_SERV, ServMgr.ACT_SAVE);
		param.setId(id).set("S_MTIME", mtime);
		ServMgr.act(param);
	}

	/**
	 * 获取索引服务对象
	 * 
	 * @return 索引服务对象
	 */
	public static IIndexServ getIndexServ() {
		return (IIndexServ) Lang.createObject(IIndexServ.class,
				Context.getInitConfig("rh.index", "com.rh.opt.plug.search.RhIndexServ"));
	}
}
