package com.rh.core.comm.msg;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.util.lang.Assert;

/**
 * 待办提醒发送功能
 * @author wanglong
 * 
 */
public class TodoMsgSender extends AbstractMsgSender {

    @Override
    public void sendMsg(Bean msgBean) {
        String sendUserCode = msgBean.getStr(MsgItem.SEND_USER);
        if (sendUserCode.isEmpty()) {
            sendUserCode = Context.getSyConf("SY_COMM_REMIND_TODO_SENDER", "");
        }
        
        UserBean userBean = (UserBean) UserMgr.getUser(sendUserCode);

        TodoBean todoBean = new TodoBean();

        todoBean.setTitle(msgBean.getStr(MsgItem.REM_TITLE));
        todoBean.setSender(userBean.getCode());

        todoBean.setCode("TODO_REMIND");
        // 取得服务ID
        String servId = msgBean.getStr(MsgItem.SERV_ID);
        todoBean.setServId(servId);
        // 取得服务名称
        todoBean.setCodeName("待办提醒");
        
        todoBean.setUrl(msgBean.getStr(servId + ".showDialog.do"));
        todoBean.setEmergency(msgBean.getInt(MsgItem.S_EMERGENCY));
        todoBean.setObjectId1(msgBean.getStr(MsgItem.DATA_ID));
        todoBean.setObjectId2(msgBean.getStr(MsgItem.DATA_ID));
        if (msgBean.isNotEmpty(MsgItem.REM_CONTENT)) {
            todoBean.setContent(msgBean.getStr(MsgItem.REM_CONTENT));
        }
        
        todoBean.setCatalog(TodoUtils.TODO_CATLOG_MSG);
        // todoBean.set(TODO_ITEM.TODO_CATALOG, ToDoUtils.TODO_CATLOG_MSG);

        List<UserBean> receivers = msgBean.getList(MsgItem.RECEIVER_LIST);
        Assert.notNull(receivers, "接收人列表" + MsgItem.RECEIVER_LIST + "的值不能为NULL");

        /**
         * 设置接收人
         */
        for (UserBean rUserBean : receivers) {
            TodoBean newToDoBean = new TodoBean(todoBean.copyOf());
            newToDoBean.setOwner(rUserBean.getCode());
            try {
                // 加入标识，通知此待办是由提醒触发的，待办发完后不再发提醒，防止嵌套循环
                newToDoBean.set("remindFlag", true);
                TodoUtils.insert(newToDoBean);
                super.addSuccessExecResult(rUserBean.getCode(), "");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                super.addFailtureExecResult(rUserBean.getCode(), "");
            }
        }
    }
}
