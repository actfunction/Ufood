package com.rh.sup.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 发起督查
 * @author yuzexing
 */
public class SupApproInitiateServ extends CommonServ {
	// 发起督查
	public OutBean saveInitiateSupervision(ParamBean paramBean) {
		Bean userBean = Context.getUserBean();
		String user_code = userBean.getStr("USER_CODE");
		String sql1 = "SELECT T2.DEPT_NAME, T1.USER_NAME FROM SY_ORG_USER T1,SY_ORG_DEPT T2 WHERE USER_CODE = '"+user_code+"' AND T1.DEPT_CODE=T2.DEPT_CODE";
		List<Bean> names = Transaction.getExecutor().query(sql1);
		String name = "";
		if(names!=null && names.size()>0){
			name = names.get(0).getStr("USER_NAME");
		}
		OutBean outBean = new OutBean();
		try{
		String str = paramBean.getStr("HOST_TDEPT_NAME");
		String str2 = paramBean.getStr("OTHER_TDEPT_NAME");
		// 插入SUP_APPRO_INITIATE_GW单挑数据
		 paramBean.setServId("OA_SUP_APPRO_INITIATE_GW");
		 paramBean.set("INITIATE_LEADER_NAME", name);
		 paramBean.set("HOST_TDEPT_NAME",str);
		 paramBean.set("OTHER_TDEPT_NAME",str2);
		 save(paramBean);
		// 查出督察处所有角色
		 String sql ="select distinct t2.USER_CODE,t2.DEPT_CODE from SY_ORG_ROLE_USER t1,sy_org_user t2  where t1.user_code=t2.user_code  and t1.role_code IN ('SUP_DC_002','SUP_DC_001')";
		List<Bean> beans = Transaction.getExecutor().query(sql);
		
		// 插入到SY_COMM_TODO
		if (beans.size() > 0) {
			for (Bean orgBean : beans) {
		            //给省厅负责人插入一条代办
				paramBean.set("TODO_TITLE", name+"领导关于"+paramBean.getStr("GW_TITLE")+"提出的督查意见");
				paramBean.set("TODO_OPERATION", "提出办理意见");
				paramBean.set("SEND_USER_CODE", user_code);
				paramBean.set("TODO_CODE", "OA_SUP_APPRO_INITIATE_GW");
				paramBean.set("S_EMERGENCY", 1);
				paramBean.set("OWNER_CODE", orgBean.getStr("USER_CODE"));
				paramBean.set("TODO_CONTENT", paramBean.getStr("MIND_CONTENT"));
				paramBean.set("TODO_BENCH_FLAG", 1);
				paramBean.set("TODO_CATALOG", 1);
				paramBean.set("SERV_ID", "OA_SUP_APPRO_INITIATE_GW");
				paramBean.setServId("SY_COMM_TODO");
				paramBean.set("TODO_FROM", "wf");
				paramBean.set("TODO_CODE_NAME", "督查办理意见");
				paramBean.set("TODO_URL", "OA_SUP_APPRO_INITIATE_GW.byid.do?data={_PK_:" + paramBean.getStr("ID")+"}");
				paramBean.set("TODO_OBJECT_ID1", paramBean.getStr("ID"));
				save(paramBean);
				paramBean.set("TODO_ID", "");
			}}
			
		} catch (Exception e) {
	            throw new RuntimeException("插入待办异常");
	        }
		return outBean;
	}
	/**
	 * 从拟立项表查询信息
	 * @author kfzx-zhangsy
	 * @param paramBean
	 * @return
	 */
	public Bean queryGwInfo(ParamBean paramBean){
		String id = paramBean.getStr("ID");
		String sql = "SELECT * FROM SUP_APPRO_INITIATE_GW WHERE ID = '"+id+"'";
		List<Bean> list = Transaction.getExecutor().query(sql);
		Bean bean = list.get(0);
		return bean;
	}
	/**
	 * 修改拟立项表s_wf_state
	 * @author kfzx-zhangsy
	 * @param paramBean
	 * @return
	 */
	public Bean updateGwInfo(ParamBean paramBean){
		String id = paramBean.getStr("ID");
		String sql = "UPDATE SUP_APPRO_INITIATE_GW SET S_WF_STATE = 1 WHERE ID = '"+id+"'";
		Transaction.getExecutor().execute(sql);
		return new OutBean().setOk();
	}
}
