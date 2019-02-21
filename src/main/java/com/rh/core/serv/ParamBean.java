package com.rh.core.serv;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.serv.bean.PageBean;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.RequestUtils;

/**
 * 服务参数扩展Bean
 * 
 * @author wanglong
 * 
 */
public class ParamBean extends Bean {
    /**_ADD_:保存是否为添加模式 '#_ADD_#'=='true'*/
    public static final String ADD_FLAG = "_ADD_";
    /**_AUTOADD_:byid查找不存在时自动添加一条记录*/
    public static final String AUTO_ADD_FLAG = "_AUTOADD_";
    /**_PAGE_: 分页包装标签 */
    public static final String QUERY_PAGE = "_PAGE_";
    /**_HIDE_: 列表隐藏字段 */
    public static final String QUERY_HIDE = "_HIDE_";
    /**_DEL_:是否强制真删除 */
    public static final String DELETE_DROP_FLAG = "_DEL_";
    /**_NOPAGE_:查询设定不分页 */
    public static final String QUERY_NOPAGE_FLAG = "_NOPAGE_";
    /**_OLDBEAN_:保存对应的原始数据信息 */
    public static final String SAVE_OLD_DATA = "_OLDBEAN_";
    
    /**_TRANS_:是否启用新事务 */
    public static final String TRANS_FLAG = "_TRANS_";
    /**_CLIENT_:是否来自手机等客户端app */
    public static final String APP_FLAG = "_APP_";

    /**
     * sid
     */
    private static final long serialVersionUID = -5601330378974575992L;
    
    /**
     * log
     */
    private static Log log = LogFactory.getLog(ParamBean.class);
    
    /**
     * 对象构造方法
     */
    public ParamBean() {
        super();
    }

    /**
     * 将客户端request对象中的参数直接转换为paramBean对象，缺省从reader中读取
     * @param request 客户端请求
     */
    public ParamBean(HttpServletRequest request) {
        this(request, true);
    }
    
