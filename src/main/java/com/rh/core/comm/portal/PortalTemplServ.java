package com.rh.core.comm.portal;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;

/**
 * 首页portal对应服务类
 * @author wanglong
 */
public class PortalTemplServ  extends CommonServ implements PortalListenInterface {
    /** 相关文件 */
    public static final String SY_COMM_TEMPL = "SY_COMM_TEMPL";
    /** 个人门户 */
    public static final String PT_TYPE_USER_CODE = "4";
    
    /**
     * 首页portal管理页面
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean show(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        outBean.setToDispatcher("/sy/comm/home/portalTempl.jsp");
        outBean.set("_PT_PARAM_", paramBean);
        return outBean; 
    }
    
    /**
     * 首页portal，将覆盖portalOA方法，新的加载方法
     * @param paramBean 含有模版定义bean+额外参数($字段$)
     * @return 传出的参数
     */
    public Bean getPortal(ParamBean paramBean) {
        Bean outBean = null;
        try {
            outBean = new Bean();
            //参数监听处理
            String expStr = paramBean.getStr("PT_PARAM"); //对应规则设定参数
            if (expStr.length() > 0) {
                Bean item = JsonUtils.toBean(expStr);
                String lisStr = item.getStr("LISTENER");
                PortalListenInterface listener = this.createListener(lisStr);
                if (listener != null) {
                    paramBean = listener.beforeInputParamBean(paramBean);
                }
            }
            //默认返回默认模板
            String allStr = ParsePortal.getTempl(paramBean);
            outBean.set("PORTAL", allStr.replaceAll("\r\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outBean;
    }
    /**
     * 获取区块的数据内容，将覆盖portalArea方法
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public Bean getPortalArea(Bean paramBean) {
        Bean outBean = null;
        try {
            outBean = new Bean();
            Bean comBean = new Bean();
            Bean selfParamBean = new Bean();
            if (paramBean.getStr("PC_ID").length() > 0) {
                comBean = ServDao.find("SY_COMM_TEMPL_COMS", paramBean.getStr("PC_ID"));
                String selfParam = comBean.getStr("PC_SELF_PARAM"); //对应个性参数定义
                if (selfParam.length() > 0) {
                    List<Bean> paramList = JsonUtils.toBeanList(selfParam);
                    for (Bean item : paramList) { //处理组合值字段
                        String itemCode = item.getStr("id");
                        //wangchen modify-begin
                        if (item.get("value") instanceof Bean) {
                            Bean value = item.getBean("value");
                            if (value != null) {
                                selfParamBean.set(itemCode, value);
                            } else {
                                selfParamBean.set(itemCode, new Bean());
                            }
                        } else {
                            String value = item.getStr("value");
                            if (value.length() > 0) {
                                selfParamBean.set(itemCode, value);
                            } else {
                                selfParamBean.set(itemCode, "");
                            }
                        }                      
                        //wangchen modify-end
                    }
                }
            }
            if (paramBean.getStr("PT_PARAM").length() > 0) { //传递的参数有模版级定义的参数，将覆盖组件本身参数
                Bean ptParamBean = JsonUtils.toBean(paramBean.getStr("PT_PARAM"));
                selfParamBean.putAll(ptParamBean);
            }
            ParsePortal.fixCustomFilePath(comBean);
            String comHtml = "";
            if (comBean.getStr("PC_DATA").indexOf("<") == 0) { //自定义html区块
                comHtml = ParsePortal.transSelfContent(comBean);
            } else {  //构造html区块
                selfParamBean.putAll(paramBean);
                comBean.set("PC_DATA", //替换$$类的变量
                        ParsePortal.replaceDataUrl(comBean.getStr("PC_DATA"), new Bean(), selfParamBean));
                comHtml  = ParsePortal.transComms(comBean, selfParamBean);
            }
            outBean.set("AREA", comHtml.replaceAll("\r\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outBean;
    }
    /**
     * 新建后特殊处理含类型参数的处理
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (paramBean.getStr("_TYPE").length() > 0) {
            if (paramBean.getStr("_TYPE").equals(PT_TYPE_USER_CODE)) { //个人
                ParamBean userBean = new ParamBean(ServMgr.SY_ORG_USER, ServMgr.ACT_SAVE);
                userBean.setId(outBean.getStr("S_USER")).set("PT_ID", outBean.getId());
                ServMgr.act(userBean);               
            }
        }
    }
    /**
     * 删除之后同步更新对应用户的对应模版编码
     * @param paramBean 参数信息
     * @param outBean 输出信息 
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
          List<Bean> dataList = outBean.getDataList(); 
          for (Bean data : dataList) {
              if (data.getStr("PT_TYPE_ATTRIBUTE").equals(PT_TYPE_USER_CODE)
                      && data.getStr("S_USER").length() > 0) {
                  ParamBean userBean = new ParamBean(ServMgr.SY_ORG_USER, ServMgr.ACT_SAVE);
                  userBean.setId(data.getStr("S_USER")).set("PT_ID", "");
                  ServMgr.act(userBean); 
              }
          }
    }
    /**
     * @param listener 对应的监听类全名
     * @return 创建监听对象
     */
    private PortalListenInterface createListener(String listener) {
        if (listener.length() == 0) {
            return null;
        }
        return (PortalListenInterface) Lang.createObject(PortalListenInterface.class, listener);
    }
    /**
     * 参数传递之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @return paramBean 处理后的参数
     */
    public ParamBean beforeInputParamBean(ParamBean paramBean) {
        return paramBean;
    }
}
