/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.serv.listener.ServLisMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;


/**
 * 公共的服务响应类
 * 
 * @author wanglong
 */
public abstract class ServMgr {
    /** 服务主键：关注 */
    public static final String SY_COMM_ATTENTION = "SY_COMM_ATTENTION";
    /** 服务主键：完成度查询 */
    public static final String SY_COMM_COMPLETE_DATA = "SY_COMM_COMPLETE_DATA";
    /** 服务主键：完成度配置 */
    public static final String SY_COMM_COMPLETE_SETTINGS = "SY_COMM_COMPLETE_SETTINGS";
    /** 服务主键：系统配置（私有）  */
    public static final String SY_COMM_CONFIG = "SY_COMM_CONFIG";
    /** 服务主键：系统配置（公共） */
    public static final String SY_COMM_CONFIG_PUBLIC = "SY_COMM_CONFIG_PUBLIC";
    /** 服务主键：数据展示 */
    public static final String SY_COMM_DATA_VIEW = "SY_COMM_DATA_VIEW";
    /** 服务主键：数据项 */
    public static final String SY_COMM_DATA_VIEW_ITEM = "SY_COMM_DATA_VIEW_ITEM";
    /** 服务主键：工作台管理 */
    public static final String SY_COMM_DESK = "SY_COMM_DESK";
    /** 服务主键：工作台管理（公共） */
    public static final String SY_COMM_DESK_PUBLIC = "SY_COMM_DESK_PUBLIC";
    /** 服务主键：主办事务 */
    public static final String SY_COMM_ENTITY = "SY_COMM_ENTITY";
    /** 服务主键：我关注的事项 */
    public static final String SY_COMM_ENTITY_ATTENTION = "SY_COMM_ENTITY_ATTENTION";
    /** 服务主键：在办事务 */
    public static final String SY_COMM_ENTITY_DOING = "SY_COMM_ENTITY_DOING";
    /** 服务主键：经办事务已办结(全部类型) */
    public static final String SY_COMM_ENTITY_DONE_FINISH = "SY_COMM_ENTITY_DONE_FINISH";
    /** 服务主键：流转已办结 */
    public static final String SY_COMM_ENTITY_DONE_FINISH_MINE = "SY_COMM_ENTITY_DONE_FINISH_MINE";
    /** 服务主键：经办事务未办结(全部类型) */
    public static final String SY_COMM_ENTITY_DONE_RUN = "SY_COMM_ENTITY_DONE_RUN";
    /** 服务主键：流转未办结 */
    public static final String SY_COMM_ENTITY_DONE_RUN_MINE = "SY_COMM_ENTITY_DONE_RUN_MINE";
    /** 服务主键：数据管理 */
    public static final String SY_COMM_ENTITY_GL = "SY_COMM_ENTITY_GL";
    /** 服务主键：全部 */
    public static final String SY_COMM_ENTITY_MINE = "SY_COMM_ENTITY_MINE";
    /** 服务主键：我接收的文件 */
    public static final String SY_COMM_ENTITY_RECEIVER = "SY_COMM_ENTITY_RECEIVER";
    /** 服务主键：我分发的文件 */
    public static final String SY_COMM_ENTITY_SENDER = "SY_COMM_ENTITY_SENDER";
    /** 服务主键：单据状态父服务 */
    public static final String SY_COMM_ENTITY_STATE = "SY_COMM_ENTITY_STATE";
    /** 服务主键：我关注的事项 */
    public static final String SY_COMM_ENTITY_TAG = "SY_COMM_ENTITY_TAG";
    /** 服务主键：个人回收站 */
    public static final String SY_COMM_ENTITY_TRASH = "SY_COMM_ENTITY_TRASH";
    /** 服务主键：机构回收站 */
    public static final String SY_COMM_ENTITY_TRASH_ORG = "SY_COMM_ENTITY_TRASH_ORG";
    /** 服务主键：主办事务 */
    public static final String SY_COMM_ENTITY_ZHUBAN = "SY_COMM_ENTITY_ZHUBAN";
    /** 服务主键：主办事务已办结(全部类型) */
    public static final String SY_COMM_ENTITY_ZHUBAN_FINISH = "SY_COMM_ENTITY_ZHUBAN_FINISH";
    /** 服务主键：主办事务未办结(全部类型) */
    public static final String SY_COMM_ENTITY_ZHUBAN_RUN = "SY_COMM_ENTITY_ZHUBAN_RUN";
    /** 服务主键：表达式 */
    public static final String SY_COMM_EXPRESSION = "SY_COMM_EXPRESSION";
    /** 服务主键：收藏 */
    public static final String SY_COMM_FAVORITES = "SY_COMM_FAVORITES";
    /** 服务主键：收藏夹-标签 */
    public static final String SY_COMM_FAVORITES_MARK = "SY_COMM_FAVORITES_MARK";
    /** 服务主键：系统文件 */
    public static final String SY_COMM_FILE = "SY_COMM_FILE";
    /** 服务主键：系统文件 */
    public static final String SY_COMM_FILE_DATA = "SY_COMM_FILE_DATA";
    /** 服务主键：系统历史文件 */
    public static final String SY_COMM_FILE_HIS = "SY_COMM_FILE_HIS";
    /** 服务主键：系统通用信息 */
    public static final String SY_COMM_INFO = "SY_COMM_INFO";
    /** 服务主键：本地任务 */
    public static final String SY_COMM_LOCAL_SCHED = "SY_COMM_LOCAL_SCHED";
    /** 服务主键：本地任务_执行日志 */
    public static final String SY_COMM_LOCAL_SCHED_HIS = "SY_COMM_LOCAL_SCHED_HIS";
    /** 服务主键：系统运行日志文件下载 */
    public static final String SY_COMM_LOGS = "SY_COMM_LOGS";
    /** 服务主键：菜单管理 */
    public static final String SY_COMM_MENU = "SY_COMM_MENU";
    /** 服务主键：菜单（公共） */
    public static final String SY_COMM_MENU_PUBLIC = "SY_COMM_MENU_PUBLIC";
    /** 服务主键：系统信息 */
    public static final String SY_COMM_MESSAGE = "SY_COMM_MESSAGE";
    /** 服务主键：意见 */
    public static final String SY_COMM_MIND = "SY_COMM_MIND";
    /** 服务主键：意见编码 */
    public static final String SY_COMM_MIND_CODE = "SY_COMM_MIND_CODE";
    /** 服务主键：固定意见 */
    public static final String SY_COMM_MIND_REGULAR = "SY_COMM_MIND_REGULAR";
    /** 服务主键：意见类型 */
    public static final String SY_COMM_MIND_TYPE = "SY_COMM_MIND_TYPE";
    /** 服务主键：个人常用意见 */
    public static final String SY_COMM_MIND_USUAL = "SY_COMM_MIND_USUAL";
    /** 服务主键：消息监听 */
    public static final String SY_COMM_MSG_LISTENER = "SY_COMM_MSG_LISTENER";
    /** 服务主键：待办提醒方式 */
    public static final String SY_COMM_MSG_TYPE = "SY_COMM_MSG_TYPE";
    /** 服务主键：导出XODC格式化文件流 */
    public static final String SY_COMM_OUTPUT_XDOC = "SY_COMM_OUTPUT_XDOC";
    /** 服务主键：打印模版管理 */
    public static final String SY_COMM_PRINT_TMPL = "SY_COMM_PRINT_TMPL";
    /** 服务主键：最近使用 */
    public static final String SY_COMM_RECENTLY_USE = "SY_COMM_RECENTLY_USE";
    /** 服务主键：提醒消息 */
    public static final String SY_COMM_REMIND = "SY_COMM_REMIND";
    /** 服务主键：提醒消息类型分组 */
    public static final String SY_COMM_REMIND_GROUP = "SY_COMM_REMIND_GROUP";
    /** 服务主键：提醒消息历史 */
    public static final String SY_COMM_REMIND_HIS = "SY_COMM_REMIND_HIS";
    /** 服务主键：提醒我的 */
    public static final String SY_COMM_REMIND_ME = "SY_COMM_REMIND_ME";
    /** 服务主键：被提醒人 */
    public static final String SY_COMM_REMIND_USERS = "SY_COMM_REMIND_USERS";
    /** 服务主键：任务计划 */
    public static final String SY_COMM_SCHED = "SY_COMM_SCHED";
    /** 服务主键：任务计划_任务执行日志 */
    public static final String SY_COMM_SCHED_HIS = "SY_COMM_SCHED_HIS";
    /** 服务主键：任务计划_触发器 */
    public static final String SY_COMM_SCHED_TRIGGER = "SY_COMM_SCHED_TRIGGER";
    /** 服务主键：分发方案表 */
    public static final String SY_COMM_SEND = "SY_COMM_SEND";
    /** 服务主键：分发明细 */
    public static final String SY_COMM_SEND_DETAIL = "SY_COMM_SEND_DETAIL";
    /** 服务主键：分发方案明细 */
    public static final String SY_COMM_SEND_ITEM = "SY_COMM_SEND_ITEM";
    /** 服务主键：分发方案明细部门 */
    public static final String SY_COMM_SEND_ITEM_DEPT = "SY_COMM_SEND_ITEM_DEPT";
    /** 服务主键：分发方案明细角色 */
    public static final String SY_COMM_SEND_ITEM_ROLE = "SY_COMM_SEND_ITEM_ROLE";
    /** 服务主键：分发方案明细用户 */
    public static final String SY_COMM_SEND_ITEM_USER = "SY_COMM_SEND_ITEM_USER";
    /** 服务主键：我发出的提醒 */
    public static final String SY_COMM_SEND_REMIND = "SY_COMM_SEND_REMIND";
    /** 服务主键：分发方案选择 */
    public static final String SY_COMM_SEND_SELECT = "SY_COMM_SEND_SELECT";
    /** 服务主键：分发卡片页面 */
    public static final String SY_COMM_SEND_SHOW_CARD = "SY_COMM_SEND_SHOW_CARD";
    /** 服务主键：分发人员列表 */
    public static final String SY_COMM_SEND_SHOW_USERS = "SY_COMM_SEND_SHOW_USERS";
    /** 服务主键：标签管理 */
    public static final String SY_COMM_TAG = "SY_COMM_TAG";
    /** 服务主键：门户模版 */
    public static final String SY_COMM_TEMPL = "SY_COMM_TEMPL";
    /** 服务主键：模板组件 */
    public static final String SY_COMM_TEMPL_COMS = "SY_COMM_TEMPL_COMS";
    /** 服务主键：组件权限服务列表 */
    public static final String SY_COMM_TEMPL_COMS_ACL = "SY_COMM_TEMPL_COMS_ACL";
    /** 服务主键：模板组件显示位置 */
    public static final String SY_COMM_TEMPL_COMS_DSLOC = "SY_COMM_TEMPL_COMS_DSLOC";
    /** 服务主键：待办事务 */
    public static final String SY_COMM_TODO = "SY_COMM_TODO";
    /** 服务主键：委办事务 */
    public static final String SY_COMM_TODO_AGENT = "SY_COMM_TODO_AGENT";
    /** 服务主键：待我处理 */
    public static final String SY_COMM_TODO_DEADLINE = "SY_COMM_TODO_DEADLINE";
    /** 服务主键：已办事务 */
    public static final String SY_COMM_TODO_HIS = "SY_COMM_TODO_HIS";
    /** 服务主键：已办事务(委托) */
    public static final String SY_COMM_TODO_HIS_AGENT = "SY_COMM_TODO_HIS_AGENT";
    /** 服务主键：待办待阅事务(接口调用) */
    public static final String SY_COMM_TODO_PT = "SY_COMM_TODO_PT";
    /** 服务主键：待阅事务 */
    public static final String SY_COMM_TODO_READ = "SY_COMM_TODO_READ";
    /** 服务主键：常用批语 */
    public static final String SY_COMM_USUAL = "SY_COMM_USUAL";
    /** 服务主键：工作日期设置 */
    public static final String SY_COMM_WORK_DAY = "SY_COMM_WORK_DAY";
    /** 服务主键：系统文件 */
    public static final String SY_MODIFY_FILE = "SY_MODIFY_FILE";
    /** 服务主键：权限控制列表 */
    public static final String SY_ORG_ACL = "SY_ORG_ACL";
    /** 服务主键：公司管理 */
    public static final String SY_ORG_CMPY = "SY_ORG_CMPY";
    /** 服务主键：机构管理(机构内) */
    public static final String SY_ORG_DEPT = "SY_ORG_DEPT";
    /** 服务主键：机构管理(全部) */
    public static final String SY_ORG_DEPT_ALL = "SY_ORG_DEPT_ALL";
    /** 服务主键：部门主管表 */
    public static final String SY_ORG_DEPT_DIRECTOR = "SY_ORG_DEPT_DIRECTOR";
    /** 服务主键：删除部门恢复 */
    public static final String SY_ORG_DEPT_RESTORE = "SY_ORG_DEPT_RESTORE";
    /** 服务主键：机构管理(含下级) */
    public static final String SY_ORG_DEPT_SUB = "SY_ORG_DEPT_SUB";
    /** 服务主键：群组管理 */
    public static final String SY_ORG_GROUP = "SY_ORG_GROUP";
    /** 服务主键：群公告 */
    public static final String SY_ORG_GROUP_NOTICE = "SY_ORG_GROUP_NOTICE";
    /** 服务主键：群组用户 */
    public static final String SY_ORG_GROUP_USER = "SY_ORG_GROUP_USER";
    /** 服务主键：用户登录 */
    public static final String SY_ORG_LOGIN = "SY_ORG_LOGIN";
    /** 服务主键：角色管理 */
    public static final String SY_ORG_ROLE = "SY_ORG_ROLE";
    /** 服务主键：角色管理（全部） */
    public static final String SY_ORG_ROLE_ALL = "SY_ORG_ROLE_ALL";
    /** 服务主键：角色管理（公共） */
    public static final String SY_ORG_ROLE_PUBLIC = "SY_ORG_ROLE_PUBLIC";
    /** 服务主键：角色管理（含下级） */
    public static final String SY_ORG_ROLE_SUB = "SY_ORG_ROLE_SUB";
    /** 服务主键：角色用户表 */
    public static final String SY_ORG_ROLE_USER = "SY_ORG_ROLE_USER";
    /** 服务主键：人员管理(机构内) */
    public static final String SY_ORG_USER = "SY_ORG_USER";
    /** 服务主键：用户委托管理 */
    public static final String SY_ORG_USER_AGENT = "SY_ORG_USER_AGENT";
    /** 服务主键：用户委托业务类型设置 */
    public static final String SY_ORG_USER_AGT_TYPE = "SY_ORG_USER_AGT_TYPE";
    /** 服务主键：人员管理(全部) */
    public static final String SY_ORG_USER_ALL = "SY_ORG_USER_ALL";
    /** 服务主键：用户中心 */
    public static final String SY_ORG_USER_CENTER = "SY_ORG_USER_CENTER";
    /** 服务主键：联系方式 */
    public static final String SY_ORG_USER_CONTACT = "SY_ORG_USER_CONTACT";
    /** 服务主键：工作台设置 */
    public static final String SY_ORG_USER_DESK = "SY_ORG_USER_DESK";
    /** 服务主键：图标化首页 */
    public static final String SY_ORG_USER_DESK_ICON = "SY_ORG_USER_DESK_ICON";
    /** 服务主键：手机桌面设置 */
    public static final String SY_ORG_USER_DESK_MB = "SY_ORG_USER_DESK_MB";
    /** 服务主键：用户浮动菜单 */
    public static final String SY_ORG_USER_FLOATMENU = "SY_ORG_USER_FLOATMENU";
    /** 服务主键：用户头像 */
    public static final String SY_ORG_USER_IMG = "SY_ORG_USER_IMG";
    /** 服务主键：用户查阅 */
    public static final String SY_ORG_USER_INFO = "SY_ORG_USER_INFO";
    /** 服务主键：个人信息 */
    public static final String SY_ORG_USER_INFO_SELF = "SY_ORG_USER_INFO_SELF";
    /** 服务主键：基本信息 */
    public static final String SY_ORG_USER_INFO_SELF_ALL = "SY_ORG_USER_INFO_SELF_ALL";
    /** 服务主键：兼岗设置 */
    public static final String SY_ORG_USER_JIANGANG = "SY_ORG_USER_JIANGANG";
    /** 服务主键：秘书设置 */
    public static final String SY_ORG_USER_MISHU = "SY_ORG_USER_MISHU";
    /** 服务主键：在线用户管理 */
    public static final String SY_ORG_USER_ONLINE = "SY_ORG_USER_ONLINE";
    /** 服务主键：用户密码 */
    public static final String SY_ORG_USER_PASSWD = "SY_ORG_USER_PASSWD";
    /** 服务主键：用户关系表 */
    public static final String SY_ORG_USER_RELATION = "SY_ORG_USER_RELATION";
    /** 服务主键：删除用户恢复 */
    public static final String SY_ORG_USER_RESTORE = "SY_ORG_USER_RESTORE";
    /** 服务主键：教育经历 */
    public static final String SY_ORG_USER_RESUME = "SY_ORG_USER_RESUME";
    /** 服务主键：工作经历 */
    public static final String SY_ORG_USER_RESUME_WORK = "SY_ORG_USER_RESUME_WORK";
    /** 服务主键：奖惩情况 */
    public static final String SY_ORG_USER_REWARD = "SY_ORG_USER_REWARD";
    /** 服务主键：用户状态信息 */
    public static final String SY_ORG_USER_STATE = "SY_ORG_USER_STATE";
    /** 服务主键：风格设定 */
    public static final String SY_ORG_USER_STYLE = "SY_ORG_USER_STYLE";
    /** 服务主键：人员管理(含下级) */
    public static final String SY_ORG_USER_SUB = "SY_ORG_USER_SUB";
    /** 服务主键：用户业务委托管理 */
    public static final String SY_ORG_USER_TYPE_AGENT = "SY_ORG_USER_TYPE_AGENT";
    /** 服务主键：委托列表 */
    public static final String SY_ORG_USER_TYPE_AGENT_FROM = "SY_ORG_USER_TYPE_AGENT_FROM";
    /** 服务主键：委托我的 */
    public static final String SY_ORG_USER_TYPE_AGENT_TO = "SY_ORG_USER_TYPE_AGENT_TO";
    /** 服务主键：工作交接 */
    public static final String SY_ORG_WORK_HANDOVER = "SY_ORG_WORK_HANDOVER";
    /** 服务主键：报表服务 */
    public static final String SY_PLUG_REPORT = "SY_PLUG_REPORT";
    /** 服务主键：报表服务字段项 */
    public static final String SY_PLUG_REPORT_ITEM = "SY_PLUG_REPORT_ITEM";
    /** 服务主键：搜索 */
    public static final String SY_PLUG_SEARCH = "SY_PLUG_SEARCH";
    /** 服务主键：搜索-个性化数据 */
    public static final String SY_PLUG_SEARCH_CUSTOM = "SY_PLUG_SEARCH_CUSTOM";
    /** 服务主键：检索关联服务 */
    public static final String SY_PLUG_SEARCH_LINK = "SY_PLUG_SEARCH_LINK";
    /** 服务主键：互联网 */
    public static final String SY_PLUG_SEARCH_WEB = "SY_PLUG_SEARCH_WEB";
    /** 服务主键：互联网抓取服务 */
    public static final String SY_PLUG_SEARCH_WEBCRAW = "SY_PLUG_SEARCH_WEBCRAW";
    /** 服务主键：全文检索词库 */
    public static final String SY_PLUG_SEARCH_WORD = "SY_PLUG_SEARCH_WORD";
    /** 服务主键：服务定义 */
    public static final String SY_SERV = "SY_SERV";
    /** 服务主键：服务按钮 */
    public static final String SY_SERV_ACT = "SY_SERV_ACT";
    /** 服务主键：按钮图标 */
    public static final String SY_SERV_ACT_ICONS = "SY_SERV_ACT_ICONS";
    /** 服务主键：参数定义 */
    public static final String SY_SERV_ACT_PARAM = "SY_SERV_ACT_PARAM";
    /** 服务主键：服务方法查询 */
    public static final String SY_SERV_ACT_QUERY = "SY_SERV_ACT_QUERY";
    /** 服务主键：评论表 */
    public static final String SY_SERV_COMMENT = "SY_SERV_COMMENT";
    /** 服务主键：评论表_基于权限过滤 */
    public static final String SY_SERV_COMMENT_ACL = "SY_SERV_COMMENT_ACL";
    /** 服务主键：评论支持反对 */
    public static final String SY_SERV_COMMENT_VOTE = "SY_SERV_COMMENT_VOTE";
    /** 服务主键：数据权限定义 */
    public static final String SY_SERV_DACL = "SY_SERV_DACL";
    /** 服务主键：数据权限 */
    public static final String SY_SERV_DACL_ITEM = "SY_SERV_DACL_ITEM";
    /** 服务主键：数据字典 */
    public static final String SY_SERV_DICT = "SY_SERV_DICT";
    /** 服务主键：内置字典 */
    public static final String SY_SERV_DICT_INNER = "SY_SERV_DICT_INNER";
    /** 服务主键：字典数据信息表 */
    public static final String SY_SERV_DICT_ITEM = "SY_SERV_DICT_ITEM";
    /** 服务主键：字典项（公司内） */
    public static final String SY_SERV_DICT_ITEM_CMPY = "SY_SERV_DICT_ITEM_CMPY";
    /** 服务主键：流经 */
    public static final String SY_SERV_FLOW = "SY_SERV_FLOW";
    /** 服务主键：内置服务 */
    public static final String SY_SERV_INNER = "SY_SERV_INNER";
    /** 服务主键：服务项 */
    public static final String SY_SERV_ITEM = "SY_SERV_ITEM";
    /** 服务主键：服务字段查询 */
    public static final String SY_SERV_ITEM_QUERY = "SY_SERV_ITEM_QUERY";
    /** 服务主键：服务关联定义 */
    public static final String SY_SERV_LINK = "SY_SERV_LINK";
    /** 服务主键：关联明细设定 */
    public static final String SY_SERV_LINK_ITEM = "SY_SERV_LINK_ITEM";
    /** 服务主键：服务监听 */
    public static final String SY_SERV_LISTENER = "SY_SERV_LISTENER";
    /** 服务主键：操作留痕 */
    public static final String SY_SERV_LOG_ACT = "SY_SERV_LOG_ACT";
    /** 服务主键：变更监控 */
    public static final String SY_SERV_LOG_ITEM = "SY_SERV_LOG_ITEM";
    /** 服务主键：变更监控 */
    public static final String SY_SERV_LOG_ITEM_SINGLE = "SY_SERV_LOG_ITEM_SINGLE";
    /** 服务主键：服务定义（全部） */
    public static final String SY_SERV_PUBLIC = "SY_SERV_PUBLIC";
    /** 服务主键：常用查询 */
    public static final String SY_SERV_QUERY = "SY_SERV_QUERY";
    /** 服务主键：相关文件 */
    public static final String SY_SERV_RELATE = "SY_SERV_RELATE";
    /** 服务主键：全文检索设置 */
    public static final String SY_SERV_SEARCH = "SY_SERV_SEARCH";
    /** 服务主键：数据库表 */
    public static final String SY_SERV_TABLE = "SY_SERV_TABLE";
    /** 服务主键：数据表字段 */
    public static final String SY_SERV_TABLE_COL = "SY_SERV_TABLE_COL";
    /** 服务主键：服务过滤规则 */
    public static final String SY_SERV_WHERE = "SY_SERV_WHERE";
    /** 服务主键：自定义变量 */
    public static final String SY_WFE_CUSTOM_VAR = "SY_WFE_CUSTOM_VAR";
    /** 服务主键：节点连线定义表 */
    public static final String SY_WFE_LINE_DEF = "SY_WFE_LINE_DEF";
    /** 服务主键：流程节点和ACT关联表 */
    public static final String SY_WFE_NODE_ACT = "SY_WFE_NODE_ACT";
    /** 服务主键：节点定义表 */
    public static final String SY_WFE_NODE_DEF = "SY_WFE_NODE_DEF";
    /** 服务主键：节点实例表 */
    public static final String SY_WFE_NODE_INST = "SY_WFE_NODE_INST";
    /** 服务主键：节点实例历史表 */
    public static final String SY_WFE_NODE_INST_HIS = "SY_WFE_NODE_INST_HIS";
    /** 服务主键：公共按钮表 */
    public static final String SY_WFE_NODE_PACTS = "SY_WFE_NODE_PACTS";
    /** 服务主键：节点实例用户信息 */
    public static final String SY_WFE_NODE_USERS = "SY_WFE_NODE_USERS";
    /** 服务主键：节点实例用户历史信息 */
    public static final String SY_WFE_NODE_USERS_HIS = "SY_WFE_NODE_USERS_HIS";
    /** 服务主键：流程服务，提供流程运行相关操作 */
    public static final String SY_WFE_PROC = "SY_WFE_PROC";
    /** 服务主键：流程定义 */
    public static final String SY_WFE_PROC_DEF = "SY_WFE_PROC_DEF";
    /** 服务主键：流程公用按钮 */
    public static final String SY_WFE_PROC_DEF_ACT = "SY_WFE_PROC_DEF_ACT";
    /** 服务主键：流程版本 */
    public static final String SY_WFE_PROC_DEF_HIS = "SY_WFE_PROC_DEF_HIS";
    /** 服务主键：流程管理 */
    public static final String SY_WFE_PROC_DEF_PUBLIC = "SY_WFE_PROC_DEF_PUBLIC";
    /** 服务主键：流程实例表 */
    public static final String SY_WFE_PROC_INST = "SY_WFE_PROC_INST";
    /** 服务主键：流程实例历史表 */
    public static final String SY_WFE_PROC_INST_HIS = "SY_WFE_PROC_INST_HIS";
    /** 服务主键：子流程节点配置信息 */
    public static final String SY_WFE_PROC_NODE_DEF = "SY_WFE_PROC_NODE_DEF";
    /** 服务主键：催办单 */
    public static final String SY_WFE_REMIND = "SY_WFE_REMIND";
    /** 服务主键：催办单进展情况 */
    public static final String SY_WFE_REMIND_PROC = "SY_WFE_REMIND_PROC";
    /** 服务主键：催办单查询 */
    public static final String SY_WFE_REMIND_SEARCH = "SY_WFE_REMIND_SEARCH";
    /** 服务主键：流程测试 */
    public static final String SY_WFE_TEST = "SY_WFE_TEST";
    /** 服务主键：流程跟踪 */
    public static final String SY_WFE_TRACK = "SY_WFE_TRACK";
    /** 服务主键：图形化流程跟踪 */
    public static final String SY_WFE_TRACK_FIGURE = "SY_WFE_TRACK_FIGURE";
    
