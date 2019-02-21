package com.rh.core.base.db.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.DS;

/**
 * 通用的数据库连接服务类，用于集成quartz分布式任务
 * 
 * @author wanglong
 * @version $Id$
 */
public class CommonConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() throws SQLException {
        return ((DataSource) BaseContext.getDSBean().get(DS.DS)).getConnection();
    }

    @Override
    public void shutdown() throws SQLException {
        // TODO Auto-generated method stub
        
    }
	
}