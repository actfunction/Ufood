package com.rh.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rh.api.service.ApiService;
//import com.rh.api.service.ApiServiceTest;
import com.rh.core.base.start.impl.ResourceUtil;

@Controller
@RequestMapping("/api")
public class ApiController {
	
	@Autowired
	private ApiService apiServiceTest;
	
	
	@RequestMapping("/index.html")
	public String index (HttpServletRequest request, HttpServletResponse response) {
		System.out.println(ResourceUtil.getKey("jdbcdriver"));
		return "index";
	}
	
	@RequestMapping("/index2")
	public String index2 (HttpServletRequest request, HttpServletResponse response) {
		System.out.println(ResourceUtil.getKey("jdbcdriver"));
//		CachingConfig cachingConfig = new CachingConfig();
//		cachingConfig.ehcache();
		return "index";
	}
	
	@RequestMapping("/index3")
	public String index3 (HttpServletRequest request, HttpServletResponse response) {
		System.out.println(ResourceUtil.getKey("jdbcdriver"));
		return "index";
	}
	
	@RequestMapping("/index4")
	public String index4 (HttpServletRequest request, HttpServletResponse response) {
		System.out.println(ResourceUtil.getKey("jdbcdriver"));
//		CachingConfig cachingConfig = new CachingConfig();
//		cachingConfig.ehcache();
		String key2 = "get a ";
		String rtxKey = apiServiceTest.testCache(key2);
		System.out.println(rtxKey);
		return rtxKey;
	}
}
