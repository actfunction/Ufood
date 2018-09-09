/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.fe.ufood.util;

/**
 * 定义系统中用到的常量
 * 
 * @author Sinclay Fe
 * @version $Id$
 */
public final class Constant {
    /**
     * 私有构建体方法
     */
    private Constant() {
    }

    /** 字段分隔符号 */
    public static final String SEPARATOR = ",";
    /** 文件分隔符号 */
    public static final String PATH_SEPARATOR = "/";
    /** 字符集 */
    public static final String ENCODING = "UTF-8";

    /** 每页显示数据量 */
    public static final String SHOWNUM = "SHOWNUM";
    /** 当前页 */
    public static final String NOWPAGE = "NOWPAGE";
    /** 数据总量 */
    public static final String ALLNUM = "ALLNUM";
    /** 总页数 */
    public static final String PAGES = "PAGES";

    /** list包装标签 */
    public static final String RTN_DATA = "_DATA_";
    /** JSP跳转传递的数据 */
    public static final String RTN_DISP_DATA = "_DISPDATA_";
    /** 文件ID列表 */
    public static final String RTN_FILE_IDS = "_FILEIDS_";
    /** 返回信息标签 */
    public static final String RTN_MSG = "_MSG_";
    /** 成功信息 */
    public static final String RTN_MSG_OK = "OK,";
    /** 警告信息 */
    public static final String RTN_MSG_WARN = "WARN,";
    /** 失败信息 */
    public static final String RTN_MSG_ERROR = "ERROR,";
    /** 登录信息 */
    public static final String RTN_MSG_LOGIN = "LOGIN,";
    /** 执行时间 */
    public static final String RTN_TIME = "_TIME_";
    /** 参数：查询字段 */
    public static final String PARAM_SELECT = "_SELECT_";
    /** 参数：查询表，支持多个  */
    public static final String PARAM_TABLE = "_TABLE_";
    /** 参数：过滤条件 */
    public static final String PARAM_WHERE = "_WHERE_";
    /** 参数：排序设置 */
    public static final String PARAM_ORDER = "_ORDER_";
    /** 参数：分组设置 */
    public static final String PARAM_GROUP = "_GROUP_";
    /** 参数：获取记录行数 */
    public static final String PARAM_ROWNUM = "_ROWNUM_";
    /** 参数：设置prepare sql变量信息 */
    public static final String PARAM_PRE_VALUES = "_PREVALUES_";
    /** 服务参数名 */
    public static final String PARAM_SERV_ID = "serv";
    /** 操作参数名 */
    public static final String PARAM_ACT_CODE = "act";
    /** 参数:json随机数 */
    public static final String PARAM_JSON_RANDOM = "expando";
    /** 参数:数据格式（xml或者json） */
    public static final String PARAM_FORMAT = "format";
    /** 参数:是否忽略空值，缺省为false */
    public static final String PARAM_EMPTY = "_EMPTY_";
    /** 参数:是否包含子数据，或是否强制级联处理 */
    public static final String PARAM_LINK_FLAG = "_LINK_";
    /**参数：是否为添加模式*/
    public static final String PARAM_ADD_FLAG = "_ADD_";
    /** 过程变量：级联处理标志 */
    public static final String IS_LINK_ACT = "isLinkAct";
    /** 参数:级联层级 */
    public static final String PARAM_LINK_LEVEL = "_LINKLEVEL_";

    /**
     * CODE_PATH字段内容分隔符
     */
    public static final String CODE_PATH_SEPERATOR = "^";

    /** 主键项 */
    public static final String KEY_ID = "_PK_";

    /** 特殊属性：空字符串，在Where生成时使用自动替换为空 */
    public static final String KEY_VALUE_NULL = "~$￥$~==~￥$￥~";

    /** 特殊属性：数字零，在Where生成时使用自动替换为数字零 */
    public static final String KEY_VALUE_ZERO = "~￥$￥~==~$￥$~";

    /** 是 */
    public static final String YES = "1";
    /** 否 */
    public static final String NO = "2";

    /** 是 */
    public static final int YES_INT = 1;
    /** 否 */
    public static final int NO_INT = 2;

    /** ----------------------服务定义子模块--------------------------------------- **/
    /** 服务类型：服务 */
    public static final int SERV_TYPE_SERV = 1;
    /** 服务类型：父服务 */
    public static final int SERV_TYPE_PSERV = 2;

