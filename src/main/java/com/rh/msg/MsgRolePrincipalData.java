package com.rh.msg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

import static com.rh.core.serv.ServDao.finds;

/**
 * @author kfzx-wuyj
 */
public class MsgRolePrincipalData {

    /*** 记录历史 */
    private static Log log = LogFactory.getLog(MsgRoleData.class);

    /**
     * *保存角色-主体关系到临时表
     *
     * @param payload
     * @param method
     */
    public static void saveRolePrincipalToTemp(Bean payload, String method) {
        log.error("MSGDATAMODIFY saveRolePrincipalToTemp Start");
        try {
            SqlBean dataBean = new SqlBean();
            dataBean.set("ID", payload.getStr("id"));
            dataBean.set("ROLE_ID", payload.get("role_id"));
            dataBean.set("PRINCIPAL_CLASS", payload.get("principal_class"));
            dataBean.set("PRINCIPAL_CLASS_NAME", payload.get("principal_class_name"));
            dataBean.set("USER_ID", payload.get("user_id"));
            dataBean.set("USER_NAME", payload.get("user_name"));
            dataBean.set("PRINCIPAL_VALUE1", payload.get("principal_value1"));
            dataBean.set("PRINCIPAL_VALUE1_NAME", payload.get("principal_value1_name"));
            dataBean.set("PRINCIPAL_VALUE2", payload.get("principal_value2"));
            dataBean.set("PRINCIPAL_VALUE2_NAME", payload.get("principal_value2_name"));
            dataBean.set("OPERATION_OWNER", payload.get("operation_owner"));
            dataBean.set("DEADLINE_TIME", payload.get("deadline_time"));
            dataBean.set("METHON", payload.get("methon"));
            ServDao.create(MsgModifyUtil.SYS_AUTH_ROLEPRINCIPAL_TEMP, dataBean);
            log.error("MSGDATAMODIFY saveRolePrincipalToTemp SUCCESS ROLE:" + payload.getStr("id"));
        } catch (Exception e) {
            log.error("MSGDATAMODIFY saveRolePrincipalToTemp ERROR:" + e.getMessage());
        }
    }

    /**
     * 保存角色-主体关系到临时表(批量)
     *
     * @param rolePrincipallist
     */
    public static void saveRolePrincipalToTemp(List<Bean> rolePrincipallist) {
        Transaction.begin();
        List<Bean> rolePrincipallists = new ArrayList<Bean>();
        try {
            log.error("MSGDATAMODIFY saveRoleToTemp list start ");
            for (Bean payload : rolePrincipallist) {
                SqlBean dataBean = new SqlBean();
                dataBean.set("ID", payload.getStr("id"));
                dataBean.set("ROLE_ID", payload.get("role_id"));
                dataBean.set("PRINCIPAL_CLASS", payload.get("principal_class"));
                dataBean.set("PRINCIPAL_CLASS_NAME", payload.get("principal_class_name"));
                dataBean.set("USER_ID", payload.get("user_id"));
                dataBean.set("USER_NAME", payload.get("user_name"));
                dataBean.set("PRINCIPAL_VALUE1", payload.get("principal_value1"));
                dataBean.set("PRINCIPAL_VALUE1_NAME", payload.get("principal_value1_name"));
                dataBean.set("PRINCIPAL_VALUE2", payload.get("principal_value2"));
                dataBean.set("PRINCIPAL_VALUE2_NAME", payload.get("principal_value2_name"));
                dataBean.set("OPERATION_OWNER", payload.get("operation_owner"));
                dataBean.set("DEADLINE_TIME", payload.get("deadline_time"));
                dataBean.set("METHON", payload.get("methon"));
                rolePrincipallists.add(dataBean);
            }
            ServDao.creates(MsgModifyUtil.SYS_AUTH_ROLEPRINCIPAL_TEMP, rolePrincipallists);
            Transaction.commit();
            log.error("MSGDATAMODIFY saveRolePrincipalToTemp list end ");
        } catch (Exception e) {
            Transaction.rollback();
            log.error("MSGDATAMODIFY saveRolePrincipalToTemp list ERROR:" + e.getMessage());
        }

    }


