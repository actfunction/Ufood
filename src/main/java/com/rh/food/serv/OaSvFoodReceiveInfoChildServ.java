package com.rh.food.serv;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
/**
 * 
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhangheng1
 * @date: 2018年11月15日 下午3:15:04
 * @version: V1.0
 * @description: OA_SV_FOOD_RECEIVE_INFO_CHILD对应的服务类，主要处理重写了afterQuery和afterByid
 */
public class OaSvFoodReceiveInfoChildServ extends CommonServ {

	  /**
     * 
     */
	@Override
	public void beforeSave(ParamBean paramBean) {
		String OBTAIN_START_DATETIME=paramBean.getStr("OBTAIN_START_DATETIME");
		String OBTAIN_END_DATETIME=paramBean.getStr("OBTAIN_END_DATETIME");
		String[] START_DATE_TIME = OBTAIN_START_DATETIME.split(" ");
		
		String[] END_DATE_TIME = OBTAIN_END_DATETIME.split(" ");
		if(START_DATE_TIME.length==2) {
			paramBean.set("OBTAIN_START_DATE", START_DATE_TIME[0]);
			paramBean.set("OBTAIN_END_DATE", START_DATE_TIME[1]);
		}
		if(END_DATE_TIME.length==2) {
			paramBean.set("OBTAIN_END_DATE", END_DATE_TIME[0]);
			paramBean.set("OBTAIN_END_TIME", END_DATE_TIME[1]);
		}
	}
	
	/**
	 * 重写afterQuery方法
	 * 处理领取开始时间和领取结束时间的格式
	 * 用于页面自定义字段的显示
	 */
	@Override
	public void afterQuery(ParamBean paramBean, OutBean outBean) {		
		List<Bean> dataList = outBean.getDataList();
		if (dataList != null && dataList.size() > 0) {
			for (Bean bean : dataList) {
				String OBTAIN_START_DATE =bean.getStr("OBTAIN_START_DATE");
				String OBTAIN_START_TIME=bean.getStr("OBTAIN_START_TIME");
				bean.put("OBTAIN_START_DATETIME", OBTAIN_START_DATE+" "+OBTAIN_START_TIME);
				String OBTAIN_END_DATE =bean.getStr("OBTAIN_END_DATE");
				String OBTAIN_END_TIME=bean.getStr("OBTAIN_END_TIME");
				bean.put("OBTAIN_END_DATETIME", OBTAIN_END_DATE+" "+OBTAIN_END_TIME);
			}
		}  
	}
  
	/**
	 * 重写afterByid方法
	 * 展示卡片中领取开始时间和领取结束时间
	 * 用于页面自定义字段的显示
	 */
	@Override
	public void afterByid(ParamBean paramBean, OutBean outBean) {
		String OBTAIN_START_DATE =outBean.getStr("OBTAIN_START_DATE");
		String OBTAIN_START_TIME=outBean.getStr("OBTAIN_START_TIME");
		outBean.put("OBTAIN_START_DATETIME", OBTAIN_START_DATE+" "+OBTAIN_START_TIME);
		String OBTAIN_END_DATE =outBean.getStr("OBTAIN_END_DATE");
		String OBTAIN_END_TIME=outBean.getStr("OBTAIN_END_TIME");
		outBean.put("OBTAIN_END_DATETIME", OBTAIN_END_DATE+" "+OBTAIN_END_TIME);
	}
	

}
