package com.rh.core.serv.comment;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.RequestUtils;

/**
 * 
 * @author wanglong
 * 
 */
public class CommentServ extends CommonServ {
    /**
     * 评论服务
     */
    public static final String SY_SERV_COMMENT = "SY_SERV_COMMENT";

    /**
     * 评论投票服务
     */
    private static final String SY_SERV_COMMENT_VOTE = "SY_SERV_COMMENT_VOTE";
    

    /**
     * 保存前设置评论编号
     * @param paramBean - 参数Bean
     */
    protected void beforeSave(ParamBean paramBean) {
        String servId = paramBean.getStr("SERV_ID");
        String dataId = paramBean.getStr("DATA_ID");
        if (0 == servId.length()) {
            throw new TipException(" SERV_ID can not be null!");
        }
        if (0 == dataId.length()) {
            throw new TipException(" DATA_ID can not be null!");
        }
        int max = getCurrentNumber(servId, dataId);
        paramBean.set("C_NUMBER", max + 1);
    }
    
   
    /**
     * 
     * @param paramBean 参数Bean
     * @return 评论list
     */
    public OutBean getCommentList(ParamBean paramBean) {
        Bean page = new Bean();
        page.set("SHOWNUM", 3);
        if (!paramBean.isEmpty("SHOWNUM")) {
            page.set("SHOWNUM", paramBean.getInt("SHOWNUM"));
        }
        if (!paramBean.isEmpty("PAGES")) {
            page.set("PAGES", paramBean.getInt("PAGES"));
        }
        if (!paramBean.isEmpty("ALLNUM")) {
            page.set("ALLNUM", paramBean.getInt("ALLNUM"));
        }
        if (!paramBean.isEmpty("NOWPAGE")) {
            page.set("NOWPAGE", paramBean.getInt("NOWPAGE"));
        }
        if (!paramBean.isEmpty("ORDER")) {
            page.set("ORDER", paramBean.getStr("ORDER"));
        }
        paramBean.set("_PAGE_", page);

        StringBuilder strWhere = new StringBuilder();
        if (!paramBean.isEmpty("_extWhere")) {
            strWhere.append(paramBean.getStr("_extWhere"));
        }
        strWhere.append(" and SERV_ID = '");
        strWhere.append(paramBean.getStr("SERV_ID"));
        strWhere.append("' and DATA_ID = '");
        strWhere.append(paramBean.getStr("DATA_ID"));
        strWhere.append("'");

        paramBean.set("_extWhere", strWhere.toString());

        OutBean result = super.query(paramBean);
        /*List<Bean> list = result.getDataList();
        List<UserBean> userList = new ArrayList<UserBean>();
        for (Bean comment : list) {
            String quoteContent = comment.getStr("C_QUOTE_CONTENT");
            // Pattern p = Pattern.compile(",user=(.*?),][quote=");
            Pattern p = Pattern.compile(",user=(.*?)]");
            Matcher m = p.matcher(quoteContent);
            while (m.find()) {
                String usercode = m.group(1); 
             // 如果不存在放入userList
                UserBean user = UserMgr.getUser(usercode);
                comment.set("REPLY_TO_USER", user);
                if (!isExits(userList, usercode)) {
                    if (null != user) {
                        userList.add(user);
                    }
                }
                
            }
        }
        
        //添加当前用户
        UserBean currentUser = Context.getUserBean();
        if (!isExits(userList, currentUser.getCode())) {
            userList.add(currentUser);
        }
        
        result.set("COMMENT_USERS", userList);*/
        return result;
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 答案对应的评论列表
     */
    public OutBean getCommentsByAid(ParamBean paramBean) {
        Bean queryBean = new Bean();
        queryBean.set("SERV_ID", paramBean.getStr("SERV_ID"));
        queryBean.set("DATA_ID", paramBean.getStr("DATA_ID"));
        queryBean.set(Constant.PARAM_ORDER, "S_CTIME ASC");

        List<Bean> comments = ServDao.finds(SY_SERV_COMMENT, queryBean);
        for (Bean commBean : comments) {
            UserBean userBean = UserMgr.getUser(commBean.getStr("S_USER"));
            commBean.set("S_USER__NAME", userBean.getName());
        }

        OutBean rtnBean = new OutBean();
        rtnBean.set("rtnBean", comments);

        return rtnBean;
    }

    /**
     * 
     * @param dataId 数据ID
     * @param servId 服务ID
     * @return 评论条数
     */
    public static int getCommentsCount(String dataId, String servId) {
        Bean queryBean = new Bean();
        queryBean.set("DATA_ID", dataId);
        queryBean.set("SERV_ID", servId);

        return ServDao.count(SY_SERV_COMMENT, queryBean);
    }

    /**
     * 修改回帖
     * @param param - 参数bean
     * @return out bean
     */
    public OutBean updateReply(ParamBean param) {
        Bean current = ServDao.find(SY_SERV_COMMENT, param);
        current.set("C_CONTENT", param.getStr("C_CONTENT"));
        
        setContent(current);
        param.set("C_QUOTE_CONTENT", current.getStr("C_QUOTE_CONTENT"));
        param.set("C_CONTENT", current.getStr("C_CONTENT"));
        
        ServDao.update(SY_SERV_COMMENT, param);
        return new OutBean().setOk();
    }

    /**
     * 回帖
     * @param param - 参数bean
     * @return out bean
     */
    public OutBean reply(ParamBean param) {
        String servId = param.getStr("SERV_ID");
        String dataId = param.getStr("DATA_ID");
        if (0 == servId.length()) {
            throw new TipException(" SERV_ID can not be null!");
        }
        if (0 == dataId.length()) {
            throw new TipException(" DATA_ID can not be null!");
        }

        int max = getCurrentNumber(servId, dataId);
        param.set("C_NUMBER", max + 1);

        setContent(param);

        ServDao.save(SY_SERV_COMMENT, param);
        return new OutBean().setOk();
    }

    /**
     * 回帖
     * @param param - 参数bean
     * @return out bean
     */
    public OutBean getCommentUser(ParamBean param) {
        String userCode = param.getId();
        OutBean user = new OutBean(UserMgr.getUser(userCode));
        return user;
    }

    /**
     * 目标用户是否存在于目标用户列表
     * @param userList - 用户列表
     * @param userCode - 目标用户code
     * @return - 是否已存在
     */
    protected boolean isExits(List<UserBean> userList, String userCode) {
        for (UserBean user : userList) {
            if (user.getCode().equals(userCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新 引用内容、评论内容字段
     * @param comment - comment bean
     */
    private void setContent(Bean comment) {
        String replyId = comment.getStr("REPLY_TO");
        String content = comment.getStr("C_CONTENT");
        
        String replyStartTag = "#回复";
        String replyEndTag = "楼#";
        boolean isReply = false;
        if (-1 < content.indexOf(replyStartTag) && -1 < content.indexOf(replyEndTag)) {
            content = content.substring(content.indexOf(replyEndTag) + replyEndTag.length());
            isReply = true;
        }
        if (null != replyId && 0 < replyId.length() && isReply) {
            ParamBean queryBean = new ParamBean(SY_SERV_COMMENT);
            queryBean.setId(replyId);
            OutBean srcComment = this.byid(queryBean);
            if (srcComment.isEmpty()) {
                // setError(param, "指定回复的评论不存在!");
                // return param;
                comment.set("C_QUOTE_CONTENT", "");
            } else {
                // 构造引用内容 
                StringBuilder quoteBuilder = new StringBuilder();
                quoteBuilder.append("[quote=");
                quoteBuilder.append(srcComment.getStr("C_NUMBER") + "楼");
                quoteBuilder.append(",time=");
                quoteBuilder.append(srcComment.getStr("S_MTIME"));
                quoteBuilder.append(",user=");
                quoteBuilder.append(srcComment.getStr("S_USER"));
                quoteBuilder.append(",username=");
                quoteBuilder.append(srcComment.getStr("S_UNAME"));
                quoteBuilder.append("]");
                quoteBuilder.append(srcComment.getStr("C_QUOTE_CONTENT"));
                quoteBuilder.append(srcComment.getStr("C_CONTENT"));
                quoteBuilder.append("[/quote]");
                
                comment.set("C_QUOTE_CONTENT", quoteBuilder.toString());
            }
        }
        comment.set("C_CONTENT", content);
    }

    /**
     * 支持数量 +1
     * @param param - 参数bean
     * @return out bean
     */
    public OutBean increaseLikevote(ParamBean param) {
        OutBean outBean = new OutBean();
        String commentId = param.getId();
        // 检查该用户是否已投票
        if (isVoted(commentId)) {
            outBean.setError("您已参与投票！");
            return outBean;
        }
        // 更新支持票数
        String key = "LIKE_VOTE";
        Bean topic = ServDao.find(SY_SERV_COMMENT, param);
        topic.set(key, topic.get(key, 0) + 1);
        ServDao.update(SY_SERV_COMMENT, topic);

        // 更新投票记录
        Bean vote = new Bean();
        vote.set("VOTE_VALUE", 1);
        vote.set("VOTE_IP", RequestUtils.getIpAddr(Context.getRequest()));
        if (null != Context.getUserBean() && null != Context.getUserBean().getId()) {
            vote.set("VOTE_USER", Context.getUserBean().getCode());
        }
        vote.set("COMMENT_ID", commentId);
        ServDao.save(SY_SERV_COMMENT_VOTE, vote);
        return outBean.setOk();
    }

    /**
     * 反对数量 +1
     * @param param - 参数bean
     * @return out bean
     */
    public OutBean increaseDislikevote(ParamBean param) {
        OutBean outBean = new OutBean();
        String commentId = param.getId();
        // 检查该用户是否已投票
        if (isVoted(commentId)) {
            outBean.setError("您已参与投票！");
            return outBean;
        }

        // 更新反对票数
        String key = "DISLIKE_VOTE";
        Bean topic = ServDao.find(SY_SERV_COMMENT, param);
        topic.set(key, topic.get(key, 0) + 1);
        ServDao.update(SY_SERV_COMMENT, topic);

        // 更新投票记录
        Bean vote = new Bean();
        vote.set("VOTE_VALUE", 2);
        vote.set("VOTE_IP", RequestUtils.getIpAddr(Context.getRequest()));
        if (null != Context.getUserBean() && null != Context.getUserBean().getId()) {
            vote.set("VOTE_USER", Context.getUserBean().getCode());
        }
        vote.set("COMMENT_ID", commentId);
        ServDao.save(SY_SERV_COMMENT_VOTE, vote);
        return outBean.setOk();
    }

    /**
     * 获取当前最大评论编号
     * @param servId - 服务ID
     * @param dataId - 数据项ID
     * @return 最大评论编号
     */
    protected int getCurrentNumber(String servId, String dataId) {
        Bean queryBean = new Bean();
        queryBean.set("SERV_ID", servId);
        queryBean.set("DATA_ID", dataId);
        queryBean.set(Constant.PARAM_SELECT, "max(C_NUMBER) MAX_");
        Bean resultBean = ServDao.find(SY_SERV_COMMENT, queryBean);
        int max = resultBean.get("MAX_", 0);
        return max;
    }

    /**
     * 当前访问者是否已进行目标评论投票
     * @param commentId - commentId
     * @return - this user voted?
     */
    protected boolean isVoted(String commentId) {
        String ip = RequestUtils.getIpAddr(Context.getRequest());
        UserBean user = Context.getUserBean();
        boolean result = false;
        // 验证IP
        result = ipVoted(commentId, ip);
        if (!result) {
            return result;
        }
        // 验证用户
        result = userVoted(commentId, user);
        return result;
    }

    /**
     * vate ip check, if this ip voted we return true
     * @param commentId - comment id
     * @param user - user bean
     * @return this user voted?
     */
    protected boolean userVoted(String commentId, UserBean user) {
        Bean vote = new Bean();
        vote.set("COMMENT_ID", commentId);
        vote.set("VOTE_USER", user.getCode());
        List<Bean> list = ServDao.finds(SY_SERV_COMMENT_VOTE, vote);
        if (0 == list.size()) {
            return false;
        }
        return true;
    }

    /**
     * vate ip check, if this ip voted we return true
     * @param commentId - comment id
     * @param ip - ip addr
     * @return this ip voted?
     */
    protected boolean ipVoted(String commentId, String ip) {
        Bean vote = new Bean();
        vote.set("COMMENT_ID", commentId);
        vote.set("VOTE_IP", ip);
        List<Bean> list = ServDao.finds(SY_SERV_COMMENT_VOTE, vote);
        if (0 == list.size()) {
            return false;
        }
        return true;
    }

}