    /** 操作：服务定义 */
    public static final String ACT_SERV = "serv";
    /** 操作：查询 */
    public static final String ACT_QUERY = "query";
    /** 操作：基于主键获取数据 */
    public static final String ACT_BYID = "byid";
    /** 操作：删除 */
    public static final String ACT_DELETE = "delete";
    /** 操作：添加 */
    public static final String ACT_ADD = "add";
    /** 操作：保存（更新保存或者插入保存） */
    public static final String ACT_SAVE = "save";
    /** 操作：批量保存（同时支持删除、插入和更新） */
    public static final String ACT_BATCHSAVE = "batchSave";
    /** 操作：根据参数查询数据，不分页 */
    public static final String ACT_FINDS = "finds";
    /** 操作：根据参数查询数量 */
    public static final String ACT_COUNT = "count";
    /** 操作：删除待办*/
    public static final String ACT_DESTROY = "destroys";
    
    
    
    /** log */
    private static Log log = LogFactory.getLog(ServMgr.class);
   
    /**
     * 服务执行入口方法，内部提供事务以及记录操作历史的支持。
     * @param paramBean 请求参数封装实体
     * @return 执行结果
     */
    public static OutBean act(ParamBean paramBean) {
        return act(paramBean.getServId(), paramBean.getAct(), paramBean);
    }

