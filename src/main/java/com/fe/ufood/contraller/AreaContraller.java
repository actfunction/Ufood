package com.fe.ufood.contraller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="area", method={RequestMethod.GET, RequestMethod.POST})
public class AreaContraller {
	
	@Autowired
//	private HttpAgent httpAgent;
	
	@ResponseBody
	@RequestMapping(value="list")
	public String getJson() {
//		httpAgent.agent(requestAttribute, request, response);
		String rtnStr = "{'name': 'zhangsan', 'age': 23}";
		return rtnStr;
	}
}
