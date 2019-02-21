package com.rh.core.comm.todo;

import com.rh.core.base.Bean;
import com.rh.core.comm.todo.TodoUtils.ToDoItem;

/**
 * 
 * 保存待办时，传递数据给TodoUtils类的数据载体，方便用户的调用。
 * @author wanglong
 * 
 */
public class TodoBean extends Bean {
    /**
     * 
     */
    private static final long serialVersionUID = 7481668986663558234L;
    
    private static final String IS_COPY_ENTITY  = "_IsCopyEntity";
    
    /**
     * 构造函数
     */
    public TodoBean() {
        super();
    }
    
    /**
     * 构造函数
     * 
     * @param bean 数据对象里面的KEY-VALUE都会复制到新对象中
     */
    public TodoBean(Bean bean) {
        super(bean);
    }
    

    /**
     * 
     * @param servId 服务ID
     */
    public void setServId(String servId) {
        this.set(ToDoItem.SERV_ID, servId);
    }

    /**
     * 
     * @return 服务ID
     */
    public String getServId() {
        return this.getStr(ToDoItem.SERV_ID);
    }

    /**
     * 
     * @param title 待办标题
     */
    public void setTitle(String title) {
        this.set(ToDoItem.TODO_TITLE, title);
    }

    /**
     * 
     * @return 待办标题
     */
    public String getTitle() {
        return this.getStr(ToDoItem.TODO_TITLE);
    }

    /**
     * 
     * @param code 待办类型编码
     */
    public void setCode(String code) {
        this.set(ToDoItem.TODO_CODE, code);
    }

    /**
     * 
     * @return 待办类型编码
     */
    public String getCode() {
        return this.getStr(ToDoItem.TODO_CODE);
    }

    /**
     * 
     * @param codeName 待办类型名称
     */
    public void setCodeName(String codeName) {
        this.set(ToDoItem.TODO_CODE_NAME, codeName);
    }

    /**
     * @return 待办类型名称
     */
    public String getCodeName() {
        return this.getStr(ToDoItem.TODO_CODE_NAME);
    }

    /**
     * 
     * @param url 待办URL
     */
    public void setUrl(String url) {
        this.set(ToDoItem.TODO_URL, url);
    }

    /**
     * 
     * @return 待办URL
     */
    public String getUrl() {
        return this.getStr(ToDoItem.TODO_URL);
    }

    /**
     * 
     * @param userCode 发送人用户编码
     */
    public void setSender(String userCode) {
        this.set(ToDoItem.SEND_USER_CODE, userCode);
    }

    /**
     * 
     * @return 发送人用户编码
     */
    public String getSender() {
        return this.getStr(ToDoItem.SEND_USER_CODE);
    }

    /**
     * 
     * @param userCode 办理人USER_ID
     */
    public void setOwner(String userCode) {
        this.set(ToDoItem.OWNER_CODE, userCode);
    }

    /**
     * 
     * @return 办理人USER_ID
     */
    public String getOwner() {
        return this.getStr(ToDoItem.OWNER_CODE);
    }

    /**
     * 
     * @param emergency 缓急
     */
    public void setEmergency(int emergency) {
        this.set(ToDoItem.S_EMERGENCY, emergency);
    }

    /**
     * 
     * @return 缓急
     */
    public String getEmergency() {
        return this.getStr(ToDoItem.S_EMERGENCY);
    }

    /**
     * 
     * @param catalog 分类：待办 1/待阅 2/消息 3
     */
    public void setCatalog(int catalog) {
        this.set(ToDoItem.TODO_CATALOG, catalog);
    }

    /**
     * 
     * @return 分类：待办 1/待阅 2/消息 3
     */
    public int getCatalog() {
        return this.getInt(ToDoItem.TODO_CATALOG);
    }

    /**
     * @param dataId 与SERV_ID对应的主数据ID
     */
    public void setObjectId1(String dataId) {
        this.set(ToDoItem.TODO_OBJECT_ID1, dataId);
    }

    /**
     * 
     * @return 与SERV_ID对应的主数据ID
     */
    public String getObjectId1() {
        return this.getStr(ToDoItem.TODO_OBJECT_ID1);
    }

