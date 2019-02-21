package com.rh.core.serv.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 服务监听管理器
 * @author wanglong
 */
public class ServLisMgr {
    private static Log log = LogFactory.getLog(ServLisMgr.class);
    /**
     * 监听map
     */
    private Map<String, List<ServListener>> lisMap =
            Collections.synchronizedMap(new HashMap<String, List<ServListener>>());
    private static ServLisMgr lisMgr = new ServLisMgr();

    /**
     * 获取实例
     * @return 实例
     */
    public static ServLisMgr getInstance() {
        return lisMgr;
    }

    /**
     * 初始化
     */
    public void init() {
        synchronized (lisMap) {
            lisMap.clear();
            List<Bean> list = Transaction.getExecutor().
                    query("select * from SY_SERV_LISTENER where S_FLAG='1' order by SERV_ID,LIS_SORT");
            List<ServListener> listeners = null;
            String preServ = "", serv;
            for (Bean bean : list) {
                serv = bean.getStr("SERV_ID");
                if (!serv.equals(preServ)) {
                    listeners = new ArrayList<ServListener>();
                    lisMap.put(serv, listeners);
                    preServ = serv;
                }
                try {
                    listeners.add(new ServListener(serv, bean.getStr("LIS_CLASS"), bean.getStr("LIS_CONF")));
                } catch (Exception e) {
                    log.error("实例化监听类错误:" + e.getMessage());
                }
            }
        }
    }

    /**
     * 设置监听
     * @param serv 服务
     * @param cls 类
     * @param conf 配置
     */
    public void setListener(String serv, String[] cls, String[] conf) {
        synchronized (lisMap) {
            List<ServListener> list = lisMap.get(serv);
            if (list == null) {
                list = new ArrayList<ServListener>();
                lisMap.put(serv, list);
            } else {
                list.clear();
            }
            for (int i = 0; i < cls.length; i++) {
                try {
                    list.add(new ServListener(serv, cls[i], conf[i]));
                } catch (Exception e) {
                    log.error("实例化监听类错误:" + e.getMessage());
                }
            }
        }
    }

    /**
     * before监听
     * @param serv 服务
     * @param act 操作
     * @param param 参数
     * @throws Exception 例外
     */
    public void before(String serv, String act, ParamBean param) throws Exception {
        List<ServListener> list = lisMap.get(serv);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).before(act, param);
            }
        }
    }

    /**
     * after监听
     * @param serv 服务
     * @param act 操作
     * @param param 参数
     * @param out 结果
     * @throws Exception 例外
     */
    public void after(String serv, String act, ParamBean param, OutBean out) throws Exception {
        List<ServListener> list = lisMap.get(serv);
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                list.get(i).after(act, param, out);
            }
        }
    }
}
