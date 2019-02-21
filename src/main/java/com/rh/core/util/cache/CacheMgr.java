package com.rh.core.util.cache;

import java.util.List;

import org.springframework.cache.annotation.EnableCaching;  
import org.springframework.context.annotation.Configuration;  

import com.rh.core.base.Context;
import com.rh.core.util.Lang;

@Configuration  
@EnableCaching//启用缓存  
public class CacheMgr {

    /** 缓存管理器静态实例。 */
    private static CacheMgr cacheManager = null;
    /** 缓存服务提供者 */
    private static ICacheProvider cacheProvider = null;
    /**
     * 获取CacheMgr的实例.
     * 
     * @return CacheMgr的实例
     */
    public static CacheMgr getInstance() {
        if (cacheManager == null) {
            cacheManager = new CacheMgr();
            cacheProvider = (ICacheProvider) Lang.createObject(ICacheProvider.class, 
                    Context.getInitConfig("rh.cache", "com.rh.core.util.cache.EhCacheProvider"));
        }
        return cacheManager;
    }

    /**
     * 关闭缓存.
     */
    public static void shutdown() {
        cacheProvider.shutdown();
    }

    /**
     * 根据缓存类型清除缓存数据。
     * 
     * @param cacheType 缓存类型/缓存名称
     */
    public void clearCache(String cacheType) {
        cacheProvider.clear(cacheType);
    }

    /**
     * 根据指定的缓存类型和key获取缓存对象
     * 
     * @param key 缓存的key
     * @param cacheType 缓存类型/缓存名称
     * @return 缓存的对象
     */
    public Object get(String key, String cacheType) {
        return cacheProvider.get(cacheType, key);
    }

    /**
     * 根据指定的缓存类型获取对应key的列表
     * 
     * @param cacheType 缓存类型/缓存名称
     * @return 缓存对象KEY列表
     */
    public List<String> getKeyList(String cacheType) {
        return cacheProvider.getKeyList(cacheType);
    }

    /**
     * 将数据写入缓存。
     * 
     * @param key 缓存的key
     * @param object 缓存的对象
     * @param cacheType 缓存类型/缓存名称
     */
    public void set(String key, Object object, String cacheType) {
        cacheProvider.set(cacheType, key, object);
    }

    /**
     * 将数据写入缓存。
     * 
     * @param key 缓存的key
     * @param object 缓存的对象
     * @param cacheType 缓存类型/缓存名称
     * @param timeToLiveSeconds 缓存存活时间(秒)
     */
    public void set(String key, Object object, String cacheType, int timeToLiveSeconds) {
        cacheProvider.set(cacheType, key, object, timeToLiveSeconds);
    }

    /**
     * 在指定的缓存中清除数据.
     * 
     * @param key 缓存的key
     * @param cacheType 缓存类型/缓存名称
     */
    public void remove(String key, String cacheType) {
        cacheProvider.delete(cacheType, key);
    }

    /**
     * 获取缓存服务器的实例。
     * @return 缓存服务器实例
     */
    public ICacheProvider getCacheProvider() {
        return cacheProvider;
    }

    /**
     * 
     * @param cacheType 缓存对象名称
     * @return 取得指定缓存对象的数据量
     */
    public int getSize(String cacheType) {
        return cacheProvider.getSize(cacheType);
    }

    /**
     * 
     * @param cacheType 缓存对象名称
     * @param offset 从那个位置开始取，从0开始
     * @param size 取多少条记录
     * @return 从指定缓存中取得数据列表
     */
    @SuppressWarnings("rawtypes")
    public List getDataList(String cacheType, int offset, int size) {
        return cacheProvider.getValueList(cacheType, offset, size);
    }
}
