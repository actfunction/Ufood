package com.rh.core.serv.dict;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.RowHandler;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.CacheMgr;
import com.rh.core.org.auth.acl.mgr.DataAclMgr;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDef;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.file.FileHelper;
import com.rh.core.util.lang.ListHandler;
import com.rh.resource.Resource;

/**
 * 数据字典帮助类.
 * 
 * @author wanglong
 * 
 */
public class DictMgr {
	/**
	 * 字典ID字段
	 */
	protected static final String COL_DICT_ID = "DICT_ID";
	private static final String COL_S_PUBLIC = "S_PUBLIC";
	private static final String COL_TABLE_ORDER = "TABLE_ORDER";
	private static final String COL_TABLE_WHERE = "TABLE_WHERE";
	private static final String COL_TABLE_ID = "TABLE_ID";
	private static final String COL_TABLE_SELECT = "TABLE_SELECT";
	private static final String COL_DICT_F_CMPY = "DICT_F_CMPY";
	private static final String COL_DICT_F_PARENT = "DICT_F_PARENT";

	protected static final String COL_DICT_TYPE = "DICT_TYPE";
	private static final String COL_DICT_F_NAME = "DICT_F_NAME";
	private static final String COL_DICT_F_ID = "DICT_F_ID";
	private static final String DICT_CLASS = "_DICT_CLASS";

	/** log. */
	private static Log log = LogFactory.getLog(DictMgr.class);

	/** 表名称：字典. */
	private static final String TABLE_DICT = "SY_SERV_DICT";
	/** 表名称：内部字典数据. */
	private static final String TABLE_DICT_ITEM = "SY_SERV_DICT_ITEM";
	/** 公共公司编码 */
	public static final String PUBLIC_CMPY = "";
	/** 子节点名称. */
	public static final String CHILD_NODE = "CHILD";
	/** 子节点MAP名称. */
	public static final String CHILD_NODE_MAP = "CHILD_MAP";
	/** 字典类型:列表. */
	public static final int DIC_TYPE_LIST = 1;
	/** 字典类型:父子形 . */
	public static final int DIC_TYPE_TREE = 2;

	/** 上下文，设置字典过滤条件 */
	public static final String THREAD_DICT_EXT_WHERE = "THREAD_DICT_EXT_WHERE";

	/** 字典类型:叶子. */
	public static final int DIC_TYPE_LEAF = 4;
	/** 缓存类型:字典. */
	private static final String CACHE_DICT = "_CACHE_SY_SERV_DICT";
	/** 缓存类型:字典. */
	private static final String CACHE_PRE_CMPY = "_CACHE_C_";
	/** 缓存类型:待清除的字典. */
	private static final String CACHE_TO_REBUILD = "_CACHE_TO_REBUILD";
	/** json src path */
	private static final String JSON_PATH_SRC = FileHelper.getJsonPath() + "/SY_SERV_DICT/";

	/**
	 * 根据字典ID获取字典定义信息
	 * 
	 * @param dictId
	 *            字典ID
	 * @return 字典定义信息
	 */
	public static Bean getDictDef(String dictId) {
		return getDictDefByFile(dictId);
	}

