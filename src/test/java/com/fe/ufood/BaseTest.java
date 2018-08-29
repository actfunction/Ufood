package com.fe.ufood;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/***
 *  配置SPRING和JUNIT整合，JUNIT启动时加载SPRINGIOC容器。
 * @author FE
 */
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉JUNIT Spring配置文件的位置
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class BaseTest {
	
}
