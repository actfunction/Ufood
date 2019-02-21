package com.rh.sup.serv;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.util.DateUtils;
import com.rh.sup.util.ImpUtils;
import com.rh.sup.util.OfficeWord;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;

import javax.servlet.http.HttpServletRequest;

/**
 * 署内立项扩展类
 * @author admin
 */
public class SupApproOfficeServ extends CommonServ{
	// 固定模板路径
	public static final String OLDFILE_PATH = "/sup/imp_template/Template_督查立项申请单.xls";
	// 新模板路径
	public static final String TEMPFILE_PATH = "/sup/imp_template/督查立项申请单.xls";
	// 数据字典表名称
	private final String SUP_SERV_DICT = "SUP_SERV_DICT";
	private final String CUE_TYPE = "001";// 提醒规则类型 DICT_KINDS
	private final String HANDLE_TYPE = "002";// 办理类型 DICT_KINDS
	private final String ITEM_TYPE = "003";// 督查事项类型 DICT_KINDS
	private final String STATIS_ITEM_SOURCE = "004";// 统计用事项来源 DICT_KINDS

	/**
	 * @description: 立项单状态值
	 * @author: kfzx-guoch
	 * @date: 2018/12/14 13:47
	 */
	public static final String APPLY_STATE_1 = "1";
	public static final String APPLY_STATE_2 = "2";
	public static final String APPLY_STATE_3 = "3";
	public static final String APPLY_STATE_4 = "4";
	public static final String APPLY_STATE_5 = "5";
	public static final String APPLY_STATE_6 = "6";
	public static final String APPLY_STATE_7 = "7";
	
	//办理状态 办理中 1
	private final String GAIN_ING_STATE = "1";
	//办理状态 待审核 2
	private final String GAIN_CHECK_STATE ="2";
	//办理状态 已审核 3
	private final String GAIN_DONE_STATE ="3";
	
	/**
	 * @description: 要点类 表名
	 * @author: kfzx-guoch
	 * @date: 2018/12/11 15:32
	 */
	private final String SUP_APPRO_OFFICE = "SUP_APPRO_OFFICE";
	private final String SUP_APPRO_PLAN = "SUP_APPRO_PLAN";
	
