package com.rh.core.comm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.chart.DataViewServ;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.base.BaseServ;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Strings;
import com.rh.core.util.lang.ListHandler;
import com.rh.core.util.var.VarMgr;

/**
 * 通用信息服务类，支持不判断Sesion信息的获取，支持根据配置获取信息的规则设定。
 * 提供字典、配置、公共查询等
 * 
 * @author wanglong
 * 
 */
public class InfoServ extends BaseServ {

    /**
     * 获取配置信息
     * @param paramBean 参数bean
     * @return 配置bean
     */
    public Bean conf(Bean paramBean) {
        Bean bean = new Bean();
        String confCode = paramBean.getId();
        boolean showFlag = false;
        UserBean userBean = Context.getUserBean();
        if (userBean == null) { //无session需要判断是否在允许的配置中
            //不需要Session的字典列表
            String[] confs = Context.getSyConf("SY_COMM_CONF_NOSESION", "SY_SSO_URL").split(Constant.SEPARATOR);
            if (Strings.isin(confs, confCode)) { //要求请求的字段在配置列表内
                showFlag = true;
            }
        } else { //有Session可以直接获取字典
            showFlag = true;
        }
        //获取字典信息，支持PID和LEVEL层级
        if (showFlag) { //可以获取字典信息
        //单点登录服务器地址
            bean.set(confCode, Context.getSyConf(confCode, ""));
        }
        return bean;
    }
    
    /**
     * 获取字典信息，支持不需要Session的字段
     * @param paramBean 参数bean，主键_PK_：传入字段编码，目前支持SY_ORG_CMPY
     * @return 字典bean
     */
    public Bean dict(ParamBean paramBean) {
        Bean bean = new Bean();
        String dictId = paramBean.contains("DICT_ID") ? paramBean.getStr("DICT_ID") : paramBean.getId();
        if (dictId.length() == 0) {
            log.error("empty dict ID!");
            return bean;
        }
        boolean showFlag = false;
        UserBean userBean = Context.getUserBean();
        if (userBean == null) { //无session需要判断是否在允许的配置中
            //不需要Session的字典列表
            String[] dictIds = Context.getSyConf("SY_SERV_DICT_NOSESSION", "SY_ORG_CMPY").split(Constant.SEPARATOR);
            if (Strings.isin(dictIds, dictId)) { //要求请求的字段在配置列表内
                showFlag = true;
            }
        } else { //有Session可以直接获取字典
            showFlag = true;
        }
        //获取字典信息，支持PID和LEVEL层级
        if (showFlag) { //可以获取字典信息
            boolean bThread = false;
            if (!paramBean.isEmpty("_extWhere")) { //设置线程变量
                Context.setThread(DictMgr.THREAD_DICT_EXT_WHERE, paramBean.getStr("_extWhere"));
                bThread = true;
            }
            if (dictId.startsWith("@")) {
                bean = DictMgr.getDynaItemList(dictId.substring(1), paramBean);
            } else {
                Bean dict = DictMgr.getDict(dictId);
                if (dict != null) {
                    bean = dict.copyOf(new String[]{"DICT_ID", "DICT_NAME", "DICT_DIS_LAYER", "DICT_CHILD_ID",
                            "DICT_TYPE"});
                    String pId = paramBean.getStr("PID");
                    if (pId.length() > 0 && pId.startsWith("@")) { //系统变量
                        pId = ServUtils.replaceSysVars(pId);
                    }
                    //显示层级， 优先取外部参数，如果没有去字典定义配置，，如果没有则层级为0，取全部
                    int layer = paramBean.get("LEVEL", dict.getInt("DICT_DIS_LAYER"));
                    boolean showPid = paramBean.getBoolean("SHOWPID"); //支持设定输出信息是否带PID节点信息，缺省不带
                    bean.set(DictMgr.CHILD_NODE, DictMgr.getTreeList(dictId, pId, layer, showPid));
                }
            }
            if (bThread) { //恢复线程变量
                Context.setThread(DictMgr.THREAD_DICT_EXT_WHERE, null);
            }
        } else {
            bean.set(OutBean.TO_LOGIN, true);
        }
        return bean;
    }

