/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.send;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.entity.EntityMgr;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.wfe.util.WfTodoProvider;

/**
 * 分发服务 send service extends <CODE>CommonServ</CODE>
 * 
 * TODO 分发范围分级： 1，本部门内部;2，本单位;3，本单位 + 下级单位;4，本单位 + 上级单位;5，所有单位;
 * 
 * @author wanglong
 */
public class SendUserServ extends SendCommonServ {

    /** 等待发送 */
    private static final int WAIT_STATUS = 1;

    /** 发送状态 */
    private static final int SEND_STATUS = 2;
    
    /** 分发类型-阅件  **/
    private static final int SEND_TYPE_READ = 2;
    
    /** 分发强制提醒-否  **/
    private static final int SEND_FLAG_NO = 2;
    
    /** 系统配置：替换给外机构的分发待办服务ID **/
    private static final String SEND_TODO_REPLACE_OUT_SRV = "SEND_TODO_OUT_SRV";

    /** 系统配置：替换本机构的分发待办服务ID **/
    private static final String SEND_TODO_REPLACE_IN_SRV = "SEND_TODO_IN_SRV";
    
    /** 分发待办的CODE_NAME，默认为：分发 **/
    private static final String  SEND_TODO_CODE_NAME = "SY_SEND_TODO_CODE_NAME";

    /**
     * 根据工作流节点配置json信息自动分发 分两种情况： 
     * 1全自动分发，不再给用户展示待分发列表，而是直接分发
     * 2半自动分发，允许用户看完待分发列表并改动后再分发
     * @param paramBean 参数
     * @return OutBean
     */
    public OutBean autoSend(ParamBean paramBean) {
        if (!paramBean.contains("SERV_ID")) {
            throw new TipException("'SERV_ID' can not be null");
        }
        if (!paramBean.contains("DATA_ID")) {
            throw new TipException("'DATA_ID' can not be null");
        }
        
        // 取得当前分发用户的信息
        UserBean currentUser = getDoUserBean(paramBean);
        
        ParamBean sendBean = paramBean.copyOf();
        List<Bean> sendItemList = paramBean.getList("SEND_ITEM");
        sendBean.remove("SEND_ITEM");
        sendBean.remove("act");
        
        //根据分发人的Code删除待分发的页面
        removeWaitingItem(paramBean);
        
        for (Bean sendItem : sendItemList) {
            sendBean.set("SEND_ID", sendItem.getStr("sendId"));
            sendBean.set("SEND_TYPE", sendItem.get("read", SEND_TYPE_READ));
            sendBean.set("SEND_FLAG", sendItem.get("notify", SEND_FLAG_NO));
            sendBean.set("SEND_NUM", sendItem.get("num", 1));
            
            //取得接收用户
            ReceiverParser recv = new ReceiverParser();
            List<UserBean> users = recv.getUserBeanList(sendBean);
            
            //根据是否包含机构内标识处理用户
            if (paramBean.contains("includeSubOdept") && !paramBean.getBoolean("includeSubOdept")) {
                if (users.size() > 0) {
                    List<UserBean> delUsers = new ArrayList<UserBean>();
                    for (UserBean user : users) {
                        if (!user.getODeptCode().equals(currentUser.getODeptCode())) {
                            delUsers.add(user);
                        }
                    }
                    users.removeAll(delUsers);
                }
            }
          
            //发送机构内的用户
            List<Bean> datas = valueOfWaitSendList(sendBean, users);
            ServDao.creates(ServMgr.SY_COMM_SEND_DETAIL, datas);
        }
        
        String servId = paramBean.getStr("SERV_ID");
        ServDefBean servDef = ServUtils.getServDef(servId);
        if (servDef.containsItem("SEND_FLAG")) { //存在发送标识字段
            //表单 字段 配置的 分发
            if (paramBean.isNotEmpty("DATA_FROM") && paramBean.getStr("DATA_FROM").equals("FIELD_CONFIG")) {
                //发送之后， 将主单中的SEND_FLAG 字段的值，设置成1
                Bean updateBean = new Bean(paramBean.getStr("DATA_ID")).set("SEND_FLAG", Constant.YES_INT);
                ServDao.update(servId, updateBean);
            }
        }

        OutBean out = new OutBean();
        
        if (paramBean.getStr("sendMode").equals("semi_auto")) { //半自动
            return out.setOk();
        } else { // 全自动
            ServMgr.act(paramBean.getServId(), "send", paramBean);
            return out.setOk();
        }
    }

