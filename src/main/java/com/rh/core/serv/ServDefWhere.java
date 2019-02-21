package com.rh.core.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 服务过滤规则定义类
 * 
 * @author wanglong
 */
public class ServDefWhere extends CommonServ {
	/**
	 * 修改服务定义后清除cache
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @param outBean
	 *            输出信息
	 */
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		if (outBean.isOk() && !paramBean.getLinkMode()) { // 非级联才更新缓存
			if (!outBean.isEmpty("SERV_ID")) {
				ServUtils.udpateMtime(outBean.getStr("SERV_ID"), outBean.getStr("S_MTIME"));
			}
		}
	}

	/**
	 * 删除规则定义后清除cache
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @param outBean
	 *            输出信息
	 */
	protected void afterDelete(ParamBean paramBean, OutBean outBean) {
		if (outBean.isOk() && !paramBean.getLinkMode()) { // 非级联才更新缓存
			List<Bean> dataList = outBean.getDataList();
			if (dataList.size() > 0) {
				Bean dataBean = dataList.get(0);
				if (dataBean != null) {
					ServUtils.udpateMtime(dataBean.getStr("SERV_ID"), null);
				}
			}
		}
	}

	/**
	 * 导入查询规则
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return 输出信息
	 */
	public Bean impWheres(Bean paramBean) {
		String servId = paramBean.getStr("SERV_ID");
		ServDefBean servDef = ServUtils.getServDef(servId);
		String wfeManagerRoleCode = Context.getSyConf("WFE_MANAGER_ROLE_CODE", "A00-080");
		String[] name = { "公司领导看全部", "部门领导看本部门", "处室领导看本处室", "其他员工查看本人" };
		String[] script = {
				"\"@ROLE_CODES@\".indexOf(\"'RGSLD'\") >= 0 || \"@ROLE_CODES@\".indexOf(\"'" + wfeManagerRoleCode
						+ "'\") >= 0",
				"\"@ROLE_CODES@\".indexOf(\"'RBMLD'\") >= 0", "\"@ROLE_CODES@\".indexOf(\"'RCSLD'\") >= 0", "1 == 1" };
		String[] content;
		int[] flowFlag = { ServConstant.FLOW_FLAG_NONE, ServConstant.FLOW_FLAG_NONE, ServConstant.FLOW_FLAG_NONE,
				ServConstant.FLOW_FLAG_NONE };
		if (servDef.hasWfAuto()) { // 如果在流程中，则启用流经判断
			content = new String[] { "", "", "", "" };
			flowFlag[1] = ServConstant.FLOW_FLAG_TDEPT;
			flowFlag[2] = ServConstant.FLOW_FLAG_DEPT;
			flowFlag[3] = ServConstant.FLOW_FLAG_USER;
		} else { // 如果不在流程中，启用sql判断
			content = new String[] { "", "and S_TDEPT='@TDEPT_CODE@'", "and S_DEPT='@DEPT_CODE@'",
					"and S_USER='@USER_CODE@'" };
		}
		String[] flag = { Constant.YES, Constant.YES, Constant.YES, Constant.YES };
		ParamBean param = new ParamBean(ServMgr.SY_SERV_WHERE, "batchSave");
		List<Bean> wheres = new ArrayList<Bean>();
		for (int i = 0; i < name.length; i++) {
			Bean where = new Bean();
			where.set("SERV_ID", paramBean.getStr("SERV_ID"));
			where.set("WHERE_NAME", name[i]);
			where.set("WHERE_SCRIPT", script[i]);
			where.set("WHERE_CONTENT", content[i]);
			where.set("WHERE_FLOW_FLAG", flowFlag[i]);
			where.set("WHERE_SERV_TYPE", "1"); // 缺省为本服务流经
			where.set("WHERE_ORDER", i * 10);
			where.set("S_FLAG", flag[i]);
			wheres.add(where);
		}
		param.setBatchSaveDatas(wheres);
		OutBean outBean = ServMgr.act(param);
		if (outBean.isOk()) {
			ServUtils.clearServCache(paramBean.getStr("SERV_ID"));
		}
		return outBean;
	}
}
