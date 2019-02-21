package com.rh.core.serv;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.comm.CacheMgr;
import com.rh.core.org.UserBean;
import com.rh.core.org.auth.acl.mgr.DataAclMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.serv.util.ServUtils.SERV_DEF;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;
import com.rh.core.util.file.FileHelper;

/**
 * 服务定义扩展Bean
 * 
 * @author wanglong
 * 
 */
public class ServDefBean extends Bean {
    /** log */
    private static Log log = LogFactory.getLog(ServDefBean.class);

    /**
     * sid
     */
    private static final long serialVersionUID = -6509118814674233395L;

    /**
     * 私有化构造方法
     */
    @SuppressWarnings("unused")
    private ServDefBean() {

    }

    /**
     * 对象构造方法
     * 
     * @param servDefbean 数据对象
     */
    public ServDefBean(Bean servDefbean) {
        super(servDefbean);
    }

    /**
     * 获取服务定义对应的实现类
     * @return 服务定义实现类
     */
    public Object getServClass() {
        return this.get(SERV_DEF.$CLASS);
    }

    /**
     * 获取所有服务项（包含自定义服务项）
     * @return 所有服务项
     */
    public LinkedHashMap<String, Bean> getAllItems() {
        return this.getLinkedMap(SERV_DEF.$ITEMS);
    }

    /**
     * 获取所有操作方法的集合
     * @return 所有操作方法集合
     */
    public LinkedHashMap<String, Bean> getAllActs() {
        return this.getLinkedMap(SERV_DEF.$ACTS);
    }

    /**
     * 获取所有操作方法的集合
     * @return 所有操作方法集合
     */
    public LinkedHashMap<String, Bean> getAllLinks() {
        return this.getLinkedMap(SERV_DEF.$LINKS);
    }

    /**
     * 获取所有常用查询
     * @return 常用查询集合
     */
    public LinkedHashMap<String, Bean> getAllQueries() {
        return this.getLinkedMap(SERV_DEF.$QUERIES);
    }

    /**
     * 获取所有常用查询
     * @return 常用查询集合
     */
    public List<Bean> getAllWheres() {
        return this.getList(ServUtils.TABLE_SERV_WHERE);
    }

    /**
     * 获取所有视图和表类型服务项（一般用于查询）
     * @return 所有服务项
     */
    public List<Bean> getViewItems() {
        return this.getList(SERV_DEF.$ITEMS_VIEW);
    }

    /**
     * 获取所有表类型服务项（一般用于插入和修改）
     * @return 所有服务项
     */
    public List<Bean> getTableItems() {
        return this.getList(SERV_DEF.$ITEMS_TABLE);
    }

    /**
     * 获取所有文件上传服务项（字段级别文件上传）
     * @return 所有服务项
     */
    public List<Bean> getFileFieldItems() {
        return this.getList(SERV_DEF.$FILE_FIELD_ITEMS);
    }

    /**
     * 获取所有文件上传服务项（自定义附件项）
     * @return 所有服务项
     */
    public List<Bean> getFileSelfItems() {
        return this.getList(SERV_DEF.$FILE_SELF_ITEMS);
    }

    /**
     * 获取所有设定了组合值的服务项列表
     * @return 组合值的服务项列表
     */
    public List<Bean> getCombineItems() {
        return this.getList(SERV_DEF.$COMBINE_ITEMS);
    }

    /**
     * 获取所有常用查询
     * @return 常用查询集合
     */
    @SuppressWarnings("unchecked")
    public HashMap<Object, List<String>> getUniqueGroups() {
        if (this.contains(SERV_DEF.$ITEM_UNIQUE_GROUP)) {
            return (HashMap<Object, List<String>>) get(SERV_DEF.$ITEM_UNIQUE_GROUP);
        } else {
            return null;
        }
    }

    /**
     * 根据服务项编码获取对应的定义信息
     * @param itemCode 服务项编码
     * @return 服务项定义信息
     */
    public Bean getItem(Object itemCode) {
        Map<String, Bean> itemMap = this.getMap(SERV_DEF.$ITEMS);
        return itemMap.get(itemCode);
    }