    @Override
    protected void beforeQuery(ParamBean paramBean) {
        String ifFirst = paramBean.getStr("ifFirst");
        
        if (ifFirst == null || !ifFirst.equals("yes")) {
            return;
        } else if (paramBean.getStr("sendMode").equals("semi_auto")) {
            //半自动的情况
            ServMgr.act(paramBean.getServId(), "autoSend", paramBean);
            return;
        }
        saveBeforeSendDtlList(paramBean);
    }

    /**
     * 保存待发送的数据
     * 
     * @param paramBean
     *            参数Bean
     */
    private void saveBeforeSendDtlList(ParamBean paramBean) {
        if (!paramBean.contains("SERV_ID")) {
            throw new TipException("'SERV_ID' can not be null");
        }
        if (!paramBean.contains("DATA_ID")) {
            throw new TipException("'DATA_ID' can not be null");
        }

        // 根据分发人的Code删除待分发的页面
        removeWaitingItem(paramBean);

        // 取得接收用户
        ReceiverParser recv = new ReceiverParser();
        List<UserBean> users = recv.getUserBeanList(paramBean);

        // 分发给指定用户
        List<Bean> datas = valueOfWaitSendList(paramBean, users);
        ServDao.creates(ServMgr.SY_COMM_SEND_DETAIL, datas);
    }

    /**
     * 移除用户等待发送的分发记录
     * 
     * @param paramBean
     *            参数Bean
     */
    private void removeWaitingItem(ParamBean paramBean) {
        // 取得当前分发用户的信息
        UserBean currentUser = getDoUserBean(paramBean);
        Bean sqlBean = new Bean();
        sqlBean.put("DATA_ID", paramBean.getStr("DATA_ID"));
        sqlBean.put("SEND_STATUS", WAIT_STATUS);
        sqlBean.put("S_USER", currentUser.getCode());

        ServDao.deletes(ServMgr.SY_COMM_SEND_DETAIL, sqlBean);
    }

    /**
     * 将待分发列表的分发记录发送出去，然后发送待办
     * 
     * @param paramBean
     *            参数Bean
     * @return 查询结果
     */
    public OutBean send(Bean paramBean) {
        String dataId = paramBean.getStr("DATA_ID");
        String servId = paramBean.getStr("SERV_ID");
        // create where condition
        Bean setBean = new Bean().set("SEND_STATUS", SEND_STATUS);
        setBean.set("SEND_TIME", DateUtils.getDatetimeTS());
        Bean whereBean = new Bean();
        whereBean.set("DATA_ID", dataId);
        whereBean.set("SERV_ID", servId);
        whereBean.set("SEND_STATUS", WAIT_STATUS);
        UserBean currentUser = Context.getUserBean();
        whereBean.set("S_USER", currentUser.getCode());
        // get wait send data
        List<Bean> waitSendList = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL,
                whereBean);

        String outServId = getOutServId(servId);
        String inServId = getInServId(servId);
        String codeName = Context.getSyConf(SEND_TODO_CODE_NAME, "分发");
        
        //文件编号
        Bean entityBean = EntityMgr.getEntity(dataId);
        String dataCode = entityBean.getStr("ENTITY_CODE");
        for (Bean sendDetail : waitSendList) {
            Bean updateBean = setBean.copyOf();
            updateBean.setId(sendDetail.getId());

            TodoBean todo = new TodoBean();
            todo.setSender(sendDetail.getStr("S_USER"));
            todo.setOwner(sendDetail.getStr("RECV_USER"));
            todo.setDataCode(dataCode); //编号
            todo.setCode(servId);
            todo.setCodeName(codeName);
            todo.setObjectId1(dataId);
            todo.setCatalog(sendDetail.getInt("SEND_TYPE")); // 待办的类型，1,办件，2，阅件
            todo.setObjectId2(sendDetail.getId()); // TODO_OBJECT_ID2 中存 分发的ID

            if (sendDetail.getStr("RECV_TYPE").equals(SendConstant.OUTSIDE)) {
                todo.setServId(outServId);
                updateBean.set("SERV_ID", outServId);
                todo.setUrl(outServId + ".byid.do?data={_PK_:" + dataId
                        + ",MODE:FENFA,SEND_ID:" + sendDetail.getId() + "}");
                //todo.setCatalog(TodoUtils.TODO_CATLOG_BAN);
                todo.setTitle("来自" + currentUser.getODeptName() + "："
                        + entityBean.getStr("TITLE"));
            } else {
                todo.setServId(inServId);
                updateBean.set("SERV_ID", inServId);
                todo.setUrl(inServId + ".byid.do?data={_PK_:" + dataId
                        + ",MODE:FENFA,SEND_ID:" + sendDetail.getId() + "}");
                todo.setTitle(entityBean.getStr("TITLE"));
            }

            TodoUtils.insert(todo);

            ServDao.update(ServMgr.SY_COMM_SEND_DETAIL, updateBean);
        }

