package com.rh.msg;

import com.rh.food.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.pentaho.di.core.util.StringUtil;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.httpclient.HttpResponseUtils;

/*
 * kfzx-xuyj01
 * 消息处理工具类
 */
public class MsgModifyUtil {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(MsgModifyUtil.class);
	public static final String ORGANIZATION = "organization";
	public static final String USER_PRINCIPLE = "user_principal";
	public static final String USER = "user";
	public static final String ROLE = "role";
	public static final String ROLE_PRINCIPLE = "role_principal";
	public static final String POSITION = "position";
	public static final String GROUP = "group";
	public static final String CREATE_ONE = "createOne";
	public static final String LOCK_ONE = "lockOne";
	public static final String ADD_ONE = "addOne";
	public static final String ACTIVE_ONE = "activeOne";
	public static final String UPDATE_ONE = "updateOne";
	public static final String UPDATE_ONE_4ADMIN = "updateOne4Admin";
	public static final String DELETE_ONE = "deleteOne";
	public static final String REMOVE_ONE = "removeOne";

	public static final String OA_SY_ORG_DEPT = "OA_SY_ORG_DEPT";
	public static final String OA_SY_ORG_USER = "OA_SY_ORG_USER";
	public static final String OA_SY_ORG_ROLE = "OA_SY_ORG_ROLE";
	public static final String OA_SY_ORG_DEPT_USER = "OA_SY_ORG_DEPT_USER";
	public static final String OA_SY_ORG_ROLE_USER = "OA_SY_ORG_ROLE_USER";

	public static final String SY_ORG_DEPT = "SY_ORG_DEPT";
	public static final String SY_ORG_USER = "SY_ORG_USER";
	public static final String SY_ORG_DEPT_SUB = "SY_ORG_DEPT_SUB";
	public static final String SY_ORG_DEPT_ALL = "SY_ORG_DEPT_ALL";
	public static final String SY_ORG_USER_SUB = "SY_ORG_USER_SUB";
	public static final String SY_ORG_USER_ALL = "SY_ORG_USER_ALL";

	public static final String SYS_AUTH_GROUP = "SYS_AUTH_GROUP";
	public static final String SYS_AUTH_GROUP_TEMP = "SYS_AUTH_GROUP_TEMP";
	public static final String SYS_AUTH_ORG_TEMP = "SYS_AUTH_ORG_TEMP";
	public static final String SYS_AUTH_ORG = "SYS_AUTH_ORG";
	public static final String SYS_AUTH_POSITION = "SYS_AUTH_POSITION";
	public static final String SYS_AUTH_POSITION_TEMP = "SYS_AUTH_POSITION_TEMP";
	public static final String SYS_AUTH_ROLE = "SYS_AUTH_ROLE";
	public static final String SYS_AUTH_ROLEPRINCIPAL = "SYS_AUTH_ROLEPRINCIPAL";
	public static final String SYS_AUTH_ROLEPRINCIPAL_TEMP = "SYS_AUTH_ROLEPRINCIPAL_TEMP";
	public static final String SYS_AUTH_ROLE_TEMP = "SYS_AUTH_ROLE_TEMP";
	public static final String SYS_AUTH_USER = "SYS_AUTH_USER";
	public static final String SYS_AUTH_USER_ORG = "SYS_AUTH_USER_ORG";
	public static final String SYS_AUTH_USER_ORG_TEMP = "SYS_AUTH_USER_ORG_TEMP";
	public static final String SYS_AUTH_USER_TEMP = "SYS_AUTH_USER_TEMP";
	public static final String SYS_AUTH_ROLE_TO_SY_ROLE = "SYS_AUTH_ROLE_TO_SY_ROLE";
	public static final String ACTIVE_STATE = "ACTIVE";
	public static final String LOCKED_STATE = "LOCKED";
	public static final String FEMALE = "F";

	public static final String WORKING="10";// "在职";
	public static final String OUT_OF_WORKING="20";//"离职";
	public static final String RETIRE="30";//"退休";
	
