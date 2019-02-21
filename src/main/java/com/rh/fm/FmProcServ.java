package com.rh.fm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.file.FileHelper;

public class FmProcServ extends CommonServ {
	private final static String filePath = Context.appStr(APP.WEBINF).replace("/", File.separator) + "flowData" + File.separator;
	
	protected void beforeQuery(ParamBean paramBean) {
		UserBean userBean = Context.getUserBean();
		String codePath = userBean.getODeptCodePath();
		String odeptCode = userBean.getODeptCode();
		int odeptLevel = userBean.getODeptLevel();
		List<Bean> list = ServDao.finds("FM_PROC_AUTH", new ParamBean());
		
		StringBuilder sb = new StringBuilder();
		for (Bean bean : list) {
			//流程编码
			String PROC_CODE = bean.getStr("PROC_CODE");
			//所属单位
			String PROC_ODEPT_CODE = bean.getStr("PROC_ODEPT_CODE");
			//公共标志 1.是 2.否
			int PUBLIC_FLAG = bean.getInt("PUBLIC_FLAG");
			//共享级别 1.总公司 2.省分公司 3.地市公司 4.县支分公司
			int SHARE_LEVEL = bean.getInt("SHARE_LEVEL");
			String path = OrgMgr.getDept(PROC_ODEPT_CODE).getCodePath();
			if(PUBLIC_FLAG == 1){
				switch (SHARE_LEVEL) {
				case 1:
					if(codePath.indexOf(path) != -1){
						sb.append(PROC_CODE+",");
					}
					break;
				case 2:
					if(codePath.indexOf(path) != -1 && odeptLevel <= 2){
						sb.append(PROC_CODE+",");
					}
					break;
				case 3:
					if(codePath.indexOf(path) != -1 && odeptLevel <= 3){
						sb.append(PROC_CODE+",");
					}
					break;
				case 4:
					if(codePath.indexOf(path) != -1 && odeptLevel <= 4){
						sb.append(PROC_CODE+",");
					}
					break;
				default:
					break;
				}
			}else{
				if(odeptCode.equals(PROC_ODEPT_CODE)){
					sb.append(PROC_CODE+",");
				}
			}
		}
		
		if(sb.length() > 0){
			String where = paramBean.getQueryExtWhere();
			String scope = sb.toString().replaceAll(",", "','");
			where += " and APPLY_CODE in ('"+scope+"')";
			paramBean.setQueryExtWhere(where);
		}else{
			paramBean.setQueryExtWhere("and 1=2");
		}
    }
	