    /**
     * 根据服务项编码获取对应的服务项名称
     * @param itemCode 服务项编码
     * @return 服务项名称，没有返回空字符串
     */
    public String getItemName(Object itemCode) {
        Bean item = getItem(itemCode);
        if (item != null) {
            return item.getStr("ITEM_NAME");
        } else {
            return "";
        }
    }

    /**
     * 根据服务项编码确定是否存在该数据项
     * @param itemCode 数据项编码
     * @return 是否存在数据项
     */
    public boolean containsItem(Object itemCode) {
        return this.getMap(SERV_DEF.$ITEMS).containsKey(itemCode);
    }

    /**
     * 根据操作编码获取对应的定义信息
     * @param actCode 操作编码
     * @return 操作定义信息
     */
    public Bean getAct(Object actCode) {
        Map<String, Bean> actMap = this.getMap(SERV_DEF.$ACTS);
        return actMap.get(actCode);
    }

    /**
     * 根据操作编码确定是否存在该操作定义
     * @param actCode 操作编码
     * @return 是否存在操作定义
     */
    public boolean containsAct(Object actCode) {
        return this.getMap(SERV_DEF.$ACTS).containsKey(actCode);
    }

    /**
     * 获取服务名称
     * @return 服务名称
     */
    public String getName() {
        return this.getStr("SERV_NAME");
    }

    /**
     * 获取父服务编码
     * @return 父服务编码
     */
    public String getPId() {
        return this.getStr("SERV_PID");
    }

    /**
     * 获取引用自服务编码
     * @return 引用自服务编码
     */
    public String getSrcId() {
        return this.getStr("SERV_SRC_ID");
    }

    /**
     * 获取待办标识，如果没有设定则采用引用自服务。
     * @return 待办标识
     */
    public String getTodoType() {
        if (this.isNotEmpty("TODO_TYPE")) {
            return this.getStr("TODO_TYPE");
        } else {
            return this.getStr("SERV_SRC_ID");
        }
    }
    
    /**
     * 获取服务主键字段
     * @return 服务主键字段
     */
    public String getPKey() {
        return this.getStr("SERV_KEYS");
    }

    /**
     * 获取服务对应查询表或者视图（查询对应表名或者视图名）
     * @return 查询表或者视图
     */
    public String getTableView() {
        String viewName = this.getStr("TABLE_VIEW");
        if (Context.appBoolean("SY_SERV_NO_VIEW") && viewName.endsWith("_V")) { //无视图模式采用查询语句替代
            Bean item = DictMgr.getItem("SY_SERV_NO_VIEW_" + Context.getDSBean().getStr(DS.DB_TYPE), viewName);
            if (item != null) {
                viewName = "(" + item.getStr("ITEM_FIELD1") + ")";
            }
        }
        return viewName;
    }

    /**
     * 获取服务对应操作表（插入、修改、删除对应的表名）
     * @return 操作表
     */
    public String getTableAction() {
        return this.getStr("TABLE_ACTION");
    }

    /**
     * 获取服务定义where条件
     * @return 服务定义where条件
     */
    public String getSqlWhere() {
        return this.getStr("SERV_SQL_WHERE");
    }

    /**
     * 获取服务定义order语句
     * @return 服务定义order语句
     */
    public String getSqlOrder() {
        return this.getStr("SERV_SQL_ORDER");
    }

    /**
     * 获取服务的公司字段（启用公司字段后，服务可以自动匹配对应的数据字典（支持公司级字典））
     * @return 服务的公司字段
     */
    public String getCmpy() {
        return this.getStr("SERV_CMPY");
    }

    /**
     * 获取每页显示记录数
     * @return 服务定义设定的缺省每页记录数
     */
    public int getPageCount() {
        return this.getInt("SERV_PAGE_COUNT");
    }

    /**
     * 获取每页显示记录数
     * @param def 缺省每页记录数
     * @return 服务定义设定的缺省每页记录数
     */
    public int getPageCount(int def) {
        return this.get("SERV_PAGE_COUNT", def);
    }

