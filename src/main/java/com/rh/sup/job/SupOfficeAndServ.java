package com.rh.sup.job;

import java.util.List;

import com.rh.sup.serv.SupApproOfficeServ;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;

/**
 * 署内　自动批量立项　定时任务
 */
public class SupOfficeAndServ extends CommonServ {
    private static Log log = LogFactory.getLog(SupOfficeAndServ.class);

    public void startJob() {

        // 查询 立项提醒规则维护表
        List<Bean> rules = ServDao.finds("OA_SUP_SERV_APPRO_RULE", "");
        // 如果 表中数据不为空，则循环遍历
        if (rules != null && rules.size() > 0) {
            for (int i = 0; i < rules.size(); i++) {
                String currentDate = DateUtils.getDate();//系统当前时间
                Bean rule = rules.get(i);
                String type = rule.getStr("AR_IFMONTH");//立项提醒规则类型
                String time = rule.getStr("AR_TIME");// 立项提醒规则时间
                String dataId = rule.getStr("AR_ITEM_CODE");// 立项单主键
                // 以年为单位
                if (type.equals("1")) {
                    currentDate = currentDate.substring(8);
                    if (time == currentDate) {
                        log.info("----开始自动立项署发督查，立项单主键：" + dataId);
                        addOffice(dataId, currentDate);
                    }
                } else if (type.equals("2")) { // 以月为单位
                    currentDate = currentDate.substring(5);
                    String[] times = time.split(",");
                    if (times.length > 0) {
                        for (int j = 0; j < times.length; j++) {
                            String t = times[j];
                            if (t == currentDate) {
                                log.info("----开始自动立项署发督查，立项单主键：" + dataId);
                                addOffice(dataId, currentDate);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 新建署内立项
     *
     * @param dataId
     */
    public void addOffice(String dataId, String currentDate) {
        // 根据主键查询署内立项单
        Bean officeBean = ServDao.find("OA_SUP_APPRO_OFFICE", dataId);
        if (officeBean != null) {
            // 复制一份新的立项单信息  并新建
            ParamBean newBean = new ParamBean();
            String year = currentDate.substring(0, 4);
            //生成立项编号
            ParamBean paramBean = new ParamBean();
            paramBean.set("actCode", "cardAdd");
            paramBean.set("servId", "OA_SUP_APPRO_OFFICE");
            paramBean.set("nowYear", year);
            SupApproOfficeServ serv = new SupApproOfficeServ();
            Bean itemNum = serv.getItemNum(paramBean);
            String item = "督立" + "〔" + year + "〕" + itemNum.getStr("ITEM_NUM") + "号";

            // 生成系统编号
            String time = DateUtils.getTime();
            String timeCode = time.substring(0, 5);
            String sCode = "SUP" + currentDate.replace("-", "") + timeCode.replace(":", "") + "000" + itemNum.getStr("ITEM_NUM");

            newBean.set("ITEM_NUM", item);
            newBean.set("CUE_TYPE", officeBean.getStr("CUE_TYPE"));
            newBean.set("HANDLE_TYPE", officeBean.getStr("HANDLE_TYPE"));
            newBean.set("ITEM_TYPE", officeBean.getStr("ITEM_TYPE"));
            newBean.set("STATIS_ITEM_SOURCE", officeBean.getStr("STATIS_ITEM_SOURCE"));
            newBean.set("SUPERV_ITEM", officeBean.getStr("SUPERV_ITEM"));
            newBean.set("LIMIT_DATE", officeBean.getStr("LIMIT_DATE"));
            newBean.set("NOT_LIMIT_TIME_REASON", officeBean.getStr("NOT_LIMIT_TIME_REASON"));
            newBean.set("APPR_DATE", currentDate);
            newBean.set("ISSUE_CODE", officeBean.getStr("ISSUE_CODE"));
            newBean.set("ISSUE_DEPT", officeBean.getStr("ISSUE_DEPT"));
            newBean.set("SECRET_RANK", officeBean.getStr("SECRET_RANK"));
            newBean.set("CENTER_DENOTE", officeBean.getStr("CENTER_DENOTE"));
            newBean.set("LEAD_DENOTE", officeBean.getStr("LEAD_DENOTE"));
            newBean.set("DEPT_DENOTE", officeBean.getStr("DEPT_DENOTE"));
            newBean.set("OFFICE_OVERSEER", officeBean.getStr("OFFICE_OVERSEER"));
            newBean.set("OFFICE_OVERSEER_TEL", officeBean.getStr("OFFICE_OVERSEER_TEL"));
            newBean.set("REMARK", officeBean.getStr("REMARK"));
            newBean.set("SELF_REMARK", officeBean.getStr("SELF_REMARK"));
            newBean.set("ITEM_SOURCE", officeBean.getStr("ITEM_SOURCE"));
            newBean.set("S_CODE", sCode);
            newBean.set("S_CMPY", officeBean.getStr("S_CMPY"));
            newBean.set("S_TDEPT", officeBean.getStr("S_TDEPT"));
            newBean.set("S_TNAME", officeBean.getStr("S_TNAME"));
            newBean.set("S_DEPT", officeBean.getStr("S_DEPT"));
            newBean.set("S_DNAME", officeBean.getStr("S_DNAME"));
            newBean.set("S_USER", officeBean.getStr("S_USER"));
            newBean.set("S_WF_USER", officeBean.getStr("S_USER"));
            newBean.set("S_UNAME", officeBean.getStr("S_UNAME"));
            newBean.set("S_FLAG", officeBean.getStr("S_FLAG"));
            newBean.set("S_EMERGENCY", officeBean.getStr("S_EMERGENCY"));
            newBean.set("S_ODEPT", officeBean.getStr("S_ODEPT"));
            newBean.set("APPLY_STATE", "1");
            newBean.set("ITEM_SOURCE_TYPE", officeBean.getStr("ITEM_SOURCE_TYPE"));
            newBean.setServId("OA_SUP_APPRO_OFFICE");

            // 将立项单的原操作人员放入context中 供生成代办
            UserBean userBean = UserMgr.getUser(officeBean.getStr("S_USER"));
            userBean.set("ODEPT_CODE", officeBean.getStr("S_ODEPT"));
            BaseContext.setThread(Context.THREAD.USERBEAN, userBean);
            BaseContext.setThread(Context.THREAD.CMPYCODE, officeBean.getStr("S_CMPY"));

            // 新建立项单
            OutBean bean = save(newBean);
            String beanId = bean.getStr("ID");
            log.info("自动新建立项单完成，新立项单主键：" + beanId);
            //根据立项单主键查询关联部门信息
            String sql = "SELECT * FROM SUP_APPRO_OFFICE_DEPT WHERE OFFICE_ID = '" + dataId + "'";
            List<Bean> officeDepts = Transaction.getExecutor().query(sql);
            if (officeDepts != null && officeDepts.size() > 0) {
                for (int i = 0; i < officeDepts.size(); i++) {
                    Bean dept = officeDepts.get(i);
                    String deptType = dept.getStr("DEPT_TYPE");
                    ParamBean newDept = new ParamBean();
                    newDept.set("OFFICE_ID", beanId);
                    newDept.set("DEPT_TYPE", deptType);
                    newDept.set("DEPT_CODE", dept.getStr("DEPT_CODE"));
                    newDept.set("D_USER_CODE", dept.getStr("D_USER_CODE"));
                    newDept.set("C_USER_CODE", dept.getStr("C_USER_CODE"));
                    newDept.set("DEPT_PHONE", dept.getStr("DEPT_PHONE"));
                    newDept.set("S_CODE", dept.getStr("S_CODE"));
                    newDept.set("S_CMPY", dept.getStr("S_CMPY"));
                    newDept.set("S_TDEPT", dept.getStr("S_TDEPT"));
                    newDept.set("S_TNAME", dept.getStr("S_TNAME"));
                    newDept.set("S_DEPT", dept.getStr("S_DEPT"));
                    newDept.set("S_DNAME", dept.getStr("S_DNAME"));
                    newDept.set("S_USER", dept.getStr("S_USER"));
                    newDept.set("S_UNAME", dept.getStr("S_UNAME"));
                    newDept.set("S_ODEPT", dept.getStr("S_ODEPT"));
                    if (deptType.equals("1")) {
                        newDept.setServId("OA_SUP_APPRO_OFFICE_HOST");
                    } else if (deptType.equals("2")) {
                        newDept.setServId("OA_SUP_APPRO_OFFICE_OTHER");
                    } else {
                        newDept.setServId("OA_SUP_APPRO_OFFICE_ASSIT");
                    }
                    // 新建立项单的主办协办单位
                    OutBean deptBean = save(newDept);
                    log.info("新建立项单关联的办理单位信息，办理单位主键：" + deptBean.getStr("ID"));
                }
            }
        } else {
            log.info("该立项单不存在，可能已被删除！");
        }
    }
}
