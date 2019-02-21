package com.rh.core.serv.util;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.util.JsonUtils;

public class SQLTransUtil {

	public static Bean SQL_MAP = new Bean();

	public static String SERV = "SY_COMM_SQL";

	public static String trans(String sqlConf, String servId) {
		if (sqlConf != null && sqlConf.length() > 0) {
			try {
				if (sqlConf.indexOf("[") == 0) { // 多条件
					List<Bean> sqlList = JsonUtils.toBeanList(sqlConf);
					StringBuilder transStr = new StringBuilder();
					for (Bean sqlBean : sqlList) {
						transStr.append(" ").append(switchByKey(sqlBean, servId));
					}
					return transStr.toString();
				} else if (sqlConf.indexOf("{") == 0) { // 单条件
					Bean sqlBean = JsonUtils.toBean(sqlConf);
					return switchByKey(sqlBean, servId);
				} else {
					throw new TipException("SQL配置格式错误! SERV:【" + servId + "】,SQL: 【" + sqlConf + "】");
				}
			} catch (Exception e) {
				throw new TipException(e.getMessage());
			}
		}
		return "";
	};

	private static String switchByKey(Bean sqlBean, String servId) {
		int type = sqlBean.getInt("type");
		switch (type) {
		case 1:
			return transFromKeys(sqlBean);
		case 2:
			return transFromKeyAndJson(sqlBean);
		case 3:
			return transFromJson(sqlBean);
		case 4:
			return transFromConf(sqlBean, servId);
		default:
			throw new TipException("未找到匹配类型!");
		}
	}

	private static String transFromKeys(Bean sqlBean) {
		if (sqlBean != null && !sqlBean.isEmpty()) {
			String key = sqlBean.getStr("key");
			StringBuilder transStr = new StringBuilder();
			if (SQL_MAP.contains(key)) {
				transStr.append(" ").append(SQL_MAP.getStr(key));
			} else {
				Bean customSqlBean = ServDao.find(SERV, key);
				if (customSqlBean != null) {
					SQL_MAP.put(customSqlBean.getId(), customSqlBean.getStr("SQL_STR"));
					transStr.append(" ").append(customSqlBean.getStr("SQL_STR"));
				} else {
					throw new TipException("自定义SQL语句对应的KEY不存在: [" + key + "]");
				}
			}
			return transStr.toString();
		}
		return "";

	}

	private static String transFromKeyAndJson(Bean sqlBean) {
		if (sqlBean != null && !sqlBean.isEmpty()) {
			String key = sqlBean.getStr("key");
			String[] fieldArr = sqlBean.getStr("field").split(",");
			String[] valueArr = sqlBean.getStr("value").split(",");
			StringBuilder transStr = new StringBuilder();
			if (SQL_MAP.contains(key)) {
				String tmpSql = SQL_MAP.getStr(key);
				for (int i = 0; i < fieldArr.length; i++) {
					String field = fieldArr[i];
					tmpSql = tmpSql.replaceAll("#" + field + "#", valueArr[i]);
				}
				tmpSql = tmpSql.replaceAll("\\^", "'");
				transStr.append(" ").append(tmpSql);
			} else {
				Bean customSqlBean = ServDao.find(SERV, key);
				if (customSqlBean != null) {
					SQL_MAP.put(customSqlBean.getId(), customSqlBean.getStr("SQL_STR"));
					String tmpSql = customSqlBean.getStr("SQL_STR");
					for (int i = 0; i < fieldArr.length; i++) {
						String field = fieldArr[i];
						tmpSql = tmpSql.replaceAll("#" + field + "#", valueArr[i]);
					}
					tmpSql = tmpSql.replaceAll("\\^", "'");
					transStr.append(" ").append(tmpSql);
				} else {
					throw new TipException("自定义SQL语句对应的KEY不存在: [" + key + "]");
				}
			}
			return transStr.toString();
		}
		return "";
	}

	private static String transFromJson(Bean sqlBean) {
		if (sqlBean != null && !sqlBean.isEmpty()) {
			String[] fieldArr = sqlBean.getStr("field").split(",");
			String[] valueArr = sqlBean.getStr("value").split(",");
			String[] conTypeArr = sqlBean.getStr("conType").split(",");
			StringBuilder transStr = new StringBuilder();
			for (int i = 0; i < fieldArr.length; i++) {
				String field = fieldArr[i];
				if (conTypeArr[i].equals("in")) {
					transStr.append(" and ").append(field).append(" ").append(conTypeArr[i]).append(" ")
							.append(valueArr[i].replaceAll("\\|", ",")).append(" ");
				} else {
					transStr.append(" and ").append(field).append(" ").append(conTypeArr[i]).append(" '")
							.append(valueArr[i]).append("' ");
				}

			}
			return transStr.toString();
		}
		return "";
	}

	private static String transFromConf(Bean sqlBean, String servId) {
		if (sqlBean != null && !sqlBean.isEmpty()) {
			ServDefBean servDef = ServUtils.getServDef(servId);
			String[] confArr = sqlBean.getStr("conf").split(",");
			StringBuilder transStr = new StringBuilder();
			for (int i = 0; i < confArr.length; i++) {
				String conf = confArr[i];
				transStr.append(ServUtils.getSearchWhere(servDef, conf));
			}
			return transStr.toString();
		}
		return "";
	}

	public static void clear() {
		SQL_MAP = null;
		SQL_MAP = new Bean();
	}

	public static void remove(String key) {
		SQL_MAP.remove(key);
	}

	public static void main(String[] args) throws Exception {
		SQL_MAP.put("SY_SERV-WHERE", "and SQL_SERV_ID in (^#SERV_ID#^,^#SERV_PID#^) OR SQL_SHARE=1");
		SQL_MAP.put("SY_SERV-TYPE", "and SERV_TYPE in (1,2) and PRO_FLAG=1");

		String sqlConf1 = "{type:1,key:'SY_SERV-TYPE',field:'SERV_ID',value:'SY_SERV'}";

		String sqlConf2 = "{type:2,key:'SY_SERV-WHERE',field:'SERV_ID,SERV_PID',value:'SY_SERV,SERV_PARENT'}";

		String sqlConf3 = "{type:3,field:'SERV_ID,SERV_NAME,SERV_TYPE,SERV_A,SERV_B',value:'SY_SERV,%系统配置%,aaa,bbb,ccc',conType:'=,like,>=,<=,!='}";

		String sqlConf4 = "[" + sqlConf1 + "," + sqlConf2 + "," + sqlConf3 + "]";

		System.out.println(trans(sqlConf4, "SY_SERV"));

	}
}
