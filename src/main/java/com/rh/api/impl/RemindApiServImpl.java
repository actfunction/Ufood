package com.rh.api.impl;

import java.util.HashMap;
import java.util.Map;

import com.rh.api.serv.IRemindApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.util.JsonUtils;
import com.rh.tc.api.client.util.HttpMethodEnum;
import com.rh.tc.api.client.util.HttpUtil;


public class RemindApiServImpl implements IRemindApiServ {

	HttpUtil client = HttpUtil.getInstance();

	Map<Object, Object> header = new HashMap<Object, Object>();

	@Override
	public ApiOutBean getRemind(Bean reqData) {
		ApiOutBean rtnBean = new ApiOutBean();

		client.setRemote(Context.getSyConf("TODO_SYNC_API_URL", "http://cochat.cn:8189"));

		HashMap<Object, Object> data = new HashMap<Object, Object>();

		header.put("appId", "ruaho");

		data.putAll(header);

		data.put("reqData", reqData);

		String rtnStr = client.send("/api/getRemind", HttpMethodEnum.POST, data);

		rtnBean.setData(JsonUtils.toBean(rtnStr));

		return rtnBean;
	}

	@Override
	public ApiOutBean saveRemind(Bean reqData) {
		ApiOutBean rtnBean = new ApiOutBean();

		client.setRemote(Context.getSyConf("TODO_SYNC_API_URL", "http://cochat.cn:8189"));

		HashMap<Object, Object> data = new HashMap<Object, Object>();

		header.put("appId", "ruaho");

		data.putAll(header);

		data.put("reqData", reqData);

		String rtnStr = client.send("/api/saveRemind", HttpMethodEnum.POST, data);

		rtnBean.setData(JsonUtils.toBean(rtnStr));

		return rtnBean;
	}
}
