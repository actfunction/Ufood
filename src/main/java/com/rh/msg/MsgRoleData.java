package com.rh.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

/**
 * *角色信息
 *
 * @author kfzx-wuyj
 */
public class MsgRoleData {

    // 记录历史
    private static Log log = LogFactory.getLog(MsgRoleData.class);
    // 状态标识
    private static final int ACTIVE = 1;
    private static final int LOCKED = 2;

    /**
     * 保存角色信息进临时表
     *
     * @param roleList
     */
    public static void saveRoleToTemp(List<Bean> roleList) {
        Transaction.begin();
        List<Bean> roleLists = new ArrayList<Bean>();
        try {
            log.info("MSGDATAMODIFY saveRoleToTemp list start ");
            for (Bean payload : roleList) {
                SqlBean dataBean = new SqlBean();
                dataBean.set("ID", payload.getStr("id"));
                dataBean.set("CODE", payload.get("code"));
                dataBean.set("NAME", payload.get("name"));
                dataBean.set("SORT", payload.get("sort"));
                dataBean.set("TYPE", payload.get("type"));
                dataBean.set("ORG_ID", payload.get("org_id"));
                dataBean.set("MEMO", payload.get("memo"));
                dataBean.set("DOMAIN_ID", payload.get("domain_id"));
                dataBean.set("METHON", payload.get("methon"));
                roleLists.add(dataBean);
            }
            ServDao.creates(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, roleLists);
            Transaction.commit();
            log.info("MSGDATAMODIFY saveRoleToTemp list end ");
        } catch (Exception e) {
            Transaction.rollback();
            log.error("MSGDATAMODIFY saveRoleToTemp list ERROR:" + e.getMessage());
        }
    }

