package com.rh.core.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import com.oscar.jdbcx.Jdbc3SimpleDataSource;
import com.rh.core.base.db.DbType;
import com.rh.core.base.db.SqlBuilder;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.impl.CommonBuilder;
import com.rh.core.base.db.impl.CommonExecutor;
import com.rh.core.base.db.impl.H2Builder;
import com.rh.core.base.db.impl.H2Executor;
import com.rh.core.base.db.impl.MssqlBuilder;
import com.rh.core.base.db.impl.MssqlExecutor;
import com.rh.core.base.db.impl.MysqlBuilder;
import com.rh.core.base.db.impl.MysqlExecutor;
import com.rh.core.base.db.impl.OracleBuilder;
import com.rh.core.base.db.impl.OracleExecutor;
import com.rh.core.base.db.impl.OscarBuilder;
import com.rh.core.base.db.impl.OscarExecutor;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;

/**
 * 系统的总体控制类，管理各种系统级变量。
 * 
 * @author wanglong
 * @version $Id$
 */
public class BaseContext {

	/**
	 * 构建体方法
	 */
	BaseContext() {
	}

	/** 数据源类型，及对应的实现类 */
	public enum DB_TYPE implements DbType {
		/** ORACLE */
		ORACLE {
			/**
			 * 获取sqlBuilder对象
			 * 
			 * @return sqlBuilder对象
			 */
			public SqlBuilder getBuilder() {
				return OracleBuilder.getBuilder();
			}

			/**
			 * 获取sqlExecutor对象
			 * 
			 * @return sqlExecutor对象
			 */
			public SqlExecutor getExecutor() {
				return OracleExecutor.getExecutor();
			}
		},
		/** MSSQL */
		MSSQL {
			/**
			 * 获取sqlBuilder对象
			 * 
			 * @return sqlBuilder对象
			 */
			public SqlBuilder getBuilder() {
				return MssqlBuilder.getBuilder();
			}

			/**
			 * 获取sqlExecutor对象
			 * 
			 * @return sqlExecutor对象
			 */
			public SqlExecutor getExecutor() {
				return MssqlExecutor.getExecutor();
			}
		},
		/** MYSQL */
		MYSQL {
			/**
			 * 获取sqlBuilder对象
			 * 
			 * @return sqlBuilder对象
			 */
			public SqlBuilder getBuilder() {
				return MysqlBuilder.getBuilder();
			}

			/**
			 * 获取sqlExecutor对象
			 * 
			 * @return sqlExecutor对象
			 */
			public SqlExecutor getExecutor() {
				return MysqlExecutor.getExecutor();
			}
		},
		/** H2 */
		H2 {
			/**
			 * 获取sqlBuilder对象
			 * 
			 * @return sqlBuilder对象
			 */
			public SqlBuilder getBuilder() {
				return H2Builder.getBuilder();
			}

			/**
			 * 获取sqlExecutor对象
			 * 
			 * @return sqlExecutor对象
			 */
			public SqlExecutor getExecutor() {
				return H2Executor.getExecutor();
			}
		},
		/** 国产神通数据库 */
        OSCAR {
            /**
             * 获取sqlBuilder对象
             * @return sqlBuilder对象
             */
            public SqlBuilder getBuilder() {
                return OscarBuilder.getBuilder();
            }
            /**
             * 获取sqlExecutor对象
             * @return sqlExecutor对象
             */
            public SqlExecutor getExecutor() {
                return OscarExecutor.getExecutor();
            }
        },
		/** DB2 */
		DB2 {
			/**
			 * 获取sqlBuilder对象
			 * 
			 * @return sqlBuilder对象
			 */
			public SqlBuilder getBuilder() {
				return CommonBuilder.getBuilder();
			}

			/**
			 * 获取sqlExecutor对象
			 * 
			 * @return sqlExecutor对象
			 */
			public SqlExecutor getExecutor() {
				return CommonExecutor.getExecutor();
			}
		},
		/** OTHER */
		OTHER {
			/**
			 * 获取sqlBuilder对象
			 * 
			 * @return sqlBuilder对象
			 */
			public SqlBuilder getBuilder() {
				return CommonBuilder.getBuilder();
			}

			/**
			 * 获取sqlExecutor对象
			 * 
			 * @return sqlExecutor对象
			 */
			public SqlExecutor getExecutor() {
				return CommonExecutor.getExecutor();
			}
		};
	};

