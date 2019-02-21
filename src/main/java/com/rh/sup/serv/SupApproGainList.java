package com.rh.sup.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.tongtech.backport.java.util.Arrays;

public class SupApproGainList extends CommonServ {


    private final static String GAINSTATE = "1";
    private final static String SUP_APPRO_OFFICE = "OA_SUP_APPRO_OFFICE";
    private final static String SUP_APPRO_BUREAU = "OA_SUP_APPRO_BUREAU";
    private final static String SUP_APPRO_POINT = "OA_SUP_APPRO_POINT";
    private final static String SUP_APPRO_GAIN = "OA_SUP_APPRO_GAIN";
 
    /**
     * 根据立项主键获取上月办理情况
     *
     * @param paramBean
     * @return
     */
    public OutBean getAlike(ParamBean paramBean) {

        //构建返回值bena
        OutBean outBean = new OutBean();
        outBean.set("code", "404");

        //获取立项单主键
        String appro_id = paramBean.getStr("APPRO_ID");

        //获取查询类型（署发，司内，要点类）；
        String servId = paramBean.getStr("servId");

        //获取当前用户信息
        UserBean userBean = Context.getUserBean();
        //获取用户部门编码
        String deptCode = userBean.getDeptCode();
        //获取用的父级部门code
        String parent = userBean.getTDeptCode();


        //根据类型查询不同sql得到不同结果
        //构建sql语句
        StringBuffer sql = new StringBuffer();
        //署发立项
        if (SUP_APPRO_OFFICE.equals(servId)) {
            sql.append("select * from SUP_APPRO_GAIN WHERE APPRO_ID = '")
                    .append(appro_id)
                    .append("' and DEPT_CODE = '")
                    .append(parent);
            //司内
        } else if (SUP_APPRO_BUREAU.equals(servId)) {
            sql.append("select * from SUP_APPRO_GAIN WHERE APPRO_ID = '")
                    .append(appro_id)
                    .append("' and DEPT_CODE = '")
                    .append(deptCode);
            //要点类
        } else if (SUP_APPRO_POINT.equals(servId)) {
            sql.append("select * from SUP_APPRO_GAIN where APPRO_ID = '")
                    .append(appro_id);
        }
        sql.append("' and GAIN_STATE = '3' order by  TO_DATE(GAIN_MONTH,'yyyy-MM' ) desc");


        //执行sql语句得到结果
        List<Bean> result = Transaction.getExecutor().query(sql.toString());

        //3.判断结果是否为空
        if (result != null && result.size() > 0) {
            outBean.set("code", "200");
            outBean.set("data", result.get(0));
        }
        //4.返回结果
        return outBean;
    }

    /**
     * 保存后执行的方法
     *
     * @param paramBean
     * @return
     */
    public OutBean updateGain(ParamBean paramBean) {
        //获取办理情况主键id
        String id = paramBean.getStr("ID");

        //获取查询类型
        String servId = paramBean.getStr("servId");

        //根据主键获取到bean
        Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, id);

        //获取用户信息
        UserBean userBean = Context.getUserBean();

        supApproGain.set("GAIN_STATE", 1);
        supApproGain.set("GAIN_MONTH",paramBean.getStr("GAIN_MONTH"));
        supApproGain.set("GAIN_GRADE_MONTH", paramBean.getStr("GAIN_GRADE_MONTH"));

        //更新主办机构
        supApproGain = updateDepeCode(servId, supApproGain, userBean);


        //执行更新操作
        ServDao.update(SUP_APPRO_GAIN, supApproGain);

        return new OutBean();


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
        //获取当前用户部门信息
        String codePath = userBean.getCodePath();
        //根据当前主单类型查询不同的子服务
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

        //构建条件sqlBean
        SqlBean sqlBean = new SqlBean();
        

        //构建返回值对象
        List<Bean> result = new ArrayList<>();

        //署发
        if (SUP_APPRO_OFFICE.equals(servId)) {
        	sqlBean.and("OFFICE_ID", approId);
            result = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
            
            //司内
        } else if (SUP_APPRO_OFFICE.equals(servId)) {
        	sqlBean.and("BUREAU_ID", approId);
        	result = ServDao.finds("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
            //要点
        } else if (SUP_APPRO_POINT.equals(servId)) {
            SqlBean sqlBean2 = new SqlBean();
            sqlBean2.selects("DEPT_CODE");
            sqlBean2.and("ID", approId);
            result = ServDao.finds(SUP_APPRO_POINT, sqlBean2);
        }
        return result;
    }

