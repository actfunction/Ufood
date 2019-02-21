package com.rh.core.comm.todo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.send.SendUtils;
import com.rh.core.util.Constant;

/**
 * 
 * @author wanglong
 *
 */
public class TodoReadServ extends CommonServ {

    /**
     * 
     * @param paramBean Bean
     * @return Bean
     */
    public Bean checkRecipt(Bean paramBean) {

        // 获取要删除的记录的id。
        String[] toDoID = paramBean.getId().split(",");

        // 定义一个ArrayList 用来存储待办表中的数据，每一条数据存储到一个Bean中，该Bean存储的值根据TODO_ID取得。
        ArrayList<Bean> beanList = new ArrayList<Bean>();

        // 根据获取的TODO_ID循环去SY_COMM_TODO表中查询数据。
        for (int i = 0; i < toDoID.length; i++) {
            Bean todoBean = new Bean();

            // 将TODO_ID拆分出来，
            todoBean.set("TODO_ID", toDoID[i]);

            // 根据TODO_ID去查询内容。并将结果存储到ArrayList中。
            beanList.add(ServDao.find(ServMgr.SY_COMM_TODO, todoBean));
        }

        // 从ArrayList中循环去Bean.Bean中保存的是SY_COMM_TODO表中的数据，从数据中获取TODO_OBJECT_ID2的值，该值对应SY_COMM_SEND_DETAIL表中的SEND_ID
        for (int j = 0; j < beanList.size(); j++) {
            Bean todoBean = beanList.get(j);
            // 获取TODO_OBJECT_ID2的值，该值为SY_COMM_SEND_DETAIL表中的SEND_ID
            String sendId = todoBean.getStr("TODO_OBJECT_ID2");

            // 如果在TODO_OBJECT_ID2的值为空，则sendId也为空，则抛出异常
            if (StringUtils.isEmpty(sendId)) {
                throw new TipException("没取到分发ID，不能签收");
            }

            // 调用“签收”方法签收文件
            SendUtils.qianShou(sendId);

        }
        return new Bean().set(Constant.RTN_MSG, Constant.RTN_MSG_OK);
    }

    /**
     * 
     * @param paramBean bean
     * @return Bean 
     */
    public Bean readAll(Bean paramBean) {
        
        //获取userBean对象，的到用户信息
        UserBean currentUser = Context.getUserBean();

        String userName = currentUser.getCode();

        // 定义一个ArrayList 用来存储待办表中的数据，每一条数据存储到一个Bean中，该Bean存储的值根据TODO_ID取得。
        List<Bean> beanList = new ArrayList<Bean>();

        // 定义查询条件的字段
        String code = "SY_COMM_SEND_DETAIL";

        // 拼凑查询语句
        String whereString = " AND TODO_CODE = '" + code + "'" + " AND OWNER_CODE = '" + userName + "'";

        // 根据where条件（where TODO_CODE = 'SY_COMM_SEND_DETAIL'）查询出SY_COMM_TODO表中的所有符合条件的数据
        beanList = ServDao.finds(ServMgr.SY_COMM_TODO, whereString);

        // 从ArrayList中循环去Bean.Bean中保存的是SY_COMM_TODO表中的数据，从数据中获取TODO_OBJECT_ID2的值，该值对应SY_COMM_SEND_DETAIL表中的SEND_ID
        for (int j = 0; j < beanList.size(); j++) {

            Bean todoBean = beanList.get(j);

            // 获取TODO_OBJECT_ID2的值，该值为SY_COMM_SEND_DETAIL表中的SEND_ID
            String sendId = todoBean.getStr("TODO_OBJECT_ID2");

            // 如果在TODO_OBJECT_ID2的值为空，则sendId也为空，则抛出异常
            if (StringUtils.isEmpty(sendId)) {
                throw new TipException("没取到分发ID，不能签收");
            }

            // 调用“签收”方法签收文件
            SendUtils.qianShou(sendId);

        }

        return new Bean().set(Constant.RTN_MSG, Constant.RTN_MSG_OK);
    }
}
