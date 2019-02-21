package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IWfeApiServ {

	public ApiOutBean getWfeTrack(Bean reqData);

	public ApiOutBean getWfeTrackForPC(Bean reqData);

	public ApiOutBean getWfeBtn(Bean reqData);

	public ApiOutBean finish(Bean reqData);

	public ApiOutBean undoFinish(Bean reqData);

	public ApiOutBean withdraw(Bean reqData);

	public ApiOutBean stopParallelWf(Bean reqData);

	public ApiOutBean deleteDoc(Bean reqData);

	public ApiOutBean wfeSend(Bean reqData);

	public ApiOutBean duZhan(Bean reqData);

	public ApiOutBean qianShou(Bean reqData);

	public ApiOutBean qianShou2Shouwen(Bean reqData);

	public ApiOutBean getWfeImg(String servId);

	public ApiOutBean saveWfeImg(Bean reqData);

	public ApiOutBean getUserBeanByUserCode(String userCode);

	public ApiOutBean getWfePercent(Bean reqData);
	
	public ApiOutBean getWfeBinderByNode(Bean reqData);
	
	public ApiOutBean rtnQcr(Bean reqData);
	
	public ApiOutBean getLineDefByNode(Bean paramBean);
	
	public ApiOutBean checkNodeRunning(Bean paramBean);
}
