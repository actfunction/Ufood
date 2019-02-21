package com.rh.gw.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;


/**
 * 署领导秘书服务的扩展类
 *
 * @author kfzx-linll
 * @date 2018/12/19
 */
public class LeaderMsServ extends CommonServ {
	
	// 署领导秘书表和服务一致
	private static final String OA_GW_LEADER_MS_SHIP = "OA_GW_LEADER_MS_SHIP";

	
	/**
	 * 同一机构的署领导不能有2个秘书
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getLeaderMsContent(ParamBean paramBean) {
        // 获取署领导编码, 所在机构, 秘书编码, 服务ID
        String leaderUserCode = paramBean.getStr("LEADER_USER_CODE");
        String sOdeptCode = paramBean.getStr("S_ODEPT_CODE");
        String msId = paramBean.getStr("MS_ID");

        // 获取当前用户信息
        UserBean userBean = Context.getUserBean();
        String deptCode = userBean.getODeptCode();

        // 构建sql语句
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM " + OA_GW_LEADER_MS_SHIP + " WHERE LEADER_USER_CODE = '" + leaderUserCode + "' AND S_ODEPT_CODE = '" + sOdeptCode + "'");

        List<Bean> result = Transaction.getExecutor().query(sql.toString());

        // 构建返回值参数
        OutBean outBean = new OutBean();
        outBean.set("data", result);

        return outBean;
    }

}
