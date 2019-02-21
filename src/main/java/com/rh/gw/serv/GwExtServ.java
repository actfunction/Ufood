package com.rh.gw.serv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rh.gw.util.GwConstant;
import com.rh.gw.util.GwOfdUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;

/***
 * 公文CommonServ类，所有公文扩展类都继承此类 公共方法写在这里，单独处理的写在扩展类里。
 *
 * @author WeiTl
 * @version 1.0
 *
 */
public class GwExtServ extends CommonServ {
    /*** 记录历史 */
    private static Log log = LogFactory.getLog(GwExtServ.class);


    public OutBean saveYwfwInfo(ParamBean param) {
        OutBean out = new OutBean();
        ServDao.save(param.getServId(), param);

        return out;
    }

    /*** 根据流程实例ID和节点ID获得按钮权限 */
    public OutBean getAuthority(ParamBean paramBean) {
        OutBean out = new OutBean();
        String pId = paramBean.getStr("pId");// 实例ID
        String nId = paramBean.getStr("nId");// 节点ID

        SqlExecutor executor = Transaction.getExecutor();
        if (!pId.equals("") && !nId.equals("")) {
            String sql = "SELECT au.* " + "FROM SY_WFE_WPS_AUTHORITY au,SY_WFE_NODE_INST_ALL_V node,SY_WFE_PROC_INST_ALL_V proc "
                    + "WHERE node.NODE_CODE=au.NODE_CODE " + "AND proc.PROC_CODE=au.PROC_CODE " + "AND node.NI_ID='"
                    + nId + "' " + "AND proc.PI_ID='" + pId + "'";
            try {
                Bean authority = executor.queryOne(sql);
                out.set("authority", authority);
            } catch (Exception e) {
                out.setError("查询失败");
            }
        }
        return out;
    }


    /**
     * 根据节点配置的Bean参数获取领导对应的秘书
     * 并把秘书名称拼接在领导名称后面：领导名（秘书：秘书名）
     *
     * @param wfeConfig 参数Bean
     * @return outBean 将最终结果放入OutBean中返回
     */
    public OutBean getLeadCorSec(ParamBean wfeConfig) {
        OutBean out = new OutBean();
        List<Bean> leadList = new ArrayList<Bean>();
        String deptCodes = wfeConfig.getStr("deptCodeStr");

        if (deptCodes.indexOf(Constant.SEPARATOR) > 0) {
            deptCodes = deptCodes.replaceAll(Constant.SEPARATOR, "'" + Constant.SEPARATOR + "'");
        }

        StringBuilder condition = new StringBuilder(" and (DEPT_CODE in ('" + deptCodes + "')");
        condition.append(" or TDEPT_CODE in ('" + deptCodes + "'))");
        condition.append(" and S_FLAG = 1");
        List<Bean> userList = ServDao.finds(ServMgr.SY_ORG_USER, condition.toString());

        List<Bean> shipList = ServDao.finds("OA_GW_LEADER_MS_SHIP", "");
        for (Bean userBean : userList) {
            String userCode = userBean.getStr("USER_CODE");
            for (Bean leadBean : shipList) {
                String leadCode = leadBean.getStr("LEADER_USER_CODE");
                if (userCode.equals(leadCode)) {
                    UserBean miShuBean = UserMgr.getUser(leadCode);
                    Bean lead = new Bean(miShuBean);
                    String showName = userBean.getStr("USER_NAME") + "（秘书：" + leadBean.getStr("MS_NAME") + "）";
                    lead.set("USER_NAME", showName);
                    lead.set("TRANSID", leadBean.getStr("MS_ID"));
                    leadList.add(lead);
                    break;
                }
            }
        }
        log.debug(DateUtils.getDatetime() + "执行getLeadCorSec领导名（秘书：秘书名）方法。");
        return out.setData(leadList);
    }

    /**
     * 废止按钮
     *
     * @param paramBean
     * @return
     */
    public OutBean abolishData(ParamBean paramBean) {
        OutBean out = new OutBean();
        String dataId = paramBean.getStr("DATA_ID"); // 获取数据id
        String servId = paramBean.getStr("SERV_ID"); // 获取数据id

        UserBean user = Context.getUserBean();
        Bean whereBean = new Bean();
        whereBean.set("SERV_ID", servId);
        whereBean.set("TODO_OBJECT_ID1", dataId);
        whereBean.set("OWNER_CODE", user.getCode());
        ServDao.destroy(GwConstant.SY_TODO, whereBean);
        ServDao.destroys(GwConstant.SY_TODO, whereBean);

        // 执行sql语句
        return out.setOk();
    }