        return new OutBean().setOk();
    }

    /**
     * 
     * @param servId
     *            原服务ID
     * @return 替换给外机构的分发待办服务ID
     */
    private String getInServId(String servId) {
        return Context.getSyConf(SEND_TODO_REPLACE_IN_SRV + "." + servId,
                servId);
    }

    /**
     * 
     * @param servId
     *            原服务ID
     * @return 替换给本机构的分发待办服务ID
     */
    private String getOutServId(String servId) {
        return Context.getSyConf(SEND_TODO_REPLACE_OUT_SRV + "." + servId,
                servId);
    }

    /**
     * 跳转至分发列表页面。 如果用户已选择分发方案，我们将自动提取分发方案用户，并保存为待分发人员 待分发人员：并未真正发送，状态为待分发。
     * 
     * @param paramBean
     *            参数Bean
     * @return out Bean
     */
    public OutBean showSend(ParamBean paramBean) {
        if (!paramBean.contains("SERV_ID")) {
            throw new TipException("'SERV_ID' can not be null");
        }
        if (!paramBean.contains("DATA_ID")) {
            throw new TipException("'DATA_ID' can not be null");
        }
        String schemeId = paramBean.getStr("SCHEME_ID");
        if (0 < schemeId.length()) {
            // get scheme details data
            List<Bean> details = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL,
                    new Bean().set("SCHEME_ID", schemeId));
            // set into scheme bean
            setOutput(paramBean, details);

            // update wait send users
            // updateWaitSendUsers(paramBean);
            saveBeforeSendDtlList(paramBean);
        }
        String jspName = "/sy/comm/send/showSend.jsp";
        OutBean outBean = new OutBean();
        outBean.setToDispatcher(jspName);
        outBean.set("PARAM_BEAN", paramBean);
        return outBean;
    }

    /**
     * 阅件签收，更新分发的记录(分发的ID, ) , 取消待办
     * 
     * @param paramBean
     *            参数bean
     * @return 返回前台Bean
     */
    public OutBean cmQianShou(ParamBean paramBean) {
        // 修改分发的接收时间
        String sendId = paramBean.getStr("SEND_ID");
        if (StringUtils.isEmpty(sendId)) {
            throw new TipException("没取到分发ID,不能签收");
        }

        // 签收人
        UserBean doUser = getDoUserBean(paramBean);

        SendUtils.qianShou(sendId, doUser);

        OutBean rtnBean = new OutBean();
        rtnBean.set("rtnstr", "success");

        return rtnBean;
    }

    /**
     * 转发
     * 
     * @param paramBean
     *            参数bean
     * @return 返回前台Bean
     */
    public OutBean cmZhuanFa(ParamBean paramBean) {
        // 往分发表中加入数据
        this.saveBeforeSendDtlList(paramBean);
        OutBean rtnBean = this.send(paramBean);
        rtnBean.setOk();

        return rtnBean;
    }

    /**
     * 退回 ， 1，更新分发记录的rtn字段(SEND_ID)， 2,完成该条待办(SEND_ID,SERV_ID,DATA_ID) ，
     * 3,给分发人发送消息说，退回了
     * 
     * @param paramBean
     *            参数bean
     * @return 返回前台Bean
     */
    public OutBean cmTuiHui(ParamBean paramBean) {
        UserBean userBean = getDoUserBean(paramBean);
        // 往分发表中加入数据
        String sendId = paramBean.getStr("SEND_ID");
        if (StringUtils.isEmpty(sendId)) {
            throw new TipException("没取到分发ID,不能退回");
        }

        Bean sendDtl = SendUtils.getSendDetailById(sendId);

        boolean isTrue = SendUtils.isReciver(sendDtl, userBean.getId())
                && SendUtils.isNotFinished(sendDtl);
        if (!isTrue) {
            throw new TipException("没有权限做此操作");
        }

        // 更新分发记录的rtn字段
        SendUtils.tuihui(sendDtl, userBean);

        // 给发送人发待办说已经退回了 , 需要SEND_ID , DOC_ID , SERV_ID
        String todoContent = paramBean.getStr("TODO_CONTENT");
        WfTodoProvider.sendToDoForTuihui(sendDtl, userBean, todoContent);

        OutBean rtnBean = new OutBean();
        rtnBean.set("rtnstr", "success");

        return rtnBean;
    }

    /**
     * value of wait send detail list --机构内
     * 
     * @param paramBean
     *            参数Bean
     * @param usersList
     *            userBean list
     * @return <code>List<Bean></code>
     */
    private List<Bean> valueOfWaitSendList(ParamBean paramBean,
            List<UserBean> usersList) {
        List<Bean> dataList = new ArrayList<Bean>();
        for (UserBean user : usersList) {
            Bean bean = createWaitSendDetail(paramBean, user);
            dataList.add(bean);
        }
        return dataList;
    }

    /**
     * create wait send detail data bean --机构内
     * 
     * @param paramBean
     *            参数bean
     * @param userBean
     *            receive user
     * @return bean
     */
    private Bean createWaitSendDetail(ParamBean paramBean, UserBean userBean) {
        // 取得当前分发用户的信息
        UserBean currentUser = getDoUserBean(paramBean);

        String recvUser = userBean.getCode();
        String recvUname = userBean.getName();
        String recvDept = userBean.getDeptCode();
        String recvDname = userBean.getFullDeptNames();
        Bean bean = new Bean();
        bean.set("SERV_ID", paramBean.getStr("SERV_ID"));
        bean.set("DATA_ID", paramBean.getStr("DATA_ID"));
        bean.set("RECV_USER", recvUser);
        bean.set("RECV_UNAME", recvUname);
        bean.set("RECV_DEPT", recvDept);
        bean.set("RECV_DNAME", recvDname);
        bean.set("SEND_STATUS", WAIT_STATUS);
        bean.set("SEND_NUM", paramBean.get("SEND_NUM", 1));
        bean.set("SEND_TYPE", paramBean.getInt("SEND_TYPE"));
        bean.set("RECV_ODEPT", userBean.getODeptCode());
        bean.set("RECV_TDEPT", userBean.getTDeptCode());
        bean.set("RECV_TNAME", userBean.getTDeptName());
        bean.set("MIND_CODE", paramBean.getStr("MIND_CODE"));

        if (userBean.getODeptCode().equals(currentUser.getODeptCode())) {
            bean.set("RECV_TYPE", SendConstant.INSIDE);
        } else {
            bean.set("RECV_TYPE", SendConstant.OUTSIDE);
        }

        return bean;
    }

    /**
     * @param sendBean
     *            分发Bean
     */
    public static void updateSend(Bean sendBean) {
        ServDao.update(ServMgr.SY_COMM_SEND_DETAIL, sendBean);
    }

    @Override
    protected void afterByid(ParamBean paramBean, OutBean outBean) {
        super.afterByid(paramBean, outBean);

        outBean.copyFrom(paramBean);
    }
    
    /**
     * 批量分发审批单
     * @param paramBean 参数
     * @return 是否成功
     */
    public OutBean batchAutoSend(ParamBean paramBean) {
        if (!paramBean.contains("SERV_IDS")) {
            throw new TipException("'SERV_IDS' can not be null");
        }
        if (!paramBean.contains("DATA_IDS")) {
            throw new TipException("'DATA_IDS' can not be null");
        }

        String[] servIds = paramBean.getStr("SERV_IDS").split("@@");

        String[] dataIds = paramBean.getStr("DATA_IDS").split("@@");
        
        if (servIds.length != dataIds.length) {
            throw new TipException("参数错误，SERV_IDS 与DATA_IDS 数量不相等。");
        }

        for (int i = 0; i < servIds.length; i++) {
            final String servId = servIds[i];
            final String dataId = dataIds[i];

            ParamBean tempParam = new ParamBean(paramBean);
            tempParam.set("SERV_ID", servId);
            tempParam.set("DATA_ID", dataId);
            this.autoSend(tempParam);
        }

        OutBean out = new OutBean();
        out.setOk();
        return out;
    }

}