	public static final String NORMAL_DEPT = "OT10";//"常规组织机构";
	public static final String LEADER_DEPT="OT20";//"组织领导";
	public static final String VM_DEPT="OT30";//"分类虚节点";
	public static final String OTHER_DEPT="OT99";//"其他";
	
	public static final String COUNTRY_LEVEL="10";//"国家级";
	public static final String PROVINCE_LEVEL="20";//"省部级";
	public static final String SI_LEVEL = "30";//"司厅局级";
	public static final String CHU_LEVEL="40";//"县处级";
	public static final String TOWN_LEVEL="50";//"乡镇科级";
	public static final String GU_LEVEL="60";//"股所级";

	public static final String CENTER_CATE="10";//"中央";
	public static final String PROVINCE_CATE="20";//"省";
	public static final String CITY_CATE = "30";//"市";
	public static final String XIAN_CATE="40";//"县";
	public static final String SPE_CATE="50";//"特派办";
	public static final String PAICHU_CATE="60";//"派出局";
	public static final String ZHISHU_CATE="70";//"直属机构";
	public static final String OTHER_CATE="90";//"其他";
	
	public static final String IS_DEPT="1";
	public static final String IS_ORG="2";
	public static final String CMPY_CODE=Context.getSyConf("MSG_CMPY_CODE", "cnao");//"其他";
	public static int PER_ROW_NUM = Context.getSyConf("PER_ROW_NUM", 1000);
	public static final String PT_ORGANIZATION= "pt_organization";

	public static final String PT_GROUP = "pt_group";//user_org统一认证表
	public static final String PT_ORG_GROUP = "pt_org_group";
	public static final String PT_USER = "pt_user";
	public static final String PT_RANK = "pt_rank";
	
	public static final String USER_DICT_CLEAN = "USER_DICT_CLEAN";
	public static final String DEPT_DICT_CLEAN = "DEPT_DICT_CLEAN";

