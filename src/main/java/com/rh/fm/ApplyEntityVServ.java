package com.rh.fm;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.Constant;

public class ApplyEntityVServ extends CommonServ {
	/** 当前用户流经列表查询 */
	public OutBean liujingQuery(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		StringBuilder where = new StringBuilder();
		if (paramBean.isNotEmpty(Constant.PARAM_WHERE)) { // 获取ParamBean里的_WHERE_
			where.append(paramBean.getStr(Constant.PARAM_WHERE));
		}
		where.append(getLiujingWhere());

		// 列表查询
		paramBean.set(Constant.PARAM_WHERE, where.toString());
		outBean = super.query(paramBean);
		return outBean;
	}
	
	/** 当前用户起草列表查询 */
	public OutBean faqiQuery(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		StringBuilder where = new StringBuilder();
		if (paramBean.isNotEmpty(Constant.PARAM_WHERE)) { // 获取ParamBean里的_WHERE_
			where.append(paramBean.getStr(Constant.PARAM_WHERE));
		}
		where.append(getFaqiWhere());

		// 列表查询
		paramBean.set(Constant.PARAM_WHERE, where.toString());
		outBean = super.query(paramBean);
		return outBean;
	}

	/** 当前用户待阅列表查询 */
	public OutBean readQuery(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		StringBuilder where = new StringBuilder();
		if (paramBean.isNotEmpty(Constant.PARAM_WHERE)) { // 获取ParamBean里的_WHERE_
			where.append(paramBean.getStr(Constant.PARAM_WHERE));
		}
		where.append(getReadWhere());

		// 列表查询
		paramBean.set(Constant.PARAM_WHERE, where.toString());
		outBean = super.query(paramBean);
		return outBean;
	}

	/**导出前设置where条件*/
	public void beforeExp(ParamBean paramBean) {
		String expType = paramBean.getStr("expType");
		
		StringBuilder where = new StringBuilder();
		if (paramBean.isNotEmpty(Constant.PARAM_WHERE)) {
			where.append(paramBean.getStr(Constant.PARAM_WHERE));
		}
		
		// 拼接不同的where条件
		if (expType.equals("liujing")) {
			where.append(getLiujingWhere());
		}else if (expType.equals("faqi")) {
			where.append(getFaqiWhere());
		}else if (expType.equals("read")) {
			where.append(getReadWhere());
		}

		// 列表查询
		paramBean.set(Constant.PARAM_WHERE, where.toString());
	}
	
	/**得到流经where条件*/
	private String getLiujingWhere(){
		UserBean userBean = Context.getUserBean();
		StringBuilder where = new StringBuilder();
		where.append(" and ( ");
		// 流程实例节点
		where.append(" exists (select s.pi_id from SY_WFE_NODE_INST s where s.pi_id = s_wf_inst and s.TO_USER_ID ='");
		where.append(userBean.getCode());
		where.append("') ");
		// 已办结流程实例节点
		where.append(" or exists (select s.pi_id from SY_WFE_NODE_INST_HIS s where s.pi_id = s_wf_inst and s.TO_USER_ID ='");
		where.append(userBean.getCode());
		where.append("') ");
		where.append(" ) ");
		return where.toString();
	}
	
	/**得到当前用户起草where条件*/
	private String getFaqiWhere(){
		UserBean userBean = Context.getUserBean();
		StringBuilder where = new StringBuilder();
		where.append(" and s_user = '");
		where.append(userBean.getCode());
		where.append("' ");
		return where.toString();
	}
	
	/**得到阅知where条件*/
	private String getReadWhere(){
		UserBean userBean = Context.getUserBean();
		StringBuilder where = new StringBuilder();
		where.append(" and ( ");
		// 待办列表中当前用户的待阅
		where.append(" exists (select s.todo_id from SY_COMM_TODO s where s.todo_object_id1 = apply_id  ");
		where.append(" and s.TODO_CATALOG = 2 and s.owner_code ='");
		where.append(userBean.getCode());
		where.append("') ");
		// 已办列表中当前用户的待阅
		where.append(" or exists (select s.todo_id from SY_COMM_TODO_HIS s where s.todo_object_id1 = apply_id  ");
		where.append(" and s.TODO_CATALOG = 2 and s.owner_code ='");
		where.append(userBean.getCode());
		where.append("') ");
		where.append(" ) ");
		return where.toString();
	}
}