    /**
     * 显示菜单树 获取用户自己的菜单： 
     *  1、从缓存中读取菜单对象； 
     *  2、如读不到，根据菜单时间判断； 
     *  3、如变化，重新生成菜单对象，放入缓存，并持久化； 
     *  4、如未变化，读取持久化的菜单，放入缓存
     * @param paramBean 参数bean
     * @return 菜单树Bean
     */
    public OutBean menu(final ParamBean paramBean) {
        OutBean menu = new OutBean();
        UserBean userBean = (UserBean) Context.getUserBean();
        if (userBean == null) { //用户必须登录才可以获取菜单
            return menu;
        }
        // 先从缓存获取菜单
        List<Bean> menuTree = UserMgr.getCacheMenuList(userBean.getCode());
        if (paramBean.isNotEmpty("PID")) { //设定了动态取一部分菜单信息
            DictMgr.handleTree(menuTree, new ListHandler<Bean>() {
                public void handle(Bean data) {
                    if (data.getStr("ID").equals(paramBean.getStr("PID"))) { //取当前节点下所有的子孙
                        paramBean.set(DictMgr.CHILD_NODE, data.getList(DictMgr.CHILD_NODE));
                        paramBean.set("PARENT_NODE", data);
                    }
                }
                
            });
            menuTree = paramBean.getList(DictMgr.CHILD_NODE);
        }
        if (!paramBean.contains(MenuServ.LEFTMENU)) {
            menu.set(MenuServ.TOPMENU, menuTree);
        } else {
            menu.set(MenuServ.LEFTMENU, menuTree);
        }
        menu.set("PARENT_NODE", paramBean.getBean("PARENT_NODE"));
        return menu;
    }
    
