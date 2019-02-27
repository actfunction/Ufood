package com.rh.api.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rh.api.service.ApiService;


@Service
public class ApiServiceImpl implements ApiService {
	
	@Cacheable(value="cacheTest",key="#key2")
	public String testCache(String key2) {
		System.out.println("key2:" + key2);
		String rtnKey = key2 + "testCacheKey";
		
		return rtnKey;
	}
}
