package com.rh.core.serv.util;

import com.rh.core.util.Constant;

/**
 * 服务定义常量
 * 
 * @author wanglong
 */
public class ServConstant extends Constant {
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
    /** 服务认证类型：不认证 */
    public static final int AUTH_FLAG_NONE = 9;
    /** 缓存标志：基于ID的缓存 */
    public static final int CACHE_FLAG_ID = 1;
    /** 缓存标志：全部缓存 */
    public static final int CACHE_FLAG_ALL = 2;
    /** 缓存标志：不装载 */
    public static final int CACHE_FLAG_NO = 3;

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

}