	/*
	 * 用户数据初始化
	 */
	public static void initialUserData() {
		log.info("initialUserData start");
		Transaction.begin();
		try {
			int count = ServDao.count(MsgModifyUtil.SYS_AUTH_USER_TEMP, new Bean());
			log.info("initialUserData count : " + count);
			PER_ROW_NUM = Context.getSyConf("PER_ROW_NUM", 1000);
			int times = count / PER_ROW_NUM + 1;
			String where = "and rowid in (select rid from (" + "select rowid rid from "
					+ MsgModifyUtil.SYS_AUTH_USER_TEMP + " order by id) where rownum <= ?) ";
			for (int i = 0; i < times; i++) {
				// 按row查询用户
				SqlBean methodBean = new SqlBean();
				methodBean.appendWhere(where, PER_ROW_NUM);
				List<Bean> createUsers = ServDao.finds(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
				// 转换到对应用户表
				if (createUsers.size() > 0) {
					MsgUserData.addUserToAuth(createUsers);
					MsgUserData.addUserToSy(createUsers);
				}
				// 删除已处理数据
				methodBean = new SqlBean();
				methodBean.appendWhere(where, PER_ROW_NUM);
				ServDao.destroys(MsgModifyUtil.SYS_AUTH_USER_TEMP, methodBean);
			}

			Transaction.commit();
			log.info("initialUserData end");
		} catch (Exception e) {
			log.error("initialUserData error:" + e.getMessage());
			Transaction.rollback();
		}
		Transaction.end();
	}

	/*
	 * 机构数据初始化
	 */
	public static void initialOrgData() {
		log.info("initialOrgData start");
		Transaction.begin();
		try {
			int count = ServDao.count(MsgModifyUtil.SYS_AUTH_ORG_TEMP, new Bean());
			PER_ROW_NUM = Context.getSyConf("PER_ROW_NUM", 1000);
			int times = count / PER_ROW_NUM + 1;
			String where = "and rowid in (select rid from (" + "select rowid rid from "
					+ MsgModifyUtil.SYS_AUTH_ORG_TEMP + " order by id) where rownum <= ?) ";
			for (int i = 0; i < times; i++) {
				// 按row查询机构
				SqlBean methodBean = new SqlBean();
				methodBean.appendWhere(where, PER_ROW_NUM);
				List<Bean> createOrgs = ServDao.finds(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
				// 转换到对应机构表
				if (createOrgs.size() > 0) {
					MsgOrgData.addOrgToAuth(createOrgs);
					MsgOrgData.addOrgToSy(createOrgs);
				}
				// 删除已处理数据
				methodBean = new SqlBean();
				methodBean.appendWhere(where, PER_ROW_NUM);
				ServDao.destroys(MsgModifyUtil.SYS_AUTH_ORG_TEMP, methodBean);
			}

			Transaction.commit();
		} catch (Exception e) {
			Transaction.rollback();
		}
		Transaction.end();

		log.info("initialOrgData end");
	}

	/*
	 * 机构用户数据初始化
	 */
	public static void initialOrgUserData() {
		log.info("initialOrgUserData start");
		Transaction.begin();
		try {
			int count = ServDao.count(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP, new Bean());
			PER_ROW_NUM = Context.getSyConf("PER_ROW_NUM", 1000);
			int times = count / PER_ROW_NUM + 1;
			String sql = "select t1.USER_ID USER_ID," + "       t1.EXT_INT EXT_INT," + "       t1.ID ID,"
					+ "       t1.PRINCIPAL_CLASS PRINCIPAL_CLASS," + "       t1.PRINCIPAL_VALUE2 PRINCIPAL_VALUE2,"
					+ "       t1.PRINCIPAL_VALUE1 PRINCIPAL_VALUE1," + "       t1.EXT_INT EXT_INT,"
					+ "       t1.EXT_STR EXT_STR," + "       t2.CODE_PATH CODE_PATH,"
					+ "       t2.ODEPT_CODE ODEPT_CODE" + "  from sys_auth_user_org_temp t1, sy_org_dept t2"
					+ " where  t1.PRINCIPAL_VALUE1 = t2.DEPT_CODE" + "   and t1.PRINCIPAL_CLASS = '" + PT_ORGANIZATION
					+ "' and t1.rowid in (select rid from (" + "select rowid rid from "
					+ MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP + " order by id) where rownum <= " + PER_ROW_NUM + ")";
			String where = "and PRINCIPAL_CLASS = '" + PT_ORGANIZATION + "' and rowid in (select rid from ("
					+ "select rowid rid from " + MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP
					+ " order by id) where rownum <= ?) ";
			for (int i = 0; i < times; i++) {
				// 按row查询机构用户
				SqlBean methodBean = new SqlBean();
				methodBean.appendWhere(where, PER_ROW_NUM);
				List<Bean> createOrgUsers = Transaction.getExecutor().query(sql);
				// 转换到对应机构用户表
				if (createOrgUsers.size() > 0) {
					MsgOrgUserData.addOrgUserToAuth(createOrgUsers);
					MsgOrgUserData.addOrgUserToSy(createOrgUsers);
				}
				// 删除已处理数据
				methodBean = new SqlBean();
				methodBean.appendWhere(where, PER_ROW_NUM);
				ServDao.destroys(MsgModifyUtil.SYS_AUTH_USER_ORG_TEMP, methodBean);
			}

			Transaction.commit();
		} catch (Exception e) {
			Transaction.rollback();
		}
		Transaction.end();
		log.info("initialOrgUserData end");
	}

	/*
	 * 根据state字段转换成sflag active启用返回1，其他返回2
	 */
	public static int getSFlag(String state) {
		if (LOCKED_STATE.equals(state)) {
			return 2;
		}
		return 1;
	}

	/*
	 * 根据gender字段转换成sflag female性别女 返回1，其他返回0
	 */
	public static int getSex(String gender) {
		if (FEMALE.equals(gender)) {
			return 1;
		}
		return 0;
	}

	/*
	 * 根据last_updated字段转换成本系统日期格式
	 */
	public static String getTime(String last_updated) {
		if (StringUtil.isEmpty(last_updated)) {
			return "";
		}
		Date date = DateUtils.getDateFromString(last_updated);
		return DateUtils.getStringFromDate(date, DateUtils.FORMAT_DATETIME);
	}

	/*
	 * 获取当前本系统日期
	 */
	public static String getTime() {
		return DateUtil.getDate(DateUtil.getNowtimeStamp(), DateUtils.FORMAT_DATETIME);
	}

	/*
	 * 根据path字段转换成本系统code_path格式
	 */
	public static String getCodePath(String path, String dept_code) {
		if (StringUtil.isEmpty(path)) {
			return "";
		}
		// 删除0.两个字符
		if (path.length() < 3) {
			StringBuffer sb = new StringBuffer(dept_code);
			String result = sb.append(Constant.CODE_PATH_SEPERATOR).toString();
			return result;
		} else {
			String result = path.substring(2, path.length() - 2);
			// String result= path;
			// 将.符号替换为^
			result = result.replace(".", "^");
			StringBuffer sb = new StringBuffer(result);
			result = sb.append(Constant.CODE_PATH_SEPERATOR).append(dept_code).append(Constant.CODE_PATH_SEPERATOR)
					.toString();
			// 追加最下层子节点
			return result;
		}
	}

	/*
	 * 清空临时数据数据
	 */
	public static void cleanTempData(String tempServ) {
		SqlBean whereBean = new SqlBean();
		whereBean.andNotNull("ID");
		ServDao.destroys(tempServ, whereBean);
	}

	/*
	 * 获取用户任职状态
	 */
	public static int getUserStatus(String userStatus) {
		if (WORKING.equals(userStatus)) {
			return 1;
		}
		if (OUT_OF_WORKING.equals(userStatus)) {
			return 2;
		}
		if (RETIRE.equals(userStatus)) {
			return 3;
		}
		return 0;
	}

	/*
	 * 获取机构类型 返回值1-部门 2-机构
	 */
	public static int getDeptType(String isOrg, String type) {
		if (VM_DEPT.equals(type)) {
			return 2;
		}
		if (LEADER_DEPT.equals(type)) {
			return 1;
		}
		if (IS_DEPT.equals(isOrg)) {
			return 1;
		}
		if (IS_ORG.equals(isOrg)) {
			return 2;
		}
		return 1;
	}

	/*
	 * 获取机构分类
	 */
	public static String getDeptSign(String type) {
		if (NORMAL_DEPT.equals(type)) {
			return "OT10";
		}
		if (LEADER_DEPT.equals(type)) {
			return "OT20";
		}
		if (VM_DEPT.equals(type)) {
			return "OT30";
		}
		if (OTHER_DEPT.equals(type)) {
			return "OT99";
		}
		return "";
	}

	/*
	 * 获取机构行政级别
	 */
	public static int getOADeptLevel(String level) {
		if (COUNTRY_LEVEL.equals(level)) {
			return 10;
		}
		if (PROVINCE_LEVEL.equals(level)) {
			return 20;
		}
		if (SI_LEVEL.equals(level)) {
			return 30;
		}
		if (CHU_LEVEL.equals(level)) {
			return 40;
		}
		if (TOWN_LEVEL.equals(level)) {
			return 50;
		}
		if (GU_LEVEL.equals(level)) {
			return 60;
		}
		return 0;
	}
	
	/*
	 * 获取机构层级
	 */
	public static int getDeptLevel(String path) {
		int result = 0;
		try {
			int index = path.indexOf("^");
			if (index > 0) {
				path = path.replace("^", ",");
			}else {
				path = path.replace(".", ",");
			}
			String[] list = path.split(",");
			result = list.length;
		}catch(Exception e) {
			result = 0;
		}
		return result;
	}

	/*
	 * 获取机构组织层级
	 */
	public static String getDeptGrade(String category) {
		if (CENTER_CATE.equals(category)) {
			return "10";
		}
		if (PROVINCE_CATE.equals(category)) {
			return "20";
		}
		if (CITY_CATE.equals(category)) {
			return "30";
		}
		if (XIAN_CATE.equals(category)) {
			return "40";
		}
		if (SPE_CATE.equals(category)) {
			return "50";
		}
		if (PAICHU_CATE.equals(category)) {
			return "60";
		}
		if (ZHISHU_CATE.equals(category)) {
			return "70";
		}
		if (OTHER_CATE.equals(category)) {
			return "90";
		}
		return "0";
	}

	/*
	 * 清理机构树缓存
	 */
	public static void cleanDeptDictCache() {
		ServDefBean servDefAll = ServUtils.getServDef(SY_ORG_DEPT_ALL);
		ServDefBean servDefSub = ServUtils.getServDef(SY_ORG_DEPT_SUB);
		ServDefBean servDef = ServUtils.getServDef(SY_ORG_DEPT);
		servDef.clearDictCache();
		servDefAll.clearDictCache();
		servDefSub.clearDictCache();
	}

	/*
	 * 清理用户缓存
	 */
	public static void cleanUserDictCache() {
		ServDefBean servDefAll = ServUtils.getServDef(SY_ORG_USER_ALL);
		ServDefBean servDefSub = ServUtils.getServDef(SY_ORG_USER_SUB);
		ServDefBean servDef = ServUtils.getServDef(SY_ORG_USER);
		servDef.clearDictCache();
		servDefAll.clearDictCache();
		servDefSub.clearDictCache();
	}

	/*
	 * 发送清理用户缓存消息
	 */
	public static void cleanAllServUserCache(String userCode) {
		try {
			String uriList = Context.getSyConf("OA_ALL_SERV_URL", "localhost:8083");
			if ("".equals(uriList) || uriList.isEmpty()) {
				log.error("cleanAllServUserCache  error OA_ALL_SERV_URL:null uri");
				return;
			}
			String[] uris = uriList.split(",");
			for (int i = 0; i < uris.length; i++) {
				String uri = "http://" + uris[i] + "/oa/OA_SY_COMM_MSG_LISTENER.cleanDeptDictCache.do";
				// String uri =
				// "http://122.18.157.139:9081/icbc/roa/CAL_SCH_INFO.actSchedule.do";
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(uri);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("USER_CODE", userCode));
				httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				HttpResponse response;
				response = httpClient.execute(httpPost);
				log.error("cleanAllServUserCache  success");
			}

		} catch (Exception e) {
			log.error("cleanAllServUserCache  error" + e.getMessage());
		}
	}

	/*
	 * 发送清理机构缓存消息
	 */
	public static void cleanAllServDeptCache() {
		try {
			String uriList = Context.getSyConf("OA_ALL_SERV_URL", "localhost:8083");
			if ("".equals(uriList) || uriList.isEmpty()) {
				log.error("cleanAllServDeptCache  error OA_ALL_SERV_URL:null uri");
				return;
			}
			String[] uris = uriList.split(",");
			for (int i = 0; i < uris.length; i++) {
				String uri = "http://" + uris[i] + "/oa/OA_SY_COMM_MSG_LISTENER.cleanDeptDictCache.do";
				// String uri =
				// "http://122.18.157.139:9081/icbc/roa/CAL_SCH_INFO.actSchedule.do";
				@SuppressWarnings({ "deprecation", "resource" })
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(uri);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				HttpResponse response;
				response = httpClient.execute(httpPost);
				String result = HttpResponseUtils.getResponseContent(response);

				log.error("cleanAllServDeptCache  success" + result);
			}

		} catch (Exception e) {
			log.error("cleanAllServDeptCache  error" + e.getMessage());
		}
	}

	/*
	 * 重构机构树 参数：机构或者部室，默认参数机构和部室已经为机构树处理完成状态 如果是参数是新增的机构和部室，需要手动处理参数机构和部室
	 */
	public static void modifyCodePathByRoot(String rootDeptCode) {
		log.info("modifyCodePathByRoot start root: " + rootDeptCode);
		SqlBean sql = new SqlBean();
		sql.and("DEPT_CODE", rootDeptCode);
		sql.and("S_FLAG", 1);
		List<String> sqlList = new ArrayList<String>();
		Bean rootBean = ServDao.find(OA_SY_ORG_DEPT, sql);
		if (rootBean.isEmpty()) {
			log.info("modifyCodePathByRoot rootBean empty: " + rootDeptCode);
			return;
		}
		// 处理机构
		if (2 == rootBean.getInt("DEPT_TYPE")) {
			String codePath = rootBean.getStr("CODE_PATH");
			String deptPcode = rootDeptCode;
			String odeptCode = rootDeptCode;
			String tdeptCode = "";
			// 机构类型 更新root节点
			// 向下递归更新每个节点的codepath和odept、tdept
			buildList(codePath, sqlList, deptPcode, odeptCode, tdeptCode);
			// 处理部室
		} else if (1 == rootBean.getInt("DEPT_TYPE")) {
			String codePath = rootBean.getStr("CODE_PATH");
			String deptPcode = rootDeptCode;
			String odeptCode = rootBean.getStr("ODEPT_CODE");
			String tdeptCode = rootDeptCode;
			// 向下递归更新每个节点的codepath和odept、tdept
			buildList(codePath, sqlList, deptPcode, odeptCode, tdeptCode);
		} else {
			log.info("modifyCodePathByRoot rootBean out of org: " + rootDeptCode);
			return;
		}
		log.info("modifyCodePathByRoot sqllist finish size: " + sqlList.size());

		String[] sqlStringList = new String[sqlList.size()];
		int i = 0;
		for (String usql : sqlList) {
			sqlStringList[i++] = usql;
		}
		Transaction.begin();
		try {
			Transaction.getExecutor().executeBatch(sqlStringList);
			Transaction.commit();
		} catch (Exception e) {
			Transaction.rollback();
		}
		Transaction.end();
		log.info("modifyCodePathByRoot end root: " + rootDeptCode);
	}

	public static void modifyCodePathByRoot() {
		log.info("modifyCodePathByRoot list start");
		SqlBean sql = new SqlBean();
		sql.and("S_FLAG", 1);
		sql.andNull("DEPT_PCODE");
		List<String> sqlList = new ArrayList<String>();
		List<Bean> rootBeanList = ServDao.finds(OA_SY_ORG_DEPT, sql);
		/*
		 * if (rootBean.isEmpty()) { log.info("modifyCodePathByRoot rootBean empty: " +
		 * rootDeptCode); return; }
		 */
		for (Bean rootBean : rootBeanList) {
			// 处理机构
			if (2 == rootBean.getInt("DEPT_TYPE")) {
				String codePath = rootBean.getStr("CODE_PATH");
				String deptPcode = rootBean.getStr("DEPT_CODE");
				String odeptCode = rootBean.getStr("ODEPT_CODE");
				if ("".equals(odeptCode)) {
					odeptCode = rootBean.getStr("DEPT_CODE");
				}
				String tdeptCode = rootBean.getStr("TDEPT_CODE");
				if ("".equals(tdeptCode)) {
					odeptCode = rootBean.getStr("DEPT_CODE");
				}
				int deptLevel = getDeptLevel(codePath);
				// 机构类型 更新root节点
				String sqlString = "update SY_ORG_DEPT set CODE_PATH= '" + codePath + "', ODEPT_CODE = '" + odeptCode
						+ "',TDEPT_CODE = '" + tdeptCode + "',DEPT_LEVEL = " + deptLevel+" WHERE DEPT_CODE = '" + rootBean.getStr("DEPT_CODE") + "'";
				sqlList.add(sqlString);
				// 向下递归更新每个节点的codepath和odept、tdept
				buildList(codePath, sqlList, deptPcode, odeptCode, tdeptCode);
				// 处理部室,正常不会走该分支
			} else if (1 == rootBean.getInt("DEPT_TYPE")) {
				String codePath = rootBean.getStr("CODE_PATH");
				String deptPcode = rootBean.getStr("DEPT_CODE");
				String odeptCode = rootBean.getStr("ODEPT_CODE");
				String tdeptCode = rootBean.getStr("TDEPT_CODE");
				if ("".equals(tdeptCode)) {
					odeptCode = rootBean.getStr("DEPT_CODE");
				}
				// 向下递归更新每个节点的codepath和odept、tdept
				buildList(codePath, sqlList, deptPcode, odeptCode, tdeptCode);
			} else {
				log.info("modifyCodePathByRoot rootBean out of org: " + rootBean.getStr("DEPT_CODE"));
				return;
			}
		}
		log.info("modifyCodePathByRoot sqllist finish size: " + sqlList.size());

		String[] sqlStringList = new String[sqlList.size()];
		int i = 0;
		for (String usql : sqlList) {
			sqlStringList[i++] = usql;
		}

		Transaction.begin();
		try {
			Transaction.getExecutor().executeBatch(sqlStringList);
			Transaction.commit();
		} catch (Exception e) {
			Transaction.rollback();
		}
		Transaction.end();
		log.info("modifyCodePathByRoot end rootlist ");
	}

	private static void buildList(String path, List<String> sqlList, String deptPcode, String odeptCode,
			String tdeptCode) {

		SqlBean sql = new SqlBean();
		sql.and("DEPT_PCODE", deptPcode);
		sql.and("S_FLAG", 1);
		List<Bean> tdeptList = ServDao.finds(OA_SY_ORG_DEPT, sql);
		if (tdeptList.size() > 0) {
			log.info("modifyCodePathByRoot buildList DEPT_PCODE: " + deptPcode);
		}
		for (Bean tdept : tdeptList) {
			String deptCode = tdept.getStr("DEPT_CODE");
			String codePath = path + deptCode + Constant.CODE_PATH_SEPERATOR;
			int deptLevel = getDeptLevel(codePath);
			// 如果是机构 ，向下级更新数据的时候用当前最新odept和tdept
			if (2 == tdept.getInt("DEPT_TYPE")) {
				String odept = deptCode;
				String sqlString = "update SY_ORG_DEPT set CODE_PATH= '" + codePath + "', ODEPT_CODE = '" + odept
						+ "',TDEPT_CODE = '" + deptCode + "',DEPT_LEVEL=" + deptLevel+ " WHERE DEPT_CODE = '" + deptCode + "'";
				sqlList.add(sqlString);

				buildList(codePath, sqlList, deptCode, deptCode, deptCode);
			}

			else if (odeptCode.equals(tdept.getStr("DEPT_PCODE"))) {// 如果是机构下一层部门，当成部室处理，向下级更新数据的时候用当前最新tdept
				String sqlString = "update SY_ORG_DEPT set CODE_PATH= '" + codePath + "', ODEPT_CODE = '" + odeptCode
						+ "',TDEPT_CODE = '" + deptCode + "' ,DEPT_LEVEL=" + deptLevel+ " WHERE DEPT_CODE = '" + deptCode + "'";
				sqlList.add(sqlString);
				buildList(codePath, sqlList, deptCode, odeptCode, deptCode);
			} else {// 如果是二层及以上部门，向下级更新时保持tdept、odept不变
				String sqlString = "update SY_ORG_DEPT set CODE_PATH= '" + codePath + "', ODEPT_CODE = '" + odeptCode
						+ "',TDEPT_CODE = '" + tdeptCode + "' ,DEPT_LEVEL=" + deptLevel+ " WHERE DEPT_CODE = '" + deptCode + "'";
				sqlList.add(sqlString);
				buildList(codePath, sqlList, deptCode, odeptCode, tdeptCode);
			}

		}
	}
	//修复特殊机构，将其挂载到指定机构下一层
	public static void modifySpecialDept() {
		String specialDept = Context.getSyConf("OA_SPECIAL_DEPT", "SHULD");
		String toDistOrg = Context.getSyConf("OA_TO_DIST_ORG", "3814431db559000");
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("DEPT_CODE", toDistOrg);
		//查询指定挂载节点
		Bean distOrgBean = ServDao.find(OA_SY_ORG_DEPT, sqlBean) ;
		if (null ==distOrgBean ||  distOrgBean.isEmpty()) {
			return;
		}
		sqlBean = new SqlBean();
		sqlBean.and("DEPT_CODE",specialDept);
		sqlBean.set("DEPT_PCODE", toDistOrg);
		sqlBean.set("ODEPT_CODE", toDistOrg);
		sqlBean.set("CODE_PATH", distOrgBean.getStr("CODE_PATH")+specialDept +"^");
		ServDao.update(OA_SY_ORG_DEPT, sqlBean);
	}
}