    /**
     * 处理角色-主体数据
     * <p>
     * principal_class表示与角色建立关系的主体:
     * "pt_organization"代表组织机构，
     * "pt_org_group"代表用户组，
     * "pt_user"代表用户，
     * "pt_rank"代表职级；
     * <p>
     * 1. principal_class表示与角色建立关系的主体，"pt_organization"代表组织机构，"pt_org_group"代表用户组，"pt_user"代表用户，"pt_rank"代表职级；
     * 2. principal_class_name表示主体类型名称，参考第一条；
     * 3.如果主体为组织，则principal_value1记录组织id，principal_value1_name记录组织名称，principal_value2与principal_value2_name均为空
     * 4.如果主体为用户组，则principal_value1，principal_value1_name分别为用户组所属组织的id 和名称，principal_value2，principal_value2_name分别记录用户组的id和名称
     * 5.如果主体为职级，则principal_value1，principal_value1_name分别为用户组所属组织的id 和名称，principal_value2，principal_value2_name分别记录职级的id和名称
     * 6.如果主体为用户，则user_id，user_name记录用户id和名称，principal_value1,principal_value2,principal_value1_name,principal_value2_name均为空
     *
     * @param payload
     * @param method
     */
    public static void modifyOneRolePrincipalData(Bean payload, String method) {
        log.error("MSGDATAMODIFY modifyRolePrincipalData start modifyOneRolePrincipalData");
       
        Transaction.begin();
        saveRolePrincipalToTemp(payload, method);
        try {
            if (MsgModifyUtil.ADD_ONE.equals(method)) {
            	String id = payload.getStr("id");
            	SqlBean sqlBean = new SqlBean();
				sqlBean.and("ID", id);
				Bean authRolePBean = ServDao.find(MsgModifyUtil.SYS_AUTH_ROLEPRINCIPAL,sqlBean);
				if (null ==authRolePBean ||  authRolePBean.isEmpty()) {
					addRolePrincipalToAuth(payload);
				}
				
                addRolePrincipalToSy(payload);
            }
            if (MsgModifyUtil.REMOVE_ONE.equals(method)) {
                deleteRolePrincipalToAuth(payload);
                deleteRolePrincipalToSy(payload);
            }
            Transaction.commit();
            log.error("MSGDATAMODIFY modifyRolePrincipalData success modifyOneRolePrincipalData");
        } catch (Exception e) {
            Transaction.rollback();
            log.error("MSGDATAMODIFY modifyRolePrincipalData error modifyOneRolePrincipalData:" + e.getMessage());

        }
        Transaction.end();
    }