    /** 全文索引附件类型：内部附件 */
    public static final int SEARCH_FILE_INNER = 1;
    /** 全文索引附件类型：外部附件 */
    public static final int SEARCH_FILE_OUTER = 2;
    /** 全文索引附件类型：不索引附件 */
    public static final int SEARCH_FILE_NONE = 3;

    /** 全文检索数据权限类型：全部可看 */
    public static final int SEARCH_DATA_AUTH_ALL = 1;
    /** 全文检索数据权限类型：公司内全部可看 */
    public static final int SEARCH_DATA_AUTH_CMPY_ALL = 2;
    /** 全文检索数据权限类型：公司内数据权限 */
    public static final int SEARCH_DATA_AUTH_CMPY_DACL = 3;
    /** 全文检索数据权限类型：跨公司数据权限 */
    public static final int SEARCH_DATA_AUTH_DACL = 4;

    /** 全文检索正文类型：无正文 */
    public static final int SEARCH_CONT_NONE = 0;
    /** 全文检索正文类型：字段正文 */
    public static final int SEARCH_CONT_FIELD = 1;
    /** 全文检索正文类型：附件表达式 */
    public static final int SEARCH_CONT_FILE = 2;
    /** 全文检索正文类型：第一个附件 */
    public static final int SEARCH_CONT_FIRST = 3;
    /** 全文检索正文类型：正文SQL */
    public static final int SEARCH_CONT_SQL = 4;

    /** 服务认证类型：sesssion认证 */
    public static final int AUTH_FLAG_SESSION = 1;
    /** 服务认证类型：服务权限认证 */
    public static final int AUTH_FLAG_SERV = 2;
    /** 服务认证类型：操作权限认证 */
    public static final int AUTH_FLAG_ACT = 3;
    /** 服务认证类型：数据权限认证 */
    public static final int AUTH_FLAG_DATA = 4;
    /** 服务认证类型：不认证 */
    public static final int AUTH_FLAG_NONE = 9;

    /** 缓存标志：基于ID的缓存 */
    public static final int CACHE_FLAG_ID = 1;
    /** 缓存标志：全部缓存 */
    public static final int CACHE_FLAG_ALL = 2;
    /** 缓存标志：不装载 */
    public static final int CACHE_FLAG_NO = 3;

    /** 服务：服务项类型：表字段 */
    public static final int ITEM_TYPE_TABLE = 1;
    /** 服务：服务项类型：视图字段 */
    public static final int ITEM_TYPE_VIEW = 2;
    /** 服务：服务项类型：自定义字段 */
    public static final int ITEM_TYPE_DEFINE = 3;
    /** 服务：服务项类型：参数字段 */
    public static final int ITEM_TYPE_PARAM = 4;
    
    /**_DISP_: 通过dispatcher方式的JSP跳转 */
    public static final String TO_DISP = "_DISP_";
    /**_DIRE_: 通过redirect的URL跳转 */
    public static final String TO_DIRE = "_DIRE_";
    /**_HTML_: 直接通过html流进行输出 */
    public static final String TO_HTML = "_HTML_";
    /**_XML_: 直接通过xml流进行输出 */
    public static final String TO_XML = "_XML_";

    /** 数据类型: 字符串 */
    public static final String ITEM_FIELD_TYPE_STR = "STR";
    /** 数据类型: 数字 */
    public static final String ITEM_FIELD_TYPE_NUM = "NUM";
    /** 数据类型: 大文本 */
    public static final String ITEM_FIELD_TYPE_BIGTEXT = "BIGTEXT";
    /** 数据类型: 时间戳 */
    public static final String ITEM_FIELD_TYPE_TIME = "TIME";
    /** 数据类型: 日期 */
    public static final String ITEM_FIELD_TYPE_DATE = "DATE";

