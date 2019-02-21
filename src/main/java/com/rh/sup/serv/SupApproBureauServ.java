package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.*;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;

import com.rh.sup.util.SupConstant;
import com.rh.sup.job.SupSendTodoJob;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 司内立项扩展类
 * 
 * @author zhaosheng
 *
 */
public class SupApproBureauServ extends CommonServ {


	/**
	 * @param paramBean,
	 * 发送到指定节点后的逻辑操作
	 */
	public OutBean beforeSendToNode(ParamBean paramBean){
		String servId=paramBean.getServId();
		OutBean outBean=new OutBean();
		String approId=paramBean.getStr("approId");
		String currNodeCode=paramBean.getStr("currNodeCode");
		String nextNodeCode=paramBean.getStr("nextNodeCode");
		String nid=paramBean.getStr("nid");
		//发送至办结环节
		if("N214".equals(currNodeCode) && "N3".equals(nextNodeCode)){
			//校验当次督查办理是否填写
			ParamBean paramGain=new ParamBean();
			paramGain.set("servId",servId);
			paramGain.set("NID",currNodeCode);
			paramGain.set("APPRO_ID",approId);
			outBean=ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"isMaintain",paramGain);
			if(!outBean.isOkOrWarn()){
				return outBean;
			}
			//办结校验
			paramBean.set("servId",servId);
			outBean=makeApproMerge(paramBean);
			if(!outBean.isOkOrWarn()){
				return outBean;
			}
		}
		return outBean.setOk();
	}

	/**
	 * @param paramBean,
	 * 发送到指定节点后的逻辑操作
	 */
	public OutBean afterSendToNode(ParamBean paramBean){
		String servId=paramBean.getServId();
		OutBean outBean=new OutBean();
		String approId=paramBean.getStr("approId");
		String currNodeCode=paramBean.getStr("currNodeCode");
		String nextNodeCode=paramBean.getStr("nextNodeCode");
		String nid=paramBean.getStr("nid");
		//发送至办结环节
		if("N214".equals(currNodeCode) && "N3".equals(nextNodeCode)){
			//推送至办结环节，直接将当次的办理情况制为待审批状态
			ParamBean gainParam=new ParamBean();
			gainParam.set("approId",approId);
			gainParam.set("curState","1");
			gainParam.set("upState","2");
			gainParam.set("deptCode",Context.getUserBean().getDeptCode());
			ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"updateWfState",gainParam);
		}else if("N3".equals(currNodeCode) && "N214".equals(nextNodeCode)){
			//推送至办结环节，直接将当次的办理情况制为待审批状态
			ParamBean gainParam=new ParamBean();
			gainParam.set("approId",approId);
			gainParam.set("curState","2");
			gainParam.set("upState","1");
			gainParam.set("deptCode",Context.getUserBean().getDeptCode());
			ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"updateWfState",gainParam);
		}else if("N212".equals(nextNodeCode)){
			//流程发送至办结，将立项单所有待审核的办理情况制为审批通过状态
			ServMgr.act(SupConstant.OA_SUP_APPRO_GAIN,"allGainPass",paramBean);
		}
		return outBean;
	}

    //推送至下月办理时，删除办理人待办
    public OutBean waitNextGain(ParamBean paramBean) {
        OutBean outBean=new OutBean();
        //系统配置项，可关闭下月推送功能
        Boolean waitNextMonth=Context.getSyConf("WAIT_NEXT_GAIN",true);
        if(!waitNextMonth){
        	return outBean;
		}
		Bean gainBean=new Bean();
        String currServId=paramBean.getStr("servId");
        String approId=paramBean.getStr("approId");
        gainBean.set("SERV_ID",currServId);
        gainBean.set("APPRO_ID",approId);
        String niId=paramBean.getStr("niId");
        List<Bean> nextNodeInstList= ServDao.finds("SY_WFE_NODE_INST"," and PRE_NI_ID='"+niId+"'");
        for(Bean nextNodeInst:nextNodeInstList){
            String currNiId=nextNodeInst.getId();
            List<Bean> nextTodoList= ServDao.finds("SY_COMM_TODO"," and TODO_OBJECT_ID2='"+currNiId+"'");
            for(Bean nextTodo:nextTodoList){
                gainBean.set("TODO_ID",nextTodo.getId());
                gainBean.set("OWNER_CODE",nextTodo.getStr("OWNER_CODE"));
                gainBean.set("NI_ID",nextNodeInst.getId());
                ServDao.create("OA_SUP_NEXT_GAIN_PUSH",gainBean);
				ServDao.delete("SY_COMM_TODO",nextTodo.getId());
            }
        }
        return outBean;
    }

	// 测试：触发督查办理定时任务
	public OutBean sendTodo(ParamBean paramBean) {
		SupSendTodoJob supSendTodoJob=new SupSendTodoJob();
		supSendTodoJob.startJob();
		return new OutBean().setOk("开始推送了");
	}

	// 查询成果体现及办理计划
	public OutBean findPlanCountByApproId(ParamBean paramBean) {
		String sql = " and APPRO_ID='" + paramBean.getStr("approId") + "'";
		List<Bean> beanList = ServDao.finds("OA_SUP_APPRO_PLAN", sql);
		int count = beanList.size();
		return new OutBean().set("count", count);
	}
	// 更新立项单状态
	public OutBean updateApproState(ParamBean paramBean) {
		String currServId=paramBean.getStr("servId");
		String approId=paramBean.getStr("approId");
		String state=paramBean.getStr("state");
		Bean approBean=ServDao.find(currServId,approId);
		approBean.set("APPLY_STATE",state);
		ServDao.update(currServId,approBean);
		return new OutBean().setOk();
	}
	// 督查办结
	public OutBean makeApproMerge(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");
		String servId = paramBean.getStr("servId");
		if (!deptHaveGain(servId,approId)) {
			return new OutBean().setError("还有机构未填写过督查办理情况！");
		}
		List<Bean> planList=new ArrayList<Bean>();
		if(SupConstant.OA_SUP_APPRO_OFFICE.equals(servId)){
			planList = ServDao.finds(SupConstant.OA_SUP_APPRO_OFFICE_PLAN, " and APPRO_ID='" + approId + "'");
		}else{
			planList = ServDao.finds(SupConstant.OA_SUP_APPRO_PLAN, " and APPRO_ID='" + approId + "'");
		}
		if (planList.size()>0 && needDc(planList)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.appendWhere(" and APPRO_ID=? and GAIN_STATE in (?,?)", approId, "2", "3");
			List<Bean> gainList = ServDao.finds(SupConstant.OA_SUP_APPRO_GAIN, sqlBean);
			for (Bean gain : gainList) {
				String gainLink = gain.getStr("GAIN_LINK");
				if (StringUtils.isNotEmpty(gainLink)) {
					return new OutBean().setOk();
				}
			}
			return new OutBean().setError("尚未关联任何公文文件！");
		} else {
			return new OutBean().setOk();
		}
	}

	// 各各单位是否办理
	public Boolean deptHaveGain(String currServId,String approId) {
		List<Bean> deptList=new ArrayList<Bean>();
		if (SupConstant.OA_SUP_APPRO_BUREAU.equals(currServId)){
			deptList = ServDao.finds(SupConstant.OA_SUP_APPRO_BUREAU_HOST, " and BUREAU_ID='" + approId + "' and DEPT_TYPE in(1,2)");
		}else if(SupConstant.OA_SUP_APPRO_OFFICE.equals(currServId)){
			deptList = ServDao.finds(SupConstant.OA_SUP_APPRO_OFFICE_HOST, " and OFFICE_ID='" + approId + "' and DEPT_TYPE in(1,2)");
		}else if(SupConstant.OA_SUP_APPRO_POINT.equals(currServId)){
			SqlBean sqlBean = new SqlBean();
			//sqlBean.appendWhere(" and APPRO_ID=? and GAIN_STATE in (?,?)", approId, "2", "3");
			//办理情况和办结可同时提交
			sqlBean.appendWhere(" and APPRO_ID=? ", approId);
			List<Bean> gainList = ServDao.finds(SupConstant.OA_SUP_APPRO_GAIN, sqlBean);
			if (gainList.size() <1) {
				return false;
			}else{
				return true;
			}
		}
		for(Bean deptBean:deptList){
			String deptCode=deptBean.getStr("DEPT_CODE");
			String deptType=deptBean.getStr("DEPT_TYPE");
			SqlBean sqlBean = new SqlBean();
			if("1".equals(deptType)){
				sqlBean.appendWhere(" and APPRO_ID=? and DEPT_CODE=?", approId, deptCode);
			}else {
				sqlBean.appendWhere(" and APPRO_ID=? and GAIN_STATE in (?,?) and DEPT_CODE=?", approId, "2", "3", deptCode);
			}
			List<Bean> gainList = ServDao.finds(SupConstant.OA_SUP_APPRO_GAIN, sqlBean);
			if(gainList.size()<1){
				return false;
			}
		}
		return true;
	}

	// 是否需要关联公文
	public Boolean needDc(List<Bean> planList) {
		for (Bean plan : planList) {
			String resStep = plan.getStr("RES_STEP");
			if (needDcDict(resStep)) {
				return true;
			}
		}
		return false;
	}

	// 根据字典判断是否需要关联公文
	public Boolean needDcDict(String dictId) {
		Bean dict = ServDao.find(SupConstant.OA_SUP_SERV_DICT, dictId);
		String dictValue = dict.getStr("EXTEND1");
		// 需要
		if ("1".equals(dictValue)) {
			return true;
		}
		return false;
	}

	// 督查取消办结
	public OutBean reMakeApproMerge(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");
		String servId = paramBean.getStr("servId");
		Bean bean = ServDao.find(servId, approId);
		String state = bean.getStr("APPLY_STATE");
		// 已在办结状态
		if ("6".equals(state)) {
			/*bean.set("APPLY_STATE", "5");
			ServDao.update(servId, bean);*/
			return new OutBean().setOk();
		} else {
			return new OutBean().setError("当前督查事项未处在办结状态！");
		}
	}

	// 督查归档
	public OutBean makeApproOver(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");
		String servId = paramBean.getStr("servId");
		Bean bean = ServDao.find(servId, approId);
		String state = bean.getStr("APPLY_STATE");
		// 已在办结审核通过状态
		if ("7".equals(state)) {
			/*bean.set("APPLY_STATE", "4");
			ServDao.update(servId, bean);*/
			return new OutBean().setOk();
		} else {
			return new OutBean().setError("当前督查事项办结审核尚未通过！");
		}
	}

	/**
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
	 * 主要负责同志，督查人员，综合处室人员
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getThreeUser(ParamBean paramBean) {
		// 获取主键
		String ID = paramBean.getStr("ID");
		// 根据主键获取
		Bean result = ServDao.find(SupConstant.OA_SUP_APPRO_BUREAU, ID);
		
		//获取UserCode值用字典进行编译成name
		String leadHostAuditStaff = result.getStr("LEAD_HOST_AUDIT_STAFF");
		if(!StringUtils.isEmpty(leadHostAuditStaff)){
			String leadHostAuditStaffName = DictMgr.getName("SY_ORG_USER", leadHostAuditStaff);
			result.set("LEAD_HOST_AUDIT_STAFF", leadHostAuditStaffName);
		}
			
		//获取UserCode值用字典进行编译成name
		String officeOverseer = result.getStr("OFFICE_OVERSEER");
		if(!StringUtils.isEmpty(officeOverseer)){
			String officeOverseerName = DictMgr.getName("SY_ORG_USER", officeOverseer);
			result.set("OFFICE_OVERSEER", officeOverseerName);
		}else {		
			String officeOverseerName = DictMgr.getName("SY_ORG_USER", Context.getUserBean().getCode());
			result.set("OFFICE_OVERSEER", officeOverseerName );
		}	
		
		//获取UserCode值用字典进行编译成name
		String officeGeneral = result.getStr("OFFICE_GENERAL");
		if(!StringUtils.isEmpty(officeGeneral)){
			String officeGeneralName = DictMgr.getName("SY_ORG_USER", officeGeneral);
			result.set("OFFICE_GENERAL", officeGeneralName);
		}
		return new OutBean().set("result", result);
	}
	

	/**
	 * 获取当前用户
	 */
	public OutBean getCurrentUser(ParamBean paramBean) {
		return new OutBean().set("OFFICE_OVERSEER", DictMgr.getName("SY_ORG_USER", Context.getUserBean().getCode()));
	}
	
	/**
	 * 获取用户
	 * @param paramBean
	 * @return
	 */
	public OutBean getUser(ParamBean paramBean) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("USER_CODE,USER_NAME,ORG_USER_TEL");
		sqlBean.and("DEPT_PCODE", Context.getUserBean().getDeptBean().getPcode());
		sqlBean.and("DEPT_CODE", paramBean.getStr("deptCode"));
		List<Bean> userList = ServDao.finds("OA_BUREAU_ORG_USER_V", sqlBean);
		OutBean outBean = new OutBean();
		outBean.set("userList", userList);
		return outBean;
	}
	
	/**
	 * 获取电话
	 * @param paramBean
	 * @return
	 */
	public OutBean getPhone(ParamBean paramBean) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("ORG_USER_TEL");
		sqlBean.and("USER_CODE", paramBean.getStr("userCode"));
		List<Bean> phoneList = ServDao.finds("OA_BUREAU_ORG_USER_V", sqlBean);
		OutBean outBean = new OutBean();
		outBean.set("phoneList", phoneList);
		return outBean;
	}
	
	/**
	 * 判断是否主办单位(流程在主办单位)
	 * @param paramBean
	 * @return
	 */
	public OutBean IsMain(ParamBean paramBean) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("DEPT_TYPE");
		sqlBean.and("BUREAU_ID", paramBean.getStr("approId"));
		sqlBean.and("DEPT_CODE", paramBean.getStr("deptCode"));
		Bean bean = ServDao.find(SupConstant.OA_SUP_APPRO_BUREAU_HOST, sqlBean);
		OutBean result = new OutBean();
		if(bean.getStr("DEPT_TYPE").equals("1")){
			result.set("IsMain", true);
		}else{
			result.set("IsMain", false);
		}
		return result;
	}

	/**
     * @description: 督查办理逾期天数
     * @author: kfzx-guoch
     * @date: 2018/12/19 19:24
     */
    public void updateOverDay(ParamBean paramBean) {
		// 立项单主键
		String approId = paramBean.getStr("approId");
	
        String sql = "SELECT * FROM SUP_APPRO_BUREAU WHERE ID = '" + approId + "'";
        Bean point = Transaction.getExecutor().query(sql).get(0);
        String limitDate = point.getStr("LIMIT_DATE");
        if(limitDate !=null && !"".equals(limitDate)){
        	SupApperUrge supApperUrge = new SupApperUrge();
	        String day = supApperUrge.getWorkDay(supApperUrge.getYDM(new Date()), limitDate);
	        if(Integer.valueOf(day) <= 0){
	            point.set("OVERDUE_DAY", 0);
	        } else {
	            point.set("OVERDUE_DAY", day);
	        }
        }else{
        	 point.set("OVERDUE_DAY", 0);
        }
        ServDao.update("OA_SUP_APPRO_BUREAU", point);
    }  
    
    
	/**
	 * 判断是否主办单位(true-主办单位，false-非主办单位)
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean IsMainDept(ParamBean paramBean) {
		//获取服务Id 
		//获取当前节点Id 
		//获取需要判断的节点编号
		String bureauId = paramBean.getStr("approId");
		String niId = paramBean.getStr("niId");
		String numN = paramBean.getStr("numN");
		
		//获取当前节点实例 
		Bean nodeBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", niId));
		//获取当前节点编号
    	String NODE = nodeBean.getStr("NODE_CODE");
		String PIID = nodeBean.getStr("PI_ID");
		//查询当前节点前共有节点数量
		StringBuilder str = new StringBuilder();
		str.append("SELECT * FROM SY_WFE_NODE_INST WHERE PI_ID = '"+PIID+"' AND NODE_IF_RUNNING = '2'");
		List<Bean> listcount= Transaction.getExecutor().query(str.toString());
		int count = listcount.size();
		//根据前一节点循环直到找到计划办理节点，循环上限count
		do{
			Bean bean = nodeBean;
			//前一节点实例
			Bean pBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", bean.getStr("PRE_NI_ID")));
			if(pBean !=null){
				//前一节点编号
				NODE = pBean.getStr("NODE_CODE");
				//前一节点赋值给当前节点
				nodeBean = pBean;
			}
			//循环每次数量减一
			count --;
		}while( !numN.equals(NODE) && count>=0);
		
		String userCode = nodeBean.getStr("DONE_USER_ID");
		//根据节点user_code找主办单位判断是否主办单位
		StringBuilder strB = new StringBuilder();
		strB.append("SELECT DEPT_TYPE FROM SUP_APPRO_BUREAU_STAFF WHERE BUREAU_ID = '"+bureauId+"' AND USER_CODE = '"+userCode+"'");
		List<Bean> list= Transaction.getExecutor().query(strB.toString());		
		OutBean result = new OutBean();
		if(list.size()>=0){
			if(list.get(0).getStr("DEPT_TYPE").equals("1")){
				result.set("IsMain", true);
			}else{
				result.set("IsMain", false);
			}			
		}	
		return result;
	}   
}
