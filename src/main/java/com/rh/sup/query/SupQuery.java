package com.rh.sup.query;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
/**
 * 新查询列表后台统一方法
 * @author kfzx-zhangsy
 *
 */
public class SupQuery extends CommonServ {
	public OutBean SupDaibanQuery(ParamBean paramBean){
		//查询待办
		/**
		 * http://127.0.0.1:8082/sy/base/view/stdCardView.jsp?frameId=OA_GW_GONGWEN_ICBCSW-card-dopkCode3m7pezMpe8lczGkwnhMpehivI-
		 * 3m7pezMpe8lczGkwnhMpehivI-tabFrame&sId=OA_GW_GONGWEN_ICBCSW&areaId=&paramsFlag=true&title=23&pkCode=
		 * 3m7pezMpe8lczGkwnhMpehivI&replaceUrl=OA_GW_GONGWEN_ICBCSW.byid.do%3Fdata%3D%7B_PK_%3A3m7pezMpe8lczGkwnhMpehivI%2CNI_ID%3A05ZFgiOFNczpBkaqP77WqS%7D

		 */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT * FROM SY_COMM_TODO WHERE SERV_ID LIKE 'OA_SUP_APPRO%'");
		List<Bean> beanList = Transaction.getExecutor().query(stringBuilder.toString());
		List<Bean> newList = new ArrayList<Bean>();
		for(Bean bean : beanList){
			String servCode = bean.getStr("SERV_ID");//获取服务编码
			String servId = bean.getStr("TODO_OBJECT_ID1");
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("ID", servId);
			String ser = servCode.substring(3, servCode.length());
			Bean dateBean = ServDao.find(servCode, sqlBean);
			
			dateBean.set("TODO_TITLE", bean.getStr("TODO_TITLE"));//获取统一标题
			dateBean.set("SERV_ID", bean.getStr("SERV_ID"));//获取统一SERV_ID
			dateBean.set("TODO_OBJECT_ID1", bean.getStr("TODO_OBJECT_ID1"));//获取统一主键
			dateBean.set("TODO_URL", bean.getStr("TODO_URL"));//获取URL
			newList.add(dateBean);
			
		}
		
		OutBean outBean = new OutBean();
		outBean.set("_DATA_", newList);
		
		return outBean;
	}
}
