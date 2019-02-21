package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.*;
import com.rh.core.serv.bean.PageBean;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.sup.util.SupConstant;
import com.rh.sup.util.UrgeWord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SupApperUrge extends CommonServ {

	private static final String SUP_APPRO_OFFICE = "OA_SUP_APPRO_OFFICE";
	private static final String SUP_APPRO_URGE = "OA_SUP_APPRO_URGE";

	/**
	 * 获取构建信息
	 *
	 * @param paramBean
	 * @return
	 */
	
	public OutBean getMainValue(ParamBean paramBean) {
		String id = paramBean.getStr("ID");
		// 获取主单信息
		Bean appro = ServDao.find(SUP_APPRO_OFFICE, id);

		// 获取催办主键
		String urgeId = paramBean.getStr("URGE_ID");
		String urgeDeptCode = paramBean.getStr("urgeDeptCode");
		
		
		// 构建当前时间
		Date date = new Date();
		String newDateString = getYDM(date);

		// 构建返回值
		OutBean outBean = new OutBean();

		//回显统计事项来源
		outBean.set("STATIS_ITEM_SOURCE",
				DictMgr.getName("SUP_STATIS_ITEM_SOURCE", appro.getStr("STATIS_ITEM_SOURCE")));

		// 获取当前用户bean
		UserBean userBean = Context.getUserBean();
		// 主单实例
		
		Bean urge = ServDao.find(SUP_APPRO_URGE, urgeId);

		// 假如当前催办主单为空的话直接结算的逾期天数or 不为空 直接从表里面区数据
		if (urge == null) {
			String str = appro.getStr("LIMIT_DATE");
			outBean.set("appro", appro);
			String ovrdueDay = getOvrdueDay(str,newDateString);
			if (Integer.parseInt(ovrdueDay) < 0) {
				outBean.set("NOTICE_TYPE", "2");
				// 逾期天数
				outBean.set("overdueDay", getWorkDay(newDateString, str));

			}else {
				outBean.set("NOTICE_TYPE", "1");
				// 剩余天数
				outBean.set("overdueDay", getWorkDay(str, newDateString));
			}
			
			// 逾期时间
			outBean.set("limitDate", appro.getStr("LIMIT_DATE"));

			
			// 通知时间
			outBean.set("urgeTime", newDateString);

			// 构建条件bean
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("APPRO_ID", id);
			String deptcodes = paramBean.getStr("DEPTCODES");
			String[] split = deptcodes.split(",");
			// 构建页面显示返回值
			StringBuilder deptNames = new StringBuilder();
			for (String s : split) {
				String deptName = DictMgr.getName("SY_ORG_DEPT_ALL", s);
				deptNames.append("," + deptName);
			}
			deptNames.deleteCharAt(0);
			outBean.set("DEPT_NAME", deptNames);

		} else {
			
			// 逾期天数
			outBean.set("overdueDay", urge.getStr("OVERDUE_DAY"));
			// 逾期时间
			outBean.set("limitDate", urge.getStr("LIMIT_DATE"));
			// 主单实例
			outBean.set("appro", ServDao.find("OA_SUP_APPRO_OFFICE", urge.getStr("APPRO_ID")));
			// 通知时间
			outBean.set("urgeTime", urge.getStr("S_ATIME"));
			//通知类型
			outBean.set("NOTICE_TYPE",urge.getStr("NOTICE_TYPE"));
			//通知编号
			outBean.set("ITEM_NUM", urge.getStr("ITEM_NUM"));
			
			//获取当前节点
			String NID = paramBean.getStr("NID");
			
			// 判断当前用户角色如果是署领导则是看到全部部门 否则只能看到本部门
			if ("N2".equals(NID) || "N1".equals(NID)) {
				// 构建条件bean
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("URGE_ID", urgeId);
				// 查询得到结果
				List<Bean> depts = ServDao.finds("OA_SUP_APPRO_URGE_DEPT", sqlBean);

				// 构建页面显示返回值
				StringBuilder deptNames = new StringBuilder();
				for (Bean dept : depts) {
					String deptName = DictMgr.getName("SY_ORG_DEPT_ALL", dept.getStr("URGED_DEPT_CODE"));
					deptNames.append("," + deptName);
				}
				//删除第一个逗号
				deptNames.deleteCharAt(0);
				outBean.set("DEPT_NAME", deptNames);
				//判断节点是否为办公厅，如果为办公厅直接查询全部
			} else if ("N22".equals(NID)) {
				String tDeptName = userBean.getTDeptName();
				outBean.set("DEPT_NAME", tDeptName);
			}
		}
		//判断用户是否是从列表点击查看的如果是获取点击的那条的部门
		if(!"".equals(urgeDeptCode)){
			outBean.set("DEPT_NAME", DictMgr.getName("SY_ORG_DEPT_ALL", urgeDeptCode));
		}

		return outBean;
	}

	/**
	 * 添加催办信息部门
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean saveUrgedDepts(ParamBean paramBean) {
		// 获取用户选择的通知部门
		String deptCodes = paramBean.getStr("DeptCodes");
		String[] split = deptCodes.split(",");
		String urgeId = paramBean.getStr("URGE_ID");
		String approId = paramBean.getStr("APPRO_ID");

		// 构建添加bean集合
		List<Bean> beans = new ArrayList<>();
		
		//遍历集合
		for (String s : split) {
			//拼接sql
			StringBuffer sql = new StringBuffer("select * from SUP_APPRO_OFFICE_DEPT where DEPT_CODE = '").append(s)
					.append("' and OFFICE_ID = '").append(approId + "'");
			//执行sql得到结果
			Bean bean = Transaction.getExecutor().queryOne(sql.toString());
			
			//构建参数bean
			Bean saveBean = new ParamBean();
			saveBean.set("URGE_ID", urgeId);
			saveBean.set("URGED_DEPT_CODE", s);
			//往集合添加
			beans.add(saveBean);
		}
		//批量插入
		ServDao.creates("OA_SUP_APPRO_URGE_DEPT", beans);

		return null;
	}
	
	/**
	 * 催办选择司局
	 * @param paramBean
	 * @return
	 */
	public OutBean getCondition(ParamBean paramBean) {
		String id = paramBean.getStr("ID");

		SqlBean sqlBean = new SqlBean();
		sqlBean.and("OFFICE_ID", id);
		sqlBean.andIn("DEPT_TYPE", "1","2");
		List<Bean> deptList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
		StringBuilder sb = new StringBuilder();
		if (deptList != null && !deptList.isEmpty()) {
			for (int i = 0; i < deptList.size(); i++) {
				if (i == 0) {
					sb.append(" and DEPT_CODE = '").append(deptList.get(i).getStr("DEPT_CODE")).append("' ");
					continue;
				}
				sb.append(" or DEPT_CODE = '").append(deptList.get(i).getStr("DEPT_CODE")).append("' ");
			}

		}

		OutBean outBean = new OutBean();
		outBean.set("param", sb);
		return outBean;
	}
	
	/**
	 * 根据字符串时间获取yyyy年MM月DD日
	 *
	 * @param date
	 * @return
	 */
	public String getYDM(Date date) {
		SimpleDateFormat sdf = null;
		sdf = new SimpleDateFormat("yyyy");
		String y = sdf.format(date);
		sdf = new SimpleDateFormat("MM");
		String m = sdf.format(date);
		sdf = new SimpleDateFormat("dd");
		String d = sdf.format(date);

		String result = y + "-" + m + "-" + d;

		return result;
	}

	/**
	 * 根据两个时间相差几天
	 *
	 * @param newDate 完成时间or当前时间
	 * @param limitDate 完成时限
	 * @return
	 */
	private String getOvrdueDay(String newDate, String limitDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = null;
		Date parse2 = null;
		try {
			date1 = sdf.parse(newDate);
			parse2 = sdf.parse(limitDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		long l = (date1.getTime() - parse2.getTime()) / (1000 * 3600 * 24);

		return String.valueOf(l);
	}
	
	
	/**
	 * 两个时间内的工作日
	 * @param newDate 当前时间
	 * @param limitDate 完成时间
	 * @return
	 */
	public String getWorkDay(String newDate,String limitDate){
		
		//取出两个时间的天数
		String ovrdueDay = getOvrdueDay(newDate, limitDate);
		//OA_GW_GONGWEN_HOLIDAYS
		StringBuilder builder = new StringBuilder("select * from SY_COMM_WORK_DAY where DAY_FLAG = '2' and  DAY_SPECIAL_DATE  between to_date('")
				.append(limitDate)
				.append("','yyyy-mm-dd') and to_date('")
				.append(newDate)
				.append("','yyyy-mm-dd') ");
		int count = Transaction.getExecutor().count(builder.toString());
		//用两个日期之间的天数 减去节假日  得到工作日 
		int day = Integer.parseInt(ovrdueDay) - count;
		return ""+day;
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
	
	/**
	 * 判断是有过催办信息and直接取出数据
	 * @param paramBean
	 * @return
	 */
	public OutBean isListShow(ParamBean paramBean){
		//获取立项主键
		String approId = paramBean.getStr("approId");
		//获取查看类型 1全部 2自己当前用户司局
		String type = paramBean.getStr("type");
		
		OutBean outBean = new OutBean();
		
		
		//根据条件判断
		if("1".equals(type)){
			//设置条件
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects(" ID,S_ATIME,S_DEPT,URGED_DEPT_CODE,S_USER,PLAN_DEPT_ID");
			sqlBean.set("APPRO_ID", approId);
			//查询得到结果
			List<Bean> list = ServDao.finds(SupConstant.OA_SUP_APPRO_URGE, sqlBean);
			//判断计划是否为空
			if (list!=null && list.size()>0) {
				outBean.setData(list);
			}
		}else if("2".equals(type)){
			//获取当前用户值
			UserBean userBean = Context.getUserBean();
			//获取用户司局部门
			String tDeptCode = userBean.getTDeptCode();
			
			//设置条件
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects(" ID,S_ATIME,S_DEPT,URGED_DEPT_CODE,S_USER,PLAN_DEPT_ID,S_WF_STATE,S_WF_INST");
			sqlBean.set("APPRO_ID", approId);
			sqlBean.set("URGED_DEPT_CODE", tDeptCode);
			//根据条件查询
			List<Bean> list= ServDao.finds(SupConstant.OA_SUP_APPRO_URGE, sqlBean);
			
			//构建返回值bean
			List<Bean> result = new ArrayList<>();
			
			list.forEach(bean ->{
				//获取流程状态
				String sWfState= bean.getStr("S_WF_STATE");
				//判断当前流程是否结束 没结束的话需要去流程实例表中查询当用户的实例编码如果有就往集合添加
				if(!"1".equals(sWfState)){
					//集合添加
					result.add(bean);
				} else {
					//获取当前实例编码
					String sWfInst = bean.getStr("S_WF_INST");
					//创建条件sqlbean
					SqlBean wfSqlBean = new SqlBean();
					wfSqlBean.set("PI_ID", sWfInst);
					wfSqlBean.set("NODE_CODE", "N22");
					//判断是有值才是推送到司内督查员的
					int count=ServDao.count("SY_WFE_NODE_INST",wfSqlBean);
					if(count > 0){
						//集合添加
						result.add(bean);
					}
				}
			});
			//设置返回值
			outBean.setData(result);
		}
		//返回
		return outBean;
	}
	
	/**
	 * 重写查询方法
	 * @param paramBean
	 * @return
	 */
	@Override
	public OutBean query(ParamBean paramBean) {
//		//调用查询之前的所执行的方法
//        this.beforeQuery(paramBean);
		//获取主单信息
		String approId = paramBean.getStr("APPRO_ID");
		paramBean.set("approId", approId);
		//获取当前用户
		UserBean userBean = Context.getUserBean();
		//获取当前用户角色编码
		String roleCodeStr = userBean.getRoleCodeStr();
		
		//判断当前角色是否包含制定角色编码
		if(roleCodeStr.contains("SUP_DC_002") || roleCodeStr.contains("SUP_DC_001")){
			//如果包含查询全部的
			paramBean.set("type", "1");
		}else {
			//如果不包含就只查询司局的
			paramBean.set("type", "2");
		}
		
		  //调用查询之前的所执行的方法
        super.beforeQuery(paramBean);
        final ServDefBean serv = ServUtils.getServDef(paramBean.getServId());
        PageBean page = paramBean.getQueryPage();
        int rowCount = paramBean.getShowNum();
        //获取系统参数对分页条件进行设定
        if (rowCount > 0) {
            page.setShowNum(rowCount);
            page.setNowPage(paramBean.getNowPage());
        } else if (!page.contains("SHOWNUM")) {
            if (paramBean.getQueryNoPageFlag()) {
                page.setShowNum(0);
            } else {
                page.setShowNum(serv.getPageCount(50));
            }
        }
        OutBean outBean = new OutBean();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        LinkedHashMap<String, Bean> items = serv.getAllItems();
        StringBuilder select = new StringBuilder(serv.getPKey());
        final LinkedHashMap<String, Bean> cols = new LinkedHashMap();
        boolean bKey = true;
        String qSelect = paramBean.getSelect();
        boolean bListFlag = qSelect.equals("*");
        for (String key : items.keySet()) {
        	if(!"ID,S_ATIME,S_DEPT,URGED_DEPT_CODE,S_USER,PLAN_DEPT_ID".contains(key)){
        		continue;
        	}
        
            Bean item = items.get(key);
            int listFlag = item.getInt("ITEM_LIST_FLAG");
            
            if (bKey && item.getStr("ITEM_CODE").equals(serv.getPKey())) {
                if (listFlag == 3) {
                    listFlag = 2;
                }
                this.addCols(cols, item, listFlag);
                bKey = false;
            } else if (listFlag != 2 || bListFlag) {
                if (item.getInt("ITEM_TYPE") == 1 || item.getInt("ITEM_TYPE") == 2) {
                    select.append(",").append(item.get("ITEM_CODE"));
                }
                if (listFlag == 3) {
                    listFlag = 2;
                }

                this.addCols(cols, item, listFlag);
            }
            
            addCols(cols, item, listFlag);
        } 
        
        OutBean listShow = isListShow(paramBean);
        
        List<Bean> dataList = (List<Bean>) listShow.getData();
        int count = dataList.size();
        int showCount = page.getShowNum();
        boolean bCount;
        //根据参数对分页类条件进行设定
        if (showCount != 0 && !serv.noCount() && !paramBean.getQueryNoPageFlag()) {
            bCount = true;
        } else {
            bCount = false;
        }
        if (bCount) {
            if (!page.contains("ALLNUM")) {
                int allNum;
                if (page.getNowPage() == 1 && count < showCount) {
                    allNum = count;
                } else {
                    allNum = Transaction.getExecutor().count(sql.toString());
                }
                page.setAllNum((long)allNum);
            }
            outBean.setCount(page.getAllNum());
        } else {
            outBean.setCount((long)dataList.size());
        }
        outBean.set("code", 0);
        //将渲染所需数据存放到对象当中
        outBean.setData(dataList);
        outBean.setPage(page);
        outBean.setCols(cols);
        this.afterQuery(paramBean, outBean);
        return outBean;
	}
	
	/**
     * 重写方法对个别返回数据进行处理
     * @param paramBean
     * @param outBean
     */
    @Override
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        List<Bean> beanList = (List<Bean>) outBean.getData();
        beanList.forEach(bean ->{
        	bean.set("URGED_DEPT_CODE__NAME", DictMgr.getName("SY_ORG_DEPT_ALL", bean.getStr("URGED_DEPT_CODE")));
        	bean.set("S_DEPT__NAME", DictMgr.getName("SY_ORG_DEPT_ALL", bean.getStr("S_DEPT")));
        	bean.set("S_USER__NAME", DictMgr.getName("SY_ORG_USER", bean.getStr("S_USER")));
        });
        Collections.reverse(beanList);
        outBean.set(Constant.RTN_DATA,beanList);
    }
    
    public OutBean urgeWord(ParamBean paramBean){
    	
    	String id = paramBean.getStr("APPRO_ID");
		// 获取主单信息
		Bean appro = ServDao.find(SUP_APPRO_OFFICE, id);

		// 获取催办主键
		String urgeId = paramBean.getStr("ID");
		String urgeDeptCode = paramBean.getStr("URGE_DEPT_CODE");
		
		// 构建当前时间
		Date date = new Date();
		String newDateString = getYDM(date);

		// 获取当前用户bean
		UserBean userBean = Context.getUserBean();
		
		Bean urge = ServDao.find(SUP_APPRO_URGE, urgeId);
				
		Bean bean = new Bean();
		//设定参数
		bean.set("STATIS_ITEM_SOURCE", DictMgr.getName("SUP_STATIS_ITEM_SOURCE", appro.getStr("STATIS_ITEM_SOURCE")));
		bean.set("SUPERV_ITEM", appro.getStr("SUPERV_ITEM"));
		bean.set("ITEM_NUM", urge.getStr("ITEM_NUM"));
		
	
	
		bean.set("S_ATIMT",DateUtils.getChineseTwoDate(urge.getStr("S_ATIME")));
		
		String LIMIT_DATE = urge.getStr("LIMIT_DATE");
		bean.set("LIMIT_DATE", DateUtils.getChineseTwoDate(LIMIT_DATE));
		// 逾期天数
		bean.set("OVERDUE_DAY", urge.getStr("OVERDUE_DAY"));
		bean.set("USER_NAME", appro.getStr("OFFICE_OVERSEER"));			
		bean.set("USER_TEL", appro.getStr("OFFICE_OVERSEER_TEL"));
		bean.set("DEPT_NAME", DictMgr.getName("SY_ORG_DEPT_ALL", urgeDeptCode));
	
    	UrgeWord.createWord(bean,urge.getStr("ITEM_NUM"),urge.getStr("NOTICE_TYPE"));
    	
    	return new OutBean().setOk();
    	
    }
    
    
    
}