    /**
     * 获取数据的标题格式规则
     * @return 标题格式规则
     */
    public String getDataTitle() {
        return this.getStr("SERV_DATA_TITLE");
    }

    /**
     * 获取数据的编码格式规则
     * @return 编码格式规则
     */
    public String getDataCode() {
        return this.getStr("SERV_DATA_CODE");
    }

    /**
     * 获取服务对应字典列表（构建字典树、清理字典缓存用到），多个逗号分隔
     * @return 服务对应字典列表
     */
    public String getDictCodes() {
        return this.getStr("SERV_DICT_CACHE");
    }

    /**
     * 如果服务项设定了自定义字段相关文件，则可以通过本方法获取所有相关服务的ID列表
     * @return 相关服务ID列表
     */
    public List<String> getRelateIds() {
        return this.getList("SERV_RELATE_IDS");
    }

    /**
     * 获取服务对应数据源： 如果为空字符串，说明是缺省数据源； 如果不为空，说明是与平台不同的数据源；
     * @return 服务对应对应数据源
     */
    public String getDataSource() {
        return this.getStr("SERV_DATA_SOURCE");
    }

    /**
     * 获取服务对应的缺省卡片模版名称： 如果本服务启用自定义模版为本服务名称； 如果父服务启用自定义模版本服务没有启用，则继承父服务的模版文件。
     * @return 服务卡片自定义模版名
     */
    public String getCardTmplName() {
        return this.getStr("SERV_CARD_TMPL_NAME");
    }

    /**
     * 查询是否不计总数
     * @return 是否不计总数
     */
    public boolean noCount() {
        return this.getInt("SERV_NO_COUNT") == Constant.YES_INT;
    }

    /**
     * 是否启用了缓存
     * @return 是否启用缓存
     */
    public boolean hasCache() {
        return this.getInt("SERV_CACHE_FLAG") == Constant.YES_INT;
    }

    /**
     * 是否存在实体数据处理
     * @return 是否实体数据处理
     */
    public boolean hasEntity() {
        return this.getDataTitle().length() > 0;
    }

    /**
     * 是否存在级联处理
     * @return 是否包含级联处理
     */
    public boolean hasLink() {
        return this.getInt("SERV_LINK_FLAG") == Constant.YES_INT;
    }

    /**
     * 是否启用乐观锁判断
     * @return 是否启用乐观锁判断
     */
    public boolean hasLock() {
        return this.getInt("SERV_LOCK_FLAG") == Constant.YES_INT;
    }

    /**
     * 是否包含附件（启用了字段级附件或者自定义附件就返回true）
     * @return 是否包含附件
     */
    public boolean hasFile() {
        return this.getInt("SERV_FILE_FLAG") == Constant.YES_INT;
    }

    /**
     * 是否启用了假删除模式 （假删除模式要求启用假删除标志，且包含S_FLAG字段）
     * @return 是否启用了假删除
     */
    public boolean hasFalseDelete() {
        return this.getInt("SERV_DELETE_FLAG") == Constant.YES_INT && this.containsItem("S_FLAG");
    }

    /**
     * 是否包含意见lable项
     * @return 是否包含意见lable
     */
    public boolean hasMindLable() {
        return this.getBoolean("SERV_MIND_LABLE_FLAG");
    }

    /**
     * 是否启用了相关文件服务
     * @return 是否启用了相关文件
     */
    public boolean hasRelate() {
        return this.contains("SERV_RELATE_IDS");
    }

    /**
     * 是否启用了全文检索服务
     * @return 是否启用了全文检索
     */
    public boolean hasSearch() {
        return this.getInt("SERV_SEARCH_FLAG") == Constant.YES_INT;
    }

    /**
     * 是否启用了评论功能
     * @return 是否启用了评论
     */
    public boolean hasComment() {
        return this.getInt("SERV_COMMENT_FLAG") == Constant.YES_INT;
    }