	/** 应用变量访问项 */
	public enum APP {
		/** 系统路径 */
		SYSPATH,
		/** rtx api对象 */
		IM,
		/** 虚路径 */
		CONTEXTPATH,
		/** 系统resources路径 */
		RESOURCESPATH,
		/** 系统WEB-INF路径 */
		WEBINF,
		/** 系统WEB-INF下的doc路径 */
		WEBINF_DOC,
		/** 系统WEB-INF下的doc下的cmpy路径 */
		WEBINF_DOC_CMPY,
		/** HTTP全路径头 */
		HTTP_URL,
		/** 工程DOC目录 */
		PRO_SRC_DOC,
		/** 工程WEB目录 */
		PRO_SRC_WEB,
	}

	/** 数据源访问项 */
	public enum DS {
		/** 数据源名称，缺省数据源为""字符串 */
		NAME,
		/** 数据源全路径名称，包含各应用服务器约定的JNDI前缀 */
		FULL_NAME,
		/** 数据所属用户名 */
		USER_NAME,
		/** 数据源类型 */
		DB_TYPE,
		/** JDBC URL */
		URL,
		/** SCHEMA对象 */
		SCHEMA,
		/** 数据源对象 */
		DS
	}

	/** 线程变量访问项 */
	public enum THREAD {
		/** 事务列表 */
		TRANSLIST,
		/** 参数信息 */
		PARAMBEAN,
		/** 服务编码 */
		SERVID,
		/** 用户信息 */
		USERBEAN,
		/** SessionID */
		SESSIONID,
		/** 公司编码 */
		CMPYCODE,
		/** LOG_DEBUG标志 */
		LOGDEBUG
	}

	/** 系统参数名称：log */
	public static final String SYS_PARAM_LOG = "log";
	/** 系统参数名称：JNDI前缀 */
	public static final String SYS_PARAM_JNDI_PREFIX = "jndi_prefix";
	/** 系统参数名称：数据源前缀 */
	public static final String SYS_PARAM_DATASOURCE_PREFIX = "datasource_prefix";
	/** 系统参数名称：启动监听类 */
	public static final String SYS_PARAM_LISTENER = "listener";
	/** conf serv文件定义 */
	public static final String SY_PARAM_FROM_FILE1 = "SERV_FILE";

	/** 数据源名称 */
	private static HashMap<String, Bean> dataSourceMap = new HashMap<String, Bean>();

	/** 系统级参数信息 */
	private static Bean appBean = null;

	/**
	 * 线程级变量
	 */
	private static ThreadLocal<Bean> thread = new ThreadLocal<Bean>() {
		public Bean initialValue() {
			return new Bean();
		}
	};

