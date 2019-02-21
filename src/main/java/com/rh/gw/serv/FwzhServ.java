package com.rh.gw.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Lang;


/**
 * 发文字号服务的扩展类
 *
 * @author kfzx-linll
 * @date 2018/11/21
 */
public class FwzhServ extends CommonServ {
	
	// 发文字号-简称代字中间表和服务一致
	private static final String OA_GW_FWZH_JCDZ = "OA_GW_FWZH_JCDZ";
	
	// 发文字号表和服务一致
	private static final String OA_GW_GONGWEN_WJTYPE = "OA_GW_GONGWEN_WJTYPE";

	
	/**
	 * 保存之前
	 *
	 * @param paramBean
	 */
	@Override
	protected void beforeSave(ParamBean paramBean) {
		
		String faWenZiHaoId = paramBean.getId(); // 主键ID
		if ("".equals(faWenZiHaoId)) {
			faWenZiHaoId = paramBean.getStr("WJTYPE_ID");
		}

		// 删除中间表faWenZiHaoId的所有数据
		ServDao.delete(OA_GW_FWZH_JCDZ, " WHERE WJTYPE_ID = '" + faWenZiHaoId + "'");

		Bean bean = ServDao.find(OA_GW_GONGWEN_WJTYPE, faWenZiHaoId);
		if (bean == null) {
			bean = new Bean();
		}
		String [] itemArr = {"WJTYPE_SYSJ_CODE", "WJTYPE_SYSJ_NAME", "WJTYPE_FINAL_JGJC", "WJTYPE_TYPE", "WJTYPE_NAME"};
		for (String item : itemArr) {
			if (paramBean.isNotEmpty(item)) {
				bean.set(item, paramBean.getStr(item));
			}
		}
		bean.setId("");

		String[] codes = bean.getStr("WJTYPE_SYSJ_CODE").split(","); // 使用司局ID
		String[] aa = bean.getStr("WJTYPE_FINAL_JGJC").split(","); // 最终机构简称代字

		int codeLen = codes.length;
		int aaLen = aa.length;
		
		//if (codeLen > 0 && codeLen == aa.length) {
			// 遍历使用司局ID
			for (int i = 0; i < codes.length; i++) {
				String name = bean.getStr("WJTYPE_NAME"); // 文件类型名称
				String type = bean.getStr("WJTYPE_TYPE"); // 文件类型
				// String jianCheng = bean.getStr("WJTYPE_JGJC"); // 机构简称：审X
				
				bean.set("WJTYPE_ID", faWenZiHaoId); // 主键ID
				bean.set("ORG_CODE", codes[i]); // 中间表的使用司局ID
				// bean.set("WJTYPE_SYSJ_CODE", codes[i]); // 发文字号表中的使用司局ID
				// bean.set("WJTYPE_FINAL_JGJC", aa[i]); // 发文字号表中的简称
				
				String finalJianCheng = "";
				if (aaLen == codeLen) {
					finalJianCheng = aa[i]; // 最终机构简称代字
				}

		
				if ((name.indexOf('X') > -1) && (!"".equals(finalJianCheng))) {
					bean.set("FINAL_JGJC", name.replace("X", finalJianCheng)); // 把大写X字替换掉
					ServDao.save(OA_GW_FWZH_JCDZ, bean);
				} else if ((name.indexOf('x') > -1) && (!"".equals(finalJianCheng))) {
					bean.set("FINAL_JGJC", name.replace("x", finalJianCheng)); // 把小写x字替换掉
					ServDao.save(OA_GW_FWZH_JCDZ, bean);
				} else {
					bean.set("FINAL_JGJC", name);
					ServDao.save(OA_GW_FWZH_JCDZ, bean);
				}
			}
//		} else {
//			throw new TipException("使用司局与对应的机构简称代字数目不匹配！");
//		}
 	}


	/**
	 * 删除之前
	 *
	 * @param paramBean
	 */
	@Override
	protected void beforeDelete(ParamBean paramBean) {
		String faWenZiHaoId = paramBean.getId();// 主键ID
		if ("".equals(faWenZiHaoId)) {
			faWenZiHaoId = paramBean.getStr("WJTYPE_ID");
		}
		
		// 删除中间表faWenZiHaoId的所有数据
		// ServDao.delete(OA_GW_FWZH_JCDZ, " WHERE WJTYPE_ID = '" + faWenZiHaoId + "'");
		
		SqlExecutor executor = Transaction.getExecutor(); //得到执行sql对象
		String sql = "UPDATE " + OA_GW_FWZH_JCDZ + " SET S_FLAG = '2' WHERE WJTYPE_ID = '" + faWenZiHaoId + "';";
		try {
			executor.execute(sql);
			log.debug("删除成功！SQL：" + sql);
		} catch(Exception e) {
			
			log.debug("删除失败");
		}
		log.debug("删除成功！WJTYPE_ID：" + faWenZiHaoId);
	}

}