    /**
     * 是否自动启动流程
     * @return 是否启用自动启动流程
     */
    public boolean hasWfAuto() {
        return this.getInt("SERV_WF_FLAG") == Constant.YES_INT;
    }

    /**
     * 获取卡片页面对应的模版内容，模版存放路径：/模块/tmpl/服务编码.html 如果在web.xml中启用调试模式（DEBUG_MODE=true），则每次会获取文件的最新内容，如果没有则取缓存内容，
     * 非调试模式下，修改模版文件后需要手工清除服务的缓存。
     * @return 模版文件的实际内容
     */
    public String getCardTmplContent() {
        if (!Context.isDebugMode() && this.contains("SERV_CARD_TMPL_CONTENT")) {
            return this.getStr("SERV_CARD_TMPL_CONTENT");
        }
        int pos = this.getId().indexOf("_");
        String path = Context.appStr(APP.SYSPATH) + this.getId().substring(0, pos).toLowerCase() + "/tmpl/"
                + this.getCardTmplName() + ".html";
        return FileHelper.readFile(path);
    }

    /**
     * 服务是否启用全文检索
     * @return 是否启用全文检索，true启用，false没有启用
     */
    public boolean getSearchFlag() {
        return this.getInt("SERV_SEARCH_FLAG") == Constant.YES_INT;
    }
    
    /**
     * 获取搜索定义信息
     * @return 全文检索定义信息，包含关联检索设定
     */
    public Bean getSearchDef() {
        Bean searchDef = null;
        if (this.contains("SY_SERV_SEARCH") && this.isNotEmpty("SY_SERV_SEARCH")) {
            searchDef = this.getBean("SY_SERV_SEARCH");
        } else {
            
            //使用引用服务的搜索配置
            searchDef = ServDao.find(ServMgr.SY_SERV_SEARCH, this.getSrcId(), true);
           
            //如果引用服务没有搜索配置,使用该服务搜索配置
            if (null == searchDef) {
                searchDef = ServDao.find(ServMgr.SY_SERV_SEARCH, this.getId(), true);
            }
            if (searchDef == null) {
                log.error(Context.getSyMsg("SY_DATA_NOT_EXIST", this.getSrcId()));
            } else {
                this.set("SY_SERV_SEARCH", searchDef);
            }
        }
        return searchDef;
    }

    /**
     * 得到服务定义的缺省过滤条件，支持变量替换和公司字段自动处理
     * @return 过滤条件
     */
    public String getServDefWhere() {
        StringBuilder servSqlWhere = new StringBuilder();
        String where = this.getSqlWhere();
        if (where.length() > 0) {
            if (where.indexOf("@@") >= 0) { // 存在数据权限
                where = DataAclMgr.replaceDataAcl(where);
            }
            if (where.indexOf("@") >= 0) { // 存在变量替换
                where = ServUtils.replaceSysVars(where);
            }
            servSqlWhere.append(where);
        }
        if (getCmpy().length() > 0) { // 自动处理存在公司字段的过滤
            String cmpy = Context.getCmpy();
            if (cmpy.length() > 0) {
                servSqlWhere.append(" and ").append(getCmpy()).append("='").append(cmpy).append("'");
            }
        }
        return servSqlWhere.append(this.getServExpressionWhere()).toString();
    }

