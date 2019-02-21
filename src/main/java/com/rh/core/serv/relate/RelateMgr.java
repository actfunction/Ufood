package com.rh.core.serv.relate;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;

/**
 * 相关数据的工具类
 * @author wanglong
 */
public class RelateMgr {
	/**
	 * 根据服务主键和数据主键删除相关的关联服务
	 * @param servDef 服务定义
	 * @param dataId 数据主键
	 */
	public static void deleteRelate(ServDefBean servDef, String dataId) {
	    ParamBean param = new ParamBean(ServMgr.SY_SERV_RELATE, ServMgr.ACT_FINDS)
	        .set("SERV_ID", servDef.getSrcId()).set("DATA_ID", dataId);
	    List<Bean> relateList = ServMgr.act(param).getDataList();
	    param = new ParamBean(ServMgr.SY_SERV_RELATE, ServMgr.ACT_DELETE).setDeleteDatas(relateList);
	    ServMgr.act(param);
	}
	
	/**
     * 相关服务修改了主键信息
     * @param servDef 服务定义
     * @param oldId  原数据主键
     * @param newId  新数据主键
     */
    public static void updateRelate(ServDefBean servDef, String oldId, String newId) {
        //正向更新主键信息
        Bean whereBean =  new Bean().set("SERV_ID", servDef.getSrcId()).set("DATA_ID", oldId);
        Bean setBean = new Bean().set("DATA_ID", newId);
        ServDao.updates(ServMgr.SY_SERV_RELATE, setBean, whereBean);
        //反向更新主键信息
        whereBean =  new Bean().set("RELATE_SERV_ID", servDef.getSrcId()).set("RELATE_DATA_ID", oldId);
        setBean = new Bean().set("RELATE_DATA_ID", newId);
        ServDao.updates(ServMgr.SY_SERV_RELATE, setBean, whereBean);
    }
}
