package com.rh.core.org.serv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import com.rh.core.org.UserBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.RowHandler;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.Strings;

/**
 * 用户关联关系服务类
 * 关于用户兼岗的设定：
 * 1.只有主用户才能设定兼岗信息，一个主帐号可以关联多个兼岗帐号，但兼岗帐号不能再关联兼岗信息；
 * 2.设定完兼岗设定后，会形成一个兼岗用户组，组内多个帐号可以自由切换；
 * 3.在SY_ORG_USER_STATE中会记录兼岗组内所有用户的列表(JIAN_CODES)，逗号分隔，一个组内的兼岗用户列表完全一致；
 * 4.用户登录后，如果JIAN_CODES不为空，则提供多个帐号切换的下拉框，允许用户不用登录快速访问多个帐号。
 * 
 * 关于领导-秘书的设定：
 * 1.一个领导可以设定多名秘书，一个秘书可以对应多个领导；
 * 2.在SY_ORG_USER_STATE中会记录秘书对应的所有领导的列表(LEAD_CODES)，逗号分隔；
 * 3.用户登录后，如果LEAD_CODES不为空，说明为秘书岗位，可以帮助领导补登意见等。
 * 
 * @author wanglong
 * 
 */
public class UserRelateServ extends CommonServ {

    /** 关联类型：1 工作兼岗 */
    public static final int RELATE_TYPE_JIANGANG = 1;
    /** 关联类型：3 领导秘书*/
    public static final int RELATE_TYPE_MISHU = 3;
    /** 关联类型：4 领导下属*/
    public static final int RELATE_TYPE_XIASHU = 4;
    
