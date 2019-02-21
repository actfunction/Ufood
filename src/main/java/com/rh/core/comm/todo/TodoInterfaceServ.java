package com.rh.core.comm.todo;

import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.workday.WorkTime;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 待阅待办服务类
 * 
 * @author wanglong
 * 
 */
public class TodoInterfaceServ extends CommonServ {
    /** 主委托服务 */
    public static final String AGENT_MAIN_SERV = "SY_ORG_USER_TYPE_AGENT";
    
    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 
     */
    public void beforeQuery(ParamBean paramBean) {
        StringBuilder strWhere = new StringBuilder();
        String ownerCode = paramBean.getStr("OWNER_CODE");
        //查询委托
        if (paramBean.containsKey("agentFlag") && paramBean.getBoolean("agentFlag")) {
            String servCode = paramBean.getServId();
            //指定待办类型
            OutBean agtWhereBean = ServMgr.act(AGENT_MAIN_SERV, "getTodoAgentWhereByUserCode", paramBean);
            if (!agtWhereBean.isOk()) {
                throw new TipException("获取委办记录失败");
            }
            strWhere.append(agtWhereBean.getData());
            paramBean.setServId(servCode);
          //查询本人
        } else {        
            strWhere.append(" and OWNER_CODE = '" + ownerCode + "'");
            //待阅
            if (paramBean.getStr("TODO_TYPE").equals("OA_DY")) {
                strWhere.append(" and TODO_CATALOG = " + Constant.NO_INT);
            //待办
            } else {
                strWhere.append(" and TODO_CATALOG <= " + Constant.YES_INT);
            }
        }
        //UserBean user = UserMgr.getUser(ownerCode);
        //strWhere.append(" and QUERY_ODEPT = '" + user.getODeptCode() + "'");
        String extWhere = paramBean.getStr("_extWhere");
        extWhere = extWhere + strWhere.toString();
        paramBean.set("_extWhere", extWhere);
    }
    
    /**
     * 查询后添加查询条件
     * 
     * @param paramBean 
     * @param outBean 查询结果
     */
    public void afterQuery(ParamBean paramBean, OutBean outBean) {
        List<Bean> dataList = outBean.getList(Constant.RTN_DATA);
        if (dataList.size() > 0) {
            for (Bean data : dataList) {
                WorkTime workTime = new WorkTime();
                String sendTime = data.getStr("TODO_SEND_TIME"); // 发送时间
                String deadLine = data.getStr("TODO_DEADLINE1"); // 规定期限
                if (sendTime.length() > 0 && deadLine.length() > 0) {
                    String currTime = DateUtils.getDatetime(); // 当前时间
                    //long stdWasteTime = workTime.calWorktime(Context.getCmpy(), sendTime, deadLine); // 规定耗时
                    long realWasteTime = workTime.calWorktime(Context.getCmpy(), sendTime, currTime); // 实际耗时
                    data.set("TODO_WASTETIME_S", DurationFormatUtils.formatDuration(realWasteTime * 60000,
                            "dd'天'HH'小时'mm'分钟'")); // 节点耗时
                    long overTimeByNorm = DateUtils.getDiffTime(deadLine, currTime); // 按正常时算
                    // long overTimeByWork = realWasteTime - stdWasteTime; //按工作时算
                    if (overTimeByNorm > 0) {
                        data.set("TODO_OVERTIME_S", DurationFormatUtils.formatDuration(overTimeByNorm,
                                "'超时'dd'天'HH'小时'mm'分钟'")); // 超出时间
                        data.set("overTimeFlag", "true"); // 超时标识，展示时用
                    } else {
                        data.set("TODO_OVERTIME_S", DurationFormatUtils.formatDuration(Math.abs(overTimeByNorm),
                                "'剩余'dd'天'HH'小时'mm'分钟'")); // 剩余时间
                    }
                }
            }
        }
    }
}
