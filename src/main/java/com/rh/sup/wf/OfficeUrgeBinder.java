package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

import java.util.List;

public class OfficeUrgeBinder implements ExtendBinder{

    private static String SUP_APPRO_URGE_DEPT = "OA_SUP_APPRO_URGE_DEPT";
    private static String SUP_APPRO_URGE= "OA_SUP_APPRO_URGE";

    @Override
    public ExtendBinderResult run(WfAct wfAct, WfNodeDef wfNodeDef, UserBean userBean) {
        //构建返回值结果
        ExtendBinderResult result = new ExtendBinderResult();
        //1.取得流程实例对象
        WfProcess process =  wfAct.getProcess();
        Bean servBean = process.getServInstBean();
        //获取该单子的ID
        String id = servBean.getId();
        //取出催办主单
        Bean urgeBean = ServDao.find(SUP_APPRO_URGE, id);

        //构建sql 根据APPRO_ID 和 DEPT_CODE 来匹配
        StringBuilder sql = new StringBuilder("select * from SUP_APPRO_OFFICE_DEPT WHERE OFFICE_ID = '")
                .append(urgeBean.getStr("APPRO_ID"))
                .append("' and DEPT_CODE in(select URGED_DEPT_CODE from SUP_APPRO_URGE_DEPT where URGE_ID = '")
                .append(urgeBean.getStr("ID"))
                .append("')");

        List<Bean> applyDeptList = Transaction.getExecutor().query(sql.toString());
        StringBuffer sb = new StringBuffer();

        //判断是否为空不为空开始
        if (applyDeptList != null && !applyDeptList.isEmpty()) {

            //遍历结果集 拼接字符串
            for (Bean user : applyDeptList) {
                sb.append(",")
                        .append(user.getStr("C_USER_CODE"));
            }
            if(sb.length() > 0) {
                sb.deleteCharAt(0);
            }
        }
        //设置处理人
        result.setUserIDs(sb.toString());
        result.setAutoSelect(true);
        return result;
    }
}