	/**
	 * 根据字典ID获取字典定义信息，先从文件定义的路径获取，如果不存在从资源文件中获取
	 * 
	 * @param dictId
	 *            字典ID
	 * @return 字典定义信息
	 */
	public static Bean getDictDefByFile(String dictId) {
		Bean dict = null;
		try {
			String fileName = JSON_PATH_SRC + dictId + ".json";
			if (FileHelper.exists(fileName + "x")) { // 优先取扩展定义
				dict = FileHelper.fromJsonFile(fileName + "x");
			} else if (FileHelper.exists(fileName)) {
				dict = FileHelper.fromJsonFile(fileName);
			} else {
				dict = Resource.getDict(dictId);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return dict;
	}

	/**
	 * 根据字典ID获取字典定义信息
	 * 
	 * @param dictId
	 *            字典ID
	 * @return 字典定义信息
	 */
	public static Bean getDictDefByDB(String dictId) {
		Bean dict = null;
		String sql = "select * from " + TABLE_DICT + " where DICT_ID='" + dictId + "' and S_FLAG=1";
		Connection conn = Context.getConn();
		try {
			dict = Context.getExecutor().queryOne(conn, sql);
			if (dict == null) {
				log.error(Context.getSyMsg("SY_SERV_DICT_NOT_FOUND", dictId));
			} else if (dict.getInt("DICT_IS_INNER") == Constant.YES_INT) { // 内部字典提前处理数据信息
				sql = "select * from " + TABLE_DICT_ITEM + " where DICT_ID='" + dictId
						+ "' and ITEM_FLAG = 1 Order by ITEM_LEVEL, ITEM_ORDER";
				dict.set(TABLE_DICT_ITEM, Context.getExecutor().query(conn, sql));
			}
		} finally {
			Context.endConn(conn);
		}
		return dict;
	}

	/**
	 * 根据字典ID获取字典.
	 * 
	 * @param dictId
	 *            字典ID
	 * @return 数据字典树
	 */
	public static Bean getDict(String dictId) {
		if (StringUtils.isEmpty(dictId)) {
			return null;
		}
		Bean dict = (Bean) CacheMgr.getInstance().get(dictId, CACHE_DICT);
		// 如果在缓存中取不到，则重新从数据库中读取
		if (dict == null) {
			dict = getDictDef(dictId);
			if (dict != null) {
				if (dict.isNotEmpty("DICT_CLASS")) {
					try {
						DictListener lsnr = (DictListener) Lang.createObject(DictListener.class,
								dict.getStr("DICT_CLASS"));
						dict.set(DICT_CLASS, lsnr);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
				setToCache(dictId, dict);
			}
		}
		return dict;
	}

	/**
	 * 根据字典信息获取公司对应数据，支持公共数据和公司数据（私有字典要求线程变量中有公司编码）
	 * 
	 * @param dict
	 *            字典信息
	 * @return 字典数据
	 */
	public static Bean getItemCmpyBean(Bean dict) {
		String cmpy = (dict.getInt("S_PUBLIC") == Constant.YES_INT) ? PUBLIC_CMPY : Context.getCmpy();
		return getItemCmpyBean(cmpy, dict);
	}

	/**
	 * @param cmpy
	 *            公司CODE
	 * @param dict
	 *            字典定义对象
	 * @return 指定公司的数据字典对象（包含字典数据）
	 */
	private static Bean getItemCmpyBean(String cmpy, Bean dict) {
		Bean cmpyBean = null;
		boolean bCache;
		// 字典设置了禁用缓存或者指定了扩展where条件,不进行缓存处理
		if ((dict.getInt("DICT_LOAD_TYPE") == Constant.NO_INT) || !Context.isEmpytyThread(THREAD_DICT_EXT_WHERE)) { // =======临时使用的字典
			bCache = false;
		} else {
			bCache = true;
		}
		if (bCache) {
			if (dict.isNotEmpty("DICT_SRC_ID")) { // 启用了引用数据，则获取对应字典的数据
				Bean dictSrc = getDict(dict.getStr("DICT_SRC_ID"));
				if (dictSrc != null) {
					cmpyBean = getItemCmpyBean(dictSrc);
				}
			} else {
				cmpyBean = (Bean) CacheMgr.getInstance().get(cmpy, CACHE_PRE_CMPY + dict.getId());
			}
		}
		if (cmpyBean == null) { // 数据没有初始化
			cmpyBean = loadItem(dict, cmpy);
			if (bCache) {
				CacheMgr.getInstance().set(cmpy, cmpyBean, CACHE_PRE_CMPY + dict.getId()); // 设置缓存
			}
		}
		return cmpyBean;
	}

	/**
	 * 获取字典对应的列表数据
	 * 
	 * @param dict
	 *            字典信息
	 * @return 列表数据
	 */
	public static List<Bean> getItemList(Bean dict) {
		return getItemCmpyBean(dict).getList(CHILD_NODE);
	}

	/**
	 * 
	 * @param cmpy
	 *            公司CODE
	 * @param dict
	 *            字典定义对象
	 * @return 字典列表数据
	 */
	public static List<Bean> getItemList(String cmpy, Bean dict) {
		return getItemCmpyBean(cmpy, dict).getList(CHILD_NODE);
	}

	/**
	 * 获取字典对应的列表数据
	 * 
	 * @param dict
	 *            字典信息
	 * @return 列表数据
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String, Bean> getItemMap(Bean dict) {
		return (LinkedHashMap<String, Bean>) getItemCmpyBean(dict).get(CHILD_NODE_MAP);
	}

	/**
	 * 获取字典对应的列表数据
	 * 
	 * @param strCmpy
	 *            公司ID
	 * @param dict
	 *            字典信息
	 * @return 列表数据
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String, Bean> getItemMap(String strCmpy, Bean dict) {
		return (LinkedHashMap<String, Bean>) getItemCmpyBean(strCmpy, dict).get(CHILD_NODE_MAP);
	}

	/**
	 * 装载字典数据信息
	 * 
	 * @param dict
	 *            字典对象
	 * @param cmpyCode
	 *            公司编码，""表示公共公司
	 * @return 字典数据信息
	 */
	public static Bean loadItem(Bean dict, String cmpyCode) {
		Bean cmpyBean;
		if (dict.getInt("DICT_IS_INNER") == Constant.YES_INT) { // 内部字典(必须为公共字典），执行内部初始化
			cmpyBean = new Bean();
			List<Bean> itemList = dict.getList(TABLE_DICT_ITEM);
			LinkedHashMap<String, Bean> map = new LinkedHashMap<String, Bean>();
			List<Bean> list = new ArrayList<Bean>();
			for (Bean item : itemList) {
				Bean nItem = item.copyOf();
				nItem.set("ID", item.getStr("ITEM_CODE")).set("NAME", item.getStr("ITEM_NAME")).set("PID",
						item.getStr("ITEM_PCODE"));
				map.put(nItem.getStr("ID"), nItem);
				if (nItem.getInt("ITEM_FLAG") != Constant.NO_INT) { // 列表只显示有效的数据
					if (!Context.isEmpytyThread(THREAD_DICT_EXT_WHERE)) {
						if (Lang.isTrueScript(
								ServUtils.replaceSysAndData(Context.getThreadStr(THREAD_DICT_EXT_WHERE), nItem))) {
							list.add(nItem);
						}
					} else {
						list.add(nItem);
					}
				}
			} // end for
			if (dict.getInt("DICT_TYPE") == DIC_TYPE_TREE) { // 树形处理
				cmpyBean.set(CHILD_NODE, toTree(dict, map));
			} else { // 列表
				cmpyBean.set(CHILD_NODE, list);
			}
			cmpyBean.set(CHILD_NODE_MAP, map);
		} else { // 外部字典，执行外部初始化
			cmpyBean = loadDictItem(dict, cmpyCode);
		}
		return cmpyBean;
	}

	/**
	 * 根据字典获取所有字典项.
	 * 
	 * @param dict
	 *            字典对象
	 * @param cmpyCode
	 *            公司编码
	 * @return dataBean 返回的字典数据信息
	 */
	@SuppressWarnings("unchecked")
	private static Bean loadDictItem(Bean dict, String cmpyCode) {
		Bean cmpyBean = new Bean();
		List<Bean> dataList = null;
		LinkedHashMap<String, Bean> itemList = getDictItemList(dict, cmpyCode);
		// 判断是否有子字典定义，如果有，装载子字典定义；只支持树状字典增加子字典项;子字典必须为叶子字典
		if (dict.getInt(COL_DICT_TYPE) == DIC_TYPE_TREE) {
			if (dict.getStr("DICT_CHILD_ID").length() > 0) {
				dataList = toTree(dict, itemList, false); // 强制父树全部给枝节点
				// 如果有子字典，取字典定义
				Bean childDict = DictMgr.getDict(dict.getStr("DICT_CHILD_ID"));
				// 判断子字典是否为叶子字典，并且配置了父字典字段ID
				if (childDict.getStr(COL_DICT_F_PARENT).length() > 0) {
					String extWhere = null;
					if (!Context.isEmpytyThread(THREAD_DICT_EXT_WHERE)) { // 忽略父字典自定义过滤条件
						extWhere = Context.getThreadStr(THREAD_DICT_EXT_WHERE);
						Context.removeThread(THREAD_DICT_EXT_WHERE); // 清除父的自定义过滤
					}
					List<Bean> childBeanList = getItemList(cmpyCode, childDict);
					itemList.putAll(getItemMap(cmpyCode, childDict));
					for (int i = childBeanList.size() - 1; i >= 0; i--) {
						Bean childBean = childBeanList.get(i);
						if (childBean.get("PID") != null) {
							Bean parentBean = itemList.get(childBean.get("PID"));
							if (parentBean != null) {
								List<Bean> child = (List<Bean>) parentBean.get(CHILD_NODE);
								if (child == null) {
									child = new ArrayList<Bean>();
									parentBean.set(CHILD_NODE, child);
								}
								childBean.set("LEAF", Constant.YES); // 设置为叶子节点
								child.add(0, childBean);
							}
						}
					}
					if (extWhere != null) { // 恢复父的自定义过滤
						Context.setThread(THREAD_DICT_EXT_WHERE, extWhere);
					}
				} // end if
			} else {
				dataList = toTree(dict, itemList); // 自动判断叶子根节点
			} // end DICT_CHILD_ID 父子树判断
		} else {
			dataList = toList(dict, itemList);
		} // end DIC_TYPE_TREE 树形判断
		cmpyBean.set(CHILD_NODE, dataList);
		cmpyBean.set(CHILD_NODE_MAP, itemList);
		return cmpyBean;
	}

	/**
	 * 根据数据字典定义获取字典项的列表信息
	 * 
	 * @param dict
	 *            数据字典对象
	 * @param cmpyCode
	 *            公司编码
	 * @return 字典项数据列表
	 */
	public static LinkedHashMap<String, Bean> getDictItemList(Bean dict, String cmpyCode) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select ");
		sqlBuilder.append(dict.getStr(COL_DICT_F_ID)).append(" ID, ");
		sqlBuilder.append(dict.getStr(COL_DICT_F_NAME)).append(" NAME");
		if (dict.get(COL_DICT_TYPE, DIC_TYPE_LIST) == DIC_TYPE_LEAF
				|| dict.get(COL_DICT_TYPE, DIC_TYPE_LIST) == DIC_TYPE_TREE) {
			sqlBuilder.append(", ").append(dict.getStr(COL_DICT_F_PARENT)).append(" PID");
		}
		if (!dict.isEmpty("DICT_F_FLAG")) {
			sqlBuilder.append(",").append(dict.getStr("DICT_F_FLAG")).append(" FLAG");
		}
		if (dict.getStr(COL_TABLE_SELECT).length() > 0) {
			sqlBuilder.append(", ").append(dict.getStr(COL_TABLE_SELECT));
		}

		sqlBuilder.append(" from ").append(dict.getStr(COL_TABLE_ID));

		sqlBuilder.append(getDictItemListWhere(dict, cmpyCode)); // 获取where设定信息

		if (!dict.isEmpty(COL_TABLE_ORDER)) {
			sqlBuilder.append(" order by ").append(dict.getStr(COL_TABLE_ORDER));
		}

		Connection conn = Context.getConn(dict.getStr("DICT_DATA_SOURCE"));
		final LinkedHashMap<String, Bean> itemList = new LinkedHashMap<String, Bean>();
		try {
			Context.getExecutor().queryCall(conn, sqlBuilder.toString(), null, new RowHandler() {
				public void handle(List<Bean> columen, Bean data) {
					data.remove(Constant.PARAM_ROWNUM); // 减小输出大小
					data.remove(Constant.KEY_ID);
					itemList.put(data.getStr("ID"), data);
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage() + "[" + dict.getId() + "]", e);
		} finally {
			Context.endConn(conn);
		}
		return itemList;
	}

	/**
	 * 根据数据字典定义获取字典项查询SQL的where信息
	 * 
	 * @param dict
	 *            数据字典对象
	 * @param cmpyCode
	 *            公司编码
	 * @return SQL的where条件
	 */
	private static String getDictItemListWhere(Bean dict, String cmpyCode) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(" where 1=1  and (");
		if (cmpyCode != null && cmpyCode.length() > 0 && dict.isNotEmpty(COL_DICT_F_CMPY)) {
			sqlBuilder.append("").append(dict.getStr(COL_DICT_F_CMPY)).append("='").append(cmpyCode).append("' ");
		} else {
			sqlBuilder.append("1=1 ");
		}
		String tableWhere = dict.getStr(COL_TABLE_WHERE);
		if (tableWhere.length() > 0) {
			if (tableWhere.indexOf("@@") >= 0) { // 存在数据权限
				tableWhere = DataAclMgr.replaceDataAcl(tableWhere);
			}
			if (tableWhere.indexOf("@") >= 0) { // 存在变量替换
				tableWhere = ServUtils.replaceSysVars(tableWhere);
			}
		}
		sqlBuilder.append(tableWhere).append(") ");
		if (!Context.isEmpytyThread(THREAD_DICT_EXT_WHERE)) { // 处理自定义过滤条件（通过线程变量传递）
			sqlBuilder.append(Context.getThreadStr(THREAD_DICT_EXT_WHERE));
		}
		return sqlBuilder.toString();
	}

	/**
	 * 形成列表结构，自动判断标志字段
	 * 
	 * @param dict
	 *            字典定义
	 * @param itemList
	 *            字典项列表
	 * @return 树形的字典数据
	 */
	public static List<Bean> toList(Bean dict, LinkedHashMap<String, Bean> itemList) {
		List<Bean> list = new ArrayList<Bean>();
		for (String key : itemList.keySet()) {
			Bean item = itemList.get(key);
			if (dict.contains(DICT_CLASS)) {
				((DictListener) dict.get(DICT_CLASS)).each(item);
			}
			if (item.getInt("FLAG") != Constant.NO_INT) { // 只显示非禁用的数据
				list.add(item);
			}
		}
		return list;
	}

	/**
	 * 形成树形结构，自动判断没有字的为叶子节点
	 * 
	 * @param dict
	 *            字典定义
	 * @param itemList
	 *            字典项列表
	 * @return 树形的字典数据
	 */
	public static List<Bean> toTree(Bean dict, LinkedHashMap<String, Bean> itemList) {
		return toTree(dict, itemList, true);
	}

	/**
	 * 形成树形结构.
	 * 
	 * @param dict
	 *            字典定义
	 * @param itemList
	 *            字典项列表
	 * @param autoFlag
	 *            true自动根据有无子设置 叶子，false为组合树强制设定所有点为枝节点
	 * @return 树形的字典数据
	 */
	@SuppressWarnings("unchecked")
	public static List<Bean> toTree(Bean dict, LinkedHashMap<String, Bean> itemList, boolean autoFlag) {
		List<Bean> topList = new ArrayList<Bean>();
		String leaf = autoFlag ? Constant.YES : Constant.NO;
		for (String key : itemList.keySet()) {
			Bean item = itemList.get(key);
			item.set("LEAF", leaf);
			if (dict.contains(DICT_CLASS)) {
				((DictListener) dict.get(DICT_CLASS)).each(item);
			}
			if (item.getInt("FLAG") == Constant.NO_INT) { // 忽略被禁用的数据
				continue;
			}
			if (!item.isEmpty("PID")) {
				Bean pItem = itemList.get(item.getStr("PID"));
				if (pItem != null) {
					List<Bean> pList;
					if (pItem.contains(CHILD_NODE)) {
						pList = (List<Bean>) pItem.get(CHILD_NODE);
					} else {
						pList = new ArrayList<Bean>();
						pItem.set("LEAF", Constant.NO); // 设置为非叶子节点
						pItem.set(CHILD_NODE, pList);
					}
					pList.add(item);
				} else {
					topList.add(item);
					log.error("ERROR PID=" + item.getStr("PID") + " ID=" + item.getStr("ID"));
				}
			} else {
				topList.add(item);
			}
		}
		return topList;
	}

	/**
	 * 递归调用当前节点，生成层级信息
	 * 
	 * @param item
	 *            当前节点
	 * @return 层级信息
	 */
	@SuppressWarnings("unchecked")
	public static List<Bean> buildItemLevel(Bean item) {
		List<Bean> rtnList = new ArrayList<Bean>();
		if (item.contains(CHILD_NODE)) {
			List<Bean> subList = (ArrayList<Bean>) item.get(CHILD_NODE);
			for (Bean sub : subList) {
				sub.set("LEVEL", item.getInt("LEVEL") + 1);
				sub.set("CODE_PATH", item.getStr("CODE_PATH") + sub.getStr("ID") + Constant.CODE_PATH_SEPERATOR);
				rtnList.add(sub);
				rtnList.addAll(buildItemLevel(sub));
			}
		}
		return rtnList;
	}

	/**
	 * 从文件读取所有服务的定义信息
	 * 
	 * @return 服务定义列表
	 */
	public static List<Bean> getDictDefListByFile() {
		return FileHelper.getJsonListByFile(JSON_PATH_SRC);
	}

	/**
	 * 将字典从数据库装载到缓存中，缺省装载所有所有内部字典
	 */
	public static synchronized void initDictCacheByDB() {
		// 读取所有有效的数据字典定义
		String sql = "select * from " + TABLE_DICT + " where S_FLAG=1";
		String itemSql = "select * from " + TABLE_DICT_ITEM + " where DICT_ID in (select DICT_ID from " + TABLE_DICT
				+ " where " + "S_FLAG=1 and DICT_IS_INNER=1) order by DICT_ID,S_CMPY,ITEM_ORDER";
		Context.getExecutor().query(sql, new QueryCallback() {
			public void call(List<Bean> cols, Bean data) {
				setToCache(data.getStr("DICT_ID"), data);
			}
		});
		Context.getExecutor().query(itemSql, new QueryCallback() {
			public void call(List<Bean> cols, Bean data) {
				Bean dict = (Bean) CacheMgr.getInstance().get(data.getStr("DICT_ID"), CACHE_DICT);
				if (dict != null) {
					List<Bean> items;
					if (dict.contains(TABLE_DICT_ITEM)) {
						items = dict.getList(TABLE_DICT_ITEM);
					} else {
						items = new ArrayList<Bean>();
						dict.set(TABLE_DICT_ITEM, items);
					}
					items.add(data);
				}
			}
		});
	}

	/**
	 * 获取字典项列表
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @return 字典项列表
	 */
	public static List<Bean> getItemList(String dictId) {
		Bean dict = getDict(dictId);
		if (dict != null) {
			return getItemList(dict);
		} else {
			return new ArrayList<Bean>();
		}
	}

	/**
	 * 获取数据字典树.
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @return 数据字典树
	 */
	public static List<Bean> getTreeList(String dictId) {
		Bean dict = getDict(dictId);
		return getTreeList(dictId, dict.getInt("DICT_DIS_LAYER"));
	}

	/**
	 * 获取数据字典树.
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param disLayer
	 *            显示层级
	 * @return 数据字典树
	 */
	public static List<Bean> getTreeList(String dictId, int disLayer) {
		return getTreeList(dictId, null, disLayer);
	}

	/**
	 * 获取数据字典树，显示全部层级。
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param pid
	 *            父编码
	 * @return 数据字典树
	 */
	public static List<Bean> getTreeList(String dictId, String pid) {
		return getTreeList(dictId, pid, 0);
	}

	/**
	 * 获取数据字典树，缺省不输出父编码节点
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param pid
	 *            父编码
	 * @param disLayer
	 *            一共显示几级
	 * @return 数据字典树
	 */
	public static List<Bean> getTreeList(String dictId, String pid, int disLayer) {
		return getTreeList(dictId, pid, disLayer, false);
	}

	/**
	 * 获取数据字典树.
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param pid
	 *            父编码
	 * @param disLayer
	 *            一共显示几级
	 * @param showPid
	 *            如果设定了父编码，则true:输出父编码作为根，false（缺省）:不输出父编码节点
	 * @return 数据字典树
	 */
	public static List<Bean> getTreeList(String dictId, String pid, int disLayer, boolean showPid) {
		List<Bean> treeList;
		Bean dict = getDict(dictId);
		if (dict != null) {
			if (pid == null) {
				pid = "";
			}
			boolean dynaRoot = false; // 动态根，缺省为false
			if (pid.length() == 0) { // 如果没有指定父
				if (dict.isNotEmpty("DICT_ROOT")) { // 且设定了动态根，用动态根
					pid = ServUtils.replaceSysVars(dict.getStr("DICT_ROOT"));
					dynaRoot = true; // 指定动态根为true
				}
			} else if (showPid) { // pid不为空，且包含PID作为根节点
				dynaRoot = true;
			}
			Bean itemCmpyBean = getItemCmpyBean(dict); // 获取字典数据信息
			LinkedHashMap<String, Bean> nodeMap = itemCmpyBean.getLinkedMap(CHILD_NODE_MAP);
			List<Bean> dataList;
			if ((pid.length() == 0) || !nodeMap.containsKey(pid)) {
				dataList = itemCmpyBean.getList(CHILD_NODE);
			} else if (!nodeMap.containsKey(pid)) {
				dataList = new ArrayList<Bean>();
			} else {
				Bean pBean = nodeMap.get(pid);
				if (dynaRoot) { // 动态根，要包含根节点
					dataList = new ArrayList<Bean>();
					dataList.add(pBean);
				} else { // 其他情况不包含根信息
					dataList = pBean.getList(CHILD_NODE);
				}
			}
			if (disLayer <= 0 && dict.isEmpty("DICT_EXPRESSION")) { // 向下显示到末级
				treeList = dataList;
			} else {
				// 分级获取数据，包含当前层级
				treeList = recurSubList(dict, dataList, disLayer);
			}
		} else {
			treeList = new ArrayList<Bean>();
		}
		return treeList;
	}

	/**
	 * 分级获取数据，获取的总级数包含当前层级
	 * 
	 * @param dict
	 *            字典定义
	 * @param treeList
	 *            当前级别数据
	 * @param layer
	 *            获取的总层数
	 * @return 包含指定层级数的子孙列表
	 */
	@SuppressWarnings("unchecked")
	public static List<Bean> recurSubList(Bean dict, List<Bean> treeList, int layer) {
		List<Bean> outList = new ArrayList<Bean>(treeList.size());
		String exp = dict.getStr("DICT_EXPRESSION");
		String subExp = (dict.isNotEmpty("DICT_CHILD_ID"))
				? getDict(dict.getStr("DICT_CHILD_ID")).getStr("DICT_EXPRESSION") : "";
		for (Bean item : treeList) {
			Bean out = (Bean) item.copyOf();
			// 根据数据规则表达式确定是否包含在显示结果中
			// 独立树或者父子树的枝节点判断当前字典的规则表达式
			if ((dict.isEmpty("DICT_CHILD_ID") || (out.getInt("LEAF") != Constant.YES_INT))) {
				if (exp.length() > 0) {
					if (!Lang.isTrueScript(ServUtils.replaceSysAndData(exp, out))) {
						continue;
					}
				}
			} else if (subExp.length() > 0) { // 父子树的叶子节点判断子字典的规则表达式
				if (!Lang.isTrueScript(ServUtils.replaceSysAndData(subExp, out))) {
					continue;
				}
			}
			if (out.contains(CHILD_NODE)) {
				if (layer == 1) { // 保留CHILD，前台才可以判断有没有子
					out.set(CHILD_NODE, new ArrayList<Bean>());
					// out.set("LEAF", Constant.NO); //设置为非叶子节点
				} else {
					List<Bean> subList = recurSubList(dict, (List<Bean>) out.get(CHILD_NODE), layer - 1);
					if (subList.size() > 0) {
						out.set(CHILD_NODE, subList);
					} else { // 没有符合条件的就清除子
						out.remove(CHILD_NODE);
					}
				}
			}
			outList.add(out);
		} // end for
		return outList;
	}

	/**
	 * 遍历树形列表的数据
	 * 
	 * @param treeItem
	 *            字典项信息
	 * @param ls
	 *            遍历处理器
	 */
	@SuppressWarnings("unchecked")
	public static void handleTree(Bean treeItem, ListHandler<Bean> ls) {
		ls.handle(treeItem);
		if (treeItem.contains(CHILD_NODE)) {
			handleTree((List<Bean>) treeItem.get(CHILD_NODE), ls);
		}
	}

	/**
	 * 遍历树形列表的数据
	 * 
	 * @param treeList
	 *            树形结构列表
	 * @param ls
	 *            遍历处理器
	 */
	@SuppressWarnings("unchecked")
	public static void handleTree(List<Bean> treeList, ListHandler<Bean> ls) {
		for (Bean data : treeList) {
			ls.handle(data);
			if (data.contains(CHILD_NODE)) {
				handleTree((List<Bean>) data.get(CHILD_NODE), ls);
			}
		}
	}

	/**
	 * 此数据字典是否包含字典项。
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param code
	 *            数据字典项编码
	 * @return 是否包含字典项
	 */
	public static boolean contains(String dictId, String code) {
		Bean dict = getDict(dictId);
		if (dict != null) {
			return contains(dict, code);
		} else {
			return false;
		}
	}

	/**
	 * 根据数据字典项编码获取数据字典名称.
	 * 
	 * @param dictId
	 *            数据字典定义编码
	 * @param code
	 *            数据字典项编码
	 * @return 数据字典项编码对应的数据
	 */
	public static Bean getItem(String dictId, String code) {
		Bean item = null;
		Bean dict = getDict(dictId);
		if (dict != null) {
			LinkedHashMap<String, Bean> nodeMap = getItemMap(dict);
			if (nodeMap != null && nodeMap.containsKey(code)) {
				item = ((Bean) nodeMap.get(code));
			}
		}
		return item;
	}

	/**
	 * 根据数据字典项值获取数据字项编码
	 * 
	 * @param dictId
	 *            数据字典定义编码
	 * @param name
	 *            数据字典项值
	 * @return 数据字典项编码，如果没有返回null字符串
	 */
	public static String getItemCodeByName(String dictId, String name) {
		String code = null;
		Bean item = getItemByName(dictId, name);
		if (item != null) {
			code = item.getStr("ID");
		}
		return code;
	}

	/**
	 * 根据数据字典项值获取数据字项。
	 * 
	 * @param dictId
	 *            数据字典定义编码
	 * @param name
	 *            数据字典项值
	 * @return 数据字典项数据
	 */
	public static Bean getItemByName(String dictId, String name) {
		Bean item = null;
		Bean dict = getDict(dictId);
		if (dict != null) {
			LinkedHashMap<String, Bean> nodeMap = getItemMap(dict);
			if (nodeMap != null) {
				for (String key : nodeMap.keySet()) {
					Bean listItem = nodeMap.get(key);
					if (listItem.getStr("NAME").equals(name)) {
						item = listItem;
						break;
					}
				}
			}
		}
		return item;
	}

	/**
	 * 根据数据字典项编码获取数据字典名称.
	 * 
	 * @param dictId
	 *            数据字典编码
	 * @param code
	 *            数据字典项编码
	 * @return 数据字典树
	 */
	public static String getName(String dictId, String code) {
		Bean item = getItem(dictId, code);
		if (item != null) {
			return item.getStr("NAME");
		} else {
			return code;
		}
	}

	/**
	 * 根据数据字典项编码获取数据字典的图片信息。
	 * 
	 * @param dictId
	 *            数据字典编码
	 * @param code
	 *            数据字典项编码
	 * @return 数据字典图片信息
	 */
	public static String getImg(String dictId, String code) {
		Bean item = getItem(dictId, code);
		if (item != null) {
			return item.getStr("IMG");
		} else {
			return code;
		}
	}

	/**
	 * 此数据字典是否包含字典项
	 * 
	 * @param dict
	 *            数据字典定义
	 * @param code
	 *            数据字典项编码
	 * @return 是否包含字典项
	 */
	public static boolean contains(Bean dict, String code) {
		LinkedHashMap<String, Bean> nodeMap = getItemMap(dict);
		if (nodeMap != null) {
			return nodeMap.containsKey(code);
		} else {
			return false;
		}
	}

	/**
	 * 根据数据字典项编码获取数据字典项名称。
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param code
	 *            数据字典项编码
	 * @return 数据字典树
	 */
	public static String getFullName(String dictId, String code) {
		if (code.length() == 0) {
			return code;
		}
		Bean dict = getDict(dictId);
		if (dict == null) {
			return code;
		}
		LinkedHashMap<String, Bean> itemMap = getItemMap(dict);
		if ((itemMap == null) || (!itemMap.containsKey(code))) { // 如果不存在字典数据，则自动返回编码
			return code;
		}
		Bean data = itemMap.get(code);
		int upLayer = 0;
		if (dict.getInt(COL_DICT_TYPE) == DIC_TYPE_TREE) { // 树形的判断向上获取层级数
			int curLayer = data.getInt("LEVEL");
			int disLayer = dict.getInt("DICT_NAME_LAYER");
			if (disLayer > 0) {
				upLayer = disLayer;
			} else {
				upLayer = curLayer - Math.abs(disLayer);
				if (upLayer < 0) {
					upLayer = 0;
				}
			}
		}
		StringBuilder fullName = new StringBuilder(getCurLayerFullName(dict, data));
		for (int i = 0; i < upLayer; i++) { // 循环获取父显示名称
			if (data.contains("PID")) {
				data = itemMap.get(data.getStr("PID"));
				if (data != null) {
					fullName.insert(0, getCurLayerFullName(dict, data) + "/");
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return fullName.toString();
	}

	/**
	 * 根据数据字典项编码获取数据字典项名称，支持多个字典项，逗号分隔获取的名字也是逗号分隔
	 * 
	 * @param dictId
	 *            数据字典Id
	 * @param codes
	 *            数据字典项编码串，多个逗号分隔
	 * @return 对应名称串，多个逗号分隔
	 */
	public static String getFullNames(String dictId, String codes) {
		if (codes.indexOf(Constant.SEPARATOR) > 0) {
			String[] cs = codes.split(Constant.SEPARATOR);
			StringBuilder result = new StringBuilder();
			for (String code : cs) {
				result.append(getFullName(dictId, code)).append(Constant.SEPARATOR);
			}
			if (cs.length > 0) {
				result.setLength(result.length() - 1);
			}
			return result.toString();
		} else {
			return getFullName(dictId, codes);
		}
	}
	
	public static String getNames(String dictId, String codes) {
		if (codes.indexOf(Constant.SEPARATOR) > 0) {
			String[] cs = codes.split(Constant.SEPARATOR);
			StringBuilder result = new StringBuilder();
			for (String code : cs) {
				result.append(getName(dictId, code)).append(Constant.SEPARATOR);
			}
			if (cs.length > 0) {
				result.setLength(result.length() - 1);
			}
			return result.toString();
		} else {
			return getName(dictId, codes);
		}
	}

	/**
	 * 将一个字典定义生成为JSON文本文件
	 * 
	 * @param dictId
	 *            字典定义主键
	 * @return 是否成功生成文件
	 */
	public static Boolean toJsonFile(String dictId) {
		Boolean result = false;
		try {
			Transaction.commit(); // 先提交前面的修改
			Bean dict = getDictDefByDB(dictId);
			String fileName = JSON_PATH_SRC + dictId + ".json";
			// 混合模式字典采用扩展定义
			if (dict.getInt("PRO_FLAG") == ServDef.PRO_FLAG_MIX) {
				fileName = fileName + "x";
			}
			FileHelper.toJsonFile(dict, fileName);

			if (Context.isDebugMode()) {
				if (Context.app(APP.PRO_SRC_DOC, "") != null && Context.app(APP.PRO_SRC_DOC, "").length() > 0) {
					String fileNameSrc = Context.app(APP.PRO_SRC_DOC, "") + "/SY_SERV_DICT/" + dictId + ".json";
					// 如果是混合模式采用扩展定义
					if (dict.getInt("PRO_FLAG") == ServDef.PRO_FLAG_MIX) {
						fileNameSrc = fileNameSrc + "x";
					}
					FileHelper.toJsonFile(dict, fileNameSrc);
				}
			}

			result = true;
		} catch (Exception e) {
			log.error(e.getMessage() + dictId, e);
		}
		return result;
	}

	/**
	 * 将一个字典定义生成的JSON的文件删除
	 * 
	 * @param dictId
	 *            字典定义主键
	 * @return 是否成功删除文件
	 */
	public static Boolean deleteJsonFile(String dictId) {
		String fileName = JSON_PATH_SRC + dictId + ".json";
		if (FileHelper.exists(fileName + "x")) { // 先判断是否为扩展定义
			if (Context.app(APP.PRO_SRC_DOC, "") != null && Context.app(APP.PRO_SRC_DOC, "").length() > 0) {
				String fileNameSrc = Context.app(APP.PRO_SRC_DOC, "") + "/SY_SERV_DICT/" + dictId + ".json";
				if (FileHelper.exists(fileNameSrc + "x")) {
					FileHelper.delete(fileNameSrc + "x");
				} else {
					FileHelper.delete(fileNameSrc);
				}
			}
			return FileHelper.delete(fileName + "x");
		} else {
			if (Context.app(APP.PRO_SRC_DOC, "") != null && Context.app(APP.PRO_SRC_DOC, "").length() > 0) {
				String fileNameSrc = Context.app(APP.PRO_SRC_DOC, "") + "/SY_SERV_DICT/" + dictId + ".json";
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
	 * 根据数据项取得当前数据项显示全名称。
	 * 
	 * @param dict
	 *            字典定义
	 * @param data
	 *            数据项信息
	 * @return 当前数据项显示全名称
	 */
	private static String getCurLayerFullName(Bean dict, Bean data) {
		if (!dict.isEmpty("DICT_DIS_FORMAT")) {
			return BeanUtils.replaceValues(dict.getStr("DICT_DIS_FORMAT"), data);
		} else {
			return data.getStr("NAME");
		}
	}

	/**
	 * 获取动态字典
	 * 
	 * @param clazz
	 *            动态字典类名
	 * @param paramBean
	 *            参数
	 * @return 字典
	 */
	public static Bean getDict(String clazz, ParamBean paramBean) {
		return DictMgr.getDict(DictMgr.getDictId(clazz, paramBean));
	}

	/**
	 * 获取动态字典ID
	 * 
	 * @param clazz
	 *            动态字典类名
	 * @param paramBean
	 *            参数
	 * @return 字典ID
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String getDictId(String clazz, ParamBean paramBean) {
		try {
			Class cls = Lang.loadClass(clazz);
			Method method = cls.getMethod("getDictId", new Class[] { ParamBean.class });
			return (String) method.invoke(cls.newInstance(), paramBean);
		} catch (Throwable e) {
			throw new RuntimeException("获取自定义字典ID", e);
		}
	}

	/**
	 * 获取动态字典项bean
	 * 
	 * @param dic
	 *            字典
	 * @param paramBean
	 *            参数
	 * @return 字典项bean
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Bean getDynaItemList(String dic, ParamBean paramBean) {
		try {
			Class cls = Lang.loadClass(dic);
			Method method = cls.getMethod("getItems", new Class[] { ParamBean.class });
			return (Bean) method.invoke(cls.newInstance(), paramBean);
		} catch (Throwable e) {
			throw new RuntimeException("获取自定义字典数据错误", e);
		}
	}

	/**
	 * 将数据字典放入缓存.
	 * 
	 * @param dictId
	 *            字典编码
	 * @param object
	 *            缓存的对象
	 */
	private static void setToCache(String dictId, Object object) {
		CacheMgr.getInstance().set(dictId, object, CACHE_DICT);
	}

	/**
	 * 根据字典ID清除缓存，如果为私有缓存，清除当前用户所在组织的缓存。
	 * 
	 * @param dictId
	 *            字典ID
	 */
	public static void clearCache(String dictId) {
		clearCache(dictId, Context.getCmpy());
	}

	/**
	 * 根据字典ID清除缓存，(缺省更新定义文件)
	 * 
	 * @param dictId
	 *            字典ID
	 */
	public static void clearAllCache(String dictId) {
		clearAllCache(dictId, true);
	}

	/**
	 * 根据字典ID清除缓存，如果为私有缓存，缺省移除全部公司缓存
	 * 
	 * @param dictId
	 *            字典ID
	 * @param fileFlag
	 *            文件标志
	 */
	public static void clearAllCache(String dictId, boolean fileFlag) {
		if (fileFlag) {
			toJsonFile(dictId);
		}
		clearCache(dictId, null);
	}

	/**
	 * 根据字典ID清除缓存.
	 * 
	 * @param dictId
	 *            字典ID
	 * @param cmpyCode
	 *            公司编码
	 */
	public static void clearCache(String dictId, String cmpyCode) {
		Bean dict = (Bean) CacheMgr.getInstance().get(dictId, CACHE_DICT);
		if (dict == null) {
			return;
		}
		// 公共的直接移除
		if (dict.getInt(COL_S_PUBLIC) == Constant.YES_INT) {
			CacheMgr.getInstance().remove(dictId, CACHE_DICT); // 移除字典定义
			CacheMgr.getInstance().remove(PUBLIC_CMPY, CACHE_PRE_CMPY + dictId); // 移除字典项缓存
		} else if ((cmpyCode != null) && (cmpyCode.length() > 0)) { // 私有的指定了公司，移除本公司的
			String schedDicts = "," + Context.appStr("CACHE_DICT_SCHED_LOAD") + ","; // 系统配置调度的缓存
			String schedDict = "," + dictId + ",";
			if (schedDicts.indexOf(schedDict) >= 0) { // 通过调度清理
				addRebuild(dictId, cmpyCode);
			} else {
				CacheMgr.getInstance().remove(cmpyCode, CACHE_PRE_CMPY + dictId);
			}
		} else {
			CacheMgr.getInstance().remove(dictId, CACHE_DICT); // 移除字典定义
			CacheMgr.getInstance().clearCache(CACHE_PRE_CMPY + dictId); // 移除所有公司的数据项
		}
	}

	/**
	 * 获取字典值对应的Like SQL信息，内部字典字符串匹配，外部字典SQL匹配
	 * 
	 * @param dictId
	 *            字典编码
	 * @param value
	 *            字典值，带%信息
	 * @return Like sql信息
	 */
	public static String getDictLikeSql(String dictId, String value) {
		StringBuilder sb = new StringBuilder();
		Bean dict = getDict(dictId);
		if (dict.getInt("DICT_IS_INNER") == Constant.YES_INT) { // 内部字典，执行内部过滤脚本
			LinkedHashMap<String, Bean> itemMap = getItemMap(dict);
			int matchType = 0; // 缺省左右全模糊查询
			if (value.startsWith("%")) {
				if (!value.endsWith("%")) {
					matchType = 1; // 左模糊
				}
			} else if (value.endsWith("%")) {
				matchType = 2; // 右模糊
			}
			value = value.replaceAll("%", "");
			for (String key : itemMap.keySet()) {
				boolean bMatch = false;
				switch (matchType) {
				case 1:
					if (itemMap.get(key).getStr("NAME").endsWith(value)) {
						bMatch = true;
					}
					break;
				case 2:
					if (itemMap.get(key).getStr("NAME").startsWith(value)) {
						bMatch = true;
					}
					break;
				default:
					if (itemMap.get(key).getStr("NAME").indexOf(value) >= 0) {
						bMatch = true;
					}
				}
				if (bMatch) {
					sb.append("'").append(key).append("',");
				}
			} // end for
			if (sb.length() > 0) {
				sb.setLength(sb.length() - 1);
			}
		} else { // 外部字典
			if (dict.isNotEmpty("DICT_CHILD_ID")) { // 组合字典需要取叶子字典进行SQL过滤
				dict = getDict(dict.getStr("DICT_CHILD_ID"));
			}
			if (dict != null) {
				String dbUserName;
				if (!dict.isEmpty("DICT_DATA_SOURCE")) { // 如果字典指定数据源则使用字典指定的用户名
					dbUserName = Context.getDBUserName(dict.getStr("DICT_DATA_SOURCE")) + ".";
				} else { // 如果字典没指定但是服务指定，则字典查询带上缺省数据源用户名，如果两个都没指定则不带
					dbUserName = Transaction.getDsName().length() > 0 ? Context.getDBUserName() + "." : "";
				}
				sb.append("select ").append(dict.getStr(COL_DICT_F_ID)).append(" from ").append(dbUserName)
						.append(dict.getStr(COL_TABLE_ID)).append(getDictItemListWhere(dict, Context.getCmpy()))
						.append(" and ").append(dict.getStr(COL_DICT_F_NAME)).append(" like '").append(value)
						.append("'");
			}
		}
		return sb.toString();
	}

	/**
	 * 获取字典值对应的SUB SQL信息，内部字典字符串匹配，外部字典SQL匹配，用于左侧导航查询，包含子数据
	 * 
	 * @param dict
	 *            字典定义（要求为树形字典定义）
	 * @param value
	 *            字典值
	 * @return sql信息
	 */
	public static String getDictSubSql(Bean dict, String value) {
		final StringBuilder sb = new StringBuilder();
		LinkedHashMap<String, Bean> itemMap = getItemMap(dict);
		Bean itemBean = itemMap.get(value);
		if (itemBean == null) {
			return sb.toString();
		}
		if (dict.getInt("DICT_IS_INNER") == Constant.YES_INT) { // 内部字典，执行内部过滤脚本
			handleTree(itemBean, new ListHandler<Bean>() {
				public void handle(Bean item) {
					sb.append("'").append(item.get("ID")).append("',");
				}
			});
			int len = sb.length();
			if (len > 0) {
				sb.setLength(len - 1);
			}
		} else { // 外部字典
			String dbUserName;
			if (!dict.isEmpty("DICT_DATA_SOURCE")) { // 如果字典指定数据源则使用字典指定的用户名
				dbUserName = Context.getDBUserName(dict.getStr("DICT_DATA_SOURCE")) + ".";
			} else { // 如果字典没指定但是服务指定，则字典查询带上缺省数据源用户名，如果两个都没指定则不带
				dbUserName = Transaction.getDsName().length() > 0 ? Context.getDBUserName() + "." : "";
			}
			String codePath = (itemBean.contains("CODE_PATH")) ? itemBean.getStr("CODE_PATH")
					: getCodePath(itemMap, value, new HashSet<String>());
			sb.append("select ").append(dict.getStr(COL_DICT_F_ID)).append(" from ").append(dbUserName)
					.append(dict.getStr(COL_TABLE_ID)).append(getDictItemListWhere(dict, Context.getCmpy()))
					.append(" and ").append(dict.getStr("DICT_F_PATH")).append(" like '").append(codePath).append("%'");
		}
		return sb.toString();
	}

	/**
	 * 递归获取完整的路径信息
	 * 
	 * @param itemMap
	 *            字典项集合
	 * @param itemCode
	 *            字典值
	 * @param addedItem
	 *            已经增加的数据
	 * @return 递归获取完整的路径信息
	 */
	public static String getCodePath(LinkedHashMap<String, Bean> itemMap, String itemCode, HashSet<String> addedItem) {
		StringBuilder sb = new StringBuilder(itemCode).append(Constant.CODE_PATH_SEPERATOR);
		Bean item = itemMap.get(itemCode);
		if ((item != null) && (item.isNotEmpty("PID"))) {

			if (addedItem.contains(itemCode)) {
				log.error("父子节点不允许被设置成环状，程序进入死循环。ID=" + itemCode);
				throw new TipException("父子节点不允许被设置成环状。");
			}
			addedItem.add(itemCode);
			sb.insert(0, getCodePath(itemMap, item.getStr("PID"), addedItem));
		}
		return sb.toString();
	}

	/**
	 * 是否为树形字典
	 * 
	 * @param dictDef
	 *            字典定义
	 * @return 是否树形字典
	 */
	public static boolean isTree(Bean dictDef) {
		return (dictDef == null ? false : dictDef.getInt(COL_DICT_TYPE) == DIC_TYPE_TREE);
	}

	/**
	 * 是否为独立树形字典
	 * 
	 * @param dictDef
	 *            字典定义
	 * @return 是否独立树形字典
	 */
	public static boolean isSingleTree(Bean dictDef) {
		return (dictDef == null ? false
				: dictDef.getInt(COL_DICT_TYPE) == DIC_TYPE_TREE && dictDef.isEmpty("DICT_CHILD_ID"));
	}

	/**
	 * 是否为组合树形字典
	 * 
	 * @param dictDef
	 *            字典定义
	 * @return 是否组合树形字典
	 */
	public static boolean isComboTree(Bean dictDef) {
		return (dictDef == null ? false
				: dictDef.getInt(COL_DICT_TYPE) == DIC_TYPE_TREE && dictDef.isNotEmpty("DICT_CHILD_ID"));
	}

	/**
	 * 根据字段编码将字典数据装载入缓存
	 * 
	 * @param dictCode
	 *            字典编码
	 */
	public static void loadDictCache(String dictCode) {
		final Bean dictDef = getDict(dictCode);
		if (dictDef == null) { // 避免字典名称配错了。
			return;
		}
		if (dictDef.isNotEmpty(COL_DICT_F_CMPY)) { // 按照不同公司存在私有字典，则依次装载
			String preCmpy = Context.getCmpy();
			List<Bean> cmpyList = getItemList("SY_ORG_CMPY");
			handleTree(cmpyList, new ListHandler<Bean>() {
				public void handle(Bean data) {
					Context.changeCmpy(data.getStr("ID"));
					getItemCmpyBean(dictDef);
				}

			});
			Context.changeCmpy(preCmpy);
		} else {
			getItemCmpyBean(dictDef);
		}
	}

	/**
	 * 将需要清理重建的缓存标志存储起来，待后台调度进行统一更新处理
	 * 
	 * @param dictId
	 *            字典编码
	 * @param cmpy
	 *            公司编码
	 */
	@SuppressWarnings("unchecked")
	public static void addRebuild(String dictId, String cmpy) {
		Map<String, String> cmpyMap = (Map<String, String>) CacheMgr.getInstance().get(dictId, CACHE_TO_REBUILD);
		if (cmpyMap == null) {
			cmpyMap = new HashMap<String, String>();
		}
		cmpyMap.put(cmpy, null);
		CacheMgr.getInstance().set(dictId, cmpyMap, CACHE_TO_REBUILD);
	}

	/**
	 * 根据字典更新状态，重建需要处理的缓存。
	 * 
	 * @return 重新装载的缓存编码列表
	 */
	@SuppressWarnings("unchecked")
	public static String rebuildCache() {
		StringBuilder dictInfo = new StringBuilder();
		List<String> objList = CacheMgr.getInstance().getKeyList(CACHE_TO_REBUILD);

		String[] dicts = Context.appStr("CACHE_DICT_SCHED_LOAD").split(Constant.SEPARATOR);
		// 按照web.xml文件配置的字典顺序装载数据，避免出现子字典后装载，父字典先装载的bug
		for (String dictId : dicts) {
			if (objList.contains(dictId)) {
				dictInfo.append(dictId).append("(");
				Bean dict = getDict(dictId);
				Map<String, String> cmpyMap = (Map<String, String>) CacheMgr.getInstance().get(dictId,
						CACHE_TO_REBUILD);
				for (String cmpy : cmpyMap.keySet()) {
					CacheMgr.getInstance().set(cmpy, loadItem(dict, cmpy), CACHE_PRE_CMPY + dictId); // 设置缓存
					dictInfo.append(cmpy).append(",");
				}
				dictInfo.append(")");
				CacheMgr.getInstance().remove(dictId, CACHE_TO_REBUILD);
			}
		}
		return dictInfo.toString();
	}

	/**
	 * 修改更新时间
	 * 
	 * @param id
	 *            字典编码
	 * @param mtime
	 *            更新时间
	 */
	public static void udpateMtime(String id, String mtime) {
		if (mtime == null || mtime.isEmpty()) {
			mtime = DateUtils.getDatetimeTS();
		}
		ParamBean param = new ParamBean(ServMgr.SY_SERV_DICT, ServMgr.ACT_SAVE);
		param.setId(id).set("S_MTIME", mtime);
		ServMgr.act(param);
	}
}
