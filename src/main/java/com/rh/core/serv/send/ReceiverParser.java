package com.rh.core.serv.send;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.Strings;

/**
 * 
 * @author wanglong
 * 
 */
public class ReceiverParser {
    /** log */
    private static Log log = LogFactory.getLog(SendCommonServ.class);

    /** 所选择的角色KEY */
    protected static final String TARGET_ROLE = "TARGET_ROLE";

    /** 本机构用户 KEY */
    protected static final String TARGET_USERS = "TARGET_USERS";

    /** 所选择的本机构部门 KEY */
    protected static final String TARGET_DEPTS = "TARGET_DEPTS";

    /** 其它机构部门 KEY **/
    protected static final String ALL_TARGET_DEPTS = "ALL_TARGET_DEPTS";

    /** 本机构 + 其它机构用户 **/
    protected static final String ALL_TARGET_USERS = "ALL_TARGET_USERS";


    /** 部门收件人角色配置 **/
    private static final String BUMEN_RECEIVER_ROLE = "SY_COMM_SEND_BUMEN_RECEIVER_ROLE";
    
    /** 公司收件人角色配置 **/
    private static final String ORG_RECEIVER_ROLE = "SY_COMM_SEND_ORG_RECEIVER_ROLE";
    
    /**分发范围：本单位**/
    private static final String SCOPE_ODEPT = "ODEPT";
    

    /** 记录用户CODE列表，用于处理用户是否重复 **/
    private List<String> cached = new ArrayList<String>();
    
    /** 接收用户列表 **/
    private List<UserBean> userBeans = new ArrayList<UserBean>();
    
    /**分发范围，可能的值为：null,无限制;ODEPT,本单位**/
    private String scope = "";
    
    /** 办理用户 **/
    private UserBean doUserBean = null;

    /**
     * 
     * @param paramBean 参数Bean
     * @return 取得分发接收用户列表
     */
    public List<UserBean> getUserBeanList(Bean paramBean) {
        this.scope = paramBean.getStr("SCOPE");
        this.doUserBean = Context.getUserBean();

        String fromScheme = paramBean.getStr("fromScheme");
        if (fromScheme != null && fromScheme.equals("yes")) {
            // 来源于方案
            createUsersBeanListByScheme(paramBean);
        } else {
            // 手工选择用户
            createUserBeanListBySelect(paramBean);
        }
        
        return this.userBeans;
    }

    /**
     * 取得用户手工选择的所有接收用户。
     * @param paramBean param Bean
     */
    private void createUserBeanListBySelect(Bean paramBean) {
        // 增加选中的用户
        addSelectedUser(paramBean);

        // 增加选中的其它用户
        addOtherSelectedUser(paramBean);

        // 增加选中的机构内部门 + 角色
        addSelectedDeptRole(paramBean);

        // 增加选择的部门，没选择角色
        addSelectedDept(paramBean);

        // 增加选中的角色，没选择部门
        addSelectedRole(paramBean);

        // 增加选中的所有机构的部门 + 角色
        addSelectedOtherDeptRole(paramBean);
        
        //增加选中的所有机构的部门
        addSelectedOtherDept(paramBean);
    }

