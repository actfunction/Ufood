package com.fe.ufood.contraller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="area", method={RequestMethod.GET, RequestMethod.POST})
public class AreaContraller {
	
	
	
	@ResponseBody
	@RequestMapping(value="list")
	public String getJson() {
		String rtnStr = "{'name': 'zhangsan', 'age': 23}";
		return rtnStr;
	}
}
