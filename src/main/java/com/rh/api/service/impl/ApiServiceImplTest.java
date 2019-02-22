package com.rh.api.service.impl;

import org.springframework.cache.annotation.Cacheable;

import com.rh.api.service.ApiServiceTest;

public class ApiServiceImplTest implements ApiServiceTest {

	@Cacheable(value="cacheTest",key="#key")
	public String testCache(String key) {
		System.out.println("进入testCacheImpl方法了");
		return key + "cacheTest";
	}

}