    /**
     * 新增主体信息-统一认证表
     *
     * @param payload
     * @throws Exception
     */
    private static void addRolePrincipalToAuth(Bean payload) throws Exception {
        log.error("MSGDATAMODIFY modifyRolePrincipalData start addRolePrincipalToAuth");
        try {
            Bean dataBean = new Bean();
            dataBean.set("ID", payload.getStr("id"));
            dataBean.set("ROLE_ID", payload.get("role_id"));
            dataBean.set("PRINCIPAL_CLASS", payload.get("principal_class"));
            dataBean.set("PRINCIPAL_CLASS_NAME", payload.get("principal_class_name"));
            dataBean.set("USER_ID", payload.get("user_id"));
            dataBean.set("USER_NAME", payload.get("user_name"));
            dataBean.set("PRINCIPAL_VALUE1", payload.get("principal_value1"));
            dataBean.set("PRINCIPAL_VALUE1_NAME", payload.get("principal_value1_name"));
            dataBean.set("PRINCIPAL_VALUE2", payload.get("principal_value2"));
            dataBean.set("PRINCIPAL_VALUE2_NAME", payload.get("principal_value2_name"));
            dataBean.set("OPERATION_OWNER", payload.get("operation_owner"));
            dataBean.set("DEADLINE_TIME", payload.get("deadline_time"));
            dataBean.set("METHON", payload.get("methon"));
            dataBean.set("S_MTIME", MsgModifyUtil.getTime());
            ServDao.create(MsgModifyUtil.SYS_AUTH_ROLEPRINCIPAL, dataBean);
            log.error("MSGDATAMODIFY modifyRolePrincipalData end addRolePrincipalToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY addRolePrincipalToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 新增主体信息-系统表
     *
     * @param payload
     * @throws Exception
     */
    private static void addRolePrincipalToSy(Bean payload) throws Exception {

        log.error("MSGDATAMODIFY modifyRolePrincipalData start addRolePrincipalToSy");
        String principal_class = payload.getStr("principal_class");

        try {
        	SqlBean sql = new SqlBean();
        	sql.and("AUTH_ROLE_CODE", payload.getStr("role_id"));
        	List<Bean> roleLists = finds(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
        	for (Bean roleBean : roleLists) {
	            //如果主体为组织，则principal_value1记录组织id，principal_value1_name记录组织名称，principal_value2与principal_value2_name均为空
	            if (MsgModifyUtil.PT_ORGANIZATION.equals(principal_class)) {
	                SqlBean sqlBean = new SqlBean();	
	                sqlBean.and("PRINCIPAL_VALUE1", payload.get("principal_value1"));
	                sqlBean.and("PRINCIPAL_CLASS", MsgModifyUtil.PT_ORGANIZATION);
	                List<Bean> user_org = finds(MsgModifyUtil.SYS_AUTH_USER_ORG, sqlBean);
	                for (Bean bean : user_org) {
	                	SqlBean tempBean = new SqlBean();
	                	tempBean.and("USER_CODE", bean.get("USER_ID"));
	                	tempBean.and("ROLE_CODE",roleBean.getStr("SY_ROLE_CODE"));
	                	Bean result =  ServDao.find(MsgModifyUtil.OA_SY_ORG_ROLE_USER, tempBean);
	                	if (null != result && (!result.isEmpty()) ) {
	                		continue;
	                	}
	                    Bean dataBean = new Bean();
	                    dataBean.set("USER_CODE", bean.get("USER_ID"));
	                    dataBean.set("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
	                    dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
	                    //dataBean.set("S_FLAG", payload.get(""));
	                    //dataBean.set("S_USER", payload.get(""));
	                    dataBean.set("S_MTIME", MsgModifyUtil.getTime());
	                    dataBean.set("DEPT_CODE", payload.get("principal_value1"));//组织id
	
	                    ServDao.create(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);
	
	                }
	            }
	            //用户组
	            if (MsgModifyUtil.PT_ORG_GROUP.equals(principal_class)) {
	                SqlBean sqlBean = new SqlBean();
	
	                sqlBean.and("PRINCIPAL_VALUE1", payload.get("principal_value1"));
	                sqlBean.and("PRINCIPAL_VALUE2", payload.get("principal_value2"));
	                sqlBean.and("PRINCIPAL_CLASS", MsgModifyUtil.PT_ORG_GROUP);
	                List<Bean> user_group = finds(MsgModifyUtil.SYS_AUTH_USER_ORG, sqlBean);
	                for (Bean bean : user_group) {
	                	SqlBean tempBean = new SqlBean();
	                	tempBean.and("USER_CODE", bean.get("USER_ID"));
	                	tempBean.and("ROLE_CODE",roleBean.getStr("SY_ROLE_CODE"));
	                	Bean result =  ServDao.find(MsgModifyUtil.OA_SY_ORG_ROLE_USER, tempBean);
	                	if (null != result &&  (!result.isEmpty()) ) {
	                		continue;
	                	}
	                    Bean dataBean = new Bean();
	                    dataBean.set("RU_ID", payload.getStr("id"));
	                    dataBean.set("USER_CODE", bean.get("USER_ID"));
	                    dataBean.set("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
	                    dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
	                    //dataBean.set("S_FLAG", payload.get(""));
	                    //dataBean.set("S_USER", payload.get(""));
	                    dataBean.set("S_MTIME", MsgModifyUtil.getTime());
	                    dataBean.set("DEPT_CODE", payload.get("principal_value1"));//组织id
	
	                    ServDao.create(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);
	
	                }
	            }
	            //用户
	            if (MsgModifyUtil.PT_USER.equals(principal_class)) {
	
	                SqlBean sqlBean = new SqlBean();
	                //查询用户信息
	                sqlBean.and("ID", payload.get("user_id"));
	                Bean user = ServDao.find(MsgModifyUtil.SYS_AUTH_USER, sqlBean);
	
                	SqlBean tempBean = new SqlBean();
                	tempBean.and("USER_CODE", payload.get("user_id"));
                	tempBean.and("ROLE_CODE",roleBean.getStr("SY_ROLE_CODE"));
                	Bean result =  ServDao.find(MsgModifyUtil.OA_SY_ORG_ROLE_USER, tempBean);
                	if (null == result || result.isEmpty() ) {
                		 Bean dataBean = new Bean();
      	                dataBean.set("RU_ID", payload.getStr("id"));
      	                dataBean.set("USER_CODE", payload.get("user_id"));
      	                dataBean.set("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
      	                dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
      	                //dataBean.set("S_FLAG", payload.get(""));
      	                //dataBean.set("S_USER", payload.get(""));
      	                dataBean.set("S_MTIME", MsgModifyUtil.getTime());
      	                dataBean.set("DEPT_CODE", user.get("ORG_ID"));//组织id
      	
      	                ServDao.create(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);
                	}
	              
	            }
	            //职级
	            if (MsgModifyUtil.PT_RANK.equals(principal_class)) {
                    SqlBean sqlBean = new SqlBean();
                    //查询用户信息
                    sqlBean.and("ZHIJI", payload.get("principal_value2_name"));
                    List<Bean> users = finds(MsgModifyUtil.SYS_AUTH_USER, sqlBean);
                    for (Bean bean : users) {
                    	SqlBean tempBean = new SqlBean();
                    	tempBean.and("USER_CODE", bean.get("USER_ID"));
                    	tempBean.and("ROLE_CODE",roleBean.getStr("SY_ROLE_CODE"));
                    	Bean result =  ServDao.find(MsgModifyUtil.OA_SY_ORG_ROLE_USER, tempBean);
                    	if (null != result && (!result.isEmpty()) ) {
                    		continue;
                    	}
                        Bean dataBean = new Bean();
                        dataBean.set("RU_ID", payload.getStr("id"));
                        dataBean.set("USER_CODE", bean.get("USER_ID"));
                        dataBean.set("ROLE_CODE", roleBean.getStr("SY_ROLE_CODE"));
                        dataBean.set("CMPY_CODE", MsgModifyUtil.CMPY_CODE);
                        //dataBean.set("S_FLAG", payload.get(""));
                        //dataBean.set("S_USER", payload.get(""));
                        dataBean.set("S_MTIME", MsgModifyUtil.getTime());
                        dataBean.set("DEPT_CODE", payload.get("principal_value1"));//组织id

                        ServDao.create(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);

                    }

	            }
        	}
            log.error("MSGDATAMODIFY modifyRolePrincipalData start addRolePrincipalToSy");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY addRolePrincipalToSy ERROR:" + e.getMessage());
            throw e;
        }
    }


    /**
     * 删除角色主体数据-统一认证表
     *
     * @param payload
     * @throws Exception
     */
    private static void deleteRolePrincipalToAuth(Bean payload) throws Exception {
        log.error("MSGDATAMODIFY modifyRolePrincipalData start deleteRolePrincipalToAuth");
        try {
            //删除数据
            SqlBean dataBean = new SqlBean();
            dataBean.and("ID", payload.getStr("id"));
            ServDao.destroy(MsgModifyUtil.SYS_AUTH_USER_ORG, dataBean);
            log.error("MSGDATAMODIFY modifyRolePrincipalData end deleteRolePrincipalToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY modifyRolePrincipalData deleteRolePrincipalToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

    /**
     * 删除角色主体-系统表
     *
     * @param payload
     * @throws Exception
     */
    private static void deleteRolePrincipalToSy(Bean payload) throws Exception {
        String principal_class = payload.getStr("principal_class");
        try {
        	SqlBean sql = new SqlBean();
        	sql.and("AUTH_ROLE_CODE", payload.getStr("role_id"));
        	List<Bean> roleLists = finds(MsgModifyUtil.SYS_AUTH_ROLE_TO_SY_ROLE, sql);
        	for (Bean roleBean : roleLists) {
	            //如果主体为组织，则principal_value1记录组织id，principal_value1_name记录组织名称，principal_value2与principal_value2_name均为空
	            if (MsgModifyUtil.PT_ORGANIZATION.equals(principal_class)) {
	                SqlBean sqlBean = new SqlBean();
	
	                sqlBean.and("PRINCIPAL_VALUE1", payload.get("principal_value1"));
	                sqlBean.and("PRINCIPAL_CLASS", MsgModifyUtil.PT_ORGANIZATION);
	                List<Bean> user_org = finds(MsgModifyUtil.SYS_AUTH_USER_ORG, sqlBean);
	                for (Bean bean : user_org) {
	                    SqlBean dataBean = new SqlBean();
	                    dataBean.and("USER_CODE", bean.get("USER_ID"));
	                    dataBean.and("ROLE_CODE", roleBean.get("SY_ROLE_CODE"));
	                  //  dataBean.set("DEPT_CODE", payload.get("principal_value1"));//组织id
	                    ServDao.destroy(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);
	
	                }
	            }
	            //用户组
	            if (MsgModifyUtil.PT_ORG_GROUP.equals(principal_class)) {
	                SqlBean sqlBean = new SqlBean();
	
	                sqlBean.and("PRINCIPAL_VALUE1", payload.get("principal_value1"));
	                sqlBean.and("PRINCIPAL_VALUE2", payload.get("principal_value2"));
	                sqlBean.and("PRINCIPAL_CLASS", MsgModifyUtil.PT_ORG_GROUP);
	                List<Bean> user_group = finds(MsgModifyUtil.SYS_AUTH_USER_ORG, sqlBean);
	                for (Bean bean : user_group) {
	                    SqlBean dataBean = new SqlBean();
	                    dataBean.and("USER_CODE", bean.get("USER_ID"));
	                    dataBean.and("ROLE_CODE", roleBean.get("SY_ROLE_CODE"));
	                   // dataBean.set("DEPT_CODE", payload.get("principal_value1"));//组织id
	
	                    ServDao.destroy(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);
	
	                }
	            }
	            //用户
	            if (MsgModifyUtil.PT_USER.equals(principal_class)) {
	
	                //SqlBean sqlBean = new SqlBean();
	                //查询用户信息
	                //sqlBean.and("ID", payload.get("user_id"));
	                //Bean user = ServDao.find(MsgModifyUtil.SYS_AUTH_USER, sqlBean);
	
	                SqlBean dataBean = new SqlBean();
	                dataBean.and("USER_CODE", payload.get("user_id"));
	                dataBean.and("ROLE_CODE", roleBean.get("SY_ROLE_CODE"));
	               // dataBean.set("DEPT_CODE", user.get("ORG_ID"));//组织id
	
	                ServDao.destroy(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);
	            }
	            //职级
	            if (MsgModifyUtil.PT_RANK.equals(principal_class)) {
                    SqlBean sqlBean = new SqlBean();
                    sqlBean.and("ZHIJI", payload.get("principal_value2_name"));
                    List<Bean> users = finds(MsgModifyUtil.SYS_AUTH_USER, sqlBean);
                    for (Bean bean : users) {
                        SqlBean dataBean = new SqlBean();
                        dataBean.and("USER_CODE", bean.get("USER_ID"));
                        dataBean.and("ROLE_CODE", roleBean.get("SY_ROLE_CODE"));
                        // dataBean.set("DEPT_CODE", payload.get("principal_value1"));//组织id

                        ServDao.destroy(MsgModifyUtil.OA_SY_ORG_ROLE_USER, dataBean);

                    }
	            }
        	}
            log.error("MSGDATAMODIFY modifyRolePrincipalData end deleteRolePrincipalToAuth");
        } catch (Exception e) {
            log.error("MSGDATAMODIFY modifyRolePrincipalData deleteRolePrincipalToAuth ERROR:" + e.getMessage());
            throw e;
        }
    }

}
