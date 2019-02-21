package com.rh.fm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.PageBean;

public class FmTjUtilServ extends CommonServ {
	public OutBean fmTjjs(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String where = paramBean.getStr("_searchWhere");
		ParamBean sqlBean = new ParamBean();
		
		sqlBean.setTable("FM_PROC_APPLY_TJ_V");
		sqlBean.setSelect("s_odept,S_ONAME,reg_proc_type_one,count(reg_proc_type_one)as num ");
		String where1 = "and s_odept is not null and S_ONAME is not null and reg_proc_type_one is not null ";
		String where2 = " group by s_odept,S_ONAME,reg_proc_type_one";
		sqlBean.setWhere(where1 + where + where2);
		
		List<Bean> list = ServDao.finds("FM_PROC_APPLY_TJJS_V", sqlBean);
		Map<String, Bean> map = new HashMap<String, Bean>();
		for (Bean bean : list) {
			String odept = bean.getStr("S_ODEPT");
			String oname = bean.getStr("S_ONAME");
			String type = bean.getStr("REG_PROC_TYPE_ONE");
			int num = bean.getInt("NUM");

			if (map.containsKey(odept)) {
				Bean tmpBean = map.get(odept);
				int applyNumOld = tmpBean.getInt("APPLY_NUM");
				tmpBean.set(type, num);
				tmpBean.set("APPLY_NUM", num + applyNumOld);
				map.put(odept, tmpBean);
			} else {
				Bean tmpBean = new Bean();
				tmpBean.set(type, num);
				tmpBean.set("APPLY_NUM", num);
				tmpBean.set("S_ONAME", oname);
				map.put(odept, tmpBean);
			}
		}

		List<Bean> resList = new ArrayList<Bean>();
		LinkedHashMap<String, Bean> cols = new LinkedHashMap<String, Bean>();
		// 遍历map中的键
		int i = 0;
		for (String key : map.keySet()) {
			Bean tmpBean = map.get(key);
			tmpBean.setId(key);
			tmpBean.set("S_ODEPT", key);
			tmpBean.set("_ROWNUM_", i);
			tmpBean.set("ROWNUM_", i + 1);
			resList.add(tmpBean);
			i++;
		}

		String[] colArray = { "S_ODEPT", "APPLY_NUM", "REG_PROC_TYPE_ONE", "NUM" };
		for (int j = 0; j < colArray.length; j++) {
			Bean colBean = new Bean();
			colBean.set("ITEM_LIST_FLAG", 1);
			colBean.set("ITEM_CODE", colArray[j]);
			colBean.set("ITEM_NAME", colArray[j]);
			cols.put(colArray[j], colBean);
		}

		outBean.setData(resList);
		outBean.setCols(cols);
		outBean.setCount(i);
		PageBean page = new PageBean();
		page.setAllNum(i);
		page.setNowPage(1);
		page.setShowNum(100);
		page.setPages(1);
		outBean.setPage(page);
		outBean.set("code", 0);
		return outBean;
	}
}