    /** 输入元素: 输入框 */
    public static final int ITEM_INPUT_TYPE_TEXT = 1;
    /** 输入元素: 下拉框 */
    public static final int ITEM_INPUT_TYPE_SELECT = 2;
    /** 输入元素: 单选按钮 */
    public static final int ITEM_INPUT_TYPE_RADIO = 3;
    /** 输入元素: 多选按钮 */
    public static final int ITEM_INPUT_TYPE_CHECKBOX = 4;
    /** 输入元素: 大文本 */
    public static final int ITEM_INPUT_TYPE_TEXTAREA = 5;
    /** 输入元素: 超大文本 */
    public static final int ITEM_INPUT_TYPE_SUPERTEXT = 6;
    /** 输入元素: 文件上传 */
    public static final int ITEM_INPUT_TYPE_FILE = 7;
    /** 输入元素: 图片链接 */
    public static final int ITEM_INPUT_TYPE_IMAGE = 8;
    /** 输入元素: 嵌入服务（自定义） */
    public static final int ITEM_INPUT_TYPE_SERV = 9;
    /** 输入元素: 分组框（自定义） */
    public static final int ITEM_INPUT_TYPE_HR = 10;
    /** 输入元素:Label，静态文本，不允许修改 **/
    public static final int ITEM_INPUT_TYPE_LABEL = 11;
    /** 输入元素:密码框 **/
    public static final int ITEM_INPUT_TYPE_PASSWORD = 12;
    /** 输入元素:附件（自定义） */
    public static final int ITEM_INPUT_TYPE_ATTACHMENT = 14;
    /** 输入元素:组合字段（自定义） */
    public static final int ITEM_INPUT_TYPE_GROUP = 15;
    /** 输入元素:相关文件（自定义） */
    public static final int ITEM_INPUT_TYPE_RELATE = 16;
    /** 输入元素:意见显示框 */
    public static final int ITEM_INPUT_TYPE_MIND = 17;
    /** 输入元素:IFRAME（自定义） */
    public static final int ITEM_INPUT_TYPE_IFRAME = 18;
    /** 输入元素:评论（自定义） */
    public static final int ITEM_INPUT_TYPE_COMMENT = 19;
    
    /** 输入类型: 无 */
    public static final int ITEM_INPUT_MODE_NO = 1;
    /** 输入类型: 查询选择 */
    public static final int ITEM_INPUT_MODE_QUERY = 2;
    /** 输入类型: 字典 */
    public static final int ITEM_INPUT_MODE_DICT = 3;
    /** 输入类型: 日期时间 */
    public static final int ITEM_INPUT_MODE_DATE = 4;
    /** 输入类型: 动态提示 */
    public static final int ITEM_INPUT_MODE_HINT = 5;
    /** 输入类型: 组合值 */
    public static final int ITEM_INPUT_MODE_COMBINE = 6;
    /** 输入类型: 自处理 */
    public static final int ITEM_INPUT_MODE_SELFTRAN = 9;

    /** 移动类型：列表标题 */
    public static final int ITEM_MOBILE_TYPE_TITLE = 1;
    /** 移动类型：列表项 */
    public static final int ITEM_MOBILE_TYPE_LIST = 2;
    /** 移动类型：卡片 */
    public static final int ITEM_MOBILE_TYPE_CARD = 3;
    /** 移动类型：列表时间 */
    public static final int ITEM_MOBILE_TYPE_TIME = 4;
    /** 移动类型：列表图片 */
    public static final int ITEM_MOBILE_TYPE_IMG = 5;
    /** 移动类型：移动不显示 */
    public static final int ITEM_MOBILE_TYPE_NONE = 9;

    /** 列表对齐: 左对齐 */
    public static final int ITEM_LIST_ALIGN_LEFT = 1;
    /** 列表对齐: 右对齐 */
    public static final int ITEM_LIST_ALIGN_RIGHT = 2;
    /** 列表对齐: 居中对齐 */
    public static final int ITEM_LIST_ALIGN_CENTER = 3;

    /** 列表显示: 显示 */
    public static final int ITEM_LIST_FLAG_SHOW = 1;
    /** 列表显示: 不显示，无数据 */
    public static final int ITEM_LIST_FLAG_NO = 2;
    /** 列表显示: 隐藏，有数据 */
    public static final int ITEM_LIST_FLAG_HIDDEN = 3;

    /** 关联显示类型：列表显示 */
    public static final int LINK_SHOW_TYPE_LIST = 1;
    /** 关联显示类型：卡片显示 */
    public static final int LINK_SHOW_TYPE_CARD = 2;
    /** 关联显示类型：自定义URL */
    public static final int LINK_SHOW_TYPE_URL = 3;

    /** 查询关系: 等于 */
    public static final String QUERY_SIGN_EQUAL = "=";
    /** 查询关系: 大于 */
    public static final String QUERY_SIGN_BIG = ">";
    /** 查询关系: 小于 */
    public static final String QUERY_SIGN_SMALL = "<";
    /** 查询关系: 大于等于 */
    public static final String QUERY_SIGN_BIGEQ = ">=";
    /** 查询关系: 小于等于 */
    public static final String QUERY_SIGN_SMALLEQ = "<=";
    /** 查询关系: 不等于 */
    public static final String QUERY_SIGN_NOTEQ = "<>";
    /** 查询关系: in */
    public static final String QUERY_SIGN_IN = "in";
    /** 查询关系: like */
    public static final String QUERY_SIGN_LIKE = "like";
    /** 查询关系: 全文检索 */
    public static final String QUERY_SIGN_CONTAINS = "contains";
    