    /**
     * 
     * @param objectId2 辅数据ID，用于区分待办
     */
    public void setObjectId2(String objectId2) {
        this.set(ToDoItem.TODO_OBJECT_ID2, objectId2);
    }

    /**
     * 
     * @return 辅数据ID，用于区分待办
     */
    public String getObjectId2() {
        return this.getStr(ToDoItem.TODO_OBJECT_ID2);
    }

    /**
     * 
     * @param deadline1 办理期限
     */
    public void setDeadline1(String deadline1) {
        this.set(ToDoItem.TODO_DEADLINE1, deadline1);
    }

    /**
     * 
     * @return 办理期限
     */
    public String getDeadline1() {
        return this.getStr(ToDoItem.TODO_DEADLINE1);
    }

    /**
     * 
     * @param flag 代理状态，1：可代理；2：不可代理
     */
    public void setBenchFlag(int flag) {
        this.set(ToDoItem.TODO_BENCH_FLAG, flag);
    }

    /**
     * 
     * @return 代理状态，1：可代理；2：不可代理
     */
    public int getBenchFlag() {
        return this.getInt(ToDoItem.TODO_BENCH_FLAG);
    }

    /**
     * 
     * @param content 待办提示消息内容
     */
    public void setContent(String content) {
        this.set(ToDoItem.TODO_CONTENT, content);
    }

    /**
     * 
     * @return 待办提示消息内容
     */
    public String getContent() {
        return this.getStr(ToDoItem.TODO_CONTENT);
    }

    /**
     * 
     * @param operation 待办的办理提示，如：请批示，请核稿等
     */
    public void setOperation(String operation) {
        this.set(ToDoItem.TODO_OPERATION, operation);
    }

    /**
     * 
     * @return 待办的办理提示，如：请批示，请核稿等
     */
    public String getOperation() {
        return this.getStr(ToDoItem.TODO_OPERATION);
    }
    
    /**
     * 
     * @param dataCode 编号
     */
    public void setDataCode(String dataCode) {
        this.set(ToDoItem.SERV_DATA_CODE, dataCode);
    }
    
    /**
     * 
     * @return 编号
     */
    public String getDataCode() {
        return this.getStr(ToDoItem.SERV_DATA_CODE);
    }
    
    /**
     * 
     * @param from 待办的办理提示，如：请批示，请核稿等
     */
    public void setFrom(String from) {
        this.set(ToDoItem.TODO_FROM, from);
    }

    /**
     * 
     * @return 待办的办理提示，如：请批示，请核稿等
     */
    public String getFrom() {
        return this.getStr(ToDoItem.TODO_FROM);
    }
    
    /**
     * 
     * @return 是否强制创建Entity实例，默认值为true。
     */
    public boolean isCopyEntity() {
        if (!this.contains(IS_COPY_ENTITY)) { //未设置则取默认值。
            return true;
        }
        return this.getBoolean(IS_COPY_ENTITY);
    }
    
    /**
     * 
     * @param isCreateEntity 是否强制创建Entity实例
     */
    public void setCopyEntity(boolean isCreateEntity) {
        this.set(IS_COPY_ENTITY, isCreateEntity);
    }

    /**
     * 起草人ID
     */
    public void setDraftUser(String draftUser) {
        this.set(ToDoItem.DRAFT_USER, draftUser);
    }

    /**
     * 起草时间
     */
    public void setDraftTime(String draftTime) {
        this.set(ToDoItem.DRAFT_TIME, draftTime);
    }

    /**
     * 起草人姓名
     */
    public void setDraftUserName(String draftUserName) {
        this.set(ToDoItem.DRAFT_UNAME, draftUserName);
    }

    /**
     * 起草部门ID
     */
    public void setDraftDept(String draftDept) {
        this.set(ToDoItem.DRAFT_DEPT, draftDept);
    }

    /**
     * 起草部门名称
     */
    public void setDraftDeptName(String draftDeptName) {
        this.set(ToDoItem.DRAFT_DNAME, draftDeptName);
    }

    /**
     * 上一个环节名称
     */
    public void setPreOptName(String preOptName) {
        this.set(ToDoItem.PRE_OPT_NAME, preOptName);
    }
}
