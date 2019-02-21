package com.rh.core.serv.bean;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * 流程服务参数扩展Bean
 * 
 * @author wanglong
 * 
 */
public class WfParamBean extends ParamBean {

    /** 流程启动 */
    public static final String ACT_START = "start";
    /** 流程删除 */
    public static final String ACT_DELETE = "delete";
    
    /** 流程状态：运行 */
    public static final int PROC_STATE_RUNNING = 1;
    /** 流程状态：已结束 */
    public static final int PROC_STATE_DONE = 2;
    
    /**
     * sid
     */
    private static final long serialVersionUID = -4064704032700817755L;

    /**
     * 对象构造方法
     */
    public WfParamBean() {
        super(ServMgr.SY_WFE_PROC);
    }

    /**
     * 对象构造方法
     * @param act 流程操作方法
     */
    public WfParamBean(String act) {
        super(ServMgr.SY_WFE_PROC, act);
    }
    
    /**
     * 设置流程操作方法
     * @param act 流程操作方法
     * @return 当前对象，用于级联设定
     */
    public WfParamBean setAct(String act) {
        set(Constant.PARAM_ACT_CODE, act);
        return this;
    }
    
    /**
     * 设置唯一Id
     * @param id 唯一Id
     * @return 当前对象，用于级联设定
     */
    public WfParamBean setId(String id) {
        set(KEY_ID, id);
        return this;
    }
    
    /**
     * 获取流程定义编码
     * @return 流程定义编码
     */
    public String getProcDefCode() {
        return this.getStr("_WF_PROC_DEF_CODE_");
    }
    
    /**
     * 设置流程定义编码
     * @param procDefCode 流程定义编码
     * @return 当前对象
     */
    public WfParamBean setProcDefCode(String procDefCode) {
        this.set("_WF_PROC_DEF_CODE_", procDefCode);
        return this;
    }
    
    /**
     * 获取流程实例编码
     * @return 流程实例编码
     */
    public String getProcInstCode() {
        return this.getStr("_WF_PROC_INST_CODE_");
    }
    
    /**
     * 设置流程实例编码
     * @param procInstCode 流程实例编码
     * @return 当前对象
     */
    public WfParamBean setProcInstCode(String procInstCode) {
        this.set("_WF_PROC_INST_CODE_", procInstCode);
        return this;
    }
    
    /**
     * 获取流程运行状态，1：在运行，2：已结束
     * @return 流程运行状态
     */
    public int getProcSate() {
        return this.getInt("_WF_PROC_STATE_");
    }
    
    /**
     * 设置流程运行状态，1：在运行，2：已结束
     * @param state 流程运行状态
     * @return 当前对象
     */
    public WfParamBean setProcState(int state) {
        this.set("_WF_PROC_STATE_", state);
        return this;
    }
    
    
    /**
     * 获取流程是否在运行状态，true：在运行，false：已结束
     * @return 流程是否在运行
     */
    public boolean getProcSateRunning() {
        return this.getInt("_WF_PROC_STATE_") == PROC_STATE_RUNNING;
    }
    
    /**
     * 获取流程定义信息
     * @return 流程定义信息
     */
    public Bean getProcDefBean() {
        return this.getBean("_WF_PROC_DEF_BEAN_");
    }
    
    /**
     * 设置流程定义信息
     * @param procDefBean 流程定义信息
     * @return 当前对象
     */
    public WfParamBean setProcDefBean(Bean procDefBean) {
        this.set("_WF_PROC_DEF_BEAN_", procDefBean);
        return this;
    }
    
    /**
     * 获取流程实例信息
     * @return 流程实例信息
     */
    public Bean getProcInstBean() {
        return this.getBean("_WF_PROC_INST_BEAN_");
    }
    
    /**
     * 设置流程实例信息
     * @param procInstBean 流程实例信息
     * @return 当前对象
     */
    public WfParamBean setProcInstBean(Bean procInstBean) {
        this.set("_WF_PROC_INST_BEAN_", procInstBean);
        return this;
    }
    
    /**
     * 获取流程对应的数据实体的服务Id
     * @return 数据实体的服务Id
     */
    public String getDataServId() {
        return this.getStr("_WF_DATA_SERV_ID_");
    }
    
    /**
     * 设置流程对应的数据实体的服务Id
     * @param dataServId 数据实体的服务Id
     * @return 当前对象
     */
    public WfParamBean setDataServId(String dataServId) {
        this.set("_WF_DATA_SERV_ID_", dataServId);
        return this;
    }
    
    /**
     * 获取流程对应的数据实体信息
     * @return 数据实体信息
     */
    public Bean getDataBean() {
        return this.getBean("_WF_DATA_BEAN_");
    }
    
    /**
     * 设置流程对应的数据实体信息
     * @param dataBean 数据实体信息
     * @return 当前对象
     */
    public WfParamBean setDataBean(Bean dataBean) {
        this.set("_WF_DATA_BEAN_", dataBean);
        return this;
    }

   /**
    * 设置对象，支持级联设置
    * @param key   键值
    * @param obj   对象数据
    * @return this，当前Bean
    */
   public WfParamBean set(Object key, Object obj) {
       put(key, obj);
       return this;
   }
}