    /**
     * 逐条处理角色数据
     *
     * @param payload
     * @param method
     */
    public static void modifyOneRoleData(Bean payload, String method) {
        log.info("MSGDATAMODIFY modifyOneRoleData start:" + payload.get("ID") + ";MERHOD:" + method);
        String id = payload.getStr("id");
        Transaction.begin();
        try {
        	saveRoleToTemp(payload, method);
            if (MsgModifyUtil.CREATE_ONE.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authRoleBean = ServDao.find(MsgModifyUtil.SYS_AUTH_ROLE,sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("ROLE_CODE", id);
				Bean oaRoleBean = ServDao.find(MsgModifyUtil.OA_SY_ORG_ROLE,sqlBean);
				if (null ==authRoleBean ||  authRoleBean.isEmpty()) {
					addRolesToAuth(payload);
				}
				if (null ==oaRoleBean ||  oaRoleBean.isEmpty()) {
					addRolesToSy(payload);
				}
            	
            }
            if (MsgModifyUtil.UPDATE_ONE.equals(method) || MsgModifyUtil.UPDATE_ONE_4ADMIN.equals(method)) {
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authRoleBean = ServDao.find(MsgModifyUtil.SYS_AUTH_ROLE,sqlBean);
				sqlBean = new SqlBean();
				sqlBean.and("ROLE_CODE", id);
				Bean oaRoleBean = ServDao.find(MsgModifyUtil.OA_SY_ORG_ROLE,sqlBean);
				if (null ==authRoleBean ||  authRoleBean.isEmpty()) {
					addRolesToAuth(payload);
				}else {
					updateRolesToAuth(payload);
				}
				if (null ==oaRoleBean ||  oaRoleBean.isEmpty()) {
					addRolesToSy(payload);
				}else {
					updateRolesToSy(payload);
				}
            	
            }
            if (MsgModifyUtil.DELETE_ONE.equals(method)) {
                deleteRolesToAuth(payload);
                deleteRolesToSy(payload);
            }
            Transaction.commit();
        } catch (Exception e) {
            Transaction.rollback();
            e.printStackTrace();
        }

    }


    /**
     * 逐条新增数据到统一认证表
     *
     * @param payload
     * @throws Exception
     */
    private static void addRolesToAuth(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyOrgData start addRolesToAuth");
        try {
            // 新增角色数据
            Bean dataBean = new Bean();
            dataBean.set("ID", payload.getStr("id"));
            dataBean.set("CODE", payload.get("code"));
            dataBean.set("NAME", payload.get("name"));
            dataBean.set("SORT", payload.get("sort"));
            dataBean.set("TYPE", payload.get("type"));
            dataBean.set("ORG_ID", payload.get("org_id"));
            dataBean.set("MEMO", payload.get("memo"));
            dataBean.set("DOMAIN_ID", payload.get("domain_id"));
            dataBean.set("METHON", payload.get("methon"));
            dataBean.set("S_MTIME", MsgModifyUtil.getTime());
            ServDao.create(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end addRoleToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY addRoleToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条增加角色数据进系统表
     *
     * @param payload
     * @throws Exception
     */
    private static void addRolesToSy(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start addRolesToSy");
        try {
        	// 新增角色数据到对照表
            Bean dataBean = new Bean();
            dataBean.set("AUTH_ROLE_CODE", payload.getStr("id"));
            dataBean.set("SY_ROLE_CODE", payload.getStr("id"));
            dataBean.set("S_MTIME", MsgModifyUtil.getTime());
            ServDao.create(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, dataBean);
            // 新增角色数据
            dataBean = new Bean();
            dataBean.set("ROLE_CODE", payload.getStr("id"));
            dataBean.set("ROLE_NAME", payload.get("name"));
            dataBean.set("ROLE_SORT", payload.get("sort"));
            dataBean.set("TYPE", payload.get("type"));
            dataBean.set("ROLE_DEPT", payload.get("org_id"));
            dataBean.set("MEMO", payload.get("memo"));
            dataBean.set("S_FLAG", MsgModifyUtil.getSFlag(payload.getStr("status")));
            dataBean.set("S_MTIME", MsgModifyUtil.getTime());
            ServDao.create(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end addRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY addRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }


    /**
     * 逐条修改角色数据到统一认证表
     *
     * @param payload
     * @throws Exception
     */
    private static void updateRolesToAuth(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start updateRolesToAuth");
        try {
        	SqlBean dataBean = new SqlBean();
            dataBean.and("ID", payload.getStr("id"));
            dataBean.set("CODE", payload.get("code"));
            dataBean.set("NAME", payload.get("name"));
            dataBean.set("SORT", payload.get("sort"));
            dataBean.set("TYPE", payload.get("type"));
            dataBean.set("ORG_ID", payload.get("org_id"));
            dataBean.set("MEMO", payload.get("memo"));
            dataBean.set("DOMAIN_ID", payload.get("domain_id"));
            dataBean.set("METHON", payload.get("methon"));
            ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end updateRolesToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY updateRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * 逐条修改角色数据到系统表
     *
     * @param payload
     * @throws Exception
     */
    private static void updateRolesToSy(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start updateRolesToSy");
        try {
        	SqlBean sql = new SqlBean();
        	sql.and("AUTH_ROLE_CODE", payload.getStr("id"));
        	List<Bean> roleLists = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
        	for (Bean roleBean :roleLists) {
        		SqlBean dataBean = new SqlBean();
                dataBean.and("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
                dataBean.set("ROLE_NAME", payload.get("name"));
                dataBean.set("ROLE_SORT", payload.get("sort"));
                dataBean.set("TYPE", payload.get("type"));
                dataBean.set("ROLE_DEPT", payload.get("org_id"));
                dataBean.set("MEMO", payload.get("memo"));
                dataBean.set("S_FLAG", MsgModifyUtil.getSFlag(payload.getStr("status")));
                dataBean.set("S_MTIME", MsgModifyUtil.getTime());
                ServDao.update(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
        	}
/*        	sql.set("SY_ROLE_CODE", payload.getStr("code"));
        	sql.set("S_MTIME", MsgModifyUtil.getTime());
        	ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);*/
            log.info("MSGDATAMODIFY modifyRoleData end updateRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY updateRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条删除统一认证角色数据
     *
     * @param payload
     * @throws Exception
     */
    private static void deleteRolesToAuth(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start deleteRolesToAuth");
        try {

            SqlBean dataBean = new SqlBean();
            dataBean.and("ID", payload.getStr("id"));
            ServDao.destroy(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyOrgData end deleteRolesToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY deleteRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条删除系统角色数据
     *
     * @param payload
     * @throws Exception
     */
    private static void deleteRolesToSy(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start deleteRolesToSy");
        try {
        	SqlBean sql = new SqlBean();
        	sql.and("AUTH_ROLE_CODE", payload.getStr("id"));
        	List<Bean> roleLists = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
        	for (Bean roleBean :roleLists) {
        		SqlBean dataBean = new SqlBean();
                dataBean.and("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
                ServDao.destroy(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
        	}
        	 ServDao.destroy(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
            log.info("MSGDATAMODIFY modifyOrgData end deleteRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY deleteRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条锁定角色数据-统一认证表
     *
     * @param payload
     * @throws Exception
     */
    private static void lockRolesToAuth(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start lockRolesToAuth");

        try {
            SqlBean dataBean = new SqlBean();
            dataBean.and("ID", payload.getStr("id"));
            dataBean.set("STATUS", LOCKED);
            ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end lockRolesToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY lockRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条锁定角色数据-系统表
     *
     * @param payload
     * @throws Exception
     */
    private static void lockRolesToSy(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start lockRolesToSy");
        try {
            SqlBean dataBean = new SqlBean();
            dataBean.andIn("ROLE_CODE", payload.getStr("id"));
            dataBean.set("S_FLAG", LOCKED);
            ServDao.update(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end lockRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY lockRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条启用角色数据-统一认证表
     *
     * @param payload
     * @throws Exception
     */
    private static void activeRolesToAuth(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start activeRolesToAuth");

        try {
            SqlBean dataBean = new SqlBean();
            dataBean.and("ID", payload.getStr("id"));
            dataBean.set("STATUS", ACTIVE);
            ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end activeRolesToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY activeRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 逐条启用角色数据-系统表
     *
     * @param payload
     * @throws Exception
     */
    private static void activeRolesToSy(Bean payload) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start activeRolesToSy");
        try {
            SqlBean dataBean = new SqlBean();
            dataBean.andIn("ROLE_CODE", payload.getStr("id"));
            dataBean.set("S_FLAG", ACTIVE);
            ServDao.update(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
            log.info("MSGDATAMODIFY modifyRoleData end activeRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY activeRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }

    /*-------------------------------------------------------------------------分割线---------------------------------------------------------------------------*/

    /**
     * 保存角色数据进入临时表
     *
     * @param payload
     * @param method
     */
    public static void saveRoleToTemp(Bean payload, String method) {
        try {
            SqlBean dataBean = new SqlBean();
            dataBean.set("ID", payload.getStr("id"));
            dataBean.set("CODE", payload.get("code"));
            dataBean.set("NAME", payload.get("name"));
            dataBean.set("SORT", payload.get("sort"));
            dataBean.set("TYPE", payload.get("type"));
            dataBean.set("ORG_ID", payload.get("org_id"));
            dataBean.set("MEMO", payload.get("memo"));
            dataBean.set("DOMAIN_ID", payload.get("domain_id"));
            dataBean.set("METHON", method);
            ServDao.create(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, dataBean);
            log.info("MSGDATAMODIFY saveRoleToTemp SUCCESS ROLE:" + payload.getStr("id"));
        } catch (Exception e) {
            log.error("MSGDATAMODIFY saveRoleToTemp ERROR:" + e.getMessage());
        }
    }

    /**
     * 处理角色数据
     */
    public static void modifyRoleData() {
        Transaction.begin();

        try {
            // 新增角色
            // 获取临时表新增角色数据
            SqlBean methodBean = new SqlBean();
            methodBean.and("METHOD", MsgModifyUtil.CREATE_ONE);
            List<Bean> createRoles = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, methodBean);
            log.info("MSGDATAMODIFY createRoles number : " + createRoles.size());
            if (createRoles.size() > 0) {
                addRolesToAuth(createRoles);
                addRolesToSy(createRoles);
            }

            // 更新角色
            // 获取临时表更新角色数据
            methodBean = new SqlBean();
            methodBean.appendWhere("and METHOD in (?,?)", MsgModifyUtil.UPDATE_ONE, MsgModifyUtil.UPDATE_ONE_4ADMIN);
            List<Bean> updateRoles = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, methodBean);
            log.info("MSGDATAMODIFY createRoles number : " + updateRoles.size());
            if (updateRoles.size() > 0) {
                updateRolesToAuth(updateRoles);
                updateRolesToSy(updateRoles);
            }

            // 删除角色
            methodBean = new SqlBean();
            methodBean.and("METHOD", MsgModifyUtil.DELETE_ONE);
            List<Bean> deleteRoles = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, methodBean);
            log.info("MSGDATAMODIFY deleteRoles number : " + deleteRoles.size());
            if (deleteRoles.size() > 0) {
                deleteRolesToAuth(deleteRoles);
                deleteRolesToSy(deleteRoles);
            }

            // 锁定角色
            methodBean = new SqlBean();
            methodBean.and("METHOD", MsgModifyUtil.LOCK_ONE);
            List<Bean> lockRoles = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, methodBean);
            log.info("MSGDATAMODIFY lockRoles number : " + lockRoles.size());
            if (lockRoles.size() > 0) {
                lockRolesToAuth(lockRoles);
                lockRolesToSy(lockRoles);
            }

            // 启用角色
            methodBean = new SqlBean();
            methodBean.and("METHOD", MsgModifyUtil.ACTIVE_ONE);
            List<Bean> activeRoles = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TEMP, methodBean);
            log.info("MSGDATAMODIFY activeRoles number : " + activeRoles.size());
            if (activeRoles.size() > 0) {
                activeRolesToAuth(activeRoles);
                activeRolesToSy(activeRoles);
            }

            // 清空角色临时表
            MsgModifyUtil.cleanTempData(MsgModifyUtil.SYS_AUTH_ROLE_TEMP);
            Transaction.commit();
            log.info("MSGDATAMODIFY modifyRoleData End");
        } catch (Exception e) {
            Transaction.rollback();
            log.error("MSGDATAMODIFY modifyRoleData ERROR:" + e.getMessage());
        }

        Transaction.end();

    }

    /**
     * 新增角色数据到统一认角色证表
     *
     * @param createRoles
     * @throws Exception
     */
    private static void addRolesToAuth(List<Bean> createRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start addRolesToAuth");
        try {
            // 新增角色数据
            if (createRoles.size() > 0) {
                ArrayList<Bean> roleList = new ArrayList<Bean>();
                for (Bean payload : createRoles) {
                    Bean dataBean = new Bean();
                    dataBean.set("ID", payload.getStr("ID"));
                    dataBean.set("CODE", payload.get("CODE"));
                    dataBean.set("NAME", payload.get("NAME"));
                    dataBean.set("SORT", payload.get("SORT"));
                    dataBean.set("TYPE", payload.get("TYPE"));
                    dataBean.set("ORG_ID", payload.get("ORG_ID"));
                    dataBean.set("MEMO", payload.get("MEMO"));
                    dataBean.set("DOMAIN_ID", payload.get("DOMAIN_ID"));
                    dataBean.set("METHON", payload.get("METHON"));
                    dataBean.set("S_MTIME", MsgModifyUtil.getTime());
                    roleList.add(dataBean);
                }
                ServDao.creates(MsgModifyUtil.SYS_AUTH_ROLE, roleList);
            }
            log.info("MSGDATAMODIFY modifyRoleData end addRoleToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY addRoleToAuth ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * 新增角色数据到系统角色表
     *
     * @param createRoles
     * @throws Exception
     */
    private static void addRolesToSy(List<Bean> createRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start addRolesToSy");
        try {
            // 新增机构数据
            if (createRoles.size() > 0) {
                List<Bean> roleList = new ArrayList<Bean>();
                for (Bean payload : createRoles) {
                    Bean dataBean = new Bean();
                    dataBean.set("ROLE_CODE", payload.getStr("ID"));
                    // dataBean.set("CODE", payload.get("CODE"));
                    dataBean.set("ROLE_NAME", payload.get("NAME"));
                    dataBean.set("ROLE_SORT", payload.get("SORT"));
                    dataBean.set("TYPE", payload.get("TYPE"));
                    dataBean.set("ROLE_DEPT", payload.get("ORG_ID"));
                    dataBean.set("MEMO", payload.get("MEMO"));
                    // dataBean.set("DOMAIN_ID", payload.get("DOMAIN_ID"));
                    // dataBean.set("METHON", payload.get("METHON"));
                    dataBean.set("S_FLAG", MsgModifyUtil.getSFlag(payload.getStr("STATUS")));
                    dataBean.set("S_MTIME", MsgModifyUtil.getTime());
                    roleList.add(dataBean);
                }
                ServDao.creates(MsgModifyUtil.OA_SY_ORG_ROLE, roleList);
            }
            log.info("MSGDATAMODIFY modifyRoleData end addRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY addRolesToSy ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * *修改角色信息至统一认证表
     *
     * @param updateRoles
     * @throws Exception
     */
    private static void updateRolesToAuth(List<Bean> updateRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start updateRolesToAuth");
        try {
            if (updateRoles.size() > 0) {
                for (Bean payload : updateRoles) {
                    SqlBean dataBean = new SqlBean();
                    dataBean.and("ID", payload.getStr("ID"));
                    dataBean.set("CODE", payload.get("CODE"));
                    dataBean.set("NAME", payload.get("NAME"));
                    dataBean.set("SORT", payload.get("SORT"));
                    dataBean.set("TYPE", payload.get("TYPE"));
                    dataBean.set("ORG_ID", payload.get("ORG_ID"));
                    dataBean.set("MEMO", payload.get("MEMO"));
                    dataBean.set("DOMAIN_ID", payload.get("DOMAIN_ID"));
                    dataBean.set("METHON", payload.get("METHON"));
                    ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
                }
            }
            log.info("MSGDATAMODIFY modifyRoleData end updateRolesToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY updateRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * *更新角色信息到系统角色表
     *
     * @param updateRoles
     * @throws Exception
     */
    private static void updateRolesToSy(List<Bean> updateRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start updateRolesToSy");
        try {
            if (updateRoles.size() > 0) {
                for (Bean payload : updateRoles) {
                	SqlBean sql = new SqlBean();
                	sql.and("AUTH_ROLE_CODE", payload.getStr("id"));
                	List<Bean> roleLists = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
                	for (Bean roleBean : roleLists) {
	                    SqlBean dataBean = new SqlBean();
	                    dataBean.and("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
	                    // dataBean.set("CODE", payload.get("CODE"));
	                    dataBean.set("ROLE_NAME", payload.get("NAME"));
	                    dataBean.set("ROLE_SORT", payload.get("SORT"));
	                    dataBean.set("TYPE", payload.get("TYPE"));
	                    dataBean.set("ROLE_DEPT", payload.get("ORG_ID"));
	                    dataBean.set("MEMO", payload.get("MEMO"));
	                    // dataBean.set("DOMAIN_ID", payload.get("DOMAIN_ID"));
	                    // dataBean.set("METHON", payload.get("METHON"));
	                    dataBean.set("S_FLAG", MsgModifyUtil.getSFlag(payload.getStr("STATUS")));
	                    dataBean.set("S_MTIME", MsgModifyUtil.getTime());
	                    ServDao.update(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
                	}
                }
            }
            log.info("MSGDATAMODIFY modifyRoleData end updateRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY updateRolesToSy ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * *删除统一认证表角色信息
     *
     * @param deleteRoles
     * @throws Exception
     */
    private static void deleteRolesToAuth(List<Bean> deleteRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start deleteRolesToAuth");
        try {
            // 删除角色数据
            if (deleteRoles.size() > 0) {
                List<String> idList = new ArrayList<String>();
                for (Bean payload : deleteRoles) {
                    idList.add(payload.getStr("ID"));
                }
                SqlBean dataBean = new SqlBean();
                dataBean.andIn("ID", idList.toArray());
                ServDao.destroy(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            }
            log.info("MSGDATAMODIFY modifyOrgData end deleteRolesToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY deleteRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * 删除系统角色表信息
     *
     * @param deleteRoles
     * @throws Exception
     */
    private static void deleteRolesToSy(List<Bean> deleteRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start deleteRolesToSy");
        try {
            // 删除角色数据
            if (deleteRoles.size() > 0) {
                List<String> idList = new ArrayList<String>();
                for (Bean payload : deleteRoles) {
                	SqlBean sql = new SqlBean();
                	sql.and("AUTH_ROLE_CODE", payload.getStr("id"));
                	List<Bean> roleLists = ServDao.finds(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
                	for (Bean roleBean : roleLists) {
                		idList.add(roleBean.getStr("SY_ROLE_CODE"));
                	}
                }
                SqlBean dataBean = new SqlBean();
                dataBean.andIn("ROLE_CODE", idList.toArray());
                ServDao.destroy(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
            }
            log.info("MSGDATAMODIFY modifyOrgData end deleteRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY deleteRolesToSy ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * *锁定统一认证角色
     *
     * @param lockRoles
     * @throws Exception
     */
    private static void lockRolesToAuth(List<Bean> lockRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start lockRolesToAuth");

        try {
            if (lockRoles.size() > 0) {
                List<String> idList = new ArrayList<String>();
                for (Bean payload : lockRoles) {
                    idList.add(payload.getStr("ID"));
                }
                SqlBean dataBean = new SqlBean();
                dataBean.andIn("ID", idList.toArray());
                dataBean.set("STATUS", LOCKED);
                ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            }
            log.info("MSGDATAMODIFY modifyRoleData end lockRolesToAuth");
        } catch (Exception e) {
            log.info("MSGDATAMODIFY lockRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }

    }

    /**
     * *锁定系统角色
     *
     * @param lockRoles
     * @throws Exception
     */
    private static void lockRolesToSy(List<Bean> lockRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start lockRolesToSy");
        try {
            if (lockRoles.size() > 0) {
                List<String> idList = new ArrayList<String>();
                for (Bean payload : lockRoles) {
                    idList.add(payload.getStr("ID"));
                }
                SqlBean dataBean = new SqlBean();
                dataBean.andIn("ROLE_CODE", idList.toArray());
                dataBean.set("S_FLAG", LOCKED);
                ServDao.update(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
            }
            log.info("MSGDATAMODIFY modifyRoleData end lockRolesToSy");
        } catch (Exception e) {
            log.info("MSGDATAMODIFY lockRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * *启用统一认证角色
     *
     * @param activeRoles
     * @throws Exception
     */
    private static void activeRolesToAuth(List<Bean> activeRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start activeRolesToAuth");
        try {
            if (activeRoles.size() > 0) {
                List<String> idList = new ArrayList<String>();
                for (Bean payload : activeRoles) {
                    idList.add(payload.getStr("ID"));
                }
                SqlBean dataBean = new SqlBean();
                dataBean.andIn("ID", idList.toArray());
                dataBean.set("STATUS", ACTIVE);
                ServDao.update(MsgModifyUtil.SYS_AUTH_ROLE, dataBean);
            }
            log.info("MSGDATAMODIFY modifyRoleData end activeRolesToAuth");
        } catch (Exception e) {
            log.info("MSGDATAMODIFY activeRolesToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * *启用系统角色
     *
     * @param activeRoles
     * @throws Exception
     */
    private static void activeRolesToSy(List<Bean> activeRoles) throws Exception {
        log.info("MSGDATAMODIFY modifyRoleData start activeRolesToSy");
        try {
            if (activeRoles.size() > 0) {
                List<String> idList = new ArrayList<String>();
                for (Bean payload : activeRoles) {
                    idList.add(payload.getStr("ID"));
                }
                SqlBean dataBean = new SqlBean();
                dataBean.andIn("ROLE_CODE", idList.toArray());
                dataBean.set("S_FLAG", ACTIVE);
                ServDao.update(MsgModifyUtil.OA_SY_ORG_ROLE, dataBean);
            }
            log.info("MSGDATAMODIFY modifyRoleData end activeRolesToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY activeRolesToSy ERROR:" + e.getMessage());
            throw e;
        }
    }
}