    /**
     * 删除之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeDelete(ParamBean paramBean) {
        List<Bean> dataList = paramBean.getDeleteDatas();
        for (Bean dataBean : dataList) {
            //清除工作兼岗的关联关系
            if (dataBean.getInt("RELATION_TYPE") == RELATE_TYPE_JIANGANG) {
				String userCode = dataBean.getStr("USER_CODE");
                // 删除兼岗用户
				ParamBean destroyBean = new ParamBean();
				destroyBean.setServId(ServMgr.SY_ORG_USER_ALL);
				destroyBean.setAct(ServMgr.ACT_DELETE);
				destroyBean.setId(userCode);
				destroyBean.set("_JIANGANG_FLAG_", true);
				ServMgr.act(destroyBean);
        		clearUserRelate(dataBean.getStr("ORIGIN_USER_CODE"), userCode);
            } else if (dataBean.getInt("RELATION_TYPE") == RELATE_TYPE_MISHU) {
                clearUserLeader(dataBean.getStr("USER_CODE"), dataBean.getStr("ORIGIN_USER_CODE"));
            }
        }
    }
    
    /**
     * 保存之前的操作
     * @param paramBean 传入的参数Bean
     */
    @Override
    protected void beforeSave(ParamBean paramBean) {
        //String servId = paramBean.getServId();
        Bean userRelate = paramBean.getSaveFullData(); //全数据的参数
        String originUserCode = userRelate.getStr("ORIGIN_USER_CODE");
        String userCode = userRelate.getStr("USER_CODE");
        
        if (originUserCode.equals(userCode)) {
            throw new TipException(Context.getSyMsg("SY_ORG_USER_RELATE_SELF_ERROR")); //不能设置自己为关联人
        }
        
        /** 处理工作兼岗类型 */
        if (userRelate.getInt("RELATION_TYPE") == RELATE_TYPE_JIANGANG) {
            /** 补登兼岗，修复两个同工号但非兼岗用户的兼岗关系 */
            boolean budengFlag = false;
            if (paramBean.getServId().equals("SY_ORG_USER_JIANGANG_BUDENG")) {
                budengFlag = true;
                repairJianRelation(paramBean);
                paramBean.set("budengFlag", budengFlag);
            }
            
            SqlBean sql;            
            //1、检查主用户身份
            String tDetpCode = paramBean.getStr("TDEPT_CODE");
            Bean oUserBean = ServDao.find(ServMgr.SY_ORG_USER_ALL, new Bean(originUserCode));
            if (oUserBean.isNotEmpty("JIANGANG_FLAG")
                    && oUserBean.getInt("JIANGANG_FLAG") == 1) {
                throw new RuntimeException("兼岗用户不能作为主用户");
            }
            //2、检查重复部门
            UserBean userBean = new UserBean(oUserBean);
            if (tDetpCode.equals(userBean.getTDeptCode())) {
    		throw new RuntimeException("不能兼职本部门");
            }
            //3、处理登录名
            String loginName = oUserBean.getStr("USER_LOGIN_NAME");
            if (Strings.isEmpty(loginName)) {
    		loginName = userCode;
            }
            String jianGangUserLoginName = loginName + "@" + RandomStringUtils.randomAlphanumeric(6);
            //int size = 40;
            //if (jianGangUserLoginName.length() > size) {
            //    jianGangUserLoginName = jianGangUserLoginName.substring(0, size);
            //}            
            //4、生成检查sql
            sql = new SqlBean();            
            sql.and("S_FLAG", Constant.YES_INT);
            sql.and("USER_STATE", Constant.YES_INT);
            sql.and("TDEPT_CODE", tDetpCode);
            //5、检查同名用户
            if (budengFlag) { //补登
                Bean auxUserBean = UserMgr.getUser(userCode);
                sql.and("USER_NAME", auxUserBean.getStr("USER_NAME"));
                if (ServDao.count("SY_ORG_USER_ALL", sql) > 1) {
                    throw new TipException("该部门下有同名用户");
                }
            } else {
                sql.and("USER_NAME", oUserBean.getStr("USER_NAME"));
                if (ServDao.count("SY_ORG_USER_ALL", sql) > 0) {
                    throw new TipException("该部门下有同名用户");
                }
            }
            sql.and("JIANGANG_FLAG", 1);
            //6、检查兼岗重复
            if (ServDao.count("SY_ORG_USER_ALL", sql) > 0) {
                throw new TipException("已经兼职该部门");
            } 
            //7、添见或修改兼岗用户
            if (!budengFlag && paramBean.getAddFlag()) { // 添加
                //检查该部门下是否有被删的重名用户
                sql = new SqlBean();            
                sql.and("S_FLAG", Constant.NO_INT);
                sql.and("JIANGANG_FLAG", 1);
                sql.and("USER_STATE", Constant.YES_INT);
                sql.and("TDEPT_CODE", tDetpCode);
                sql.and("USER_NAME", oUserBean.getStr("USER_NAME"));
                List<Bean> deletedUsers = ServDao.finds("SY_ORG_USER_ALL", sql);
                Bean deletedJianUser = null;
                if (deletedUsers != null) {
                    for (Bean delUser : deletedUsers) {
                        if (delUser.getStr("USER_LOGIN_NAME").startsWith(
                                oUserBean.getStr("USER_LOGIN_NAME") + "@")) {
                            deletedJianUser = delUser;
                            break;
                        }
                    }
                }
                if (deletedJianUser != null) {
                    // 恢复已删除的用户
                    ParamBean recoverBean = new ParamBean();
                    recoverBean.setId(deletedJianUser.getId());
                    recoverBean.set("S_FLAG", Constant.YES_INT);
                    recoverBean.set("JIANGANG_FLAG", 1);
                    recoverBean.set("_JIANGANG_FLAG_", true);
                    recoverBean.setAct(ServMgr.ACT_SAVE);
                    recoverBean.setServId(ServMgr.SY_ORG_USER_ALL);
                    recoverBean.setAddFlag(false);
                    Bean retBean = ServMgr.act(recoverBean);

                    // 该字段保存到兼岗表里
                    paramBean.set("USER_CODE", retBean.getId());
                } else {
                    // 复制主用户到兼岗部门下
                    oUserBean.set("DEPT_CODE", tDetpCode);
                    oUserBean.set("USER_LOGIN_NAME", jianGangUserLoginName);
                    oUserBean.set("JIANGANG_FLAG", 1);
                    oUserBean.set("_JIANGANG_FLAG_", true);
                    oUserBean.remove(Constant.KEY_ID);
                    oUserBean.remove("USER_CODE");
                    oUserBean.remove("S_MTIME");
                    oUserBean.remove("S_USER");
                    oUserBean.remove("USER_PASSWORD");
                    ParamBean addBean = new ParamBean(oUserBean);
                    addBean.setAct(ServMgr.ACT_SAVE);
                    addBean.setServId(ServMgr.SY_ORG_USER_ALL);
                    addBean.setAddFlag(true);
                    Bean retBean = ServMgr.act(addBean);

                    // 该字段保存到兼岗表里
                    paramBean.set("USER_CODE", retBean.getId());
                }      
            } else { // 修改
            	// 修改登录名和部门
            	ParamBean modifyBean = new ParamBean();
            	modifyBean.setId(userCode);            	
            	modifyBean.set("USER_LOGIN_NAME", jianGangUserLoginName);
            	modifyBean.set("_JIANGANG_FLAG_", true);
                if (budengFlag) {
                    modifyBean.set("JIANGANG_FLAG", 1);
                } else {
                    modifyBean.set("DEPT_CODE", tDetpCode);
                }
            	modifyBean.setAct(ServMgr.ACT_SAVE);
            	modifyBean.setServId(ServMgr.SY_ORG_USER_ALL);
	        ServMgr.act(modifyBean);
            }
        }
	}

    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        Bean userRelate = paramBean.getSaveFullData(); //全数据的参数
        String originUserCode = userRelate.getStr("ORIGIN_USER_CODE");
        String userCode = userRelate.getStr("USER_CODE");
        Bean oldBean = paramBean.getSaveOldData(); //原始数据
        //String oldUserCode = oldBean.getStr("USER_CODE");
        
