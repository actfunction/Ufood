package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IDataStatsServ {
	public ApiOutBean getUserTabDataByStime(Bean reqData);

	public ApiOutBean getUserStatsDataByStime(Bean reqData);

	public ApiOutBean getDeptTabDataByStime(Bean reqData);

	public ApiOutBean getOrgTabDataByStime(Bean reqData);

	public ApiOutBean getDeptStatsDataZBByStime(Bean reqData);

	public ApiOutBean getDeptStatsDataHQByStime(Bean reqData);

	public ApiOutBean getDeptStatsDataPHBByStime(Bean reqData);

	public ApiOutBean getOrgStatsDataSLSXByStime(Bean reqData);

	public ApiOutBean getOrgStatsDataQSJByStime(Bean reqData);

	public ApiOutBean getOrgStatsDataBLSJByStime(Bean reqData);

	public ApiOutBean getOrgStatsDataPHBByStime(Bean reqData);

	public ApiOutBean getUserStatsCharBJLByStime(Bean reqData);

	public ApiOutBean getUserStatsCharWORKByStime(Bean reqData);

	public ApiOutBean getDeptStatsCharBJLByStime(Bean reqData);

	public ApiOutBean getDeptStatsCharRunningByStime(Bean reqData);

	public ApiOutBean getDeptStatsCharGWCountByStime(Bean reqData);

	public ApiOutBean getDeptStatsCharZBGWCountByStime(Bean reqData);

	public ApiOutBean getDeptStatsCharHQGWCountByStime(Bean reqData);

	public ApiOutBean getOrgStatsCharBJLByStime(Bean reqData);

	public ApiOutBean getOrgStatsCharRunningByStime(Bean reqData);

	public ApiOutBean getOrgStatsTabGWCountByStime(Bean reqData);

	public ApiOutBean getOrgStatsCharSLSXGWCountByStime(Bean reqData);

	public ApiOutBean getOrgStatsQSJGWCountByStime(Bean reqData);

	public ApiOutBean getOrgStatsCharQSJGWCountByStime(Bean reqData);

	public ApiOutBean getOrgStatsCharBLSJByStime(Bean reqData);

	public ApiOutBean getOrgStatsGWSXByStime(Bean reqData);

	public ApiOutBean getRingCount(Bean reqData);

	public ApiOutBean getDealCount(Bean reqData);

	public ApiOutBean getGwCount(Bean reqData);

	public ApiOutBean getGwAgingCount(Bean reqData);

	public ApiOutBean getGwBlCount(Bean reqData);

	public ApiOutBean getZbRingCount(Bean reqData);

	public ApiOutBean getZbBarCount(Bean reqData);

	public ApiOutBean getZbCount(Bean reqData);

	public ApiOutBean getHqBarCount(Bean reqData);

	public ApiOutBean getHqCount(Bean reqData);

	public ApiOutBean getGwDealSitu(Bean reqData);

	public ApiOutBean getGwSxzlCount(Bean reqData);

	public ApiOutBean getGwPhbCount(Bean reqData);

	public ApiOutBean getUserGwCount(Bean reqData);

	public ApiOutBean getUserRingCount(Bean reqData);

	public ApiOutBean getUserGwDealSitu(Bean reqData);

	public ApiOutBean getUserGwRepList(Bean reqData);

	public ApiOutBean getUserGwList(Bean reqData);

	public ApiOutBean getUserStatsData(Bean reqData);

	public ApiOutBean getDeptStatsDataTodo(Bean reqData);

	public ApiOutBean getDeptStatsDataTodoHis(Bean reqData);

	public ApiOutBean getOrgGwCount(Bean reqData);

	public ApiOutBean getOrgGwDelay(Bean reqData);

	public ApiOutBean getOrgSWQsCount(Bean reqData);

	public ApiOutBean getOrgBLTime(Bean reqData);
}