    /**
     * 获取查询规则表达式对应的Where
     * @return 查询规则表达式where
     */
    public String getServExpressionWhere() {
        StringBuilder where = new StringBuilder();
        List<Bean> whereList = this.getAllWheres();
        if (whereList.size() > 0) {
            for (Bean whereBean : whereList) {
                if ((whereBean.getInt("S_FLAG") == Constant.YES_INT)
                        && Lang.isTrueScript(ServUtils.replaceSysVars(whereBean.getStr("WHERE_SCRIPT")))) {
                    String whereStr = (whereBean.isEmpty("WHERE_CONTENT")) ? ""
                            : ServUtils.replaceSysVars(whereBean.getStr("WHERE_CONTENT"));
                    where.append(" ").append(whereStr);
                    // 处理流经过滤规则，如果存在流经规则，流经SQL会并上过滤SQL
                    int flowFlag = whereBean.getInt("WHERE_FLOW_FLAG");
                    UserBean userBean = Context.getUserBean();
                    switch (flowFlag) {
                    case ServConstant.FLOW_FLAG_ODEPT:
                      whereStr = " and OWNER_ID='" + userBean.getODeptCode() + "'";
                        break;
                    case ServConstant.FLOW_FLAG_TDEPT: //部门领导或同等权限
                        whereStr = " and OWNER_ID='" + userBean.getTDeptCode() + "'";
                        break;
                    case ServConstant.FLOW_FLAG_DEPT: //处室领导或同等权限
                        whereStr = " and OWNER_ID in ('" + userBean.getDeptCode() + "','" 
                                + userBean.getCode() + "')";
                        break;
                    case ServConstant.FLOW_FLAG_USER:
                        whereStr = " and OWNER_ID ='" + userBean.getCode() + "'";
                        break;
                    default:
                        whereStr = null;
                    }
                    if (whereStr != null) {
                        where.append(" and ").append(this.getPKey())
                                .append(" in (select DATA_ID from SY_SERV_FLOW where 1=1 ")
                                .append(whereStr).append(")");
                    }
                    break;
                }
            }
        }
        return where.toString();
    }

    /**
     * 获取服务对应的数据缓存信息
     * @param key 缓存主键
     * @return 对应的数据信息，如果不存在则返回null
     */
    public Object getDataCache(String key) {
        return CacheMgr.getInstance().get(key, this.getSrcId());
    }

    /**
     * 获取服务对应的数据缓存信息
     * @param key 缓存主键
     * @param data 需要被缓存的数据信息
     */
    public void setDataCache(String key, Object data) {
        CacheMgr.getInstance().set(key, data, this.getSrcId());
    }

    /**
     * 清除获取服务对应的所有数据缓存信息
     */
    public void clearDataCache() {
        clearDataCache(null);
    }

    /**
     * 清除获取服务对应的某条数据缓存信息
     * @param dataId 数据主键，为null表示清除所有本服务下的缓存
     */
    public void clearDataCache(String dataId) {
        if (this.hasCache()) { // 设置了缓存处理
            if (dataId == null) {
                CacheMgr.getInstance().clearCache(this.getSrcId());
            } else {
                CacheMgr.getInstance().remove(dataId, this.getSrcId());
            }
        }
    }

    /**
     * 清除服务对应的外部字典缓存，确保数据的时效性
     */
    public void clearDictCache() {
        clearDictCache(Context.getCmpy());
    }

    /**
     * 清除服务对应的外部字典缓存，确保数据的时效性
     * @param cmpyCode 公司编码，null为不指定，则清除全部公司对应的字典。
     */
    public void clearDictCache(String cmpyCode) {
        String codes = this.getDictCodes();
        if (codes.length() == 0) { // 没有设定字典缓存的清除设置
            return;
        }
        String[] dicts = codes.split(Constant.SEPARATOR);
        for (String dict : dicts) {
            if (dict.length() > 0) {
                DictMgr.clearCache(dict.trim(), cmpyCode);
            }
        }
    }

    /**
     * 
     * @return 服务中定义的表字段和视图字段组成的用于列表查询的Select 语句字符串
     */
    public String getListSelectFields() {
        StringBuilder select = new StringBuilder();
        List<Bean> tableFields = this.getViewItems();
        int fieldCount = tableFields.size();
        for (int i = 0; i < fieldCount; i++) { // 获取全部字段数据
            Bean itemBean = tableFields.get(i);
            if (itemBean.getInt("ITEM_LIST_FLAG") != Constant.NO_INT) { // 除不显示的字段之外都要放到本地
                select.append(itemBean.get("ITEM_CODE")).append(",");
            }
        }

        if (select.length() > 0) {
            select.setLength(select.length() - 1);
            return select.toString();
        }

        return "";
    }
}