	//根据署发立项申请单id获取承办单位列表
	public OutBean getApplyDeptsByOfficeId(ParamBean paramBean){
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_PHONE,DEPT_TYPE");
		sqlBean.and("OFFICE_ID", paramBean.getStr("officeId"));
		List<Bean>deptList = ServDao.finds("OA_SUP_APPRO_OFFICE_APPLY_DEPT", sqlBean);
		return new OutBean().set("deptList", deptList);
	}
	//根据id查询主单信息
	public OutBean getApproOfficeById(ParamBean paramBean){
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("ID");
		sqlBean.and("ID", paramBean.getStr("officeId"));
		Bean bean = ServDao.find("OA_SUP_APPRO_OFFICE", sqlBean);
		OutBean outBean = new OutBean();
		if(bean==null){
			outBean.set("flag", "0");
		}else{
			outBean.set("flag", "1");
		}
		
		return outBean;
	}
	//根据申请单ID查询所有待办
	public OutBean getTodoListByDataId(ParamBean paramBean){
		List<Bean> list = ServDao.finds("SY_COMM_TODO", new SqlBean().set("TODO_OBJECT_ID1", paramBean.getStr("id")));
		return new OutBean().set("todoList", list);
	}
	//终止子流程时删除对应的会签部门
	public OutBean deleteOneHuiQianDept(ParamBean paramBean){
		SqlBean sqlBean = new SqlBean();
		String currServId=paramBean.getStr("servId");
		//署发
		if("OA_SUP_APPRO_OFFICE".equals(currServId)){
			sqlBean.and("OFFICE_ID", paramBean.getStr("dataId"));
			sqlBean.and("C_USER_CODE", paramBean.getStr("userCode"));
			ServDao.delete("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
			//司内
		}else if("OA_SUP_APPRO_BUREAU".equals(currServId)){
			sqlBean.and("BUREAU_ID", paramBean.getStr("dataId"));
			sqlBean.and("USER_CODE", paramBean.getStr("userCode"));
			ServDao.delete("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
		}
		return new OutBean().set("MSG", "OK");
	}
	//恢复中止的流程
	public OutBean recoveryTodoAndDeleteBake(ParamBean paramBean){
		OutBean outBean = new OutBean();
		List<Bean> list = ServDao.finds("OA_SUP_COMM_TODO_BAK", new SqlBean().set("TODO_OBJECT_ID1", paramBean.getStr("id")));
		SqlBean sqlBean = new SqlBean();
		sqlBean.set("APPLY_STATE", "20");
		sqlBean.and("ID", paramBean.getStr("id"));
		try {
			int todos = ServDao.creates("SY_COMM_TODO", list);
			ServDao.delete("OA_SUP_COMM_TODO_BAK", new SqlBean().and("TODO_OBJECT_ID1", paramBean.getStr("id")));
			ServDao.update("OA_SUP_APPRO_OFFICE", sqlBean);
		} catch (Exception e) {
			outBean.set("MSG", "false");
			outBean.set("error", e.getMessage());
			return outBean;
		}
		outBean.set("MSG", "ok");
		return outBean;
	}
	
	//中止流程即删除现有待办
	public OutBean deleteTodoAndBake(ParamBean paramBean){
		OutBean outBean = new OutBean();
		List<Bean> list = ServDao.finds("SY_COMM_TODO", new SqlBean().set("TODO_OBJECT_ID1", paramBean.getStr("id")));
		SqlBean sqlBean = new SqlBean();
		sqlBean.set("APPLY_STATE", "30");
		sqlBean.and("ID", paramBean.getStr("id"));
		try {
			int todos = ServDao.creates("OA_SUP_COMM_TODO_BAK", list);
			ServDao.delete("SY_COMM_TODO", new SqlBean().and("TODO_OBJECT_ID1", paramBean.getStr("id")));
			ServDao.update("OA_SUP_APPRO_OFFICE", sqlBean);
		} catch (Exception e) {
			outBean.set("MSG", "false");
			outBean.set("error", e.getMessage());
			return outBean;
		}
		outBean.set("MSG", "ok");
		return outBean;
	}
	
	//获取用户的mobile
	public OutBean getUserMobile(ParamBean paramBean){
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("ORG_DEPT_CODE,ORG_USER_CODE,ORG_USER_TEL");
		sqlBean.and("ORG_USER_CODE", paramBean.getStr("userCode"));
		sqlBean.and("S_FLAG", Constant.YES);
		Bean userBean = ServDao.find("OA_SUP_SERV_OFFICE_ORG_DEPT", sqlBean);
		return new OutBean().set("userBean", userBean);
	}
	
	//获取当前立项单关联的办理单位列表
	public OutBean getDeptByOfficeId(ParamBean paramBean){
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_PHONE,DEPT_TYPE");
		sqlBean.and("OFFICE_ID", paramBean.getStr("officeId"));
		List<Bean>deptList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
		return new OutBean().set("deptList", deptList);
	}
	//获取领导和督查员列表
	public OutBean getSupLeaderListByDeptAndRole(ParamBean paramBean){
		SqlBean selectedSqlBean = new SqlBean();
		selectedSqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_PHONE,DEPT_TYPE");
		selectedSqlBean.and("OFFICE_ID", paramBean.getStr("officeId"));
		selectedSqlBean.and("DEPT_CODE", paramBean.getStr("deptCode"));
		List<Bean> currUserList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", selectedSqlBean);
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("ORG_DEPT_CODE,ORG_USER_CODE,ORG_USER_TEL");
		sqlBean.and("ORG_DEPT_CODE", paramBean.getStr("deptCode"));
		sqlBean.and("IS_DEF_LEADER", Constant.YES);
		List<Bean> defaultLeader = ServDao.finds("OA_SUP_SERV_OFFICE_ORG_DEPT", sqlBean);
		sqlBean = new SqlBean();
		sqlBean.selects("ORG_DEPT_CODE,ORG_USER_CODE,ORG_USER_TEL");
		sqlBean.and("ORG_DEPT_CODE", paramBean.getStr("deptCode"));
		sqlBean.and("IS_DEF_INSPECTOR", Constant.YES);
		List<Bean> defaultUser = ServDao.finds("OA_SUP_SERV_OFFICE_ORG_DEPT", sqlBean);
		StringBuilder sb = new StringBuilder();
		sb.append("select u.USER_CODE,u.USER_NAME,u.DEPT_CODE,u.USER_MOBILE,r.ROLE_CODE from SY_BASE_USER_V u");
		sb.append(" left join SY_ORG_ROLE_USER r on u.USER_CODE=r.USER_CODE");
		sb.append(" where r.ROLE_CODE in ("+paramBean.getStr("leaderRoleCode")+")");
		sb.append(" and u.TDEPT_CODE='"+paramBean.getStr("deptCode")+"'");
		List<Bean> leaderList = Transaction.getExecutor().query(sb.toString());
		
		sb = new StringBuilder();
		sb.append("select u.USER_CODE,u.USER_NAME,u.DEPT_CODE,u.USER_MOBILE,r.ROLE_CODE from SY_BASE_USER_V u");
		sb.append(" left join SY_ORG_ROLE_USER r on u.USER_CODE=r.USER_CODE");
		sb.append(" where r.ROLE_CODE = '"+paramBean.getStr("userRoleCode")+"'");
		sb.append(" and u.TDEPT_CODE='"+paramBean.getStr("deptCode")+"'");
		List<Bean> userList = Transaction.getExecutor().query(sb.toString());
		OutBean outBean = new OutBean();
		outBean.set("leaderList", leaderList);
		outBean.set("userList", userList);
		outBean.set("defaultLeader", defaultLeader);
		outBean.set("defaultUser", defaultUser);
		outBean.set("currUserList", currUserList);
		return outBean;
	}

	public void beforeSave(ParamBean paramBean){}
	
	/*
	 * 判断当前是否有无跳过成果体现及办理计划
	 */
	public  OutBean findPlanNum(ParamBean paramBean) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPRO_ID", paramBean.getStr("approId"));
		List<Bean> planList = ServDao.finds("OA_SUP_APPRO_PLAN", sqlBean);
		int num = planList.size();
		return new OutBean().set("num", num);
	}

	/*
	 * 获取立项编号
	 */
	public OutBean getItemNum(ParamBean paramBean){

		String actCode = paramBean.getStr("actCode");//操作表示
		String servId = paramBean.getStr("servId");//服务ID
		String nowYear = paramBean.getStr("nowYear");//当前年份
		String pkCode = paramBean.getStr("pkCode");//pkCode
		//如果actCode为cardAdd说明是新增的单子
		if(actCode.equalsIgnoreCase("cardAdd")){
			//对于新增的单子,如果数据库中无单子则编号为1,如果有单子则根据单子加1
			SqlBean sqlBean = new SqlBean();
			sqlBean.appendWhere("and S_ATIME between ? and ?", nowYear+"-01-01 00:00:00", nowYear+"-12-31 23:59:59");
			List<Bean> supDatas = ServDao.finds(servId, sqlBean);

			return new OutBean().set("ITEM_NUM", supDatas.size()+1);
		}else{
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("ID", pkCode);
			Bean supData = ServDao.find(servId, sqlBean);
			if(supData == null){
				return new OutBean().set("ITEM_NUM", 1);
			}else{
				return new OutBean().set("ITEM_NUM", supData.getStr("ITEM_NUM"));
			}
		}
	}
	
	/*
	 * 通过督查员code后可自动链接座机信息(USER_OFFICE_PHONE)
	 */
	public OutBean getUserPhone(ParamBean paramBean){
		StringBuilder sql = new StringBuilder("SELECT USER_CODE,USER_OFFICE_PHONE FROM SUP_APPRO_OFFICE_DEPT d,SY_ORG_USER u");
		//获取paramBean传来的参数
		String approId = paramBean.getStr("approId1");
		//拼接sql语句中where条件 WHERE d.S_USER_CODE=u.USER_CODE AND d.APPRO_ID='2Ea1KZmFl7kbm0fWCck9Qg';
		sql.append(" WHERE d.C_USER_CODE=u.USER_CODE AND d.OFFICE_ID = '"+approId+"'");
		List<Bean> supervierList = Transaction.getExecutor().query(sql.toString());
		return new OutBean().set("supervierList", supervierList);
	}
	
	/*
	 * 督查立项单下载
	 */
	public OutBean OfficeWord(ParamBean paramBean){
		String ITEM_NUM = "";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT t1.ID,t1.ITEM_NUM,t1.SUPERV_ITEM,t1.APPR_DATE,t1.LIMIT_DATE,t1.ITEM_SOURCE,t1.ISSUE_CODE,\n" +
				"\t\t\t t1.ISSUE_DEPT,t1.CENTER_DENOTE,t1.LEAD_DENOTE,t1.DEPT_DENOTE,'' AS DIRECT,\n" +
				"\t\t\t '' AS OTHER_DIRECT,'' AS JOINTLY,t1.REMARK,t1.OFFICE_OVERSEER,t1.OFFICE_OVERSEER_TEL\n" +
				"FROM SUP_APPRO_OFFICE t1 WHERE ID = '");
		stringBuilder.append(paramBean.getStr("ID"));
		stringBuilder.append("'");
		List<Bean> list = Transaction.getExecutor().query(stringBuilder.toString());
		StringBuilder stringBuilder1 = new StringBuilder();
		stringBuilder1.append("SELECT t1.DEPT_TYPE,t2.DEPT_NAME,t3.USER_NAME AS ONE_NAME,t4.USER_NAME AS TWO_NAME,t1.DEPT_PHONE " +
				"FROM SUP_APPRO_OFFICE_DEPT t1,SY_ORG_DEPT t2,SY_ORG_USER t3,SY_ORG_USER t4\n" +
				"WHERE t1.OFFICE_ID = '");
		stringBuilder1.append(list.get(0).getStr("ID"));
		stringBuilder1.append("' AND t1.DEPT_CODE = t2.DEPT_CODE AND t1.D_USER_CODE = t3.USER_CODE AND t1.C_USER_CODE = t4.USER_CODE");
		List<Bean> list1 = Transaction.getExecutor().query(stringBuilder1.toString());
		String DIRECT = "";
		String DIRECT_NAME_ONE="";
		String DIRECT_NAME_TWO="";
		String DIRECT_PHONE="";
		String OTHER_DIRECT = "";
		String OTHER_DIRECT_NAME_ONE="";
		String OTHER_DIRECT_NAME_TWO="";
		String OTHER_DIRECT_PHONE="";
		String JOINTLY = "";
		String JOINTLY_NAME_ONE="";
		String JOINTLY_NAME_TWO="";
		String JOINTLY_PHONE="";
		for (int i = 0 ; i < list1.size() ; i++ ){
			if (list1.get(i).getStr("DEPT_TYPE").equals("1")){
				if (DIRECT.length()!=0){
					DIRECT = "\n" + list1.get(i).getStr("DEPT_NAME");
					DIRECT_NAME_ONE += "\n" + list1.get(i).getStr("ONE_NAME");
					DIRECT_NAME_TWO += "\n" + list1.get(i).getStr("TWO_NAME");
					DIRECT_PHONE += "\n" + list1.get(i).getStr("DEPT_PHONE");
				}else{
					DIRECT = list1.get(i).getStr("DEPT_NAME");
					DIRECT_NAME_ONE += list1.get(i).getStr("ONE_NAME");
					DIRECT_NAME_TWO += list1.get(i).getStr("TWO_NAME");
					DIRECT_PHONE += list1.get(i).getStr("DEPT_PHONE");
				}
			}else if (list1.get(i).getStr("DEPT_TYPE").equals("2")){
				if (OTHER_DIRECT.length()!=0){
					OTHER_DIRECT += "\n" + list1.get(i).getStr("DEPT_NAME");
					OTHER_DIRECT_NAME_ONE += "\n" + list1.get(i).getStr("ONE_NAME");
					OTHER_DIRECT_NAME_TWO += "\n" + list1.get(i).getStr("TWO_NAME");
					OTHER_DIRECT_PHONE += "\n" + list1.get(i).getStr("DEPT_PHONE");
				}else{
					OTHER_DIRECT += list1.get(i).getStr("DEPT_NAME");
					OTHER_DIRECT_NAME_ONE += list1.get(i).getStr("ONE_NAME");
					OTHER_DIRECT_NAME_TWO += list1.get(i).getStr("TWO_NAME");
					OTHER_DIRECT_PHONE += list1.get(i).getStr("DEPT_PHONE");
				}
			}else{
				if (JOINTLY.length()!=0){
					JOINTLY += "\n" + list1.get(i).getStr("DEPT_NAME");
					JOINTLY_NAME_ONE += "\n" + list1.get(i).getStr("ONE_NAME");
					JOINTLY_NAME_TWO += "\n" + list1.get(i).getStr("TWO_NAME");
					JOINTLY_PHONE += "\n" + list1.get(i).getStr("DEPT_PHONE");
				}else{
					JOINTLY += list1.get(i).getStr("DEPT_NAME");
					JOINTLY_NAME_ONE += list1.get(i).getStr("ONE_NAME");
					JOINTLY_NAME_TWO += list1.get(i).getStr("TWO_NAME");
					JOINTLY_PHONE += list1.get(i).getStr("DEPT_PHONE");
				}
			}
		}
		list.get(0).set("DIRECT",DIRECT);
		list.get(0).set("DIRECT_NAME_ONE",DIRECT_NAME_ONE);
		list.get(0).set("DIRECT_NAME_TWO",DIRECT_NAME_TWO);
		list.get(0).set("DIRECT_PHONE",DIRECT_PHONE);
		list.get(0).set("OTHER_DIRECT",OTHER_DIRECT);
		list.get(0).set("OTHER_DIRECT_NAME_ONE",OTHER_DIRECT_NAME_ONE);
		list.get(0).set("OTHER_DIRECT_NAME_TWO",OTHER_DIRECT_NAME_TWO);
		list.get(0).set("OTHER_DIRECT_PHONE",OTHER_DIRECT_PHONE);
		list.get(0).set("JOINTLY",JOINTLY);
		list.get(0).set("JOINTLY_NAME_ONE",JOINTLY_NAME_ONE);
		list.get(0).set("JOINTLY_NAME_TWO",JOINTLY_NAME_TWO);
		list.get(0).set("JOINTLY_PHONE",JOINTLY_PHONE);
		ITEM_NUM = list.get(0).getStr("ITEM_NUM");
		OfficeWord.createWord(list,ITEM_NUM);
		return new OutBean().setOk();
	} 
	
	
	/*
	 * 改变APPLY_STATE的状态值
	 */
	public OutBean changeState(ParamBean paramBean) {
		String id = paramBean.getStr("ID");
		StringBuilder sql = new StringBuilder("UPDATE SUP_APPRO_OFFICE SET APPLY_STATE=TO_CHAR(TO_NUMBER(APPLY_STATE)+1) ");
		sql.append("WHERE ID = '"+id+"'");
		Transaction.getExecutor().query(sql.toString());
		return new OutBean().setOk();
	}