    /**
     * 服务执行入口方法，内部提供事务以及记录操作历史的支持。
     * @deprecated 建议使用act(ParamBean param)做参数的方法
     * @param servId 服务服务名
     * @param act 执行方法名
     * @param param 请求参数封装实体
     * @return 执行结果
     */
    public static Bean act(String servId, String act, Bean param) {
        return act(servId, act, new ParamBean(param));
    }
    
    /**
     * 服务执行入口方法，内部提供事务以及记录操作历史的支持。
     * @param servId 服务服务名
     * @param act 执行方法名
     * @param paramBean 请求参数封装实体
     * @return 执行结果
     */
    public static OutBean act(String servId, String act, ParamBean paramBean) {
    	OutBean result = null;
    	ServDefBean servDef = ServUtils.getServDef(servId);
        if (paramBean.contains("_CLIENT_REQ_")) { //如果是客户端请求才需要判断权限
            paramBean.remove("_CLIENT_REQ_"); //移除客户端访问标记，避免影响其他操作
            if (!OrgMgr.checkServAuth(servDef)) {
                throw new TipException(Context.getSyMsg("SY_SERV_AUTH_ERROR", servDef.getName(), servId)
                		+ "，登录用户：" + (Context.getUserBean() == null ? null : Context.getUserBean().getName())
                		+ "，访问节点：" + System.getProperty("servName"));
            }
        }
        
        boolean verifyFlag = Context.getSyConf("SY_SERVER_VERIFY_FLAG", false);
		if (verifyFlag && act.equals(ACT_SAVE) && !verify(paramBean, servDef)) {
			throw new RuntimeException("参数格式不正确");
		}
        
    	long beginTime = System.currentTimeMillis();
    	boolean bTrans = paramBean.getTransFlag(); //是否启用事务处理
    	String ds = servDef.getDataSource();
    	if (!bTrans && ds.length() > 0) { //如果没启用事务且使用自定义数据源，则强制启用事务
    		bTrans = true;
    	}
        // 事务处理
        if (bTrans) {
        	Transaction.begin(ds);
        	paramBean.setTransFlag(false); //清除参数的事务标志，确保不被误传递
        }
        Object servClass = servDef.getServClass();
        try {
            paramBean.set(Constant.PARAM_SERV_ID, servId);
            paramBean.set(Constant.PARAM_ACT_CODE, act);
            //服务监听before
            ServLisMgr.getInstance().before(servId, act, paramBean);
            Object rtn = doMethod(servClass, act, paramBean);
            if (rtn == null) {
                result = new OutBean().setError(Context.getSyMsg("SY_RETURN_NULL"));
            } else if (rtn instanceof OutBean) {
                result = (OutBean) rtn;
            } else {
                result = new OutBean((Bean) rtn);
            }

            if (bTrans) {
            	Transaction.commit();
            }
            
            //服务监听after
            ServLisMgr.getInstance().after(servId, act, paramBean, result);
            long time = System.currentTimeMillis() - beginTime;
            result.set(Constant.RTN_TIME, ((float) (time + 1) / 1000)); //增加1毫秒的servlet处理
            ServUtils.actLog(servDef, act, paramBean.getId(), time); //记录操作历史及时效
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("SERV act error:" + servId + "." + act);
                log.debug(e.getMessage());
            }
            if (e.getCause() instanceof TipException) {
                throw (TipException) (e.getCause());
            } else if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException(Context.getSyMsg("SY_RUN_ERROR", 
                        servClass.getClass().getName() + "." + act), e);
            }
        } finally {
            if (bTrans) {
            	Transaction.end();
            }
        }
        return result;
    }
    
    /**
     * 提供给JSP或者前端获取服务定义信息
     * @param servId 服务编码
     * @return 提供给前端的服务定义信息
     */
    public static OutBean servDef(String servId) {
        try {
            ServDefBean servDef = ServUtils.getServDef(servId);
            Object servClass = servDef.getServClass();
            return (OutBean) doMethod(servClass, "serv", new ParamBean(servId));
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return new OutBean().setError(msg);
        }
    }
    
    /**
     * 监听类执行入口
     * @param obj 监听类实例
     * @param methodName 方法名
     * @param paramBean 请求参数封装实体
     * @return 执行结果
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    private static Object doMethod(Object obj, String methodName, ParamBean paramBean) 
            throws IllegalAccessException, InvocationTargetException {
    	Object result = null;
        Method method = null;
        Object[] params = null;
        Class<?> cls = obj.getClass();
        try {
            method = cls.getMethod(methodName, Bean.class);
            params = new Object[] {paramBean};
        } catch (Exception e) {
            try {
                method = cls.getMethod(methodName, ParamBean.class);
                params = new Object[] {paramBean};
            } catch (Exception e2) {
                try {
                    method = cls.getMethod(methodName);
                    params = new Object[] { };
                } catch (Exception e3) {
                    method = null;
                }
            }
        }
        if (method != null) {
            result = method.invoke(obj, params);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(Context.getSyMsg("SY_RUN_ERROR", obj.getClass().getName() + "." + methodName
                        + paramBean.toString() + ";指定方法不存在:" + methodName));
            }
        }
        return result;
    }
    
    private static boolean verify(ParamBean paramBean, ServDefBean servDef) {
		List<Bean> itemList = servDef.getTableItems();
		for (Bean item : itemList) {
			if (paramBean.containsKey(item.getStr("ITEM_CODE")) && item.getInt("ITEM_NOTNULL") == 1
					&& item.getInt("ITEM_HIDDEN") == 2 && paramBean.isEmpty(item.getStr("ITEM_CODE"))) {
				return false;
			}
		}
		return true;
	}

}