        /** 处理工作兼岗类型 */
        if (userRelate.getInt("RELATION_TYPE") == RELATE_TYPE_JIANGANG) {
            
            if (!paramBean.getBoolean("budengFlag") && paramBean.getAddFlag()) { //添加
                addUserRelate(originUserCode, userCode);
            } else { //修改               
                //清除上一个兼岗人设定
                //clearUserRelate(originUserCode, oldUserCode);
                //添加新的兼岗人设定
                //addUserRelate(originUserCode, paramBean.getStr("USER_CODE"));
            }
        }
        
        /** 处理领导秘书类型 */
        if (userRelate.getInt("RELATION_TYPE") == RELATE_TYPE_MISHU) {
            if (paramBean.getId().length() == 0) { //新增模式，增加新的秘书设定
                addUserLeader(paramBean.getStr("USER_CODE"), paramBean.getStr("ORIGIN_USER_CODE"));
            } else if (paramBean.contains("USER_CODE")) { //修改了秘书
                //清除上一个秘书的设定
                clearUserLeader(oldBean.getStr("USER_CODE"), userRelate.getStr("ORIGIN_USER_CODE"));
                //添加新的秘书的领导设定
                addUserLeader(paramBean.getStr("USER_CODE"), oldBean.getStr("ORIGIN_USER_CODE"));
            }
        }
    }
    
    /**
     * 添加用户关联信息
     * @param originUserCode 主关联人编码
     * @param userCode 关联到人编码
     */
    private void addUserRelate(String originUserCode, String userCode) {
        Bean originUserState = UserMgr.getUserStateOrCreate(originUserCode); //获取主用户的用户状态信息
        String jianCodes = originUserState.getStr("JIAN_CODES");
        if (jianCodes.length() == 0) { //第一次设置兼岗设定，先把自己加入兼岗列表
            jianCodes = Strings.addValue(jianCodes, originUserCode);
        }
        jianCodes = Strings.addValue(jianCodes, userCode); //加入兼岗人
        String[] jianArray = jianCodes.split(Constant.SEPARATOR);
        for (String jianCode : jianArray) { //依次更新整个用户组的兼岗人设定
            originUserState = new Bean();
            originUserState.set("USER_CODE", jianCode).set("JIAN_CODES", jianCodes);
            UserMgr.saveUserState(originUserState); //更新兼岗状态信息
        }
    }
    
    /**
     * 移除用户关联信息
     * @param originUserCode 主关联人编码
     * @param userCode 关联到人编码
     */
    private void clearUserRelate(String originUserCode, String userCode) {
        Bean originUserState = UserMgr.getUserStateOrCreate(originUserCode); //获取主用户的用户状态信息
        String jianCodes = originUserState.getStr("JIAN_CODES");
        if (jianCodes.length() > 0) { //必须已经存在兼岗设定再在组内清除所有的兼岗设置
            String[] jianArray = jianCodes.split(Constant.SEPARATOR); //取得所有组内所有用户
            jianCodes = Strings.removeValue(jianCodes, userCode);
            if (jianCodes.equals(originUserCode)) { //没有剩余的兼岗人，清除兼岗用户
                jianCodes = "";
            }
            for (String jianCode : jianArray) { //依次更新整个用户组的兼岗人设定
                originUserState = new Bean();
                originUserState.set("USER_CODE", jianCode).set("JIAN_CODES", jianCodes);
                UserMgr.saveUserState(originUserState); //更新兼岗状态信息
            }
        }
    }
    
    
    /**
     * 添加领导秘书关联
     * @param userCode 秘书编码
     * @param leaderCode 领导编码
     */
    private void addUserLeader(String userCode, String leaderCode) {
        Bean userState = UserMgr.getUserState(userCode); //获取主用户的用户状态信息
        String codes = (userState != null) ? userState.getStr("LEAD_CODES") : "";
        codes = Strings.addValue(codes, leaderCode); //加入领导编码
        userState = new Bean();
        userState.set("USER_CODE", userCode).set("LEAD_CODES", codes);
        UserMgr.saveUserState(userState); //更新领导秘书状态信息
    }
    
    /**
     * 移除领导秘书
     * @param userCode 秘书编码
     * @param leaderCode 领导编码
     */
    private void clearUserLeader(String userCode, String leaderCode) {
        Bean userState = UserMgr.getUserState(userCode); //获取主用户的用户状态信息
        String codes = (userState != null) ? userState.getStr("LEAD_CODES") : "";
        codes = Strings.removeValue(codes, leaderCode);
        userState = new Bean();
        userState.set("USER_CODE", userCode).set("LEAD_CODES", codes);
        UserMgr.saveUserState(userState); //更新领导秘书状态信息
    }

    /**
     * 修复兼岗关系
     * 针对情形：1 两个用户同工号但是都不是兼岗用户 
     *            1.1 有兼岗关系 将兼岗用户标志置为兼岗
     *            1.2 无兼岗关系 建立兼岗关系、将兼岗用户标志置为兼岗
     * @param paramBean 修复bean
     */
    private void repairJianRelation(ParamBean paramBean) {
        String mainUserCode = paramBean.getStr("ORIGIN_USER_CODE");
        String auxUserCode = paramBean.getStr("USER_CODE");
        UserBean mainUser = UserMgr.getUser(mainUserCode);
        UserBean auxUser = UserMgr.getUser(auxUserCode);
        String mainWorkNum = mainUser.getStr("USER_WORK_NUM");
        String auxWorkNum = auxUser.getStr("USER_WORK_NUM");
        int mainUserState = mainUser.getInt("USER_STATE");
        int auxUserState = auxUser.getInt("USER_STATE");
        int mainJianFlag = mainUser.getInt("JIANGANG_FLAG");
        int auxJianFlag = auxUser.getInt("JIANGANG_FLAG");
        if (mainWorkNum.equals(auxWorkNum) && !mainWorkNum.isEmpty()) {
            if (mainUserState != Constant.YES_INT
                    || auxUserState != Constant.YES_INT) {
                throw new TipException("只修复用户都是在岗用户的情况");
            }
            if (mainJianFlag == Constant.NO_INT
                    && auxJianFlag == Constant.NO_INT) {
                String tempJianCodes = UserMgr
                        .getJiangangUserStrWithoutMainUser(auxUserCode);
                if (!tempJianCodes.isEmpty()) {
                    throw new TipException("兼岗用户已经做为主用户存在兼岗关系");
                }
                String jianCodes = UserMgr
                        .getJiangangUserStrWithoutMainUser(mainUserCode);
                //修复开始
                if (!jianCodes.isEmpty() && jianCodes.indexOf(auxUserCode) >= 0) {
                    // 有兼岗关系
                    List<Bean> records;
                    SqlBean sql = new SqlBean();
                    sql.and("RELATION_TYPE", Constant.YES_INT);
                    sql.and("S_FLAG", Constant.YES_INT);
                    sql.and("ORIGIN_USER_CODE", mainUserCode);
                    sql.and("USER_CODE", auxUserCode);
                    records = ServDao.finds("SY_ORG_USER_JIANGANG", sql);
                    if (records == null) {
                        records = new ArrayList<Bean>();
                    }
                    if (records.size() != 1) {
                        throw new TipException("已存在兼岗关系但不唯一");
                    }
                    String pkCode = records.get(0).getId();
                    paramBean.setId(pkCode);
                    paramBean.set("UR_ID", pkCode);
                    paramBean.set("RELATION_MEMO", records.get(0).getStr("RELATION_MEMO"));
                    paramBean.set("S_USER", records.get(0).getStr("S_USER"));
                    //先删除该关系
                    ServDao.delete(paramBean.getServId(), new Bean().setId(pkCode));
                } else {
                    // 无兼岗关系
                    paramBean.set("TDEPT_CODE", auxUser.getStr("TDEPT_CODE"));
                    //paramBean.set("serv", "SY_ORG_USER_JIANGANG");             
                }
            } else {
                throw new TipException("只修复用户都是非兼岗用户的情况");
            }
        } else {
            throw new TipException("只修复用户工号不为空并且两个用户工号相同的情况");
        }
    }

    /**
     * 根据用户编码查找下属
     * 
     * @param paramBean  参数
     * @return String
     */
    public Bean getBelongUserInStr(ParamBean paramBean) {
        OutBean rtnBean = new OutBean();
        String servId = paramBean.getServId();
        final StringBuilder users = new StringBuilder();
        String userCode = Context.getUserBean().getCode();
        if (userCode.length() > 0) {
            SqlBean sqlBean = new SqlBean().and("RELATION_TYPE", RELATE_TYPE_XIASHU).and("S_FLAG", 1)
                    .and("ORIGIN_USER_CODE", userCode);
            ServDao.findsCall(servId, sqlBean, new RowHandler() {
                @Override
                public void handle(List<Bean> columns, Bean dataBean) {
                    users.append(dataBean.getStr("USER_CODE")).append(",");
                }
            });
        }
        if (users.length() > 0) {
            users.setLength(users.length() - 1);
        }
        return rtnBean.set("belongUserStr", users.toString());
    }

    /**
     * 根据用户编码查找下属
     * 
     * @param paramBean 参数paramBean
     * @return outBean
     */
    public OutBean getBelongUsers(ParamBean paramBean) {
        OutBean rtnBean = new OutBean();
        String servId = paramBean.getServId();
        String userCode = Context.getUserBean().getCode();
        if (userCode.length() > 0) {
            SqlBean sqlBean = new SqlBean().and("RELATION_TYPE", RELATE_TYPE_XIASHU).and("S_FLAG", 1)
                    .and("ORIGIN_USER_CODE", userCode);
            List<Bean> dataList = ServDao.finds(servId, sqlBean, new QueryCallback() {
                @Override
                public void call(List<Bean> columns, Bean dataBean) {
                    dataBean.set("USER_CODE__NAME", 
                            DictMgr.getName("SY_ORG_DEPT_USER_ALL", dataBean.getStr("USER_CODE")));
                }
                
            });
            rtnBean.setData(dataList);
        }
        return rtnBean;
    }
}
