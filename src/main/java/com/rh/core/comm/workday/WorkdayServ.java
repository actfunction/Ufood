package com.rh.core.comm.workday;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;

/**
 * 日期设置
 * @author wanglong
 *
 */
public class WorkdayServ extends CommonServ {
    /**
     * 日期设置页面
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean show(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        outBean.setToDispatcher("/sy/comm/workday/workday.jsp");
        return outBean;
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 初始化考勤设置
     */
    public OutBean initWorkDay(ParamBean paramBean) {
        int year = paramBean.getInt("YEAR");
        
        for (int i = 1; i < 13; i++) {
            List<Bean> dayList = new ArrayList<Bean>();
            
            int dayCount = DateUtils.getDayOfMonth(i, year);
            
            for (int j = 1; j <= dayCount; j++) {
                Bean day = new Bean();
                String dateStr = year + "-" + convert(i) + "-" + convert(j);
                
                day.set("DAY_SPECIAL_DATE", dateStr);
                day.set("S_CMPY", Context.getCmpy());
                
                //如果已经设置了，就不再次进行添加了
                int count = ServDao.count(WorkDay.SY_COMM_WORK_DAY_SERV, day);
                if (count > 0) {
                    continue;
                }
                
                int dayOfWeek = WorkDay.getDayOfWeek(dateStr);
                if (dayOfWeek == 1 || dayOfWeek == 7) {
                    day.set("DAY_FLAG", WorkDay.WORK_DAY_NOT); //非工作日
                } else {
                    day.set("DAY_FLAG", WorkDay.WORK_DAY); //工作日
                }
                
                dayList.add(day);
            }
            
            ServDao.creates(WorkDay.SY_COMM_WORK_DAY_SERV, dayList);
        }
        
        OutBean outBean = new OutBean();
        outBean.setOk();
        return outBean;
    }
    

    /**
     * 
     * @param num 数字
     * @return 不足10的数字前面补0
     */
    private String convert(int num) {
        if (num < 10) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 初始化考勤设置
     */
    public OutBean changeDateFlag(ParamBean paramBean) {
        String dateStr = paramBean.getStr("DATE");
        int oldDateFlag = paramBean.getInt("DAY_FLAG");
        
        Bean queryBean = new Bean();
        queryBean.set("DAY_SPECIAL_DATE", dateStr);
        Bean dateBean = ServDao.find(WorkDay.SY_COMM_WORK_DAY_SERV, queryBean);
        
        if (null == dateBean) {
            String year = dateStr.split("-")[0];
            
            throw new TipException("日期" + dateStr + "不存在,请初始化 " + year + "年度的工作日信息。");
        }
        
        if (oldDateFlag == WorkDay.WORK_DAY) {
            dateBean.set("DAY_FLAG", WorkDay.WORK_DAY_NOT);
        } else {
            dateBean.set("DAY_FLAG", WorkDay.WORK_DAY);
        }
        
        ServDao.update(WorkDay.SY_COMM_WORK_DAY_SERV, dateBean);
        
        OutBean outBean = new OutBean();
        outBean.setOk();
        return outBean;
    }
    
}
