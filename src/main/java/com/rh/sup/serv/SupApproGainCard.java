
package com.rh.sup.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 历史办理情况显示扩展类
 */
public class SupApproGainCard extends CommonServ {

    /**
     * 查询历史办理情况显示
     *
     * @param paramBean
     * @return
     */
    public OutBean getViewGain(ParamBean paramBean) {
        //获取立项单主键
        String appro_id = paramBean.getStr("APPRO_ID");

        //获取查询类型（署发，司内，要点类）；
        String servId = paramBean.getStr("servId");

        //获取当前用户信息
        UserBean userBean = Context.getUserBean();
        //获取用户部门编码
        String deptCode = userBean.getDeptCode();

        //获取用的父级部门code
        String parent = userBean.getTDeptCode();

        //根据类型查询不同sql得到不同结果
        //构建sql语句
        StringBuffer sql = new StringBuffer();
        //署发立项
        if (servId.equals("OA_SUP_APPRO_OFFICE")){
            sql.append("select * from SUP_APPRO_GAIN WHERE APPRO_ID = '")
                    .append(appro_id)
                    .append("' and DEPT_CODE = '")
                    .append(parent);
            //司内
        }else if (servId.equals("OA_SUP_APPRO_BUREAU")){
            sql.append("select * from SUP_APPRO_GAIN WHERE APPRO_ID = '")
                    .append(appro_id)
                    .append("' and DEPT_CODE = '")
                    .append(deptCode);
            //要点类
        }else if (servId.equals("OA_SUP_APPRO_POINT")){
            sql.append("select * from SUP_APPRO_GAIN where APPRO_ID = '")
                    .append(appro_id);
        }
        sql.append("' and GAIN_STATE = '3' order by  TO_DATE(GAIN_MONTH,'yyyy-MM' ) desc");

        //执行sql语句得到结果
        List<Bean> result = Transaction.getExecutor().query(sql.toString());

        //构建返回值参数
        OutBean outBean = new OutBean();
        outBean.set("data", result);

        return outBean;
    }

}
