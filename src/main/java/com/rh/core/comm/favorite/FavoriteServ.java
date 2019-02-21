package com.rh.core.comm.favorite;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * 收藏： 问答收藏、共享文档收藏、OA文档收藏
 * @author wanglong
 *
 */
public class FavoriteServ extends CommonServ {

    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        super.afterSave(paramBean, outBean);
        //1.保存标签
        
        //获取当前用户
        UserBean userBean = Context.getUserBean();
        String[] tags     = paramBean.getStr("TAGS").split(" ");
        
        StringBuilder sql = new StringBuilder();
                      sql.append(" AND S_USER='" + userBean.getCode() + "'")
                         .append(" AND MARK_LEVEL='PRIVATE' ");
        ParamBean query = new ParamBean(ServMgr.SY_COMM_FAVORITES_MARK, ServMgr.ACT_FINDS);
        
        for (String tag : tags) {
            if ("".equals(tag)) {
                continue;
            }
            query.setWhere(sql.toString() + " AND MARK_NAME  like '%" + tag + "%' ");
            List<Bean> list = ServMgr.act(query).getDataList(); 
            String markId = "";
            if (list.size() > 0) { //如果已存在标签，取出 MARK_ID
                markId = list.get(0).getStr("MARK_ID");
            } else { //如果不存在，则保存标签，并返回 MARK_ID
                ParamBean markBean =  new ParamBean();
                          markBean.set("MARK_NAME", tag);
                          markBean.set("MARK_LEVEL", "PRIVATE");
                OutBean out = ServMgr.act(ServMgr.SY_COMM_FAVORITES_MARK, ServMgr.ACT_SAVE, markBean);
                markId = out.getStr("MARK_ID");
            }
            
            if (!"".equals(markId)) { //保存 收藏-标签
                ParamBean bean =  new ParamBean();
                bean.set("FAVORITE_ID", outBean.getStr("FAVORITE_ID"));
                bean.set("MARK_ID", markId);
                ServMgr.act(ServMgr.SY_COMM_FAVORITES_MARK, ServMgr.ACT_SAVE, bean);
            }
        }
    }
    
    @Override
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        super.afterDelete(paramBean, outBean);
      //获取收藏ID
        String favoriteId = paramBean.getId();
        
        ParamBean query = new ParamBean(ServMgr.SY_COMM_FAVORITES_MARK , ServMgr.ACT_FINDS);
                  query.set("FAVORITE_ID", favoriteId);
        OutBean out = ServMgr.act(query);
        List<Bean> list = out.getDataList();
        if (list.size() > 0) {
            Bean tempBean = new Bean();
            tempBean.set("FAVORITE_ID", favoriteId);
            //删除  收藏-标签
            ServDao.deletes(ServMgr.SY_COMM_FAVORITES_MARK, tempBean);
            
            query.remove("FAVORITE_ID");
            for (Bean bean : list) {
                //获取标签ID  
                String markId = bean.getStr("MARK_ID");
                query.set("MARK_ID", markId);
                out = ServMgr.act(query);
                //如果在关系表中不存在markId，则删除
                if (out.getDataList().size() == 0) {
                    Bean markBean = new Bean();
                    markBean.set("MARK_ID", markId);
                    ServDao.deletes(ServMgr.SY_COMM_FAVORITES_MARK, markBean);
                }
            }
        }
    }

}