    /** 查询语句Select语句之后的关键字，例如distinct、ORACLE SQL HINTS等 **/
    public static final String SELECT_KEYWORDS = "_AFTER_SELECT_KEYWORDS";

    /** 过滤规则-流经类型：1 流经部门 */
    public static final int FLOW_FLAG_TDEPT = 1;
    /** 过滤规则-流经类型：2 流经处室 */
    public static final int FLOW_FLAG_DEPT = 2;
    /** 过滤规则-流经类型：3 流经人 */
    public static final int FLOW_FLAG_USER = 3;
    /** 过滤规则-流经类型：4 流经机构 */
    public static final int FLOW_FLAG_ODEPT = 4;
    /** 过滤规则-流经类型：9 不判断流程 */
    public static final int FLOW_FLAG_NONE = 9;

    /** 过滤规则-流经服务类型：1 本服务流经 */
    public static final int FLOW_SERV_CUR = 1;
    /** 过滤规则-流经服务类型：2 父服务流经 */
    public static final int FLOW_SERV_PARENT = 2;
    /** 过滤规则-流经服务类型：3 引用自服务流经 */
    public static final int FLOW_SERV_SRC = 3;

    /** ----------------------字典子模块--------------------------------------- **/
    /** 字典缓存装载方式：自动装载 */
    public static final int CACHE_LOAD_AUTO = 1;
    /** 字典缓存装载方式：用时装载 */
    public static final int CACHE_LOAD_USE = 2;
    /** 字典缓存装载方式：不装载 */
    public static final int CACHE_LOAD_NONE = 3;

    /** ----------------------部门模块--------------------------------------- **/
    /** 部门类型：部门 */
    public static final int DEPT_TYPE_DEPT = 1;
    /** 部门类型：机构 */
    public static final int DEPT_TYPE_ORG = 2;

    /** ----------------------文件模块--------------------------------------- **/
    /** 内部文件前缀 */
    public static final String FILE_INNER_URL_PREFIX = "internal://";

    /** 办件 */
    public static final int TODO = 1;

    /** 阅件 */
    public static final int READ = 2;

    /** 分发方案明细类型--用户 */
    public static final String USER = "USER";

    /** 分发方案明细类型--部门 */
    public static final String DEPT = "DEPT";

    /** 分发方案明细类型--角色 */
    public static final String ROLE = "ROLE";

    /** 分发方案明细类型--其他机构 */
    public static final String OTHER_ODEPT = "OTHER_ODEPT";

    /** 机构内 */
    public static final String INSIDE = "inside";

    /** 机构外 */
    public static final String OUTSIDE = "outside";
    /**
     * 执行byid方法时忽略流程信息
     */
    public static final String IGNORE_WF_INFO = "_IGNORE_WF_INFO_";
    
    /**
     * 公共角色code值
     */
    public static final String PUBLIC_ROLE_CODE = "RPUB";
    
    /**
     * 委托人用户CODE；原始的参数名为ORIGINAL_USER，现在改为_AGENT_USER_
     */
    public static final String AGENT_USER = "_AGENT_USER_";
    
    /**
     * 委托人用户Bean；
     */
    public static final String AGENT_USER_BEAN = "_AGENT_USER_BEAN_";
    
    /** ----------------------工作流按钮渲染模式系统配置状态值--------------------------------------- **/
    /** 操作按钮平铺，流程按钮平铺 */
    public static final String FLAT_FLAT = "0";
    /** 操作按钮平铺，流程按钮下拉组 */
    public static final String FLAT_GROUP = "10";
    /** 操作按钮平铺，流程按钮单独按钮条 */
    public static final String FLAT_BAR = "11";
    /** 操作按钮下拉组，流程按钮平铺 */
    public static final String GROUP_FLAT = "100";
    /** 操作按钮下拉组，流程按钮下拉组 */
    public static final String GROUP_GROUP = "110";
    /** 操作按钮下拉组，流程按钮单独按钮条 */
    public static final String GROUP_BAR = "111";
    
    /** 本系统主机地址：如：http://oa.cic.com:8080 **/
    public static final String CONF_SYS_HOST_ADDR = "SYS_HOST_ADDR";
    
    
    /** 编辑（查看）word文件时，是否显示文件修改痕迹，默认值为true（显示） **/
    public static final String CONF_OFFICE_DISPLAY_REVISION = "OFFICE_DISPLAY_REVISION";
}
