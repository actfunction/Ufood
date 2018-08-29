package com.fe.ufood.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fe.ufood.BaseTest;
import com.fe.ufood.dao.AreaDao;
import com.fe.ufood.entity.Area;

public class AreaDaoTest extends BaseTest {

	@Autowired
	private AreaDao areaDao;

	@Test
	public void testQeryArea() {
		List<Area> areaList = areaDao.queryArea();
		assertEquals(2, areaList);
	}
}
