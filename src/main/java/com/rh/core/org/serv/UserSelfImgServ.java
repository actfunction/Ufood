package com.rh.core.org.serv;

import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.DateUtils;

/**
 * 用户头像处理类
 * @author wanglong
 * 
 */
public class UserSelfImgServ extends UserSelfInfoServ {

    /**
     * 重载父类的保存方法
     * @param paramBean 参数信息
     * @return outBean 参数信息
     */
    public OutBean save(ParamBean paramBean) {
        String servId = paramBean.getServId();
        ServDefBean servDef = ServUtils.getServDef(servId);
        // 缺省只能保存自己的头像
        UserBean userBean = Context.getUserBean();
        paramBean.setId(userBean.getCode());
        // 用户头像id
        String userImg = "";
        // 如果系统中无法获取，我们将从参数中获取(第一次上传)
        if (paramBean.isNotEmpty("USER_IMG_SRC")) {
            userImg = paramBean.getStr("USER_IMG_SRC"); 
        } else {
            userImg = userBean.getStr("USER_IMG_SRC"); 
        }
        
        if (-1 < userImg.lastIndexOf(",")) {
            userImg = userImg.substring(0, userImg.lastIndexOf(","));
        }

        // 头像截取坐标
        int x1 = paramBean.get("x1", -1);
        int y1 = paramBean.get("y1", -1);
        int x2 = paramBean.get("x2", -1);
        int y2 = paramBean.get("y2", -1);

        //坐标均不为空,将进行截取,并更新
        if (x1 > -1 && y1 > -1 && x2 > -1 && y2 > -1) {
            // 根据坐标计算出宽、高
            int width = x2 - x1;
            int height = y2 - y1;
            FileMgr.createIconImg(userImg, x1, y1, width, height);
        }
        paramBean.set("S_MTIME", DateUtils.getDatetimeTS());
        OutBean outBean = ServMgr.act(ServMgr.SY_ORG_USER, ServMgr.ACT_SAVE, paramBean);
        if (outBean.isOk()) { //保存成功清除用户缓存已经对应字典缓存
            UserMgr.clearSelfUserCache();
            servDef.clearDictCache();
        }
        return outBean;
    }
}
