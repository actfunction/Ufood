package com.rh.fm;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

public class ApplyServ extends CommonServ  {
	private static final String SERV_ID1 = "FM_PROC_COMM";
	private static final String SERV_ID2 = "FM_PROC_DATA_TJ";
	
	
	/**
     * 查询之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeQuery(ParamBean paramBean) {
    	String servId = paramBean.getServId();
    	String extWhere = " and APPLY_CATALOG = '"+servId+"'";
    	paramBean.setQueryExtWhere(extWhere);
    }
	    
    /**
     * 保存之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     *      可以通过paramBean获取数据库中的原始数据信息：
     *          Bean oldBean = paramBean.getSaveOldData();
     *      可以通过方法paramBean.getFullData()获取数据库原始数据加上修改数据的完整的数据信息：
     *          Bean fullBean = paramBean.getSaveFullData();
     *      可以通过paramBean.getAddFlag()是否为true判断是否为添加模式
     * @param outBean 输出信息
     *      可以通过outBean.getSaveIds()获取实际插入的数据主键
     */
    public void afterSave(ParamBean paramBean, OutBean outBean) {
    	if(paramBean.getAddFlag()){
    		UserBean userBean = Context.getUserBean();
    		String userCode = userBean.getCode();
    		String odeptCode = userBean.getODeptCode();
    		String catalog = paramBean.getStr("APPLY_CATALOG");
    		ParamBean whereBean1 = new ParamBean();
    		whereBean1.set("USER_CODE", userCode);
    		whereBean1.set("PROC_CODE", catalog);
    		//常用流程
    		Bean resBean1 = ServDao.find(SERV_ID1, whereBean1);
    		if(resBean1 != null){
    			int num = resBean1.getInt("PROC_NUM");
    			resBean1.set("PROC_NUM", num + 1);
    			ServDao.save(SERV_ID1, resBean1);
    		}else{
    			Bean dataBean = new Bean();
    			dataBean.set("USER_CODE", userCode);
    			dataBean.set("PROC_CODE", catalog);
    			dataBean.set("PROC_NUM", 1);
    			ServDao.save(SERV_ID1, dataBean);
    		}
    		
    		ParamBean whereBean2 = new ParamBean();
    		whereBean2.set("ODEPT_CODE", odeptCode);
    		whereBean2.set("SERV_ID", catalog);
    		Bean resBean2 = ServDao.find(SERV_ID2, whereBean2);
    		if(resBean2 != null){
    			int num = resBean2.getInt("USE_NUMS");
    			resBean2.set("USE_NUMS", num + 1);
    			ServDao.save(SERV_ID2, resBean2);
    		}else{
    			Bean dataBean = new Bean();
    			dataBean.set("ODEPT_CODE", odeptCode);
    			dataBean.set("SERV_ID", catalog);
    			dataBean.set("USE_NUMS", 1);
    			ServDao.save(SERV_ID2, dataBean);
    		}
    	}
    }
    
    
    /**
     * 办结流程之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    public void afterFinish(ParamBean paramBean) {
    	String PI_ID = paramBean.getStr("PI_ID");
    	ParamBean sqlBean = new ParamBean();
    	sqlBean.set("_SELECT_", "sum(NODE_DAYS) as NODE_DAYS" );
    	sqlBean.set("_WHERE_", " and PI_ID = '"+PI_ID+"'" );
		Bean result = ServDao.finds("SY_WFE_NODE_INST_HIS", sqlBean).get(0);
    	
    	List<Bean> list = ServDao.finds("FM_PROC_APPLY", "and S_WF_INST = '"+PI_ID+"'");
		String odeptCode = list.get(0).getStr("S_ODEPT");
		String catalog = paramBean.getStr("serv");
		ParamBean whereBean = new ParamBean();
		whereBean.set("ODEPT_CODE", odeptCode);
		whereBean.set("SERV_ID", catalog);
		
		Bean resBean = ServDao.finds(SERV_ID2, whereBean).get(0);
		int num = resBean.getInt("OVER_NUMS");
		int time = resBean.getInt("TOTAL_TIME");
		
		int a = time + result.getInt("NODE_DAYS");
		int b = num + 1;
		
		resBean.set("TOTAL_TIME", a);
		resBean.set("OVER_NUMS", b);
		
		int avg = a/b;
		resBean.set("AVG_TIME", avg);
		ServDao.save(SERV_ID2, resBean);
		
    }
}
