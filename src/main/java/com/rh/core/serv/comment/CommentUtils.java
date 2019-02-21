package com.rh.core.serv.comment;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 评论公用类
 * @author wanglong
 *
 */
public class CommentUtils {
	/**
	 * 
	 * @param servId 服务ID
	 * @param dataId 数据ID
	 * @return 答案对应的评论列表
	 */
    public static List<Bean> getComments(String servId, String dataId) {
    	Bean queryBean = new Bean();
    	queryBean.set("SERV_ID", servId);
    	queryBean.set("DATA_ID", dataId);
    	queryBean.set(Constant.PARAM_ORDER, "S_CTIME DESC");
    	
    	List<Bean> comments = ServDao.finds(CommentServ.SY_SERV_COMMENT, queryBean);
    	for (Bean commBean: comments) {
    		UserBean userBean = UserMgr.getUser(commBean.getStr("S_USER"));
    		commBean.set("S_USER__NAME", userBean.getName());
    	}

    	return comments;
    }

    /**
     * 
     * @param dataId 数据ID
     * @param servId 服务ID
     * @return 评论条数
     */
    public static int getCommentsCount(String servId, String dataId) {
    	Bean queryBean = new Bean();
    	queryBean.set("SERV_ID", servId);
    	queryBean.set("DATA_ID", dataId);
    	
    	return ServDao.count(CommentServ.SY_SERV_COMMENT, queryBean);
    }
}
