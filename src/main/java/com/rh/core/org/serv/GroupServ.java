/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.org.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * 群组服务
 * @author wanglong
 * 
 */
public class GroupServ extends CommonServ {

    private static final String SITE_SERVICE = "CM_SITE";

    private static final String SY_ORG_GROUP = ServMgr.SY_ORG_GROUP;

    private static final String SY_ORG_GROUP_USER = ServMgr.SY_ORG_GROUP_USER;

    /**
     * 根据站点查询群组成员
     * @param paramBean - 参数(站点主键)
     * @return 成员列表
     */
    public Bean getGroupUsers(Bean paramBean) {
        // 查询群组
        Bean groupBean = getGroup(paramBean);

        // 查询群组成员
        String where = " AND GROUP_CODE='" + groupBean.getId() + "'";
        ParamBean userQuery = new ParamBean(SY_ORG_GROUP_USER);
        userQuery.set("GROUP_CODE", groupBean.getId()).setQueryPageOrder("GU_ADMIN asc").setQueryExtWhere(where);
        OutBean result = new GroupUserServ().query(userQuery);
        List<Bean> users = result.getDataList();
        for (Bean user : users) {
            String code = user.getStr("USER_CODE");
            // 将用户头像加入
            UserBean u = UserMgr.getUser(code);
            String uImg = u.getStr("USER_IMG");
            user.set("USER_IMG", uImg);
        }
        return result;
    }

    /**
     * 查询群组公告
     * @param paramBean - 站点参数bean(主键为站点ID)
     * @return - 公告
     */
    public Bean getGroupNotice(Bean paramBean) {
        // 查询群组
        Bean groupBean = getGroup(paramBean);

        Bean queryBean = new Bean();
        queryBean.set("SERV_ID", SY_ORG_GROUP);
        queryBean.set("DATA_ID", groupBean.getId());
        Bean notice = ServDao.find("SY_ORG_GROUP_NOTICE", queryBean);
        return notice;
    }

    /**
     * 获取群组
     * @param paramBean - 站点参数(主键为站点ID)
     * @return - 站点所对应的群组
     */
    private Bean getGroup(Bean paramBean) {
        String groupServ = SY_ORG_GROUP;
        Bean queryBean = new Bean();
        if (paramBean.isNotEmpty("SITE_ID")) {
            queryBean.setId(paramBean.getStr("SITE_ID"));
        } else {
            queryBean.setId(paramBean.getId());
        }
        
        queryBean.set("SERV_ID", groupServ);
        Bean groupSite = ServDao.find(SITE_SERVICE, queryBean);
        if (null == groupSite || groupSite.isEmpty()) {
            throw new TipException("group site not found, site id:" + paramBean.getId());
        }

        String groupId = groupSite.getStr("DATA_ID");
        // 查询群组
        Bean groupBean = ServDao.find(groupServ, new Bean().setId(groupId));
        if (null == groupBean || groupBean.isEmpty()) {
            throw new TipException("group not found, group id:" + groupId);
        }
        return groupBean;
    }
    
    /**
     * 根据站点ID取得群组信息、群组成员、群组公告
     * @param paramBean 带有站点ID的Bean
     * @return 回复群组Bean
     */
    public Bean getGroupMessage(Bean paramBean) {
        Bean outBean = new Bean();
        outBean.set("group", this.getGroup(paramBean));
        outBean.set("notice", this.getGroupNotice(paramBean));
        outBean.set("users", this.getGroupUsers(paramBean));
        return outBean;
    }
    
    /**
     * 服务于群组导航组件
     * @param paramBean 带有站点ID的参数Bean
     * @return 取得群组名称、群组新闻数目、群组文档数目、群组主题数目
     */
    public Bean getGroupNav(Bean paramBean) {
        Bean outBean = new Bean();
        
        outBean.set("group", this.getGroup(paramBean));
        outBean.set("doc_num", ServDao.count("CM_SITE_WENKU",
                new Bean().set("SITE_ID", paramBean.getId()).set("DOCUMENT_STATUS", 2)
                .set("SITE_ID", paramBean.getStr("SITE_ID"))));
        outBean.set("news_num", ServDao.count("CM_SITE_NEWS", 
                new Bean().set("SITE_ID", paramBean.getId()).set("NEWS_CHECKED", 1)
                .set("SITE_ID", paramBean.getStr("SITE_ID"))));
        outBean.set("topic_num", ServDao.count("CM_SITE_BBS", 
                new Bean().set("SITE_ID", paramBean.getId()).set("TOPIC_CHECKED", 1)
                .set("SITE_ID", paramBean.getStr("SITE_ID"))));
        
        return outBean;
    }
}