	/**
	 * 清理ThreadLocal中的数据
	 */
	public static void cleanThreadData() {
		thread.remove();
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @return 变量信息
	 */
	public static Object app(Object key) {
		return appBean.get(key);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @param def
	 *            缺省值
	 * @return 变量信息
	 */
	public static String app(Object key, String def) {
		return appBean.get(key, def);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @param def
	 *            缺省值
	 * @return 变量信息
	 */
	public static int app(Object key, int def) {
		return appBean.get(key, def);
	}

	/**
	 * 设置系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @param value
	 *            变量信息
	 */
	public static void setApp(Object key, Object value) {
		appBean.set(key, value);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @return 变量信息字符串
	 */
	public static String appStr(Object key) {
		return appBean.getStr(key);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @return int类型变量信息
	 */
	public static int appInt(Object key) {
		return appBean.getInt(key);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @return boolean类型变量信息
	 */
	public static Boolean appBoolean(Object key) {
		return appBean.getBoolean(key);
	}

	/**
	 * 获取appBean信息
	 * 
	 * @return appBean信息
	 */
	public static Bean getAppBean() {
		return appBean;
	}

	/**
	 * 获取线程级实例对象
	 * 
	 * @return rtnBean rtnBean
	 */
	private static Bean threadBean() {
		return thread.get();
	}

	/**
	 * 对应线程变量是否为空
	 * 
	 * @param key
	 *            键值
	 * @return 是否为空
	 */
	public static boolean isEmpytyThread(Object key) {
		return threadBean().isEmpty(key);
	}

	/**
	 * 清除对应的线程变量
	 * 
	 * @param key
	 *            键值
	 */
	public static void removeThread(Object key) {
		threadBean().remove(key);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @return 变量信息字符串
	 */
	public static Object getThread(Object key) {
		return threadBean().get(key);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @param def
	 *            缺省值
	 * @return 变量信息字符串
	 */
	public static String getThread(Object key, String def) {
		return threadBean().get(key, def);
	}

	/**
	 * 获取系统变量信息（布尔值）
	 * 
	 * @param key
	 *            变量键值
	 * @param def
	 *            缺省值
	 * @return 变量信息字符串
	 */
	public static boolean getThread(Object key, boolean def) {
		return threadBean().get(key, def);
	}

	/**
	 * 获取系统变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @return 变量信息字符串
	 */
	public static String getThreadStr(Object key) {
		return threadBean().getStr(key);
	}

	/**
	 * 设置线程变量信息
	 * 
	 * @param key
	 *            变量键值
	 * @param value
	 *            变量信息
	 * @return 本线程对象，支持级联设置
	 */
	public static Bean setThread(Object key, Object value) {
		return threadBean().set(key, value);
	}

	/**
	 * 系统启动，装载必须的应用
	 * 
	 * @param confBean
	 *            初始配置信息，需要系统路径、启动类名列表和数据源列表
	 */
	public static synchronized void start(Bean confBean) {
		appBean = confBean;
		String webinf = appBean.getStr(APP.WEBINF);
		String webinfoDoc = webinf + "doc" + Constant.PATH_SEPARATOR;
		String webinfoDocCmpy = webinfoDoc + "cmpy" + Constant.PATH_SEPARATOR;
		// appBean.set(APP.WEBINF, webinf);
//		Properties prop = getProperties(app(APP.WEBINF) + "rh.properties");
		appBean.set(APP.WEBINF_DOC, webinfoDoc);
		appBean.set(APP.WEBINF_DOC_CMPY, webinfoDocCmpy);
//		appBean.set(APP.PRO_SRC_DOC, prop.getProperty("PRO_SRC_DOC", ""));
//		appBean.set(APP.PRO_SRC_WEB, prop.getProperty("PRO_SRC_WEB", ""));

		String[] listeners = confBean.getStr("listener").split(",");
		for (String listener : listeners) { // 依次执行启动监听类
			try {
				if (listener.length() > 0) {
					System.out.println("listener: " + listener);
					Lang.doMethod(listener, "start");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 系统关闭，关闭必须关闭的资源
	 */
	protected static synchronized void stop() {
		String[] listeners = appStr(SYS_PARAM_LISTENER).split(",");
		for (String listener : listeners) { // 依次执行启动监听类
			if (listener.length() > 0) {
				Lang.doMethod(listener, "stop");
			}
		}
	}

	/**
	 * 获取系统缺省对应数据源。
	 * 
	 * @return 对应数据源
	 */
	public static Bean getDSBean() {
		return getDSBean("");
	}

	/**
	 * 获取所有数据源名称列表。
	 * 
	 * @return 数据源名称集合
	 */
	public static Set<String> getDSNames() {
		return dataSourceMap.keySet();
	}

	/**
	 * 获取指定的数据源。
	 * 
	 * @param dsName
	 *            数据源名称
	 * @return 对应数据源
	 */
	public static Bean getDSBean(String dsName) {
		if (dataSourceMap.containsKey(dsName)) {
			return dataSourceMap.get(dsName);
		} else {
			return null;
		}
	}

	/**
	 * 获取系统缺省数据源的用户名
	 * 
	 * @return 缺省数据源用户名
	 */
	public static String getDBUserName() {
		return getDSBean().getStr(DS.USER_NAME);
	}

	/**
	 * 获取指定数据源的用户名
	 * 
	 * @param dsName
	 *            数据源名称
	 * @return 指定数据源用户名
	 */
	public static String getDBUserName(String dsName) {
		return getDSBean(dsName).getStr(DS.USER_NAME);
	}

	/**
	 * 得到缺省数据源的连接
	 * 
	 * @return 数据库连接
	 */
	public static Connection getConn() {
		return getConn("");
	}

	/**
	 * 得到指定数据源的连接
	 * 
	 * @param dsName
	 *            指定数据源名称
	 * @return 数据库连接
	 */
	public static Connection getConn(String dsName) {
		try {
			Connection conn = ((DataSource) getDSBean(dsName).get(DS.DS)).getConnection();
			return conn;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 关闭数据库连接数据源的连接
	 * 
	 * @param conn
	 *            数据库连接
	 */
	public static void endConn(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 得到缺省数据源的SQL执行器
	 * 
	 * @return SQS执行器
	 */
	public static SqlExecutor getExecutor() {
		return getExecutor("");
	}

	/**
	 * 得到指定数据源的SQL执行器
	 * 
	 * @param dsName
	 *            数据源名称
	 * @return SQS执行器
	 */
	public static SqlExecutor getExecutor(String dsName) {
		return ((DbType) getDSBean(dsName).get(DS.DB_TYPE)).getExecutor();
	}

	/**
	 * 得到缺省数据源的SQL执行器
	 * 
	 * @return SQS执行器
	 */
	public static SqlBuilder getBuilder() {
		return ((DbType) getDSBean().get(DS.DB_TYPE)).getBuilder();
	}

	/**
	 * 得到指定数据库类型的SQL执行器
	 * 
	 * @param type
	 *            数据库类型，要求全部大写
	 * @return SQS执行器
	 */
	public static SqlBuilder getBuilderByType(String type) {
		if (type.isEmpty()) {
			return getBuilder();
		} else {
			return ((DbType) DB_TYPE.valueOf(type)).getBuilder();
		}
	}

	/**
	 * 设置数据源
	 * 
	 * @param dsName
	 *            数据源名称
	 * @param dsBean
	 *            数据源信息
	 */
	public static void setDSBean(String dsName, Bean dsBean) {
		dataSourceMap.put(dsName, dsBean);
	}

	/**
	 * 得到系统启动配置信息，此配置信息在web.xml中设定
	 * 
	 * @param name
	 *            配置项名称
	 * @return 配置项对应的值
	 */
	public static String getInitConfig(String name) {
		return appStr(name);
	}

	/**
	 * 得到系统启动配置信息，此配置信息在web.xml中设定
	 * 
	 * @param name
	 *            配置项名称
	 * @param def
	 *            缺省值
	 * @return 配置项对应的值
	 */
	public static int getInitConfig(String name, int def) {
		int value = appInt(name);
		if (value == 0) {
			value = def;
		}
		return value;
	}

	/**
	 * 得到系统启动配置信息，此配置信息在web.xml中设定
	 * 
	 * @param name
	 *            配置项名称
	 * @param def
	 *            缺省值
	 * @return 配置项对应的值
	 */
	public static String getInitConfig(String name, String def) {
		String value = (String) app(name);
		if (value == null || value.isEmpty()) {
			value = def;
		}
		return value;
	}

	/**
	 * 从线程获取 参数信息
	 * 
	 * @return 参数信息
	 */
	public static Bean getParamBean() {
		return (Bean) getThread(THREAD.PARAMBEAN);
	}

	/**
	 * 设置request对象到线程级变量供userInfo等使用
	 * 
	 * @param req
	 *            request对象
	 */
	public static void setRequest(HttpServletRequest req) {
		if ((req != null) && (req instanceof HttpServletRequest)) {
			setThread("^^REQUEST^^", req);
		}
	}

	/**
	 * 设置response对象到线程级变量供下载等使用
	 * 
	 * @param res
	 *            response对象
	 */
	public static void setResponse(HttpServletResponse res) {
		if ((res != null) && (res instanceof HttpServletResponse)) {
			setThread("^^RESPONSE^^", res);
		}
	}

	/**
	 * 获取response对象到线程级变量供下载等使用
	 * 
	 * @return response对象
	 */
	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) getThread("^^RESPONSE^^");
	}

	/**
	 * 获取request对象到线程级变量供下载等使用
	 * 
	 * @return request对象
	 */
	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) getThread("^^REQUEST^^");
	}

	/**
	 * 根据属性文件地址（全路径地址）获取属性文件内容
	 * 
	 * @param fileName
	 *            属性文件全路径地址
	 * @return 属性文件内容
	 */
	public static Properties getProperties(String fileName) {
		Properties prop = new Properties();
		FileInputStream input = null;
		try {
			input = new FileInputStream(fileName);
			prop.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}

	/**
	 * 增加数据源
	 * 
	 * @param fullName
	 *            数据源全称
	 * @param name
	 *            数据源名称
	 * @param url
	 *            数据源URL
	 * @param userName
	 *            数据源对应的用户名
	 * @param isDefaultDs
	 *            是否是默认数据源
	 * @param ds
	 *            数据源
	 * @param dbType
	 *            数据类型
	 * @param schema
	 *            数据库默认Schema名
	 **/
	public static void addDataSource(String fullName, String name, String url, String userName, boolean isDefaultDs,
			DataSource ds, DbType dbType, String schema) {
		if (dbType != null) {

		} else if (url.indexOf("oracle") >= 0) {
			dbType = DB_TYPE.ORACLE;
			schema = userName;// oracle schema为用户名
		} else if (url.indexOf("sqlserver") >= 0) {
			dbType = DB_TYPE.MSSQL;
		} else if (url.indexOf("mysql") >= 0) {
			dbType = DB_TYPE.MYSQL;
			Pattern pattern = Pattern.compile(".*/(\\w+)\\??.*");
			Matcher mt = pattern.matcher(url); // 在URL上通过正则表达式查找数据库名
			if (mt.find()) {
				schema = mt.group(1); // mysql schema为数据库名
			}
		} else if (url.indexOf("h2") >= 0) {
			dbType = DB_TYPE.H2;
			Pattern pattern = Pattern.compile(".*/(\\w+)\\??.*");
			Matcher mt = pattern.matcher(url); // 在URL上通过正则表达式查找数据库名
			if (mt.find()) {
				schema = mt.group(1);
			}
		} else if (url.indexOf("db2") >= 0) {
			dbType = DB_TYPE.DB2;
		}  else if (url.indexOf("oscar") >= 0) {
			dbType = DB_TYPE.OSCAR;
		} else {
			dbType = DB_TYPE.OTHER;
		}

		Bean dsBean = new Bean();
		dsBean.set(DS.DB_TYPE, dbType);
		if (StringUtils.isNotEmpty(schema)) {
			dsBean.set(DS.SCHEMA, schema);
		}
		dsBean.set(DS.NAME, name);
		dsBean.set(DS.FULL_NAME, fullName);
		dsBean.set(DS.DS, ds);
		dsBean.set(DS.USER_NAME, userName);
		dsBean.set(DS.URL, url);
		BaseContext.setDSBean(name, dsBean); // 放入应用级变量
		if (isDefaultDs) {
			BaseContext.setDSBean("", dsBean); // 将缺省数据源放入应用级变量
		}
	}

	/**
	 * 增加数据源
	 * 
	 * @param fullName
	 *            数据源全称
	 * @param name
	 *            数据源名称
	 * @param url
	 *            数据源URL
	 * @param userName
	 *            数据源对应的用户名
	 * @param isDefaultDs
	 *            是否是默认数据源
	 * @param ds
	 *            数据源
	 * @param dbType
	 *            数据类型
	 * @param schema
	 *            数据库默认Schema名
	 **/
	public static void addOscarDataSource(String fullName, String name, String url, String userName, boolean isDefaultDs,
			Jdbc3SimpleDataSource ds, DbType dbType, String schema) {
		if (dbType != null) {

		} else if (url.indexOf("oracle") >= 0) {
			dbType = DB_TYPE.ORACLE;
			schema = userName;// oracle schema为用户名
		} else if (url.indexOf("sqlserver") >= 0) {
			dbType = DB_TYPE.MSSQL;
		} else if (url.indexOf("mysql") >= 0) {
			dbType = DB_TYPE.MYSQL;
			Pattern pattern = Pattern.compile(".*/(\\w+)\\??.*");
			Matcher mt = pattern.matcher(url); // 在URL上通过正则表达式查找数据库名
			if (mt.find()) {
				schema = mt.group(1); // mysql schema为数据库名
			}
		} else if (url.indexOf("h2") >= 0) {
			dbType = DB_TYPE.H2;
			Pattern pattern = Pattern.compile(".*/(\\w+)\\??.*");
			Matcher mt = pattern.matcher(url); // 在URL上通过正则表达式查找数据库名
			if (mt.find()) {
				schema = mt.group(1);
			}
		} else if (url.indexOf("db2") >= 0) {
			dbType = DB_TYPE.DB2;
		}  else if (url.indexOf("oscar") >= 0) {
			dbType = DB_TYPE.OSCAR;
		}  else {
			dbType = DB_TYPE.OTHER;
		}

		Bean dsBean = new Bean();
		dsBean.set(DS.DB_TYPE, dbType);
		if (StringUtils.isNotEmpty(schema)) {
			dsBean.set(DS.SCHEMA, schema);
		}
		dsBean.set(DS.NAME, name);
		dsBean.set(DS.FULL_NAME, fullName);
		dsBean.set(DS.DS, ds);
		dsBean.set(DS.USER_NAME, userName);
		dsBean.set(DS.URL, url);
		BaseContext.setDSBean(name, dsBean); // 放入应用级变量
		if (isDefaultDs) {
			BaseContext.setDSBean("", dsBean); // 将缺省数据源放入应用级变量
		}
	}
}