	/**
	 *  复制模板文件 防止模板被损坏
	 * @param request
	 */
	public static void copyFileTemplate(HttpServletRequest request){
		try {
			File oldFile = new File(request.getRealPath(OLDFILE_PATH));
			File tempFile = new File(request.getRealPath(TEMPFILE_PATH));
			if(tempFile.exists()){
				tempFile.delete();
			}
			FileUtils.copyFile(oldFile, tempFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *    根绝字典的ID 获取字典值 并写入到 sheet中
	 * @param type  字典 ID
	 * @param odept_code   当前人所在机构
	 * @param sheet
	 * @throws WriteException
	 */
	public void findTypeToSheet(String type,String odept_code,WritableSheet sheet,int col) throws WriteException {
		// 根据 字典id 查询字典值
		String typeSql = "SELECT DICT_NAME FROM " + SUP_SERV_DICT + " WHERE DICT_KINDS = '" + type + "' AND S_ODEPT = '" + odept_code + "'";
		List<Bean> enumList = Transaction.getExecutor().query(typeSql);
		if(enumList.size() > 0) {
			for(int i = 0; i < enumList.size(); i++) {
				sheet.addCell(new Label(col, i, enumList.get(i).getStr("DICT_NAME")));
			}
		}
	}

	/**
	 *  下载模板时 更新模板的字典值
	 * @return
	 */
	public OutBean updateExcelTemplate() {
		HttpServletRequest request = Context.getRequest();
		copyFileTemplate(request);
		String msgError = "";
		WritableWorkbook book = null;
		try {
			//获得文件
			Workbook wb = Workbook.getWorkbook(new File(request.getRealPath(TEMPFILE_PATH)));
			//打开一个文件的副本，并且指定数据写回到原文件
			book = Workbook.createWorkbook(new File (request.getRealPath(TEMPFILE_PATH)), wb);

			// 获取第二个sheet的数据
			WritableSheet sheet = book.getSheet(1);

			// 准备数据
			UserBean userBean = Context.getUserBean();
			String odept_code = userBean.getStr("ODEPT_CODE");

			findTypeToSheet(CUE_TYPE,odept_code,sheet,0);// 获取所有提醒规则类型
			findTypeToSheet(HANDLE_TYPE,odept_code,sheet,1);// 获取所有办理类型
			findTypeToSheet(ITEM_TYPE,odept_code,sheet,2);// 获取所有督查事项类型
			findTypeToSheet(STATIS_ITEM_SOURCE,odept_code,sheet,3);// 获取所有统计用事项来源

			if(book!=null) {
				book.write();
				book.close();
			}
		} catch (IOException e) {
			msgError = "读取文件失败，请稍后重试！";
		} catch (RowsExceededException e) {
			msgError = "对应列数加载失败，请稍后重试！";
		} catch (WriteException e) {
			msgError = "更新文件失败，请稍后重试！";
		} catch (BiffException e) {
			msgError = "文件加载失败，请稍后重试！";
		} finally {
			OutBean out = new OutBean();
			out.set(ImpUtils.ERROR_NAME, msgError);
			return out;
		}
	}

	/*
	 * 判断当前是否为主办单位
	 */
	public OutBean IsHost(ParamBean paramBean) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("DEPT_TYPE");
		sqlBean.and("OFFICE_ID", paramBean.getStr("approId"));
		sqlBean.and("DEPT_CODE", paramBean.getStr("deptCode"));
		Bean bean = ServDao.find("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
		OutBean result = new OutBean();
		if(bean.getStr("DEPT_TYPE").equals("1")){
			result.set("IsHost", true);
		}else{
			result.set("IsHost", false);
		}
		return result;
	}

	/**
	 * @description: 更新立项单状态
	 * @author: kfzx-guoch
	 * @date: 2018/12/14 13:47
	 */
	public void updateApplyState(ParamBean paramBean){
		String currentNodeCode = paramBean.getStr("CURRENT_NODE_CODE");
		String nodeCode = paramBean.getStr("NEXT_NODE_CODE");
		String ID = paramBean.getStr("ID");
		String deptCode = paramBean.getStr("DEPT_CODE");
		String flag = currentNodeCode + nodeCode;
		String office_state = "";
		String plan_state = "";
		switch (flag) {
			case "N1N2": {
				office_state = APPLY_STATE_2;
				break;
			}
			case "N2N1": {
				office_state = APPLY_STATE_3;
				break;
			}
			case "N1N4": {
				office_state = APPLY_STATE_4;
				break;
			}
			case "N1N45": {
				office_state = APPLY_STATE_5;
				break;
			}
			case "N22N45": {
//				OutBean outBean = IsHost(new ParamBean().set("approId", ID).set("deptCode", deptCode));
//				String str = outBean.getStr("IsHost");
//				if("true".equals(str)){
//					office_state = APPLY_STATE_5;
//				}
				office_state = APPLY_STATE_5;
				plan_state = APPLY_STATE_3;
				break;
			}
			case "N45N3": {
				office_state = APPLY_STATE_6;
				break;
			}
			case "N27N28": {
				office_state = APPLY_STATE_7;
				break;
			}
			case "N28N27": {
				office_state = APPLY_STATE_6;
				break;
			}
			case "N3N45": {
				office_state = APPLY_STATE_5;
				break;
			}
			//计划状态
			case "N23N22": {
				plan_state = APPLY_STATE_3;
				break;
			}
			case "N4N22": {
				plan_state = APPLY_STATE_2;
				break;
			}
			case "N22N4": {
				plan_state = APPLY_STATE_1;
				break;
			}
			case "N45N46": {
				// 填写完成时间，逾期天数
                addFinishTimeAndOverdueDay(ID);
				// 如果督察员直接进行督查办理，则需要补填写完成时间，逾期天数
				updateExpectedDay(ID);
				break;
			}
			case "N45N24": {
				// 如果督察员直接进行督查办理，则需要补填写完成时间，逾期天数
				updateExpectedDay(ID);
				break;
			}
			case "N3N5": {
				// 填写办结时间
                updateDealtTime(ID);
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                // 修改办理情况的状态
                updateGainState(ID, GAIN_ING_STATE, GAIN_CHECK_STATE);
				break;
			}
			case "N3N25": {
				// 填写办结时间
				updateDealtTime(ID);
				// 修改待办状态为 督查办结
				updateToDoOperation(ID);
				break;
			}
			case "N5N3": {
				// 填写办结时间
                updateDealtTime(ID);
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
				break;
			}
			case "N52N3": {
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
				break;
			}
			case "N25N3": {
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
				break;
			}
			case "N26N3": {
				// 修改待办状态为 督查办结
				updateToDoOperation(ID);
				break;
			}
			case "N27N3": {
				office_state = APPLY_STATE_6;
				// 修改待办状态为 督查办结
				updateToDoOperation(ID);
				break;
			}
			case "N28N3": {
				office_state = APPLY_STATE_6;
				// 修改待办状态为 督查办结
				updateToDoOperation(ID);
				break;
			}
		}
		// 修改主单状态
		if(StringUtils.isNotEmpty(office_state)){
			Transaction.getExecutor().execute("UPDATE " + SUP_APPRO_OFFICE + " SET APPLY_STATE = '" + office_state + "' WHERE ID = '" + ID + "' ");
		}
		// 修改 计划单 办理状态
		if(StringUtils.isNotEmpty(plan_state)){
			Transaction.getExecutor().execute("UPDATE " + SUP_APPRO_PLAN + " SET PLAN_STATE = '" + plan_state + "' WHERE APPRO_ID = '" + ID + "' ");
		}
	}
	
	
	/**
	 * 督查办理，填写完成时间，逾期天数
	 */
	public void addFinishTimeAndOverdueDay(String ID){
        String sql = "SELECT * FROM SUP_APPRO_OFFICE WHERE ID = '" + ID + "'";
        Bean office = Transaction.getExecutor().query(sql).get(0);
        office.set("FINISH_TIME", LocalDate.now());
        String limitDate = office.getStr("LIMIT_DATE");
        SupApperUrge supApperUrge = new SupApperUrge();
        String day = supApperUrge.getWorkDay(supApperUrge.getYDM(new Date()), limitDate);
        if(Integer.valueOf(day) <= 0){
        	office.set("OVERDUE_DAY", 0);
        } else {
        	office.set("OVERDUE_DAY", day);
        }
        ServDao.update("OA_SUP_APPRO_OFFICE", office);
    }
	
	
	/**
	 * 如果督察员直接进行督查办理，则需要补填写完成时间，逾期天数
	 */
	public void updateExpectedDay(String ID) {
		String sql = "SELECT * FROM SUP_APPRO_OFFICE WHERE ID = '" + ID + "'";
        Bean office = Transaction.getExecutor().query(sql).get(0);
        if(office.getStr("FINISH_TIME") == null || "".equals(office.getStr("FINISH_TIME"))){
        	office.set("FINISH_TIME", LocalDate.now());
        }
        String limitDate = office.getStr("LIMIT_DATE");
        SupApperUrge supApperUrge = new SupApperUrge();
        String day = supApperUrge.getWorkDay(supApperUrge.getYDM(new Date()), limitDate);
        if(Integer.valueOf(day) <= 0){
        	office.set("OVERDUE_DAY", 0);
        } else {
        	office.set("OVERDUE_DAY", day);
        }
        ServDao.update("OA_SUP_APPRO_OFFICE", office);
	}
	
	
	/**
	 * 督查办结的时候填写办结时间
	 */
	public void updateDealtTime(String ID){
        String sql = "SELECT * FROM SUP_APPRO_OFFICE WHERE ID = '" + ID + "'";
        Bean office = Transaction.getExecutor().query(sql).get(0);
        office.set("DEALT_TIME", LocalDate.now());
        ServDao.update("OA_SUP_APPRO_OFFICE", office);
    }
	
	
	 /**
     * 更新待办表的 当前操作环节
     */
    public void updateToDoOperation(String ID) {
        Transaction.getExecutor().execute("UPDATE SY_COMM_TODO SET TODO_OPERATION = '督查办结' WHERE TODO_OBJECT_ID1 = '" + ID + "' AND TODO_CODE_NAME != '分发'");
    }
	
    
    /**
     * 推送至办结环节，校验办理状态
     */
    private void updateGainState(String approId, String curState, String upState) {
    	ParamBean paramBean = new ParamBean();
    	paramBean.set("approId", approId);
    	paramBean.set("curState", curState);
    	paramBean.set("upState", upState);
    	new SupApproGain().updateWfState(paramBean);
    }
    
	
	/**
	 * 导入方法开始的入口
	 */
	public OutBean saveFromExcel(ParamBean paramBean) {
		String fileId = paramBean.getStr("FILE_ID");
		//保存方法入口
		paramBean.set("SERVMETHOD", "savedata");
		Transaction.begin();
		OutBean out = ImpUtils.getDataFromXls(fileId, paramBean);
		Transaction.end();
		String failnum = out.getStr("failernum");
		String successnum = out.getStr("oknum");
		String errorMsg = out.getStr("_MSG_");
		if(errorMsg.contains(Constant.RTN_MSG_ERROR)){
			return new OutBean().setError(errorMsg);
		}
		//返回导入结果
		if(Integer.valueOf(failnum) > 0 ){
			return new OutBean().set("FILE_ID",out.getStr("fileid")).set("_MSG_", "正确数据："+successnum+"条；错误数据："+(Integer.valueOf(failnum)+1)+"条；请修改后重新导入。");
		} else {
			return new OutBean().set("FILE_ID",out.getStr("fileid")).set("_MSG_", "正确数据："+successnum+"条；错误数据："+failnum+"条；导入成功。请前往我的工作-待办工作中查看待办事项。");
		}
	}

	/**
	 *   导入保存的方法
	 * @param paramBean
	 * @return
	 */
    public OutBean savedata(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        String odept_code = userBean.getStr("ODEPT_CODE");

        // 获取所有提醒规则类型
        List<Bean> cueTypeEnumList = findTypeList(CUE_TYPE, odept_code);
        // 获取所有办理类型
        List<Bean> handleTypeEnumList = findTypeList(HANDLE_TYPE, odept_code);
        // 获取所有督查事项类型
        List<Bean> itemTypeEnumList = findTypeList(ITEM_TYPE, odept_code);
        // 获取所有统计用事项来源
        List<Bean> itemSoureEnumList = findTypeList(STATIS_ITEM_SOURCE, odept_code);

        // 获取文件内容
        List<Bean> rowBeanList = paramBean.getList("datalist");
        // 创建set用于筛选 提醒规则类型，办理类型，督查事项类型，统计用事项来源 的重复项
        HashMap<String, String> map = new HashMap<>();
        // 校验数据 数据全部正确 提交事务
        boolean flag = true;
        StringBuffer errorMsg = null;
        List<ParamBean> returnList = new ArrayList<>();
        for (int i = 0; i < rowBeanList.size(); i++) {
            StringBuffer sb = new StringBuffer();

            Bean rowbean = rowBeanList.get(i);
            errorMsg = new StringBuffer();

            // 提醒规则类型
            String cueType = rowbean.getStr(ImpUtils.COL_NAME + "2");
            boolean cueTypeFlag = false;
            for (Bean bean : cueTypeEnumList) {
                System.out.println(bean.getStr("ID") + "---" + bean.getStr("DICT_NAME"));
                if (bean.getStr("DICT_NAME").equals(cueType)) {
                    cueTypeFlag = true;
                    cueType = bean.getStr("ID");
                    sb.append(cueType);
                }
            }
            if (!cueTypeFlag) {
                errorMsg.append("提醒规则类型不存在！");
                flag = false;
            }

            // 办理类型
            String handleType = rowbean.getStr(ImpUtils.COL_NAME + "3");
            boolean handleTypeFlag = false;
            for (Bean bean : handleTypeEnumList) {
                System.out.println(bean.getStr("ID") + "---" + bean.getStr("DICT_NAME"));
                if (bean.getStr("DICT_NAME").equals(handleType)) {
                    handleTypeFlag = true;
                    handleType = bean.getStr("ID");
                    sb.append(handleType);
                }
            }
            if (!handleTypeFlag) {
                errorMsg.append("办理类型不存在！");
                flag = false;
            }

            // 督查事项类型
            String itemType = rowbean.getStr(ImpUtils.COL_NAME + "4");
            boolean itemTypeFlag = false;
            for (Bean bean : itemTypeEnumList) {
                System.out.println(bean.getStr("ID") + "---" + bean.getStr("DICT_NAME"));
                if (bean.getStr("DICT_NAME").equals(itemType)) {
                    itemTypeFlag = true;
                    itemType = bean.getStr("ID");
                    sb.append(itemType);
                }
            }
            if (!itemTypeFlag) {
                errorMsg.append("督查事项类型不存在！");
                flag = false;
            }

            // 统计用事项来源
            String itemSoure = rowbean.getStr(ImpUtils.COL_NAME + "5");
            boolean itemSoureFlag = false;
            for (Bean bean : itemSoureEnumList) {
                System.out.println(bean.getStr("ID") + "---" + bean.getStr("DICT_NAME"));
                if (bean.getStr("DICT_NAME").equals(itemSoure)) {
                    itemSoureFlag = true;
                    itemSoure = bean.getStr("ID");
                    sb.append(itemSoure);
                }
            }
            if (!itemSoureFlag) {
                errorMsg.append("统计用事项来源不存在！");
                flag = false;
            }

            if (map.containsKey(sb.toString())) {
                map.put(sb.toString(), map.get(sb.toString()) + "," + (i + 1));
            } else {
                map.put(sb.toString(), (i + 1) + "");
            }

            String SUPERV_ITEM = rowbean.getStr(ImpUtils.COL_NAME + "6"); // 督查事项
            String APPR_DATE = rowbean.getStr(ImpUtils.COL_NAME + "7"); // 立项时间
            String LIMIT_DATE = rowbean.getStr(ImpUtils.COL_NAME + "8"); // 时限要求
            String NOT_LIMIT_TIME_REASON = rowbean.getStr(ImpUtils.COL_NAME + "9"); // 未明确完成时限的原因
            String ISSUE_CODE = rowbean.getStr(ImpUtils.COL_NAME + "10"); // 发文字号
            String ISSUE_DEPT = rowbean.getStr(ImpUtils.COL_NAME + "11"); // 成文单位
            String SECRET_RANK = rowbean.getStr(ImpUtils.COL_NAME + "12"); // 文件密级
            String CENTER_DENOTE = rowbean.getStr(ImpUtils.COL_NAME + "13"); // 党中央国务院领导指示要求
            String LEAD_DENOTE = rowbean.getStr(ImpUtils.COL_NAME + "14"); // 署领导批示
            String DEPT_DENOTE = rowbean.getStr(ImpUtils.COL_NAME + "15"); // 办公厅拟办（办理）意见
            String HOST_DEPT = rowbean.getStr(ImpUtils.COL_NAME + "16"); // 主办单位
            String OTHER_DEPT = rowbean.getStr(ImpUtils.COL_NAME + "17"); // 其他主办单位
            String ASSIT_DEPT = rowbean.getStr(ImpUtils.COL_NAME + "18"); // 协办单位
            String REMARK = rowbean.getStr(ImpUtils.COL_NAME + "19"); // 备注
            String SELF_REMARK = rowbean.getStr(ImpUtils.COL_NAME + "20"); // 备注（自用）
            String ITEM_SOURCE = rowbean.getStr(ImpUtils.COL_NAME + "21"); // 事项来源（文件标题等）
            String ITEM_SOURCE_TYPE = rowbean.getStr(ImpUtils.COL_NAME + "22"); // 事项来源（1.系统内;2.系统2外））
            String ITEM_SOURCE_INPUT = rowbean.getStr(ImpUtils.COL_NAME + "23"); // 事项来源（手输）
            String GONGWEN_ID = ""; // 公文ID

            // 根据主办单位名称获得主办单位的信息
            ParamBean hostBean = findDeptByName(HOST_DEPT, errorMsg);
            if (hostBean.getBoolean("flag")) {
                hostBean.set("DEPT_TYPE", "1");
            } else {
                flag = hostBean.getBoolean("flag");
            }
            // 根据其他主办单位名称获得其他主办单位的信息
            ParamBean otherBean = findDeptByName(OTHER_DEPT, errorMsg);
            if (otherBean.getBoolean("flag")) {
                otherBean.set("DEPT_TYPE", "2");
            } else {
                flag = otherBean.getBoolean("flag");
            }
            // 根据协办单位名称获得协办单位的信息
            ParamBean assitBean = findDeptByName(ASSIT_DEPT, errorMsg);
            if (assitBean.getBoolean("flag")) {
                assitBean.set("DEPT_TYPE", "3");
            } else {
                flag = assitBean.getBoolean("flag");
            }

            // 校验关联公文文件是否正确
			if(ITEM_SOURCE_TYPE.equals("1") ){
            	// 通过 文件标题 和 发文字号 校验
				if (ITEM_SOURCE!="" && ISSUE_CODE!=""){
					String jgjc = ISSUE_CODE.substring(0,ISSUE_CODE.indexOf("〔"));
					String gwYear = ISSUE_CODE.substring(ISSUE_CODE.indexOf("〔")+1,ISSUE_CODE.indexOf("〕"));
					String yearNum = ISSUE_CODE.substring(ISSUE_CODE.indexOf("〕")+1);
					String sql = "SELECT * FROM OA_GW_GONGWEN GW LEFT JOIN OA_GW_FWZH_JCDZ JCDZ ON JCDZ.FJ_ID = GW.GW_YEAR_CODE " +
							"WHERE GW.GW_TITLE = '"+ITEM_SOURCE+"' AND JCDZ.FINAL_JGJC = '"+jgjc+"' AND GW.GW_YEAR = '"+gwYear+"' AND GW.GW_YEAR_NUMBER = '"+yearNum+"'";
					List<Bean> gwList = Transaction.getExecutor().query(sql);
					if (gwList!=null && gwList.size()==1){
						Bean gw = gwList.get(0);
						GONGWEN_ID = gw.getStr("GW_ID");
						ISSUE_DEPT = gw.getStr("GW_CW_TNAME");
						SECRET_RANK = gw.getStr("GW_SRCRET");
					}else{
						errorMsg.append("关联公文文件不正确，请重新输入！");
						flag = false;
					}
				}else{
					errorMsg.append("内部关联文件输入为空，请确认公文标题和发文字号是否填写！");
					flag = false;
				}
			}

            if (!"".equals(errorMsg.toString().trim())) {
                rowbean.set(ImpUtils.ERROR_NAME, errorMsg.toString().trim());
            } else {
                ParamBean bean = new ParamBean(); // 存放导入的所有信息
                // 生成立项编号
                String currentDate = DateUtils.getDate();
                String year = currentDate.substring(0, 4);
                ParamBean p = new ParamBean();
                p.set("actCode", "cardAdd");//操作表示
                p.set("servId", "OA_SUP_APPRO_OFFICE");//服务ID
                p.set("nowYear", year);//当前年份
                Bean itemNum = getItemNum(p);
                String item = "督立" + "〔" + year + "〕" + itemNum.getStr("ITEM_NUM") + "号";

                // 生成系统编号
                String time = DateUtils.getTime();
                String timeCode = time.substring(0, 5);
                String sCode = "SUP" + currentDate.replace("-", "") + timeCode.replace(":", "") + "000" + itemNum.getStr("ITEM_NUM");
                // 给主表立项单的字段赋值
                bean.set("ITEM_NUM", item);
                bean.set("S_CODE", sCode);
                bean.set("CUE_TYPE", cueType);
                bean.set("HANDLE_TYPE", handleType);
                bean.set("ITEM_TYPE", itemType);
                bean.set("STATIS_ITEM_SOURCE", itemSoure);
                bean.set("SUPERV_ITEM", SUPERV_ITEM);
                bean.set("APPR_DATE", APPR_DATE);
                bean.set("LIMIT_DATE", LIMIT_DATE);
                bean.set("NOT_LIMIT_TIME_REASON", NOT_LIMIT_TIME_REASON);
                bean.set("CENTER_DENOTE", CENTER_DENOTE);
                bean.set("LEAD_DENOTE", LEAD_DENOTE);
                bean.set("DEPT_DENOTE", DEPT_DENOTE);
                bean.set("REMARK", REMARK);
                bean.set("SELF_REMARK", SELF_REMARK);
				bean.set("ITEM_SOURCE_TYPE", ITEM_SOURCE_TYPE);

				bean.set("ISSUE_CODE", ISSUE_CODE);
				bean.set("ISSUE_DEPT", ISSUE_DEPT);
				bean.set("SECRET_RANK", SECRET_RANK);

                // 事项来源系统内 关联公文内容
                if(ITEM_SOURCE_TYPE.equals("1")){
					bean.set("ITEM_SOURCE", ITEM_SOURCE);// 事项来源（文件标题）
					bean.set("GONGWEN_ID",GONGWEN_ID);// 关联公文ID
				}else if(ITEM_SOURCE_TYPE.equals("2")){
                	// 事项来源系统外 保存Excel填写的信息
					bean.set("ITEM_SOURCE_INPUT", ITEM_SOURCE_INPUT);// 事项来源（手输）
				}

                // 立项单单位赋值
                bean.set("hostBean", hostBean);
                bean.set("otherBean", otherBean);
                bean.set("assitBean", assitBean);
                returnList.add(bean);
            }
        }
        // 修改逻辑 校验 这四项指标在数据库中是否已存在相同的记录

        StringBuffer titleError = new StringBuffer();
        if (map.size() < rowBeanList.size()) {
            for (String str : map.keySet()) {
                titleError.append(map.get(str) + "行数据重复！");
            }
            flag = false;
        }

        // 批量插入数据
        // flag 为 true 表示 数据全部正确，false表示有错误数据存在，不进行批量插入
        if (flag) {
            // 批量保存
            for (ParamBean returnBean : returnList) {
                ParamBean bean = new ParamBean();
                bean.set("ITEM_NUM", returnBean.getStr("ITEM_NUM"));
                bean.set("S_CODE", returnBean.getStr("S_CODE"));
                bean.set("CUE_TYPE", returnBean.getStr("CUE_TYPE"));
                bean.set("HANDLE_TYPE", returnBean.getStr("HANDLE_TYPE"));
                bean.set("ITEM_TYPE", returnBean.getStr("ITEM_TYPE"));
                bean.set("STATIS_ITEM_SOURCE", returnBean.getStr("STATIS_ITEM_SOURCE"));
                bean.set("SUPERV_ITEM", returnBean.getStr("SUPERV_ITEM"));
                bean.set("APPR_DATE", returnBean.getStr("APPR_DATE"));
                bean.set("LIMIT_DATE", returnBean.getStr("LIMIT_DATE"));
                bean.set("NOT_LIMIT_TIME_REASON", returnBean.getStr("NOT_LIMIT_TIME_REASON"));
                bean.set("ISSUE_CODE", returnBean.getStr("ISSUE_CODE"));
                bean.set("ISSUE_DEPT", returnBean.getStr("ISSUE_DEPT"));
                bean.set("SECRET_RANK", returnBean.getStr("SECRET_RANK"));
                bean.set("CENTER_DENOTE", returnBean.getStr("CENTER_DENOTE"));
                bean.set("LEAD_DENOTE", returnBean.getStr("LEAD_DENOTE"));
                bean.set("DEPT_DENOTE", returnBean.getStr("DEPT_DENOTE"));
                bean.set("REMARK", returnBean.getStr("REMARK"));
                bean.set("SELF_REMARK", returnBean.getStr("SELF_REMARK"));
                bean.set("ITEM_SOURCE", returnBean.getStr("ITEM_SOURCE"));
                bean.set("ITEM_SOURCE_TYPE", returnBean.getStr("ITEM_SOURCE_TYPE"));
                bean.set("ITEM_SOURCE_INPUT", returnBean.getStr("ITEM_SOURCE_INPUT"));
                bean.set("GONGWEN_ID", returnBean.getStr("GONGWEN_ID"));
                bean.setServId("OA_SUP_APPRO_OFFICE");
                OutBean officeBean = save(bean);
                String beanId = officeBean.getStr("ID");
                // 保存主办协办单位
                ParamBean hostBean = (ParamBean) returnBean.get("hostBean");
                hostBean.set("OFFICE_ID", beanId);
                hostBean.setServId("OA_SUP_APPRO_OFFICE_HOST");
                save(hostBean);
                ParamBean otherBean = (ParamBean) returnBean.get("otherBean");
                otherBean.set("OFFICE_ID", beanId);
                otherBean.setServId("OA_SUP_APPRO_OFFICE_OTHER");
                save(otherBean);
                ParamBean assitBean = (ParamBean) returnBean.get("assitBean");
                assitBean.set("OFFICE_ID", beanId);
                assitBean.setServId("OA_SUP_APPRO_OFFICE_ASSIT");
                save(assitBean);
            }
        }
        OutBean outBean = new OutBean();
        outBean.set("alllist", rowBeanList).set("successlist", returnList).set("flag", flag);
        outBean.set("titleError", titleError.toString());
        return outBean;
    }

	/**
	 *   根绝字典ID 获取字典的list值
	 * @param type
	 * @param odept_code
	 * @return
	 */
	public List<Bean> findTypeList(String type,String odept_code){
		String enumSql = "SELECT DICT_NAME,ID FROM " + SUP_SERV_DICT + " WHERE DICT_KINDS = '" + type + "' AND S_ODEPT = '" + odept_code + "'";
		return Transaction.getExecutor().query(enumSql);
	}

    /**
     *  根据单位名称校验单位的默认领导默认督查员是否存在
     * @param deptName  单位名称
     * @param errorMsg  错误信息
     * @return
     */
    public ParamBean findDeptByName(String deptName,StringBuffer errorMsg){
        boolean flag = true;
        ParamBean deptBean = new ParamBean();
        // 根据填写的单位名称查找单位code
        String sql = "SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE DEPT_NAME = '"+deptName+"'";
        Bean dept = Transaction.getExecutor().queryOne(sql);
        if(dept!=null){
            String deptCode =dept.getStr("DEPT_CODE");
            if (deptCode!=""){
                //根据单位code 查找本单位默认的领导信息
                String leaderSql = "SELECT * FROM SUP_SERV_ORG_DEPT WHERE ORG_DEPT_CODE = '"+deptCode+"' AND IS_DEF_LEADER = '1' AND DEPT_TYPE = '1'";
                List<Bean> leaderList = Transaction.getExecutor().query(leaderSql);
                if (leaderList!=null&&leaderList.size()>0){
                    //取默认的领导信息
                    Bean leader = leaderList.get(0);
                    deptBean.set("D_USER_CODE",leader.getStr("ORG_USER_CODE"));
                }else{
                    errorMsg.append("输入的"+deptName+"默认领导不存在！");
                    flag = false;
                }

                // 根据单位code 查找本单位默认的督察员信息
                String inspectorSql = "SELECT * FROM SUP_SERV_ORG_DEPT WHERE ORG_DEPT_CODE = '"+deptCode+"' AND IS_DEF_INSPECTOR = '1' AND DEPT_TYPE = '1'";
                List<Bean> inspectorList = Transaction.getExecutor().query(inspectorSql);
                if (inspectorList!=null&inspectorList.size()>0){
                    //取默认督查员的信息
                    Bean inspector = inspectorList.get(0);
                    deptBean.set("C_USER_CODE",inspector.getStr("ORG_USER_CODE"));
                    deptBean.set("DEPT_PHONE",inspector.getStr("ORG_USER_TEL"));
                }else{
                    errorMsg.append("输入的"+deptName+"默认督查员不存在！");
                    flag = false;
                }
                deptBean.set("DEPT_CODE",deptCode);
            }else{
                errorMsg.append("输入的"+deptName+"code不存在！");
                flag = false;
            }
        }else{
            errorMsg.append("输入的"+deptName+"不存在！");
            flag = false;
        }
        deptBean.set("flag",flag);
        return deptBean;
    }

}