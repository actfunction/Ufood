package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.DateUtils;
import com.rh.sup.util.SupConstant;
import com.tongtech.backport.java.util.Arrays;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SupApproGain extends CommonServ {

	private final static String GAINSTATE = "1";
	private final static String SUP_APPRO_OFFICE = "OA_SUP_APPRO_OFFICE";
	private final static String SUP_APPRO_BUREAU = "OA_SUP_APPRO_BUREAU";
	private final static String SUP_APPRO_POINT = "OA_SUP_APPRO_POINT";
	private final static String SUP_APPRO_GAIN = "OA_SUP_APPRO_GAIN";
	private final static String HOST_DEPT_LIMIT_TIME = "HostDeptLimitTime";

	/**
	 * 根据立项主键获取上月办理情况
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean getAlike(ParamBean paramBean) {

		// 构建返回值bena
		OutBean outBean = new OutBean();
		outBean.set("code", "404");

		// 获取立项单主键
		String appro_id = paramBean.getStr("APPRO_ID");

		// 获取查询类型（署发，司内，要点类）；
		String servId = paramBean.getStr("servId");

		// 获取当前用户信息
		UserBean userBean = Context.getUserBean();
		// 获取用户部门编码
		String deptCode = userBean.getDeptCode();
		// 获取用的父级部门code
		String parent = userBean.getTDeptCode();

		// 根据类型查询不同sql得到不同结果
		// 构建sql语句
		StringBuffer sql = new StringBuffer();
		// 署发立项
		if (SUP_APPRO_OFFICE.equals(servId)) {
			sql.append("select * from SUP_APPRO_GAIN WHERE APPRO_ID = '").append(appro_id).append("' and DEPT_CODE = '")
					.append(parent);
			// 司内
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			sql.append("select * from SUP_APPRO_GAIN WHERE APPRO_ID = '").append(appro_id).append("' and DEPT_CODE = '")
					.append(deptCode);
			// 要点类
		} else if (SUP_APPRO_POINT.equals(servId)) {
			sql.append("select * from SUP_APPRO_GAIN where APPRO_ID = '").append(appro_id);
		}
		sql.append("' and GAIN_STATE = '3' order by  TO_DATE(GAIN_MONTH,'yyyy-MM' ) desc");

		// 执行sql语句得到结果
		List<Bean> result = Transaction.getExecutor().query(sql.toString());

		// 3.判断结果是否为空
		if (result != null && result.size() > 0) {
			outBean.set("code", "200");
			outBean.set("data", result.get(0));
		}
		// 4.返回结果
		return outBean;
	}

	/**
	 * 保存后执行的方法
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean updateGain(ParamBean paramBean) {
		// 获取办理情况主键id
		String id = paramBean.getStr("ID");

		// 获取查询类型
		String servId = paramBean.getStr("servId");

		// 根据主键获取到bean
		Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, id);

		// 获取用户信息
		UserBean userBean = Context.getUserBean();

		System.out.println(supApproGain.getStr("DEPT_CODE").isEmpty());

		// 更新主办机构
		if (supApproGain.getStr("DEPT_CODE").isEmpty()) {
			supApproGain.set("DEPT_CODE", getDepeCode(servId));

		}
		// 执行更新操作
		ServDao.update(SUP_APPRO_GAIN, supApproGain);
		return new OutBean().setOk();
	}

	/**
	 * 取出部门
	 * 
	 * @param servId
	 * @return
	 */
	private String getDepeCode(String servId) {

		// 获取当前用户userBean
		UserBean userBean = Context.getUserBean();

		String DeptCode = userBean.getDeptCode();
		// 判断司内 还是 署发
		if (SupConstant.OA_SUP_APPRO_OFFICE.equals(servId)) {
			// 取出机构id
			DeptCode = userBean.getTDeptCode();
		}
		return DeptCode;
	}

	/**
	 * 更新当前办理情况的机构编码
	 *
	 * @param servId
	 * @param supApproGain
	 * @param userBean
	 * @return
	 */
	private Bean updateDepeCode(String servId, Bean supApproGain, UserBean userBean) {
		// 获取当前用户部门信息
		String codePath = userBean.getCodePath();
		// 根据当前主单类型查询不同的子服务
		List<Bean> result = findAppro(servId, supApproGain.getStr("APPRO_ID"));
		if (result != null) {
			for (Bean bean : result) {
				if (codePath.contains(bean.getStr("DEPT_CODE"))) {
					supApproGain.set("DEPT_CODE", bean.getStr("DEPT_CODE"));
				}
			}
		}
		return supApproGain;
	}

	/**
	 * 查询服务名来分别查询立项内的机构code
	 *
	 * @param servId
	 * @param approId
	 * @return
	 */
	private List<Bean> findAppro(String servId, String approId) {

		// 构建条件sqlBean
		SqlBean sqlBean = new SqlBean();

		// 构建返回值对象
		List<Bean> result = new ArrayList<>();

		// 署发
		if (SUP_APPRO_OFFICE.equals(servId)) {
			sqlBean.and("OFFICE_ID", approId);
			result = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);

			// 司内
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			sqlBean.and("BUREAU_ID", approId);
			result = ServDao.finds("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
			// 要点
		} else if (SUP_APPRO_POINT.equals(servId)) {
			SqlBean sqlBean2 = new SqlBean();
			sqlBean2.selects("DEPT_CODE");
			sqlBean2.and("ID", approId);
			result = ServDao.finds(SUP_APPRO_POINT, sqlBean2);
		}
		return result;
	}

	/**
	 * 回显页面信息
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean getLinkMan(ParamBean paramBean) {
		// 获取条件值
		String approId = paramBean.getStr("APPRO_ID");
		String servId = paramBean.getStr("servId");

		// 执行得到结果
		Bean bean = ServDao.find(servId, approId);
		String phone = "";
		if(SUP_APPRO_OFFICE.equals(servId)){
			phone = bean.getStr("OFFICE_OVERSEER_TEL");
		}else {
			// 根据得到用户code去查询用户信息
			SqlBean userSqlBean = new SqlBean();
			userSqlBean.selects("ORG_USER_TEL");
			userSqlBean.and("ORG_USER_CODE", bean.getStr("S_USER"));
			// 执行条件得到结果
			Bean userResult = ServDao.find("OA_SUP_SERV_OFFICE_ORG_DEPT", userSqlBean);


			// 判断查询结果是否为空
			if (userResult != null) {
				phone = userResult.getStr("ORG_USER_TEL");
			}
		}
		String userName = DictMgr.getName("SY_ORG_USER", bean.getStr("S_USER"));

		// 构建返回值类型
		OutBean outBean = new OutBean();
		outBean.set("userName", userName);
		outBean.set("phone", phone);

		// 获取当前用户bean
		UserBean userBean = Context.getUserBean();

		// 获取主键参数
		String ID = paramBean.getStr("ID");

		Bean gainBean = ServDao.find(SUP_APPRO_GAIN, ID);

		// 构建deptCode
		String deptCode = null;

		// 判断当前是否是新建状况 如果是显示用的信息 不是的话显示表里面的数据
		if (gainBean == null) {
			if (SUP_APPRO_OFFICE.equals(servId)) {
				deptCode = userBean.getTDeptCode();
			} else if (SUP_APPRO_BUREAU.equals(servId)) {
				deptCode = userBean.getDeptCode();
			} else if (SUP_APPRO_POINT.equals(servId)) {
				deptCode = userBean.getDeptCode();
			}
		} else {
			deptCode = gainBean.getStr("DEPT_CODE");
			// 回显主办单位同志审核情况
			outBean.set("hostGainCase", DictMgr.getName("SY_ORG_USER", gainBean.getStr("HOST_GAIN_CASE")));
			if(!"".equals(gainBean.getStr("GAIN_LINK"))){
				List<Bean> gw = getGw(gainBean.getStr("GAIN_LINK"));
				outBean.set("geList", gw);
			} 
		}
		// 回显部门名称
		String deptName = DictMgr.getName("SY_ORG_DEPT_ALL", deptCode);
		outBean.set("deptName", deptName);

		String supervItem = bean.getStr("SUPERV_ITEM");

		if (SUP_APPRO_POINT.equals(servId)) {
			supervItem = bean.getStr("TITLE");
		}
		// 回显督查事项
		outBean.set("supervItem", supervItem);
		


		return outBean;

	}
	
	/**
	 * 转译公文
	 * @param gainLinks
	 * @return
	 */
	private List<Bean> getGw(String gainLinks){
		
		List<String> linkList = Arrays.asList(gainLinks.toString().split(","));
		
		List<Bean> beans = new ArrayList<>();
		
		
		linkList.forEach(link -> {
			//构成查询sqlBean
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("GW_TITLE");
			sqlBean.and("GW_ID", link);
			Bean find = ServDao.find("OA_GW_GONGWEN_ICBC_GWKDC", sqlBean);
			//构建Bean
			Bean result = new OutBean();
			result.set("title", find.getStr("GW_TITLE"));
			result.set("id", link);
			beans.add(result);
		});;
	
		return beans;
	}

	/**
	 * 判断当前是否为新的办理情况
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean isNULL(ParamBean paramBean) {

		OutBean outBean = new OutBean();
		outBean.set("code", 200);
		String id = paramBean.getStr("ID");
		Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, id);
		if (supApproGain == null) {
			outBean.set("code", 404);
		} else {
			outBean.set("data", supApproGain);
		}
		return outBean;
	}

	/**
	 * 根据条件判断查看全部的办理情况还是部门的
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean getList(ParamBean paramBean) {

		// 获取查询类
		String servId = paramBean.getStr("servId");

		// 判断是否为全部查询
		String isALL = paramBean.getStr("isALL");

		// 构建集合接受返回值
		List<OutBean> allGain = new ArrayList<>();

		// 获取立项单主键
		String approId = paramBean.getStr("APPRO_ID");
		// 根据条件来判断是查询全部和单个部门下面的
		if (isALL.equals("1")) {

			// 获取当前用户bean
			UserBean userBean = Context.getUserBean();
			// 获取用户部门
			String deptCode = userBean.getDeptCode();

			// 署发立项时当前用户的上级节点
			if (servId.equals(SUP_APPRO_OFFICE)) {
				deptCode = userBean.getTDeptCode();
			}
			// 根据部门查询
			allGain = findConditionGain(approId, deptCode);
		} else if (isALL.equals("2")) {
			// 查询全部
			allGain = getAllGain(approId);
		}

		return new OutBean().set("result", allGain);
	}

	/**
	 * 牵头主办和督察处 可以查看全部的
	 *
	 * @param approId
	 * @return
	 */
	private List<OutBean> getAllGain(String approId) {

		List<String> data = getDateDesc(approId);

		// 构建sql语句
		StringBuilder stringBuilder = new StringBuilder("select * from SUP_APPRO_GAIN ")
				.append("where APPRO_ID = '" + approId).append("' and GAIN_STATE in(2,3)");
		// 查询得到结果
		List<Bean> sqlResult = Transaction.getExecutor().query(stringBuilder.toString());

		// 构建返回值bean
		List<OutBean> result = new ArrayList<>();

		// 遍历日期
		for (String datum : data) {
			// 构建存入月份和表格值的outBean
			OutBean outBean = new OutBean();
			// 设置k为月份
			outBean.set("key", datum);
			List<Bean> list = new ArrayList<>();
			// 根据日期来取出对应的值
			for (Bean bean : sqlResult) {
				if (bean.getStr("GAIN_MONTH").equals(datum)) {
					bean.set("deptName", DictMgr.getName("SY_ORG_DEPT_ALL", bean.getStr("DEPT_CODE")));
					bean.set("userName", DictMgr.getName("SY_ORG_USER", bean.getStr("S_USER")));
					bean.set("gainName", DictMgr.getName("SUP_APPRO_GAIN_STATE", bean.getStr("GAIN_STATE")));

					list.add(bean);
				}
			}
			// 设置value 为表格值
			outBean.set("value", list);
			// 返回值集合添加
			result.add(outBean);
		}
		return result;
	}

	/**
	 * 根据部门查询
	 *
	 * @param approId
	 * @param deptCode
	 * @return
	 */
	private List<OutBean> findConditionGain(String approId, String deptCode) {
		List<String> data = getDateDesc(approId, deptCode);

		// 构建sql语句
		StringBuilder stringBuilder = new StringBuilder("select * from SUP_APPRO_GAIN ")
				.append("where APPRO_ID = '" + approId).append("' and DEPT_CODE = '" + deptCode)
				.append("' and GAIN_STATE in(2,3)");
		// 查询得到结果
		List<Bean> sqlResult = Transaction.getExecutor().query(stringBuilder.toString());

		// 构建返回值bean
		List<OutBean> result = new ArrayList<>();

		// 遍历日期
		for (String datum : data) {
			// 构建存入月份和表格值的outBean
			OutBean outBean = new OutBean();
			// 设置k为月份
			outBean.set("key", datum);
			List<Bean> list = new ArrayList<>();
			// 根据日期来取出对应的值
			for (Bean bean : sqlResult) {
				if (bean.getStr("GAIN_MONTH").equals(datum)) {
					bean.set("deptName", DictMgr.getName("SY_ORG_DEPT_ALL", bean.getStr("DEPT_CODE")));
					bean.set("userName", DictMgr.getName("SY_ORG_USER", bean.getStr("S_USER")));
					bean.set("gainName", DictMgr.getName("SUP_APPRO_GAIN_STATE", bean.getStr("GAIN_STATE")));

					list.add(bean);
				}
			}
			// 设置value 为表格值
			outBean.set("value", list);
			// 返回值集合添加
			result.add(outBean);
		}
		return result;

	}

	/**
	 * 根据部门查询
	 *
	 * @param approId
	 * @param deptCode
	 * @return
	 */
	private List<String> getDateDesc(String approId, String deptCode) {
		// 构建sql语句
		StringBuilder stringBuilder = new StringBuilder("select distinct GAIN_MONTH from SUP_APPRO_GAIN ")
				.append("where APPRO_ID =  '" + approId).append("' and DEPT_CODE = '" + deptCode)
				.append("' and GAIN_STATE in(2,3) order by GAIN_MONTH desc");
		// 查询得到结果
		List<Bean> query = Transaction.getExecutor().query(stringBuilder.toString());

		// 构建返回值集合
		List<String> reslut = new ArrayList<>();
		for (Bean bean : query) {
			reslut.add(bean.getStr("GAIN_MONTH"));
		}
		return reslut;
	}

	/**
	 * 根据条件查询日期的倒序
	 *
	 * @param approId
	 * @return
	 */
	private List<String> getDateDesc(String approId) {

		// 构建sql语句
		StringBuilder stringBuilder = new StringBuilder("select distinct GAIN_MONTH from SUP_APPRO_GAIN ")
				.append("where APPRO_ID = '" + approId).append("' and GAIN_STATE in(2,3) order by GAIN_MONTH desc");

		// 查询得到结果
		List<Bean> query = Transaction.getExecutor().query(stringBuilder.toString());

		// 构建返回值集合
		List<String> reslut = new ArrayList<>();
		for (Bean bean : query) {
			reslut.add(bean.getStr("GAIN_MONTH"));
		}
		return reslut;
	}

	/**
	 * 判断当前是否为牵头主办单位
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean isHead(ParamBean paramBean) {
		String servId = paramBean.getStr("servId");
		String approId = paramBean.getStr("APPRO_ID");

		String result = "1";

		UserBean userBean = Context.getUserBean();

		if (SUP_APPRO_OFFICE.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
			if (host != null) {
				result = "2";
			}

		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("BUREAU_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getDeptCode());
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
			if (host != null) {
				result = "2";
			}

		} else if (SUP_APPRO_POINT.equals(servId)) {
			result = "2";
		}

		return new OutBean().set("result", result);
	}

	/**
	 * 获取司内督查员填写办理情况的key
	 */
	public OutBean getInspectorUpdateGainKey(ParamBean paramBean) {

		String servId = paramBean.getStr("servId");

		// 获取立项主单
		String approId = paramBean.getStr("APPRO_ID");

		// 根据userBean获取当前用户的上级
		UserBean userBean = Context.getUserBean();

		String result = "";

		if (SUP_APPRO_OFFICE.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("ID");
			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
			sqlBean.and("GAIN_STATE", "1");
			Bean sup_appro_gain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
			result = sup_appro_gain.getStr("ID");
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("ID");
			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getDeptCode());
			sqlBean.and("GAIN_STATE", "1");
			Bean sup_appro_gain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
			result = sup_appro_gain.getStr("ID");

		} else if (SUP_APPRO_POINT.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("ID");
			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("GAIN_STATE", "1");
			Bean sup_appro_gain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
			result = sup_appro_gain.getStr("ID");
		}
		return new OutBean().set("KEY", result);
	}

	/**
	 * 获取根据类型获取主单的完成时间、逾期天数、办结时间信息并且回显
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean getMSK(ParamBean paramBean) {

		// 获取查询类型
		String servId = paramBean.getStr("servId");

		// 获取立项单主键
		String approId = paramBean.getStr("APPRO_ID");

		// 创建sql条件
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("FINISH_TIME,DEALT_TIME,OVERDUE_DAY");

		// 构建返回值
		Bean bean = new OutBean();

		// 根据条件判断
		if (SUP_APPRO_OFFICE.equals(servId)) {
			sqlBean.and("ID", approId);
			bean = ServDao.find(SUP_APPRO_OFFICE, approId);
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			sqlBean.and("ID", approId);
			bean = ServDao.find(SUP_APPRO_BUREAU, approId);
		} else if (SUP_APPRO_POINT.equals(servId)) {
			sqlBean.and("ID", approId);
			bean = ServDao.find(SUP_APPRO_POINT, approId);
		}

		return new OutBean().set("result", bean);
	}

	/**
	 * 更新办结时间
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean updateDealtTime(ParamBean paramBean) {
		// 获取查询类型
		String servId = paramBean.getStr("servId");

		// 获取立项单主键
		String approId = paramBean.getStr("approId");

		// 获取更新的时间
		String dealtTime = paramBean.getStr("dealtTime");

		Bean bean = ServDao.find(servId, approId);

		bean.set("DEALT_TIME", dealtTime);

		// 创建催办对象
		SupApperUrge supApperUrge = new SupApperUrge();
		// 获取新的预期天数
		String workDay = supApperUrge.getWorkDay(dealtTime, bean.getStr("LIMIT_DATE"));

		// 把字符串转成int
		int day = Integer.parseInt(workDay);
		// 判断是否预期 当结果<=0时未逾期
		if (day <= 0) {
			day = 0;
		}
		bean.set("OVERDUE_DAY", day);
		ServDao.update(servId, bean);

		return new OutBean().set("OVERDUE_DAY", day);

	}

	/**
	 * 根据方法名来更新
	 * 
	 * @param paramBean
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public OutBean updateGainMethods(ParamBean paramBean) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// 取出方法要执行的方法
		String Methods = paramBean.getStr("Methods");

		// 取出要执行的方法
		String[] split = Methods.split(",");

		// 获取当前类的字节码文件
		Class<? extends SupApproGain> gainClass = this.getClass();

		// 循环遍历方法名
		for (String string : split) {

			// 获取方法对象
			Method method = gainClass.getMethod(string, ParamBean.class);
			// 执行方法
			method.invoke(this, paramBean);
		}
		return new OutBean().setOk();
	}

	/**
	 * 流程中更新办理状态
	 * 
	 * @return
	 */
	public OutBean updateWfState(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");// 立项单主键
		String curState = paramBean.getStr("curState");// 当前办理状态
		String upState = paramBean.getStr("upState");// 更新后的办理状态
		String deptCode = paramBean.getStr("deptCode");// 办理机构
		String niId = paramBean.getStr("niId");// 流程实例ID
		// 如果参数中有流程实例ID，则判断上个节点处理人机构
		if (StringUtils.isNotEmpty(niId)) {
			deptCode = getWFDeptCode(niId);
		}
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("GAIN_STATE", curState);
		if (!deptCode.equals("")) {
			sql.and("DEPT_CODE", deptCode);
		}
		Bean gain = ServDao.find(SUP_APPRO_GAIN, sql);
		if (gain != null) {
			gain.set("GAIN_STATE", upState);
			ServDao.update(SUP_APPRO_GAIN, gain);
		}
		return new OutBean().setOk();
	}

	/**
	 * 获取上一节点处理人的DEPT_CODE
	 * 
	 * @param niId
	 *            当前流程实例编码
	 * @return
	 */
	private String getWFDeptCode(String niId) {
		// 获取当前流程实例
		Bean nowNodeInst = ServDao.find("SY_WFE_NODE_INST", niId);
		// 获取上一流程实例id
		String preId = nowNodeInst.getStr("PRE_NI_ID");
		// 获取上一流程实例
		Bean preNodeInst = ServDao.find("SY_WFE_NODE_INST", preId);
		// 获取上一流程实例操作用户
		String userCode = preNodeInst.getStr("DONE_USER_ID");
		// 获取版本
		String procType = preNodeInst.getStr("PROC_CODE");
		// 上个处理人
		UserBean userBean = UserMgr.getUser(userCode);
		// 构建返回值
		String deptCode = "";
		// 判断署发和司内
		if (procType.contains(SupConstant.OA_SUP_APPRO_BUREAU) || procType.contains(SupConstant.OA_SUP_APPRO_POINT)) {
			deptCode = userBean.getDeptCode();
		} else if (procType.contains(SupConstant.OA_SUP_APPRO_OFFICE)) {
			deptCode = userBean.getTDeptCode();
		}
		return deptCode;
	}

	/**
	 * 校验 当前维护节点是否填写值 参数 NID节点id servId主单服务id APPRO_ID主单当前主键id
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean isMaintain(ParamBean paramBean) {
		// 获取服务id
		String servId = paramBean.getStr("servId");

		// 获取节点id
		String NID = paramBean.getStr("NID");

		// 获取主单id
		String approId = paramBean.getStr("APPRO_ID");

		// 获取当前用户信息
		UserBean userBean = Context.getUserBean();

		// 构建返回值信息
		OutBean outBean = new OutBean();
		outBean.set("isMaintain", true);

		// 根据条件判断
		if (SUP_APPRO_OFFICE.equals(servId)) {
			// 获取当前填写办理情况
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
			sqlBean.and("GAIN_STATE", "1");
			Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, sqlBean);

			// 判断当前是否保存，未保存 就找不到状态为1 的办理情况 当办理情况不为空的时候判断 该节点能维护的信息是否有值
			if (supApproGain == null || supApproGain.isEmpty()) {
				outBean.set("isMaintain", false);
			} else {
				// 督查员
				if (NID.equals("N45")) {
					// 督察员 填写处事办理情况 初始承办月份 办理情况 承办月份

					String text2 = supApproGain.getStr("GAIN_TEXT");
					String month2 = supApproGain.getStr("GAIN_MONTH");

					if (StringUtils.isEmpty(text2) || StringUtils.isEmpty(month2)) {
						outBean.set("isMaintain", false);
					}

					// 主办人
				} else if (NID.equals("N46")) {
					// 主办人填写处室办理情况 初始承办月份
					String month = supApproGain.getStr("GAIN_GRADE_MONTH");
					String text = supApproGain.getStr("GAIN_GRADE_TEXT");
					if (StringUtils.isEmpty(month) || StringUtils.isEmpty(text)) {
						outBean.set("isMaintain", false);
					}
				}

			}

		} else if (SUP_APPRO_BUREAU.equals(servId)) {

			SqlBean sqlBean = new SqlBean();

			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getDeptCode());
			sqlBean.and("GAIN_STATE", "1");
			Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
			// 判断当前是否保存，未保存 就找不到状态为1 的办理情况 当办理情况不为空的时候判断 该节点能维护的信息是否有值
			if (supApproGain == null || supApproGain.isEmpty()) {
				outBean.set("isMaintain", false);
			} else {
				if (NID.equals("N32") || NID.equals("N214")) {
					// 主办人填写 办理情况 承办月份
					String text = supApproGain.getStr("GAIN_TEXT");
					String month = supApproGain.getStr("GAIN_MONTH");
					if (StringUtils.isEmpty(month) || StringUtils.isEmpty(text)) {
						outBean.set("isMaintain", false);
					}

				}
			}
		} else if (SUP_APPRO_POINT.equals(servId)) {

			SqlBean sqlBean = new SqlBean();
			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("GAIN_STATE", "1");
			Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, sqlBean);

			// 判断当前是否保存，未保存 就找不到状态为1 的办理情况 当办理情况不为空的时候判断 该节点能维护的信息是否有值
			if (supApproGain == null || supApproGain.isEmpty()) {
				outBean.set("isMaintain", false);
			} else {
				// 督查员
				if (NID.equals("N3")) {
					// 督察员 填写处事办理情况 初始承办月份 办理情况 承办月份

					String text2 = supApproGain.getStr("GAIN_TEXT");
					String month2 = supApproGain.getStr("GAIN_MONTH");

					if (StringUtils.isEmpty(text2) || StringUtils.isEmpty(month2)) {
						outBean.set("isMaintain", false);
					}
					// 主办人
				} else if (NID.equals("N32")) {
					// 主办人填写处室办理情况 初始承办月份
					String month = supApproGain.getStr("GAIN_GRADE_MONTH");
					String text = supApproGain.getStr("GAIN_GRADE_TEXT");
					if (StringUtils.isEmpty(month) || StringUtils.isEmpty(text)) {
						outBean.set("isMaintain", false);
					}
				}
			}
		}

		return outBean;
	}

	/**
	 * 判断是否可提交 如果是牵头主办单位则需要判断其他主办单位是否填写完成 如果是主办单位则不用判断
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean isPass(ParamBean paramBean) {
		// 判断当前用户是否为牵头主办
		OutBean outBean = isHead(paramBean);
		String isHead = outBean.getStr("result");

		OutBean result = new OutBean();

		result.set("isPass", true);

		if ("2".equals(isHead)) {
			// 判断其他主办是否在办理中 如果有办理中的就返回false
			OutBean isNotHead = isNotHead(paramBean);

			String isNotHeadResult = isNotHead.getStr("result");
			// 等于2的话确定其他主办中有代办
			if ("2".equals(isNotHeadResult)) {
				result.set("isPass", false);

				// 查询牵头主办提交办理情况限制时间
				SqlBean sqlBean = new SqlBean();
				sqlBean.selects("ST_VALUE");
				sqlBean.and("ST_KEY", HOST_DEPT_LIMIT_TIME);
				Bean bean = ServDao.find("OA_SUP_SERV_SETTING_VALUE", sqlBean);

				// 判断公式 当前月份总天数 - 设定天数 + 1 = 当前天数

				// 获取当前月份多天
				int monthDay = getCurrentMonthDay();

				// 判断是否达到时间要求
				// 转译date进行计算
				int confirmDay = monthDay - Integer.parseInt(bean.getStr("ST_VALUE"));

				// 当前月份的第几天
				SimpleDateFormat sdf = new SimpleDateFormat("dd");
				String day = sdf.format(new Date());
				int parseInt = Integer.parseInt(day);
				// 得到结果是相等后运行提交
				if (confirmDay <= parseInt) {
					result.set("isPass", true);
				}

			}
		}
		return result;
	}

	/**
	 * 判断其他主办是否在办理 办理情况
	 * 
	 * @param paramBean
	 * @return
	 */
	private OutBean isNotHead(ParamBean paramBean) {
		// 获取服务编码
		String servId = paramBean.getStr("servId");

		// 获取立项主键
		String approId = paramBean.getStr("APPRO_ID");

		// 构建返回值
		String result = "1";
		// 构建查看节点
		String stringNid = "";
		// 根据立项单来设置查询的节点
		if (SUP_APPRO_OFFICE.equals(servId)) {
			stringNid = "'N45','N46','N47'";

		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			stringNid = "'N214','N32','N26'";
		}
		Bean find = ServDao.find(servId, approId);

		StringBuffer buffer = new StringBuffer("SELECT TODO_ID FROM SY_COMM_TODO WHERE OWNER_CODE != '")
				.append(Context.getUserBean().getCode())
				.append("' AND TODO_OBJECT_ID2 IN( SELECT NI_ID FROM SY_WFE_NODE_INST WHERE PI_ID = '")
				.append(find.getStr("S_WF_INST")).append("' AND PROC_CODE LIKE'").append(servId)
				.append("%' AND NODE_CODE IN(").append(stringNid).append(") and NODE_IF_RUNNING = '1' )");
		int count = Transaction.getExecutor().count(buffer.toString());

		if (count > 0) {
			result = "2";
		}

		return new OutBean().set("result", result);
	}

	/**
	 * 获取当前月份天数
	 * 
	 * @return
	 */
	public int getCurrentMonthDay() {
		LocalDate now = LocalDate.now();
		int lengthOfMonth = now.lengthOfMonth();

		return lengthOfMonth;
	}

	/**
	 * 更新主办单位更新时间
	 * 
	 * @param paramBean
	 */
	public OutBean updateHostGainUpdate(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");// 立项单主键
		String curState = paramBean.getStr("curState");// 当前办理状态
		String deptCode = paramBean.getStr("deptCode");// 办理机构
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("GAIN_STATE", curState);
		if (!deptCode.equals("")) {
			sql.and("DEPT_CODE", deptCode);
		}
		Bean gain = ServDao.find(SUP_APPRO_GAIN, sql);
		if (gain != null) {
			gain.set("HOST_GAIN_UPDATE", DateUtils.getStringFromDate(new Date(), DateUtils.FORMAT_DATE));
			ServDao.update(SUP_APPRO_GAIN, gain);
		}
		return new OutBean().setOk();
	}

	/**
	 * 添加主办单位主要负责同志审核情况 信息
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean updateHostGainCase(ParamBean paramBean) {
		// 获取条件信息

		UserBean userBean = Context.getUserBean();

		String approId = paramBean.getStr("approId");// 立项单主键
		String curState = paramBean.getStr("curState");// 当前办理状态
		String deptCode = paramBean.getStr("deptCode");// 办理机构
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("GAIN_STATE", curState);
		if (!deptCode.equals("")) {
			sql.and("DEPT_CODE", deptCode);
		}
		Bean gain = ServDao.find(SUP_APPRO_GAIN, sql);
		if (gain != null) {
			gain.set("HOST_GAIN_CASE", userBean.getCode());
			ServDao.update(SUP_APPRO_GAIN, gain);
		}
		return new OutBean();
	}

	// 判断是否显示去查看历史督查
	public OutBean isHeanAndValueble(ParamBean paramBean) {
		String approId = paramBean.getStr("APPRO_ID");

		SqlBean sqlBean = new SqlBean();
		sqlBean.set("APPRO_ID", approId);
		sqlBean.set("GAIN_STATE", "3");
		// 根据条件查询是否有已经办理完成的记录
		int count = ServDao.count(SupConstant.OA_SUP_APPRO_GAIN, sqlBean);

		// 构建返回值bean
		OutBean outBean = new OutBean();

		outBean.set("result", "1");

		if (count > 0) {
			// 获取判断是否是牵头
			OutBean result = isHead(paramBean);
			String str = result.getStr("result");
			// 判断是否是牵头
			if (str.equals("2")) {
				outBean.set("result", "2");
			}
		}

		return outBean;
	}

	/**
	 * 获取当前督察处审核的部门名称
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getDispostDeptName(ParamBean paramBean) {
		// 获取当前流程实例
		String niId = paramBean.getStr("niId");
		// 上个流程实例处理的部门
		String deptCode = getWFDeptCode(niId);

		return new OutBean().set("deptName", DictMgr.getName("SY_ORG_DEPT_ALL", deptCode));
	}

	/**
	 * 保存前对数据进行处理
	 */
	@Override
	public void beforeSave(ParamBean paramBean) {
		StringBuilder gainLink = new StringBuilder(paramBean.getStr("GAIN_LINK")) ;
		if(gainLink.length() > 0){
			gainLink.deleteCharAt(0);
			List<String> linkList = Arrays.asList(gainLink.toString().split(","));
			gainLink.delete(0, gainLink.length());
			linkList.stream().distinct().forEach(link -> {
				gainLink.append(",")
					.append(link);
				
			});;
			gainLink.deleteCharAt(0);
			paramBean.set("GAIN_LINK", gainLink);
		}
	}
	
	/**
	 * 办结时将所有待审核的办理情况制为通过
	 *
	 * @return
	 */
	public void allGainPass(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");// 立项单主键
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("GAIN_STATE", "2");
		List<Bean> gainList = ServDao.finds(SUP_APPRO_GAIN, sql);
		for(Bean gainBean:gainList){
			gainBean.set("GAIN_STATE","3");
			ServDao.update(SUP_APPRO_GAIN,gainBean);
		}
	}
}