    /**
     * 将客户端request对象中的参数直接转换为paramBean对象，先取request中键值data的json格式数据，再取其他参数数据
     * @param request 客户端请求
     * @param readerFlag 是否从reader中读取参数
     */
    @SuppressWarnings("unchecked")
    public ParamBean(HttpServletRequest request, boolean readerFlag) {
        String data = null;
        String device = request.getHeader("X-DEVICE-NAME");
        if (device != null) { //客户端请求
            if (!device.startsWith("@")) {
                this.set(APP_FLAG, true);
            }
            if (readerFlag) {
                try {  //预处理request payload信息
                    data = IOUtils.toString(request.getReader());
                    if (data != null && !data.isEmpty()) {
                        this.putAll(JsonUtils.toBean(data));
                    }
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    data = null;
                }
            } else {
                data = request.getParameter("data");
            }
        } else { //本身前端请求
            data = request.getParameter("data"); //预处理data参数
        }
        if (data != null) {
            this.putAll(JsonUtils.toBean(data));
        }

        //获取其他参数信息
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String pName = paramNames.nextElement();
            if (!pName.equals("data")) { // 不再处理data中的数据
                String value = RequestUtils.getStr(request, pName);
                if (pName.equals(Constant.KEY_ID)) { // 预处理直接传主键的机制
                    this.setId(value);
                } else {
                    this.set(pName, value);
                }
            }
        }
        this.remove(Constant.PARAM_JSON_RANDOM); //去掉客户端的 随机参数     
    }
    
    /**
     * 对象构造方法
     * @param servId 服务编码
     */
    public ParamBean(String servId) {
        this.set(Constant.PARAM_SERV_ID, servId);
    }

    /**
     * 对象构造方法
     * @param servId 服务编码
     * @param act 操作编码
     */
    public ParamBean(String servId, String act) {
        this(servId);
        this.set(Constant.PARAM_ACT_CODE, act);
    }
    

    /**
     * 对象构造方法
     * @param servId 服务编码
     * @param act 操作编码
     * @param dataId 数据主键
     */
    public ParamBean(String servId, String act, String dataId) {
        this(servId, act);
        this.setId(dataId);
    }
    
    /**
     * 对象构造方法
     * 
     * @param bean 数据对象
     */
    public ParamBean(Bean bean) {
        super(bean);
    }

    /**
     * 设置唯一Id
     * @param id 唯一Id
     * @return 当前对象，用于级联设定
     */
    public ParamBean setId(String id) {
        set(KEY_ID, id);
        return this;
    }
    
    /**
     * 服务编码
     * @return 服务编码
     */
    public String getServId() {
        return this.getStr(Constant.PARAM_SERV_ID);
    }
    
    /**
     * 设定服务编码
     * @param servId 服务编码
     * @return 对象本身
     */
    public ParamBean setServId(String servId) {
        this.set(Constant.PARAM_SERV_ID, servId);
        return this;
    }

    /**
     * 操作编码
     * @return 操作编码
     */
    public String getAct() {
        return this.getStr(Constant.PARAM_ACT_CODE);
    }
    
    /**
     * 设定操作编码
     * @param act 操作编码
     * @return 操作编码
     */
    public ParamBean setAct(String act) {
        this.set(Constant.PARAM_ACT_CODE, act);
        return this;
    }
    
    /**
     * 获取查询select的字段列表，多个逗号分隔
     * 【query、finds方法用到】
     * 【query:*表示取所有列】
     * @return select的字段列表
     */
    public String getSelect() {
        return this.getStr(Constant.PARAM_SELECT);
    }
    
    
    /**
     * 获取查询隐藏的字段列表，多个逗号分隔
     * 【query方法用到】
     * @return 隐藏的字段列表
     */
    public String getQueryHide() {
        return this.getStr(QUERY_HIDE);
    }
    
    /**
     * 设置select的字段列表，多个逗号分隔
     * 【query、finds方法用到】
     * @param select select的字段列表
     * @return 当前对象
     */
    public ParamBean setSelect(String select) {
        this.set(Constant.PARAM_SELECT, select);
        return this;
    }
    
    /**
     * 设置from的表名
     * 【query、finds方法用到】
     * @param tableName sql中from的表名
     * @return 当前对象
     */
    public ParamBean setTable(String tableName) {
        this.set(Constant.PARAM_TABLE, tableName);
        return this;
    }
    
    /**
     * 获取from的表名
     * 【query、finds方法用到】
     * @return from的表名
     */
    public String getTable() {
        return this.getStr(Constant.PARAM_TABLE);
    }
    
    /**
     * 获取查询的where条件，以and开头
     * 【finds、query方法用到，用于设定通用过滤条件】
     * @return 查询的排序信息
     */
    public String getWhere() {
        return this.getStr(Constant.PARAM_WHERE);
    }
    
    /**
     * 设置查询的where条件，以and开头
     * @param where 查询的where条件
     * 【finds、query方法用到，用于设定过滤条件】
     * @return 当前对象
     */
    public ParamBean setWhere(String where) {
        this.set(Constant.PARAM_WHERE, where);
        return this;
    }
    
    /**
     * 获取查询的排序信息
     * 【query、finds方法用到】 
     * 【query中当没有设定pageOrder时此项设定才生效】
     * @return 查询的排序信息
     */
    public String getOrder() {
        return this.getStr(Constant.PARAM_ORDER);
    }
    
    /**
     * 设置查询的排序信息
     * @param order 查询的排序信息
     * 【query、finds方法用到】 
     * 【query中当没有设定pageOrder时此项设定才生效】
     * @return 当前对象
     */
    public ParamBean setOrder(String order) {
        this.set(Constant.PARAM_ORDER, order);
        return this;
    }
    
    /**
     * 设置分页的当前为第几页
     * 【query、finds方法用到】 
     * 支持分页处理，与setShowNum(int count)配合使用，实现分页查询数据数据
     * 【query中设定此项后则QueryNoPageFlag自动为true，不再计算总记录数和分页信息】
     * @param curPage 当前页数
     * @return 当前对象
     */
    public ParamBean setNowPage(int curPage) {
        this.setQueryNoPageFlag(true);
        this.set(Constant.PAGE_NOWPAGE, curPage);
        return this;
    }

    /**
     * 获取从第几页开始取数据，缺省为第一页
     * 【query、finds方法用到】 
     * 支持分页处理，与getShowNum()配合使用，实现分页查询数据数据
     * @return 当前页数，缺省为1
     */
    public int getNowPage() {
        return this.get(Constant.PAGE_NOWPAGE, 1);
    }
    
    /**
     * 设置查询的返回最大记录数
     * 【query、finds方法用到】 
     * 支持分页处理，与setNowPage(int curPage)配合使用，实现分页查询数据数据
     * 【query中设定此项后则QueryNoPageFlag自动为true，不再计算总记录数和分页信息】
     * @param count 查询的排序信息
     * @return 当前对象
     */
    public ParamBean setShowNum(int count) {
        this.setQueryNoPageFlag(true);
        this.set(Constant.PARAM_ROWNUM, count);
        return this;
    }

    /**
     * 获取查询最大记录数
     * 【query、finds方法用到】 
     * @return 查询的排序信息
     */
    public int getShowNum() {
        return this.getInt(Constant.PARAM_ROWNUM);
    }
    
    /**
     * 分页信息，如果不存在则新建一个缺省分页设定
     * 【query方法用到】
     * @return 分页信息
     */
    public PageBean getQueryPage() {
        PageBean page;
        if (this.contains(QUERY_PAGE)) {
            page = new PageBean(this.getBean(QUERY_PAGE));
        } else { //初始化页面配置，设定缺省值
            page = new PageBean();
            this.set(QUERY_PAGE, page);
        }
        return page;
    }
    
    /**
     * 获取非分页标志，true：表示不分页；false表示分页
     * 【query方法用到】
     * @return 非分页标志
     */
    public boolean getQueryNoPageFlag() {
        return this.getBoolean(QUERY_NOPAGE_FLAG);
    }
    
    /**
     * 设置非分页标志，true：表示不分页；false表示分页
     * 【query方法用到】
     * @param noPage 非分页标志，一旦设定为ture不分页，则不再计算总记录数，返回的记录数为实际取回的数据数
     * @return 当前对象
     */
    public ParamBean setQueryNoPageFlag(boolean noPage) {
        this.set(QUERY_NOPAGE_FLAG, noPage);
        return this;
    }
    
    /**
     * 设置分页的当前为第几页
     * 【query方法用到】
     * @param curPage 当前页数
     * @return 当前对象
     */
    public ParamBean setQueryPageNowPage(int curPage) {
        this.getQueryPage().setNowPage(curPage);
        return this;
    }
    
    /**
     * 获取页面排序信息
     * 【query方法用到】
     * @return 页面排序信息
     */
    public String getQueryPageOrder() {
        return this.getQueryPage().getOrder();
    }
    
    /**
     * 获取页面排序信息
     * 【query方法用到】
     * @param order 页面排序信息
     * @return 当前对象
     */
    public ParamBean setQueryPageOrder(String order) {
        this.getQueryPage().setOrder(order);
        return this;
    }
    
    /**
     * 设置分页的每页显示记录数
     * 【query方法用到】
     * @param showNum 每页显示记录数
     * @return 当前对象
     */
    public ParamBean setQueryPageShowNum(int showNum) {
        this.getQueryPage().setShowNum(showNum);
        return this;
    }
    
    /**
     * 获取查询where条件
     * 【query方法用到】 
     * @return 查询where条件
     */
    public String getQuerySearchWhere() {
        return this.getStr("_searchWhere");
    }
    
    /**
     * 设置查询where条件
     * 【query方法用到】 
     * @param searchWhere 查询where条件
     * @return 当前对象
     */
    public ParamBean setQuerySearchWhere(String searchWhere) {
        this.set("_searchWhere", searchWhere);
        return this;
    }
    
    /**
     * 获取关联where条件，
     *      如果同时传递了_linkServQuery为1，则合并服务定义的过滤条件，
     *      如果没有传递_linkServQuery或者为2，则不合并服务定义的where
     * 【query方法用到】 
     * @return 关联where条件
     */
    public String getQueryLinkWhere() {
        return this.getStr("_linkWhere");
    }
    
    /**
     * 设置关联where条件，
     *      如果同时传递了_linkServQuery为1，则合并服务定义的过滤条件，
     *      如果没有传递_linkServQuery或者为2，则不合并服务定义的where
     * 【query方法用到】 
     * @param linkWhere 关联where条件
     * @return 当前对象
     */
    public ParamBean setQueryLinkWhere(String linkWhere) {
        this.set("_linkWhere", linkWhere);
        return this;
    }
    
    /**
     * 获取扩展where条件
     * 【query方法用到】 
     * @return 扩展where条件
     */
    public String getQueryExtWhere() {
        return this.getStr("_extWhere");
    }
    
    /**
     * 设置扩展where条件
     * 【query方法用到】 
     * @param extWhere 扩展where条件
     * @return 当前对象
     */
    public ParamBean setQueryExtWhere(String extWhere) {
        this.set("_extWhere", extWhere);
        return this;
    }
    
    /**
     * 
     * @return GroupBy子语句
     */
    public String getGroupBy() {
        return this.getStr("_groupBy");
    }
    
    /**
     * 
     * @param groupBy 设置GroupBy语句
     * @return 当前对象
     */
    public ParamBean setGroupBy(String groupBy) {
        this.set("_groupBy", groupBy);
        return this;
    }
    
    /**
     * 获取添加模式标志，true无论是否有主键强制设为添加模式，false为修改保存模式
     * 【save、batchSave方法用到】
     * @return 添加模式标志
     */
    public boolean getAddFlag() {
        return this.getBoolean(ADD_FLAG);
    }
    
    /**
     * 设置添加模式标志，true无论是否有主键强制设为添加模式，false为修改保存模式
     * 【save、batchSave方法用到】
     * @param addFlag  添加模式标志
     * @return 本对象
     */
    public ParamBean setAddFlag(boolean addFlag) {
        this.set(ADD_FLAG, addFlag);
        return this;
    }
    
    
    /**
     * 获取byid数据不存在是否自动添加一条记录的标志，缺省为false，不自动添加记录，true如果数据找不到自动添加一条
     * 【byid方法用到】
     * @return 添加模式标志
     */
    public boolean getByidAutoAddFlag() {
        return this.getBoolean(AUTO_ADD_FLAG);
    }
    
    /**
     * 级联处理子数据标志，可以级联获取、保存、删除关联数据信息
     * 【byid、finds、delete方法用到】
     * 【finds：带此参数因为性能考虑缺省最多向下获取两级数据】
     * 【delete：设定此参数会强制级联删除所有非只读关联子服务，无论关联子服务是否设定关联删除，】
     * 【save、batchSave：无需设定级联标志，自动根据数据格式进行级联处理
     *      如果存在与子服务名称一致的列表数据自动进行添加或者保存处理，添加模式：没有主键、有主键但有添加标志；其他为修改模式
     *      如果存在与【子服务名__DELS】一致的数据，则自动设定级联标志进行级联删除，删除所有非只读关联子服务
     *  】
     * @return 级联标志：true、false（缺省为不级联）
     */
    public boolean getLinkFlag() {
        return this.getBoolean(Constant.PARAM_LINK_FLAG);
    }
    
    
    /**
     * 设置级联处理子数据标志，可以级联获取关联数据信息，true为启用级联
     * 【byid、finds、delete方法用到】
     * @param linkFlag  级联处理标志
     * @return 本对象
     */
    public ParamBean setLinkFlag(boolean linkFlag) {
        this.set(Constant.PARAM_LINK_FLAG, linkFlag);
        return this;
    }
    
    /**
     * 获取事务处理标志，true：强制启用新的事务，false（缺省）：使用线程中已经存在的事务
     * @return 事务处理标志
     */
    public boolean getTransFlag() {
        return this.getBoolean(TRANS_FLAG);
    }
    
    /**
     * 设置事务处理标志，true：强制启用新的事务，false：使用线程中已经存在的事务
     * @param transFlag  事务处理标志
     * @return 本对象
     */
    public ParamBean setTransFlag(boolean transFlag) {
        this.set(TRANS_FLAG, transFlag);
        return this;
    }
    
    /**
     * 级联模式标志，true说明当前操作属于被级联处理的操作，false说明当前操作为独立模式操作
     * 【在save和delete的扩展类经常要用到，用来判断是因为父服务的级联操作导致的本服务操作，还是直接用户触发的本服务操作】
     * 【例如服务项自身更新的是否需要生成定义文件，但是父服务的级联更新导致的本操作不需要由本操作生成定义文件，就需要判断此标志】
     * @return 级联模式标志
     */
    public boolean getLinkMode() {
        return this.getBoolean(Constant.IS_LINK_ACT);
    }
    
    /**
     * 获取对应数据库原始数据内容
     * 【save：用于获取修改前的数据库原始数据内容】
     * @return 数据库原始内容
     */
    public Bean getSaveOldData() {
        return this.getBean(SAVE_OLD_DATA);
    }
    
    /**
     * 自动区分是添加模式还是修改模式，如果修改模式则自动将数据库中值并入，以得到一个全部为最新数据的bean
     * 【save：自动根据修改还是添加模式，合并出一个包含数据库原始数据以及最新修改数据的混合数据体】
     * @return 全数据的bean
     */
    public Bean getSaveFullData() {
        Bean fullBean = this;
        if (this.getId().length() > 0) { // 添加模式，增加原有数据值
            Bean oldBean = getSaveOldData();
            fullBean = BeanUtils.mergeBean(oldBean, this);
        }
        return fullBean;
    }
    
    /**
     * 获取批量保存的数据列表信息
     * 【batchSave：设定批量保存的数据列表，每条数据上如果有添加标志addFlag或者无主键自动添加，否则为修改】
     * @param <T> 数据类型
     * @return 用于批量保存的数据列表信息
     */
    public <T> List<T> getBatchSaveDatas() {
        return this.getList("BATCHDATAS");
    }
    
    /**
     * 设置批量保存的数据列表
     * 【batchSave：设定批量保存的数据列表，每条数据上如果有添加标志addFlag或者无主键自动添加，否则为修改】
     * @param <T> 数据类型
     * @param dataList 需要批量保存的数据列表
     * @return 当前对象
     */
    public <T> ParamBean setBatchSaveDatas(List<T> dataList) {
        this.set("BATCHDATAS", dataList);
        return this;
    }
    
    /**
     * 获取批量保存中待删除的数据主键列表，多个逗号分隔
     * 【batchSave：待批量删除的数据数据主键列表】
     * @return 用于批量保存的数据列表信息
     */
    public String getBatchSaveDelIds() {
        return this.getStr("BATCHDELS");
    }
    
    /**
     * 设置批量保存中待删除的数据主键列表，多个逗号分隔
     * 【batchSave：待批量删除的数据主键列表，批量保存中的包含的批量删除需要用此方法设置主键列表】
     * @param delIds 待删除的数据主键列表
     * @return 当前对象
     */
    public ParamBean setBatchSaveDelIds(String delIds) {
        this.set("BATCHDELS", delIds);
        return this;
    }
    
    /**
     * 获取待删除的数据列表信息
     * 【delete：获取待批量删除的数据列表，可以在beforeDelete、afterDelete中获取到进行逻辑判断处理】
     * @return 用于批量保存的数据列表信息
     */
    public List<Bean> getDeleteDatas() {
        return this.getList("_DELETE_DATAS_");
    }
    
    /**
     * 设置删除的数据列表
     * 【delete：待批量删除的数据列表
     *      如果没有通过paramBean.setId(ids)将主键列表传入，可以通过此方法设定删除列表以避免在删除中二次检索数据
     *  】
     * @param delDataList 待删除的数据列表
     * @return 当前对象
     */
    public ParamBean setDeleteDatas(List<Bean> delDataList) {
        this.set("_DELETE_DATAS_", delDataList);
        return this;
    }
    
    /**
     * 获取删除时强制真删除标志
     * 【delete:获取删除时是否设定强制真删除标志，可以在beforeDelete和afterDelete中获取到删除标志是真删除还是假删除
     *      true:无论服务是否启用假删除，强制真删除数据，且级联真删除子服务数据，与linkFlag配合可以强制删除所有非只读关联数据；
     *      false:缺省为false，根据服务假删除设定进行真假删除处理
     *  】
     * @return 强制真删除标志
     */
    public boolean getDeleteDropFlag() {
        return this.getBoolean(DELETE_DROP_FLAG);
    }
    
    /**
     * 设置删除时强制真删除标志
     * 【delete:删除时判断是否设定强制真删除标志
     *      true:无论服务是否启用假删除，强制真删除数据，且级联真删除子服务数据，与linkFlag配合可以强制删除所有非只读关联数据；
     *      false:缺省为false，根据服务假删除设定进行真假删除处理
     *  】
     * @param dropFlag  强制真删除标志
     * @return 本对象
     */
    public ParamBean setDeleteDropFlag(boolean dropFlag) {
        this.set(DELETE_DROP_FLAG, dropFlag);
        return this;
    }
    
    /**
     * 获取是否压缩空值（不输出空值）标志，缺省为false
     * @return 是否压缩空值
     */
    public boolean getEmptyFlag() {
        return this.getBoolean(Constant.PARAM_EMPTY);
    }
    
    /**
     * 获取是否为来自客户端app(手机或桌面客户端软件）的请求
     * @return 是否为客户端app请求
     */
    public boolean isApp() {
        return this.getBoolean(APP_FLAG);
    }
    
    /**
    * 将在数组中所有的内容传递到另外一个bean
    * @return 复制出来的数据内容，
    */
   public ParamBean copyOf()  {
       return copyOf(null);
   }
    
    /**
    * 将在数组中设定属性键值的内容传递到另外一个bean
    * @param keys  键值数组 null表示传全部src中的数据
    * @return 复制出来的数据内容，
    */
   public ParamBean copyOf(Object[] keys)  {
       ParamBean tar = new ParamBean(); 
       if (keys != null) {
           for (Object key : keys) {
               tar.set(key, get(key));
           }
       } else {
           tar.putAll(this);
       }
       return tar;
   }

   /**
    * 设置对象，支持级联设置
    * @param key   键值
    * @param obj   对象数据
    * @return this，当前Bean
    */
   public ParamBean set(Object key, Object obj) {
       put(key, obj);
       return this;
   }
   
   /**
    * 
    * @param keywords  指定用于查询语句，Select之后的关键字，如：distinct 、Oracle Sql hints等
    */
    public void setSelectKeyWord(String keywords) {
        this.set(Constant.SELECT_KEYWORDS, keywords);
    }
}