    /**
     * 获取联系人信息
     *
     * @param paramBean
     * @return
     */
    public OutBean getLinkMan(ParamBean paramBean) {
    	// 获取条件值
		String approId = paramBean.getStr("APPRO_ID");
		String servId = paramBean.getStr("servId");

		// 构建条件sqlBean
		SqlBean sqlBean = new SqlBean();
		// 设置查询列条件
		sqlBean.selects("S_USER");
		// 设置查询条件
		sqlBean.and("ID", approId);
		// 执行得到结果
		Bean bean = ServDao.find(servId, sqlBean);

		// 根据得到用户code去查询用户信息
		SqlBean userSqlBean = new SqlBean();
		userSqlBean.selects("USER_NAME,USER_OFFICE_PHONE");
		userSqlBean.and("USER_CODE", bean.getStr("S_USER"));
		// 执行条件得到结果
		Bean userResult = ServDao.find("SY_ORG_USER_ALL", userSqlBean);
		String userName = userResult.getStr("USER_NAME");
		String phone = userResult.getStr("USER_OFFICE_PHONE");

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
			} else if (SUP_APPRO_OFFICE.equals(servId)) {
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

		Bean appro = ServDao.find(servId, approId);
		String supervItem = appro.getStr("SUPERV_ITEM");

		if (SUP_APPRO_POINT.equals(servId)) {
			supervItem = appro.getStr("TITLE");
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
     * 添加主办单位主要负责同志审核情况 信息
     *
     * @param paramBean
     * @return
     */
    public OutBean updateHostGainCase(ParamBean paramBean) {
        //获取条件信息
        String gainId = paramBean.getStr("ID");
        UserBean userBean = Context.getUserBean();

        Bean supApproGain = ServDao.find(SUP_APPRO_GAIN, gainId);
        supApproGain.set("HOST_GAIN_CASE", userBean.getCode());
        ServDao.update("SUP_APPRO_GAIN", supApproGain);
        return new OutBean();
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

        //获取查询类
        String servId = paramBean.getStr("servId");

        //判断是否为全部查询
        String isALL = paramBean.getStr("isALL");

        //构建集合接受返回值
        List<OutBean> allGain = new ArrayList<>();

        //获取立项单主键
        String approId = paramBean.getStr("APPRO_ID");
        //根据条件来判断是查询全部和单个部门下面的
        if (isALL.equals("1")) {

            //获取当前用户bean
            UserBean userBean = Context.getUserBean();
            //获取用户部门
            String deptCode = userBean.getDeptCode();

            //署发立项时当前用户的上级节点
            if (servId.equals(SUP_APPRO_OFFICE )) {
                deptCode = userBean.getTDeptCode();
            }
            //根据部门查询
            allGain = findConditionGain(approId, deptCode);
        } else if (isALL.equals("2")) {
            //查询全部
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

        //构建sql语句
        StringBuilder stringBuilder = new StringBuilder("select * from SUP_APPRO_GAIN ")
                .append("where APPRO_ID = '" + approId)
                .append("' and GAIN_STATE in(2,3)");
        //查询得到结果
        List<Bean> sqlResult = Transaction.getExecutor().query(stringBuilder.toString());


        //构建返回值bean
        List<OutBean> result = new ArrayList<>();

        //遍历日期
        for (String datum : data) {
            //构建存入月份和表格值的outBean
            OutBean outBean = new OutBean();
            //设置k为月份
            outBean.set("key", datum);
            List<Bean> list = new ArrayList<>();
            //根据日期来取出对应的值
            for (Bean bean : sqlResult) {
                if (bean.getStr("GAIN_MONTH").equals(datum)) {
                    bean.set("deptName",DictMgr.getName("SY_ORG_DEPT_ALL", bean.getStr("DEPT_CODE")));
                    bean.set("userName",DictMgr.getName("SY_ORG_USER", bean.getStr("S_USER")));
                    bean.set("gainName", DictMgr.getName("SUP_APPRO_GAIN_STATE", bean.getStr("GAIN_STATE")));

                    list.add(bean);
                }
            }
            //设置value 为表格值
            outBean.set("value", list);
            //返回值集合添加
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

        //构建sql语句
        StringBuilder stringBuilder = new StringBuilder("select * from SUP_APPRO_GAIN ")
                .append("where APPRO_ID = '" + approId)
                .append("' and DEPT_CODE = '" + deptCode)
                .append("' and GAIN_STATE in (2,3) ");
        //查询得到结果
        List<Bean> sqlResult = Transaction.getExecutor().query(stringBuilder.toString());


        //构建返回值bean
        List<OutBean> result = new ArrayList<>();

        //遍历日期
        for (String datum : data) {
            //构建存入月份和表格值的outBean
            OutBean outBean = new OutBean();
            //设置k为月份
            outBean.set("key", datum);
            List<Bean> list = new ArrayList<>();
            //根据日期来取出对应的值
            for (Bean bean : sqlResult) {
                if (bean.getStr("GAIN_MONTH").equals(datum)) {
                    bean.set("deptName",DictMgr.getName("SY_ORG_DEPT_ALL", bean.getStr("DEPT_CODE")));
                    bean.set("userName",DictMgr.getName("SY_ORG_USER", bean.getStr("S_USER")));
                    bean.set("gainName", DictMgr.getName("SUP_APPRO_GAIN_STATE", bean.getStr("GAIN_STATE")));

                    list.add(bean);
                }
            }
            //设置value 为表格值
            outBean.set("value", list);
            //返回值集合添加
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
        //构建sql语句
        StringBuilder stringBuilder = new StringBuilder("select distinct GAIN_MONTH from SUP_APPRO_GAIN ")
                .append("where APPRO_ID =  '" + approId)
                .append("' and DEPT_CODE = '" + deptCode)
                .append("' and GAIN_STATE in(2,3) order by GAIN_MONTH desc");
        //查询得到结果
        List<Bean> query = Transaction.getExecutor().query(stringBuilder.toString());

        //构建返回值集合
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

        //构建sql语句
        StringBuilder stringBuilder = new StringBuilder("select distinct GAIN_MONTH from SUP_APPRO_GAIN ")
                .append("where APPRO_ID = '" + approId)
                .append("' and GAIN_STATE in (2,3) order by GAIN_MONTH desc");
             
        //查询得到结果
        List<Bean> query = Transaction.getExecutor().query(stringBuilder.toString());

        //构建返回值集合
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
            sqlBean.and("PART_TYPE", "1");
            Bean host = ServDao.find("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
            if (host != null) {
                result = "2";
            }

        } else if (SUP_APPRO_POINT.equals(servId)) {
            result = "2";
        }

        return new OutBean().set("result", result);
    }

    //根据
    private Bean getDeptBean(String approId){


        return null;
    }


    /**
     * 获取司内督查员填写办理情况的key
     */
    public OutBean getInspectorUpdateGainKey(ParamBean paramBean) {

        String servId = paramBean.getStr("servId");

        //获取立项主单
        String approId = paramBean.getStr("APPRO_ID");

        //根据userBean获取当前用户的上级
        UserBean userBean = Context.getUserBean();

        String result = "";

        if (SUP_APPRO_OFFICE.equals(servId)) {
            SqlBean sqlBean = new SqlBean();
            sqlBean.selects("GAIN_ID");
            sqlBean.and("APPRO_ID", approId);
            sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
            sqlBean.and("GAIN_STATE", "1");
            Bean sup_appro_gain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
            result = sup_appro_gain.getStr("GAIN_ID");
        } else if (SUP_APPRO_BUREAU.equals(servId)) {
            SqlBean sqlBean = new SqlBean();
            sqlBean.selects("GAIN_ID");
            sqlBean.and("APPRO_ID", approId);
            sqlBean.and("DEPT_CODE", userBean.getDeptCode());
            sqlBean.and("GAIN_STATE", "1");
            Bean sup_appro_gain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
            result = sup_appro_gain.getStr("GAIN_ID");

        } else if (SUP_APPRO_POINT.equals(servId)) {
            SqlBean sqlBean = new SqlBean();
            sqlBean.selects("GAIN_ID");
            sqlBean.and("APPRO_ID", approId);
            sqlBean.and("GAIN_STATE", "1");
            Bean sup_appro_gain = ServDao.find(SUP_APPRO_GAIN, sqlBean);
            result = sup_appro_gain.getStr("GAIN_ID");
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

        //获取查询类型
        String servId = paramBean.getStr("servId");

        //获取立项单主键
        String approId = paramBean.getStr("APPRO_ID");

        //创建sql条件
        SqlBean sqlBean = new SqlBean();
        sqlBean.selects("FINISH_TIME,DEALT_TIME,OVERDUE_DAY");

        //构建返回值
        Bean bean = new OutBean();

        //根据条件判断
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
        //获取查询类型
        String servId = paramBean.getStr("servId");

        //获取立项单主键
        String approId = paramBean.getStr("APPRO_ID");

        //获取更新的时间
        String dealtTime = paramBean.getStr("dealtTime");

        //根据条件判断
        if (SUP_APPRO_OFFICE.equals(servId)) {
            Bean bean = ServDao.find(SUP_APPRO_OFFICE, approId);
            bean.set("DEALT_TIME", dealtTime);
            ServDao.update(SUP_APPRO_OFFICE, bean);

        } else if (SUP_APPRO_BUREAU.equals(servId)) {
            Bean bean = ServDao.find(SUP_APPRO_BUREAU, approId);
            bean.set("DEALT_TIME", dealtTime);
            ServDao.update(SUP_APPRO_BUREAU, bean);

        } else if (SUP_APPRO_POINT.equals(servId)) {
            Bean bean = ServDao.find(SUP_APPRO_POINT, approId);
            bean.set("DEALT_TIME", dealtTime);
            ServDao.update(SUP_APPRO_POINT, bean);

        }
        return new OutBean();
    }
    
   /** 
    *   流程中更新办理状态
    * @return
    */
   public void updateWfState(ParamBean paramBean){
       String approId = paramBean.getStr("approId");// 立项单主键
       String curState = paramBean.getStr("curState");//当前办理状态
       String upState = paramBean.getStr("upState");// 更新后的办理状态
       String deptCode = paramBean.getStr("deptCode");// 办理机构
       SqlBean sql = new SqlBean();
       sql.and("APPRO_ID",approId);
       sql.and("GAIN_STATE",curState);
       if (!deptCode.equals("")){
           sql.and("DEPT_CODE",deptCode);
       }
       Bean gain = ServDao.find(SUP_APPRO_GAIN,sql);
       if(gain!=null){
           gain.set("GAIN_STATE",upState);
           ServDao.update(SUP_APPRO_GAIN,gain);
       }
   }
}