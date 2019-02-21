package com.rh.core.base;

import java.io.File;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.util.Constant;

/**
 * 启动listener类，用于系统环境总体初始化
 * @author wanglong
 * @version $id$
 */
public class ContextListener implements ServletContextListener {

    /**
     * 初始化系统
     * @param sce 存放于WEB.XML中的配置信息
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 加载配置参数
        System.out.println(".........................................................");
        System.out.println("正在启动系统 ... ...");
        ServletContext sc = sce.getServletContext();
        // 获取系统真实路径
        String systemPath = sc.getRealPath("/");
        
        if (!systemPath.endsWith(File.separator)) {
            systemPath += Constant.PATH_SEPARATOR;
        }
        
        String contextPath = sc.getContextPath();
        if (contextPath.equals("/")) {
            contextPath = "";
        } else if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        // 获取系统resources真实路径
        String resourcesPath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
        
        System.out.println("系统工作目录: " + systemPath);
        System.out.println("系统服务路径: " + contextPath);
        Bean paramBean = new Bean();
        String name;
        // 获取StartupServlet的配置信息
        Enumeration<String> names = sc.getInitParameterNames();
        while (names.hasMoreElements()) {
            name = names.nextElement();
            paramBean.set(name, sc.getInitParameter(name));
        }
        paramBean.set(APP.SYSPATH, systemPath).set(APP.CONTEXTPATH, contextPath)
        .set(APP.RESOURCESPATH, resourcesPath).set(APP.WEBINF, systemPath  + "WEB-INF" 
                + Constant.PATH_SEPARATOR);
        BaseContext.start(paramBean);
        System.out.println("系统初始化完毕，开始接收请求！");
        System.out.println(".........................................................");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        BaseContext.stop();
    }
}
