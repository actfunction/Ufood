package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

import com.rh.sup.util.SUUtils;

import java.util.List;

/**
 * 司内督查办理扩展类
 * @author zhaosheng
 *
 */
public class BureauGainWfBinder implements ExtendBinder{
	private static String BUREAU_STAFF = "OA_SUP_APPRO_BUREAU_HOST";
	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {

		ExtendBinderResult result = new ExtendBinderResult();
		//1.取得流程实例对象
		WfProcess process =  currentWfAct.getProcess();
		Bean nodeBean = currentWfAct.getNodeInstBean();
		Bean servBean = process.getServInstBean();
		
		//获取服务的ID
		String servId = servBean.getId();
		//获取前一节点ID
		String preNiId = nodeBean.getStr("PRE_NI_ID");
		//获取前一节点实例(NI_ID:节点实例主键)
		Bean preNodeBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", preNiId));
		
		if("N1".equals(nodeBean.getStr("NODE_CODE")) || "N3".equals(nodeBean.getStr("NODE_CODE")) ){
			SqlBean sqlBean = new SqlBean();
			String [] deptType = {"1","2"};
			sqlBean.and("BUREAU_ID", servId);
			sqlBean.and("S_FLAG", Constant.YES);
			sqlBean.andIn("DEPT_TYPE", deptType);
			//获取该申请单关联的主办单位表数据
			List<Bean> applyDeptList = ServDao.finds(BUREAU_STAFF, sqlBean);
			String userIds = SUUtils.getUserIds(applyDeptList);
			//设置处理人
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
			return result;
		}
		
		if(preNodeBean != null){
			//当前节点编号 24
			String NODE = nodeBean.getStr("NODE_CODE");
			String PIID = nodeBean.getStr("PI_ID");
			StringBuilder str = new StringBuilder();
			str.append("SELECT * FROM SY_WFE_NODE_INST WHERE PI_ID = '"+PIID+"' AND NODE_IF_RUNNING = '2'");
			List<Bean> list= Transaction.getExecutor().query(str.toString());
			int count = list.size();
			do{
				Bean bean = nodeBean;
				//前一节点
				Bean pBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", bean.getStr("PRE_NI_ID")));
				if(pBean !=null){
					NODE = pBean.getStr("NODE_CODE");
					nodeBean = pBean;	
				}
				count --;
			}while(!"N22".equals(NODE) && !"N214".equals(NODE) && count>=0);
			String userId = nodeBean.getStr("DONE_USER_ID");
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("USER_CODE,USER_NAME");
			sqlBean.and("USER_CODE", userId);
			//查询出当前需要的处理人
			List<Bean> userList = ServDao.finds("SY_ORG_USER", sqlBean);
			String userIds = SUUtils.getUserIds(userList);
			result.setUserIDs(userIds);
			result.setAutoSelect(true);		
		}
		return result;
	}
}













