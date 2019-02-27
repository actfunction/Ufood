package com.rh.api.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.rh.api.service.ApiService;


public class EhCacheTestServiceTest extends SpringTestCase {

    @Autowired  
    private ApiService apiService;

    @Test
    public void getTimestampTest() throws InterruptedException{  
    	String key = "get a ";
        System.out.println("第一次调用：" + apiService.testCache(key));
        Thread.sleep(2000);
        System.out.println("2秒之后调用：" + apiService.testCache(key));
        Thread.sleep(11000);
        System.out.println("再过11秒之后调用：" + apiService.testCache(key));
    } 
}
