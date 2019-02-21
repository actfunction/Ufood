package com.rh.core.wfe.def;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流节点定义对象
 */
public class WfNodeDef extends Bean {
    /** 分组管理字段名称 **/
    public static final String[] GROUP_FIELDS = { "GROUP_DISPLAY", "GROUP_HIDE", "GROUP_EXPAND", "GROUP_COLLAPSE" };

    private static Log log = LogFactory.getLog(WfNodeDef.class);
    
    private static final long serialVersionUID = 2208621859642936684L;
    
    private List<Bean> bindingBeanList = new ArrayList<Bean>();
    
    private List<Bean> actDataBeanList = new ArrayList<Bean>();
    
    private Bean emergencyYiban = new Bean();

    private Bean emergencyJinji = new Bean();
    
    private Bean emergencyTeji = new Bean();
    
    
    /**
     * 字段更新表达式列表
     */
    private List<Bean> updateExpressList = new ArrayList<Bean>();
    
    /**
     * 字段控制类型：1 完全控制； 2 只读控制
     */
    private boolean entirelyControl = false;
    
    /**
     * '可编辑字段，可以是逗号分隔的多个字段的字符串。<br>
     * 需要和【字段控制类型】配合起来使用：<br>
     * 完全控制：可编辑字段不起作用，在当前节点可以修改任何数据项。<br>
     * 只读控制：可编辑字段起作用，所有不在可编辑字段范围内的数据项全部只读不可修改。<br>
     * ';
     */
    private String fieldException = "";
        
    /**
     * 流程节点必填字段设定，字段编号的列表，使用逗号分隔，在只读控制模式下设定为必填字段后必须将字段设为可编辑字段才可编辑，系统也会自动显示保存按钮，如果本身是隐藏字段则不再隐藏
     */
    private String fieldMust = "";
    
    /** 控制某些被隐藏的输入框是否显示，内容为逗号分隔的字段名称。如：FIELD_DISPLAY,FIELD_UPDATE **/
    private String fieldDisplay = "";
    
    /**
     * @return 是否完全控制
     */
    public boolean isEntirelyControl() {
        return entirelyControl;
    }
    
    /**
     * @param entirelyContro 是否完全控制
     */
    public void setEntirelyControl(boolean entirelyContro) {
        this.entirelyControl = entirelyContro;
    }
    
    /**
     * 文件类型的控制
     */
    private String fileControl = "";
    
    /**
     * @return 文件类型控制串
     */
    public String getFileControl() {
        return fileControl;
    }
    
    /**
     * @param aFileControl 文件类型控制串
     */
    public void setFileControl(String aFileControl) {
        this.fileControl = aFileControl;
    }
    
    /**
     * @return 必填字段
     */
    public String getFieldMust() {
        return fieldMust;
    }
    
    /**
     * @param aFieldMust 必填字段
     */
    public void setFieldMust(String aFieldMust) {
        this.fieldMust = aFieldMust;
    }
    
    /**
     * @return 可编辑字段
     */
    public String getFieldException() {
        return fieldException;
    }
    
    /**
     * @param afieldException 可编辑字段
     */
    public void setFieldException(String afieldException) {
        this.fieldException = afieldException;
    }
    
    /**
     * @return 隐藏字段
     */
    public String getFieldHidden() {
        return fieldHidden;
    }
    
    /**
     * @param afieldHidden 隐藏字段
     */
    public void setFieldHidden(String afieldHidden) {
        this.fieldHidden = afieldHidden;
    }
    
    /**
     * 隐藏字段
     */
    private String fieldHidden = "";
    
    /**
     * 表单按钮
     */
    private ArrayList<Bean> formButton = new ArrayList<Bean>();
    
    /**
     * 流程按钮
     */
    private ArrayList<Bean> wfButton = new ArrayList<Bean>();
    
    /**
     * @return 表单按钮
     */
    public ArrayList<Bean> getFormButton() {
        return formButton;
    }
    
    /**
     * @param button 表单按钮
     */
    public void addFormButton(Bean button) {
        formButton.add(button);
    }
    
    /**
     * @return 流程按钮
     */
    public ArrayList<Bean> getWfButton() {
        return wfButton;
    }
    
    /**
     * 
     * @return 节点上 紧急程度为一般的超时设置
     */
    public Bean getEmergencyYiban() {
        return emergencyYiban;
    }

    /**
     * 
     * @return 节点上 紧急程度为紧急的超时设置
     */
    public Bean getEmergencyJinji() {
        return emergencyJinji;
    }

    /**
     * 
     * @return 节点上 紧急程度为特急的超时设置
     */
    public Bean getEmergencyTeji() {
        return emergencyTeji;
    }
    
    /**
     * 
     * @param emerValue 紧急值
     * @return 超时设置的Bean
     */
    public Bean getEmerGency(int emerValue) {
        if (emerValue <= 10) {
            return emergencyYiban;
        } else if (emerValue <= 20) {
            return emergencyJinji;
        }
        return emergencyTeji;
    }
    
    /**
     * @param wfbutton 流程按钮
     */
    public void addWfButton(Bean wfbutton) {
        wfButton.add(wfbutton);
        log.debug("wfButton size = " + wfButton.size());
    }
    
    /**
     * @param aNodeDefBean 节点定义数据
     */
    public WfNodeDef(Bean aNodeDefBean) {
        this.copyFrom(aNodeDefBean);
        //避免按钮名称前后有空格
        String title = this.getStr("NODE_NAME").trim();
        this.set("NODE_NAME", title);
        
        initEmergency();
    }
    