    /**
     * 根据方案ID取得包含的所有用户--机构内
     * @param paramBean 参数bean
     */
    private void createUsersBeanListByScheme(Bean paramBean) {
        // 分发方案ID
        String sendId = paramBean.getStr("SEND_ID");

        String[] sendIdArr = sendId.split(",");
        List<String> depts = new ArrayList<String>();
        List<String> sendCodes = new ArrayList<String>();

        for (String send : sendIdArr) {
            // 以分发方案前缀开始的字符串
            if (send.startsWith(SendConstant.PREFIX_SCHM)) {
                String schm = send.substring(SendConstant.PREFIX_SCHM.length());
                sendCodes.add(schm);
                continue;
            } else if (send.startsWith(SendConstant.PREFIX_DEPT)) {
                //以部门前缀开始的字符串
                String deptId = send.substring(SendConstant.PREFIX_DEPT.length());
                depts.add(deptId);
                continue;
            }
            
            try {
                // 如果ID代表用户
                UserBean user = UserMgr.getUser(send);
                if (user != null) { // 如果ID是用户
                    this.appendUser(user);
                    continue;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            
            try {
                // 如果ID代表部门，则
                DeptBean dept = OrgMgr.getDept(send);
                if (dept != null) { // 部门
                    if (dept.getType() == OrgConstant.DEPT_TYPE_ORG) {
                        appendOdeptUser(dept);
                        continue;
                    } else {
                        depts.add(send);
                        continue;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            
            //兼容其它老的分发方案数据
            sendCodes.add(send);
        }

        appendDept(depts); // 部门
        appendSendSchema(sendCodes); // 分发方案
    }
    
    /**
     * 
     * @param dept 机构对象
     */
    private void appendOdeptUser(DeptBean dept) {
        String roleCode = Context.getSyConf(ORG_RECEIVER_ROLE, "");
        List<UserBean> userList = UserMgr.getUsersInOdept(dept.getCode(), roleCode);
        appendUserList(userList);
    }

    /**
     * 增加选中的角色 + 部门内的用户
     * @param paramBean 参数
     */
    private void addSelectedDeptRole(Bean paramBean) {
        if (paramBean.isEmpty(TARGET_ROLE)) {
            return;
        }

        String role = getRoleParam(paramBean);
        // 机构内的部门+角色的组合，查询出所有的人进行分发
        if (!role.isEmpty() && paramBean.isNotEmpty(TARGET_DEPTS)) {
            List<UserBean> usersOfDepts = UserMgr.getUsersInOdept(paramBean.getStr(TARGET_DEPTS),
                    role);
            appendUserList(usersOfDepts);
        }
    }

    /**
     * 增加选中的其它用户（本单位 + 外单位）
     * @param paramBean 参数
     */
    private void addOtherSelectedUser(Bean paramBean) {
        if (paramBean.isEmpty(ALL_TARGET_USERS)) {
            return;
        }

        String strUsers = paramBean.getStr(ALL_TARGET_USERS).trim();
        String[] users = strUsers.split(",");

        for (String user : users) {
            if (0 == user.length()) {
                continue;
            }

            UserBean userBean = null;
            try {
                userBean = UserMgr.getUser(user);
            } catch (Exception ignore) {
                log.warn(" get user error.", ignore);
            }
            appendUser(userBean);
        }
    }

    /**
     * 增加选中的用户
     * @param paramBean 参数
     */
    private void addSelectedUser(Bean paramBean) {
        if (paramBean.isEmpty(TARGET_USERS)) {
            return;
        }

        String[] users = getUsersParam(paramBean);

        for (String user : users) {
            if (0 == user.length()) {
                continue;
            }

            UserBean userBean = null;
            try {
                userBean = UserMgr.getUser(user);
            } catch (Exception ignore) {
                log.warn(" get user error.", ignore);
            }
            appendUser(userBean);
        }
    }

    /**
     * 按部门分发，没指定角色，只指定了部门。增加指定部门下，且在同一个机构内的用户列表。
     * @param paramBean 参数
     */
    private void addSelectedDept(Bean paramBean) {
        if (paramBean.isNotEmpty(TARGET_ROLE)) { // 如果存在角色则返回
            return;
        }

        // 机构内的部门+角色的组合，查询出所有的人进行分发
        if (paramBean.isNotEmpty(TARGET_DEPTS)) {
            List<UserBean> usersOfDepts = UserMgr.getUsersInDepts(paramBean.getStr(TARGET_DEPTS));
            appendUserList(usersOfDepts);
        }
    }

    /**
     * 按角色分发，没有指定部门。增加指定角色内，且在同一个机构内的用户列表。
     * @param paramBean 参数
     */
    private void addSelectedRole(Bean paramBean) {
        if (paramBean.isNotEmpty(TARGET_DEPTS)) { // 如果存在部门则返回
            return;
        }

        if (paramBean.isEmpty(TARGET_ROLE)) {
            return;
        }

        List<UserBean> usersOfDepts = UserMgr.getUsersInOdept(Context.getUserBean().getODeptCode(),
                paramBean.getStr(TARGET_ROLE));
        appendUserList(usersOfDepts);
    }

    /**
     * 按部门分发，没指定角色，只指定了部门。增加指定部门下，且在同一个机构内的用户列表。
     * @param paramBean 参数
     */
    private void addSelectedOtherDept(Bean paramBean) {
        if (paramBean.isNotEmpty(TARGET_ROLE)) { // 如果存在角色则返回
            return;
        }

        if (paramBean.isEmpty(ALL_TARGET_DEPTS)) {
            return;
        }

        // 机构内的部门+角色的组合，查询出所有的人进行分发
        String depts = paramBean.getStr(ALL_TARGET_DEPTS);
        List<UserBean> usersOfDepts = UserMgr.getUsersInDepts(depts);
        appendUserList(usersOfDepts);
    }

    /**
     * 取得机构外的部门+角色
     * @param paramBean 参数
     */
    private void addSelectedOtherDeptRole(Bean paramBean) {
        if (paramBean.isEmpty(ALL_TARGET_DEPTS)) {
            return;
        }

        String role = getRoleParam(paramBean);
        if (role.isEmpty()) {
            return;
        }

        String otherDepts = paramBean.getStr(ALL_TARGET_DEPTS);
        try {
            // 取得角色对象
            List<UserBean> userList = UserMgr.getUsersInOdept(otherDepts, role);
            appendUserList(userList);
        } catch (Exception ignore) {
            log.warn(" get user error.", ignore);
        }
    }

    /**
     * add user into list
     * 
     * @param targetUser target user bean
     */
    private void appendUser(UserBean targetUser) {
        if (null == targetUser) {
            return;
        }
        if (cached.contains(targetUser.getCode())) {
            return;
        } 
            
        if (StringUtils.isNotEmpty(scope)) {
            if (scope.equals(SCOPE_ODEPT)) { 
                //分发范围为本机构，则与分发人不在一个机构的用户都忽略
                if (!this.doUserBean.getODeptCode().equals(targetUser.getODeptCode())) {
                    return;
                }
            }
        }
        
        userBeans.add(targetUser);
        cached.add(targetUser.getCode());
    }

    /**
     * 
     * @param usersOfDepts 需要加入的用户列表
     */
    private void appendUserList(List<UserBean> usersOfDepts) {
        for (UserBean userBean : usersOfDepts) {
            appendUser(userBean);
        }
    }

    /**
     * 
     * 取出指定部门的部门综合 放到UserBeans列表中
     * @param deptCodes 带前缀的部门ID
     */
    private void appendDept(List<String> deptCodes) {

        String strDeptCodes = Strings.toString(deptCodes);

        String roleCode = Context.getSyConf(BUMEN_RECEIVER_ROLE, "");
        //
        List<UserBean> userList = UserMgr.getUsersInOdept(strDeptCodes, roleCode);
        appendUserList(userList);
    }

    /**
     * 增加分发方案中包含的用户
     * @param sendCodes 分发方案ID列表
     */
    private void appendSendSchema(List<String> sendCodes) {
        String where = " AND SEND_ID in('" + Strings.toString(sendCodes, "','") + "')";
        List<Bean> sendItemList = ServDao.finds(ServMgr.SY_COMM_SEND_ITEM, where);

        for (Bean itemBean : sendItemList) {
            if (itemBean.getStr("ITEM_TYPE").equals(Constant.USER)) { // 如果是user类型，则直接添加到返回的用户集合中
                try {
                    UserBean userBean = UserMgr.getUser(itemBean.getStr("ROLE_USER_CODE"));
                    appendUser(userBean);
                } catch (Exception ignore) {
                    log.warn(" get user error.", ignore);
                }

            } else if (itemBean.getStr("ITEM_TYPE").equals(Constant.DEPT)) { // 如果是部门，则需要从部门中查到所有的人，添加到集合中
                List<UserBean> userList = UserMgr.getUsersByDept(itemBean.getStr("ROLE_USER_CODE"));
                appendUserList(userList);
            } else if (itemBean.getStr("ITEM_TYPE").equals(Constant.ROLE)) { // 如果是角色，则根据角色和部门查到对应的人，添加到集合中
                List<UserBean> userList = null;
                if (itemBean.getStr("DEPT_CODES") != null && itemBean.getStr("DEPT_CODES").length() > 0) {
                    userList = UserMgr.getUsersInOdept(itemBean.getStr("DEPT_CODES"),
                            itemBean.getStr("ROLE_USER_CODE"));
                } else {
                    // 如果没有设置部门，则使用本机构作为部门过滤条件，避免出现错误。
                    UserBean user = Context.getUserBean();
                    userList = UserMgr.getUsersInOdept(user.getODeptCode(), itemBean.getStr("ROLE_USER_CODE"));
                }
                appendUserList(userList);
            }
        }
    }

    /**
     * get target send users params
     * 
     * @param paramBean paran Bean
     * @return String[] target sends user ids
     */
    private String[] getUsersParam(Bean paramBean) {
        String users = paramBean.getStr(TARGET_USERS).trim();
        String[] usersArray = users.split(",");
        return usersArray;
    }

    /**
     * get target send role param
     * 
     * @param paramBean paran Bean
     * @return String[] target send role id
     */
    private String getRoleParam(Bean paramBean) {
        String roleId = paramBean.getStr(TARGET_ROLE);
        return roleId;
    }
}
