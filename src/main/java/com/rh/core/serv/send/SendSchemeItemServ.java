package com.rh.core.serv.send;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * send scheme service extends <CODE>CommonServ</CODE>
 * 
 * @author wanglong
 */
public class SendSchemeItemServ extends CommonServ {

    /** 用户明细服务 */
    private static final String USER_ITEM_SERV = "SY_COMM_SEND_ITEM_USER";

    /** 部门明细服务 */
    private static final String DEPT_ITEM_SERV = "SY_COMM_SEND_ITEM_DEPT";

    /** 角色明细服务 */
    private static final String ROLE_ITEM_SERV = "SY_COMM_SEND_ITEM_ROLE";

    /** 其他结构服务 */
    private static final String OTHER_ODEPT_ITEM_SERV = "";

    /**
     * 保存分发方案明细信息
     * 
     * @param paramBean 参数bean
     * @return 返回参数
     */
    public Bean saveItem(Bean paramBean) {
        String[] idArr = paramBean.getStr("idArray").split(",");

        String[] nameArr = paramBean.getStr("nameArray").split(",");

        String itemType = paramBean.getStr("ITEM_TYPE");

        String sendId = paramBean.getStr("SEND_ID");

        ArrayList<Bean> beanList = new ArrayList<Bean>();

        List<Bean> itemList = ServDao.finds(ServMgr.SY_COMM_SEND_ITEM, " AND SEND_ID = '" + sendId + "'");

        for (int i = 0; i < idArr.length; i++) {
            Bean bean = new Bean();
            bean.set("SEND_ID", sendId).set("ITEM_TYPE", itemType).set("ROLE_USER_CODE", idArr[i])
                    .set("ITEM_NAME", nameArr[i]);
            if (itemType.equals(Constant.USER)) {
                UserBean userBean = UserMgr.getUser(idArr[i]);
                bean.set("DEPT_CODES", userBean.getDeptCode()).set("DEPT_NAMES", userBean.getDeptName());
            }
            boolean flag = true;
            for (Bean b : itemList) {
                if (b.getStr("ROLE_USER_CODE").equals(bean.getStr("ROLE_USER_CODE"))) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                beanList.add(bean);
            }
        }

        String servId = "";

        if (itemType.equals(Constant.USER)) {
            servId = USER_ITEM_SERV;
        } else if (itemType.equals(Constant.DEPT)) {
            servId = DEPT_ITEM_SERV;
        } else if (itemType.equals(Constant.ROLE)) {
            servId = ROLE_ITEM_SERV;
        } else if (itemType.equals(Constant.OTHER_ODEPT)) {
            servId = OTHER_ODEPT_ITEM_SERV;
        }

        ServDao.creates(servId, beanList);

        return paramBean;
    }

    /**
     * 保存角色关联的部门信息
     * 
     * @param paramBean 参数bean
     * @return 参数bean
     */
    public Bean saveRoleDept(Bean paramBean) {

        paramBean.setId(paramBean.getStr("ITEM_ID"));

        ServDao.update(ROLE_ITEM_SERV, paramBean);

        return paramBean;
    }
}