    /**
     * 根据数据展示的数据源配置获取数据
     * 1、数据源类型为“服务”
     *   服务配置格式：服务名.方法名.do?data={}，如：SY_ORG_USER.data.do?data={}
     *   如果未传递方法名和参数，则默认执行服务的query方法
     *   返回结果：
     *   调用的服务方法必须返回Bean对象，并且Bean中有一个key值为"_DATA_"的List<Bean>,List<Bean>的JSON字符串格式如下：
     *   [
     *    {"USER_NAME":"用户1","USER_HEIGHT":175},
     *    {"USER_NAME":"用户2","USER_HEIGHT":170},
     *    {"USER_NAME":"用户3","USER_HEIGHT":165}
     *   ]
     * 2、数据源类型为“SQL查询”
     *   SQL查询配置格式：合法的SQL查询语句
     * @param paramBean 参数bean
     * @return 数据Bean
     */
    public OutBean chart(ParamBean paramBean) {
        //跳转页面
        String jspName = "/sy/comm/dataview/view.jsp";
        String rCode = paramBean.getId();
        OutBean bean = new OutBean(DataViewServ.getDataViewDef(rCode)); //级联查询图标定义
        bean.setToDispatcher(jspName);
        String dsType = bean.getStr("DS_TYPE");
        String dsConf = bean.getStr("DS_CONF");
        List<Bean> items = bean.getList("SY_COMM_DATA_VIEW_ITEM");
        int rowCount = bean.get("DS_COUNT", 50);
        if (dsType.equals("SERV")) {
            OutBean resultBean = null;
            int indexDo = dsConf.indexOf(".do");
            //配置中有".do"字符串，说明配置了服务名和方法名
            if (indexDo != -1) {
                String[] mainUri = dsConf.substring(0, indexDo).split("\\.");

                //取得第一个?的位置
                int indexQ = dsConf.indexOf("data=");
                //存在参数
                ParamBean servParamBean;
                if (indexQ != -1) { //预定义了JSON格式的参数
                    servParamBean = new ParamBean(JsonUtils.toBean(VarMgr.replaceSysVar(dsConf.substring(indexQ + 5))));
                } else {
                    servParamBean = new ParamBean();
                }
                if (paramBean != null) { //增补paramBean参数
                    BeanUtils.trans(paramBean, servParamBean);
                }
                resultBean = ServMgr.act(mainUri[0], mainUri[1], servParamBean);
            } else {
                //只配置了服务名称，默认调用服务的query方法
                StringBuffer sb = new StringBuffer();
                for (Bean item : items) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(item.getStr("ITEM_CODE"));
                }
                paramBean.setSelect(sb.toString()).setQueryNoPageFlag(true).setShowNum(rowCount);
                resultBean = ServMgr.act(dsConf, ServMgr.ACT_QUERY, paramBean);
            }
            //将结果数据放入bean的data中
            bean.set("data", resultBean.getDataList());
        } else if (dsType.equals("SQL")) {
            // 替换系统级变量,格式如@变量@
            dsConf = VarMgr.replaceSysVar(dsConf);
            // 替换参数变量,格式如#变量#
            if (paramBean != null) {
                dsConf = BeanUtils.replaceValues(dsConf, paramBean);
            }
            // 判断是否是时间图，取数据项配置的第一行作为时间轴，转换为时间对应的毫秒数
            String viewType = bean.getStr("VIEW_TYPE");
            final boolean isDateView = viewType.equals("datetime") ? true : false;
            final Bean param = new Bean();
            for (Bean item : items) {
                if (isDateView && item.getStr("ITEM_TYPE").equals("X")) { //X轴为时间轴
                    param.set("DATE_ITEM", item.getStr("ITEM_CODE"));
                }
                if (item.isNotEmpty("DICT_ID")) {
                    Map<String, String> dicts;
                    if (!param.contains("DICTS")) {
                        dicts = new HashMap<String, String>();
                        param.set("DICTS", dicts);
                    } else {
                        dicts = param.getMap("DICTS");
                    }
                    dicts.put(item.getStr("ITEM_CODE"), item.getStr("DICT_ID"));
                }
            }
            // 查询数据
            bean.set("data", Transaction.getExecutor().queryPage(dsConf, 1, rowCount, new QueryCallback() {
                public void call(List<Bean> columns, Bean data) {
                    if (isDateView) { //时间图，特殊处理
                        String dateItem = param.getStr("DATE_ITEM");
                        data.set(dateItem, DateUtils.getCalendar(data.getStr(dateItem)).getTimeInMillis());
                    }
                    if (param.contains("DICTS")) {
                        Map<String, String> dicts = param.getMap("DICTS");
                        for (Bean column : columns) {
                            String code = column.getStr("NAME");
                            if (dicts.containsKey(code)) { //此列为字典列
                                String dictId = dicts.get(code);
                                data.set(code, DictMgr.getFullName(dictId, data.getStr(code))); //替换字典编码为名称
                            }
                        }
                    }
                }
            }));
        }
        return bean;
    }
    
    /**
     * 获取字典或服务的项值,并动态呈现供用户选择的表单
     * @param paramBean 参数 例如:{sdata=分, surl=PJ_PROJ_INFO_MGTYPE.show.do, 
     * column=ITEM_NAME, serv=SY_COMM_SUGGEST, _TRANS_=true, act=filterSuggest}
     * @return 返回 例如: {datas=[]}
     */
    public Bean filterSuggest(Bean paramBean) {
        List<String> datas = new ArrayList<String>();
        String url = paramBean.getStr("surl");
        String column = paramBean.getStr("column");
        String data = paramBean.getStr("sdata");
        String extwhere = paramBean.getStr("_extWhere");
        List<Bean> list = new ArrayList<Bean>();
        ParamBean b = new ParamBean();
        String servId = url.substring(0, url.indexOf("."));
        if (column.equals("ITEM_NAME") && !servId.equals("SY_SERV_ITEM")) { //调用字典数据
            b.set("DICT_ID", url.substring(0, url.indexOf(".")));
            list = new InfoServ().dict(b).getList("CHILD");
        } else { //调用系统链接
            ParamBean extBean = new ParamBean(url.substring(0, url.indexOf(".")), 
                    url.substring(url.indexOf(".") + 1, url.lastIndexOf(".")));
            extBean.set("_extWhere", extwhere);
            list = ServMgr.act(extBean).getDataList();
        }
        for (int i = 0; i < list.size(); i++) {
            Bean bean = list.get(i);
            if (bean.getStr(column).startsWith(data) && !data.equals(bean.getStr(column))) { //以输入值开头的内容
                datas.add(bean.getStr(column));
                if (datas.size() == 5) { //显示5项
                    break;
                }
            }
        }   
        Bean outBean = new Bean();
        outBean.put("datas", datas);
        return outBean;
    } 
}