    /**
     * 初始化紧急程度
     */
    private void initEmergency() {
        if (this.isNotEmpty("NODE_TIMEOUT") && this.getStr("NODE_TIMEOUT").indexOf("TIMEOUT") > 0) {
            String timeOutStr = this.getStr("NODE_TIMEOUT");
            List<Bean> timeOutList = JsonUtils.toBeanList(timeOutStr);
            
            for (Bean timeOut: timeOutList) {
                if (timeOut.getStr("TYPE").equals("YIBAN")) {
                    this.emergencyYiban = timeOut;
                } else if (timeOut.getStr("TYPE").equals("JINJI")) {
                    this.emergencyJinji = timeOut;
                } else if (timeOut.getStr("TYPE").equals("TEJI")) {
                    this.emergencyTeji = timeOut;
                }
            }
        }
    }

    /**
     * 查找本节点定义的节点变量的CODE
     * 
     * @param name TODO
     * @return TODO
     */
    public Bean findActDataBean(String name) {
        for (Bean actDataBean : actDataBeanList) {
            if (actDataBean.getStr("").equals(name)) {
                return actDataBean;
            }
        }
        return null;
    }
    
    /**
     * @return 取得本节点定义的组织资源
     */
    public List<Bean> getBindingFormList() {
        return bindingBeanList;
    }
    
    /**
     * @return 节点上定义的文件类型的 权限 ， 读/写
     */
    public List<Bean> getNodeFileAuth() {
        List<Bean> nodeFileAuthList = new ArrayList<Bean>();
        
        String fileAuthStr = this.getFileControl();
        fileAuthStr = fileAuthStr.replace("[", "");
        fileAuthStr = fileAuthStr.replace("]", "");
        fileAuthStr = fileAuthStr.replace("},{", "}{");
        for (String subStr : fileAuthStr.split("[{.*?}]")) {
            if (subStr.length() < 2) {
                continue;
            }
            
            Bean authBean = JsonUtils.toBean("{" + subStr + "}");
            
            boolean canRead = false;
            boolean canWrite = false;
            boolean canAdd = false;
            if (authBean.getInt("VALUE") >= 4) {
                canAdd = true;
                canRead = true;
                canWrite = true;
            } else if (authBean.getInt("VALUE") >= 2) {
                canRead = true;
                canWrite = true;
            } else if (authBean.getInt("VALUE") == 1) {
                canRead = true;
            }
            authBean.set("CANREAD", canRead);
            authBean.set("CANWRITE", canWrite);
            authBean.set("CANADD", canAdd);
            
            log.debug("节点上定义的文件类型的权限  " + authBean.toString());
            
            nodeFileAuthList.add(authBean);
        }
        
        return nodeFileAuthList;
    }
    
    /**
     * @return 操作数据
     */
    public List<Bean> getActDataList() {
        return actDataBeanList;
    }
    
    /**
     * @param bindingBean 绑定数据
     */
    protected void addBinding(Bean bindingBean) {
        this.bindingBeanList.add(bindingBean);
    }
    
    /**
     * @param actDataBean 操作数据
     */
    protected void addActData(Bean actDataBean) {
        actDataBeanList.add(actDataBean);
    }
    
    /**
     * @return 显示字段列表
     */
    public String getFieldDisplay() {
        return fieldDisplay;
    }
    
    /**
     * @param strFieldDisplay 设置显示字段
     */
    public void setFieldDisplay(String strFieldDisplay) {
        this.fieldDisplay = strFieldDisplay;
    }
    
    /**
     * @return 取得当进入节点时，字段更新表达式列表
     */
    public List<Bean> getUpdateExpressWhenEnter() {
        List<Bean> rtn = new ArrayList<Bean>();
        for (Bean bean : updateExpressList) {
            if (bean.getStr("UPDATE_MOMENT").equals("ENTER")) {
                rtn.add(bean);
            }
        }
        return rtn;
    }
    
    /**
     * @return 取得当节点办结时，需要更新的表达式
     */
    public List<Bean> getUpdateExpressWhenFinish() {
        List<Bean> rtn = new ArrayList<Bean>();
        for (Bean bean : updateExpressList) {
            if (bean.getStr("UPDATE_MOMENT").equals("FINISH")) {
                rtn.add(bean);
            }
        }
        return rtn;
    }
    
    /**
     * 
     * @return 取得当节点保存意见时，需要更新的表达式
     */
    public List<Bean> getUpdateExpressWhenMindSave() {
        List<Bean> rtn = new ArrayList<Bean>();
        for (Bean bean : updateExpressList) {
            if (bean.getStr("UPDATE_MOMENT").equals("MIND")) {
                rtn.add(bean);
            }
        }
        return rtn;
    }
    
    /**
     * 
     * @return 取得当节点保存意见时，需要更新的表达式
     */
    public List<Bean> getUpdateExpressWhenView() {
        List<Bean> rtn = new ArrayList<Bean>();
        for (Bean bean : updateExpressList) {
            if (bean.getStr("UPDATE_MOMENT").equals("VIEW")) {
                rtn.add(bean);
            }
        }
        return rtn;
    }
    
    /**
     * @param updateExpressList 字段更新表达式列表
     */
    public void setFieldUpdateExpressList(List<Bean> updateExpressList) {
        this.updateExpressList = updateExpressList;
    }
    
    /**
     *  @return 获取扩展办理人
     */
	public List<Bean> findCustomBeanList() {
		return getList(WfeConstant.CUSTOM_BL);
	}	
}
