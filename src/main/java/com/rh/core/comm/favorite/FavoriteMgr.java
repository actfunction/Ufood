package com.rh.core.comm.favorite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * @author wanglong
 *
 */
public class FavoriteMgr {
    // 日志记录
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(FavoriteMgr.class);
    
    private static FavoriteMgr instance = new FavoriteMgr();
    
    /**
     * Singleton
     * @return - Singleton instance
     */
    public static FavoriteMgr getInstance() {
        return instance;
    }

    /**
     * can not new instance
     */
    private FavoriteMgr() {
    }
    
    /**
     * 添加收藏
     * @param paramBean - 传入参数
     */
    public void favorite(ParamBean paramBean) {
        ServDao.save(ServMgr.SY_COMM_FAVORITES, paramBean);
    }
    /**
     * 取消收藏
     * @param paramBean - 传入参数
     * @return true 删除成功 ，false删除失败
     */
    public boolean unFavorite(ParamBean paramBean) {
        return ServDao.deletes(ServMgr.SY_COMM_FAVORITES, paramBean) > 1 ? true : false;
    }
}