	public static Bean getFile(){
		Calendar today = Calendar.getInstance();
		int year = today.get(Calendar.YEAR);
		int week = today.get(Calendar.WEEK_OF_YEAR);
		String fileName = year +""+ week + ".txt";
		String path = filePath + fileName;
		File file = new File(path);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedReader reader = null;
		// 返回值,使用StringBuffer
		StringBuffer data = new StringBuffer();
		try {
			reader = new BufferedReader(new FileReader(file));
			// 每次读取文件的缓存
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				data.append(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Bean();
		}  finally {
			// 关闭文件流
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Bean dataBean = JsonUtils.toBean(data.toString());
		return dataBean;
	}
	/**
	 * 把流程看板数据结果写入本地文件
	 * @param methodNum 方法号
	 * @param odeptCode 机构编码
	 * @param tdeptCode 部门编码
	 * @param type		数据根据时间统计类型 1.年 2.周 3.月
	 * @param cengType  全机构数据/本机构数据  0.不区分 1.全机构数据 2.本机构数据
	 * @param outBean   结果
	 */
	public static void writeFile(int methodNum,String odeptCode,String tdeptCode,int type,int cengType,OutBean outBean){
		Calendar today = Calendar.getInstance();
		int year = today.get(Calendar.YEAR);
		int week = today.get(Calendar.WEEK_OF_YEAR);
		String fileName = year +""+ week + ".txt";
		String path = filePath + fileName;
		try {
			checkFile(new File(path));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Bean writeBean = getFile();
		switch (methodNum) {
		case 1:
			Bean tmpBean1 = writeBean.getBean("data1");
			tmpBean1.set(type+"", outBean);
			writeBean.set("data1", tmpBean1);
			break;
		case 2:
			Bean tmpBean2 = writeBean.getBean("data2");
			// 本机构
			Bean myOdeptDataBean2 = tmpBean2.getBean(odeptCode);
			myOdeptDataBean2.set(type + "", outBean);
			tmpBean2.set(odeptCode, myOdeptDataBean2);
			writeBean.set("data2", tmpBean2);
			break;		
		case 3:
			Bean tmpBean3 = writeBean.getBean("data3");
			// 本部门
			Bean myOdeptDataBean3 = tmpBean3.getBean(tdeptCode);
			myOdeptDataBean3.set(type + "", outBean);
			tmpBean3.set(tdeptCode, myOdeptDataBean3);
			writeBean.set("data3", tmpBean3);
			break;	
		case 4:
			Bean tmpBean4 = writeBean.getBean("data4");
			// 本部门
			Bean myOdeptDataBean4 = tmpBean4.getBean(tdeptCode);
			myOdeptDataBean4.set(type + "", outBean);
			tmpBean4.set(tdeptCode, myOdeptDataBean4);
			writeBean.set("data4", tmpBean4);
			break;
		case 5:
			Bean tmpBean5 = writeBean.getBean("data5");
			// 本部门
			Bean myOdeptDataBean5 = tmpBean5.getBean(tdeptCode);
			myOdeptDataBean5.set(type + "", outBean);
			tmpBean5.set(tdeptCode, myOdeptDataBean5);
			writeBean.set("data5", tmpBean5);
			break;
		case 6:
			Bean tmpBean6 = writeBean.getBean("data6");
			if(cengType == 2){
				//本机构
				Bean myOdeptDataBean = tmpBean6.getBean(odeptCode);
				myOdeptDataBean.set(type+"", outBean);
				tmpBean6.set(odeptCode, myOdeptDataBean);
			}else{
				//全系统
				Bean allBean = tmpBean6.getBean("all");
				allBean.set(type+"", outBean);
				tmpBean6.set("all", allBean);
			}
			writeBean.set("data6", tmpBean6);
			break;
		case 7:
			Bean tmpBean7 = writeBean.getBean("data7");
			if(cengType == 2){
				//本机构
				Bean myOdeptDataBean = tmpBean7.getBean(odeptCode);
				myOdeptDataBean.set(type+"", outBean);
				tmpBean7.set(odeptCode, myOdeptDataBean);
			}else{
				//全系统
				Bean allBean = tmpBean7.getBean("all");
				allBean.set(type+"", outBean);
				tmpBean7.set("all", allBean);
			}
			writeBean.set("data7", tmpBean7);
			break;
		case 8:
			Bean tmpBean8 = writeBean.getBean("data8");
			if(cengType == 2){
				//本机构
				Bean myOdeptDataBean = tmpBean8.getBean(odeptCode);
				myOdeptDataBean.set(type+"", outBean);
				tmpBean8.set(odeptCode, myOdeptDataBean);
			}else{
				//全系统
				Bean allBean = tmpBean8.getBean("all");
				allBean.set(type+"", outBean);
				tmpBean8.set("all", allBean);
			}
			writeBean.set("data8", tmpBean8);
			break;
		case 9:
			Bean tmpBean9 = writeBean.getBean("data9");
			if(cengType == 2){
				//本机构
				Bean myOdeptDataBean = tmpBean9.getBean(odeptCode);
				myOdeptDataBean.set(type+"", outBean);
				tmpBean9.set(odeptCode, myOdeptDataBean);
			}else{
				//全系统
				Bean allBean = tmpBean9.getBean("all");
				allBean.set(type+"", outBean);
				tmpBean9.set("all", allBean);
			}
			writeBean.set("data9", tmpBean9);
			break;
		case 10:
			Bean tmpBean10 = writeBean.getBean("data10");
			tmpBean10.set(type+"", outBean);
			writeBean.set("data10", tmpBean10);
			break;
		default:
			break;
		}
		
		try {
			FileHelper.toJsonFile(writeBean, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static String checkFile(File file) throws IOException {
		if (!file.exists())
			file.getParentFile().mkdirs();
		return file.getAbsolutePath();
	}
	public OutBean getData1(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		//1.年 2.周 3.月
		int type = paramBean.getInt("type");
		Bean fileData = getFile();
		if(fileData.getBean("data1").isNotEmpty(type+"")){
			outBean.setData(fileData.getBean("data1").getBean(type+"").get("_DATA_"));
			return outBean;
		}
		
		ParamBean whereBean1 = new ParamBean();
		ParamBean whereBean2 = new ParamBean();
		ParamBean whereBean3 = new ParamBean();
		ParamBean whereBean4 = new ParamBean();
		whereBean1.setWhere("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"'");
		whereBean2.setSelect("DISTINCT APPLY_CATALOG");
		whereBean2.setWhere("and S_ATIME >= '"+start+"' and S_ATIME <= '"+end+"'");
		whereBean3.setSelect("sum(REG_BEFORE_OVER_TIME - day) as val");
		whereBean3.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end + "' and REG_BEFORE_OVER_TIME is not null");
		whereBean4.setSelect("DISTINCT DONE_USER_ID");
		whereBean4.setWhere("and NODE_ETIME	 >= '"+start+"' and NODE_ETIME <= '"+end+"'");
		int num1 = ServDao.count("FM_PROC_ONLINE_REG", whereBean1);
		int num2 = ServDao.count("FM_PROC_APPLY", whereBean2);
		Double num3 = ServDao.finds("FM_PROC_REG_TJ_V2", whereBean3).get(0).getDouble("VAL");
		int num4 = ServDao.count("SY_WFE_TRACK", whereBean4);
		List<Integer> array = new ArrayList<Integer>();
		array.add(num1);
		array.add(num2);
		array.add(num3.intValue());
		array.add(num4);
		outBean.setData(array);
		writeFile(1,null,null,type,0,outBean);
		return outBean;
	}
	
	@SuppressWarnings("deprecation")
	public OutBean getData2(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");
		// 获取总行机构
		UserBean user = Context.getUserBean();
		String myOdept = user.getODeptCode();
		String cmpyCode = user.getCmpyCode();
		
		Bean fileData = getFile();
		if (fileData.getBean("data2").isNotEmpty(myOdept)
				&& fileData.getBean("data2").getBean(myOdept).isNotEmpty(type + "")) {
			outBean.setData(fileData.getBean("data2").getBean(myOdept).getBean(type + "").get("_DATA_"));
			return outBean;
		}
		//左侧数据
		ParamBean whereBean1 = new ParamBean();
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"'");
		//type 1年 2周 3月
		if(type == 1){
			whereBean1.setSelect("substr(REG_ONLINE_TIME, 0, 7) as unit,count(*) as num");
			sbWhere.append(" group by substr(REG_ONLINE_TIME, 0, 7)");
		}else{
			whereBean1.setSelect("REG_ONLINE_TIME as unit,count(*) as num");
			sbWhere.append(" group by REG_ONLINE_TIME");
		}
		whereBean1.setWhere(sbWhere.toString());
		List<Bean> list = ServDao.finds("FM_PROC_ONLINE_REG", whereBean1);
		List<Integer> arrLeft = new ArrayList<Integer>();
		List<String> arrTmp = new ArrayList<String>();
		//年
		if(type == 1){
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR) - 1;
			int month = cal.get(Calendar.MONTH)+2;
			if(month > 12){
				month = 1;
				year += 1;
			}
			for (int i = 0; i < 12; i++) {
				int amonth = month;
				String tmpMonth = year + "-" + (amonth < 10 ? ("0" + amonth) : amonth);
				arrTmp.add(tmpMonth);
				month += 1;
				if (month > 12) {
					month = 1;
					year += 1;
				}
			}
			
			for (int i = 0; i < arrTmp.size(); i++) {
				boolean addFlag = true;
				for (int j = 0; j < list.size(); j++) {
					String unit = list.get(j).getStr("UNIT");
					if(unit.equals(arrTmp.get(i))){
						int num = list.get(j).getInt("NUM");
						arrLeft.add(num);
						addFlag = false;
						break;
					}
				}
				if(addFlag){
					arrLeft.add(0);
				}
			}
		}else{
			//周
			//月
			//start
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        try {
				Date startDate = sdf.parse(start);
				Date endDate = sdf.parse(end);
				endDate.setDate(endDate.getDate()+1);
				while(!DateUtils.isSameDay(startDate, endDate)){
					String tmpDate = sdf.format(startDate);
					boolean addFlag = true;
					for (int i = 0; i < list.size(); i++) {
						String unit = list.get(i).getStr("UNIT");
						if(unit.equals(tmpDate)){
							int num = list.get(i).getInt("NUM");
							arrLeft.add(num);
							addFlag = false;
							break;
						}
					}
					if(addFlag){
						arrLeft.add(0);
					}
					startDate.setDate(startDate.getDate()+1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		Bean resBean = new Bean();
		resBean.set("leftData", arrLeft);
		
		//右侧数据
		
		ParamBean whereBean2 = new ParamBean();
		whereBean2.setSelect("DEPT_CODE");
		whereBean2.setWhere(" and dept_code = odept_code and dept_level = 2 and cmpy_code = '"+cmpyCode+"'");
		whereBean2.setOrder("dept_sort");
		List<Bean> listodept = ServDao.finds("SY_ORG_DEPT_ALL", whereBean2);
		String topOdept = listodept.get(0).getStr("DEPT_CODE");
		
		ParamBean whereBean3 = new ParamBean();
		whereBean3.setWhere("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"'");
		//所有
		int total= ServDao.count("FM_PROC_ONLINE_REG", whereBean3);
		
		ParamBean whereBean4 = new ParamBean();
		whereBean4.setWhere("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"' and REG_PROC_ODEPT = '"+topOdept+"'");
		//总行
		int topNum= ServDao.count("FM_PROC_ONLINE_REG", whereBean4);
		//分行
		int fenNum = total - topNum;
		ParamBean whereBean5 = new ParamBean();
		whereBean5.setWhere("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"' and REG_PROC_ODEPT = '"+myOdept+"'");
		//本行
		int myNum= ServDao.count("FM_PROC_ONLINE_REG", whereBean5);
		
		double percent1 = (double)(100*topNum)/total;
		double percent2 = 100 - percent1;
		double percent3 = (double)(100*myNum)/total;
		if(total == 0){
			percent1 = 0;
			percent2 = 0;
			percent3 = 0;
		}
		DecimalFormat df = new DecimalFormat("#0.00");
		
		String A2 = df.format(percent1) + "%";
		String B2 = df.format(percent2) + "%";
		String C2 = df.format(percent3) + "%";
		Bean rightData = new Bean();
		rightData.set("A1", topNum);
		rightData.set("A2", A2);
		rightData.set("B1", fenNum);
		rightData.set("B2", B2);
		rightData.set("C1", myNum);
		rightData.set("C2", C2);
		resBean.set("rightData", rightData);
		
		outBean.setData(resBean);
		writeFile(2,myOdept,null,type,0,outBean);
		return outBean;
	}
	
	/**
	 * 流程发起
	 * @param paramBean
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public OutBean getData3(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");
		
		// 获取总行机构
		UserBean user = Context.getUserBean();
		String mytdeptCode = user.getTDeptCode();
//		String cmpyCode = user.getCmpyCode();
		Bean fileData = getFile();
		if (fileData.getBean("data3").isNotEmpty(mytdeptCode)
				&& fileData.getBean("data3").getBean(mytdeptCode).isNotEmpty(type + "")) {
			outBean.setData(fileData.getBean("data3").getBean(mytdeptCode).getBean(type + "").get("_DATA_"));
			return outBean;
		}

		// 左侧数据
		ParamBean whereBean1 = new ParamBean();
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append("and S_ATIME >= '" + start + "' and S_ATIME <= '" + end + "'");
		// type 1年 2周 3月
		if (type == 1) {
			whereBean1.setSelect("substr(S_ATIME, 0, 7) as unit,count(*) as num");
			sbWhere.append(" group by substr(S_ATIME, 0, 7)");
		} else {
			whereBean1.setSelect("substr(S_ATIME, 0, 10) as unit,count(*) as num");
			sbWhere.append(" group by substr(S_ATIME, 0, 10)");
		}
		whereBean1.setWhere(sbWhere.toString());
		List<Bean> list = ServDao.finds("FM_PROC_APPLY", whereBean1);
		List<Integer> arrLeft = new ArrayList<Integer>();
		List<String> arrTmp = new ArrayList<String>();
		// 年
		if (type == 1) {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR) - 1;
			int month = cal.get(Calendar.MONTH) + 2;
			if (month > 12) {
				month = 1;
				year += 1;
			}
			for (int i = 0; i < 12; i++) {
				int amonth = month;
				String tmpMonth = year + "-" + (amonth < 10 ? ("0" + amonth) : amonth);
				arrTmp.add(tmpMonth);
				month += 1;
				if (month > 12) {
					month = 1;
					year += 1;
				}
			}

			for (int i = 0; i < arrTmp.size(); i++) {
				boolean addFlag = true;
				for (int j = 0; j < list.size(); j++) {
					String unit = list.get(j).getStr("UNIT");
					if (unit.equals(arrTmp.get(i))) {
						int num = list.get(j).getInt("NUM");
						arrLeft.add(num);
						addFlag = false;
						break;
					}
				}
				if (addFlag) {
					arrLeft.add(0);
				}
			}
		} else {
			// 周
			// 月
			// start
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date startDate = sdf.parse(start);
				Date endDate = sdf.parse(end);
				endDate.setDate(endDate.getDate() + 1);
				while (!DateUtils.isSameDay(startDate, endDate)) {
					String tmpDate = sdf.format(startDate);
					boolean addFlag = true;
					for (int i = 0; i < list.size(); i++) {
						String unit = list.get(i).getStr("UNIT");
						if (unit.equals(tmpDate)) {
							int num = list.get(i).getInt("NUM");
							arrLeft.add(num);
							addFlag = false;
							break;
						}
					}
					if (addFlag) {
						arrLeft.add(0);
					}
					startDate.setDate(startDate.getDate()+1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		Bean resBean = new Bean();
		resBean.set("leftData", arrLeft);

		// 右侧数据

		ParamBean whereBean3 = new ParamBean();
		whereBean3.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end + "'");
		// 所有
		int total = ServDao.count("FM_PROC_APPLY", whereBean3);

		ParamBean whereBean4 = new ParamBean();
		whereBean4.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end
				+ "' and s_odept in (select dept_code from sy_org_dept where dept_code = odept_code and dept_level < 3)");
		// 总/省 发起
		int topNum = ServDao.count("FM_PROC_APPLY", whereBean4);
		//地市及以下发起
		int dishiNum = total - topNum;
		ParamBean whereBean5 = new ParamBean();
		whereBean5.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end
				+ "' and s_tdept = '" + mytdeptCode + "'");
		// 本部门发起
		int myNum = ServDao.count("FM_PROC_APPLY", whereBean5);

		double percent1 = (double) (100 * topNum) / total;
		double percent2 = 100 - percent1;
		double percent3 = (double) (100 * myNum) / total;
		if(total == 0){
			percent1 = 0;
			percent2 = 0;
			percent3 = 0;
		}
		DecimalFormat df = new DecimalFormat("#0.00");

		String A2 = df.format(percent1) + "%";
		String B2 = df.format(percent2) + "%";
		String C2 = df.format(percent3) + "%";
		Bean rightData = new Bean();
		rightData.set("A1", topNum);
		rightData.set("A2", A2);
		rightData.set("B1", dishiNum);
		rightData.set("B2", B2);
		rightData.set("C1", myNum);
		rightData.set("C2", C2);
		resBean.set("rightData", rightData);

		outBean.setData(resBean);
		writeFile(3,null,mytdeptCode,type,0,outBean);
		return outBean;
	}
	/**
	 * 节约时效
	 * @param paramBean
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public OutBean getData4(ParamBean paramBean){
		DecimalFormat df = new DecimalFormat("#0.00");
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");
		UserBean user = Context.getUserBean();
		String mytdeptCode = user.getTDeptCode();
		String cmpyCode = user.getCmpyCode();
		
		Bean fileData = getFile();
		if (fileData.getBean("data4").isNotEmpty(mytdeptCode)
				&& fileData.getBean("data4").getBean(mytdeptCode).isNotEmpty(type + "")) {
			outBean.setData(fileData.getBean("data4").getBean(mytdeptCode).getBean(type + "").get("_DATA_"));
			return outBean;
		}
		
		// 左侧数据
		ParamBean whereBean1 = new ParamBean();
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append("and S_ATIME >= '" + start + "' and S_ATIME <= '" + end + "' and REG_BEFORE_OVER_TIME is not null");
		// type 1年 2周 3月
		if (type == 1) {
			whereBean1.setSelect("substr(S_ATIME, 0, 7) as unit,sum(REG_BEFORE_OVER_TIME) as day1,sum(day) as day2");
			sbWhere.append(" group by substr(S_ATIME, 0, 7)");
		} else {
			whereBean1.setSelect("substr(S_ATIME, 0, 10) as unit,sum(REG_BEFORE_OVER_TIME) as day1,sum(day) as day2");
			sbWhere.append(" group by substr(S_ATIME, 0, 10)");
		}
		whereBean1.setWhere(sbWhere.toString());
		List<Bean> list = ServDao.finds("FM_PROC_REG_TJ_V2", whereBean1);
		List<Double> arrLeftdata1 = new ArrayList<Double>();
		List<Double> arrLeftdata2 = new ArrayList<Double>();
		List<String> arrTmp = new ArrayList<String>();
		// 年
		if (type == 1) {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR) - 1;
			int month = cal.get(Calendar.MONTH) + 2;
			if (month > 12) {
				month = 1;
				year += 1;
			}
			for (int i = 0; i < 12; i++) {
				int amonth = month;
				String tmpMonth = year + "-" + (amonth < 10 ? ("0" + amonth) : amonth);
				arrTmp.add(tmpMonth);
				month += 1;
				if (month > 12) {
					month = 1;
					year += 1;
				}
			}

			for (int i = 0; i < arrTmp.size(); i++) {
				boolean addFlag = true;
				for (int j = 0; j < list.size(); j++) {
					String unit = list.get(j).getStr("UNIT");
					if (unit.equals(arrTmp.get(i))) {
						double day1 = list.get(j).getDouble("DAY1");
						double day2 = list.get(j).getDouble("DAY2");
						arrLeftdata1.add(Double.parseDouble(df.format(day1)));
						arrLeftdata2.add(Double.parseDouble(df.format(day2)));
						addFlag = false;
						break;
					}
				}
				if (addFlag) {
					arrLeftdata1.add(0.0);
					arrLeftdata2.add(0.0);
				}
			}
		} else {
			// 周
			// 月
			// start
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date startDate = sdf.parse(start);
				Date endDate = sdf.parse(end);
				endDate.setDate(endDate.getDate() + 1);
				while (!DateUtils.isSameDay(startDate, endDate)) {
					String tmpDate = sdf.format(startDate);
					boolean addFlag = true;
					for (int i = 0; i < list.size(); i++) {
						String unit = list.get(i).getStr("UNIT");
						if (unit.equals(tmpDate)) {
							double day1 = list.get(i).getDouble("DAY1");
							double day2 = list.get(i).getDouble("DAY2");
							arrLeftdata1.add(Double.parseDouble(df.format(day1)));
							arrLeftdata2.add(Double.parseDouble(df.format(day2)));
							addFlag = false;
							break;
						}
					}
					if (addFlag) {
						arrLeftdata1.add(0.0);
						arrLeftdata2.add(0.0);
					}
					startDate.setDate(startDate.getDate()+1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		Bean resBean = new Bean();
		Bean leftData = new Bean();
		leftData.set("data1", arrLeftdata1);
		leftData.set("data2", arrLeftdata2);
		resBean.set("leftData", leftData);

		// 右侧数据
		// 获取总行机构
		ParamBean whereBean2 = new ParamBean();
		whereBean2.setSelect("DEPT_CODE");
		whereBean2.setWhere(" and dept_code = odept_code and dept_level = 2 and cmpy_code = '" + cmpyCode + "'");
		whereBean2.setOrder("dept_sort");
		List<Bean> listodept = ServDao.finds("SY_ORG_DEPT_ALL", whereBean2);
		String topOdept = listodept.get(0).getStr("DEPT_CODE");

		ParamBean whereBean3 = new ParamBean();
		whereBean3.setSelect("sum(REG_BEFORE_OVER_TIME-day) as val");
		whereBean3.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end + "' and REG_BEFORE_OVER_TIME is not null");
		// 所有
		Double total = ServDao.finds("FM_PROC_REG_TJ_V2", whereBean3).get(0).getDouble("VAL");

		ParamBean whereBean4 = new ParamBean();
		whereBean4.setSelect("sum(REG_BEFORE_OVER_TIME-day) as val");
		whereBean4.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end
				+ "' and REG_BEFORE_OVER_TIME is not null and s_odept = '"+topOdept+"'");
		//总行节约时效
		Double topNum = ServDao.finds("FM_PROC_REG_TJ_V2", whereBean4).get(0).getDouble("VAL");
		//分行节约时效
		Double fenNum = total - topNum;
		ParamBean whereBean5 = new ParamBean();
		whereBean5.setSelect("sum(REG_BEFORE_OVER_TIME-day) as val");
		whereBean5.setWhere("and S_ATIME > '" + start + "' and S_ATIME < '" + end
				+ "' and REG_BEFORE_OVER_TIME is not null and s_tdept = '" + mytdeptCode + "'");
		//本部门节约时效
		Double myNum = ServDao.finds("FM_PROC_REG_TJ_V2", whereBean5).get(0).getDouble("VAL");

		double percent1 = (double) (100 * topNum) / total;
		double percent2 = 100 - percent1;
		double percent3 = (double) (100 * myNum) / total;
		if(total == 0){
			percent1 = 0;
			percent2 = 0;
			percent3 = 0;
		}

		String A2 = df.format(percent1) + "%";
		String B2 = df.format(percent2) + "%";
		String C2 = df.format(percent3) + "%";
		Bean rightData = new Bean();
		rightData.set("A1", df.format(topNum));
		rightData.set("A2", A2);
		rightData.set("B1", df.format(fenNum));
		rightData.set("B2", B2);
		rightData.set("C1", df.format(myNum));
		rightData.set("C2", C2);
		resBean.set("rightData", rightData);

		outBean.setData(resBean);
		writeFile(4,null,mytdeptCode,type,0,outBean);
		return outBean;
	}
	
	@SuppressWarnings("deprecation")
	public OutBean getData5(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");

		// 获取总行机构
		UserBean user = Context.getUserBean();
		String mytdeptCode = user.getTDeptCode();
		String cmpyCode = user.getCmpyCode();
		
		Bean fileData = getFile();
		if (fileData.getBean("data5").isNotEmpty(mytdeptCode)
				&& fileData.getBean("data5").getBean(mytdeptCode).isNotEmpty(type + "")) {
			outBean.setData(fileData.getBean("data5").getBean(mytdeptCode).getBean(type + "").get("_DATA_"));
			return outBean;
		}
		
		// 左侧数据
		ParamBean whereBean1 = new ParamBean();
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append("and NODE_ETIME >= '" + start + "' and NODE_ETIME <= '" + end + "'");
		// type 1年 2周 3月
		if (type == 1) {
			whereBean1.setSelect("substr(NODE_ETIME, 0, 7) as unit,count(*) as num");
			sbWhere.append(" group by substr(NODE_ETIME, 0, 7)");
		} else {
			whereBean1.setSelect("substr(NODE_ETIME, 0, 10) as unit,count(*) as num");
			sbWhere.append(" group by substr(NODE_ETIME, 0, 10)");
		}
		whereBean1.setWhere(sbWhere.toString());
		List<Bean> list = ServDao.finds("SY_WFE_TRACK", whereBean1);
		List<Integer> arrLeft = new ArrayList<Integer>();
		List<String> arrTmp = new ArrayList<String>();
		// 年
		if (type == 1) {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR) - 1;
			int month = cal.get(Calendar.MONTH) + 2;
			if (month > 12) {
				month = 1;
				year += 1;
			}
			for (int i = 0; i < 12; i++) {
				int amonth = month;
				String tmpMonth = year + "-" + (amonth < 10 ? ("0" + amonth) : amonth);
				arrTmp.add(tmpMonth);
				month += 1;
				if (month > 12) {
					month = 1;
					year += 1;
				}
			}

			for (int i = 0; i < arrTmp.size(); i++) {
				boolean addFlag = true;
				for (int j = 0; j < list.size(); j++) {
					String unit = list.get(j).getStr("UNIT");
					if (unit.equals(arrTmp.get(i))) {
						int num = list.get(j).getInt("NUM");
						arrLeft.add(num);
						addFlag = false;
						break;
					}
				}
				if (addFlag) {
					arrLeft.add(0);
				}
			}
		} else {
			// 周
			// 月
			// start
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date startDate = sdf.parse(start);
				Date endDate = sdf.parse(end);
				endDate.setDate(endDate.getDate() + 1);
				while (!DateUtils.isSameDay(startDate, endDate)) {
					String tmpDate = sdf.format(startDate);
					boolean addFlag = true;
					for (int i = 0; i < list.size(); i++) {
						String unit = list.get(i).getStr("UNIT");
						if (unit.equals(tmpDate)) {
							int num = list.get(i).getInt("NUM");
							arrLeft.add(num);
							addFlag = false;
							break;
						}
					}
					if (addFlag) {
						arrLeft.add(0);
					}
					startDate.setDate(startDate.getDate()+1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		Bean resBean = new Bean();
		resBean.set("leftData", arrLeft);

		// 右侧数据
		ParamBean whereBean3 = new ParamBean();
		whereBean3.setSelect("DISTINCT DONE_USER_ID");
		whereBean3.setWhere("and NODE_ETIME > '" + start + "' and NODE_ETIME < '" + end + "' and s_cmpy = '"+cmpyCode+"'");
		// 所有
		int total = ServDao.count("SY_WFE_NODE_INST_TJ_V", whereBean3);

		ParamBean whereBean4 = new ParamBean();
		whereBean4.setSelect("DISTINCT DONE_USER_ID");
		whereBean4.setWhere("and NODE_ETIME > '" + start + "' and NODE_ETIME < '" + end
				+ "' and odept_code in (select dept_code from sy_org_dept where dept_code = odept_code and dept_level < 3) and s_cmpy = '"+cmpyCode+"'");
		// 总/省 发起
		int topNum = ServDao.finds("SY_WFE_NODE_INST_TJ_V", whereBean4).size();
		//地市及以下发起
		int dishiNum = total - topNum;
		ParamBean whereBean5 = new ParamBean();
		whereBean5.setSelect("DISTINCT DONE_USER_ID");
		whereBean5.setWhere("and NODE_ETIME > '" + start + "' and NODE_ETIME < '" + end
				+ "' and tdept_code = '" + mytdeptCode + "' and s_cmpy = '"+cmpyCode+"'");
		// 本部门发起
		int myNum = ServDao.count("SY_WFE_NODE_INST_TJ_V", whereBean5);

		double percent1 = (double) (100 * topNum) / total;
		double percent2 = 100 - percent1;
		double percent3 = (double) (100 * myNum) / total;
		if(total == 0){
			percent1 = 0;
			percent2 = 0;
			percent3 = 0;
		}
		DecimalFormat df = new DecimalFormat("#0.00");

		String A2 = df.format(percent1) + "%";
		String B2 = df.format(percent2) + "%";
		String C2 = df.format(percent3) + "%";
		Bean rightData = new Bean();
		rightData.set("A1", topNum);
		rightData.set("A2", A2);
		rightData.set("B1", dishiNum);
		rightData.set("B2", B2);
		rightData.set("C1", myNum);
		rightData.set("C2", C2);
		resBean.set("rightData", rightData);

		outBean.setData(resBean);
		writeFile(5,null,mytdeptCode,type,0,outBean);
		return outBean;
	}
	
	/**
	 * 流程类型分布
	 * @param paramBean
	 * @return
	 */
	public OutBean getData6(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");
		//1.年 2.周 3.月
		int timeType = paramBean.getInt("timeType");
		UserBean user = Context.getUserBean();
		String odeptCode = user.getODeptCode();
		
		Bean fileData = getFile();
		if(type == 1){
			if (fileData.getBean("data6").isNotEmpty("all") && fileData.getBean("data6").getBean("all").isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data6").getBean("all").getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}else{
			if (fileData.getBean("data6").isNotEmpty(odeptCode) && fileData.getBean("data6").getBean(odeptCode).isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data6").getBean(odeptCode).getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}
		
		ParamBean whereBean = new ParamBean();
		if(type == 2){
			//本单位
			whereBean.setSelect("reg_proc_type_one,count(*) as num");
			whereBean.setWhere("and S_ODEPT = '"+odeptCode+"' and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"' group by reg_proc_type_one");
			whereBean.setOrder("count(*) desc");
		}else{
			//全系统
			whereBean.setSelect("reg_proc_type_one,count(*) as num");
			whereBean.setWhere("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"' group by reg_proc_type_one");
			whereBean.setOrder("count(*) desc");
		}
		
		List<Bean> list = ServDao.finds("FM_PROC_ONLINE_REG", whereBean);
		List<String> array0 = new ArrayList<String>();
		List<Bean> array1 = new ArrayList<Bean>();
		int otherNum = 0;
		for (int i = 0; i < list.size(); i++) {
			Bean bean = list.get(i);
			Bean tmpBean = new Bean();
			String tmpCode = bean.getStr("REG_PROC_TYPE_ONE");
			String tmpName = DictMgr.getName("SY_PROC_TYPE", tmpCode);
			tmpBean.set("name", tmpName);
			tmpBean.set("value", bean.getInt("NUM"));
			if(i > 5){
				otherNum += bean.getInt("NUM");
			}else{
				array0.add(tmpName);
				array1.add(tmpBean);
			}
		}
		if(list.size() > 5){
			Bean tmpBean = new Bean();
			tmpBean.set("name", "其他");
			tmpBean.set("value", otherNum);
			array0.add("其他");
			array1.add(tmpBean);
		}
		Bean resBean = new Bean();
		resBean.set("data0",array0);
		resBean.set("data1",array1);
		outBean.setData(resBean);
		writeFile(6,odeptCode,null,timeType,type,outBean);
		return outBean;
	}
	
	/**
	 * 流程使用时间热度分布
	 * @param paramBean
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public OutBean getData7(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");
		//1.年 2.周 3.月
		int timeType = paramBean.getInt("timeType");
		UserBean user = Context.getUserBean();
		String odeptCode = user.getODeptCode();
		
		Bean fileData = getFile();
		if(type == 1){
			if (fileData.getBean("data7").isNotEmpty("all") && fileData.getBean("data7").getBean("all").isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data7").getBean("all").getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}else{
			if (fileData.getBean("data7").isNotEmpty(odeptCode) && fileData.getBean("data7").getBean(odeptCode).isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data7").getBean(odeptCode).getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}
		
		ParamBean whereBean = new ParamBean();
		if(type == 2){
			//本单位
			whereBean.setSelect("S_ATIME");
			whereBean.setWhere("and S_ODEPT = '"+odeptCode+"' and S_ATIME > '"+start+"' and S_ATIME < '"+end+"'");
			whereBean.setOrder("S_ATIME");
		}else{
			//本系统
			whereBean.setSelect("S_ATIME");
			whereBean.setWhere("and S_ATIME > '"+start+"' and S_ATIME < '"+end+"'");
			whereBean.setOrder("S_ATIME");
		}
		List<Bean> list = ServDao.finds("FM_PROC_APPLY", whereBean);
		List<Bean> array = new ArrayList<Bean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (Bean bean : list) {
			Bean tmpBean = new Bean();
			int i = 0;
			int j = 0;
			String atime = bean.getStr("S_ATIME");
			if(atime.length() > 0){
				try {
					Date date = sdf.parse(atime);
					i = date.getDay();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				String time = atime.substring(11,13);
				j = Integer.valueOf(time)/2;
			}
			tmpBean.set("i", i);
			tmpBean.set("j", j);
			array.add(tmpBean);
		}
		outBean.setData(array);
		writeFile(7,odeptCode,null,timeType,type,outBean);
		return outBean;
	}
	/**
	 * 流程使用频率Top30 (次)
	 * @param paramBean
	 * @return
	 */
	public OutBean getData8(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		int type = paramBean.getInt("type");
		//1.年 2.周 3.月
		int timeType = paramBean.getInt("timeType");
		UserBean user = Context.getUserBean();
		String odeptCode = user.getODeptCode();
		
		Bean fileData = getFile();
		if(type == 1){
			if (fileData.getBean("data8").isNotEmpty("all") && fileData.getBean("data8").getBean("all").isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data8").getBean("all").getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}else{
			if (fileData.getBean("data8").isNotEmpty(odeptCode) && fileData.getBean("data8").getBean(odeptCode).isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data8").getBean(odeptCode).getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}
		
		ParamBean whereBean = new ParamBean();
		if(type == 2){
			//本单位
			whereBean.setSelect("apply_catalog,count(*) as num");
			whereBean.setWhere("and S_ODEPT = '"+odeptCode+"' and S_ATIME > '"+start+"' and S_ATIME < '"+end+"' group by apply_catalog");
			whereBean.setOrder("count(*) desc");
		}else{
			//本系统
			whereBean.setSelect("apply_catalog,count(*) as num");
			whereBean.setWhere("and S_ATIME > '"+start+"' and S_ATIME < '"+end+"' group by apply_catalog");
			whereBean.setOrder("count(*) desc");
		}
		List<Bean> list = ServDao.finds("FM_PROC_APPLY", whereBean);
		List<Bean> array = new ArrayList<Bean>();
	
		//取top30
		int size = list.size();
		if(size > 30){
			size = 30;
		}
		for (int i = 0; i < size; i++) {
			Bean bean = list.get(i);
			Bean tmpBean = new Bean();
			String tmpCode = bean.getStr("APPLY_CATALOG");
			String tmpName = DictMgr.getName("FM_PROC_ONLINE_REG", tmpCode);
			tmpBean.set("title", tmpName);
			tmpBean.set("count", bean.getInt("NUM"));
			array.add(tmpBean);
		}
		outBean.setData(array);
		writeFile(8,odeptCode,null,timeType,type,outBean);
		return outBean;
	}
	
	/**
	 * 流程起草次数排名
	 * @param paramBean
	 * @return
	 */
	public OutBean getData9(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		//1.系统 2.本单位
		int type = paramBean.getInt("type");
		//1.年 2.周 3.月
		int timeType = paramBean.getInt("timeType");
		UserBean user = Context.getUserBean();
		String odeptCode = user.getODeptCode();
		
		Bean fileData = getFile();
		if(type == 1){
			if (fileData.getBean("data9").isNotEmpty("all") && fileData.getBean("data9").getBean("all").isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data9").getBean("all").getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}else{
			if (fileData.getBean("data9").isNotEmpty(odeptCode) && fileData.getBean("data9").getBean(odeptCode).isNotEmpty(timeType + "")) {
				outBean.setData(fileData.getBean("data9").getBean(odeptCode).getBean(timeType + "").get("_DATA_"));
				return outBean;
			}
		}
		
		ParamBean whereBean = new ParamBean();
		if(type == 2){
			//本单位
			whereBean.setSelect("S_DEPT,count(*) as num");
			whereBean.setWhere("and S_ODEPT = '"+odeptCode+"' and S_ATIME > '"+start+"' and S_ATIME < '"+end+"' group by S_DEPT");
			whereBean.setOrder("count(*) desc");
		}else{
			//本系统
			whereBean.setSelect("s_odept,count(*) as num");
			whereBean.setWhere("and S_ATIME > '"+start+"' and S_ATIME < '"+end+"' group by s_odept");
			whereBean.setOrder("count(*) desc");
		}
		List<Bean> list = ServDao.finds("FM_PROC_APPLY", whereBean);
		List<Bean> array = new ArrayList<Bean>();
	
		for (Bean bean : list) {
			Bean tmpBean = new Bean();
			String tmpCode = (type == 2?bean.getStr("S_DEPT"):bean.getStr("S_ODEPT"));
			String tmpName = DictMgr.getName("SY_ORG_DEPT_ALL", tmpCode);
			tmpBean.set("title", tmpName);
			tmpBean.set("count", bean.getInt("NUM"));
			array.add(tmpBean);
		}
		outBean.setData(array);
		writeFile(9,odeptCode,null,timeType,type,outBean);
		return outBean;
	}
	/**
	 * 流程建设数量排名
	 * @param paramBean
	 * @return
	 */
	public OutBean getData10(ParamBean paramBean){
		OutBean outBean = new OutBean();
		String start = paramBean.getStr("start");
		String end = paramBean.getStr("end");
		
		// 1.年 2.周 3.月
		int type = paramBean.getInt("type");
		Bean fileData = getFile();
		if (fileData.getBean("data10").isNotEmpty(type + "")) {
			outBean.setData(fileData.getBean("data10").getBean(type + "").get("_DATA_"));
			return outBean;
		}
		
		ParamBean whereBean = new ParamBean();
		//全系统
		whereBean.setSelect("s_odept,count(*) as num");
		whereBean.setWhere("and REG_ONLINE_TIME	 >= '"+start+"' and REG_ONLINE_TIME <= '"+end+"' group by s_odept");
		whereBean.setOrder("count(*) desc");
		
		List<Bean> list = ServDao.finds("FM_PROC_ONLINE_REG", whereBean);
		List<Bean> array = new ArrayList<Bean>();
		for (Bean bean : list) {
			Bean tmpBean = new Bean();
			String odeptCode = bean.getStr("S_ODEPT");
			String odeptName = DictMgr.getName("SY_ORG_ODEPT_ALL", odeptCode);
			tmpBean.set("title", odeptName);
			tmpBean.set("count", bean.getInt("NUM"));
			array.add(tmpBean);
		}
		outBean.setData(array);
		writeFile(10,null,null,type,0,outBean);
		return outBean;
	}
}