    /**
     * 添加留言
     *
     * @param bean 当前流程信息和留言
     * @return
     * @author yxb
     */
    public OutBean addLiuYan(ParamBean bean) {
        OutBean out = new OutBean();
        try {
            String aTime = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss").format(new Date());
            bean.set("MIND_TIME", aTime);
            bean.set("MIND_ID", Lang.getUUID());
            ServDao.save("SY_COMM_MIND", bean);
            return out.setOk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.setError("数据异常,请联系管理员!");
    }

    /**
     * 查看留言的权限
     *
     * @param bean
     * @return
     */
    public OutBean getQx(ParamBean bean) {
        OutBean out = new OutBean();
        String mindId = (String) bean.get("mindId");
        Bean mind = ServDao.find("SY_COMM_MIND", mindId);
        return out.setData(mind);
    }


    /**
     * 根据user_code获得签名和用户姓名
     *
     * @param
     * @return
     */
    public OutBean getSign(ParamBean paramBean) {
        OutBean out = new OutBean();
        String uId = paramBean.getStr("uId");
        SqlExecutor executor = Transaction.getExecutor();
        if (!uId.equals("")) {
            String sql = "SELECT USER_NAME,USER_AUTOGRAPH FROM SY_ORG_USER WHERE USER_CODE='" + uId + "'";
            try {
                Bean user = executor.queryOne(sql);
                out.set("user", user);
            } catch (Exception e) {
                out.setError("查询失败");
            }
        }
        return out;
    }


    /**
     * 转密网
     *
     * @param
     * @return
     */
    public OutBean zhuanMiWang(ParamBean paramBean) {
        OutBean out = new OutBean();
        String dataId = paramBean.getStr("dataId");
        if (!dataId.equals("")) {
            SqlExecutor executor = Transaction.getExecutor();
            String updateGw = "update OA_GW_GONGWEN set S_FLAG=3 WHERE GW_ID='" + dataId + "'";
            String updateTodo = "update SY_COMM_TODO set S_FLAG=3 WHERE TODO_OBJECT_ID1='" + dataId + "'";
            try {
                Transaction.begin();
                int i = executor.execute(updateGw);
                if (i > 0) {
                    int j = executor.execute(updateTodo);
                    if (j >= 0) {
                        Transaction.commit();
                        out.setMsg("OK");
                    } else {
                        Transaction.rollback();
                    }
                } else {
                    Transaction.rollback();
                }
            } catch (Exception e) {
                out.setError("转密网失败");
            } finally {
                Transaction.end();
            }
        }
        return out;
    }

    /**
     * 修改数据库某个字段的值
     *
     * @param
     * @return OutBean
     */
    public OutBean updateFieldValue(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String servId = "OA_GW_GONGWEN";
        String dataId = paramBean.getStr("dataId");//主键
        String key = paramBean.getStr("key");//字段
        String value = paramBean.getStr("value");//值

        try {

            SqlExecutor se = Context.getExecutor();//获取SqlExecutor对象，用于执行sql语句
            String orgSumSql = "update OA_GW_GONGWEN set " + key + " = " + value + " WHERE GW_ID='" + dataId + "'";
            int successCount = se.execute(orgSumSql);
            System.out.println(successCount);
        } catch (Exception e) {
            outBean.setError("执行错误");
            e.printStackTrace();
        }
        return outBean;
    }

    /**
     * DOC文件转OFD
     *
     * @param paramBean 传入参数 需要包含 Bean: zhengwen 和 zhengwen的fileId
     * @return
     */
    public OutBean cov(ParamBean paramBean) {
//            获得正文ID
        String fileId = paramBean.getBean("zhengwen").getStr("fileId");
        //        调用转OFD方法
        OutBean cov = GwOfdUtil.cov(paramBean);
//        将原文件信息迁入历史版本表中
        if (cov.getStr("_MSG_").length() == 0) {
            try {
//                FileMgr.hongTouFileSave(fileId);
//            ServDao.delete("SY_COMM_FILE", fileId);
            } catch (Exception e) {
//            e.printStackTrace();
            }
        }

        return cov;
    }    
    
}
