package com.rh.core.util.http;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.comm.CacheMgr;

/**
 * 页面缓存过滤器，需要在web.xml中配置启用。 
 * 采用ehCache实现缓存处理机制。
 * @author wanglong
 * 
 */
public class PageCacheFilter extends SimplePageCachingFilter {
    /** log */
    private static Log log = LogFactory.getLog(PageCacheFilter.class);

    /**
     * 扩展采用系统设定的的缓存实例。
     * @return EhCacheManager实例
     */
    @Override
    protected CacheManager getCacheManager() {
        return (CacheManager) CacheMgr.getInstance().getCacheProvider().getCacheMgr();
    }
    
    @Override
    protected void doFilter(final HttpServletRequest request, 
            final HttpServletResponse response, final FilterChain chain) throws Exception {
        boolean cacheFlag = !Context.isDebugMode();  //非调试模式启动缓存处理
        if (cacheFlag) {
            log.debug(request.getRequestURL() + "     =>FROM CACHE!");
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
