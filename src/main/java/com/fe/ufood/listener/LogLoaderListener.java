package com.fe.ufood.listener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.validation.Validation;

/**
 * 日志系统初始化监听器
 * @author  anran
 */
public class LogLoaderListener implements ServletContextListener {



    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String path = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
        PropertyConfigurator.configure(new StringBuffer(path).append("/conf/log4j.properties").toString());
        Logger logger = Logger.getRootLogger();
        logger.setLevel(Level.DEBUG);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
