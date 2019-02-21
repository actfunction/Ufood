package com.rh.gw.serv;

import java.util.List;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.tongtech.backport.java.util.Arrays;

/***
 * 所有公文扩展类都继承此类 公共方法写在这里，单独处理的写在扩展类里，用户可根据自身权限查看相关公文
 * 
 * @author zhoumeng
 * @version 1.0
 *
 */

enum RoleCodeDucha {

	办公厅督察处经办("SUP_DC_001", 0), 办公厅督察处复合("SUP_DC_002", 1), 机构督察员("SUP003", 2);

	private String name;
	private int index;

	private RoleCodeDucha(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public static String getName(int index) {
		for (RoleCodeDucha r : RoleCodeDucha.values()) {
			if (r.getIndex() == index) {
				return r.getName();
			}
		}
		return "不存在";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}

public class GongWenKuSupervise extends GwExtServ {
	
	
	//获取督察列表
	public OutBean getSuperviseList(ParamBean paramBean) {
		StringBuffer buffer = new StringBuffer();
		OutBean out = new OutBean();
		buffer.append(" 1=1 ");
		System.out.println(paramBean.toString());
		String limitstr = paramBean.getStr("limit");// 该参数可传，也可以不传，默认为50条数据
		String pagestr = paramBean.getStr("page");// 该参数为页数
		int limit = Integer.parseInt(limitstr.equals("") ? "50" : limitstr);
		int page = Integer.parseInt(pagestr.equals("") ? "1" : pagestr);
		int pagetrue = (page - 1) * limit + 1;// 系统查询分页的方法有问题,做一个封装。
		SqlExecutor executor = Transaction.getExecutor();
		/*String sql = "SELECT * FROM OA_GW_GONGWEN gw WHERE (GW_GONGWEN_DCFQ ='1' OR gw.GW_ID IN (SELECT gain.GAIN_LINK FROM SUP_APPRO_GAIN gain)) AND gw.S_FLAG = '1'"+
				" UNION SELECT * FROM OA_GW_GONGWEN gw WHERE gw.GW_ID IN (SELECT sup.GONGWEN_ID  FROM SUP_APPRO_OFFICE sup)  AND gw.S_FLAG = '1'";
	*/
		String sql ="SELECT gw.*,zcdz.FINAL_JGJC FROM OA_GW_GONGWEN gw  INNER JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE WHERE (GW_GONGWEN_DCFQ ='1' OR gw.GW_ID IN (SELECT gain.GAIN_LINK FROM SUP_APPRO_GAIN gain)) AND gw.S_FLAG = '1' "+
				" UNION SELECT gw.*,zcdz.FINAL_JGJC FROM OA_GW_GONGWEN gw  INNER JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE WHERE gw.GW_ID IN (SELECT sup.GONGWEN_ID  FROM SUP_APPRO_OFFICE sup)  AND gw.S_FLAG = '1'";
		UserBean userbean = Context.getUserBean();
	
		String[] roles = userbean.getRoleCodes();// 获取当前登录人的角色
		List<String> list = Arrays.asList(roles);
		for (int i = 0; i < 3; i++) {
			if (list.contains(RoleCode.getName(i))) {
				List<Bean> SuperviseData = executor.query(sql, pagetrue, limit);
				String sqlcount = "SELECT COUNT(*) " + "FROM (" + sql + ")";// 查询有多少条数据
				Bean beancount = executor.queryOne(sqlcount);
				out.set("superviseData", SuperviseData);
				out.set("beancount", beancount);
				out.set("pagetrue", pagetrue);
				return out;
			}
		}
		return out.set("msg", "数据不存在");
	}
	
	
	//公文详情列表展示
	//public OutBean get
	
	
}
